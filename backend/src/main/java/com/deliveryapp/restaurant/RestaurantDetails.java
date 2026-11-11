package com.deliveryapp.restaurant;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

/** Public listing facts; null values mean not verified. */
@Embeddable
public class RestaurantDetails {
  @Column(length = 2000) public String photoUrl;
  @Column(length = 1000) public String photoSourceUrl;
  @jakarta.persistence.Convert(converter = StringListConverter.class)
  @Column(length = 60000) public java.util.List<String> menuImages;
  @Column(length = 2000) public String menuNote;
  @jakarta.persistence.Convert(converter = StringListConverter.class)
  @Column(length = 20000) public java.util.List<String> menuCategories;
  @jakarta.persistence.Convert(converter = MenuCategoryDefinitionsConverter.class)
  @Column(length = 100000) public java.util.List<MenuCategoryDefinition> menuCategoryDefinitions;
  public Integer catalogVersion;
  public String phone;
  @Column(length = 1000) public String address;
  public Double latitude;
  public Double longitude;
  public String mapsUrl;
  public Integer reviewCount;
  public String openingHours;
  public String openingTime;
  public String closingTime;
  @Column(length = 1000) public String menuSourceUrl;
  public String verifiedOn;
  public Boolean orderingTermsVerified;
}
