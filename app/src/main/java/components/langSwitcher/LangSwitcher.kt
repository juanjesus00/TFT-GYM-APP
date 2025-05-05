package components.langSwitcher

import android.content.Context
import android.content.res.Configuration
import uiPrincipal.LanguageManager
import java.util.Locale

fun getLocalizedResources(context: Context): android.content.res.Resources? {
    val locale = Locale(LanguageManager.languageCode)
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    return context.createConfigurationContext(config).resources
}

fun getStringByName(context: Context, stringName: String): String? {
    val resources = getLocalizedResources(context)
    val resId = resources?.getIdentifier(stringName, "string", context.packageName)
    return if (resId != 0) resId?.let { resources?.getString(it) } else "String not found"
}