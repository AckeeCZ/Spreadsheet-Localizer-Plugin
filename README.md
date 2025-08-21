# Spreadsheet-Localizer-Plugin
Android Studio plugin that loads string resources from Google Spreadsheet

## Installation and usage
You can install the plugin to your Android Studio directly from the JetBrains plugin repository as "Spreadsheet Localizer" or you can of course build and deploy the plugin directly from the source code using the gradle wrapper ```./gradlew buildPlugin```. The plugin then adds the action to "Load localization strings" (little globe icon) to your main toolbar. When you trigger this action the settings dialog with the following properties is shown.

1. Project/project name - you may have multiple projects with different configurations.
2. Config file path - path to config JSON file which provides configuration for project localizations. 

Those properties are then saved in the ```$PROJECT_DIR$/.idea/localizerSettings.xml``` file. It is recommended to add this file to your VCS to allow other developers to easily download fresh dependencies without another configuration needs.

## Configuration file

You should provide configuration JSON file for your project. It is separated from the plugin and may be consumed by some other service (e.g. Gradle script).

The structure of this JSON is described in the example:

```json
{
  "fileId": "1122334455", // The unique identifier of your Google Sheets file - it is the hash part of the URL
  "sheetName": "Sheet 1", // The name of particular sheet in your file
  "apiKey": "AIza112233", // Google Spreadsheets API, available in your Google developers console
  "serviceAccountPath": "localizer/service-account.json",
  // Service Account specification json
  "languageMapping": { // Mapping between the column in the sheet and Android values folder suffix
    "EN": null, // null means that the language is default, localizations will be saved to "values" folder
    "CZ": "cs",
    "FR-FR": "fr-rFR" // E.g. the column name is "FR-FR", the localizations will be saved to "values-fr-rFR" folder
  },
  "resourcesFolderPath": "libraries/translations/src/main/res" // Relative path to your "res" folder with respect to json configuration path
  "supportEmptyStrings": false // Enables to support empty strings in the localization sheet
  "resourcesStructure" : "DEFAULT" // Structure of the generated resources files, could be DEFAULT or MOKO_RESOURCES
}
```

Note: in previous versions of Spreadsheet localizer plugin this configuration was stored directly in plugin settings

### Empty strings
Plugin has a configurable option to either support or not support empty strings in the localization sheet. This can be
configured via `supportEmptyStrings` attribute. When set to `true`, it takes empty strings from the localization sheet and
generates them to the XML file. When set to `false`, it just discards any empty strings found in the sheet. `supportEmptyStrings`
can be omitted from the configuration JSON for backwards compatibility and defaults to `false` in that case to preserve
a previous behaviour.

## Spreadsheet structure
The localization plugin of course depends on a predefined Google sheet structure. These are the rules the spreadsheet must follow for the plugin to work correctly:

1. The optional "Section" column. Based on values from this column the plugin visually separates the logical chunks of your resources (usually screens).The row containing section name shouldn't contain anything else.
2. The column "key_android" - the resource keys for your strings.xml should be put here
3. Locale columns (e.g. "en", "de", "cs") - contain actual translations. 
4. Base on config file they are mapped to resource files in corresponding to the `resourcesStructure` type.
  - For `DEFAULT` are created files like `values/strings.xml`, `values-cs/strings.xml`, `values-fr-rFR/strings.xml` etc.
  - For `MOKO_RESOURCES` are created files like `base/strings.xml`, `cs/strings.xml`, `fr-rFR/strings.xml` etc.

## Formatting params and plurals support
The translations can of course contain all the formatting symbols like %s or %1$d to correctly support the strings formatting. It is also possible to include the HTML tags in the strings. All these symbols are correctly escaped and usable in the resources files.

The plurals support is achieved using the special key formatting. You can specify plural key in a format key##{amount} which is then correctly translated into the plurals elements.

## Authorization

There are two ways this plugin can authorize towards a spreadsheet

- API key. This requires to create a new API key in Google console. This approach has disadvantage that a spreadsheet
  must be public for read
- service account. This requires to create a new Service account within Google Console. Then the spreadsheet does not
  have to be public and the service account must have access to this file

## Example spreadsheet

You can find the example
spreadsheet [here](https://docs.google.com/spreadsheets/d/1Z5g7bHavCe1YKnnpLcGiaKbO0OOQB5VctPvACUQVDMs/edit#gid=0) and
test it in your project with the localizerSettings.xml like this:
```
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="LocalizerSettings">
    <option name="projects">
      <list>
        <ProjectSettings>
          <option name="projectName" value="MyProject" />
          <option name="resPath" value="$PROJECT_DIR$\localization_config.json" />
        </ProjectSettings>
      </list>
    </option>
  </component>
</project>
```

## Changelog
- `1.2.1`
  - Fix plugin min and max IDE versions support
- `1.2.0`
  - Support for including empty strings from localization sheet to the generated XML file
- `1.1.0`
  - Support for service accounts authentication
- `1.0.0` 
    - ⚠️ BREAKING CHANGE ⚠️
    - Configuration is moved to separate JSON which now needs to be provided
    - Language mapping support: column keys may be mapped to values suffixes instead of using limited amount of supported languages. `null` suffix means default language.
