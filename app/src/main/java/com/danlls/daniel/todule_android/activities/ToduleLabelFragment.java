package com.danlls.daniel.todule_android.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.danlls.daniel.todule_android.R;
import com.danlls.daniel.todule_android.adapter.LabelAdapter;
import com.danlls.daniel.todule_android.parcelable.LongSparseArrayBooleanParcelable;
import com.danlls.daniel.todule_android.provider.ToduleDBContract;
import com.danlls.daniel.todule_android.provider.ToduleDBContract.TodoLabel;


/**
 * Created by danieL on 10/20/2017.
 */

public class ToduleLabelFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 99;
    LabelAdapter lAdapter;
    MainActivity myActivity;
    OnLabelSelectedListener mCallback;
    Long selectedLabelId = null;
    boolean selecting;
    private LongSparseArray<Boolean> selectedIds;

    public static ToduleLabelFragment newInstance(boolean select, Long selected_label_id) {

        Bundle args = new Bundle();
        args.putBoolean("select", select);
        if (selected_label_id != null){
            args.putLong("selected_label_id", selected_label_id);
        }
        ToduleLabelFragment fragment = new ToduleLabelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            selecting = bundle.getBoolean("select", false);
            selectedLabelId = bundle.getLong("selected_label_id", -1L);
        } else {
            selecting = false;
        }
        if(savedInstanceState != null) {
            selectedLabelId = savedInstanceState.getLong("selected_label_id", -1L);
            selectedIds = savedInstanceState.getParcelable("myLongSparseBooleanArray");
        }

        if (selectedIds == null){
            selectedIds = new LongSparseArray<>();
        }

        setHasOptionsMenu(true);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lAdapter = new LabelAdapter(getActivity(), null, 0);
        setListAdapter(lAdapter);
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        myActivity = (MainActivity) getActivity();
        myActivity.getSupportActionBar().setTitle("Labels");
        myActivity.hideSoftKeyboard(true);
        myActivity.fabVisibility(false);

        ListView listView = getListView();

        if(selecting){
            setActivateOnItemClick(true);
            myActivity.getSupportActionBar().setTitle("Select label");
            // Add "no label" to list
            View noLabel =  View.inflate(getContext(), R.layout.fragment_label_item, null);
            TextView labelTag = noLabel.findViewById(R.id.label_tag);
            labelTag.setText(R.string.none);
            listView.addHeaderView(noLabel);
        } else {
            listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(myMultiChoiceModeListener);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ToduleLabelAddFragment f = ToduleLabelAddFragment.newInstance(l);
                    myActivity.getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.fragment_container, f)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(selecting) {
            outState.putLong("selected_label_id", selectedLabelId);
        } else {
            outState.putParcelable("myLongSparseBooleanArray", new LongSparseArrayBooleanParcelable(selectedIds));
        }
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(selecting) {
            menu.findItem(R.id.label_new).setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        } else {
            menu.findItem(R.id.label_confirm).setVisible(false);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri LABEL_URI = TodoLabel.CONTENT_URI;
        String select = "(" + TodoLabel.COLUMN_NAME_TAG + " NOTNULL)";
        CursorLoader cursorLoader = new CursorLoader(getActivity(), LABEL_URI,
                TodoLabel.PROJECTION_ALL, select, null, TodoLabel.SORT_ORDER_DEFAULT);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        lAdapter.swapCursor(data);
        getListView().postDelayed(new Runnable() {
            @Override
            public void run() {
                ListView lv = getListView();
                if(selecting) {
                    if (selectedLabelId == -1L){
                        // Set headerview as checked
                        lv.setItemChecked(0, true);
                    } else {
                        lv.setItemChecked(getAdapterItemPosition(selectedLabelId) + lv.getHeaderViewsCount(), true);
                    }
                }
            }
        }, 100);

    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        lAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_label, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.label_new:
                ToduleLabelAddFragment f = ToduleLabelAddFragment.newInstance(null);
                myActivity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_container, f)
                        .addToBackStack(null)
                        .commit();
                return true;
            case R.id.label_confirm:
                mCallback.onLabelSelected(selectedLabelId);
                myActivity.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0){
                    // If user choose the headerview "no label"
                    selectedLabelId = -1L;
                } else {
                    Cursor cr = (Cursor) adapterView.getItemAtPosition(i);
                    selectedLabelId = cr.getLong(cr.getColumnIndexOrThrow(TodoLabel._ID));
                }
            }
        });
        lAdapter.setShowCheckbox(true);
    }

    public interface OnLabelSelectedListener {
        public void onLabelSelected(long id);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        if (context instanceof OnLabelSelectedListener){
            try {
                mCallback = (OnLabelSelectedListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement OnHeadlineSelectedListener");
            }
        }

    }

    private int getAdapterItemPosition(long id){
        for (int position=0; position < getListView().getCount(); position++){
            if(lAdapter.getItemId(position) == id){
                return position;
            }
        }
        return -1;
    }

    private AbsListView.MultiChoiceModeListener myMultiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
            if(b){
                selectedIds.put(l, b);
            } else {
                selectedIds.remove(l);
            }
            actionMode.setTitle(selectedIds.size() + " selected");
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.menu_action_mode_label, menu);

            lAdapter.setShowCheckbox(true);
            actionMode.setTitle(selectedIds.size() + " selected");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch(menuItem.getItemId()){
                case R.id.action_delete:
                    deleteSelectedLabels();
                    actionMode.finish();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            selectedIds.clear();
            lAdapter.setShowCheckbox(false);
        }
    };

    private void deleteSelectedLabels(){
        ContentResolver resolver = getContext().getContentResolver();
        int size = selectedIds.size();
        Long[] mArray = new Long[size];
        for (int i = 0; i < size; i++) {
            long id = selectedIds.keyAt(i);
            mArray[i] = id;
        }
        // Update affected entries by label deletion
        ContentValues cv = new ContentValues();
        cv.putNull(ToduleDBContract.TodoEntry.COLUMN_NAME_LABEL);
        String select = ToduleDBContract.TodoEntry.COLUMN_NAME_LABEL +  " IN(" + constructPlaceholders(mArray.length)+ ")";
        String[] selectionArgs = new String[mArray.length];
        for (int i =0; i< mArray.length; i++){
            selectionArgs[i] = String.valueOf(mArray[i]);
        }
        resolver.update(ToduleDBContract.TodoEntry.CONTENT_URI, cv, select ,selectionArgs);

        // Delete label from db
        select = TodoLabel._ID + " IN(" + constructPlaceholders(mArray.length)+ ")";
        int count = resolver.delete(TodoLabel.CONTENT_URI, select, selectionArgs);
        Toast.makeText(myActivity, String.valueOf(count) + " label deleted", Toast.LENGTH_SHORT).show();
    }

    private String constructPlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }
}
