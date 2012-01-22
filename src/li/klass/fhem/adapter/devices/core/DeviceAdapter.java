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

package li.klass.fhem.adapter.devices.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.activities.graph.ChartingActivity;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.Device;
import li.klass.fhem.service.graph.ChartSeriesDescription;

public abstract class DeviceAdapter<D extends Device> {


    public boolean supports(Class<? extends Device> deviceClass) {
        return getSupportedDeviceClass().isAssignableFrom(deviceClass);
    }

    @SuppressWarnings("unchecked")
    public View getOverviewView(LayoutInflater layoutInflater, Device device) {
        return getDeviceOverviewView(layoutInflater, (D) device);
    }

    @SuppressWarnings("unchecked")
    public View getDetailView(Context context, Device device) {
        if (supportsDetailView(device)) {
            return getDeviceDetailView(context, (D) device);
        }
        return null;
    }

    public void gotoDetailView(Context context, Device device) {
        if (! supportsDetailView(device)) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtras(new Bundle());
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.ROOM_NAME, device.getRoom());

        intent = onFillDeviceDetailIntent(context, device, intent);
        if (intent != null) {
            context.startActivity(intent);
        }
    }


    public abstract int getDetailViewLayout();
    public abstract boolean supportsDetailView(Device device);
    protected abstract View getDeviceDetailView(Context context, D device);
    protected abstract Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent);

    public abstract Class<? extends Device> getSupportedDeviceClass();
    protected abstract View getDeviceOverviewView(LayoutInflater layoutInflater, D device);


    protected void setTextViewOrHideTableRow(View view, int tableRowId, int textFieldLayoutId, int stringId) {
        String value = AndFHEMApplication.getContext().getString(stringId);
        setTextViewOrHideTableRow(view, tableRowId, textFieldLayoutId, value);
    }
    
    protected void setTextViewOrHideTableRow(View view, int tableRowId, int textFieldLayoutId, String value) {
        TableRow tableRow = (TableRow) view.findViewById(tableRowId);

        if (hideIfNull(tableRow, value)) {
            return;
        }

        setTextView(view, textFieldLayoutId, value);
    }

    protected void setTextView(View view, int textFieldLayoutId, String value) {
        TextView textView = (TextView) view.findViewById(textFieldLayoutId);
        if (textView != null) {
            textView.setText(value);
        }
    }

    protected boolean hideIfNull(View view, int id, Object valueToCheck) {
        View layoutElement = view.findViewById(id);
        return layoutElement != null && hideIfNull(layoutElement, valueToCheck);
    }

    protected boolean hideIfNull(View layoutElement, Object valueToCheck) {
        if (valueToCheck == null || valueToCheck instanceof String && ((String) valueToCheck).length() == 0) {
            layoutElement.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    protected boolean createPlotButton(final Context context, View view, int buttonLayoutId, Object hideButtonIfNull,
                                       final D device, int yTitleId, int... columnSpecifications) {
        ChartSeriesDescription[] descriptions = new ChartSeriesDescription[columnSpecifications.length];

        for (int i = 0; i < columnSpecifications.length; i++) {
            int columnSpecification = columnSpecifications[i];
            descriptions[i] = new ChartSeriesDescription(columnSpecification, false);
        }
        return createPlotButton(context, view, buttonLayoutId, hideButtonIfNull, device, yTitleId, descriptions);
    }



    protected boolean createPlotButton(final Context context, View view, int buttonLayoutId, Object hideButtonIfNull,
                                       final D device, final int yTitleId, final ChartSeriesDescription... seriesDescriptions) {
        if (! hideIfNull(view, buttonLayoutId, hideButtonIfNull)) {
            Button button = (Button) view.findViewById(buttonLayoutId);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String yTitle = context.getResources().getString(yTitleId);
                    ChartingActivity.showChart(context, device, yTitle, seriesDescriptions);
                }
            });

            return false;
        } else {
            return true;
        }
    }

}
