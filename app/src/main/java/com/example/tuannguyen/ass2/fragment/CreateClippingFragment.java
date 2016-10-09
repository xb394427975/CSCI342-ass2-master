package com.example.tuannguyen.ass2.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.tuannguyen.ass2.R;
import com.example.tuannguyen.ass2.model.Clipping;
import com.example.tuannguyen.ass2.model.MyApplication;
import com.example.tuannguyen.ass2.model.ScrapbookModel;
import com.example.tuannguyen.ass2.utils.DialogCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

/**
 * Created by tuannguyen on 1/09/2015.
 */
public class CreateClippingFragment extends Fragment implements View.OnClickListener {
    private Callbacks callbacks;
    private Button btnSelectImage;
    private Button btnSaveClipping;
    private boolean mIsEditing;
    private EditText edtNotes;
    private ImageView cancelButton;
    private ImageView selectedImage;
    private ScrapbookModel mScrapbookModel;
    private Clipping mClipping;
    private String mCollectionName;
    private Bitmap mOriginalImage;
    private File mOriginalFile;
    private String mOldNotes;
    private int mId;
    File imageFile;
    public static final int REQUEST_CAPTURE_PHOTO = 1;
    public static final int REQUEST_BROWSE_GALLERY = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScrapbookModel = ((MyApplication) getActivity().getApplication()).getScrapbookModel();
        //getting data
        if (getArguments() != null) {
            mIsEditing = getArguments().getBoolean(ClippingListFragment.EDIT_MODE);
            mCollectionName = getArguments().getString(ClippingListFragment.COLLECTION_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_clipping, container, false);
        btnSelectImage = (Button) v.findViewById(R.id.btn_selectImg);
        btnSaveClipping = (Button) v.findViewById(R.id.btn_saveClipping);
        edtNotes = (EditText) v.findViewById(R.id.edt_notes);
        selectedImage = (ImageView) v.findViewById(R.id.img_selectedImage);
        cancelButton = (ImageView) v.findViewById(R.id.img_cancel);
        cancelButton.setOnClickListener(this);
        btnSelectImage.setOnClickListener(this);
        btnSaveClipping.setOnClickListener(this);

        if (mIsEditing) {
            getActivity().getActionBar().setTitle("Edit Clipping");
            setHasOptionsMenu(true);
            mId = getArguments().getInt(ClippingDetailFragment.CLIPPING_ID);
            mClipping = mScrapbookModel.getClipping(mId);
            String referencedPath = mClipping.getReferencedPath();
            mOldNotes = mClipping.getText();
            edtNotes.setText(mOldNotes);
            if (referencedPath != null) {
                mOriginalFile = new File(mClipping.getReferencedPath());
                imageFile = mOriginalFile;
                mOriginalImage = mClipping.getImage(getActivity());
                toggleImage();
                selectedImage.setImageBitmap(mOriginalImage);
            }
        } else {
            getActivity().getActionBar().setTitle("Add a new Clipping");
        }
        return v;
    }


    private void toggleImage() {
        if (selectedImage.getVisibility() == View.GONE) {
            cancelButton.setVisibility(View.VISIBLE);
            selectedImage.setVisibility(View.VISIBLE);
        } else {
            cancelButton.setVisibility(View.GONE);
            selectedImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btnSelectImage) {
            //display 2 options
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.alert_dialog_choose_image_string))
                    .setPositiveButton("Cancel", null);
            builder.setItems(new String[]{"Take a photo", "Choose an image from the gallery"}, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int position) {
                    if (position == 0) {
                        takePicture();
                    } else if (position == 1) {
                        browseGallery();
                    }
                }
            });
            builder.create().show();
        } else if (view == btnSaveClipping) {
            //save clipping
            saveClipping();


        } else if (view == cancelButton) {
            //toggle images and buttons
            toggleImage();
            imageFile = null;
        }
    }

    private void browseGallery() {
        //pick a photo in the gallery
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_BROWSE_GALLERY);

    }

    private void takePicture() {

        //take a new photo
        File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String imageName = "Scrapbook" + new Date().toString() + ".jpg";
        imageFile = new File(picturesDirectory, imageName);
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(i, REQUEST_CAPTURE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE_PHOTO) {
            if (resultCode == Activity.RESULT_CANCELED) {
                imageFile = null;
            } else {
                setImage();
            }
        } else if (requestCode == REQUEST_BROWSE_GALLERY) {

            String[] projetions = {MediaStore.Images.Media.DATA};
            if (data != null) {
                Uri imageUri = data.getData();

                Cursor cursor = getActivity().getContentResolver().query(imageUri, projetions, null, null, null);
                cursor.moveToFirst();
                //get the image filePath
                String imagePath = cursor.getString(cursor.getColumnIndex(projetions[0]));
                cursor.close();
                imageFile = new File(imagePath);
            } else {
                imageFile = null;
            }

            setImage();
        }
    }

    private void setImage() {

        //update the preview image
        if (imageFile == null || !imageFile.exists()) {
            imageFile = null;
            return;
        }
        Bitmap photo = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        if (photo != null) {
            if (selectedImage.getVisibility() == View.GONE) {
                toggleImage();
            }
            selectedImage.setImageBitmap(photo);
        } else {
            Toast.makeText(this.getActivity().getApplicationContext(), "Unable to take the image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveClipping() {
        final String notes = edtNotes.getText().toString();

        //nothing has changed
        if (mIsEditing && imageFile == mOriginalFile && notes.equals(mOldNotes)) {
            DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    callbacks.onSaveClipping(mId);
                }
            };
            DialogCreator.createDialog(getActivity(), "Warning", "Nothing has changed. Proceed?", null, "Continue", positiveListener, "Cancel", null);
        }
        //if the user has not chosen any image
        else if (imageFile == null) {
            //display dialog
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        if (!mIsEditing) {
                            //create a clipping and add it to the collection
                            mClipping = mScrapbookModel.createClipping(notes.toString(), null);
                            mScrapbookModel.addClippingToCollection(mClipping.getId(), mCollectionName);
                        } else {
                            //delete the old reference path in the database
                            mScrapbookModel.editClipping(mClipping.getId(), mClipping.getReferencedPath(), notes, null, true);
                        }
                        callbacks.onSaveClipping(mClipping.getId());
                        dialogInterface.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            DialogCreator.createDialog(getActivity(), "Warning", "No image has been chosen. Proceed?", null, "Continue", listener, "Cancel", null);
        } else {
            boolean error = false;
            try {
                //create a new clipping
                if (!mIsEditing) {
                    mClipping = mScrapbookModel.createClipping(notes, new FileInputStream(imageFile));
                    mScrapbookModel.addClippingToCollection(mClipping.getId(), mCollectionName);
                } else {
                    //the image has not changed but the notes has been updated
                    if (mOriginalFile == imageFile) {
                        mScrapbookModel.editClipping(mClipping.getId(), mClipping.getReferencedPath(), notes, null, false);
                    } else {
                        //both image and notes are updated
                        mScrapbookModel.editClipping(mClipping.getId(), mClipping.getReferencedPath(), notes, new FileInputStream(imageFile), true);
                    }
                }
            } catch (FileNotFoundException e) {
                error = true;
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (error) {
                DialogCreator.createDialog(getActivity(), "Error!", "Fail to create clipping", null, "OK", null, null, null);
            } else {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callbacks.onSaveClipping(mClipping.getId());
                        dialogInterface.dismiss();
                    }
                };
                if (!mIsEditing)
                    DialogCreator.createDialog(getActivity(), null, "New Clipping has been added", null, "Continue", listener, null, null);
                else
                    DialogCreator.createDialog(getActivity(), null, "Clipping has been updated", null, "Continue", listener, null, null);
            }
        }
    }


    //attach to an activity, add the callbacks
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbacks = (CreateClippingFragment.Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callbacks");
        }
    }

    //remove the callbacks
    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    public interface Callbacks {
        public void onSaveClipping(long id);

        public void onEditingCancel(long id, String collectionName);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_create_clipping, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //allow the user to undo changes
            case R.id.menu_undo_editing:
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callbacks.onEditingCancel(mId, mCollectionName);
                    }
                };
                DialogCreator.createDialog(getActivity(), "Warning", "You are about to discard the changes you have made. Proceed?", null, "Continue", listener, "Cancel", null);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
