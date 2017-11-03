package com.example.daniel.todule_android.activities;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.provider.ToduleDBContract;
import com.example.daniel.todule_android.utilities.DateTimeUtils;


/**
 * Created by danieL on 11/2/2017.
 */

public class ToduleDetailFragment extends Fragment {
    MainActivity myActivity;
    private Long entryId;

    public static ToduleDetailFragment newInstance(long id) {
        ToduleDetailFragment f= new ToduleDetailFragment();

        Bundle args = new Bundle();
        args.putLong("entry_id", id);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity = (MainActivity) getActivity();
        entryId = getArguments().getLong("entry_id");
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        TextView titleView = view.findViewById(R.id.detail_title);
        TextView descriptionView = view.findViewById(R.id.detail_description);
        TextView labelView = view.findViewById(R.id.detail_label);
        TextView createdDateView = view.findViewById(R.id.detail_created_date);
        TextView dueDateView = view.findViewById(R.id.detail_due_date);
        TextView countdownView = view.findViewById(R.id.detail_countdown);

        Uri entryUri = ContentUris.withAppendedId(ToduleDBContract.TodoEntry.CONTENT_ID_URI_BASE, entryId);
        Cursor cr = getContext().getContentResolver().query(entryUri, ToduleDBContract.TodoEntry.PROJECTION_ALL, null, null, null);
        cr.moveToFirst();
        String title = cr.getString(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_TITLE));
        String description = cr.getString(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_DESCRIPTION));
        Long dueDate = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_DUE_DATE));
        Long createdDate = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_CREATED_DATE));
        Long labelId;
        if(cr.isNull(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_LABEL))){
            labelId = null;
        } else{
            labelId = cr.getLong(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_LABEL));
        }
        int taskStatus = cr.getInt(cr.getColumnIndexOrThrow(ToduleDBContract.TodoEntry.COLUMN_NAME_TASK_DONE));
        cr.close();

        titleView.setText(title);
        if(description.isEmpty()){
            descriptionView.setText(R.string.no_descrption);
        } else {
            descriptionView.setText(description);
        }

        String createDateString = DateUtils.formatDateTime(getContext(), createdDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_TIME);
        String dueDateString = DateUtils.formatDateTime(getContext(), dueDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_TIME);
        String countdownString =  DateTimeUtils.dateTimeDiff(dueDate);

        if(dueDate < System.currentTimeMillis()) {
            countdownView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        } else {
            countdownView.setTextColor(ContextCompat.getColor(getContext(), R.color.normalGreen));
        }

        createdDateView.setText("Created: " + createDateString);
        dueDateView.setText(dueDateString);
        countdownView.setText(countdownString);

        if(labelId != null){
            Uri labelUri = ContentUris.withAppendedId(ToduleDBContract.TodoLabel.CONTENT_ID_URI_BASE, labelId);
            Cursor labelCr = getContext().getContentResolver().query(labelUri, ToduleDBContract.TodoLabel.PROJECTION_ALL, null, null, null);
            labelCr.moveToFirst();
            String labelTitle = labelCr.getString(labelCr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_TAG));
            int labelColor = labelCr.getInt(labelCr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_COLOR));
            int labelTextColor = labelCr.getInt(labelCr.getColumnIndexOrThrow(ToduleDBContract.TodoLabel.COLUMN_NAME_TEXT_COLOR));

            labelView.setText(labelTitle);
            labelView.setBackgroundColor(labelColor);
            labelView.setTextColor(labelTextColor);
            labelView.setVisibility(View.VISIBLE);

            labelCr.close();
        } else {
            labelView.setVisibility(View.GONE);
        }

        myActivity.getSupportActionBar().setTitle(title);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_edit:
                ToduleAddFragment frag = new ToduleAddFragment();
                Bundle args = new Bundle();
                args.putString("mode", "edit_entry");
                args.putLong("entry_id", entryId);
                frag.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, frag, "add_frag")
                        .addToBackStack(null)
                        .commit();
        }
        return super.onOptionsItemSelected(item);
    }
}