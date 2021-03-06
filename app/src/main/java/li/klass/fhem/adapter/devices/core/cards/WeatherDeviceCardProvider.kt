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

package li.klass.fhem.adapter.devices.core.cards

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.device_detail_card_weather.view.*
import kotlinx.android.synthetic.main.weather_forecast_item.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.GlideApp
import li.klass.fhem.R
import li.klass.fhem.devices.backend.WeatherService
import li.klass.fhem.devices.backend.WeatherService.WeatherForecastInformation
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.util.DateFormatUtil
import li.klass.fhem.util.view.setTextOrHide
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class WeatherDeviceCardProvider @Inject constructor(
        private val weatherService: WeatherService
) : GenericDetailCardProvider {
    override fun ordering(): Int = 1

    override fun provideCard(device: FhemDevice, context: Context, connectionId: String?): CardView? {
        if (device.xmlListDevice.type != "Weather") {
            return null
        }
        val view = context.layoutInflater.inflate(R.layout.device_detail_card_weather, null, false)
        view.forecast.layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically() = false
        }
        view.forecast.adapter = Adapter()

        async(UI) {
            val forecasts = bg { weatherService.forecastsFor(device) }.await()
            updateListWith(view.forecast, forecasts)
            view.invalidate()
        }

        return view as CardView
    }

    private fun updateListWith(content: RecyclerView,
                               forecasts: List<WeatherForecastInformation>) {
        content.adapter = Adapter(forecasts)
        content.invalidate()
    }

    private class Adapter(
            private val elements: List<WeatherForecastInformation> = emptyList()
    ) : RecyclerView.Adapter<Adapter.WeatherViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder =
                WeatherViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.weather_forecast_item, parent, false))

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
            holder.apply {
                val element = elements[position]
                holder.view.apply {
                    date.text = "${element.weekday}. ${DateFormatUtil.ANDFHEM_DATE_FORMAT.print(element.date)}"
                    temperature.text = element.temperature
                    condition.text = element.condition

                    windChill.setTextOrHide(element.windChill, tableRowWindChill)
                    humidity.setTextOrHide(element.humidity, tableRowHumidity)
                    visibilityCondition.setTextOrHide(element.visibility, tableRowVisibilityCondition)

                    GlideApp.with(context)
                            .load(element.icon)
                            .error(R.drawable.empty)
                            .into(forecastWeatherImage)
                }
            }
        }

        override fun getItemCount(): Int = elements.size

        class WeatherViewHolder(val view: View) : RecyclerView.ViewHolder(view)
    }
}