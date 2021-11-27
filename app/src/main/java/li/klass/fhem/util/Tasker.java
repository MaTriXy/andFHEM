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

package li.klass.fhem.util;

import android.content.Context;
import android.content.Intent;

import li.klass.fhem.activities.locale.LocaleIntentConstants;
import li.klass.fhem.activities.locale.TaskerPlugin;
import li.klass.fhem.activities.locale.condition.query.ConditionQueryLocaleSettingActivity;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;

public class Tasker {
    public static void sendTaskerNotifyIntent(Context context, String deviceName, String key, String value) {
        Intent taskerNotifyIntent = new Intent(Actions.INSTANCE.getEXT_DEVICE_STATE_NOTIFY());
        taskerNotifyIntent.putExtra(BundleExtraKeys.ACTION, "deviceStateChange");
        taskerNotifyIntent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        taskerNotifyIntent.putExtra(BundleExtraKeys.STATE_NAME, key);
        taskerNotifyIntent.putExtra(BundleExtraKeys.STATE_VALUE, value);
        context.sendBroadcast(taskerNotifyIntent);
    }

    public static void requestQuery(Context context) {
        Intent requestQueryIntentForTasker = new Intent(LocaleIntentConstants.ACTION_REQUEST_QUERY)
                .putExtra(LocaleIntentConstants.EXTRA_ACTIVITY,
                        ConditionQueryLocaleSettingActivity.class.getName());;
        TaskerPlugin.Event.addPassThroughMessageID(requestQueryIntentForTasker);
        context.sendBroadcast(requestQueryIntentForTasker);
    }
}
