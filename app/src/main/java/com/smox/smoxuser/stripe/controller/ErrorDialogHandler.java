package com.smox.smoxuser.stripe.controller;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import com.smox.smoxuser.R;
import com.smox.smoxuser.stripe.dialog.ErrorDialogFragment;

/**
 * A convenience class to handle displaying error dialogs.
 */
public class ErrorDialogHandler {

    @NonNull
    private final FragmentManager mFragmentManager;

    public ErrorDialogHandler(@NonNull FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public void show(@NonNull String errorMessage) {
        ErrorDialogFragment.newInstance(R.string.validationErrors, errorMessage)
                .show(mFragmentManager, "error");
    }
}
