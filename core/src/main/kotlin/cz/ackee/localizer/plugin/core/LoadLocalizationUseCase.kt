package cz.ackee.localizer.plugin.core

import cz.ackee.localizer.plugin.core.auth.CredentialsService
import cz.ackee.localizer.plugin.core.auth.CredentialsServiceImpl
import cz.ackee.localizer.plugin.core.configuration.ConfigurationParser
import cz.ackee.localizer.plugin.core.configuration.ConfigurationParserImpl
import cz.ackee.localizer.plugin.core.localization.LocalizationsRepository
import cz.ackee.localizer.plugin.core.localization.LocalizationsRepositoryImpl
import cz.ackee.localizer.plugin.core.sheet.XmlGenerator

class LoadLocalizationUseCase(
    private val configurationParser: ConfigurationParser = ConfigurationParserImpl(),
    private val credentialsService: CredentialsService = CredentialsServiceImpl(),
    private val localizationsRepository: LocalizationsRepository = LocalizationsRepositoryImpl()
) {

    operator fun invoke(configPath: String) {
        val configuration = configurationParser.parse(configPath)
        val credentials = credentialsService.getCredentials(configPath, configuration)
        val localization = localizationsRepository.getLocalization(configuration, credentials)

        val xmlGenerator = XmlGenerator.from(configPath, configuration)
        xmlGenerator.createResourcesForLocalization(localization, configuration.resourcesStructure)
    }
}
