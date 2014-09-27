package eu.se_bastiaan.popcorntimeremote.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.IpAddress;
import com.mobsandgeeks.saripaar.annotation.NumberRule;
import com.mobsandgeeks.saripaar.annotation.Required;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.R;
import eu.se_bastiaan.popcorntimeremote.activities.PairingScannerActivity;
import eu.se_bastiaan.popcorntimeremote.database.InstanceEntry;
import eu.se_bastiaan.popcorntimeremote.database.InstanceProvider;
import eu.se_bastiaan.popcorntimeremote.models.ScanModel;

public class InstanceEditorDialogFragment extends DialogFragment {

    private Boolean mIsNewInstance = false;
    private Validator mValidator;
    private String mId;

    @InjectView(R.id.nameInput)
    @Required(order = 0)
    EditText nameInput;
    @InjectView(R.id.ipInput)
    @Required(order = 1)
    @IpAddress(order = 2)
    EditText ipInput;
    @InjectView(R.id.portInput)
    @Required(order = 3)
    @NumberRule(order = 4, type = NumberRule.NumberType.INTEGER)
    EditText portInput;
    @InjectView(R.id.usernameInput)
    @Required(order = 5)
    EditText usernameInput;
    @InjectView(R.id.passwordInput)
    @Required(order = 6)
    EditText passwordInput;
    @InjectView(R.id.manualButton)
    Button manualButton;
    @InjectView(R.id.scanButton)
    Button scanButton;
    @InjectView(R.id.pairingLayout)
    LinearLayout pairingLayout;

    private Validator.ValidationListener mValidationListener = new Validator.ValidationListener() {
        @Override
        public void onValidationSucceeded() {
            ContentValues values = new ContentValues();
            values.put(InstanceEntry.COLUMN_NAME_NAME, nameInput.getText().toString());
            values.put(InstanceEntry.COLUMN_NAME_IP, ipInput.getText().toString());
            values.put(InstanceEntry.COLUMN_NAME_PORT, portInput.getText().toString());
            values.put(InstanceEntry.COLUMN_NAME_USERNAME, usernameInput.getText().toString());
            values.put(InstanceEntry.COLUMN_NAME_PASSWORD, passwordInput.getText().toString());

            if(mIsNewInstance) {
                getActivity().getContentResolver().insert(InstanceProvider.INSTANCES_URI, values);
            } else {
                getActivity().getContentResolver().update(Uri.withAppendedPath(InstanceProvider.INSTANCES_URI, "/" + mId), values, null, null);
            }

            dismiss();
        }

        @Override
        public void onValidationFailed(View failedView, Rule<?> failedRule) {
            String message = failedRule.getFailureMessage();

            if (failedView instanceof EditText) {
                failedView.requestFocus();
                ((EditText) failedView).setError(message);
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args == null || !args.containsKey("_id")) {
            mIsNewInstance = true;
        } else {
            mId = args.getString("_id");
        }

        mValidator = new Validator(this);
        mValidator.setValidationListener(mValidationListener);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_instanceeditor, null, false);
        ButterKnife.inject(this, view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setView(view)
            .setPositiveButton(R.string.save,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) { }
                    }
            )
            .setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }
            );

        if(mIsNewInstance) {
            builder.setTitle(R.string.add_instance);
        } else {
            builder.setTitle(R.string.edit_instance);
        }

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mValidator.validateAsync();
                    }
                });

                if(!mIsNewInstance) {
                    Cursor cursor = getActivity().getContentResolver().query(Uri.withAppendedPath(InstanceProvider.INSTANCES_URI, "/" + mId), null, null, null, null);
                    cursor.moveToFirst();
                    ipInput.setText(cursor.getString(1));
                    portInput.setText(cursor.getString(2));
                    nameInput.setText(cursor.getString(3));
                    usernameInput.setText(cursor.getString(4));
                    passwordInput.setText(cursor.getString(5));
                    cursor.close();

                    ipInput.setVisibility(View.VISIBLE);
                    portInput.setVisibility(View.VISIBLE);
                    usernameInput.setVisibility(View.VISIBLE);
                    passwordInput.setVisibility(View.VISIBLE);
                    pairingLayout.setVisibility(View.GONE);
                }
            }
        });

        manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipInput.setVisibility(View.VISIBLE);
                portInput.setVisibility(View.VISIBLE);
                usernameInput.setVisibility(View.VISIBLE);
                passwordInput.setVisibility(View.VISIBLE);
                pairingLayout.setVisibility(View.GONE);
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PairingScannerActivity.class);
                startActivityForResult(intent, PairingScannerActivity.SCAN);
            }
        });

        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PairingScannerActivity.SCAN && resultCode == PairingScannerActivity.SUCCESS) {
            ScanModel model = data.getParcelableExtra("result");
            ipInput.setText(model.ip);
            portInput.setText(model.port);
            usernameInput.setText(model.user);
            passwordInput.setText(model.user);

            manualButton.performClick();
        }
    }
}
