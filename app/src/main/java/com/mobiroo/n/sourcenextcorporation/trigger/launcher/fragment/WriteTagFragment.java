package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildTools;
import com.mobiroo.n.sourcenextcorporation.trigger.R;

public class WriteTagFragment extends Fragment {

    public boolean LockTag;
    
    public static final String FRAGMENT_TAG = "WriteTagFragment";
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_write_tag,  null);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (!BuildTools.shouldShowShop()) {
            try {
                ((View) view.findViewById(R.id.shop_button)).setVisibility(View.GONE);
            } catch (Exception e) {
                
            }
        }
    }
    
}
