# Periodic Lock
An Android app that periodically locks the phone screen for a given time interval.

(work in progress)

## Possible Use Cases
- Health: User wants to limit their time spent on their phone.
- Security: If phone was unlocked before it went missing/stolen, limit the amount of time that unauthorized users may have with your data. (Store this app in a secured folder so unauthorized users cannot disable/delete the app)


## Simple to use
1) Input time interval
2) Toggle on


## Details on use
- Enter a time interval and choose time unit as seconds or minutes
- if using seconds, the minimum time value is 20
- if using minutes, the minimum time value is 1
- an empty time value field will set the time value to the above minimum values corresponding to the selected time unit
- toggle to activate/deactivate periodic locking
- upon activation, a persistent notification will appear which states the time when the screen will lock
- periodic locking is still be active after the app is closed
- tapping on the notification will open up the app
- upon deactivation, the notification will disappear and the app will no longer lock the phone
