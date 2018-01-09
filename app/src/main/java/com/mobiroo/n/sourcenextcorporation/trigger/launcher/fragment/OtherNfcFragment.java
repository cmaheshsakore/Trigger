package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.EraseTagActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.ImportTagActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.CopyTagActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListIconTextItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.TagstandWriterLauncher;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class OtherNfcFragment extends Fragment implements OnItemClickListener {
    
    private GridView    mGridView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_other_nfc,  null);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGridView = (GridView) view.findViewById(android.R.id.list);
        
        ArrayList<ListIconTextItem> items = new ArrayList<ListIconTextItem>();
        items.add(new ListIconTextItem(getActivity(), R.string.basic_tags, R.drawable.ic_action_tags));
        items.add(new ListIconTextItem(getActivity(), R.string.layoutMainBuilderCopyText, R.drawable.ic_action_copy));
        items.add(new ListIconTextItem(getActivity(), R.string.layoutHomeFormat, R.drawable.ic_action_trash));
        items.add(new ListIconTextItem(getActivity(), R.string.layoutMainBuilderImportText, R.drawable.ic_action_upload));
        
        // set up menu
       ListItemsAdapter adapter = new ListItemsAdapter(getActivity(), items.toArray(new ListIconTextItem[items.size()]));
       mGridView.setNumColumns(Utils.isPhoneLayout(getActivity(), Utils.MAX_PHONE_800) ? 1 : 2);
       mGridView.setAdapter(adapter);
       mGridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        ListIconTextItem item = (ListIconTextItem) adapter.getItemAtPosition(position);
        if (item.getText().equals(getActivity().getString(R.string.basic_tags))) {
            TagstandWriterLauncher.launch(getActivity());
        } else if (item.getText().equals(getActivity().getString(R.string.layoutMainBuilderCopyText))) {
            startActivity(new Intent(getActivity(), CopyTagActivity.class));
        } else if (item.getText().equals(getActivity().getString(R.string.layoutHomeFormat))) {
            startActivity(new Intent(getActivity(), EraseTagActivity.class));
        } else if (item.getText().equals(getActivity().getString(R.string.layoutMainBuilderImportText))) {
            startActivity(new Intent(getActivity(), ImportTagActivity.class));
        }
        
    }

}
