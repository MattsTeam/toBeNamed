package me.chrislewis.mentorship;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Objects;

import me.chrislewis.mentorship.models.Camera;
import me.chrislewis.mentorship.models.Match;
import me.chrislewis.mentorship.models.User;

public class ProfileFragment extends Fragment {
    ImageView ivProfile;
    TextView tvName;
    TextView tvJob;
    TextView tvRating;
    TextView tvNumMatches;

    ImageButton bLogOut;
    ImageButton bEdit;
    Button bSave;
    CalendarFragment calendarFragment;
    EditProfileFragment editProfileFragment;
    BottomNavigationView bottomNavigationView;
    int checkedItemId;

    TextView tvAveRating;
    TextView tvMatches;

    User user;
    private SharedViewModel model;

    private Camera camera;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        container.startAnimation(fadeIn);
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        user = model.getCurrentUser();
        calendarFragment = (CalendarFragment) Objects.requireNonNull(getActivity())
                .getSupportFragmentManager()
                .findFragmentByTag("CalendarFragment");

        ivProfile = view.findViewById(R.id.ivProfile);
        if(user.getProfileImage() != null) {
            Glide.with(getContext())
                    .load(user.getProfileImage().getUrl())
                    .apply(new RequestOptions().circleCrop())
                    .into(ivProfile);
        }

        tvName = view.findViewById(R.id.tvName);
        tvJob = view.findViewById(R.id.tvJob);

        tvAveRating = view.findViewById(R.id.tvAveRating);
        tvMatches = view.findViewById(R.id.tvMatches);

        String name = user.getName();
        if (name != null) {
            tvName.setText(name);
        }
        String job = user.getJob();
        if (job != null) {
            tvJob.setText(job);
        }

        double rating = user.getOverallRating();
        if (rating != 0) {
            tvAveRating.setText(String.valueOf(rating));
        }

        Match.Query query = new Match.Query();
        query.findMatches(user);
        query.whereEqualTo("accepted", true);
        query.findInBackground();
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                tvMatches.setText(String.valueOf(count));
            }
        });

        bEdit = view.findViewById(R.id.bEdit);

        bEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = model.getFragmentManager();
                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                editProfileFragment = new EditProfileFragment();
                //fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                fragmentTransaction.replace(R.id.flContainer, editProfileFragment).commit();
            }
        });


        bLogOut = view.findViewById(R.id.bLogOut);
        bLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });


        bottomNavigationView = view.findViewById(R.id.nb_profile);

        if (checkedItemId == R.id.nb_categories) {
            ProfileCategoriesFragment profileCategoriesFragment = new ProfileCategoriesFragment();
            FragmentTransaction fragmentTransaction = this.getChildFragmentManager().beginTransaction();
            //fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            fragmentTransaction.replace(R.id.flProfile, profileCategoriesFragment).commit();
        } else if (checkedItemId == R.id.nb_reviews) {
            ProfileReviewsFragment profileReviewsFragment = new ProfileReviewsFragment();
            FragmentTransaction fragmentTransaction = this.getChildFragmentManager().beginTransaction();
            //fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            fragmentTransaction.replace(R.id.flProfile, profileReviewsFragment).commit();
        } else {
            ProfileGeneralFragment profileGeneralFragment = new ProfileGeneralFragment();
            FragmentTransaction fragmentTransaction = this.getChildFragmentManager().beginTransaction();
            //fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            fragmentTransaction.replace(R.id.flProfile, profileGeneralFragment).commit();
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                FragmentTransaction fragmentTransaction;
                switch (menuItem.getItemId()) {
                    case R.id.nb_general:
                        ProfileGeneralFragment profileGeneralFragment = new ProfileGeneralFragment();
                        fragmentTransaction = getChildFragmentManager().beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                        fragmentTransaction.replace(R.id.flProfile, profileGeneralFragment).commit();
                        checkedItemId = R.id.nb_general;
                        return true;
                    case R.id.nb_categories:
                        ProfileCategoriesFragment profileCategoriesFragment = new ProfileCategoriesFragment();
                        fragmentTransaction = getChildFragmentManager().beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                        fragmentTransaction.replace(R.id.flProfile, profileCategoriesFragment).commit();
                        checkedItemId = R.id.nb_categories;
                        return true;
                    case R.id.nb_reviews:
                        ProfileReviewsFragment profileReviewsFragment = new ProfileReviewsFragment();
                        fragmentTransaction = getChildFragmentManager().beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                        fragmentTransaction.replace(R.id.flProfile, profileReviewsFragment).commit();
                        checkedItemId = R.id.nb_reviews;
                        return true;
                }
                return false;
            }
        });

    }


}
