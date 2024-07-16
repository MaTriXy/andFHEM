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
package li.klass.fhem.connection.backend

import android.content.Context
import android.content.Intent
import li.klass.fhem.R
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys

enum class RequestResultError(val errorStringId: Int) {
    HOST_CONNECTION_ERROR(R.string.error_host_connection),
    AUTHENTICATION_ERROR(R.string.error_authentication),
    CONNECTION_TIMEOUT(R.string.error_timeout),
    INTERNAL_SERVER_ERROR(R.string.error_internal_server_error),
    BAD_REQUEST(R.string.error_bad_request),
    NOT_FOUND(R.string.error_not_found),
    INVALID_CONTENT(R.string.error_invalid_content),
    DEVICE_LIST_PARSE(R.string.error_device_list_parse),
    INTERNAL_ERROR(R.string.error_internal);

    fun handleError(context: Context) {
        context.sendBroadcast(Intent(Actions.CONNECTION_ERROR)
                .putExtra(BundleExtraKeys.STRING_ID, errorStringId)
            .apply { setPackage(context.packageName) })
    }
}