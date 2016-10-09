package com.example.tuannguyen.ass2.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tuannguyen.ass2.R;
import com.example.tuannguyen.ass2.model.Clipping;
import com.example.tuannguyen.ass2.model.MyApplication;
import com.example.tuannguyen.ass2.model.ScrapbookModel;
import com.example.tuannguyen.ass2.utils.DialogCreator;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by tuannguyen on 1/09/2015.
 */
public class ClippingDetailFragment extends Fragment {
    public static final String CLIPPING_ID = "Clipping id";
    private TextView mNotes;
    private ImageView mClippingImage;
    private TextView mDates;
    private ScrapbookModel mScrapbookModel;
    private Clipping mClipping;
    private String mCollectionName;
    private ClippingListFragment.Callbacks callbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScrapbookModel = ((MyApplication)getActivity().getApplication()).getScrapbookModel();
        long id = 0;
        if (getArguments()!= null)
        {
            id = getArguments().getLong(ClippingDetailFragment.CLIPPING_ID);
            mCollectionName = getArguments().getString(ClippingListFragment.COLLECTION_NAME);
        }
        mClipping = mScrapbookModel.getClipping(id);
        Toast.makeText(getActivity().getApplicationContext(), mClipping.getReferencedPath(), Toast.LENGTH_SHORT).show();
        getActivity().getActionBar().setTitle("Clipping Details");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment_clipping_detail, container, false);
        if (mClipping != null)
        {
            mClippingImage = (ImageView)v.findViewById(R.id.img_clipping);
            mClippingImage.setImageBitmap(mClipping.getImage(getActivity()));
            mNotes = (TextView)v.findViewById(R.id.tv_notes);
            mNotes.setText(mClipping.getText());
            mDates = (TextView)v.findViewById(R.id.tv_dates);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss a");
            formatter.setTimeZone(TimeZone.getDefault());
            mDates.setText((formatter.format(mClipping.getCreatedDate())));
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_clipping_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_delete_clipping:
                if (mClipping != null)
                {
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mScrapbookModel.deleteClipping(mClipping.getId());
                            getActivity().onBackPressed();
                        }
                    };

                    DialogCreator.createDialog(getActivity(), "Warning", "Do you want to delete this clipping?", null, "Yes", listener, "No", null);
                }
                return true;
            case R.id.menu_edit_clipping:
                if (mClipping != null)
                {
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            callbacks.onEditClippingDetails(mClipping.getId(), mCollectionName);
                        }
                    };
                    DialogCreator.createDialog(getActivity(), "Warning", "Do you want to edit this clipping?", null, "Continue", listener, "Cancel", null);
                }
                return true;
            case R.id.menu_share_clipping:
                if (mClipping != null)
                {
                    //share clipping
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    if (mClipping.getImageUri() != null)
                        i.putExtra(Intent.EXTRA_STREAM, mClipping.getImageUri());
                    i.putExtra(Intent.EXTRA_TEXT, mClipping.getText());
                    if (getActivity().getPackageManager().resolveActivity(i, 0) != null)
                    {
                        Intent chooser = Intent.createChooser(i, "Share via");
                        startActivity(chooser);
                    }
                }

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
}
