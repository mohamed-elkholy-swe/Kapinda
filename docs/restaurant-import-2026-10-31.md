# Qanater restaurant listings — 31 October 2026

Source: the four Google Maps listings supplied by the user, inspected in browser.
Catalog: backend/src/main/resources/data/qanater-restaurants.json.

| Restaurant | Phone | Google rating | Reviews |
| --- | --- | --- | --- |
| فطاطري القليوبي | 01221304268 | 4.0 | 939 |
| مشويات الجلاد | 01111160100 | 3.9 | 75 |
| كبابجى اولاد مسعد بيومى | 01011113928 | 4.2 | 1101 |
| حلواني خير زاد | 01063674217 | 4.3 | 65 |

The catalog preserves addresses, coordinates, source links and observation date. Ratings and review counts are snapshots, not live values. Google Maps listed Qalyoubi and Khair Zad as open 24 hours. Weekly opening hours for the two grill restaurants are unverified; their currently displayed closing time was 3 AM.

Menu research found an El Galad listing at https://menuegypt.com/Mashwayat--El-Hag-Naser-El-Galad?menu=2 (updated 2019), and Khair Zad at https://menuegypt.com/ar/حلواني-خير-زاد/al-qanater-al-khaireya (matching Qanater phone). These are source references for the imported historical menus, not confirmation of current prices. At the user’s request, the Qalyoubi article’s old menu and selected dishes are now included, explicitly marked as potentially belonging to other branches. No current item-level menu and price list was verified for any of the four businesses.

No delivery fees, delivery estimates, minimum orders or owner accounts were invented. The required legacy minimum-order column uses 0 internally; imported listings mark orderingTermsVerified=false and customer views suppress that value. Delivery time is shown as unconfirmed. Menu prices have now been imported at the user’s explicit request despite being unverified; source notes are displayed on web and mobile. Confirm prices and portion sizes with the restaurant.

On the next backend startup, the seeder inserts missing catalog IDs even into a populated database. Catalog version 2 enriches existing IDs with media and fills only empty menus; subsequent startups preserve admin edits and menu deletions. Existing restaurants and orders are retained. New empty databases now receive these real listings instead of the previous fictional examples. Review older sample entries separately in administration if present.

## Photos and historical menus added

All four Google Maps lead photographs are displayed with source attribution. Remote images have load-error fallbacks and depend on the source remaining available. The Qalyoubi lead image is the pizza photograph shown on its listing; the other three show the storefronts.

Menu galleries contain 29 source images: Qalyoubi 3, El Galad 5, Bayoumi 1, Khair Zad 20. Menus open at full size and page selection loads one page at a time. Selected dishes are also imported as ordinary menu items: Qalyoubi 15, El Galad 7 historical offers, Bayoumi 21, Khair Zad 25 sweets (68 total). Descriptions state price/portion uncertainty; no current validity is asserted. The scans preserve the full sourced menus beyond the selected structured dishes.

Sources: https://www.delivery44.com/2021/09/Fatatry-Qalyubi-restaurant.html ; https://menuegypt.com/Mashwayat--El-Hag-Naser-El-Galad?menu=2 ; https://restaurantguru.com/kbabja-bywma-Egypt-2/menu ; https://menuegypt.com/ar/حلواني-خير-زاد/al-qanater-al-khaireya .

Validation: 23 backend tests pass, web production build passes, mobile TypeScript check passes. Local PostgreSQL startup and import succeeded on verification backend port 8081; preview frontend runs on port 5174. Existing processes on 8080 and 5173 were left running. Restart the normal backend to expose the new photo/gallery fields through its API.
