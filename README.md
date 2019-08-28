# Periodic Lock
An Android app that periodically locks the phone screen for a given time interval.


Available in the Play Store:  
https://play.google.com/store/apps/details?id=com.danwan.periodiclock

## Simple to use
1) Input time interval
2) Toggle on


## Details on use
- Enter a time interval and choose a time unit as seconds, minutes, or hours
- minimum time values:
  - seconds: 20
  - minutes: 1
  - hours: 1
- an empty time value field will set the time value to the above minimum values corresponding to the selected time unit
- maximum time values is equivalent to 1 day:
  - seconds: 86400
  - minutes: 1440
  - hours: 24
- toggle to activate/deactivate periodic locking
- upon activation, a persistent notification will appear which states the time when the screen will lock
- upon unlocking the screen, the notification will update to display next time that the phone will lock which is the current time plus time interval
- periodic locking service will still be active after the app is closed
- tapping on the service notification will open up the app
- toggle OFF to deactivate the service
- upon deactivation, the service notification will disappear and the app will no longer lock the phone


### Credits

Screen locking tutorial:  
https://medium.com/@ssaurel/creating-a-lock-screen-device-app-for-android-4ec6576b92e0

Foreground service tutorial:  
https://codinginflow.com/tutorials/android/foreground-service
