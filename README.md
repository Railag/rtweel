Rtweel
======
Lightweight Twitter application available even for old android devices starting from android 3.0 version (HONEYCOMB). 

Already done:
- Twitter OAuth authentification through integrated twitter4j library, initial browser login
- Home timeline, which is start screen
- Profile section with 5 different timelines for each user, which you can swipe by viewpager, with header hiding animation after swiping down
- Moving to profile via user's logos/ replies in tweets
- Direct Messages
- Search (both users & tweets)
- Trends
- HashTags
- Followers
- Replying on tweets function
- Navigation via navigation drawer, with toggle and its title animation
- Toolbar 
- Floating up button for scrolling to the beginning
- RecyclerView wrapping the timeline
- CardView wrapping tweet's text
- Round profile logos
- Notifications which check updates on your twitter and download tweets, cyan LED color
- Some buttons for manual updating timelines, posting tweets, etc.
- Up/bottom swipes for timeline updating
- Caching profile images, so the second and later launches will be faster.
- Caching your downloaded timeline into SQLite DB and retrieving it from if there are any tweets cached.
- Timeline updating animation (circle).
- Using Picasso library for async image downloading & caching
- Dialogs for loading moments
- Icons
- Some fragments for tweet posting, for detail tweet view, for files, etc.
- Using support library, 3.0 > version support.

TODO:
- ~~Tweet UI (date parsing, text assignment)~~
- ~~Timeline updating mechanism improving~~
- ~~Loading animation on app start~~
- ~~Something with first app pre-login activity~~
- ~~Improving the detail view tweet window~~
- ~~Adding network exceptions avoiding~~, ~~screen rotating info saving~~
- ~~Logout~~
- ~~Followers~~
- ~~Direct messages~~
- ~~Mentions~~
- ~~Trends~~
- ~~Paging system improvement~~
- ~~Tweet deletion~~
- Last tweet edition
- ~~links~~ ~~and pictures in tweets~~
- ~~some effects~~
- ~~localization (EN/RU)~~
- ~~File choosing section improvements~~
- ~~Update token exception fix~~
- ~~Round image views for profile images~~
- ~~All media resources processing in detail screen~~
- ~~Changing activity structure to fully fragments based~~
- ~~Improving app behavior with bad connections~~
- ~~Improving timeline system~~
- ~~Replacing rotate animation with something more smoother~~
- Adding webview for urls handling // some bugs there
- ~~Replacing standard menu with nav drawer one~~
- Add view pager for detail tweet scrolling
- ~~Replace main listview with recyclerview~~
- ~~Adding floating button to scroll up and down on timeline~~
- ~~Profile section implementation~~
- ~~PN system changes + settings~~:
-  ~~- settings for PN to disable all-time notifications about new tweets~~
-  ~~- PN for new tweet with user's mention and opening this tweet detail~~
-  ~~- PN for new tweet with direct message and opening this direct message~~
- ~~Smooth Progress Bar for all loadings~~
- ~~Show profile by name in nav drawer~~
- ~~Search~~
- ~~Empty content processing (no messages / no tweets in timelines etc)~~
- ~~Progress Bar cancelling fix~~
- ~~New tweets messages within viewpager visibility bug~~
- Main app icon
- App icons
- Some design things
- Direct messages design update
- Retain fragment fix for detail image
- Add search under mentions timeline for fetching


Note: To launch this, you'll need to get your own consumer key and consumer secret for twitter app at dev.twitter.com and put them into app/src/main/com.rtweel/Const.java class.

