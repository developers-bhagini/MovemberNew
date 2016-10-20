package com.app.movember.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.app.movember.R;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private AlphaAnimation mAnimation = new AlphaAnimation(1F, 0.5F);
    private Button mMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_details_new);
        mMenuButton = (Button) findViewById(R.id.menu_button);
        mMenuButton.setOnClickListener(this);
        FragmentTransaction lTransaction = getFragmentManager().beginTransaction();
        DoctorDetailsFragment lFragment = new DoctorDetailsFragment();
        lTransaction.replace(R.id.container, lFragment);
        lTransaction.commit();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_button:
                PopupWindow popupwindow_obj = popupDisplay();
                popupwindow_obj.showAsDropDown(view, -40, 18); // where u want show on view click event popupwindow.showAsDropDown(view, x, y);
                break;
            case R.id.home_button:
                FragmentTransaction lTranscation = getFragmentManager().beginTransaction();
                lTranscation.setCustomAnimations(R.animator.card_flip_left_in, R.animator.card_flip_left_out,
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out);
                // Create a new Fragment to be placed in the activity layout
                DoctorDetailsFragment lDoctorDetails = new DoctorDetailsFragment();
                lTranscation.addToBackStack("DoctorDetailsFragment");
                // Add the fragment to the 'fragment_container' FrameLayout
                lTranscation.replace(R.id.container, lDoctorDetails).commit();
                break;
            case R.id.help_button:
                Toast.makeText(this, getString(R.string.in_progress_text), Toast.LENGTH_SHORT).show();
                break;

        }
    }

    public PopupWindow popupDisplay() {

        final PopupWindow popupWindow = new PopupWindow(this);

        // inflate your layout or dynamically add view
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.custom_popup_layout, null);

        Button item_home = (Button) view.findViewById(R.id.home_button);
        Button item_help = (Button) view.findViewById(R.id.help_button);
        item_home.setOnClickListener(this);
        item_help.setOnClickListener(this);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);

        return popupWindow;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home:
                FragmentTransaction lTranscation = getFragmentManager().beginTransaction();
                lTranscation.setCustomAnimations(R.animator.card_flip_left_in, R.animator.card_flip_left_out,
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out);
                // Create a new Fragment to be placed in the activity layout
                DoctorDetailsFragment lDoctorDetails = new DoctorDetailsFragment();
                lTranscation.addToBackStack("DoctorDetailsFragment");
                // Add the fragment to the 'fragment_container' FrameLayout
                lTranscation.replace(R.id.container, lDoctorDetails).commit();
                break;
            case R.id.menu_help:
                Toast.makeText(this, getString(R.string.in_progress_text), Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        Fragment myFragment = getFragmentManager().findFragmentById(R.id.container);
        if (myFragment != null && myFragment instanceof DoctorDetailsFragment) {
            finishAffinity();
        }else{
            super.onBackPressed();
        }
    }
}
