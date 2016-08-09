package com.nuvoton.socketmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.nuvoton.nuplayer.R;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by timsheu on 8/4/16.
 */
public class CustomDialogFragment extends DialogFragment {
    private DialogFragmentInterface dialogFragmentInterface;
    public interface DialogFragmentInterface {
        void sendOkay(String category);
        void chooseHistory(CustomDialogFragment fragment, int index);
    }
    private static final String TAG = "CustomDialogFragment";
    private String label = "Dialog Label";
    private String content = "Dialog Content";
    private String type = "Dialog";
    private NiceSpinner spinner;
    private List<String> localHistory;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (type.compareTo("Dialog ") == 0){
            builder.setTitle(label).setMessage(content).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogFragmentInterface.sendOkay(label);
                }
            }).setNegativeButton("Cancel", null);
        }else if (type.compareTo("Spinner") == 0){
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_confirm, null);
            spinner = (NiceSpinner) view.findViewById(R.id.historySpinner);
            spinner.attachDataSource(localHistory);
            spinner.setTextColor(Color.BLACK);
            Log.d(TAG, "onCreateDialog: " + localHistory);
            final CustomDialogFragment fragment = this;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "onItemSelected: " + String.valueOf(position));
                    dialogFragmentInterface.chooseHistory(fragment, position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG, "onNothingSelected: ");
                }
            });
            builder.setView(view).setTitle(label).setNegativeButton("Cancel", null);
        }

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach: ");
    }

    public void setLabel(String label){
        this.label = label;
    }

    public void setContent(String content){
        this.content = content;
    }

    public void setInterface(DialogFragmentInterface dialogFragmentInterface){
        this.dialogFragmentInterface = dialogFragmentInterface;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setHistoryData(List<String> history){
        localHistory = new LinkedList<>(history);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        spinner.dismissDropDown();
    }
}
