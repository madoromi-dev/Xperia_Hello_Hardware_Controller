# Xperia Hello! (G1209) Hardware Controller & Motion Player

### 言語 / Language : [日本語](README.md) | English

> [!CAUTION]
> This project requires Bootloader Unlocking and Rooting of your "Xperia Hello!" device.

> [!NOTE]
> This project is currently in the development stage and is implemented as a hobby. If you have any feedback or suggestions, please feel free to contact me!

![Main image](https://github.com/madoromi-dev/Xperia_Hello_Hardware_Controller/blob/main/image.jpg?raw=true)

## Overview

This project provides a Java-based hardware controller, a motion player for animations, and a demo app designed to revive the hardware functionality of Sony's "Xperia Hello!" (G1209 / codename: Bright), which has become non-functional due to the termination of its official services.

### Background

Since its service ended on March 31, 2023, "Xperia Hello!" has lost all of its functions, including basic communication. This project began when I happened to find the device at a bargain price on a flea market site and bought it, impressed by its high build quality. Fortunately, a predecessor had already published the communication protocols and a control method using Kotlin. Based on their findings, I developed this controller by rewriting it in Java, fixing crashes, and optimizing movements. Additionally, I implemented a JSON-based Motion Player to make it easier to execute animations like nodding or winking from an app.

### How it Works

> [!IMPORTANT]
> This project does not contain any of Sony's official SDKs, binaries, or other proprietary intellectual property, nor is it intended to infringe upon them. This software is an unofficial implementation based on community analysis and publicly available information, and is in no way affiliated with Sony Corporation or its subsidiaries.

Xperia Hello! controls its hardware components (Pan/Tilt of the neck, Body rotation, and LEDs for the eyes and neck) via a serial port (`/dev/ttyHS1`). Since the Bootloader can be unlocked, these components can be controlled by gaining Superuser (root) privileges.

## Project Structure

### Features

- **Motor Control**: Full control over Neck Tilt, Neck Pan, and Body rotation. Includes software limits to protect the hardware.
- **LED Control**: Control for both the eye and neck LEDs. The eye LEDs use bitmask conversion, allowing expressions to be created by specifying intuitive values (degree of openness). The neck LED supports HEX RGB values.
- **Motion Playback**: Sequences of the above actions can be defined in JSON files. Asynchronous processing enables smooth animation playback. Since the eye LED and motor layers, as well as the neck LED layer, are managed separately, parallel execution is possible, allowing for rich expressiveness.

### Source Files

- **`BrightController.java`**: The core controller handling serial communication.
- **`BrightMotionPlayer.java`**: A class dedicated to managing and playing motions.
- **`MainActivity.java`**: A demo Activity to test the controller and motion player.
- **`assets/motions.json`**: A JSON file containing motions.

## Installation & Build

### 1. Preparing "Xperia Hello!"

Ensure your "Xperia Hello!" has an unlocked Bootloader and is Rooted.
For specific methods and patched `boot.img` files, refer to [**Sony-Xperia-Hello-Control** (by r00li)](https://github.com/r00li/Sony-Xperia-Hello-Control).

### 2. Adding Libraries

If you are creating a new project, import the following library for serial communication:
~~~gradle
dependencies {
    implementation 'io.github.xmaihh:serialport:2.1.1'
}
~~~

### 3. Build and Install

You can clone this project and build it yourself, or install the [pre-built APK](https://github.com/madoromi-dev/Xperia_Hello_Hardware_Controller/blob/main/app-release.apk) to try the demo app. When the Superuser request popup appears, please tap "Grant".

## License

### Acknowledgments

The analysis of the serial communication protocol and the foundation of the hardware control were based on the following project. I am deeply grateful to the author for sharing such wonderful insights!

[**Sony-Xperia-Hello-Control** (by r00li)](https://github.com/r00li/Sony-Xperia-Hello-Control)
~~~
Copyright (c) 2024 Andrej Rolih

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
~~~

### Trademarks

- "Xperia" is a trademark or registered trademark of Sony Corporation.

### Project License

This software is released under the [MIT License](LICENSE.txt).



