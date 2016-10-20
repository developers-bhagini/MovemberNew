package com.app.movember.ui;


import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.movember.R;
import com.app.movember.util.Constants;
import com.app.movember.util.NetworkUtil;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.app.movember.util.Constants.REQUEST_PERMISSION;


/**
 * A simple {@link Fragment} subclass.
 */
public class DoctorShareFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = DoctorShareFragment.class.getSimpleName();

    private LoginButton mLoginButton;
    private Button mShareButton;
    private FrameLayout mBadgeView;
    private TextView mName;
    private TextView mPlace;
    private View rootView;
    private Bitmap mShareBitmap;
    private AlertDialog mAlertDialog;
    private Button mPhotoChooser;
    private Button mCommentButton;
    private ImageView mShareImageView;
    private String userCommentsData;
    private Uri imageUri = null;
    private ProgressDialog pd;
    private CallbackManager mCallbackManager;

    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private String[] mPermissions = new String[]{"publish_actions"};
    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            postToPage();
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "User cancelled ");
        }

        @Override
        public void onError(FacebookException e) {
            Toast.makeText(getActivity(), "Failed to login :: user not authorized", Toast.LENGTH_SHORT).show();

        }
    };


    public DoctorShareFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() ");
        initializeFacebookSdk();
        pd = new ProgressDialog(getActivity());
        rootView = inflater.inflate(R.layout.fragment_doctor_share, container, false);
        mShareButton = (Button) rootView.findViewById(R.id.share_button);
        mShareButton.setOnClickListener(this);
        mBadgeView = (FrameLayout) rootView.findViewById(R.id.badge_layout);
        mName = (TextView) rootView.findViewById(R.id.name);
        mPlace = (TextView) rootView.findViewById(R.id.place);
        mShareImageView = (ImageView) rootView.findViewById(R.id.sharing_image);
        mPhotoChooser = (Button) rootView.findViewById(R.id.photo_chooser);
        mCommentButton = (Button) rootView.findViewById(R.id.comment_button);
        mPhotoChooser.setOnClickListener(this);
        mCommentButton.setOnClickListener(this);
        if (null != getArguments()) {
            Bundle lData = getArguments();
            String lNameValue = lData.getString(Constants.DOCTOR_NAME);
            String lPlaceValue = lData.getString(Constants.DOCTOR_LOCATION);
            mName.setText(getString(R.string.dr_text) + lNameValue);
            mPlace.setText(lPlaceValue);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private void initView() {
        mLoginButton = (LoginButton) rootView.findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);
        mLoginButton.setPublishPermissions(mPermissions);
        mLoginButton.setFragment(new NativeFragmentWrapper(this));
        mLoginButton.registerCallback(mCallbackManager, callback);
        if (null != accessTokenTracker) {
            accessTokenTracker.startTracking();
        }
        if (null != profileTracker) {
            profileTracker.startTracking();
        }
    }

    /**
     * Method to initialize the facebook sdk
     */
    private void initializeFacebookSdk() {
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        if (Constants.FACEBOOK_DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }
        mCallbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
            }


        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                Log.d(TAG, "profile " + newProfile);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == Constants.REQUEST_CAMERA) {
                String lImageId = convertImageUriToFile(imageUri, getActivity());
                if (lImageId != null) {
                    new LoadImagesFromSDCard().execute("" + lImageId);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.device_not_supported_text), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (null != mCallbackManager) {
                    mCallbackManager.onActivityResult(requestCode, resultCode, data);
                }
            }
        }else if(resultCode == Activity.RESULT_CANCELED){
            Toast.makeText(getActivity(), getString(R.string.device_not_supported_text), Toast.LENGTH_SHORT).show();
            /*if(requestCode == Constants.REQUEST_CAMERA){
                String lImageId = convertImageUriToFile(imageUri, getActivity());
                if (lImageId != null) {
                    new LoadImagesFromSDCard().execute("" + lImageId);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.device_not_supported_text), Toast.LENGTH_SHORT).show();
                }
            }*/
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != accessTokenTracker) {
            accessTokenTracker.startTracking();
        }
        if (null != profileTracker) {
            profileTracker.startTracking();
        }
    }

    @Override
    public void onStop() {
        if (null != accessTokenTracker) {
            accessTokenTracker.stopTracking();
        }
        if (null != profileTracker) {
            profileTracker.stopTracking();
        }
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.share_button:
                if (NetworkUtil.areWeConnectedTonetwork(getActivity())) {
                    showFacebookPostConfirmation();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network_error_text), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.photo_chooser:
                checkForPermission();
               // selectImage();
                break;
            case R.id.comment_button:
                addComments();
                break;


        }
    }

    public static class NativeFragmentWrapper extends android.support.v4.app.Fragment {
        private Fragment nativeFragment = null;

        public NativeFragmentWrapper(Fragment nativeFragment) {
            this.nativeFragment = nativeFragment;
        }

        public NativeFragmentWrapper() {
        }

        @Override
        public void startActivityForResult(Intent intent, int requestCode) {
            nativeFragment.startActivityForResult(intent, requestCode);
        }

        @Override
        public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            nativeFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroyView() {
        ViewGroup mContainer = (ViewGroup) getActivity().findViewById(R.id.container);
        mContainer.removeAllViews();
        super.onDestroyView();

    }

    private void postToPage() {
        Bundle params = new Bundle();
        mBadgeView.setDrawingCacheEnabled(true);
        mBadgeView.buildDrawingCache();
        mShareBitmap = mBadgeView.getDrawingCache();
        if (null != userCommentsData && !userCommentsData.trim().equals("")) {
            params.putString("message", userCommentsData);
        }
        Log.d(TAG, "postToPage :: " + mShareBitmap);
        params.putParcelable("source", mShareBitmap);

        Log.d(TAG, "AccessToken.getCurrentAccessToken()" + AccessToken.getCurrentAccessToken());
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + Constants.PAGE_ID + "/photos",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (pd.isShowing()) {
                            pd.dismiss();
                        }
                        Log.d(TAG, "response :" + response);
                        if (response.getError() == null) {
                            Toast.makeText(getActivity(), "Posted successfully", Toast.LENGTH_SHORT).show();
                            FragmentTransaction lTranscation = getFragmentManager().beginTransaction();
                            lTranscation.setCustomAnimations(R.animator.card_flip_left_in, R.animator.card_flip_left_out,
                                    R.animator.card_flip_right_in, R.animator.card_flip_right_out);
                            // Create a new Fragment to be placed in the activity layout
                            DoctorDetailsFragment lDoctorDetails = new DoctorDetailsFragment();
                            // Add the fragment to the 'fragment_container' FrameLayout
                            lTranscation.addToBackStack("DoctorDetailsFragment");
                            lTranscation.replace(R.id.container, lDoctorDetails).commit();

                        } else {
                            Toast.makeText(getActivity(), "Failed to post " + response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ).executeAsync();
        pd.setTitle("Please wait");
        pd.setMessage("Facebook posting is in progress..");
        pd.show();
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
    }

    private void showFacebookPostConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirmation_title_message_text);
        builder.setCancelable(false);
        builder.setMessage(R.string.confirmation_dialog_message_text)
                .setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isLoggedIn()) {
                            postToPage();
                        } else {
                            mLoginButton = (LoginButton) rootView.findViewById(R.id.login_button);
                            mLoginButton.performClick();
                        }
                    }
                })
                .setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (null != mAlertDialog) {
                            mAlertDialog.dismiss();
                        }
                    }
                });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private void selectImage() {
        final CharSequence[] items = {getString(R.string.take_photo_text), getString(R.string.select_from_gallery_text)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.select_photo_dialog__title_text));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getString(R.string.take_photo_text))) {
                    String fileName = String.valueOf(System.currentTimeMillis());
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, fileName);
                    values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
                    imageUri = getActivity().getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    startActivityForResult(intent, Constants.REQUEST_CAMERA);
                } else if (items[item].equals(getString(R.string.select_from_gallery_text))) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            Constants.SELECT_FILE);
                }
            }
        });
        builder.show();
    }

    public void setImageBitMap(Bitmap bitMap) {
        if (null != mShareImageView) {
            mShareImageView.setImageBitmap(bitMap);
            TextView lPledgeText= (TextView) rootView.findViewById(R.id.pledgetext);
            lPledgeText.setVisibility(View.VISIBLE);
            lPledgeText.bringToFront();
        }
    }

    private void addComments() {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.comments_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.fb_user_comments);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                userCommentsData = userInput.getText().toString().trim();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    //TODO:needs to be more generic code for xiomi
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.MediaColumns.DATA};

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
            //Giving device width to the square image
            bitmap = Bitmap.createScaledBitmap(bitmap, getDeviceWidth(), getDeviceWidth(), true);
            setImageBitMap(getCroppedBitmap(bitmap));
        } catch (IOException e) {
            Log.e(TAG,"Exception :: ",e);
        }

/*        Cursor cursor = getActivity().getContentResolver().query(selectedImageUri, projection, null, null,
                null);
        Log.d(TAG, "data cursor");
        if (null != cursor && cursor.moveToNext()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String selectedImagePath = cursor.getString(column_index);

            Bitmap thumbnail;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(selectedImagePath, options);
            final int REQUIRED_SIZE = 400;
            int scale = 1;
            while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                    && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;

            thumbnail = BitmapFactory.decodeFile(selectedImagePath, options);
            //Giving device width to the square image
            thumbnail = Bitmap.createScaledBitmap(thumbnail, getDeviceWidth(), getDeviceWidth(), true);
            setImageBitMap(getCroppedBitmap(thumbnail));
        } else {
            Toast.makeText(getActivity(), getString(R.string.device_not_supported_text), Toast.LENGTH_SHORT).show();
        }*/
    }

    private int getDeviceWidth() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static String convertImageUriToFile(Uri imageUri, Activity activity) {

        Cursor cursor = null;
        int imageID = 0;

        try {

            /*********** Which columns values want to get *******/
            String[] proj = {
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Thumbnails._ID,
                    MediaStore.Images.ImageColumns.ORIENTATION
            };

            cursor = activity.getContentResolver().query(

                    imageUri,         //  Get data for specific image URI
                    proj,             //  Which columns to return
                    null,             //  WHERE clause; which rows to return (all rows)
                    null,             //  WHERE clause selection arguments (none)
                    null              //  Order-by clause (ascending by name)

            );

            //  Get Query Data
            if (null != cursor) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int size = cursor.getCount();

                /*******  If size is 0, there are no images on the SD Card. *****/

                if (size == 0) {


                    Log.d(TAG, "size is 0");
                } else {

                    int thumbID = 0;
                    if (cursor.moveToFirst()) {
                        imageID = cursor.getInt(columnIndex);
                    }
                }
            } else {
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "" + imageID;
    }


    public class LoadImagesFromSDCard extends AsyncTask<String, Void, Void> {

        private ProgressDialog Dialog = new ProgressDialog(getActivity());

        Bitmap mBitmap;

        protected void onPreExecute() {
            Dialog.setMessage(" Loading image from Sdcard..");
            Dialog.show();
        }


        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {

            Bitmap bitmap = null;
            Bitmap newBitmap = null;
            Uri uri = null;


            try {

                uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + urls[0]);
                bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri));

                if (bitmap != null) {
                    //taking device width for both height and width for the square image
                    newBitmap = Bitmap.createScaledBitmap(bitmap, getDeviceWidth(), getDeviceWidth(), true);

                    bitmap.recycle();

                    if (newBitmap != null) {
                        mBitmap = newBitmap;
                    }
                }
            } catch (IOException e) {
                cancel(true);
            }

            return null;
        }


        protected void onPostExecute(Void unused) {
            if (Dialog != null) {
                Dialog.dismiss();
            }
            if (mBitmap != null) {
                // Set Image to ImageView
                setImageBitMap(getCroppedBitmap(mBitmap));
            }

        }

    }

    private void checkForPermission(){
        boolean hasPermission = (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)&&
                (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)&&
                (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }else{
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_LONG).show();
                    selectImage();
                } else
                {
                    Toast.makeText(getActivity(), "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

}
