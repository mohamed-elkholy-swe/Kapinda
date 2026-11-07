package com.deliveryapp.restaurant;

import com.deliveryapp.account.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantRatingTest {
  private final RestaurantRatingRepository ratings = mock(RestaurantRatingRepository.class);
  private final RestaurantRepository restaurants = mock(RestaurantRepository.class);
  private final AccountService accounts = mock(AccountService.class);
  private final RestaurantRatingService service = new RestaurantRatingService(ratings, restaurants, accounts);
  private final AuthenticatedUser user = new AuthenticatedUser("customer", "customer@example.test");
  private final RestaurantEntity restaurant = new RestaurantEntity("r", "Kitchen", "food", "3.9", "30 min", 0, "restaurant");

  private void allowCustomer() {
    when(accounts.requireActive(user)).thenReturn(new AccountEntity("customer", "customer@example.test", AccountRole.CUSTOMER, AccountStatus.ACTIVE));
    when(restaurants.findById("r")).thenReturn(Optional.of(restaurant));
    when(restaurants.findForRating("r")).thenReturn(Optional.of(restaurant));
  }

  @Test void createsRatingAndUpdatesExistingVoteWithoutAddingAnother() {
    allowCustomer();
    when(ratings.averageForRestaurant("r")).thenReturn(4.5);
    when(ratings.countByRestaurantId("r")).thenReturn(2L);
    assertNull(service.mine(user, "r").myRating());
    assertEquals(5, service.rate(user, "r", 5).myRating());
    assertEquals("4.5", restaurant.getRating());
    assertEquals(2, restaurant.getRatingCount());
    var rating = org.mockito.ArgumentCaptor.forClass(RestaurantRatingEntity.class);
    verify(ratings).saveAndFlush(rating.capture());
    when(ratings.findByRestaurantIdAndCustomerUid("r", user.uid())).thenReturn(Optional.of(rating.getValue()));
    when(ratings.averageForRestaurant("r")).thenReturn(3.0);
    assertEquals(2, service.rate(user, "r", 2).myRating());
    assertEquals(2, rating.getValue().getStars());
    assertEquals(2, service.mine(user, "r").myRating());
    assertEquals("3.0", restaurant.getRating());
    assertEquals(2, restaurant.getRatingCount());
    verify(ratings, times(2)).saveAndFlush(rating.getValue());
  }

  @Test void rejectsStaffInactiveRestaurantsAndInvalidScores() {
    for (var role : List.of(AccountRole.ADMIN, AccountRole.SUB_ADMIN, AccountRole.RESTAURANT, AccountRole.DELIVERY)) {
      when(accounts.requireActive(user)).thenReturn(new AccountEntity("staff", "staff@example.test", role, AccountStatus.ACTIVE));
      assertThrows(SecurityException.class, () -> service.rate(user, "r", 5));
      assertThrows(SecurityException.class, () -> service.mine(user, "r"));
    }
    allowCustomer();
    assertThrows(IllegalArgumentException.class, () -> service.rate(user, "r", 0));
    assertThrows(IllegalArgumentException.class, () -> service.rate(user, "r", 6));
    restaurant.setActive(false);
    assertThrows(RestaurantNotFoundException.class, () -> service.rate(user, "r", 5));
    assertThrows(RestaurantNotFoundException.class, () -> service.mine(user, "r"));
    verifyNoInteractions(ratings);
  }

  @Test void publicSummaryUsesCustomerRatingsAndKeepsImportedScoresSeparate() {
    when(restaurants.findById("r")).thenReturn(Optional.of(restaurant));
    var service = new RestaurantService(restaurants, mock(MenuItemRepository.class));
    assertEquals("New", service.findPublicById("r").rating());
    assertEquals(0, service.findPublicById("r").ratingCount());
    restaurant.updateCustomerRating(4.25, 4);
    assertEquals("4.3", service.findPublicById("r").rating());
    assertEquals(4, service.findPublicById("r").ratingCount());
  }

  @Test void validatesRequiredWholeStarRange() {
    try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      assertFalse(validator.validate(new RestaurantRatingService.RatingRequest(null)).isEmpty());
      assertFalse(validator.validate(new RestaurantRatingService.RatingRequest(0)).isEmpty());
      assertFalse(validator.validate(new RestaurantRatingService.RatingRequest(6)).isEmpty());
      for (int stars = 1; stars <= 5; stars++) assertTrue(validator.validate(new RestaurantRatingService.RatingRequest(stars)).isEmpty());
    }
  }
}
