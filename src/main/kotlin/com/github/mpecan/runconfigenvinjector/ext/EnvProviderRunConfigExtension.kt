package com.github.mpecan.runconfigenvinjector.ext

import com.github.mpecan.runconfigenvinjector.service.EnvProviderFactory
import com.github.mpecan.runconfigenvinjector.service.EnvProviderService
import com.github.mpecan.runconfigenvinjector.service.EnvValueCache
import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.idea.maven.execution.MavenRunConfiguration
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration

class EnvProviderRunConfigExtension : RunConfigurationExtension() {
    private val logger = Logger.getInstance(EnvProviderRunConfigExtension::class.java)
    private val configService = service<EnvProviderService>()
    private val envValueCache = service<EnvValueCache>()

    override fun <T : RunConfigurationBase<*>> updateJavaParameters(
        configuration: T,
        params: JavaParameters,
        runnerSettings: RunnerSettings?
    ) {
        val runConfigurationName = configuration::class.java.simpleName
        logger.warn("Updating Java parameters for $runConfigurationName")
        configService.getEnabledConfigurations().filter { config ->
            config.enabledRunConfigurations.contains(runConfigurationName)
        }.mapNotNull { config ->
            val provider = EnvProviderFactory.createProvider(config)
            envValueCache.getValue(provider)?.let { token ->
                config.environmentVariable to token.value
            }
        }.let {
            if (it.isEmpty()) {
                logger.warn("No environment variables to set for $runConfigurationName")
                return
            }
            when(configuration){
                is MavenRunConfiguration -> {
                    params.env = params.env + it
                }
                is GradleRunConfiguration -> {
                    (configuration as GradleRunConfiguration).settings.env = (configuration as GradleRunConfiguration).settings.env + it
                }
                else -> {
                    params.env = params.env + it
                }
            }
            runInEdt {
                Notifications.Bus.notify(
                    Notification(
                        "env-provider-injector",
                        "Env injector",
                        "Environment variables:<br> ${it.map { (key, _) -> "- $key" }.joinToString("<br>")} <br>set for $runConfigurationName",
                        NotificationType.INFORMATION
                    )
                )
            }
        }
    }

    override fun isApplicableFor(configuration: RunConfigurationBase<*>): Boolean =
        configuration is MavenRunConfiguration || configuration is GradleRunConfiguration

}