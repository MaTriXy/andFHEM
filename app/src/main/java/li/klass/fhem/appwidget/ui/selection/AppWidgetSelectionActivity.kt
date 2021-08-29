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

package li.klass.fhem.appwidget.ui.selection

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager.*
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import dagger.android.AndroidInjection
import kotlinx.coroutines.*
import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.WidgetConfigurationCreatedCallback
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetTypeProvider
import li.klass.fhem.appwidget.ui.widget.base.AppWidgetView
import li.klass.fhem.appwidget.update.AppWidgetInstanceManager
import li.klass.fhem.appwidget.update.AppWidgetUpdateService
import li.klass.fhem.appwidget.update.WidgetConfiguration
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.dagger.ScopedFragmentFactory
import li.klass.fhem.databinding.AppwidgetSelectionBinding
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DialogUtil
import javax.inject.Inject

abstract class AppWidgetSelectionActivity(private val widgetSize: WidgetSize) : AppCompatActivity() {
    @Inject
    lateinit var appWidgetInstanceManager: AppWidgetInstanceManager

    @Inject
    lateinit var applicationProperties: ApplicationProperties

    @Inject
    lateinit var appWidgetUpdateService: AppWidgetUpdateService

    @Inject
    lateinit var widgetTypeProvider: WidgetTypeProvider

    @Inject
    lateinit var scopedFragmentFactory: ScopedFragmentFactory

    @Inject
    lateinit var connectionService: ConnectionService

    private var widgetId: Int = 0

    private lateinit var binding: AppwidgetSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        supportFragmentManager.fragmentFactory = scopedFragmentFactory

        widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)

        if (ACTION_APPWIDGET_CONFIGURE != intent.action || widgetId == INVALID_APPWIDGET_ID) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        if (applicationProperties.getStringSharedPreference(SettingsKeys.STARTUP_PASSWORD, null) != null) {
            DialogUtil.showAlertDialog(this, R.string.app_title, R.string.widget_application_password, Runnable {
                finish()
                setResult(Activity.RESULT_CANCELED)
            })
        }
        val model: WidgetSelectionViewModel by viewModels()
        model.widgetSize = widgetSize
        model.roomClicked.observe(this, Observer {
            onRoomSelect(it)
        })
        model.deviceClicked.observe(this, Observer {
            onDeviceSelect(it)
        })
        model.otherWidgetClicked.observe(this, Observer {
            onOtherWidgetSelect(it)
        })

        binding = AppwidgetSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpNavigation()
    }

    private fun setUpNavigation() {
        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val navController = navHostFragment!!.navController
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
    }

    private fun openWidgetSelection(widgets: List<AppWidgetView>, vararg payload: String) {
        val widgetNames = widgets.map { getString(it.getWidgetName()) }.toTypedArray()
        AlertDialog.Builder(this)
                .setTitle(R.string.widget_type_selection)
                .setItems(widgetNames) { dialogInterface, position ->
                    dialogInterface.dismiss()

                    val widget = widgets[position]
                    createWidget(widget, *payload)
                }.show()
    }

    private fun createWidget(widget: AppWidgetView, vararg payload: String) {
        widget.createWidgetConfiguration(this, widgetId, connectionService.getSelectedId(), object : WidgetConfigurationCreatedCallback {
            override fun widgetConfigurationCreated(widgetConfiguration: WidgetConfiguration) {
                appWidgetInstanceManager.save(widgetConfiguration)

                GlobalScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        appWidgetUpdateService.updateWidget(widgetId)
                    }

                    setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId))
                    finish()
                }
            }
        }, *payload)
    }

    private fun onRoomSelect(roomName: String) {
        val widgets = widgetTypeProvider.getSupportedRoomWidgetsFor(widgetSize)
        openWidgetSelection(widgets, roomName)
    }

    private fun onDeviceSelect(clickedDevice: FhemDevice) {
        val widgetTypes = widgetTypeProvider.getSupportedDeviceWidgetsFor(widgetSize, clickedDevice)
        openWidgetSelection(widgetTypes, clickedDevice.name)
    }

    private fun onOtherWidgetSelect(widgetType: AppWidgetView) {
        createWidget(widgetType)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}
