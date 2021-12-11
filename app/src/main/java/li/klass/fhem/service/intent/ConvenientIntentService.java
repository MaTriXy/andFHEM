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

package li.klass.fhem.service.intent;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;
import android.util.Log;

import java.util.concurrent.ExecutorService;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.update.backend.DeviceListService;

/**
 * Abstract class extending {@link IntentService} to provide some more convenience methods.
 */
public abstract class ConvenientIntentService extends IntentService {
    private static boolean outOfMemoryOccurred = false;
    private ExecutorService executorService = null;

    public ConvenientIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        inject(((AndFHEMApplication) getApplication()).getDaggerComponent());
    }

    protected abstract void inject(ApplicationComponent applicationComponent);

    @Override
    protected void onHandleIntent(final Intent intent) {
        try {
            if (executorService != null && !outOfMemoryOccurred) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        handleTaskInternal(intent);
                    }
                });
            } else {
                handleTaskInternal(intent);
            }
        } catch (OutOfMemoryError e) {
            Log.e(ConvenientIntentService.class.getSimpleName(), "out of memory occurred", e);
            outOfMemoryOccurred = true;
            executorService = null;
            onHandleIntent(intent);
        }
    }

    private void handleTaskInternal(Intent intent) {
        ResultReceiver resultReceiver = intent.getParcelableExtra(BundleExtraKeys.RESULT_RECEIVER);
        boolean doRefresh = intent.getBooleanExtra(BundleExtraKeys.DO_REFRESH, false);
        long updatePeriod = intent.getLongExtra(BundleExtraKeys.UPDATE_PERIOD, DeviceListService.NEVER_UPDATE_PERIOD);
        if (doRefresh) {
            updatePeriod = DeviceListService.ALWAYS_UPDATE_PERIOD;
        }

        try {
            State state = handleIntent(intent, updatePeriod, resultReceiver);
            if (state == State.SUCCESS) {
                sendNoResult(resultReceiver, ResultCodes.SUCCESS);
            } else if (state == State.ERROR) {
                sendNoResult(resultReceiver, ResultCodes.ERROR);
            }
        } catch (Exception e) {
            Log.e(ConvenientIntentService.class.getName(), "An error occurred while processing an intent (" + intent + ")", e);
            sendNoResult(resultReceiver, ResultCodes.ERROR);
        }
    }

    protected void sendNoResult(ResultReceiver receiver, int resultCode) {
        if (receiver != null) {
            receiver.send(resultCode, null);
        }
    }

    protected abstract State handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver);

    protected enum State {
        SUCCESS, ERROR, DONE
    }
}
