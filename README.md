# Manoj VoIPSim - Full Project (Java, Android 15-ready)

This archive contains a ready-to-open Android Studio project (Java only) that simulates VoIP calls.
Features included:
- AlarmManager exact + WorkManager fallback for scheduled incoming calls
- Full-screen incoming UI with ringtone, vibration, auto-miss after 10s
- Ongoing call with foreground service and live timer
- Local Room DB call logs (missed/answered + duration)
- Missed-call notification with Quick Callback
- Battery optimization Supported 
- Material 3 theme integration (XML-based) and simple XML animations (fade in/out)
- Inline comments across Java files for clarity

## How to open & run (Mac / Android Studio)
1. Unzip `Manoj_VoIP.zip`.
2. Open the project folder in **Android Studio** (AGP 8.5+ recommended).
3. Let Gradle sync. Install any SDK/NDK prompts.
4. Run on a **physical device** (recommended) with Android 14/15 to fully test call UI behavior.

## Build signed APK on macOS (outline)
You mentioned you'll build the signed APK on your Mac. Quick steps:
1. In Android Studio: Build -> Generate Signed Bundle / APK
2. Choose APK -> Create new key store or use existing. Enter passwords and aliases.
3. Select release build and finish. Android Studio will produce a signed APK under `app/build/outputs/apk/release/`.

## Testing checklist
- Schedule a test call (5s) and lock the phone. Show incoming full-screen UI with ringtone & vibration.
- Wait 10s to auto-miss and show missed-call notification (tap to open Call Logs).
- Schedule again, Answer, show Ongoing Call screen with timer, press Home (timer continues via foreground service), then return and End Call. Verify call log entry shows ANSWERED duration.
- Show the WorkManager fallback: (optional) Force device to sleep or test on an OEM that throttles alarms.

## Notes & Troubleshooting
- Some OEMs aggressively throttle alarms; use Battery Help to whitelist the app for reliable behavior.
- Notifications require runtime permission on Android 13+ (the app asks on first launch).
- This project is Java-only and avoids any third-party calling SDKs as required by the assignment.
