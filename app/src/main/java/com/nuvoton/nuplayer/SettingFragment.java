package com.nuvoton.nuplayer;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.EditTextPreference;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

import com.longevitysoft.android.xml.plist.domain.PListObject;
import com.longevitysoft.android.xml.plist.domain.sString;
import com.nuvoton.socketmanager.CustomDialogFragment;
import com.nuvoton.socketmanager.ReadConfigure;
import com.nuvoton.socketmanager.SocketInterface;
import com.nuvoton.socketmanager.SocketManager;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, SocketInterface, CustomDialogFragment.DialogFragmentInterface{
    private ReadConfigure configure;
    private SocketManager socketManager;
    private String key; 
    private static String platform, cameraSerial, preferenceName;
    private String TAG = "SettingFragment";
    private ArrayList<Preference> settingArrayList;
    private LinkedList<String> historyList = null;
    SettingFragmentInterface settingFragmentInterface;

    @Override
    public void chooseHistory(CustomDialogFragment fragment, int index) {
        getFragmentManager().beginTransaction().remove(fragment).commit();
        String temp = new String(historyList.get(index));
        historyList.addFirst(temp);
        historyList.removeLast();
        updateHistoryList();
    }

    @Override
    public void sendOkay(String category) {
        if (category.compareTo("Reboot")  == 0){
            ArrayList<Map> videoCommandSet = configure.systemCommandSet;
            Map<String, PListObject>targetCommand = videoCommandSet.get(0);
            sString baseCommand;
            String command = getDeviceURL();
            baseCommand = (sString) targetCommand.get("Base Command");
            command = command + baseCommand.getValue();
            String commandType = "";
            commandType = SocketManager.CMDSET_REBOOT;
            socketManager.executeSendGetTask(command, commandType);
        }else if(category.compareTo("Send Report") == 0){
            sendReport();
        }
        Log.d(TAG, "sendOkay: setting fragment");
    }

    public interface SettingFragmentInterface {
        public void restartStream();
        public void manualSendReport();
        public void setResolution(String resolution);
    }
    public static SettingFragment newInstance(Bundle bundle){
        platform = bundle.getString("Platform");
        cameraSerial = bundle.getString("CameraSerial");
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    public SettingFragment(){
        Log.d(TAG, "SettingFragment: " + platform);
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        preferenceName = "Setup Camera " + String.valueOf(cameraSerial);
        getPreferenceManager().setSharedPreferencesName(preferenceName);
        Log.d(TAG, "onCreate: " + preferenceName + " pref name: " + getPreferenceManager().getSharedPreferencesName());
        // Inflate the layout for this fragment

        if (platform.equals("SkyEye")) {
            addPreferencesFromResource(R.xml.settings);
        } else if (platform.equals("DVR")) {
            addPreferencesFromResource(R.xml.settings_dvr);
        }

        configure = ReadConfigure.getInstance(getActivity().getApplicationContext(), false);
        getHistoryList();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged: " + key);
        determineSettings(key, sharedPreferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        getActivity().getApplicationContext().getSharedPreferences(preferenceName, Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
        updateSetting();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        getActivity().getApplicationContext().getSharedPreferences(preferenceName, Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        Log.d(TAG, "onPreferenceTreeClick: " + key);
        CustomDialogFragment dialog = new CustomDialogFragment();
        dialog.setInterface(this);
        if (key.compareTo("Reboot") == 0){
            dialog.setLabel("Reboot");
            dialog.setContent("Click OK to reboot device!");
            dialog.show(getFragmentManager(), "Reboot");
        }else if (key.compareTo("Send Report")  == 0){
            dialog.setLabel("Send Report");
            dialog.setContent("Click OK to send E-mail report!");
            dialog.show(getFragmentManager(), "Send Report");
        }else if (key.compareTo("History") == 0){
            dialog.setType("Spinner");
            dialog.setLabel("History");
            dialog.setHistoryData(historyList);
            dialog.show(getFragmentManager(), "History");
        }else if (key.compareTo("URL") == 0){
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            Log.d(TAG, "onPreferenceTreeClick: " + editTextPreference.getEditText().getText());
            editTextPreference.getEditText().setText(historyList.get(0));
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void determineSettings(String key, SharedPreferences sharedPreference){
        socketManager = new SocketManager();
        socketManager.setSocketInterface(this);
        boolean callSend = true, plugin = false;
        String command = getDeviceURL();
        sString baseCommand, subCommand;
        String pipe="&pipe=0", type="&type=h264", value, commandType = "";
        ArrayList<String> commandList = new ArrayList<>();
        String pluginCommand = "param.cgi?action=update&group=plugin";
        String finalCommand = "";
        String index = "0";
        switch (key){
            case "Resolution":
                ArrayList<Map> videoCommandSet = configure.videoCommandSet;
                Map<String, PListObject> targetCommand = videoCommandSet.get(1);
                baseCommand = (sString) targetCommand.get("Base Command");
                subCommand = (sString) targetCommand.get("Sub Command");
                value = sharedPreference.getString(key, "0");
                command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type + "&value=" + value;
                commandType = SocketManager.CMDSET_RESOLUTION;
                settingFragmentInterface.setResolution(value);
                break;
            case "Adaptive":
                index = sharedPreference.getString("Adaptive", "0");
                commandList = new ArrayList<>();

                String adaptiveTemp = "Pipe0_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=0";
                commandList.add(finalCommand);

                adaptiveTemp = "Pipe0_Min_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=20";
                commandList.add(finalCommand);

                adaptiveTemp = "Pipe0_Max_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=52";
                commandList.add(finalCommand);

                if (index.equals("0")){
                    adaptiveTemp = "Pipe0_Min_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=512";
                    commandList.add(finalCommand);

                    adaptiveTemp = "Pipe0_Max_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=2000";
                    commandList.add(finalCommand);
                }else if (index.equals("1")) {
                    adaptiveTemp = "Pipe0_Min_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=512";
                    commandList.add(finalCommand);

                    adaptiveTemp = "Pipe0_Max_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=5000";
                    commandList.add(finalCommand);
                }else if (index.equals("2")){
                    adaptiveTemp = "Pipe0_Min_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=2000";
                    commandList.add(finalCommand);

                    adaptiveTemp = "Pipe0_Max_Bitrate";
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + adaptiveTemp + "&value=5000";
                    commandList.add(finalCommand);
                }else if (index.equals("3")){
                    return;
                }
                plugin = true;
                socketManager.setCommandList(commandList);
                commandType = SocketManager.CMDSET_ADAPTIVE;
                sharedPreference.edit().putString("Fixed Quality", "3");
                ListPreference listPreference = (ListPreference) getPreferenceManager().findPreference("Fixed Quality");
                listPreference.setValue("3");

                sharedPreference.edit().putString("Fixed Bit Rate", "3");
                listPreference = (ListPreference) getPreferenceManager().findPreference("Fixed Bit Rate");
                listPreference.setValue("3");
                break;
            case "Fixed Bit Rate":
                index = sharedPreference.getString("Fixed Bit Rate", "0");
                commandList = new ArrayList<>();

                String fixedBitRateTemp = "Pipe0_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=0";
                commandList.add(finalCommand);

                fixedBitRateTemp = "Pipe0_Min_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=1";
                commandList.add(finalCommand);

                fixedBitRateTemp = "Pipe0_Max_Quality";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=52";
                commandList.add(finalCommand);

                fixedBitRateTemp = "Pipe0_Max_Bitrate";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=0";
                commandList.add(finalCommand);

                fixedBitRateTemp = "Pipe0_Bitrate";

                if (index.equals("0")){
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=2000";
                }else if (index.equals("1")){
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=3000";
                }else if (index.equals("2")){
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedBitRateTemp + "&value=5000";
                }else if (index.equals("3")){
                    return;
                }
                plugin = true;
                commandList.add(finalCommand);
                socketManager.setCommandList(commandList);
                commandType = SocketManager.CMDSET_FIXED_BITRATE;
                sharedPreference.edit().putString("Adaptive", "3");
                listPreference = (ListPreference) getPreferenceManager().findPreference("Adaptive");
                listPreference.setValue("3");

                sharedPreference.edit().putString("Fixed Quality", "3");
                listPreference = (ListPreference) getPreferenceManager().findPreference("Fixed Quality");
                listPreference.setValue("3");
                break;
            case "Fixed Quality":
                index = sharedPreference.getString("Fixed Bit Rate", "0");
                commandList = new ArrayList<>();

                String fixedQualityTemp = "Pipe0_Min_Bitrate";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedQualityTemp + "&value=512";
                commandList.add(finalCommand);

                fixedQualityTemp = "Pipe0_Max_Bitrate";
                finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedQualityTemp + "&value=4096";
                commandList.add(finalCommand);

                fixedQualityTemp = "Pipe0_Quality";

                if (index.equals("0")){
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedQualityTemp + "&value=50";
                }else if (index.equals("1")){
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedQualityTemp + "&value=40";
                }else if (index.equals("2")){
                    finalCommand = command + pluginCommand + "&name=h264_encoder" + "&param=" + fixedQualityTemp + "&value=25";
                }else if (index.equals("3")){
                    return;
                }
                plugin = true;
                commandList.add(finalCommand);
                socketManager.setCommandList(commandList);
                commandType = SocketManager.CMDSET_FIXED_QUALITY;
                sharedPreference.edit().putString("Adaptive", "3");
                listPreference = (ListPreference) getPreferenceManager().findPreference("Adaptive");
                listPreference.setValue("3");

                sharedPreference.edit().putString("Fixed Bit Rate", "3");
                listPreference = (ListPreference) getPreferenceManager().findPreference("Fixed Bit Rate");
                listPreference.setValue("3");
                break;
            case "FPS":
                videoCommandSet = configure.videoCommandSet;
                targetCommand = videoCommandSet.get(7);
                baseCommand = (sString) targetCommand.get("Base Command");
                subCommand = (sString) targetCommand.get("Sub Command");
                value = sharedPreference.getString(key, "30");
                command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type + "&value=" + value;
                commandType = SocketManager.CMDSET_FPS;
                break;
            case "Device Mic":
                videoCommandSet = configure.audioCommandSet;
                targetCommand = videoCommandSet.get(0);
                baseCommand = (sString) targetCommand.get("Base Command");
                subCommand = (sString) targetCommand.get("Sub Command");
                boolean mute = sharedPreference.getBoolean("key", false);
                value = (mute == true) ? "1" : "0";
                command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type + "&value=" + value;
                commandType = SocketManager.CMDSET_MUTE;

                break;
            case "Transmission":
                callSend = false;
                break;
            case "Wi-Fi QR Code":
                Log.d(TAG, "determineSettings: Wi-Fi QR Code");
                String QRCode, SSID = sharedPreference.getString("SSID", "SkyEye"), Password = sharedPreference.getString("Password", "12345678");
                QRCode = "BOOTPROTO DHCP\nIPADDR 192.168.3.1\nGATEWAY 192.168.3.1\nSSID \"NT_ZY\"\nAUTH_MODE WPA2PSK\nENCRYPT_TYPE AES\nAUTH_KEY 12345678\nWPS_TRIG_KEY HOME\n\nAP_IPADDR 192.168.100.1\nAP_SSID \"" +
                SSID + "\"\nAP_AUTH_MODE WPA2PSK\nAP_ENCRYPT_TYPE AES\nAP_AUTH_KEY " + Password + "\nAP_CHANNEL AUTO\n\nBRIF";
                callSend = false;
                Intent intent = new Intent(getActivity(), QRCode.class);
                intent.putExtra("QR code", QRCode);
                startActivity(intent);
                break;
            case "Recorder Status":
                ArrayList<Map> recordCommandSet = configure.recordCommandSet;
                String recorderStatus = sharedPreference.getString(key, "0");
                if (recorderStatus.equals("0")){
                    targetCommand = recordCommandSet.get(3);
                }else {
                    targetCommand = recordCommandSet.get(2);
                }
                baseCommand = (sString) targetCommand.get("Base Command");
                subCommand = (sString) targetCommand.get("Sub Command");
                value = sharedPreference.getString(key, "30");
                command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type + "&value=" + value;
                commandType = SocketManager.CMDSET_RECORD;
                break;
            case "URL":
                String url = sharedPreference.getString("URL", "");
                historyList.removeLast();
                historyList.addFirst(url);
                Preference preference = getPreferenceManager().findPreference("History");
                preference.setSummary(url);
            case "Camera Port":
                settingFragmentInterface.restartStream();
                callSend= false;
                break;
            default:
                return;
        }
        Log.d(TAG, "determineSettings: " + command);
        if (socketManager != null && callSend && !plugin){
            socketManager.executeSendGetTask(command, commandType);
        }else if (socketManager != null && callSend && plugin){
            socketManager.executeSendGetTaskList(commandList, commandType);
        }
        sharedPreference.edit().commit();
    }

    private String getDeviceURL(){
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getApplicationContext().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        String urlString = preference.getString("URL", "DEFAULT");
        String [] ipCut = urlString.split("/");
        String ip = ipCut[2];
        String port = preference.getString("Port", "80");
        String url = "http://" + ip + ":" + port + "/";
        return url;
    }

    @Override
    public void showToastMessage(String message) {
        Log.d(TAG, "showToastMessage: ");
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateFileList(ArrayList<FileContent> fileList) {
        Log.d(TAG, "updateFileList: ");
    }

    @Override
    public void deviceIsAlive() {
        Log.d(TAG, "deviceIsAlive: ");
    }

    @Override
    public void updateSettingContent(String category, String value) {
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getApplicationContext().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        preference.edit().putString(category, value);
        preference.edit().commit();
        if (category.equals("Recorder Status")){
            Preference pref = getPreferenceManager().findPreference(category);
            if (value.equals("1"))
                pref.setSummary("Recorder is recording");
            else
                pref.setSummary("Recorder is stopped");
        }else if(category.equals("Available Storage")){
            Preference pref = getPreferenceManager().findPreference(category);
            if (value.equals("1"))
                pref.setSummary("Storage available on device.");
            else
                pref.setSummary("No storage available on device.");
        }else {
            ListPreference pref = (ListPreference) getPreferenceManager().findPreference(category);
            pref.setValue(value);
        }
    }

    private void updateSetting(){
        socketManager = new SocketManager();
        socketManager.setSocketInterface(this);
        boolean callSend = true, plugin = false;
        sString baseCommand, subCommand;
        String pipe="&pipe=0", type="&type=h264", commandType = "";
        ArrayList<String> commandList = new ArrayList<>();
        String command = getDeviceURL();
// get resolution
        ArrayList<Map> videoCommandSet = configure.videoCommandSet;
        Map<String, PListObject> targetCommand = videoCommandSet.get(2);
        baseCommand = (sString) targetCommand.get("Base Command");
        subCommand = (sString) targetCommand.get("Sub Command");
        command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type;
        commandList.add(command);
// get fps
        command = getDeviceURL();

        targetCommand = videoCommandSet.get(8);
        baseCommand = (sString) targetCommand.get("Base Command");
        subCommand = (sString) targetCommand.get("Sub Command");
        command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type;
        commandList.add(command);
// get available storage
        command = getDeviceURL();

        ArrayList<Map> infoCommandSet = configure.infoCommandSet;
        targetCommand = infoCommandSet.get(0);
        baseCommand = (sString) targetCommand.get("Base Command");
        subCommand = (sString) targetCommand.get("Sub Command");
        command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type;
        commandList.add(command);
// get recorder status
        command = getDeviceURL();

        ArrayList<Map> recordCommandSet = configure.recordCommandSet;
        targetCommand = recordCommandSet.get(1);
        baseCommand = (sString) targetCommand.get("Base Command");
        subCommand = (sString) targetCommand.get("Sub Command");
        command = command + baseCommand.getValue() + "?command=" + subCommand.getValue() + pipe + type;
        commandList.add(command);

        commandType = SocketManager.CMDGET_ALL;
        socketManager.setCommandList(commandList);
        socketManager.executeSendGetTaskList(commandList, commandType);
    }
    public void sendReport(){
        if (isAdded()){
            Toast.makeText(getActivity(), R.string.email_toast_text, Toast.LENGTH_SHORT).show();
        }
        ACRA.getErrorReporter().handleException(new RuntimeException("Error"));
    }

    private void getHistoryList(){
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getApplicationContext().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        historyList = new LinkedList<>();
        for (int i=0; i<5; i++){
            String temp = preference.getString("History " + i, "-");
            historyList.add(temp);
        }
        Preference preference1 = getPreferenceManager().findPreference("History");
        preference1.setSummary(historyList.get(0));
    }

    private void updateHistoryList(){
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getApplicationContext().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
//        Log.d(TAG, "updateHistoryList: " + preference.getString("URL", ""));
        for (int i=0; i<5; i++){
            editor.putString("History " + i, historyList.get(i));
        }
        editor.putString("URL", historyList.get(0));
        editor.commit();
        Preference preference1 = getPreferenceManager().findPreference("History");
        preference1.setSummary(historyList.get(0));
//        Log.d(TAG, "updateHistoryList: " + preference.getString("URL", ""));
    }
}
