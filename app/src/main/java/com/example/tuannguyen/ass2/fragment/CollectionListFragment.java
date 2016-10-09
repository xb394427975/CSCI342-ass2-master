package com.example.tuannguyen.ass2.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tuannguyen.ass2.R;
import com.example.tuannguyen.ass2.model.Collection;
import com.example.tuannguyen.ass2.model.MyApplication;
import com.example.tuannguyen.ass2.model.ScrapbookModel;
import com.example.tuannguyen.ass2.utils.DialogCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuannguyen on 1/09/2015.
 */
public class CollectionListFragment extends Fragment {

    private ListView mCollectionListView;
    private List<String> mCollectionList;
    private ScrapbookModel mScrapbookModel;
    private ArrayAdapter<String> collectionArrayAdapter;
    private CollectionListFragment.Callbacks callbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScrapbookModel = ((MyApplication)getActivity().getApplication()).getScrapbookModel();
        mCollectionList = new ArrayList<String>();
        mCollectionList.add("All clippings");
        for (Collection collection : mScrapbookModel.getCollectionList())
        {
            mCollectionList.add(collection.getCollectionName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_list_collection, container, false);
        setHasOptionsMenu(true);
        mCollectionListView = (ListView)fragmentView.findViewById(R.id.lv_collections);

        //get data from the database
        getActivity().getActionBar().setTitle(R.string.app_name);

        collectionArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mCollectionList);
        mCollectionListView.setAdapter(collectionArrayAdapter);
        mCollectionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String collectionName;
                if (position == 0)
                {
                    collectionName = null;
                }
                else
                {
                    collectionName = mCollectionList.get(position);
                }
                callbacks.openClippingList(collectionName);
            }
        });

        registerForContextMenu(mCollectionListView);
        return fragmentView;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_collection_list_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final int position = menuInfo.position;
        DialogInterface.OnClickListener listener;
        if (position == 0)
        {
            return true;
        }
        switch(item.getItemId())
        {
            //delete a collection
            case R.id.menu_delete_collection:
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mScrapbookModel.deleteCollection(mCollectionList.get(position));
                        mCollectionList.remove(position);
                        collectionArrayAdapter.notifyDataSetChanged();
                    }
                };

                DialogCreator.createDialog(getActivity(), "Warning", "Do you want to delete this collection?", null, "Yes", listener, "No", null);
                break;
            //edit a collection name
            case R.id.menu_edit_collection:
                final String oldCollectionName = mCollectionList.get(position);
                final View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_collection,null);
                final EditText edt = (EditText) v.findViewById(R.id.edt_collection_name);
                edt.setText(oldCollectionName);
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String newCollectionName = edt.getText().toString().trim();
                        if (newCollectionName.isEmpty()) {
                            Toast.makeText(CollectionListFragment.this.getActivity().getApplicationContext(), "You must enter a collection name", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (newCollectionName.equals(oldCollectionName))
                        {
                            Toast.makeText(CollectionListFragment.this.getActivity().getApplicationContext(), "You have entered the same name", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (mScrapbookModel.findCollection(newCollectionName))
                        {
                            Toast.makeText(CollectionListFragment.this.getActivity().getApplicationContext(), "Collection already exists", Toast.LENGTH_LONG).show();
                            return;
                        }
                        mCollectionList.set(position, newCollectionName);
                        mScrapbookModel.editCollection(oldCollectionName, newCollectionName);
                        collectionArrayAdapter.notifyDataSetChanged();
                        dialogInterface.dismiss();
                    }
                };
                DialogCreator.createDialog(getActivity(), "Edit Collection", null, v, "Update", listener, "Cancel", null);


        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_collection_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_add_collection:
                final View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_collection,null);
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText edt = (EditText) v.findViewById(R.id.edt_collection_name);
                        String name = edt.getText().toString().trim();
                        if (name.isEmpty()) {
                            Toast.makeText(CollectionListFragment.this.getActivity().getApplicationContext(), "You must enter a collection name", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (mScrapbookModel.findCollection(name))
                        {
                            Toast.makeText(CollectionListFragment.this.getActivity().getApplicationContext(), "Collection already exists", Toast.LENGTH_LONG).show();
                            return;
                        }
                        mCollectionList.add(name);
                        mScrapbookModel.createCollection(name);
                        collectionArrayAdapter.notifyDataSetChanged();
                        dialogInterface.dismiss();
                    }
                };
                DialogCreator.createDialog(getActivity(), "Add a new Collection", null, v, "Create", listener, "Cancel", null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbacks = (CollectionListFragment.Callbacks) activity;
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
        void openClippingList(String collectionName);
    }
}
