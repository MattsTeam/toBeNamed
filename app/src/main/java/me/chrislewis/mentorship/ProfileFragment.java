package me.chrislewis.mentorship;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import me.chrislewis.mentorship.models.Camera;
import me.chrislewis.mentorship.models.User;

import static android.app.Activity.RESULT_OK;
import static me.chrislewis.mentorship.MainActivity.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;
import static me.chrislewis.mentorship.MainActivity.PICK_IMAGE_REQUEST;

public class ProfileFragment extends Fragment {

    FavoritesAdapter adapter;
    RecyclerView rvFavorites;

    ImageView ivProfile;
    EditText etName;
    EditText etJob;
    EditText etSkills;
    EditText etSummary;
    EditText etEdu;

    Button bLogOut;
    Button bUploadProfileImage;
    Button bTakePhoto;
    Button bEdit;
    Button bSave;
    CalendarFragment calendarFragment;

    User user;

    private SharedViewModel model;

    private Camera camera;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etName = view.findViewById(R.id.etName);
        etName.setEnabled(false);
        etJob = view.findViewById(R.id.etJob);
        etJob.setEnabled(false);
        etSkills = view.findViewById(R.id.etSkills);
        etSkills.setEnabled(false);
        etSummary = view.findViewById(R.id.etSummary);
        etSummary.setEnabled(false);
        etEdu = view.findViewById(R.id.etEdu);
        etEdu.setEnabled(false);
        ivProfile = view.findViewById(R.id.ivProfile);
        calendarFragment = (CalendarFragment) getActivity().getSupportFragmentManager().findFragmentByTag("CalendarFragment");

        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        user = model.getCurrentUser();
        camera = new Camera(getContext(), this, model);

        populateInfo();


        bEdit = view.findViewById(R.id.bEdit);

        bEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                etName.setEnabled(true);
                etJob.setEnabled(true);
                etSkills.setEnabled(true);
                etSummary.setEnabled(true);
                etEdu.setEnabled(true);
            }
        });


        bLogOut = view.findViewById(R.id.bLogOut);
        bLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                //TODO revoke gmail credentials
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        bTakePhoto = view.findViewById(R.id.bTakePhoto);
        bTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.launchCamera();
            }
        });

        bUploadProfileImage = view.findViewById(R.id.bUploadProfileImage);
        bUploadProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.launchPhotos();
            }
        });

        bSave = view.findViewById(R.id.bSave);
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String skills = etSkills.getText().toString();
                String job = etJob.getText().toString();
                String summary = etSummary.getText().toString();
                String edu = etEdu.getText().toString();

                user.setName(name);
                user.setSkills(skills);
                user.setJob(job);
                user.setSummary(summary);
                user.setEducation(edu);
                if (camera.getPhotoFile() != null) {
                    user.setProfileImage(camera.getPhotoFile());
                }
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d("Profile", "Working");
                        } else {
                            Log.d("Profile", "Fail " + e);
                        }
                    }
                });
                Toast.makeText(getActivity(), "Updated Profile", Toast.LENGTH_SHORT).show();
            }
        });

        adapter = new FavoritesAdapter(user.getFavorites(), model);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rvFavorites.setAdapter(adapter);
    }

    private void populateInfo() {
        etName.setText(user.getName());
        if (user.getJob() != null ) {
            etJob.setText(user.getJob());
        }
        if (user.getSkills() != null ) {
            etSkills.setText(user.getSkills());
        }
        if (user.getSummary() != null ) {
            etSummary.setText(user.getSummary());
        }
        if (user.getEducation() != null ) {
            etEdu.setText(user.getEducation());
        }
        if (user.getProfileImage() != null) {
            Glide.with(getContext())
                    .load(user.getProfileImage().getUrl())
                    .apply(new RequestOptions().circleCrop())
                    .into(ivProfile);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Glide.with(getContext())
                        .load(camera.getPhoto())
                        .apply(new RequestOptions().circleCrop())
                        .into(ivProfile);
            } else {
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                if (resultCode == RESULT_OK) {
                    Glide.with(getContext())
                            .load(camera.getChosenPhoto(data))
                            .apply(new RequestOptions().circleCrop())
                            .into(ivProfile);
                } else {
                    Toast.makeText(getContext(), "Picture wasn't chosen!", Toast.LENGTH_SHORT).show();
                }

            }
        }

    }
}
