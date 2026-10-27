# Anecdote 

Android app that simply display videos, images and text from the best sources  (9GAG, XKCD, CommitStrip, DTC (danstonchat.com), Se couchez moins bête, Bash.org). 

This is a Free and Open Source Software (FOSS).

<a href="https://play.google.com/store/apps/details?id=io.gresse.hugo.anecdote" target="_blank">
  <img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="80"/>
</a>
<a href="https://f-droid.org/repository/browse/?fdid=io.gresse.hugo.anecdote" target="_blank">
  <img src="https://f-droid.org/badge/get-it-on.png" height="80"/>
</a>
<br>
<img src="https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat" alt="API 24+">
<a href="https://github.com/HugoGresse/Anecdote/actions/workflows/quality.yaml">
  <img src="https://github.com/HugoGresse/Anecdote/actions/workflows/quality.yaml/badge.svg" alt="quality">
</a>

![9GAG Feed](https://raw.githubusercontent.com/HugoGresse/Anecdote/master/design/screenshots/9gag.png)
![Websites](https://raw.githubusercontent.com/HugoGresse/Anecdote/master/design/screenshots/website_selector.png)

_The application content does not belong to the application._


Build
-----
Requires JDK 17+ and the Android SDK (compile SDK 37).

Two flavors share the same code:

- `fdroid`: fully free software, the one F-Droid builds.
- `play`: also bundles ObjectBox (non-free) to import the favorites saved by versions up to 1.3.0 into Room, once.

```bash
./gradlew assembleFdroidDebug
```

Run the unit tests and lint:

```bash
./gradlew testFdroidDebugUnitTest lintFdroidDebug
```

Release builds are signed with `signing.properties` (see `signing.properties.example`) or the
`RELEASE_KEYSTORE_*` environment variables. Pushing a `vX.Y.Z` tag matching `versionName` builds the
release APK and publishes it on GitHub Releases.



Author
------
[Hugo Gresse](http://hugo.gresse.io)


Contributors
-----------
 - [mueller-ma](https://github.com/mueller-ma)
 - [ashed](https://github.com/ashed)


License
--------
``` 
Copyright 2017 Hugo Gresse

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
Google Play and the Google Play logo are trademarks of Google Inc.

[Donate $5]:   https://paypal.me/HugoGresse/5
[Donate $10]:  https://paypal.me/HugoGresse/10
[Donate $50]:  https://paypal.me/HugoGresse/50
