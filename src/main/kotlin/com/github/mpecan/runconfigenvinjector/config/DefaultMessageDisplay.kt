package com.github.mpecan.runconfigenvinjector.config

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.ui.Messages

class DefaultMessageDisplay : MessageDisplay {
    override fun showError(message: String, title: String) {
        runInEdt {
            Messages.showErrorDialog(message, title)
        }
    }

    override fun showInfo(message: String, title: String) {
        runInEdt {
            Messages.showInfoMessage(message, title)
        }
    }
}