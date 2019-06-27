package com.example.ccg.generatingcontacts;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.UserHandle;
import android.support.annotation.Nullable;

/**
 * @author cai_gp
 */
public class SmsSendService extends Service {
    public SmsSendService() {

    }

    @Override
    public synchronized ComponentName startForegroundServiceAsUser(Intent service, UserHandle user) {
        return null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
