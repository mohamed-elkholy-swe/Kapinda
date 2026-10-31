package com.deliveryapp.restaurant;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

/** Public listing facts; null values mean not verified. */
@Embeddable
public class RestaurantDetails {
  @Column(length = 2000) public String photoUrl;
  @Column(length = 1000) public String photoSourceUrl;
  @jakarta.persistence.Convert(converter = StringListConverter.class)
  @Column(length = 20000) public java.util.List<String> menuImages;
  @Column(length = 2000) public String menuNote;
  @jakarta.persistence.Convert(converter = StringListConverter.class)
  @Column(length = 20000) public java.util.List<String> menuCategories;
  public Integer catalogVersion;
  public String phone;
  @Column(length = 1000) public String address;
  public Double latitude;
  public Double longitude;
  public String mapsUrl;
  public Integer reviewCount;
  public String openingHours;
  @Column(length = 1000) public String menuSourceUrl;
  public String verifiedOn;
  public Boolean orderingTermsVerified;
}
