# Privacy Policy

**Last updated:** September 2026

**Volley Manager** is a free and open-source Android app for organizing recreational volleyball matches. This policy explains how data is handled in the app, including its optional sign-in, premium cloud sync, live spectator sharing, and telemetry features.

The project is distributed under the **GNU General Public License v3.0 (GPL-3.0)**.

---

### 1. Data Collected
The app stores only the data you manually provide in the local database, including:
* Player names;
* Groups and group settings;
* Match history;
* Elo scores and related game metadata.

By default, the app does not collect personal identifiers, advertising identifiers, location, contact information, or browsing behavior. No sensitive personal data is collected automatically.

### 2. Anonymous Telemetry (Analytics & Crash Reporting)
The app includes an optional, anonymous telemetry feature, powered by **Firebase Analytics** and **Firebase Crashlytics** (Google). It is only enabled after you explicitly opt in through a clear consent dialog shown on first use (or later from the app's settings menu). The purpose of this telemetry is to understand feature usage, app stability, and performance issues, not to profile individuals.

No data is collected before you opt in: telemetry collection is disabled by default at the app level, and is only turned on in response to your explicit consent.

When enabled, the telemetry may include aggregated information such as:
* App version;
* Android version;
* Basic device/platform information required for compatibility checks;
* Anonymous crash and error data (via Firebase Crashlytics);
* Feature usage events (for example: "group created", "match finished", "teams rebalanced", "streak-break rebalance", "backup/CSV exported or imported"), without personal or group identifiers.

This data does not include player names, match details, group names, contact information, or any raw content you enter into the app. It is not used to identify you personally. You may revoke consent at any time in the app's settings menu (the same toggle used to opt in), and telemetry collection stops immediately once consent is withdrawn.

### 3. Local Storage
All core app data is stored locally on your device using the app's internal database (**Room/SQLite**). No routine data is sent to external servers as part of the app's base functionality.

### 4. Sign-In, Premium Cloud Sync, and Live Spectator Sharing
The app offers optional sign-in and premium features that go beyond the local-only experience described above:
* **Sign-in**: you may create a free account (e-mail/password or Google Sign-In) or, for spectators, join a group anonymously with an invite code, all through **Firebase Authentication**.
* **Premium cloud sync**: a paid subscription lets the group organizer synchronize group data — players, live scoreboard, match history, and Elo logs — across devices, backed by **Firebase/Firestore**.
* **Live spectator sharing**: the organizer can share an invite/join code so spectators can follow the live scoreboard in real time and, if the organizer enables it, the match history and player Elo ratings — without spectators needing a full registered account.
* **Team color customization**: subscribers can personalize each team's colors on their own device; the organizer of a synced group can also set colors that apply to everyone viewing that group.

These features are optional and separate from the core, local-only app experience. Account data may include your e-mail address, display name, and a unique account identifier used for authentication and access control, in addition to the synchronized group data described above.

### 5. Subscriptions and Payments
Premium subscriptions (monthly and annual plans) are purchased and managed through **Google Play Billing**. Prices are set and displayed by Google Play at checkout and may vary by country/region and applicable taxes. Payment details (such as card information) are processed entirely by Google and are never collected or stored by the app or its developer. Subscriptions renew automatically until canceled and can be reviewed, changed, or canceled at any time from the Google Play Store's subscriptions page. Refunds follow Google Play's own policies.

### 6. Data Sharing
The app does not sell your data and does not share your local data with third parties as part of the base product.

If you use cloud sync or live spectator sharing, the corresponding group data (players, live scoreboard, match history, Elo logs) is stored in Firebase/Firestore and made available only to the devices/accounts you explicitly grant access to — group members and spectators who hold a valid invite code. Subscription and payment data is handled by Google Play Billing under Google's own privacy policy, and telemetry (Section 2) is shared only as described there.

### 7. Third-Party Services
The app uses **Firebase Authentication**, **Firebase Firestore**, and **Firebase Cloud Functions** (Google) to provide sign-in, premium cloud sync, and live spectator sharing, and **Google Play Billing** (Google) to process subscription purchases. If you opt in to the anonymous telemetry feature described in Section 2, the app also uses **Firebase Analytics** and **Firebase Crashlytics**. The app does not use any other analytics, ad networks, or behavioral tracking services.

### 8. Permissions
The app requests the INTERNET permission, used to sync data with Firebase when you use the optional sign-in, premium cloud sync, or live spectator features, and to send the anonymous telemetry described in Section 2 when you opt in (no network request is made for these purposes if you don't use them). The app does not request other special device permissions such as camera, microphone, or location.

### 9. Data Deletion
You control your local data:
* You can delete players, groups, or match history directly in the app.
* Uninstalling the app removes all locally stored data from the device.

If premium cloud sync is used, deleting a synced group in the app also erases that group's cloud data (players, live scoreboard, match history, Elo logs, member/spectator records, and invite codes) from our servers — this cannot be undone. Deleting your account similarly erases your account record and all cloud groups you own, even if the app couldn't confirm the deletion before the account was removed. Canceling a subscription stops future billing but does not, by itself, delete your cloud data — use the in-app group deletion or account deletion for that.

### 10. Children
The app is not directed to children under 13 and does not knowingly collect children's personal data.

### 11. Changes
This policy may be updated in future versions to reflect changes in the app or legal requirements. Significant changes will be communicated in release notes and in-app notices when applicable.

### 12. Contact
Questions about this policy can be sent to the developer through the official app page on the **Google Play Store** or through the GitHub repository associated with the project.
