package ir.kamranvahdati.lawoffice;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.test.runner.AndroidJUnitRunner;

/** Database fixture replacement must not race an OS alarm receiver in the test process. */
public final class OfficeTestRunner extends AndroidJUnitRunner {
    private ComponentName receiver;
    private int previousState;

    @Override public void onCreate(Bundle arguments) {
        receiver=new ComponentName(getTargetContext(),ReminderReceiver.class);
        PackageManager manager=getTargetContext().getPackageManager();
        previousState=manager.getComponentEnabledSetting(receiver);
        manager.setComponentEnabledSetting(receiver,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP);
        // Runs on the main thread before instrumentation tests start. Initialize
        // the fixture key here so any already queued receiver sees the same key.
        // This changes only the test APK, including on the immutable baseline.
        DatabaseKey.read(getTargetContext());
        // ReminderDeliveryTest still calls onReceive directly to verify real notifications,
        // permission checks and deduplication. Alarm timing/reboot remains a separate QA gate.
        super.onCreate(arguments);
    }

    @Override public void onDestroy() {
        if(receiver!=null)getTargetContext().getPackageManager().setComponentEnabledSetting(
            receiver,previousState,PackageManager.DONT_KILL_APP);
        super.onDestroy();
    }
}
