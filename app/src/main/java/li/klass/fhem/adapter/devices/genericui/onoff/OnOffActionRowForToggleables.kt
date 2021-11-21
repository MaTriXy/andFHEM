/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.adapter.devices.genericui.onoff

import android.content.Context
import android.view.View
import android.widget.TableRow
import li.klass.fhem.adapter.devices.hook.ButtonHook
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.behavior.toggle.OnOffBehavior
import li.klass.fhem.domain.core.FhemDevice

class OnOffActionRowForToggleables(layoutId: Int,
                                   private val hookProvider: DeviceHookProvider,
                                   private val onOffBehavior: OnOffBehavior,
                                   stateUiService: StateUiService,
                                   text: Int?,
                                   connectionId: String?
) : OnOffStateActionRow(layoutId, text, connectionId, stateUiService) {

    override fun createRow(device: FhemDevice, context: Context): TableRow {
        val tableRow = super.createRow(device, context)

        val onButton = findOnButton(tableRow)
        val offButton = findOffButton(tableRow)

        when (hookProvider.buttonHookFor(device)) {
            ButtonHook.ON_DEVICE -> {
                offButton.visibility = View.GONE
                onButton.visibility = View.VISIBLE
            }
            ButtonHook.OFF_DEVICE -> {
                onButton.visibility = View.GONE
                offButton.visibility = View.VISIBLE
            }
            else -> {
            }
        }

        return tableRow
    }

    override fun getOnStateName(device: FhemDevice, context: Context): String =
            onOffBehavior.getOnStateName(device) ?: super.getOnStateName(device, context)

    override fun getOffStateName(device: FhemDevice, context: Context): String =
            onOffBehavior.getOffStateName(device) ?: super.getOffStateName(device, context)

    override fun isOn(device: FhemDevice, context: Context): Boolean = onOffBehavior.isOn(device)
}
