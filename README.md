# DroidLane
[![Build Status](https://travis-ci.org/Jintin/DroidLane.svg?branch=master)](https://travis-ci.org/Jintin/DroidLane) [![JetBrains compatible](https://img.shields.io/badge/JetBrains-compatible-brightgreen.svg)](https://plugins.jetbrains.com/plugin/8068)

DroidLane is an deploy tool help you upload your apk to Google Play from Android Studio or IntelliJ IDEA. An Android Studio / Intellij plug-in help you upload your apk file to Google Play

## Installation
- Open Android Studio or IntelliJ IDEA
- Open [Preferences] -> [Plugins] -> [Browse repositories], search DroidLane to install and restart

## Usage
- Go to Google Play **[Settings] -> [API access] -> [Create OAuth Client]** and keep your [CLIENT ID] and [CLIENT SECRET]
- Go to your project root dir create `.droidlane/[profile_name]/data.json` with content:

  ```json
  {
  "client_id": "[CLIENT ID]",
  "package": "[PACKAGE NAME]",
  "apk": "[APK PATH]",
  "track": "['alpha', 'beta' or 'production'](optional)"
  }
  ```

- Run DroidLane command in **[Build] -> [Upload Apk]**
- Select track if not define in data.json
- Set [CLIENT SECRET] and encrypt with password
- OAuth permission accept in browser

After first-time setup, you only need click button and key-in password to upload apk.

### Multiple profile support
You can also add multiple profile in flowing format.

```
└── .droidlane
    ├── [profile_name1]
    │   └── data.json
    └── [profile_name2]
        └── data.json
```

### Recent Change Text with multiple language support
Create the recent change file with content in flowing directory

```
  └── .droidlane
      └── [profile_name]
          ├── data.json
          └── recentChange
              ├── en_US
              ├── zh_TW
              │   ...
              └── (other language)
```

## Contributing
Bug reports and pull requests are welcome on GitHub at [https://github.com/Jintin/DroidLane](https://github.com/Jintin/DroidLane).

## License
The module is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).
