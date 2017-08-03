package io.branch.branchster.feature;

import android.app.Application;

import io.branch.referral.Branch;

/**
 * Created by Evan on 7/24/17.
 */

public class ApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Branch.enableLogging();
        Branch.getAutoInstance(this);
    }
}
