/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import org.mozilla.fenix.R
import org.mozilla.fenix.components.metrics.MetricServiceType
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.getPreferenceKey
import org.mozilla.fenix.ext.nav
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.ext.showToolbar

/**
 * Lets the user toggle telemetry on/off.
 */
class DataChoicesFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = requireContext()

        // --- MODIFICATION 1 : On coupe immédiatement les services au démarrage de l'écran ---
        context.components.analytics.metrics.stop(MetricServiceType.Data)
        context.components.analytics.metrics.stop(MetricServiceType.Marketing)

        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this) { _, key ->
            // --- MODIFICATION 2 : On force l'arrêt (.stop) à chaque événement, on ne démarre (.start) plus JAMAIS ---
            if (key == getPreferenceKey(R.string.pref_key_telemetry)) {
                context.components.analytics.metrics.stop(MetricServiceType.Data)
                context.components.analytics.experiments.resetTelemetryIdentifiers()
            } else if (key == getPreferenceKey(R.string.pref_key_marketing_telemetry)) {
                context.components.analytics.metrics.stop(MetricServiceType.Marketing)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.preferences_data_collection))
        updateStudiesSection()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.data_choices_preferences, rootKey)

        // --- MODIFICATION 3 : On force l'interrupteur Télémétrie à "Désactivé" et on le grise ---
        requirePreference<SwitchPreference>(R.string.pref_key_telemetry).apply {
            isChecked = false // Forcé à décoché
            isEnabled = false // Grisé et impossible à cliquer

            val appName = context.getString(R.string.app_name)
            summary = context.getString(R.string.preferences_usage_data_description, appName)

            onPreferenceChangeListener = SharedPreferenceUpdater()
        }

        // --- MODIFICATION 4 : On force l'interrupteur Marketing à "Désactivé" et on le grise ---
        requirePreference<SwitchPreference>(R.string.pref_key_marketing_telemetry).apply {
            isChecked = false // Forcé à décoché
            isEnabled = false // Grisé et impossible à cliquer
            onPreferenceChangeListener = SharedPreferenceUpdater()
        }
    }

    private fun updateStudiesSection() {
        val studiesPreference = requirePreference<Preference>(R.string.pref_key_studies_section)

        // --- MODIFICATION 5 : On force l'affichage des Études (Nimbus) sur "Désactivé" et on bloque l'accès ---
        studiesPreference.summary = getString(R.string.studies_off) // Indique "Désactivé"
        studiesPreference.isEnabled = false // Rend la section incréable (on ne peut plus ouvrir le sous-menu)
    }
}
