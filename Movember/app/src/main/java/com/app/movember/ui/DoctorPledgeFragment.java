package com.app.movember.ui;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.app.movember.R;


public class DoctorPledgeFragment extends Fragment implements View.OnClickListener {

    private Button yesButton;
    private Button noButton;
    private Bundle mBundle;
    View rootView;


    public DoctorPledgeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBundle = getArguments();
        rootView = inflater.inflate(R.layout.fragment_doctor_pledge, container, false);
        yesButton = (Button) rootView.findViewById(R.id.yes_button);
        noButton = (Button) rootView.findViewById(R.id.no_button);
        yesButton.setOnClickListener(this);
        noButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.yes_button:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.card_flip_left_in, R.animator.card_flip_left_out,
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out);


                // Create a new Fragment to be placed in the activity layout
                DoctorShareFragment firstFragment = new DoctorShareFragment();

                // In case this activity was started with special instructions from an
                // Intent, pass the Intent's extras to the fragment as arguments
                firstFragment.setArguments(mBundle);
                ft.addToBackStack("DoctorShareFragment");
                // Add the fragment to the 'fragment_container' FrameLayout
                ft.replace(R.id.container,firstFragment).commit();
                break;
            case R.id.no_button:
                FragmentTransaction lTranscation = getFragmentManager().beginTransaction();
                lTranscation.setCustomAnimations(R.animator.card_flip_left_in, R.animator.card_flip_left_out,
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out);


                // Create a new Fragment to be placed in the activity layout
                DoctorDetailsFragment lDoctorDetails = new DoctorDetailsFragment();
                lTranscation.addToBackStack("DoctorDetailsFragment");
                // Add the fragment to the 'fragment_container' FrameLayout
                lTranscation.replace(R.id.container, lDoctorDetails).commit();
                break;
        }
    }

    private void finishActivityIfRequired() {
        if (!getActivity().isFinishing()) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        //mContainer.removeAllViews();
        ViewGroup mContainer = (ViewGroup) getActivity().findViewById(R.id.container);
        mContainer.removeAllViews();
        super.onDestroyView();
    }
}
