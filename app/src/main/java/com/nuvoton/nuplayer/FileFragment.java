package com.nuvoton.nuplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.longevitysoft.android.xml.plist.domain.PListObject;
import com.longevitysoft.android.xml.plist.domain.sString;
import com.nuvoton.socketmanager.ReadConfigure;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FileFragment extends Fragment {
    ReadConfigure configure;
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
    public static FileFragment newInstance(int index){
        FileFragment fragment = new FileFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        fragment.setArguments(bundle);
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
        configure = ReadConfigure.getInstance(getActivity());
        platform = getArguments().getString("Platform");
        cameraSerial = getArguments().getString("CameraSerial");
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
        ArrayList<FileContent> itemsData = new ArrayList<>();
        for (int i=0; i<50; i++){
            FileContent content = new FileContent(String.valueOf(i), "Test Name" + String.valueOf(i), "Test Date" + String.valueOf(i));
            itemsData.add(content);
        }

        MyFileRecyclerViewAdapter adapter = new MyFileRecyclerViewAdapter(itemsData);
        recyclerView.setAdapter(adapter);
    }

    private URL getDeviceURL(){
        String cameraName = "Setup Camera " + cameraSerial;
        SharedPreferences preference = getActivity().getSharedPreferences(cameraName, Context.MODE_PRIVATE);
        String urlString = preference.getString("URL", "DEFAULT");
        Log.d(TAG, "sendFileListCommand: " + urlString);
        String [] ipCut = urlString.split("/");
        String ip = ipCut[2];
        Log.d(TAG, "sendFileListCommand: " + ip);
        try {
            URL url = new URL("http://" + ip + ":80/");
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendListFilename(){
        URL url = getDeviceURL();
        sString name, baseCommand;
        ArrayList<Map> fileCommandSet = configure.fileCommandSet;
        Map<String, PListObject> targetCommand = fileCommandSet.get(0);
        name = (sString) targetCommand.get("Name");
        baseCommand = (sString) targetCommand.get("Base Command");
        Log.d(TAG, "sendListFilename: " + name.getValue() + ", " + baseCommand.getValue());
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
