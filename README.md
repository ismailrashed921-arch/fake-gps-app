# Fake GPS (Ki-Lagbe style)

Personal mock-location app for Android. Lets you set a fixed GPS location via
Android's standard **Developer Options → Select mock location app** system,
switch between saved locations from a quick-action notification, and manage
everything from a teal-branded map UI (free OpenStreetMap tiles, no API key).

## What this app does — and doesn't do

- Uses Android's official mock location provider (`LocationManager.addTestProvider`).
  This requires you to manually enable Developer Options and select this app
  once — Android will never let an app do this silently, by design.
- Any app that checks `Location.isFromMockProvider()` (most delivery / ride
  apps do) will detect that mock location is active. This app does not try to
  hide that fact or spoof the check — doing so would just be a fraud tool
  wearing a different UI.

## Project structure

```
app/src/main/java/com/kilagbe/fakegps/
  MainActivity.kt              # Compose UI: map, saved locations, settings
  MockLocationService.kt       # Foreground service feeding the mock location
  NotificationHelper.kt        # Quick-switch notification with actions
  NotificationActionReceiver.kt
  BootReceiver.kt              # Re-applies last location after reboot
  LocationRepository.kt        # DataStore persistence for saved locations
  ui/Theme.kt                  # Ki-Lagbe teal color tokens
```

## Build via GitHub Actions (no local Android Studio needed)

1. Push this folder to a new GitHub repo:
   ```bash
   cd fake-gps-app
   git init
   git add .
   git commit -m "Initial Fake GPS app"
   git remote add origin https://github.com/<your-username>/fake-gps-app.git
   git branch -M main
   git push -u origin main
   ```
2. Go to the repo's **Actions** tab — the `Build APK` workflow runs
   automatically on push.
3. Once it finishes (green check), open the workflow run → **Artifacts** →
   download `fake-gps-debug-apk`. Unzip it to get `app-debug.apk`.
4. Transfer the APK to your phone and install it (you'll need to allow
   "install unknown apps" for whichever app you use to open the file).

## One-time setup on your phone

1. Settings → About phone → tap **Build number** 7 times to unlock
   Developer Options.
2. Settings → Developer Options → **Select mock location app** → choose
   **Fake GPS**.
3. Open the app, drag the map (or tap the `#` button to type exact
   coordinates), then tap **এই লোকেশন সেট করুন**.
4. Pull down the notification shade any time to quick-switch between saved
   locations or turn it off.

## Notes / known gaps to finish before shipping

- `MockLocationService` currently defaults the mock location's `provider`
  to `GPS_PROVIDER` only — for full coverage on newer Android versions you
  may also want to register `LocationManager.FUSED_PROVIDER` and
  `NETWORK_PROVIDER`.
- Runtime permission requests (`ACCESS_FINE_LOCATION`,
  `POST_NOTIFICATIONS`) aren't wired into `MainActivity` yet — add an
  `ActivityResultContracts.RequestMultiplePermissions()` launcher on first
  screen render.
- App icon (`@mipmap/ic_launcher`) isn't included — Android Studio's
  built-in icon generator (or a placeholder) is needed before the first
  real build.
