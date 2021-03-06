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

package li.klass.fhem.adapter.devices.core.cards.device.values

import android.content.Context
import android.support.v7.widget.CardView
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.core.cards.GenericDetailCardProvider
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceItemProvider
import li.klass.fhem.adapter.devices.core.deviceItems.XmlDeviceViewItem
import li.klass.fhem.domain.core.FhemDevice
import javax.inject.Inject

class InternalsCardProvider @Inject constructor(
        private val detailCardWithDeviceValuesProvider: DetailCardWithDeviceValuesProvider
) : GenericDetailCardProvider {
    override fun ordering(): Int = 50

    override fun provideCard(device: FhemDevice, context: Context, connectionId: String?): CardView? {
        return detailCardWithDeviceValuesProvider.createCard(device, connectionId,
                R.string.detailInternalsSection, InternalsItemProvider(), context)
    }

    class InternalsItemProvider : ItemProvider {
        override fun itemsFor(provider: XmlDeviceItemProvider, device: FhemDevice, showUnknown: Boolean, context: Context): Set<XmlDeviceViewItem> =
                provider.getInternalsFor(device, showUnknown, context)
    }
}