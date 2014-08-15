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

package li.klass.fhem.domain.culhm;

import org.junit.Test;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class THSensorTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributes() {
        CULHMDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getState(), is("T: 14.6 H: 67"));
        assertThat(device.getSubType(), is(CULHMDevice.SubType.TEMPERATURE_HUMIDITY));
        assertThat(device.supportsDim(), is(false));

        assertThat(device.getMeasuredTemp(), is("-2.4 (°C)"));
        assertThat(device.getHumidity(), is("67 (%)"));

        assertThat(device.getLogDevices(), is(notNullValue()));
        assertThat(device.getDeviceCharts().size(), is(1));

        assertThat(device.isSupported(), is(true));
    }

    @Test
    public void testOC3Sensor() {
        CULHMDevice device = getDeviceFor("oc3");

        assertThat(device, is(notNullValue()));
        assertThat(device.getMeasuredTemp(), is("5.1 (°C)"));
        assertThat(device.getHumidity(), is("92 (%)"));
        assertThat(device.getBrightness(), is("9"));
        assertThat(device.getSunshine(), is("224"));
        assertThat(device.getIsRaining(), is("no"));
        assertThat(device.getRain(), is("74.045 (l/m2)"));
    }

    @Override
    protected String getFileName() {
        return "THSensor.xml";
    }
}
