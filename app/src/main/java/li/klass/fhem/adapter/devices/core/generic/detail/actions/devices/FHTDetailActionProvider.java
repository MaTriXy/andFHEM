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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.devices;

import android.content.Context;
import android.content.Intent;

import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.DeviceDetailActionProvider;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardAction;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardButton;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.state.FHTModeStateOverwrite;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.heating.schedule.configuration.FHTConfiguration;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.ui.FragmentType;
import li.klass.fhem.update.backend.xmllist.XmlListDevice;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DateTimeProvider;

import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_TARGET_STATE;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT;
import static li.klass.fhem.constants.BundleExtraKeys.HEATING_CONFIGURATION;

@Singleton
public class FHTDetailActionProvider extends DeviceDetailActionProvider {
    public static double MAXIMUM_TEMPERATURE = 30.5;
    public static double MINIMUM_TEMPERATURE = 5.5;

    @Inject
    public FHTDetailActionProvider(ApplicationProperties applicationProperties, DateTimeProvider dateTimeProvider) {
        addStateAttributeAction("mode", new FHTModeStateOverwrite(applicationProperties, dateTimeProvider));
    }

    @Override
    public List<ActionCardAction> actionsFor(Context context) {
        return ImmutableList.<ActionCardAction>of(
                new ActionCardButton(R.string.timetable, context) {
                    @Override
                    protected void onClick(XmlListDevice device, String connectionId, Context context) {
                        context.sendBroadcast(
                                new Intent(Actions.SHOW_FRAGMENT)
                                        .putExtra(FRAGMENT, FragmentType.FROM_TO_WEEK_PROFILE)
                                        .putExtra(CONNECTION_ID, connectionId)
                                        .putExtra(DEVICE_NAME, device.getName())
                                        .putExtra(HEATING_CONFIGURATION, new FHTConfiguration())
                        );
                    }
                },
                new ActionCardButton(R.string.requestRefresh, context) {
                    @Override
                    protected void onClick(XmlListDevice device, String connectionId, Context context) {
                        context.startService(
                                new Intent(Actions.DEVICE_SET_STATE)
                                        .setClass(context, DeviceIntentService.class)
                                        .putExtra(CONNECTION_ID, connectionId)
                                        .putExtra(DEVICE_NAME, device.getName())
                                        .putExtra(DEVICE_TARGET_STATE, "refreshvalues")
                        );
                    }
                }
        );
    }

    @Override
    protected String getDeviceType() {
        return "FHT";
    }
}
