package com.smox.smoxuser.stripe.controller;

import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;
import com.smox.smoxuser.stripe.dialog.ProgressDialogFragment;

/**
 * Class used to show and hide the progress spinner.
 */
public class ProgressDialogController {

    @NonNull
    private final Resources mRes;
    @NonNull private final FragmentManager mFragmentManager;
    @Nullable
    private ProgressDialogFragment mProgressFragment;

    public ProgressDialogController(@NonNull FragmentManager fragmentManager,
                                    @NonNull Resources res) {
        mFragmentManager = fragmentManager;
        mRes = res;
    }

    public void show(@StringRes int resId) {
        if (mProgressFragment != null && mProgressFragment.isVisible()) {
            mProgressFragment.dismiss();
            mProgressFragment = null;
        }
        mProgressFragment = ProgressDialogFragment.newInstance(mRes.getString(resId));
        mProgressFragment.show(mFragmentManager, "progress");
    }

    public void dismiss() {
        if (mProgressFragment != null) {
            mProgressFragment.dismiss();
        }
    }
}
