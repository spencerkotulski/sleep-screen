# Sleep Screen

Sleep Screen is an early beta for Android TV and Google TV, combining a sleep timer, optional countdown overlay, dimmer, blue light filter, and remote shortcut.

## Install the beta

1. Install **Downloader by AFTVnews** on your Android TV or Google TV.
2. To install version 0.2 beta, open the [0.2 release page](https://github.com/spencerkotulski/sleep-screen/releases/tag/v0.2.0-beta) and download its APK. Downloader code **8672497** installs the older version 0.1 beta.
3. Download and install the APK. Android may ask you to allow Downloader to install unknown apps.
4. Open Sleep Screen and follow the setup screen to enable the features you want.

[Download the APK on GitHub](https://github.com/spencerkotulski/sleep-screen/releases/tag/v0.2.0-beta).

**Beta status:** This standalone APK is a debug build and has not been tested on a device. Sleep, HDMI-CEC, accessibility and remote mapping behavior can vary by device. The Netflix button may not be independently mappable. Remote mapping may conflict with another button mapper.

## Setup and permissions

The sleep accessibility service puts the device to sleep and displays optional overlays. The optional remote service listens for a user-selected button and does not read screen content. Overlay and exact-alarm access may be required for warnings and timing. Android may restrict accessibility settings for sideloaded apps.

The blue light filter is a warm-colored overlay rather than a calibrated display color transform. HDMI-CEC determines whether putting the streaming device to sleep also turns off the TV.

## Build

Install JDK 17 and Android SDK 34, then run `gradlew assembleDebug`.

## Attribution

Contains adapted code from the [CleanYourTV](https://github.com/Gathin23/cleanyourtv) sleep timer add-on by luyzfernando08 and contributors. Its MIT license and copyright notice are retained in [LICENSE](LICENSE).
