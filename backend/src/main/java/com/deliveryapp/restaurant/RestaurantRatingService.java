package com.deliveryapp.restaurant;

import com.deliveryapp.account.*;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantRatingService {
  public record RatingRequest(@NotNull @Min(1) @Max(5) Integer stars, @Size(max = 2000) String feedback) {
    public RatingRequest(Integer stars) { this(stars, null); }
  }
  public record RatingResponse(Integer myRating, String feedback) {}
  private final RestaurantRatingRepository ratings;
  private final RestaurantRepository restaurants;
  private final AccountService accounts;

  public RestaurantRatingService(RestaurantRatingRepository ratings, RestaurantRepository restaurants, AccountService accounts) {
    this.ratings = ratings; this.restaurants = restaurants; this.accounts = accounts;
  }

  public record PublicFeedback(int stars, String feedback, java.time.Instant updatedAt) {}
  public record FeedbackPage(java.util.List<PublicFeedback> items, boolean hasMore) {}

  @Transactional(readOnly = true)
  public FeedbackPage publicFeedback(String id, int page) {
    var restaurant = restaurants.findById(id).orElseThrow(() -> new RestaurantNotFoundException(id));
    if (!restaurant.isActive()) throw new RestaurantNotFoundException(id);
    if (page < 0) throw new IllegalArgumentException("Page must be zero or greater");
    var results = ratings.publicFeedback(id, org.springframework.data.domain.PageRequest.of(page, 10));
    return new FeedbackPage(results.stream().map(r -> new PublicFeedback(r.getStars(), r.getFeedback(), r.getUpdatedAt())).toList(), results.hasNext());
  }

  private void requireCustomer(AuthenticatedUser user) {
    if (accounts.requireActive(user).getRole() != AccountRole.CUSTOMER) {
      throw new SecurityException("Only customers can rate restaurants");
    }
  }

  @Transactional(readOnly = true)
  public RatingResponse mine(AuthenticatedUser user, String id) {
    requireCustomer(user);
    var restaurant = restaurants.findById(id).orElseThrow(() -> new RestaurantNotFoundException(id));
    if (!restaurant.isActive()) throw new RestaurantNotFoundException(id);
    var rating = ratings.findByRestaurantIdAndCustomerUid(id, user.uid());
    return new RatingResponse(rating.map(RestaurantRatingEntity::getStars).orElse(null), rating.map(RestaurantRatingEntity::getFeedback).orElse(""));
  }

  @Transactional
  public RatingResponse rate(AuthenticatedUser user, String id, int stars) {
    return rate(user, id, stars, null);
  }

  @Transactional
  public RatingResponse rate(AuthenticatedUser user, String id, int stars, String feedback) {
    requireCustomer(user);
    if (feedback != null && feedback.length() > 2000) throw new IllegalArgumentException("Feedback must be at most 2000 characters");
    if (stars < 1 || stars > 5) throw new IllegalArgumentException("Rating must be between 1 and 5");
    // Serialize submissions per restaurant so counts and averages cannot drift.
    var restaurant = restaurants.findForRating(id).orElseThrow(() -> new RestaurantNotFoundException(id));
    if (!restaurant.isActive()) throw new RestaurantNotFoundException(id);
    var rating = ratings.findByRestaurantIdAndCustomerUid(id, user.uid())
      .orElseGet(() -> new RestaurantRatingEntity(restaurant, user.uid(), stars));
    rating.setStars(stars);
    if (feedback != null) rating.setFeedback(feedback.trim());
    ratings.saveAndFlush(rating);
    restaurant.updateCustomerRating(ratings.averageForRestaurant(id), ratings.countByRestaurantId(id));
    return new RatingResponse(stars, rating.getFeedback());
  }
}
