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

package li.klass.fhem.appwidget.view.widget.medium

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.hook.ButtonHook
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior
import li.klass.fhem.appwidget.WidgetConfiguration
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.core.ToggleableDevice
import li.klass.fhem.service.intent.DeviceIntentService
import javax.inject.Inject

open class ToggleWidgetView : DeviceAppWidgetView() {
    @Inject
    lateinit var deviceHookProvider: DeviceHookProvider

    @Inject
    lateinit var onOffBehavior: OnOffBehavior

    override fun getWidgetName(): Int = R.string.widget_toggle

    override fun getContentView(): Int = R.layout.appwidget_toggle

    override fun fillWidgetView(context: Context, view: RemoteViews, device: FhemDevice, widgetConfiguration: WidgetConfiguration) {
        var isOn = onOffBehavior.isOn(device)

        val actionIntent: Intent

        val hook = deviceHookProvider.buttonHookFor(device)

        if (hook != ButtonHook.ON_DEVICE && hook != ButtonHook.OFF_DEVICE) {
            actionIntent = Intent(Actions.DEVICE_WIDGET_TOGGLE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(BundleExtraKeys.APP_WIDGET_ID, widgetConfiguration.widgetId)
                    .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                    .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId.orNull())
        } else {
            actionIntent = Intent(Actions.DEVICE_SET_STATE)
                    .putExtra(BundleExtraKeys.DEVICE_NAME, device.name)
                    .putExtra(BundleExtraKeys.CONNECTION_ID, widgetConfiguration.connectionId.orNull())


            when (hook) {
                ButtonHook.ON_DEVICE -> {
                    isOn = true
                    actionIntent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, deviceHookProvider.getOnStateName(device))
                }
                ButtonHook.OFF_DEVICE -> {
                    isOn = false
                    actionIntent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, deviceHookProvider.getOffStateName(device))
                }
            }
        }
        actionIntent.setClass(context, DeviceIntentService::class.java)

        if (isOn) {
            view.setViewVisibility(R.id.toggleOff, View.GONE)
            view.setViewVisibility(R.id.toggleOn, View.VISIBLE)
            view.setTextViewText(R.id.toggleOn, device.getEventMapStateFor(deviceHookProvider.getOnStateName(device)))
        } else {
            view.setViewVisibility(R.id.toggleOff, View.VISIBLE)
            view.setViewVisibility(R.id.toggleOn, View.GONE)
            view.setTextViewText(R.id.toggleOff, device.getEventMapStateFor(deviceHookProvider.getOffStateName(device)))
        }

        val pendingIntent = PendingIntent.getService(context, widgetConfiguration.widgetId, actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        view.setOnClickPendingIntent(R.id.toggleOff, pendingIntent)
        view.setOnClickPendingIntent(R.id.toggleOn, pendingIntent)

        openDeviceDetailPageWhenClicking(R.id.main, view, device, widgetConfiguration, context)
    }

    override fun supports(device: FhemDevice, context: Context): Boolean =
            device is ToggleableDevice && device.supportsToggle()

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }
}
