package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.nfc.NdefMessage;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.ActionPickerActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TaskWizardActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListStringItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SavedAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.util.BetterAsyncLoader;
import com.mobiroo.n.sourcenextcorporation.trigger.util.BetterAsyncTask;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.ConfigureActionsActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.FragmentLifecycle;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.NFCUtil;

import java.util.ArrayList;
import java.util.Locale;

public class TagBuilderFragment extends TaskBuilderBaseFragment implements OnClickListener, FragmentLifecycle {

    public static final String EXTRA_TAG_ID = "com.trigger.launcher.EXTRA_TAG_ID";
    public static final String EXTRA_TAG_NAME = "com.trigger.launcher.EXTRA_TAG_NAME";

    private static final String KEY_SAVED_TASK = "CurrentTag";
    private static final String KEY_IS_VALID = "isValid";

    // Task ID passed in for loading
    public String mId;

    // Secondary id and name passed in for loading
    private String mSecondaryId;
    private String mSecondaryName;

    // Name from a pre-loaded task
    private String mName;

    // Actions passed in to populate
    private Parcelable[] mActions;

    // Object representing full task
    public Task mTask;

    // Flag indicating if something has changed and we need to prompt
    public boolean isDirty;

    // Action mode for deleting, moving or editing tasks
    private ActionMode mActionMode;
    private int mClickedPosition = -1;
    private View mView;

    // UI holders
    private TextInputLayout mTaskName;
    private RelativeLayout mIntro;

    public boolean mIsValid;

    public int mTaskNum;

    private Bundle mArguments;

    private SimpleDialogFragment mProfileDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_tag_builder_new, null);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mArguments = getArguments();

        setupUiElements(view);
        inspectBundle(savedInstanceState);
        showContent(view, savedInstanceState);

    }


    private void showContent(View view, Bundle savedInstanceState) {

        if (mTask == null) {
            if (savedInstanceState != null) {
                mTask = savedInstanceState.getParcelable(KEY_SAVED_TASK);
            }
        }

        if (mIsValid) {
            mIntro.setVisibility(View.GONE);
            enableActionMode(view);

            if ((mId != null) && (!mId.isEmpty())) {
                initLoaderForId(mId);
            } else if ((mSecondaryId != null) && (!mSecondaryId.isEmpty())) {
                initLoaderForAlt(mSecondaryId, mSecondaryName);
            } else if (mTask != null) {
                showTask(mTask);
            } else {
                loadNewTask(savedInstanceState);
            }

            if ((savedInstanceState == null) && (mActions != null)) {
                Intent intent = new Intent(getActivity().getBaseContext(), ConfigureActionsActivity.class);
                intent.putExtra(Constants.EXTRA_PRELOADED_ACTIONS, mActions);
                intent.putExtra(Constants.EXTRA_FLAG_PRELOADED_ACTIONS, true);
                getActivity().startActivityForResult(intent, TaskWizardActivity.REQUEST_ADD_ACTION);
                mActions = null;
            }

            if (mDataChangedListener != null) {
                mDataChangedListener.signalLoadFinished();
            }
        } else {
            mIntro.setVisibility(View.VISIBLE);
            FloatingActionButton fabSwitch = (FloatingActionButton) view.findViewById(R.id.fab_switch);
            fabSwitch.setBackgroundTintList(getResources().getColorStateList(R.color.fab_grey));
        }
    }

    @SuppressLint("WrongViewCast")
    // Wrong cast is being detected in a different layout.  Ignoring here.
    private void setupUiElements(View view) {
        mView = view;
        mIntro = (RelativeLayout) view.findViewById(R.id.intro);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.fab_grey));
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d("Starting activity to add action...");
                onActionFabClicked();
            }
        });

        FloatingActionButton fabSwitch = (FloatingActionButton) view.findViewById(R.id.fab_switch);
        fabSwitch.setBackgroundTintList(getResources().getColorStateList(R.color.fab_grey));
        fabSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d("Starting activity to select task...");
                promptSelectTask();
            }
        });


        mTaskName = (TextInputLayout) view.findViewById(R.id.tag_name);
        mTaskName.setHint(getString(R.string.tag_name));
        mTaskName.getEditText().addTextChangedListener(mTextWatcher);

        mEmpty = view.findViewById(R.id.header);
        mEmpty.setVisibility((mShowHelp) ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.header).setOnClickListener(this);

    }

    private void onActionFabClicked() {
        getActivity().startActivityForResult(
                new Intent(getActivity().getBaseContext(), ActionPickerActivity.class).putExtra(TaskWizardActivity.EXTRA_FRAGMENT_NUM, mTaskNum),
                TaskWizardActivity.REQUEST_ADD_ACTION);
    }

    private LoaderManager.LoaderCallbacks<BetterAsyncTask.TaskResult<Task>> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<BetterAsyncTask.TaskResult<Task>>() {
        @Override
        public Loader<BetterAsyncTask.TaskResult<Task>> onCreateLoader(int id, Bundle args) {
            final String tagId = args.getString(EXTRA_TAG_ID);
            final String name = args.getString(EXTRA_TAG_NAME);
            return new BetterAsyncLoader<Task>(getActivity().getBaseContext(), BetterAsyncLoader.INTERVAL_NONE) {
                @Override
                protected Task doLoadInBackground() throws Exception {

                    Cursor cursor = null;
                    try {
                        String tagName = name;
                        String tagDate = "";
                        if (name == null) {
                            cursor = getActivity().getContentResolver().query(TaskProvider.Contract.TASKS, new String[]{DatabaseHelper.FIELD_TASK_NAME, DatabaseHelper.FIELD_TASK_LAST_ACCESSED}, DatabaseHelper.FIELD_TASK_ID + "=?", new String[]{tagId}, null);
                            if (cursor == null) {
                                return null;
                            }

                            /* This stops tasks without actions from being loaded
                            if (!cursor.moveToFirst()) {
                                throw new Exception("Not found");
                            } */

                            if (cursor.moveToFirst()) {
                                tagName = cursor.getString(0);
                                tagDate = cursor.getString(1);
                            }

                            cursor.close();
                        } else {
                            tagName = name;
                            tagDate = "";
                        }

                        ArrayList<SavedAction> actions = Lists.newArrayList();

                        cursor = getActivity().getContentResolver().query(TaskProvider.Contract.ACTIONS, new String[]{DatabaseHelper.FIELD_ACTIVITY, DatabaseHelper.FIELD_DESCRIPTION}, DatabaseHelper.FIELD_TAG_ID + "=?", new String[]{tagId}, null);
                        if ((cursor != null) && (cursor.moveToFirst())) {
                            do {
                                String actionMessage = cursor.getString(0);
                                String actionPretty = cursor.getString(1);
                                actions.add(new SavedAction(actionMessage, actionPretty, "", ""));
                            } while (cursor.moveToNext());
                        }
                        Logger.i("Name is " + tagName);
                        return new Task(tagId, tagName, tagDate, actions);

                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<BetterAsyncTask.TaskResult<Task>> loader, BetterAsyncTask.TaskResult<Task> result) {
            if (!result.success()) {
                Toast.makeText(getActivity().getBaseContext(), result.getException().toString(), Toast.LENGTH_SHORT).show();
                return;
            }
            showTask(result.getObject());
        }

        @Override
        public void onLoaderReset(Loader<BetterAsyncTask.TaskResult<Task>> loader) {
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            isDirty = true;
            if (mTask != null) {
                mTask.setName(String.valueOf(s));
            }
            reloadUI();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            if (inflater != null) {
                inflater.inflate(R.menu.menu_tag_builder_context, menu);
            }
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        private void moveSelectedItemDown(int position) {
            if (position < mTask.getActions().size() - 1) {
                mClickedPosition = position + 1;
                SavedAction action = mTask.getActions().get(position);
                mTask.getActions().remove(action);
                mTask.getActions().add(mClickedPosition, action);
                isDirty = true;
                reloadUI();
            }
        }

        private void moveSelectedItemUp(int position) {
            if (position > 0) {
                mClickedPosition = Math.max(position - 1, 0);

                SavedAction action = mTask.getActions().get(position);
                mTask.getActions().remove(action);
                mTask.getActions().add(mClickedPosition, action);
                isDirty = true;
                reloadUI();
            }
        }

        private void editActions(int position) {
            mClickedPosition = position;

            Intent intent = new Intent(getActivity(), ConfigureActionsActivity.class);
            intent.putExtra(ConfigureActionsActivity.EXTRA_TASK, mTask);
            intent.putExtra(ConfigureActionsActivity.EXTRA_POSITION, position);
            getActivity().startActivityForResult(intent, TaskWizardActivity.REQUEST_EDIT_ACTIONS);

        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.context_delete:
                    if (mClickedPosition != -1) {
                        SavedAction action = mTask.getActions().get(mClickedPosition);
                        mTask.getActions().remove(action);
                        isDirty = true;
                        reloadUI();
                        mode.finish();
                    }
                    break;
                case R.id.context_up:
                    moveSelectedItemUp(mClickedPosition);
                    break;
                case R.id.context_down:
                    moveSelectedItemDown(mClickedPosition);
                    break;
                case R.id.context_edit:
                    editActions(mClickedPosition);
                    mode.finish();
                    break;
            }
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mClickedPosition = -1;
            reloadUI();
        }
    };


    public void addAction() {
        /*Usage.logEvent(
                Usage.getAnalyticsObject(getActivity()),
                "Add action plus",
                false
        );*/

        getActivity().startActivityForResult(
                new Intent(getActivity().getBaseContext(), ActionPickerActivity.class).putExtra(TaskWizardActivity.EXTRA_FRAGMENT_NUM, mTaskNum),
                TaskWizardActivity.REQUEST_ADD_ACTION);
    }

    private void inspectBundle(Bundle savedInstanceState) {

        if (mArguments != null) {
            if (mArguments.containsKey(TaskWizardActivity.EXTRA_TAG_ID)) {
                mId = mArguments.getString(TaskWizardActivity.EXTRA_TAG_ID);
                if (mId == null) {
                    mId = String.valueOf(mArguments.getInt(TaskWizardActivity.EXTRA_TAG_ID));
                }
            }

            if (mArguments.containsKey(Constants.EXTRA_IMPORTED_TAG)) {
                mTask = (Task) mArguments.getParcelable(Constants.EXTRA_IMPORTED_TAG);
            }

            if (mArguments.containsKey(Constants.EXTRA_PRELOAD_NAME)) {
                mName = mArguments.getString(Constants.EXTRA_PRELOAD_NAME);
            }

            if (mArguments.containsKey(Constants.EXTRA_PRELOADED_ACTIONS)) {
                mActions = mArguments.getParcelableArray(Constants.EXTRA_PRELOADED_ACTIONS);
            }

            if (mArguments.containsKey(TaskWizardActivity.EXTRA_IS_VALID)) {
                if (savedInstanceState == null) {
                    mIsValid = mArguments.getBoolean(TaskWizardActivity.EXTRA_IS_VALID);
                } else {
                    mIsValid = savedInstanceState.getBoolean(KEY_IS_VALID);
                }
            }

            if (mArguments.containsKey(TaskWizardActivity.EXTRA_FRAGMENT_NUM)) {
                mTaskNum = mArguments.getInt(TaskWizardActivity.EXTRA_FRAGMENT_NUM);
            }

            if (mArguments.containsKey(TaskWizardActivity.EXTRA_TAG_TWO_ID)) {
                mSecondaryId = mArguments.getString(TaskWizardActivity.EXTRA_TAG_TWO_ID);
            }

            if (mArguments.containsKey(TaskWizardActivity.EXTRA_TAG_TWO_NAME)) {
                mSecondaryName = mArguments.getString(TaskWizardActivity.EXTRA_TAG_TWO_NAME);
            }


        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_IS_VALID)) {
                mIsValid = savedInstanceState.getBoolean(KEY_IS_VALID);
            }

            if (savedInstanceState.containsKey(KEY_SAVED_TASK)) {
                mTask = (Task) savedInstanceState.getParcelable(KEY_SAVED_TASK);
            }
        }
    }

    private void enableActionMode(View view) {
        ListView actionsList = (ListView) view.findViewById(R.id.actions_list);
        actionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) {
                    mActionMode.finish();
                }
                mClickedPosition = position;
                mActionMode = getActivity().startActionMode(mActionModeCallback);
                reloadUI();
            }
        });
    }

    public void loadNewTask(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            String name = "Tag 1";
            Context context = null;
            try {
                context = getActivity().getBaseContext();
            } catch (Exception e) { /* Fail silently, context is null */ }

            if (context != null) {
                name = (mName != null) ? mName : Utils.getNextTagName(context);
            }

            showTask(new Task(null, name, null, new ArrayList<SavedAction>()));
        } else {
            // Pull tag from bundle
            mTask = (Task) savedInstanceState.getParcelable(KEY_SAVED_TASK);
            if (mTask != null) {
                showTask(mTask);
            }
        }
    }

    public void initLoaderForId(String tagId) {
        Bundle args = new Bundle();
        args.putString(EXTRA_TAG_ID, tagId);
        try {
            getLoaderManager().initLoader(0, args, mLoaderCallbacks);
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception starting loader", e);
        }
    }

    public void initLoaderForAlt(String id, String name) {
        Bundle args = new Bundle();
        args.putString(EXTRA_TAG_ID, id);
        args.putString(EXTRA_TAG_NAME, name);
        try {
            getLoaderManager().initLoader(0, args, mLoaderCallbacks);
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception starting loader", e);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        /* Workaround for bug in API 11+ in Support package
         * Need some data in place before calling super */
        savedInstanceState.putString("STATIC_DATA", "1234");

        savedInstanceState.putBoolean(KEY_IS_VALID, mIsValid);

        if (mTask != null) {
            savedInstanceState.putParcelable(KEY_SAVED_TASK, mTask);
        }

        savedInstanceState.putInt("tag", mListenerTag);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void removeActions() {
        if (mTask.getActions() != null) {
            mTask.getActions().clear();
        }
        reloadUI();
        isDirty = true;
        notifyTaskChanged();
    }

    public void addActions(ArrayList<SavedAction> actions) {
        try {
            mTask.getActions().addAll(actions);
        } catch (Exception e) {
            e.printStackTrace();
        }

        reloadUI();
        isDirty = true;
        notifyTaskChanged();
    }

    public void replaceActionAtPosition(SavedAction action, int position) {
        try {
            Logger.d("Removing action at " + position);
            mTask.getActions().remove(position);
            Logger.d("Re-adding action at position " + position);
            mTask.getActions().add(position, action);
        } catch (Exception e) {
            e.printStackTrace();
        }

        reloadUI();
        isDirty = true;
        notifyTaskChanged();

    }

    private void notifyTaskChanged() {
        if (mDataChangedListener != null) {
            mDataChangedListener.taskChanged(mTask, getListenerTag());
        }
    }

    private void showTask(Task tag) {
        mTask = tag;

        TextInputLayout tagNameView = (TextInputLayout) mView.findViewById(R.id.tag_name);
        tagNameView.getEditText().removeTextChangedListener(mTextWatcher);
        tagNameView.getEditText().setText(tag.getName());
        tagNameView.getEditText().addTextChangedListener(mTextWatcher);

        ListView actionsList = (ListView) mView.findViewById(R.id.actions_list);
        actionsList.setAdapter(new ArrayAdapter<SavedAction>(getActivity().getBaseContext(), R.layout.list_item_tag_contents, tag.getActions()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row;

                if (null == convertView) {
                    row = LayoutInflater.from(getActivity().getBaseContext()).inflate(R.layout.list_item_tag_contents, parent, false);
                } else {
                    row = convertView;
                }

                // FIXME: For some reason row.setSelected isn't working, but an inner view works fine...
                row.findViewById(R.id.inner).setSelected(position == mClickedPosition);

                SavedAction action = getItem(position);
                if (action != null) {
                    TextView tv = (TextView) row.findViewById(R.id.row1Text);
                    if (tv != null) {
                        tv.setText(action.getDescription());
                    }

                    if (SettingsHelper.getPrefBool(getActivity(), Constants.PREF_SHOW_TASK_SIZE, false)) {
                        TextView size = (TextView) row.findViewById(R.id.row1Size);
                        if (size != null) {
                            int tagSize = action.getMessage().length();
                            size.setText(getResources().getQuantityString(R.plurals.bytes_format, tagSize, String.valueOf(tagSize)));
                        }
                    }

                }
                return row;
            }
        });

        reloadUI();
        notifyTaskChanged();

    }

    @SuppressWarnings("rawtypes")
    private void reloadUI() {

        /* mView is null in some resuming cases */
        if (mView == null) {
            try {
                LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = inflater.inflate(R.layout.fragment_tag_builder_new, null);
            } catch (Exception e) {
            }

        }

        if (mView != null) {
            ListView actionsList = (ListView) mView.findViewById(R.id.actions_list);
            if (actionsList != null) {
                try {
                    ((ArrayAdapter) actionsList.getAdapter()).notifyDataSetChanged();
                } catch (Exception e) {
                    /* This is being invoked on what appears to be a null adapter 
                    allow notification to fail*/
                }
            }


            TextView infoTextView = (TextView) mView.findViewById(R.id.info_text);
            TextView emptyTextView = (TextView) mView.findViewById(R.id.empty);
            TextView sizeTextView = (TextView) mView.findViewById(R.id.info_size);
            int actionsCount = ((mTask == null) || (mTask.getActions() == null)) ? 0 : mTask.getActions().size();
            if (actionsCount == 0) {
                actionsList.setVisibility(View.GONE);
                infoTextView.setText(R.string.no_actions);
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                emptyTextView.setVisibility(View.GONE);
                actionsList.setVisibility(View.VISIBLE);

                if (SettingsHelper.getPrefBool(getActivity(), Constants.PREF_SHOW_TASK_SIZE, false)) {
                    int tagSize = calcTagSize();
                    String sizeString = getResources().getQuantityString(R.plurals.bytes_format, tagSize, String.valueOf(tagSize));
                    infoTextView.setText(getString(R.string.no_actions));
                    sizeTextView.setText(sizeString);
                }
            }
        }

        if ((mTask != null) && (mTask.getActions() != null) && (mTask.getActions().size() > 0)) {
            hideHelp();
        } else {
            showHelp();
        }
    }

    public Task getSavedTag() {
        return mTask;
    }

    public String getTagName() {
        if (mTaskName == null) {
            if (mView != null) {
                try {
                    mTaskName = (TextInputLayout) mView.findViewById(R.id.tag_name);
                    mTaskName.getEditText().addTextChangedListener(mTextWatcher);
                    mTask.setName(mTaskName.getEditText().getText().toString());
                } catch (Exception ignored) {
                }
            }

            return ((mTask == null) || (mTask.getName() == null)) ? "" : mTask.getName();
        } else {
            String tagName = "";
            if (mTask != null) {
                try {
                    tagName = mTask.getName();
                } catch (Exception e) {
                    tagName = "";
                }
            }

            if ((tagName == null) || (tagName.isEmpty())) {
                try {
                    tagName = mTaskName.getEditText().getText().toString();
                } catch (Exception e) {
                    tagName = "";
                }
            }
            return tagName;
        }
    }

    public void setSavedTag(Task tag) {
        mTask = tag;
    }

    public String saveTask(int taskType, String taskCondition, String value1, String value2) {
        mTask.setName(getTagName());
        return DatabaseHelper.saveTask(getActivity().getBaseContext(), mTask, taskType, taskCondition, value1, value2, false);
    }

    private int calcTagSize() {
        NdefMessage msg = new NdefMessage(NFCUtil.buildNdefRecord(getActivity().getBaseContext(), getActivity().getPackageName(), mTask.buildPayloadString(true, false), Locale.ENGLISH, true));
        return msg.toByteArray().length;
    }

    public void setTagId(String id) {
        mId = id;
    }

    public void showIntro() {
        mIsValid = false;
        try {
            getView().findViewById(R.id.intro).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            /* Should not throw a NPE if view is rendered and attached */
        }
    }

    public void hideIntro() {
        mIsValid = true;
        if (mIntro != null) {
            mIntro.setVisibility(View.GONE);
            enableActionMode(getView());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            ArrayList<SavedAction> actions;

            switch (requestCode) {
                case TaskWizardActivity.REQUEST_ADD_ACTION:
                    actions = data.getParcelableArrayListExtra(ConfigureActionsActivity.EXTRA_ACTIONS);
                    addActions(actions);
                    break;
                case TaskWizardActivity.REQUEST_EDIT_ACTIONS:

                    actions = data.getParcelableArrayListExtra(ConfigureActionsActivity.EXTRA_ACTIONS);
                    int position = data.getIntExtra(ConfigureActionsActivity.EXTRA_POSITION, -1);
                    Logger.d("Got edit back at position " + position);
                    Logger.d("Got back " + actions.size());
                    if (position < 0) {
                        // We don't have a position, this should be an edit of ALL actions
                        removeActions();
                        addActions(actions);
                    } else {
                        // we have a current position.
                        // This array list should contain ONE item
                        if (actions.size() > 0) {
                            replaceActionAtPosition(actions.get(0), position);
                        }

                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    @Override
    public Task getTask(int which) {
        return mTask;
    }

    @Override
    public void notifyResult(int requestCode, int resultCode, Intent data) {
        Logger.d("ACTIONS: Got result " + resultCode + " from request " + requestCode);
        onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void addClicked(int position) {
        Logger.d("TBF: Comparing tag " + mListenerTag + " to " + position);
        if (position == mListenerTag) {
            // + clicked
            if (mIsValid) {
                Intent intent = new Intent(getActivity(), ActionPickerActivity.class);
                intent.putExtra(TaskWizardActivity.EXTRA_FRAGMENT_NUM, position);
                getActivity().startActivityForResult(intent, TaskWizardActivity.REQUEST_ADD_ACTION);
            } else {
                // this is used for a switch task.  Prompt the user to 
                // pick an existing task or create a new one
                promptSelectTask();
            }
        }
    }

    private void promptSelectTask() {
        mProfileDialog = new SimpleDialogFragment(SimpleDialogFragment.layoutListView);
        mProfileDialog.setTitle(getActivity().getString(R.string.layoutProfileTaskChooserTitle));


        ArrayList<ListStringItem> tagList = getTagList();

        mProfileDialog.setListAdapter(new ListItemsAdapter(getActivity(), tagList.toArray(new ListStringItem[tagList.size()])));
        mProfileDialog.setListOnItemClickListener(convertProfileItemClicked);

        if (!getActivity().isFinishing()) {
            mProfileDialog.show(getFragmentManager(), "dialog");
        }
    }

    private OnItemClickListener convertProfileItemClicked = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String tagClicked = getString(R.string.layoutProfileNewTask);
            try {
                tagClicked = ((TextView) view.findViewById(R.id.row1Text)).getText().toString();
            } catch (Exception ignored) {
            }

            if (tagClicked.equals(getString(R.string.layoutProfileNewTask))) {
                mProfileDialog.dismiss();
                loadNewTask(null);
                makeProfileTag();
            } else {
                long tagId = getTagIDFromName(tagClicked);
                initLoaderForId(Long.toString(tagId));
                mProfileDialog.dismiss();
                makeProfileTag();
            }
        }
    };

    private long getTagIDFromName(String tName) {
        long tagID = 0;
        try {
            Cursor c = getActivity().getContentResolver().query(TaskProvider.Contract.TASKS, new String[]{DatabaseHelper.FIELD_TASK_ID}, DatabaseHelper.FIELD_TASK_NAME + "=?", new String[]{tName}, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    String sTagID = c.getString(0);
                    tagID = Long.parseLong(sTagID);
                }
                c.close();
            }
        } catch (Exception e) {
            Logger.e("Exception getting Tag ID: " + e, e);
        }
        return tagID;
    }

    private ArrayList<ListStringItem> getTagList() {
        ArrayList<ListStringItem> tagList = new ArrayList<ListStringItem>();
        tagList.add(new ListStringItem(getActivity(), getString(R.string.layoutProfileNewTask)));

        try {
            Cursor c = getActivity().getContentResolver().query(TaskProvider.Contract.TASKS, new String[]{DatabaseHelper.FIELD_TASK_ID, DatabaseHelper.FIELD_TASK_NAME}, null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    boolean keepReading = true;
                    while (keepReading) {
                        tagList.add(new ListStringItem(getActivity(), c.getString(1)));
                        keepReading = c.moveToNext();
                    }
                }
                c.close();
            }
        } catch (Exception e) {
            Logger.e("Exception getting tag list: " + e, e);
        }
        return tagList;
    }

    private void makeProfileTag() {
        hideIntro();
        mIsValid = true;
    }

    @Override
    public void onClick(View v) {
        /*Usage.logEvent(
                Usage.getAnalyticsObject(getActivity()),
                "Add action header",
                false
        );*/
        getActivity().startActivityForResult(
                new Intent(getActivity().getBaseContext(), ActionPickerActivity.class).putExtra(TaskWizardActivity.EXTRA_FRAGMENT_NUM, mTaskNum),
                TaskWizardActivity.REQUEST_ADD_ACTION);
    }


    @Override
    public void onResumeFragment() {

       if(mTask != null && mTask.getActions() != null && mTask.getActions().size() == 0) {
           onActionFabClicked();
       }
    }

}
