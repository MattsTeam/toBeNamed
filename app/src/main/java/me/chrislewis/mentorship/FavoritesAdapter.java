package me.chrislewis.mentorship;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.chrislewis.mentorship.models.Chat;
import me.chrislewis.mentorship.models.Match;
import me.chrislewis.mentorship.models.User;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private Context mContext;
    private List<Match> matches;
    User currentUser;
    MatchAdapter matchAdapter;

    private boolean isMentor;

    SharedViewModel model;
    private FragmentTransaction fragmentTransaction;
    private DetailsFragment2 detailsFragment = new DetailsFragment2();
    private MessageFragment messageFragment = new MessageFragment();

    FavoritesAdapter(List<User> favorites, List<Match> matches, SharedViewModel model) {
        this.matches = matches;
        this.model = model;
        currentUser = model.currentUser;
        isMentor = currentUser.getIsMentor();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder (@NonNull ViewGroup viewGroup,int i){
        mContext = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = inflater.inflate(R.layout.item_favorite, viewGroup, false);
        matchAdapter = model.getMatchAdapter();

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder ( @NonNull final ViewHolder viewHolder, int i){
        Match match = matches.get(i);
        User user = null;
        try {
            if (isMentor) {
                user = match.getMentee();
                if (!match.isAccepted()) {
                    viewHolder.btAccept.setVisibility(View.VISIBLE);
                    viewHolder.btDecline.setVisibility(View.VISIBLE);
                }
            } else {
                user = match.getMentor();
            }

            viewHolder.tvName.setText(user.getName());
            Double dist = user.getRelDistance();
            if (dist != 0) {
                viewHolder.tvDist.setText(dist.toString() + " miles away");
            }
            String description = user.getDescription();
            if (description != null) {
                viewHolder.tvDescription.setText(description);
            }

            Glide.with(mContext)
                    .load(user.getProfileImage().getUrl())
                    .apply(new RequestOptions().circleCrop())
                    .into(viewHolder.ivProfile);
        } catch (Exception e) {
            currentUser.removeMatch(match);
        }
    }

    @Override
    public int getItemCount () {
        if (matches == null) {
            return 0;
        } else {
            return matches.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivProfile;
        TextView tvName;
        TextView tvDescription;
        Button btAccept;
        Button btDecline;
        TextView tvDist;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvDist = itemView.findViewById(R.id.tvDistPending);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btAccept = itemView.findViewById(R.id.btAccept);
            btDecline = itemView.findViewById(R.id.btDecline);

            btAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Match match = matches.get(position);
                        match.setAccepted(true);
                        match.saveInBackground();
                        btAccept.setVisibility(View.INVISIBLE);
                        btDecline.setVisibility(View.INVISIBLE);
                        Chat chat = new Chat(new ArrayList<User>(Arrays.asList(match.getMentee(), match.getMentor())));
                        chat.saveInBackground();
                        notifyDataSetChanged();
                        matchAdapter.notifyDataSetChanged();

                    }
                }
            });
            btDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Match match = matches.get(position);
                        match.deleteInBackground();
                        notifyDataSetChanged();
                        matchAdapter.notifyDataSetChanged();
                    }
                }
            });
            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                User user = null;
                if(isMentor) {
                    user = matches.get(position).getMentee();
                } else {
                    user = matches.get(position).getMentor();
                }
                model.setUser(user);

                fragmentTransaction = model.getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.flContainer, detailsFragment).commit();
            }
        }
    }

    public void clear () {
        matches.clear();
        notifyDataSetChanged();
    }

    public void addAll (List < Match > list) {
        matches.addAll(list);
        notifyDataSetChanged();
    }
}
