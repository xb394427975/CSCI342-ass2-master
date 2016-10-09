package com.example.tuannguyen.ass2.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.example.tuannguyen.ass2.R;
import com.example.tuannguyen.ass2.fragment.ClippingDetailFragment;
import com.example.tuannguyen.ass2.fragment.ClippingListFragment;
import com.example.tuannguyen.ass2.fragment.CollectionListFragment;
import com.example.tuannguyen.ass2.fragment.CreateClippingFragment;
import com.example.tuannguyen.ass2.model.ScrapbookModel;


public class MainActivity extends Activity implements CollectionListFragment.Callbacks, ClippingListFragment.Callbacks, CreateClippingFragment.Callbacks{

    private ScrapbookModel model;
    private boolean flag;
    public static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createFragment(new CollectionListFragment(), false);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
    }


    @Override
    public void openClippingList(String collectionName) {
        ClippingListFragment fragment = new ClippingListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ClippingListFragment.COLLECTION_NAME, collectionName);
        fragment.setArguments(bundle);
        createFragment(fragment, true);
    }

    public void createFragment(Fragment fragment, boolean savedToBackStack)
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment);
        if (savedToBackStack == true)
        {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() != 0)
        {
            removeCurrentFragment(false);
            fragmentManager.popBackStack();
        }
        else
            super.onBackPressed();
    }

    private void removeCurrentFragment(boolean savedToBackStack) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        Fragment currentFrag =  getFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFrag != null)
            transaction.remove(currentFrag);
        if (savedToBackStack)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onAddClipping(String collectionName) {
        CreateClippingFragment fragment = new CreateClippingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ClippingListFragment.COLLECTION_NAME, collectionName);
        bundle.putBoolean(ClippingListFragment.EDIT_MODE, false);
        fragment.setArguments(bundle);
        createFragment(fragment, true);
    }

    @Override
    public void onEditClippingDetails(long id, String collectionName) {
        Fragment currentFrag =  getFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFrag instanceof ClippingDetailFragment)
        {
            onBackPressed(); // pop it out of the back stack so that the user cannot get back
            flag = true; // this flag allows the user to revert back to the old clipping detail after undo
        }
        CreateClippingFragment fragment = new CreateClippingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ClippingListFragment.COLLECTION_NAME, collectionName);
        bundle.putBoolean(ClippingListFragment.EDIT_MODE, true);
        bundle.putLong(ClippingDetailFragment.CLIPPING_ID, id);
        fragment.setArguments(bundle);
        createFragment(fragment, true);
    }

    @Override
    public void onOpenClippingDetails(long id, String collectionName) {
        ClippingDetailFragment fragment = new ClippingDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ClippingDetailFragment.CLIPPING_ID, id);
        bundle.putString(ClippingListFragment.COLLECTION_NAME, collectionName);
        fragment.setArguments(bundle);
        createFragment(fragment, true);
    }

    @Override
    public void onSaveClipping(long id) {
        ClippingDetailFragment fragment = new ClippingDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ClippingDetailFragment.CLIPPING_ID, id);
        fragment.setArguments(bundle);
        createFragment(fragment, false);
        flag = false;
    }

    @Override
    public void onEditingCancel(long id, String collectionName) {
        onBackPressed();
        if (flag)
        {
            flag = true;
            onOpenClippingDetails(id, collectionName);
            flag = false;
        }
    }
}
