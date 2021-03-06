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

package li.klass.fhem.devices.list.backend

import android.content.Context
import li.klass.fhem.adapter.rooms.GroupComparator
import li.klass.fhem.domain.core.DeviceFunctionality
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.RoomDeviceList
import javax.inject.Inject

// No singleton (to reread the ordering configuration regularly)
class ViewableElementsCalculator @Inject constructor(
        private val parentsProvider: ParentsProvider
) {
    fun calculateElements(context: Context, roomDeviceList: RoomDeviceList): List<Element> {

        val parents = parentsProvider.parentsFor(roomDeviceList)
        val groupComparator = GroupComparator(DeviceFunctionality.UNKNOWN.getCaptionText(context), parents)
        val elementsInGroup = parents
                .map { Pair(it, roomDeviceList.getDevicesOfFunctionality(it)) }
                .toMap()

        val groups = elementsInGroup.keys.sortedWith(groupComparator)
                .filter { elementsInGroup[it]!!.isNotEmpty() }

        return groups
                .flatMap { group ->
                    val devices = elementsInGroup[group]!!
                            .sortedWith(FhemDevice.BY_NAME)
                            .map { Element.Device(it) }
                    listOf(Element.Group(group)) + devices
                }
    }

    sealed class Element {
        data class Device(val device: FhemDevice) : Element()
        data class Group(val group: String) : Element()
    }
}