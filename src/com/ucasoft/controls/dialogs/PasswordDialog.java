package com.ucasoft.controls.dialogs;

import android.annotation.TargetApi;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.ucasoft.controls.R;

/**
 * Created by UCASoft.
 * User: Antonov Sergey
 * Date: 08.03.14
 * Time: 23:32
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PasswordDialog extends DialogFragment {

    public interface PasswordDialogListener {
        public void onDialogEnterCorrectPassword();
        public void onDialogNegativeClick();
    }

    private final String DIALOG_TAG = "ucasoft.passwordDialog";
    private String title;
    private int titleId = -1;
    private EditText editText;
    private String password = "";
    private boolean useVibration = false;
    private PasswordDialogListener listener;

    public void setTitle(int resourceId) {
        titleId = resourceId;
    }

    public void setUseVibration(boolean useVibration) {
        this.useVibration = useVibration;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, DIALOG_TAG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.password_dialog, null);
        editText = (EditText) view.findViewById(R.id.password_edit_text);
        builder.setView(view)
                .setTitle(getTitle())
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (listener != null) {
                            listener.onDialogNegativeClick();
                        }
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        return keyEvent.getKeyCode() == keyEvent.KEYCODE_BACK;
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (password.equals(getPassword())) {
                            if (listener != null) {
                                listener.onDialogEnterCorrectPassword();
                            }
                            dialog.dismiss();
                        } else {
                            if (useVibration) {
                                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                vibrator.vibrate(300);
                            }
                        }
                    }
                });
            }
        });
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (PasswordDialogListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
            listener = null;
        }
    }

    private String getTitle() {
        if (title == null || title.equals("")) {
            if (titleId > -1) {
                return getResources().getString(titleId);
            }
        }
        return title;
    }

    private String getPassword() {
        return editText.getText().toString();
    }

}
