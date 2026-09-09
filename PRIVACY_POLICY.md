# Privacy Policy

**Last updated:** September 2026

**Volley Manager** is a free and open-source Android app for organizing recreational volleyball matches. This policy explains how data is handled in the app and how future optional features may operate.

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

### 4. Future Premium Sync Feature
We may introduce a future premium sync feature that allows a user to synchronize group data, including players, across devices via a user account. This feature may require:
* User registration and authentication;
* Sign-in through the supported provider(s);
* Explicit acceptance of the feature's terms and consent flows, including an electronic acknowledgment/signature where applicable;
* Storage and synchronization using **Firebase/Firestore**.

When this feature is available, account data may include user identity information required for authentication and access control, as well as synchronized group data such as players, groups, settings, and related metadata. The use of the premium sync feature is optional and separate from the core app experience.
### 5. Data Sharing
The app does not sell your data and does not share your local data with third parties as part of the base product.

For any future optional telemetry or premium sync feature, sharing will be limited to what is necessary to provide that feature and will follow the consent and disclosures defined in the relevant in-app notice.

### 6. Third-Party Services
If you opt in to the anonymous telemetry feature described in section 2, the app uses **Firebase Analytics** and **Firebase Crashlytics** (Google) to collect that anonymous, aggregated data. The app does not use any other analytics, ad networks, or behavioral tracking services. If a future premium sync is introduced, the service providers involved (such as Firebase/Firestore) will be used only as needed to deliver that specific feature.

### 7. Permissions
The app requests the INTERNET permission, used only to send the anonymous telemetry described in section 2 when you opt in (no data is sent while telemetry is disabled). The app does not request other special device permissions such as camera, microphone, or location. If future cloud features require additional permissions or services, they will be requested only when required for that feature and clearly explained to the user.

### 8. Data Deletion
You control your local data:
* You can delete players, groups, or match history directly in the app.
* Uninstalling the app removes all locally stored data from the device.

If the future premium sync feature is used, you may also be able to delete or disconnect your synced account data through the account settings or the cloud sync controls available in the app, subject to the service's account management features.

### 9. Children
The app is not directed to children under 13 and does not knowingly collect children's personal data.

### 10. Changes
This policy may be updated in future versions to reflect changes in the app or legal requirements. Significant changes will be communicated in release notes and in-app notices when applicable.

### 11. Contact
Questions about this policy can be sent to the developer through the official app page on the **Google Play Store** or through the GitHub repository associated with the project.
