package me.chrislewis.mentorship;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;

import me.chrislewis.mentorship.models.Camera;
import me.chrislewis.mentorship.models.Review;
import me.chrislewis.mentorship.models.User;

import static android.app.Activity.RESULT_OK;
import static me.chrislewis.mentorship.MainActivity.PICK_IMAGE_REQUEST;

public class ComposeReviewFragment extends DialogFragment {
    private SharedViewModel model;
    private TextView tvUser;
    private RatingBar rbCompose;
    private EditText etReviewBody;
    private Button btnSubmitReview;
    private Button btnUploadPhoto;
    private ImageView ivReviewImage;

    User currentUser;
    User reviewedUser;

    private Camera camera;
    Bitmap photoBitmap;
    ParseFile parseFile;

    DetailsFragment2 detailsFragment2 = new DetailsFragment2();

    public ComposeReviewFragment() { }

    public static ComposeReviewFragment newInstance() {
        ComposeReviewFragment frag = new ComposeReviewFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.round_event_dialog);
        return inflater.inflate(R.layout.fragment_compose_review, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = ViewModelProviders.of((FragmentActivity) getActivity()).get(SharedViewModel.class);
        camera = new Camera(getContext(), this, model);
        currentUser = model.getCurrentUser();
        reviewedUser = model.getUser();

        tvUser = view.findViewById(R.id.tvUser);
        rbCompose = view.findViewById(R.id.rbCompose);
        LayerDrawable stars = (LayerDrawable) rbCompose.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.rgb(255, 128, 0), PorterDuff.Mode.SRC_ATOP);
        etReviewBody = view.findViewById(R.id.etReviewBody);
        btnSubmitReview = view.findViewById(R.id.btnSubmitReview);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);
        ivReviewImage = view.findViewById(R.id.ivReviewImage);


        if (reviewedUser.getUsername() != null ) {
            tvUser.setText(reviewedUser.getName() + "'s Review");
        } else {
            tvUser.setText("reviewed user's username");
        }


        btnSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String body = etReviewBody.getText().toString();
                double rating = rbCompose.getRating();
                final ParseUser otherUser = reviewedUser.getParseUser();
                String otherId = otherUser.getObjectId();
                Review review = new Review();
                review.setBody(body);
                review.setWriter(currentUser.getParseUser());

                Double mRating = Double.valueOf(rating);
                if (mRating != null) {
                    review.setRating(mRating);
                    Double pastUserRating = reviewedUser.getOverallRating();
                    Integer oldNumRatings = reviewedUser.getNumRatings();
                    Integer newNumRatings = oldNumRatings + 1;
                    reviewedUser.setNumRatings(newNumRatings);
                    Double newRating = (pastUserRating * oldNumRatings + mRating) / newNumRatings;
                    reviewedUser.setOverallRating(newRating);
                }

                if (photoBitmap != null) {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    photoBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                    parseFile = new ParseFile(bytes.toByteArray());
                    review.setReviewPhoto(parseFile);
                }

                review.setUserId(otherId);
                otherUser.revert();
                review.saveInBackground(new com.parse.SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(getActivity(), "You submitted a review", Toast.LENGTH_SHORT).show();
                            dismiss();
                            FragmentTransaction fragmentTransaction = model.getFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.flContainer, detailsFragment2).commit();
                            Log.d("Debug Reviews", "Reviews work");
                        }
                        else {
                            Log.d("Debug Reviews", "Failed review" + e);
                        }
                    }
                });
            }
        });

        btnUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.launchPhotos();
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                if (resultCode == RESULT_OK) {
                    photoBitmap = camera.getChosenPhoto(data);
                    Glide.with(getContext())
                            .load(photoBitmap)
                            .apply(new RequestOptions().circleCrop())
                            .into(ivReviewImage);
                } else {
                    Toast.makeText(getContext(), "Picture wasn't chosen!", Toast.LENGTH_SHORT).show();
                }

            }
        }

    }

}

