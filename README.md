# Spreadsheet-Localizer-Plugin
Android Studio plugin that loads string resources from Google Spreadsheet

## Installation and usage
You can install the plugin to your Android Studio directly from the JetBrains plugin repository as "Spreadsheet Localizer" or you can of course build and deploy the plugin directly from the source code using the gradle wrapper ```./gradlew buildPlugin```. The plugin then adds the action to "Load localization strings" (little globe icon) to your main toolbar. When you trigger this action the settings dialog with the following properties is shown.

1. Google API key - your key for the access to the Google Spreadsheets API, you can get it in your Google developers console
2. Google Sheet ID - the unique identificator of your Google sheet - it is the hash part of the URL you see your sheet at (e.g. ```1Z5g7bHavCe1YKnnpLcGiaKbO0OOQB5VctPvACUQVDMs```)
3. Spreadsheet name - the name of the spreadsheet in your file the localizations are in
4. Default language - locale id of the localization language you consider as your default language, this localizations will be stored in your default resources directory
5. Resources directory - path to the resources directory you want to put your localizations in, it is recommended to use the placholder for the current project path to ensure the path is user and environment independent (e.g. ```$PROJECT_DIR$/app/src/main/res```)

The configuration of the localization plugin is stored per project into the file ```$PROJECT_DIR$/.idea/localizerSettings.xml``` and it is recommended to store this file in your private VCS to allow other developers to easily download fresh dependencies without another configuration needs.

## Spreadsheet structure
The localization plugin of course depends on a predefined Google sheet structure. These are the rules the spreadheet must follow for the plugin to work correctly:

1. the optional "Section" column. When you include the column named "Section" the plugin then can divide your resources within the single resource file into commented sections. You put the names of the respective sections on a separate rows in the sheet.
2. column "key_android" - you put the resurces keys for your application in this columns
3. optional custom-named "key_" columns - you can put the resource keys for your other platforms in these columns (e.g. key_ios or key_web) this allows to use the same localizations for more platforms, when you use similar design for these platforms.
4. locale named columns with the translations (e.g. "en", "de", "cs") these columns are filled with the translations for the languages you use in your app. The translations in the column specified in the settings as default language are tranferred to your default string resources file, others are transferred to their respective locale-specific resource files.

## Formatting params and plurals support
The translations can of course contain all the formatting symbols like %s or %1$d to correctly support the strings formatting. It is also possible to include the HTML tags in the strings. All these symbols are correcly escaped and usable in the resources files.

The plurals support is achieved using the special key formatting. You can specify plural key in a format key##{amount} which is then correctly translated into the plurals elements.

## Example spreadheet
You can find the example spreadsheet [here](https://docs.google.com/spreadsheets/d/1Z5g7bHavCe1YKnnpLcGiaKbO0OOQB5VctPvACUQVDMs/edit#gid=0) and test in in your project with the localizerSettings.xml like this:
```
<?xml version="1.0" encoding="UTF-8"?>
<project version="1">
    <component name="LocalizerSettings">
        <option name="apiKey" value="YOUR API KEY"/>
        <option name="defaultLang" value="en"/>
        <option name="resPath" value="$PROJECT_DIR$/app/src/main/res"/>
        <option name="sheetId" value="1Z5g7bHavCe1YKnnpLcGiaKbO0OOQB5VctPvACUQVDMs"/>
        <option name="sheetName" value="Localizations"/>
    </component>
</project>
```
