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

package li.klass.fhem.appwidget.ui.widget.base;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.ui.widget.WidgetConfigurationCreatedCallback;
import li.klass.fhem.appwidget.ui.widget.WidgetType;
import li.klass.fhem.appwidget.update.WidgetConfiguration;
import li.klass.fhem.connection.backend.ConnectionService;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.ui.FragmentType;
import li.klass.fhem.update.backend.DeviceListService;
import li.klass.fhem.update.backend.device.configuration.DeviceConfiguration;
import li.klass.fhem.update.backend.device.configuration.DeviceConfigurationProvider;
import li.klass.fhem.update.backend.xmllist.ViewItemConfig;

import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT;
import static li.klass.fhem.util.ReflectionUtil.getValueAndDescriptionForAnnotation;

public abstract class DeviceAppWidgetView extends AppWidgetView {

    public static final String TAG = DeviceAppWidgetView.class.getName();

    @Inject
    public DeviceListService deviceListService;

    @Inject
    public DeviceConfigurationProvider deviceConfigurationProvider;

    @Inject
    protected ConnectionService connectionService;

    public static final Logger LOG = LoggerFactory.getLogger(DeviceAppWidgetView.class);

    public boolean supports(FhemDevice device, Context context) {
        boolean supportsFromJson = supportsFromJsonConfiguration(device);
        boolean supportsFromAnnotation = supportsFromAnnotation(device);

        return supportsFromJson || supportsFromAnnotation;
    }

    private boolean supportsFromAnnotation(FhemDevice device) {
        if (!device.getClass().isAnnotationPresent(SupportsWidget.class)) return false;

        if (!device.supportsWidget(this.getClass())) {
            return false;
        }

        SupportsWidget annotation = device.getClass().getAnnotation(SupportsWidget.class);
        Class<? extends DeviceAppWidgetView>[] supportedWidgetViews = annotation.value();
        for (Class<? extends DeviceAppWidgetView> supportedWidgetView : supportedWidgetViews) {
            if (supportedWidgetView.equals(getClass())) {
                return true;
            }
        }
        return false;
    }

    private boolean supportsFromJsonConfiguration(FhemDevice device) {
        Optional<DeviceConfiguration> deviceConfiguration = device.getDeviceConfiguration();
        if (deviceConfiguration.isPresent()) {
            Set<String> supportedWidgets = deviceConfiguration.get().getSupportedWidgets();
            for (String supportedWidget : supportedWidgets) {
                if (getClass().getSimpleName().equalsIgnoreCase(supportedWidget)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public RemoteViews createView(Context context, WidgetConfiguration widgetConfiguration) {
        RemoteViews views = super.createView(context, widgetConfiguration);
        Log.i(TAG, "creating appwidget view for " + widgetConfiguration);

        if (shouldSetDeviceName()) {
            String deviceName = deviceNameFrom(widgetConfiguration);

            FhemDevice device = getDeviceFor(deviceName, widgetConfiguration.connectionId);
            if (device == null) return null;

            views.setTextViewText(R.id.deviceName, device.getWidgetName());
        }

        return views;
    }

    public String deviceNameFrom(WidgetConfiguration widgetConfiguration) {
        return widgetConfiguration.payload.get(0);
    }

    public boolean shouldSetDeviceName() {
        return true;
    }

    private FhemDevice getDeviceFor(String deviceName, Optional<String> connectionId) {
        return deviceListService.getDeviceForName(deviceName, connectionId.orNull()).orNull();
    }

    protected void openDeviceDetailPageWhenClicking(int viewId, RemoteViews view, FhemDevice device, WidgetConfiguration widgetConfiguration, Context context) {
        PendingIntent pendingIntent = createOpenDeviceDetailPagePendingIntent(device, widgetConfiguration, context);

        view.setOnClickPendingIntent(viewId, pendingIntent);
    }

    protected PendingIntent createOpenDeviceDetailPagePendingIntent(FhemDevice device, WidgetConfiguration widgetConfiguration, Context context) {
        Intent openIntent = createOpenDeviceDetailPageIntent(device, widgetConfiguration, context);
        return PendingIntent.getActivity(context, widgetConfiguration.widgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected Intent createOpenDeviceDetailPageIntent(FhemDevice device, WidgetConfiguration widgetConfiguration, Context context) {
        return new Intent(context, AndFHEMMainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(FRAGMENT, FragmentType.DEVICE_DETAIL)
                .putExtra(DEVICE_NAME, device.getName())
                .putExtra(CONNECTION_ID, widgetConfiguration.connectionId.orNull())
                .putExtra("unique", "foobar://" + SystemClock.elapsedRealtime());
    }

    @Override
    public void createWidgetConfiguration(Context context, WidgetType widgetType, int appWidgetId,
                                          WidgetConfigurationCreatedCallback callback, String... payload) {
        Optional<FhemDevice> device = deviceListService.getDeviceForName(payload[0], null);
        if (device.isPresent()) {
            createDeviceWidgetConfiguration(context, widgetType, appWidgetId, device.get(), callback);
        } else {
            Log.i(TAG, "cannot find device for " + payload[0]);
        }
    }

    protected String valueForAnnotation(FhemDevice device, Class<? extends Annotation> annotationCls, Context context) {
        Optional<DeviceConfiguration> configuration = deviceConfigurationProvider.configurationFor(device);
        if (configuration.isPresent()) {
            Set<ViewItemConfig> states = configuration.get().getStates();
            for (ViewItemConfig state : states) {
                if (state.getMarkers().contains(annotationCls.getSimpleName())) {
                    return device.getXmlListDevice().stateValueFor(state.getKey()).orNull();
                }
            }
        }
        return getValueAndDescriptionForAnnotation(device, annotationCls, context);
    }

    protected void createDeviceWidgetConfiguration(Context context, WidgetType widgetType, int appWidgetId,
                                                   FhemDevice device, WidgetConfigurationCreatedCallback callback) {
        Optional<String> connectionId = getCurrentConnectionId(context);
        callback.widgetConfigurationCreated(new WidgetConfiguration(appWidgetId, widgetType, connectionId, ImmutableList.of(device.getName())));
    }

    @NonNull
    protected Optional<String> getCurrentConnectionId(Context context) {
        return Optional.of(connectionService.getSelectedId(context));
    }

    protected void fillWidgetView(Context context, RemoteViews view,
                                  WidgetConfiguration widgetConfiguration) {
        FhemDevice device = getDeviceFor(deviceNameFrom(widgetConfiguration), widgetConfiguration.connectionId);
        if (device != null) {
            view.setTextViewText(R.id.deviceName, device.getWidgetName());
            fillWidgetView(context, view, device, widgetConfiguration);
        } else {
            Log.i(TAG, "cannot find device for " + deviceNameFrom(widgetConfiguration));
        }
    }

    protected abstract void fillWidgetView(Context context, RemoteViews view, FhemDevice device,
                                           WidgetConfiguration widgetConfiguration);
}
