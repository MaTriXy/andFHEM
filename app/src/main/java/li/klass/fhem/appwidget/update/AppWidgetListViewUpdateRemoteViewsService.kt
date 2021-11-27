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

package li.klass.fhem.appwidget.update

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.widget.RemoteViewsService
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.WidgetTypeProvider
import li.klass.fhem.appwidget.ui.widget.base.DeviceListAppWidgetView
import li.klass.fhem.appwidget.ui.widget.base.EmptyRemoteViewsFactory
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.update.backend.DeviceListService
import org.slf4j.LoggerFactory
import javax.inject.Inject

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class AppWidgetListViewUpdateRemoteViewsService : RemoteViewsService() {

    @Inject
    lateinit var deviceListService: DeviceListService
    @Inject
    lateinit var widgetTypeProvider: WidgetTypeProvider

    override fun onCreate() {
        super.onCreate()
        (application as AndFHEMApplication).daggerComponent.inject(this)
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory? {
        val appWidgetId = intent.getIntExtra(BundleExtraKeys.APP_WIDGET_ID, -1)
        val widgetTypeName =
            intent.getStringExtra(BundleExtraKeys.APP_WIDGET_TYPE_NAME) ?: return null
        val widgetType = WidgetType.valueOf(widgetTypeName)
        val deviceName = intent.getStringExtra(BundleExtraKeys.DEVICE_NAME) ?: return null
        val connectionId = intent.getStringExtra(BundleExtraKeys.CONNECTION_ID)
        val device = deviceListService.getDeviceForName(deviceName, connectionId)
        if (device == null) {
            LOG.error("device is null, at least in the current connection")
            return null
        }

        if (appWidgetId == -1) {
            LOG.error("no appwidget id given")
            return null
        }

        val view = widgetTypeProvider.widgetFor(widgetType)
        if (view !is DeviceListAppWidgetView<*>) {
            LOG.error(
                    "can only handle list widget views, got " + view.javaClass.name)

            /*
            * We may not return null here, as the source code within {@link RemoteViewsService#onBind}
            * depends on a factory which is _non_ null. This is why we simply return a factory
            * handling no data at all.
            */
            return EmptyRemoteViewsFactory.INSTANCE
        }

        return view.getRemoteViewsFactory(this, device, appWidgetId)
    }

    companion object {
        val LOG = LoggerFactory.getLogger(AppWidgetListViewUpdateRemoteViewsService::class.java)!!
    }
}
