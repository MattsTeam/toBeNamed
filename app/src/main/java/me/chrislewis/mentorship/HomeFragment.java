package me.chrislewis.mentorship;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.chrislewis.mentorship.models.User;

public class HomeFragment extends Fragment {

    private User currentUser;

    private List<String> currentCategories;
    private GridAdapter gridAdapter;
    private ArrayList<ParseUser> users;
    private RecyclerView rvUsers;
    private ProgressBar pb;
    private SwipeRefreshLayout swipeContainer;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ArrayAdapter<String> drawerAdapter;
    private List<ParseUser> sameCategoryUsers;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ParseUser.getCurrentUser().fetchInBackground();
        currentUser = new User(ParseUser.getCurrentUser());
        mDrawerLayout = view.findViewById(R.id.drawer_layout);
        NavigationView navigationView = view.findViewById(R.id.drawer_view);
        final Menu menu = navigationView.getMenu();


        currentCategories = currentUser.getCategories();
        if (currentCategories != null) {
            for (String category : currentCategories) {
                int itemId = currentCategories.indexOf(category);
                menu.add(Menu.NONE, itemId, Menu.NONE, category);
            }
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                uncheckOtherItems(menuItem, menu);
                Toast.makeText(getActivity(),"Showing mentors for " + menuItem, Toast.LENGTH_LONG).show();
                filterByCategory(menuItem.toString());
                switch (menuItem.getItemId()) {
                    case 0:
                        Toast.makeText(getActivity(), "Showing mentors for" + menuItem, Toast.LENGTH_LONG).show();
                    case 1:
                        Toast.makeText(getActivity(), "art", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });

        rvUsers = view.findViewById(R.id.rvGrid);

        users = new ArrayList<>();
        gridAdapter = new GridAdapter(users);
        pb = view.findViewById(R.id.pbLoading);

        rvUsers.setLayoutManager(new GridLayoutManager(this.getActivity(), 2));
        rvUsers.setAdapter(gridAdapter);

        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllUsers();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        getAllUsers();
    }

    public void getAllUsers() {
        pb.setVisibility(ProgressBar.VISIBLE);
        gridAdapter.clear();
        ParseUser.getCurrentUser().fetchInBackground();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("isMentor", true);
        query.whereNotEqualTo("objectId", currentUser.getObjectId());


        try {
            List<ParseUser> users = query.find();
            sameCategoryUsers = new ArrayList<>(users);

            for (ParseUser user : users) {
                User mUser = new User(user);
                List<String> common = mUser.getCategories();
                common.retainAll(currentUser.getCategories());

                if (common.size() == 0) {
                    sameCategoryUsers.remove(user);
                    Log.i("common", "Common is null");
                }
            }

            for(int i = 0; i < sameCategoryUsers.size(); i++) {
                User user = new User (sameCategoryUsers.get(i));
                user.setRank(calculateRank(user));
            }
            Collections.sort(sameCategoryUsers, new Comparator<ParseUser>() {
                @Override
                public int compare(ParseUser o1, ParseUser o2) {
                    return (int)(o1.getDouble("rank") - (o2.getDouble("rank")));
                }
            });
            gridAdapter.addAll(sameCategoryUsers);

        }
        catch (com.parse.ParseException e) {
            e.printStackTrace();
        }
        pb.setVisibility(ProgressBar.INVISIBLE);
        swipeContainer.setRefreshing(false);

    }

    public double calculateRank(User user) {
        double distanceRank = 0;
        double organizationRank = 0;
        double educationRank = 0;
        distanceRank = 10 * user.getRelDistance();
        if(!user.getOrganization().equals(currentUser.getOrganization())) {
            organizationRank = 4;
        }
        if(!user.getEducation().equals(currentUser.getOrganization())) {
            educationRank = 5;
        }
        Log.d("UserRank", " username: " + user.getUsername());
        Log.d("UserRank", "distance rank: " + Double.toString(distanceRank));
        Log.d("UserRank", "organization rank: " + Double.toString(organizationRank));
        Log.d("UserRank", "education rank: " + Double.toString(educationRank));
        Log.d("UserRank", "total points: " + Double.toString(distanceRank + organizationRank + educationRank));
        return distanceRank + organizationRank + educationRank;
    }

    public void filterByCategory(String category) {
        mDrawerLayout.closeDrawers();

        pb.setVisibility(ProgressBar.VISIBLE);
        gridAdapter.clear();
        ParseUser.getCurrentUser().fetchInBackground();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("isMentor", true);
        query.whereNotEqualTo("objectId", currentUser.getObjectId());
        query.whereContains("categories", category);

        try {
            List<ParseUser> users = query.find();
            sameCategoryUsers = new ArrayList<>(users);

            for(int i = 0; i < sameCategoryUsers.size(); i++) {
                User user = new User (sameCategoryUsers.get(i));
                user.setRank(calculateRank(user));
            }
            Collections.sort(sameCategoryUsers, new Comparator<ParseUser>() {
                @Override
                public int compare(ParseUser o1, ParseUser o2) {
                    return (int)(o1.getDouble("rank") - (o2.getDouble("rank")));
                }
            });
            gridAdapter.addAll(sameCategoryUsers);

        }
        catch (com.parse.ParseException e) {
            e.printStackTrace();
        }
        pb.setVisibility(ProgressBar.INVISIBLE);
        swipeContainer.setRefreshing(false);

    }

    public void uncheckOtherItems(MenuItem item, Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            if (i != item.getItemId()) {
                menu.getItem(i).setChecked(false);
            }
        }
    }
}