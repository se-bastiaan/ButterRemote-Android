/*
 * Copyright (C) 2014 Vlad Mihalachi
 *
 * This file is part of Turbo Editor.
 *
 * Turbo Editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Turbo Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.se_bastiaan.popcorntimeremote.iab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

import eu.se_bastiaan.popcorntimeremote.BuildConfig;
import eu.se_bastiaan.popcorntimeremote.Constants;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.iab.utils.IabHelper;
import eu.se_bastiaan.popcorntimeremote.iab.utils.IabResult;
import eu.se_bastiaan.popcorntimeremote.iab.utils.Inventory;
import eu.se_bastiaan.popcorntimeremote.iab.utils.Purchase;
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;

/**
 * Fragment that represents an ability to donate to me. Be sure to redirect
 * {@link android.app.Activity#onActivityResult(int, int, android.content.Intent)}
 * to this fragment!
 *
 * @author Artem Chepurnoy
 */
public class DonationFragment extends DialogFragment {

    public static final String TAG_FRAGMENT_DONATION = "dialog_donate";
    public static final int RC_REQUEST = 10001;
    private static final String TAG = "DonationFragment";
    private final HashSet<String> mInventorySet = new HashSet<>();
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private TextView mError;
    private IabHelper mHelper;
    private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener =
            new IabHelper.OnIabPurchaseFinishedListener() {
                public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                    if (mHelper == null) return;
                    if (result.isFailure()) {
                        complain("Error purchasing: " + result);
                        setWaitScreen(false);
                        return;
                    }

                    if (!verifyDeveloperPayload(purchase)) {
                        complain("Error purchasing. Authenticity verification failed.");
                        setWaitScreen(false);
                        return;
                    }

                    // else, it is a success, the user has donated!
                    String sku = purchase.getSku();
                    mInventorySet.add(sku);
                }
            };
    private Donation[] mDonationList;
    private final IabHelper.QueryInventoryFinishedListener mGotInventoryListener =
            new IabHelper.QueryInventoryFinishedListener() {
                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                    if (mHelper == null) return;
                    if (result.isFailure()) {
                        complain("Failed to query inventory: " + result);
                        return;
                    }

                    mInventorySet.clear();
                    for (Donation donation : mDonationList) {
                        Purchase purchase = inventory.getPurchase(donation.sku);
                        boolean isBought = (purchase != null && verifyDeveloperPayload(purchase));

                        if (isBought) {
                            mInventorySet.add(donation.sku);
                        }
                    }

                    /*
                    // Fake items to debug user interface.
                    mInventorySet.add(mDonationList[0].sku);
                    mInventorySet.add(mDonationList[1].sku);
                    mInventorySet.add(mDonationList[2].sku);
                    */

                    updateUi();
                    setWaitScreen(false);
                }
            };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDonationList = DonationItems.get(getResources());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        assert activity != null;

        View view = getActivity().getLayoutInflater().inflate(R.layout.donation_dialog, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.donation_title)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null);

        TextView info = (TextView) view.findViewById(R.id.info);
        info.setText(Html.fromHtml(getString(R.string.donation_info)));
        info.setMovementMethod(new LinkMovementMethod());

        mError = (TextView) view.findViewById(R.id.error);
        mProgressBar = (ProgressBar) view.findViewById(android.R.id.progress);
        mGridView = (GridView) view.findViewById(R.id.grid);
        mGridView.setAdapter(new DonationAdapter(getActivity(), mDonationList, mInventorySet));
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DonationAdapter adapter = (DonationAdapter) parent.getAdapter();
                Donation donation = adapter.getItem(position);

                if (!mInventorySet.contains(donation.sku)) {
                    /**
                     * See {@link sharedcode.turboeditor.iab.DonationFragment#verifyDeveloperPayload(Purchase)}.
                     */
                    String payload = "";
                    try {
                        mHelper.launchPurchaseFlow(
                                getActivity(), donation.sku, RC_REQUEST,
                                mPurchaseFinishedListener, payload);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Failed to launch a purchase flow.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.donation_item_bought), Toast.LENGTH_LONG);
                }
            }
        });

        final AlertDialog alertDialog;

        alertDialog = builder.create();

        initBilling();

        return alertDialog;
    }

    /**
     * Shows a warning alert dialog to note, that those methods
     * may suck hard and nobody will care about it.<br/>
     * Starts an intent if user is agree with it.
     */
    private void startPaymentIntentWithWarningAlertDialog(final Intent intent) {
        CharSequence messageText = getString(R.string.donation_no_responsibility);
        new AlertDialog.Builder(getActivity())
                .setMessage(messageText)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            startActivity(intent);
                            dismiss(); // Dismiss main fragment
                        } catch (ActivityNotFoundException e) { /* hell no */ }
                    }
                })
                .create()
                .show();
    }

    private void setWaitScreen(boolean loading) {
        mProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        mGridView.setVisibility(!loading ? View.VISIBLE : View.GONE);
        mError.setVisibility(View.GONE);
    }

    private void setErrorScreen(String errorMessage, final Runnable runnable) {
        mProgressBar.setVisibility(View.GONE);
        mGridView.setVisibility(View.GONE);
        mError.setVisibility(View.VISIBLE);
        mError.setText(errorMessage);
        mError.setOnClickListener(runnable != null ? new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runnable.run();
            }
        } : null);
    }

    /**
     * Updates GUI to display changes.
     */
    private void updateUi() {
        DonationAdapter adapter = (DonationAdapter) mGridView.getAdapter();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposeBilling();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper.handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Releases com.android.vending.billing service.
     *
     * @see #initBilling()
     */
    private void disposeBilling() {
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    /**
     * <b>Make sure you call {@link #disposeBilling()}!</b>
     *
     * @see #disposeBilling()
     */
    private void initBilling() {
        setWaitScreen(true);
        disposeBilling();

        String base64EncodedPublicKey = BuildConfig.GOOGLE_PLAY_PUBLIC_KEY;
        mHelper = new IabHelper(getActivity(), base64EncodedPublicKey);
        mHelper.enableDebugLogging(Constants.LOG_ENABLED);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (mHelper == null) return;
                if (!result.isSuccess()) {
                    LogUtils.d(TAG, result.getMessage());
                    setErrorScreen(getString(R.string.donation_error_iab_setup), new Runnable() {
                        @Override
                        public void run() {
                            // Try to initialize billings again.
                            initBilling();
                        }
                    });
                    return;
                }

                setWaitScreen(false);
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    private boolean verifyDeveloperPayload(Purchase purchase) {
        // TODO: This method itself is a big question.
        // Personally, I think that this whole ‘best practices’ part
        // is confusing and is trying to make you do work that the API
        // should really be doing. Since the purchase is tied to a Google account,
        // and the Play Store obviously saves this information, they should
        // just give you this in the purchase details. Getting a proper user ID
        // requires additional permissions that you shouldn’t need to add just
        // to cover for the deficiencies of the IAB API.
        return true;
    }

    private void complain(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public static void show(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DonationFragment donationFragment = new DonationFragment();
        donationFragment.show(ft, TAG_FRAGMENT_DONATION);
    }

}
