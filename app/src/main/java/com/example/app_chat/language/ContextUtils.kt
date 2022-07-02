package com.example.app_chat.language

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import android.widget.Switch
import java.util.*

class ContextUtils(baseContext: Context) : ContextWrapper(baseContext) {
    companion object {
        var language = "en"

        fun updateLocale(context : Context, localeToSwitch: Locale) : ContextWrapper {
            var newContext = context
            val resource = context.resources
            val config = resource.configuration
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(localeToSwitch)
                LocaleList.setDefault(localeList)
                config.setLocales(localeList)
            } else {
                config.locale = localeToSwitch
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                newContext = context.createConfigurationContext(config)
            } else {
                resource.updateConfiguration(config, resource.displayMetrics)
            }
            return ContextUtils(newContext)
        }
    }
}