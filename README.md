Rtweel
======
Lightweight Twitter application available even for old android devices starting from android 3.0 version (HONEYCOMB). 

Already done:
- Twitter OAuth authentification through integrated twitter4j library, initial browser login
- Navigation via navigation drawer, with toggle and its title animation
- Toolbar 
- Floating up button for scrolling to the beginning
- RecyclerView wrapping the timeline
- CardView wrapping tweet's text
- Round profile logos
- Notifications which check updates on your twitter and download tweets, cyan LED color
- One timeline, which can contain both your tweets or your home timeline's. 
- Some buttons for manual updating timelines, posting tweets, etc.
- Up/bottom swipes for timeline updating
- Caching profile images, so the second and later launches will be faster.
- Caching your downloaded timeline into SQLite DB and retrieving it from if there are any tweets cached.
- Timeline updating animation (circle).
- Using Picasso library for async image downloading & caching
- Dialogs for loading moments
- Icons
- Some fragments for tweet posting, for detail tweet view, for file etc.
- Using support library, 3.0 > version support.

TODO:
- ~~Tweet UI (date parsing, text assignment)~~
- ~~Timeline updating mechanism improving~~
- ~~Loading animation on app start~~
- ~~Something with first app pre-login activity~~
- ~~Improving the detail view tweet window~~
- ~~Adding network exceptions avoiding~~, screen rotating info saving
- ~~Logout~~
- Followers
- Direct messages
- Mentions
- Trends
- ~~Paging system improvement~~
- ~~Tweet deletion~~
- Last tweet edition
- ~~links~~ and pictures in tweets
- ~~some effects~~
- ~~localization (EN/RU)~~
- File choosing section improvements
- Update token exception fix
- ~~Round image views for profile images~~
- All media resources processing in detail screen
- ~~Changing activity structure to fully fragments based~~
- Improving app behavior with bad connections
- ~~Improving timeline system~~
- ~~Replacing rotate animation with something more smoother~~
- Adding webview for auth
- ~~Replacing standard menu with nav drawer one~~
- Add view pager for detail tweet scrolling
- ~~Replace main listview with recyclerview~~
- ~~Adding floating button to scroll up and down on timeline~~
- Profile section implementation
- Mention in tweet custom push notification with opening this tweet
- PN system changes + settings


Note: To launch this, you'll need to get your own consumer key and consumer secret for twitter app at dev.twitter.com and put them into app/src/com.rtweel.twitteroauth/ConstantValues.java class.

