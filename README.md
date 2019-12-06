# NotABadPlayer-Android

Created: 2019 March

Android's default music player sucks so much that I remade it with some extra features.

Platform: Android SDK 14+ (Android 4.0 IceCream Sandwich)

Usage: Media player

Technologies: Android SDK 21, Android's MediaStore, Guava (com.google.guava:guava:26.0-android)

Architectural design:

* MVP (Model=data, View=interface, Presenter=data/interface bridge, state controller)

* Application is mostly single activity. There are two other activities, but the Main Activity is the one in which users will spend most of their time, since they have access to the player controls there.

* Activities communicate trough serialization objects (Java Base64 serialization style)

* The fragments are the views in this MVP design (inherit BaseView). Some activities also inherit BaseView. Fragments use onActivityCreate() to start their presenters.

* Presenters hold the model, responsible for decision making. They respond to view input (user interaction) and then forward back messages to their views, to order them what to do.

* The fragments are the observers for the audio player (rather than the presenters), this is because audio player events merely change the UI information rather than changing complex "business" logic

Design patterns:

* Delegate - presenters and their views are delegates, both handling requests and forwarding them to each other, views are the responders to user input, who forward those events to the presenters, who, based on decision making, may or may not forward an action to their views

* Observer - some interface is updated trough a Looper singleton service, that notifies their observers when the timer elapses a specific interval; audio player notifies their observers when the audio state changes (start, pause, etc...)

* Singleton - used to easily refer to services such as the Audio Player (a wrapper of the built in android player), the Looper (repeated interval update for its clients), and the user storage used to store general info such at the app settings

* Decorator - the Audio Player wraps the android built in audio player - the Media Player

* Command - keybind actions

* Bridge - Player class is abstraction while AudioPlayerService & AudioPlayerDummy are implementation.

* Flyweight - some audio models such as AudioTrackSource and AudioTrackDate are shared between other classes for lower memory usage

* Builder - used to build audio models, in order to reduce the complexity and to add support for future variatuins of these models: AudioPlaylistV1 may have have a newer implementetion V2, V3, V4...

General design:

* CPU and energy efficient, memory ineffecient since the audio information is retrieved once and reused when trying to use the audio player

* Audio Library, a singleton that store audio data of albums and tracks. It uses the Android API MediaStore.

* Multiple task android app: when launching the player from another program, like the Files program, a separate task is created and put ontop of the navigation for the Files program, rather than transfering the user to one single unique task window (like in iOS).

* Lifecycle: Application is used to handle the app launch process: it holds the app state and alerts its activities when the app is completely finished with its launching process. The app does not wait to start all the services: it shows the UI and gives some control to the user for better experience. When the Application is finished starting all services, it alerts its activities.

* Using Android Services to play the audio. If the app is killed by the OS, the service (which is a daemon basically), will still keep running. A notification is displayed while the player is running, which gives you some control of the audio player.

* Supports one orientation only: portrait

# Features

Bind all kinds of user actions like making the next/previous buttons jump backwards and forwards.

3 app themes, different sorting options,optional volume bar on the player screen.

CPU & energy efficient.

Includes standart player features like creating playlists, searching for tracks, controlling the audio player even when not on the player screen (a quick player is available, attached to the bottom of the screen).

Includes slightly more fancy features like jumping back to the previously played song, regardless to which album or list it belonged to.

Background playback (audio can usually remain playing even when the app is killed due to low memory)

Notification playback - control the player from the status screen

Portrait mode only.

# Screens

Albums screen (quick player at the bottom, swipe up to open player screen)

![alt text](https://github.com/felixisto/NotABadPlayer-Android/blob/master/About/2019scrn1.jpg)

Player screen

![alt text](https://github.com/felixisto/NotABadPlayer-Android/blob/master/About/2019scrn2.jpg)

Playlist screen

![alt text](https://github.com/felixisto/NotABadPlayer-Android/blob/master/About/2019scrn3.jpg)

Search screen

![alt text](https://github.com/felixisto/NotABadPlayer-Android/blob/master/About/2019scrn4.jpg)

Settings screen - keybind options

![alt text](https://github.com/felixisto/NotABadPlayer-Android/blob/master/About/2019scrn5.jpg)

Dark app theme

![alt text](https://github.com/felixisto/NotABadPlayer-Android/blob/master/About/2019scrn6.jpg)
