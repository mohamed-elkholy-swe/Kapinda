package com.deliveryapp.restaurant;

import com.deliveryapp.account.*;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantRatingService {
  public record RatingRequest(@NotNull @Min(1) @Max(5) Integer stars) {}
  public record RatingResponse(Integer myRating) {}
  private final RestaurantRatingRepository ratings;
  private final RestaurantRepository restaurants;
  private final AccountService accounts;

  public RestaurantRatingService(RestaurantRatingRepository ratings, RestaurantRepository restaurants, AccountService accounts) {
    this.ratings = ratings; this.restaurants = restaurants; this.accounts = accounts;
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
    return new RatingResponse(ratings.findByRestaurantIdAndCustomerUid(id, user.uid()).map(RestaurantRatingEntity::getStars).orElse(null));
  }

  @Transactional
  public RatingResponse rate(AuthenticatedUser user, String id, int stars) {
    requireCustomer(user);
    if (stars < 1 || stars > 5) throw new IllegalArgumentException("Rating must be between 1 and 5");
    // Serialize submissions per restaurant so counts and averages cannot drift.
    var restaurant = restaurants.findForRating(id).orElseThrow(() -> new RestaurantNotFoundException(id));
    if (!restaurant.isActive()) throw new RestaurantNotFoundException(id);
    var rating = ratings.findByRestaurantIdAndCustomerUid(id, user.uid())
      .orElseGet(() -> new RestaurantRatingEntity(restaurant, user.uid(), stars));
    rating.setStars(stars);
    ratings.saveAndFlush(rating);
    restaurant.updateCustomerRating(ratings.averageForRestaurant(id), ratings.countByRestaurantId(id));
    return new RatingResponse(stars);
  }
}
