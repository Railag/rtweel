Rtweel
======
Lightweight Twitter application available even for old android devices starting from android 2.3 version (GINGERBREAD). 

Already done:
- Twitter OAuth authentification through integrated twitter4j library, initial browser login
- Notifications which check updates on your twitter and download tweets, cyan LED color
- One timeline, which can contain both your tweets or your home timeline's. 
- Some buttons for manual updating timelines, posting tweets, etc.
- Right swipe to receive newest tweets and left swipe to receive older tweets.
- Caching profile images, so the second and later launches will be faster.
- Caching your downloaded timeline into SQLite DB and retrieving it from if there are any tweets cached.
- Timeline updating animation (circle).
- Some screens for tweet posting, for detail tweet view.
- Using support library, 2.3 > version support.
- 1 Mb apk file weight, 2.8 Mb installed app size.

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
- Tweet deletion
- Last tweet edition
- ~~links~~ and pictures in tweets
- ~~some effects~~
- localization (EN/RU)
And a lot of small fixes

Note: Current 0.20 version isn't stable a bit. 
Note: To launch this, you'll need to get your own consumer key and consumer secret for twitter app at dev.twitter.com and put them into /src/com.rtweel.twitteroauth/ConstantValues.java class.

