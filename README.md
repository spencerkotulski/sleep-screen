# Sleep Screen

Sleep Screen is an independent Android TV sleep timer with an optional countdown, dimmer, blue light filter, and remote shortcut. Requires Android 9 or later.

## Setup
Install the APK and open Sleep Screen. Use Setup to enable its accessibility service for sleep and overlays; grant alarm and overlay access where available. Optional remote mapping uses a second accessibility service and may conflict with other key mappers. Some keys may be unavailable or reported as unknown.

## Build
Install JDK 17 and Android SDK 34, then run `gradlew assembleDebug`.

## Attribution
This project contains adapted code from the CleanYourTV sleep timer add-on by luyzfernando08 and contributors. The original repository is https://github.com/Gathin23/cleanyourtv. Its MIT license and copyright notice are retained in LICENSE.

Blue light filtering uses a warm overlay, not a calibrated color transform. Device sleep and HDMI-CEC support vary.
