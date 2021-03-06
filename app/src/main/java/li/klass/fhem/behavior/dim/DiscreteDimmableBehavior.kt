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

package li.klass.fhem.behavior.dim

import android.content.Context
import com.google.common.base.Optional
import com.google.common.base.Predicate
import com.google.common.collect.FluentIterable.from
import com.google.common.collect.ImmutableList
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.SetList
import java.util.*
import java.util.regex.Pattern

class DiscreteDimmableBehavior(val foundDimStates: ImmutableList<String>) : DimmableTypeBehavior {

    override fun getDimLowerBound(): Float = 0f

    override fun getDimStep(): Float = 1f

    override fun getCurrentDimPosition(device: FhemDevice): Float {
        val state = device.internalState
        val position = getPositionForDimState(state)
        return if (position == -1f) 0f else position
    }

    override fun getDimUpperBound(): Float = foundDimStates.size.toFloat()

    override fun getDimStateForPosition(fhemDevice: FhemDevice, position: Float): String {
        val pos = position.toInt()
        if (pos.toFloat() <= getDimLowerBound()) {
            return "off"
        } else if (pos.toFloat() >= getDimUpperBound()) {
            return "on"
        }
        return foundDimStates[pos - 1]
    }

    override fun getPositionForDimState(dimState: String): Float {
        if ("on".equals(dimState, ignoreCase = true)) {
            return getDimUpperBound()
        } else if ("off".equals(dimState, ignoreCase = true)) {
            return getDimLowerBound()
        }
        return (foundDimStates.indexOf(dimState) + 1).toFloat()
    }

    override fun getStateName(): String = "state"

    override fun switchTo(stateUiService: StateUiService, context: Context, fhemDevice: FhemDevice, connectionId: String?, state: Float) {
        stateUiService.setState(fhemDevice, getDimStateForPosition(fhemDevice, state), context, connectionId)
    }

    companion object {

        private val dimStatePattern = Pattern.compile("dim[0-9]+[%]?")!!
        private val dimmableState = Predicate<String> { input -> dimStatePattern.matcher(input!!).matches() }
        private val compareByDimValue: Comparator<String> = Comparator { lhs, rhs ->
            val leftToCompare = lhs.replace("dim", "").replace("%", "")
            val rightToCompare = rhs.replace("dim", "").replace("%", "")

            Integer.parseInt(leftToCompare).compareTo(Integer.parseInt(rightToCompare))
        }

        fun supports(device: FhemDevice) = behaviorFor(device.setList).isPresent

        fun behaviorFor(setList: SetList): Optional<DiscreteDimmableBehavior> {

            val keys = setList.sortedKeys
            val foundDimStates = from(keys).filter(dimmableState).toSortedList(compareByDimValue)

            return if (foundDimStates.isEmpty())
                Optional.absent()
            else
                Optional.of(DiscreteDimmableBehavior(foundDimStates))
        }
    }
}
