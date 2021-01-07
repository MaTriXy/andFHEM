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

package li.klass.fhem.adapter.devices.toggle

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import li.klass.fhem.adapter.devices.hook.ButtonHook
import li.klass.fhem.adapter.devices.hook.ButtonHook.*
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.testutil.MockRule
import li.klass.fhem.update.backend.device.configuration.DeviceConfiguration
import li.klass.fhem.update.backend.device.configuration.DeviceConfigurationProvider
import li.klass.fhem.update.backend.xmllist.DeviceNode
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DataProviderRunner::class)
class OnOffBehaviorTest {
    @get:Rule
    val mockitoRule = MockRule()

    @InjectMockKs
    private lateinit var onOffBehavior: OnOffBehavior

    @MockK
    private lateinit var deviceHookProvider: DeviceHookProvider

    @MockK
    private lateinit var deviceConfigurationProvider: DeviceConfigurationProvider

    @MockK
    private lateinit var deviceConfiguration: DeviceConfiguration

    @Before
    fun setUp() {
        every { deviceConfigurationProvider.configurationFor(ofType(FhemDevice::class)) } returns deviceConfiguration
    }

    @UseDataProvider("isOnProvider")
    @Test
    fun should_recognize_on_and_off_states_correctly(testCase: IsOnTestCase) {

        //  given
        val xmlListDevice = XmlListDevice("BLUB")
                .apply {
                    setInternal("NAME", "Name")
                    setAttribute("eventMap", testCase.eventMap)
                    setState("state", testCase.readingsState)
                    setHeader("sets", testCase.setList)
                }
        every { deviceConfiguration.stateAttributeName } returns "state"
        every { deviceConfiguration.additionalOffStateNames } returns testCase.additionalOffStates
        every { deviceConfiguration.additionalOnStateNames } returns testCase.additionalOnStates
        every { deviceHookProvider.getOffStateName(any()) } returns "off"
        every { deviceHookProvider.getOnStateName(any()) } returns "on"

        val device = FhemDevice(xmlListDevice)
        device.xmlListDevice.setInternal("STATE", testCase.internalState)
        every { deviceHookProvider.getOffStateName(device) } returns "off"

        // expect
        assertThat(onOffBehavior.isOnByState(device)).isEqualTo(testCase.expected)
        assertThat(onOffBehavior.isOffByState(device)).isEqualTo(!testCase.expected)
    }

    @UseDataProvider("isOnProvider")
    @Test
    fun should_calculate_on_off(testCase: IsOnTestCase) {
        //  given
        val xmlListDevice = XmlListDevice("BLUB")
                .apply {
                    setInternal("NAME", "Name")
                    setAttribute("eventMap", testCase.eventMap)
                    setState("state", testCase.readingsState)
                    setHeader("sets", testCase.setList)
                }
        every { deviceConfiguration.stateAttributeName } returns "state"
        every { deviceConfiguration.additionalOffStateNames } returns testCase.additionalOffStates
        every { deviceConfiguration.additionalOnStateNames } returns testCase.additionalOnStates
        every { deviceHookProvider.getOffStateName(any()) } returns "off"
        every { deviceHookProvider.getOnStateName(any()) } returns "on"

        val device = FhemDevice(xmlListDevice)
        device.xmlListDevice.setInternal("STATE", testCase.internalState)
        every { deviceHookProvider.getOffStateName(device) } returns "off"
        every { deviceHookProvider.invertState(device) } returns false

        assertThat(onOffBehavior.isOn(device)).isEqualTo(testCase.expected)
    }

    @UseDataProvider("isOnProvider")
    @Test
    fun should_handle_invert_state(testCase: IsOnTestCase) {
        val xmlListDevice2 = XmlListDevice("BLA")
                .apply {
                    setInternal("NAME", "Name")
                    setAttribute("eventMap", testCase.eventMap)
                    setState("state", testCase.readingsState)
                    setHeader("sets", testCase.setList)
                }
        every { deviceConfiguration.stateAttributeName } returns "state"
        every { deviceConfiguration.additionalOffStateNames } returns testCase.additionalOffStates
        every { deviceConfiguration.additionalOnStateNames } returns testCase.additionalOnStates
        every { deviceHookProvider.getOffStateName(any()) } returns "off"
        every { deviceHookProvider.getOnStateName(any()) } returns "on"

        val device2 = FhemDevice(xmlListDevice2)
        device2.xmlListDevice.setInternal("STATE", testCase.internalState)
        every { deviceHookProvider.getOffStateName(device2) } returns "off"
        every { deviceHookProvider.invertState(device2) } returns true

        // expect
        assertThat(onOffBehavior.isOn(device2)).isEqualTo(!testCase.expected)
    }

    @Test
    @UseDataProvider("hookProvider")
    @Throws(Exception::class)
    fun isOnConsideringHooks(testCase: HookProviderTestCase) {
        val device = FhemDevice(XmlListDevice("GENERIC", HashMap(), HashMap(), HashMap(), HashMap()))
        every { deviceConfiguration.stateAttributeName } returns "state"
        every { deviceHookProvider.buttonHookFor(device) } returns testCase.hook
        every { deviceHookProvider.invertState(any()) } returns false
        every { deviceHookProvider.getOffStateName(device) } returns "off"
        every { deviceHookProvider.getOnStateName(device) } returns "on"
        every { deviceConfiguration.additionalOnStateNames } returns emptySet()
        every { deviceConfiguration.additionalOffStateNames } returns emptySet()
        device.xmlListDevice.setInternal("STATE", if (testCase.isOn) "on" else "off")

        val result = onOffBehavior.isOnConsideringHooks(device)

        assertThat(result).isEqualTo(testCase.expected)
    }

    @Test
    @UseDataProvider("supportsProvider")
    fun calculateSupports(testCase: SupportsTestCase) {
        val sets = testCase.setList.joinToString(" ")
        val headers = mapOf("sets" to DeviceNode(DeviceNode.DeviceNodeType.HEADER, "sets", sets, DateTime.now()))
                .toMutableMap()
        val device = FhemDevice(XmlListDevice("GENERIC", HashMap(), HashMap(), HashMap(), headers))

        every { deviceConfiguration.stateAttributeName } returns "state"
        every { deviceHookProvider.getOffStateName(device) } returns testCase.offStateNameHook
        every { deviceHookProvider.getOnStateName(device) } returns testCase.onStateNameHook
        every { deviceHookProvider.buttonHookFor(device) } returns NORMAL
        every { deviceConfiguration.additionalOnStateNames } returns emptySet()
        every { deviceConfiguration.additionalOffStateNames } returns emptySet()

        val supports = onOffBehavior.supports(device)

        assertThat(supports).isEqualTo(testCase.expectedSupports)
    }

    data class IsOnTestCase(val internalState: String,
                            val readingsState: String = internalState,
                            val eventMap: String = "",
                            val setList: String = "",
                            val expected: Boolean,
                            val additionalOnStates: Set<String> = emptySet(),
                            val additionalOffStates: Set<String> = emptySet())

    data class HookProviderTestCase(val hook: ButtonHook, val isOn: Boolean, val expected: Boolean)

    data class SupportsTestCase(val setList: Set<String>,
                                val onStateNameHook: String?,
                                val offStateNameHook: String?,
                                val expectedSupports: Boolean)

    companion object {
        @DataProvider
        @JvmStatic
        fun supportsProvider(): List<SupportsTestCase> {
            return listOf(
                    SupportsTestCase(
                            setList = setOf("on", "off"),
                            onStateNameHook = null,
                            offStateNameHook = null,
                            expectedSupports = true
                    ),
                    SupportsTestCase(
                            setList = setOf("ON", "OFF"),
                            onStateNameHook = null,
                            offStateNameHook = null,
                            expectedSupports = true
                    ),
                    SupportsTestCase(
                            setList = setOf("on", "bla"),
                            onStateNameHook = null,
                            offStateNameHook = "bla",
                            expectedSupports = true
                    ),
                    SupportsTestCase(
                            setList = setOf("blub", "bla"),
                            onStateNameHook = "blub",
                            offStateNameHook = "bla",
                            expectedSupports = true
                    ),
                    SupportsTestCase(
                            setList = setOf("bla", "blub"),
                            onStateNameHook = null,
                            offStateNameHook = null,
                            expectedSupports = false
                    )
            )
        }

        @DataProvider
        @JvmStatic
        fun isOnProvider(): List<IsOnTestCase> {
            return listOf(
                    IsOnTestCase(internalState = "on", expected = true),
                    IsOnTestCase(internalState = "off", expected = false),
                    IsOnTestCase(internalState = "dim20%", expected = true),
                    IsOnTestCase(internalState = "off-for-timer 2000", setList = "off-for-timer", expected = false),
                    IsOnTestCase(internalState = "on-for-timer 2000", setList = "on-for-timer", expected = true),
                    IsOnTestCase(internalState = "B0", eventMap = "BI:on B0:off", setList = "internalState:BI,B0 on off", expected = false),
                    IsOnTestCase(internalState = "B1", eventMap = "BI:on B0:off", setList = "internalState:BI,B0 on off", expected = true),
                    IsOnTestCase(internalState = "100", additionalOnStates = setOf("100"), additionalOffStates = setOf("0"), expected = true),
                    IsOnTestCase(internalState = "0", additionalOnStates = setOf("100"), additionalOffStates = setOf("0"), expected = false),
                    IsOnTestCase(internalState = "ON", setList = "on off", expected = true),
                    IsOnTestCase(internalState = "OFF", setList = "on off", expected = false),
                    IsOnTestCase(internalState = "off", setList = "on off", eventMap = "/gpio 12 on:on/gpio 12 off:off/gpio 12 gpio:off/gpio 12 output:off/", expected = false),
                    IsOnTestCase(internalState = "on", setList = "on off", eventMap = "/gpio 12 on:on/gpio 12 off:off/gpio 12 gpio:off/gpio 12 output:off/", expected = true),
                    IsOnTestCase(internalState = "off", readingsState = "on", setList = "on off", expected = false),
                    IsOnTestCase(internalState = "Temperatur: 26.5 C", readingsState = "off", expected = false),
                    IsOnTestCase(internalState = "Temperatur: 26.5 C", readingsState = "on", expected = true)
            )
        }

        @DataProvider
        @JvmStatic
        fun hookProvider(): List<HookProviderTestCase> {
            return listOf(
                    HookProviderTestCase(hook = ON_DEVICE, isOn = true, expected = true),
                    HookProviderTestCase(hook = ON_DEVICE, isOn = false, expected = true),
                    HookProviderTestCase(hook = OFF_DEVICE, isOn = true, expected = false),
                    HookProviderTestCase(hook = OFF_DEVICE, isOn = false, expected = false),
                    HookProviderTestCase(hook = NORMAL, isOn = true, expected = true),
                    HookProviderTestCase(hook = NORMAL, isOn = false, expected = false)
            )
        }
    }
}