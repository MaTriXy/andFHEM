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

package li.klass.fhem.constants;

public class Actions {
    private static final String prefix = Actions.class.getName() + ".";

    public static final String SHOW_FRAGMENT = prefix + "SHOW_FRAGMENT";

    public static final String SHOW_EXECUTING_DIALOG = prefix + "SHOW_EXECUTING_DIALOG";
    public static final String DISMISS_EXECUTING_DIALOG = prefix + "DISMISS_EXECUTING_DIALOG";
    public static final String BACK = prefix + "BACK";

    public static final String SHOW_TOAST = prefix + "SHOW_TOAST";
    public static final String SHOW_ALERT = prefix + "SHOW_ALERT";

    public static final String DEVICE_WIDGET_TOGGLE = prefix + "DEVICE_WIDGET_TOGGLE";
    public static final String DEVICE_WIDGET_TARGET_STATE = prefix + "DEVICE_WIDGET_TARGET_STATE";

    public static final String DO_UPDATE = prefix + "DO_UPDATE";
    public static final String DO_REMOTE_UPDATE = prefix + "DO_REMOTE_UPDATE";
    public static final String DEVICES_UPDATED = prefix + "REMOTE_DEVICES_UPDATED";
    public static final String REDRAW = prefix + "REDRAW";

    public static final String EXECUTE_COMMAND = prefix + "EXECUTE_COMMAND";

    public static final String REDRAW_WIDGET = prefix + "REDRAW_WIDGET";
    public static final String WIDGET_REQUEST_UPDATE = prefix + "WIDGET_REQUEST_UPDATE";

    public static final String TOP_LEVEL_BACK = prefix + "TOP_LEVEL_BACK";

    public static final String NOTIFICATION_SET_FOR_DEVICE = prefix + "NOTIFICATION_SET_FOR_DEVICE";
    public static final String NOTIFICATION_GET_FOR_DEVICE = prefix + "NOTIFICATION_GET_FOR_DEVICE";

    public static final String CONNECTIONS_CHANGED = prefix + "CONNECTIONS_CHANGED";
    public static final String CONNECTION_UPDATE = prefix + "CONNECTION_UPDATE";

    public static final String CONNECTION_ERROR = prefix + "CONNECTION_ERROR";
    public static final String CONNECTION_ERROR_HIDE = prefix + "CONNECTION_ERROR_HIDE";


    public static final String EXT_DEVICE_STATE_NOTIFY = prefix + "EXT_DEVICE_STATE_NOTIFY";

    public static final String UPDATE_NEXT_ALARM_CLOCK = prefix + "NEXT_ALARM_CLOCK";
}
