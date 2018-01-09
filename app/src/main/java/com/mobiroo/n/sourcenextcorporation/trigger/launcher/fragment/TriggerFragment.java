package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TaskWizardActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui.Holders;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.FragmentLifecycle;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.SelectTriggerActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TriggerWizardActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListIconTextItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint.Constraint;

import java.util.ArrayList;

public class TriggerFragment extends TaskBuilderBaseFragment implements OnClickListener, FragmentLifecycle {

    private ArrayList<Trigger> mTriggers;
    private ListView mList;
    private ArrayAdapter<Trigger> mAdapter;

    private Trigger mActionTrigger;
    private int mActionPosition;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_triggers, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            loadDataFromInstanceState(savedInstanceState);
        } else {
            loadDataFromArguments(getArguments());
        }

        if (mTriggers == null) {
            mTriggers = new ArrayList<Trigger>();
            onFabClicked();
        } else {
            for (Trigger t : mTriggers) {
                Logger.d("Loading constraint for " + t.getId());
                t.loadConstraints(getActivity());
            }
        }

        initUiElements(view);


        if ((mTriggers == null) || (mTriggers.size() < 1)) {
            showHelp();
        } else {
            hideHelp();
        }

        mAdapter = new ArrayAdapter<Trigger>(getActivity().getBaseContext(), R.layout.list_item_trigger, mTriggers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Trigger item = mTriggers.get(position);
                if (item == null) {
                    Logger.d("Item is null");
                }
                if (item != null) {
                    convertView = item.getView(item, getActivity(), convertView);
                    ((Holders.Trigger) convertView.getTag()).spacer.setVisibility((position == 0) ? View.GONE : View.VISIBLE);
                    ((Holders.Trigger) convertView.getTag()).content.setOnClickListener(TriggerFragment.this);
                    ((Holders.Trigger) convertView.getTag()).no_constraints_container.setOnClickListener(TriggerFragment.this);
                    return convertView;
                } else {
                    return null;
                }
            }
        };

        mList.setAdapter(mAdapter);
        mList.setVisibility(View.VISIBLE);

        getView().findViewById(R.id.header).setOnClickListener(this);
    }

    private void initUiElements(View view) {
        mEmpty = view.findViewById(R.id.header);
        mList = (ListView) view.findViewById(android.R.id.list);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.fab_grey));
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               onFabClicked();
            }
        });
    }

    private void onFabClicked() {
        Logger.d("Starting activity to select trigger...");
        Intent intent = new Intent(getActivity(), SelectTriggerActivity.class);
        getActivity().startActivityForResult(intent, TaskWizardActivity.REQUEST_SELECT_TRIGGER);
    }

    private void loadDataFromArguments(Bundle args) {
        if (args.containsKey(TaskSet.EXTRA_TRIGGERS)) {
            mTriggers = args.getParcelableArrayList(TaskSet.EXTRA_TRIGGERS);
        }
    }

    private void loadDataFromInstanceState(Bundle state) {
        mTriggers = state.getParcelableArrayList("triggers");
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putParcelableArrayList("triggers", mTriggers);

        state.putInt("tag", mListenerTag);
    }

    private void addTrigger(Trigger trigger) {
        if (mTriggers == null) {
            mTriggers = new ArrayList<Trigger>();
        }

        Logger.d("Adding a trigger with " + trigger.getConstraints().size());
        for (Constraint c : trigger.getConstraints()) {
            Logger.d("Class: " + c.getClass());
        }
        boolean add = true;
        if (trigger.getType() == TaskTypeItem.TASK_TYPE_NFC) {
            for (Trigger t : mTriggers) {
                if (t.getType() == TaskTypeItem.TASK_TYPE_NFC) {
                    add = false;
                }
            }

        }

        if (add) {
            mTriggers.add(trigger);
            updateHelpDisplay();
            mAdapter.notifyDataSetChanged();

            // Somehow people are getting a null listener here
            if (mDataChangedListener != null) {
                mDataChangedListener.triggersChanged(mTriggers);
            } else {
                Logger.d("TRIGGER: Data changed listener is null!");
            }
        }
    }

    // Listener implementation
    @Override
    public ArrayList<Trigger> getTriggers() {
        return mTriggers;
    }

    @Override
    public void notifyResult(int requestCode, int resultCode, Intent data) {
        Logger.d("TRIGGER: Got result " + resultCode + " from request " + requestCode);
        onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void addClicked(int position) {
        Logger.d("TR: Comparing tag " + mListenerTag + " to " + position);
        if (position == mListenerTag) {
            // + clicked
            /*Usage.logEvent(
                    Usage.getAnalyticsObject(getActivity()),
                    "Add trigger plus",
                    false
            )*/;
            Intent intent = new Intent(getActivity(), SelectTriggerActivity.class);
            getActivity().startActivityForResult(intent, TaskWizardActivity.REQUEST_SELECT_TRIGGER);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("Got notified of " + requestCode);
        switch (requestCode) {
            case TaskWizardActivity.REQUEST_SELECT_TRIGGER:
                if (data != null) {
                    Trigger trigger = (Trigger) data.getParcelableExtra(Trigger.EXTRA_TRIGGER);
                    if (trigger != null) {
                        addTrigger(trigger);
                    } else {
                        Logger.d("Trigger is null");
                    }
                }

                break;
            case TaskWizardActivity.REQUEST_EDIT_TRIGGER:
                if (data != null) {
                    Trigger trigger = (Trigger) data.getParcelableExtra(Trigger.EXTRA_TRIGGER);
                    if (trigger != null) {
                        Logger.d("Replacing trigger at " + mActionPosition);
                        mTriggers.remove(mActionPosition);
                        mTriggers.add(mActionPosition, trigger);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Logger.d("Trigger is null");
                    }
                }
                break;
        }
    }

    private void deleteTrigger(Trigger trigger) {
        mTriggers.remove(trigger);
        mAdapter.notifyDataSetChanged();
        if (mDataChangedListener == null) {
            Logger.d("Data changed listener is null");
        }

        mDataChangedListener.triggersChanged(mTriggers);
    }

    private void editTrigger(Trigger trigger) {
        editTrigger(trigger, 0);
    }

    private void editTrigger(Trigger trigger, int page) {
        Intent intent = new Intent(getActivity(), TriggerWizardActivity.class);
        if (trigger.getConstraints() != null) {
            Logger.d("Passing in a trigger with " + trigger.getConstraints().size() + " constraints");
        }
        intent.putExtra(Trigger.EXTRA_TRIGGER, trigger);
        intent.putExtra(Trigger.EXTRA_PAGE, page);
        getActivity().startActivityForResult(intent, TaskWizardActivity.REQUEST_EDIT_TRIGGER);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.header:
                /*Usage.logEvent(
                        Usage.getAnalyticsObject(getActivity()),
                        "Add trigger header",
                        false
                );*/
                Intent intent = new Intent(getActivity(), SelectTriggerActivity.class);
                getActivity().startActivityForResult(intent, TaskWizardActivity.REQUEST_SELECT_TRIGGER);
                break;
            case R.id.no_constraints:
                final int p = mList.getPositionForView((View) v);
                mActionTrigger = mAdapter.getItem(p);
                mActionPosition = p;
                editTrigger(mActionTrigger, 1);
                break;
            default:
                final int position = mList.getPositionForView((View) v);
                Trigger item = mAdapter.getItem(position);
                mActionTrigger = item;
                mActionPosition = position;

                ArrayList<ListIconTextItem> items = new ArrayList<ListIconTextItem>();
                items.add(new ListIconTextItem(getActivity(), R.string.menu_delete, R.drawable.ic_action_trash));
                items.add(new ListIconTextItem(getActivity(), R.string.menu_context_edit, R.drawable.ic_action_edit));

                showPopupMenu(mActionTrigger, v);

        }


    }

    private void showPopupMenu(Trigger trigger, View v) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.inflate(R.menu.menu_popup_trigger);
        popupMenu.setOnMenuItemClickListener(mPopupClickedListener);
        if (!getActivity().isFinishing()) {
            popupMenu.show();
        }
    }

    private OnMenuItemClickListener mPopupClickedListener = new OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.pop_up_delete:
                    deleteTrigger(mActionTrigger);
                    break;
                case R.id.pop_up_edit:
                    editTrigger(mActionTrigger);
                    break;
            }
            return false;
        }

    };



    @Override
    public void onResumeFragment() {

    }
}
