# DroidLane
An Android Studio plug-in help you upload your apk file to Google Play.

## Installation
- Download [zip](https://raw.githubusercontent.com/Jintin/DroidLane/master/DroidLane.zip) file
- Open [Preferences] -> [Plugins] -> [Install plugin from dist], select DroidLane.zip to install

## Usage
- Go to Google Play **[Settings] -> [API access] -> [Create OAuth Client]** and keep your [CLIENT ID] and [CLIENT SECRET] (first-time)
- Go to your project root dir create `.droidlane/[name]/data.json` with content(first-time):

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
- Set [CLIENT SECRET] and encrypt with password(first-time) or decrypt [CLIENT SECRET] with password(non first-time)
- OAuth permission accept in browser(first-time)

After first-time setup, you only need click button and key-in password to upload apk.

## Contributing
Bug reports and pull requests are welcome on GitHub at [https://github.com/Jintin/DroidLane](https://github.com/Jintin/DroidLane).

## License
The module is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).
