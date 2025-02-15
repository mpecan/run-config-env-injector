package com.github.mpecan.runconfigenvinjector.service.awscf

import com.intellij.openapi.application.runInEdt
import javax.swing.JOptionPane

class AwsAuthenticationHandler {
    data class SsoChallenge(val url: String, val code: String)

    fun handleSsoChallenge(error: String): SsoChallenge? {
        val ssoPattern = Regex(".*SSO authorization.*URL:\\s*(\\S+)\\s*.*enter the code:\\s+(\\S+)\\s.*")
        return ssoPattern.find(error)?.let { matchResult ->
            SsoChallenge(
                url = matchResult.groupValues[1],
                code = matchResult.groupValues[2]
            )
        }
    }

    fun handleMfaChallenge(error: String): Boolean {
        val mfaPattern = Regex(".*?(Enter MFA code for \\S+\\s)$")
        return mfaPattern.find(error) != null
    }

    fun promptForMfaCode(): String {
        var mfaCode: String? = null
        runInEdt {
            JOptionPane.showInputDialog("Enter MFA code:").also { mfaCode = it }
        }
        return mfaCode ?: throw RuntimeException("No MFA code provided")
    }

    fun showSsoInstructions(challenge: SsoChallenge) {
        runInEdt {
            JOptionPane.showMessageDialog(
                null,
                "Please complete the SSO login in your browser.\nURL: ${challenge.url}\nCode: ${challenge.code}"
            )
        }
    }

    fun showError(message: String) {
        runInEdt {
            JOptionPane.showMessageDialog(
                null,
                "Error getting CodeArtifact token: $message",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    fun isSsoExpiredOrInvalid(error: String): Boolean {
        return error.contains("The SSO session associated with this profile has expired") || error.contains("Error loading SSO Token")
    }
}