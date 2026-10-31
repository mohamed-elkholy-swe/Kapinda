# Sample restaurant menus

At the user's request, SampleMenuSeeder adds six fictional dishes in three categories to every existing restaurant, and fills missing categories and item photos. It runs after the restaurant catalog import. Existing dish names, descriptions, prices, categories and photos are preserved; only missing category/photo fields are filled. Samples include a label in their names and descriptions. Images are representative Unsplash food photos, not actual restaurant product photos.

An individual restaurant's sampleMenuVersion marker makes this a one-time import, preserving subsequent admin edits or deletions. The marker applies to new restaurants on their next backend startup too.

Validated against the local database: all four restaurants have three categories, six new sample dishes each, and no items without photo URLs. Web build, mobile type check and backend tests passed.
