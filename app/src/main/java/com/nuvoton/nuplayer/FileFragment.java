package com.nuvoton.nuplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.longevitysoft.android.xml.plist.domain.PListObject;
import com.longevitysoft.android.xml.plist.domain.sString;
import com.nuvoton.socketmanager.ReadConfigure;
import com.nuvoton.socketmanager.SocketInterface;
import com.nuvoton.socketmanager.SocketManager;

import java.util.ArrayList;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FileFragment extends Fragment implements SocketInterface{
    private String localIP;
    ReadConfigure configure;
    SocketManager socketManager;
    private static final String TAG = "FileFragment";

    private String platform, cameraSerial;

    private int mColumnCount = 1;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public static FileFragment newInstance(Bundle b){
        FileFragment fragment = new FileFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_list, container, false);
        configure = ReadConfigure.getInstance(getActivity().getApplicationContext(), false);
        platform = getArguments().getString("Platform");
        cameraSerial = getArguments().getString("CameraSerial");
        socketManager = new SocketManager();
        socketManager.setSocketInterface(this);
        initList(view);
        sendListFilename();
        // Set the adapter
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnListFragmentInteractionListener) {
//            mListener = (OnListFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnListFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void initList(View view){
        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_file_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        Log.d(TAG, "initList: " + platform + ", " + cameraSerial);
//        ArrayList<FileContent> itemsData = new ArrayList<>();
//        for (int i=0; i<50; i++){
//            FileContent content = new FileContent(String.valueOf(i), "Test Name" + String.valueOf(i), "Test dDate" + String.valueOf(i));
//            itemsData.add(content);
//        }


    }

    private String getDeviceURL(){
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        String urlString = preference.getString("URL", "DEFAULT");
        Log.d(TAG, "sendFileListCommand: " + urlString);
        String [] ipCut = urlString.split("/");
        String ip = ipCut[2];
        Log.d(TAG, "sendFileListCommand: " + ip);

        String url = "http://" + ip + ":80/";
        localIP = new String(ip);
        return url;
    }

    private void sendListFilename(){
        String command = getDeviceURL();
        sString name, baseCommand, action, group;
        ArrayList<Map> fileCommandSet = configure.fileCommandSet;
        Map<String, PListObject> targetCommand = fileCommandSet.get(0);
        name = (sString) targetCommand.get("Name");
        baseCommand = (sString) targetCommand.get("Base Command");
        action = (sString) targetCommand.get("action");
        group = (sString) targetCommand.get("group");
        command = command + baseCommand.getValue() + "?action=" + action.getValue() + "&group=" + group.getValue();
        if (socketManager != null){
            socketManager.executeSendGetTask(command, SocketManager.CMD_FILELIST);
        }
    }

    //delegate
    @Override
    public void showToastMessage(String message) {
        Toast.makeText(getActivity().getBaseContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateFileList(ArrayList<FileContent> fileList) {
        MyFileRecyclerViewAdapter adapter = new MyFileRecyclerViewAdapter(fileList);
        recyclerView.setAdapter(adapter);
        adapter.setOnRecyclerViewItemClickListener(new MyFileRecyclerViewAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, FileContent data) {
                Log.d(TAG, "onItemClick: " + data.toString());
                Intent intent = new Intent(getActivity(), FilePlayActivity.class);
                String fullUrl = "rtsp://" + localIP + "/file/" + data.toString();
                intent.putExtra("FileURL", fullUrl);
                startActivity(intent);
            }
        });
//        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
    }

    @Override
    public void deviceIsAlive() {

    }

    @Override
    public void updateSettingContent(String category, String value) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(FileContent item);
    }
}
