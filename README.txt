VIT Pulse Android/GitHub project with native class reminders.

Upload the contents of this ZIP to a GitHub repository, then use Actions -> Build VIT Pulse APK -> Run workflow.
The APK artifact is VIT-Pulse-debug-apk.

Native reminders are scheduled by Android AlarmManager and delivered by a BroadcastReceiver, so they can appear while VIT Pulse is completely closed. The schedule is restored after reboot/app update.
