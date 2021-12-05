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

package li.klass.fhem.update.backend.group

import android.content.Context
import li.klass.fhem.domain.CulHmSubType
import li.klass.fhem.domain.core.DeviceFunctionality
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CulHmDeviceGroupProvider @Inject
constructor() : DeviceGroupProvider("CUL_HM") {

    private fun getFunctionality(xmlListDevice: XmlListDevice): DeviceFunctionality? {
        val typeAttribute =
            xmlListDevice.getAttribute("subType") ?: xmlListDevice.getAttribute("genericDeviceType")
            ?: return null
        val model = xmlListDevice.getAttribute("model")
        val functionality = CulHmSubType.subTypeFor(typeAttribute)?.functionality
        val channel1 = xmlListDevice.getInternal("channel_01")
        if (channel1 != null) {
            return DeviceFunctionality.FHEM
        }

        return functionality ?: functionalityForModel(model)
    }

    override fun groupFor(xmlListDevice: XmlListDevice, context: Context): String? =
        getFunctionality(xmlListDevice)?.getCaptionText(context)

    private fun functionalityForModel(model: String?): DeviceFunctionality? {
        return when (model) {
            "HM-Sen-Wa-Od" -> DeviceFunctionality.FILL_STATE
            else -> null
        }
    }
}
