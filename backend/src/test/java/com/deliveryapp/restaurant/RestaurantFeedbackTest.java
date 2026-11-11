package com.deliveryapp.restaurant;

import com.deliveryapp.account.AccountService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RestaurantFeedbackTest {
  private final RestaurantRatingRepository ratings = mock(RestaurantRatingRepository.class);
  private final RestaurantRepository restaurants = mock(RestaurantRepository.class);
  private final AccountService accounts = mock(AccountService.class);
  private final RestaurantRatingService service = new RestaurantRatingService(ratings, restaurants, accounts);
  private final RestaurantEntity restaurant = new RestaurantEntity("r", "Kitchen", "food", "New", "30", 0, "restaurant");

  @Test void guestsCanReadFeedbackWithoutCustomerIdentifiers() throws Exception {
    when(restaurants.findById("r")).thenReturn(Optional.of(restaurant));
    var rating = new RestaurantRatingEntity(restaurant, "private-customer-uid", 4);
    rating.setFeedback("Great meal");
    when(ratings.publicFeedback("r", PageRequest.of(0, 10)))
      .thenReturn(new SliceImpl<>(List.of(rating), PageRequest.of(0, 10), true));
    MockMvcBuilders.standaloneSetup(new RestaurantFeedbackController(service)).build()
      .perform(get("/api/restaurants/r/feedback"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.items[0].stars").value(4))
      .andExpect(jsonPath("$.items[0].feedback").value("Great meal"))
      .andExpect(jsonPath("$.items[0].customerUid").doesNotExist())
      .andExpect(jsonPath("$.items[0].restaurant").doesNotExist())
      .andExpect(jsonPath("$.hasMore").value(true));
    verifyNoInteractions(accounts);
  }

  @Test void rejectsHiddenRestaurantsAndInvalidPages() {
    when(restaurants.findById("r")).thenReturn(Optional.of(restaurant));
    assertThrows(IllegalArgumentException.class, () -> service.publicFeedback("r", -1));
    restaurant.setActive(false);
    assertThrows(RestaurantNotFoundException.class, () -> service.publicFeedback("r", 0));
    assertThrows(RestaurantNotFoundException.class, () -> service.publicFeedback("missing", 0));
    verifyNoInteractions(ratings);
  }
}
