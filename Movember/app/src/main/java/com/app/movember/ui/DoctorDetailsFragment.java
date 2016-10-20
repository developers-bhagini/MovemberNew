package com.app.movember.ui;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.app.movember.R;
import com.app.movember.util.Constants;


public class DoctorDetailsFragment extends Fragment implements View.OnClickListener {


    private Bundle mBundle;
    private View rootView;
    private EditText doctorNameText;
    private EditText doctorSpecialityText;
    private EditText doctorLocationText;
    private Button submitButton;


    public DoctorDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBundle = getArguments();
        rootView = inflater.inflate(R.layout.fragment_doctor_details, container, false);
        doctorNameText = (EditText) rootView.findViewById(R.id.doctor_name);
        doctorSpecialityText = (EditText) rootView.findViewById(R.id.speciality);
        doctorLocationText = (EditText) rootView.findViewById(R.id.location);
        submitButton = (Button) rootView.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_button:
                String doctorName = doctorNameText.getText().toString().trim();
                if (TextUtils.isEmpty(doctorName)) {
                    doctorNameText.setError("Name cannot be blank");
                    return;
                }
                String doctorSpeciality = doctorSpecialityText.getText().toString().trim();
                if (TextUtils.isEmpty(doctorSpeciality)) {
                    doctorSpecialityText.setError("Speciality cannot be blank");
                    return;
                }
                String doctorLocation = doctorLocationText.getText().toString().trim();
                if (TextUtils.isEmpty(doctorLocation)) {
                    doctorLocationText.setError("Location cannot be blank");
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString(Constants.DOCTOR_NAME, doctorName);
                bundle.putString(Constants.DOCTOR_SPECIALITY, doctorSpeciality);
                bundle.putString(Constants.DOCTOR_LOCATION, doctorLocation);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.card_flip_left_in, R.animator.card_flip_left_out,
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out);
                DoctorPledgeFragment pledgeFragment = new DoctorPledgeFragment();
                pledgeFragment.setArguments(bundle);
                ft.addToBackStack("DoctorPledgeFragment");
                ft.replace(R.id.container, pledgeFragment).commit();
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        doctorNameText.setText("");
        doctorLocationText.setText("");
        doctorSpecialityText.setText("");
    }

    @Override
    public void onDestroyView() {
        //mContainer.removeAllViews();
        ViewGroup mContainer = (ViewGroup) getActivity().findViewById(R.id.container);
        mContainer.removeAllViews();
        super.onDestroyView();
    }


}
