package com.example.tuannguyen.ass2.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tuannguyen.ass2.R;
import com.example.tuannguyen.ass2.model.Clipping;
import com.example.tuannguyen.ass2.model.MyApplication;
import com.example.tuannguyen.ass2.model.ScrapbookModel;
import com.example.tuannguyen.ass2.utils.DialogCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuannguyen on 1/09/2015.
 */
public class ClippingListFragment extends Fragment {
    public static final String COLLECTION_NAME = "Collection name";
    public static final String EDIT_MODE = "Edit mode";
    private List<Clipping> mClippingList;
    private ListView mClippingListView;
    private ScrapbookModel mScrapbookModel;
    private Callbacks callbacks;
    private String mCollectionName;
    private ClippingAdapter mFullAdapter;
    private ClippingAdapter mPartialAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScrapbookModel = ((MyApplication)getActivity().getApplication()).getScrapbookModel();


    }

    private EditText mEdtSearchBox;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_list_clipping, container, false);


        mClippingListView = (ListView)v.findViewById(R.id.lv_clipping);

        Bundle bundle = getArguments();
        if (bundle != null)
        {
            mCollectionName = bundle.getString(COLLECTION_NAME);
            mClippingList = mScrapbookModel.getClippingsFromCollection(mCollectionName);
        }
        else
        {
            mClippingList = new ArrayList<>();
        }
        if (mCollectionName != null)
        {
            getActivity().getActionBar().setTitle(mCollectionName);
            setHasOptionsMenu(true);
        }
        else
        {
            getActivity().getActionBar().setTitle("All Clippings");
        }
        mClippingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Clipping selectedClipping  = mClippingList.get(position);
                callbacks.onOpenClippingDetails(selectedClipping.getId(), mCollectionName);
            }
        });
        mEdtSearchBox = (EditText)v.findViewById(R.id.edt_search_box);
        mEdtSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_SEARCH ||
                        id == EditorInfo.IME_ACTION_DONE ||
                        keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                                keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (!keyEvent.isShiftPressed()) {
                        doSearch();
                        return true;
                    }
                }
                return false;
            }
        });
        //check if the edit text has lost the focus
        mEdtSearchBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    doSearch();
                }
            }
        });
        mFullAdapter = new ClippingAdapter(mClippingList);
        mClippingListView.setAdapter(mFullAdapter);
        registerForContextMenu(mClippingListView);
        return v;
    }

    private void doSearch() {
        String searchString = mEdtSearchBox.getText().toString().trim();
        if (searchString.isEmpty())
        {
            mClippingListView.setAdapter(mFullAdapter);
            return;
        }
        List<Clipping> clippings = mScrapbookModel.getClippingsFromSearchString(searchString);
        mPartialAdapter = new ClippingAdapter(clippings);
        mClippingListView.setAdapter(mPartialAdapter);
    }


    private class ClippingAdapter extends ArrayAdapter<Clipping>
    {
        private List<Clipping> mClippings;
        public ClippingAdapter(List<Clipping> clippings) {
            super(getActivity(), R.layout.clipping_list_item, clippings);
            mClippings = clippings;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            Clipping clipping = mClippings.get(position);
            if (convertView == null)
            {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.clipping_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.notes = (TextView)convertView.findViewById(R.id.tv_notes);
                viewHolder.images = (ImageView)convertView.findViewById(R.id.image_clipping);
                convertView.setTag(viewHolder);
            }
            else
                viewHolder = (ViewHolder)convertView.getTag();
            viewHolder.notes.setText(clipping.getText());
            viewHolder.images.setImageBitmap(clipping.getThumbnail(getActivity()));
            return convertView;
        }

        private class ViewHolder
        {
            TextView notes;
            ImageView images;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.menu_clipping_list_context, menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_clipping_list, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final int position = menuInfo.position;
        switch(item.getItemId())
        {
            case R.id.menu_delete_clipping:
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mScrapbookModel.deleteClipping(mClippingList.get(position).getId());
                        mClippingList.remove(position);
                        ((ArrayAdapter<Clipping>)mClippingListView.getAdapter()).notifyDataSetChanged();
                    }
                };

                DialogCreator.createDialog(getActivity(), "Warning", "Do you want to delete this clipping?", null, "Yes", listener, "No", null);

                break;
            case R.id.menu_edit_clipping:
                callbacks.onEditClippingDetails(mClippingList.get(position).getId(), mCollectionName);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_add_clipping:
                callbacks.onAddClipping(mCollectionName);
                return true;
            case R.id.menu_delete_collection:
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mScrapbookModel.deleteCollection(mCollectionName);
                        getActivity().onBackPressed();
                    }
                };

                DialogCreator.createDialog(getActivity(), "Warning", "Do you want to delete this collection?", null, "Yes", listener, "No", null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbacks = (ClippingListFragment.Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }



    public interface Callbacks
    {
        void onAddClipping(String collectionName);
        void onEditClippingDetails(long id, String collectionName);
        void onOpenClippingDetails(long id, String collectionName);
    }


}
