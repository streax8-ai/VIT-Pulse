# VIT Pulse Android

VIT Pulse is packaged as an Android app. Class reminders use native Android `AlarmManager` + notifications, so reminders can fire while the app is closed and after a phone reboot.

## Build on GitHub from a phone
1. Upload the contents of this folder to a GitHub repository.
2. Keep `.github/workflows/build-apk.yml` in the repository root.
3. Open **Actions → Build VIT Pulse APK → Run workflow**.
4. Download the `VIT-Pulse-debug-apk` artifact.

The app requests Android's notification permission on first launch. Enable **Class reminders** and choose 5/10/15/30 minutes. The native scheduler pre-schedules the next 21 days and refreshes after reboot or app update.
