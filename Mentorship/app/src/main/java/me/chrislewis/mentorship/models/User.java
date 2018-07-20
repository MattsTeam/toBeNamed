package me.chrislewis.mentorship.models;

import android.text.format.DateUtils;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User{
    private final static String NAME_KEY = "name";
    private final static String USERNAME_KEY = "username";
    private final static String JOB_KEY = "job";
    private final static String PROFILE_IMAGE_KEY = "profileImage";
    private final static String SKILLS_KEY = "skills";
    private final static String SUMMARY_KEY = "summary";
    private final static String EDUCATION_KEY = "education";
    private final static String DESCRIPTION_KEY = "description";
    private final static String FAVORITE_KEY = "favorites";
    private final static String LOCATION_KEY = "location";
    private final static String DISTANCE_KEY = "distance";
    private final static String REL_DISTANCE_KEY = "relativeDistance";

    public String name;
    public String username;
    public String job;
    public String profileImage;
    public String skills;
    public String summary;
    public String education;
    public String description;
    public String location;
    public String distance;
    public String relativeDistance;

    ArrayList<User> favorites;

    private ParseUser user;

    public User(ParseUser user){
        this.user = user;
    }

    public void setUser(ParseUser user) {
        this.user = user;
    }

    public String getObjectId() {
        return user.getObjectId();
    }

    public String getUsername() {
        return user.getString(USERNAME_KEY);
    }

    public void setUsername(String username) {
        user.put(USERNAME_KEY, username);
    }

    public String getName() {
        return user.getString(NAME_KEY);
    }

    public void setName(String name) {
        user.put(NAME_KEY, name);
    }

    public String getJob() {
        return user.getString(JOB_KEY);
    }

    public void setJob(String job) {
        user.put(JOB_KEY, job);
    }

    public ParseFile getProfileImage() {
        return user.getParseFile(PROFILE_IMAGE_KEY);
    }

    public void setProfileImage(ParseFile file) {
        user.put(PROFILE_IMAGE_KEY, file);
    }

    public String getSkills() {
        return user.getString(SKILLS_KEY);
    }

    public void setSkills(String skills) {
        user.put(SKILLS_KEY, skills);
    }

    public String getSummary() {
        return user.getString(SUMMARY_KEY);
    }

    public void setSummary(String summary) {
        user.put(SUMMARY_KEY, summary);
    }

    public String getEducation() {
        return user.getString(EDUCATION_KEY);
    }

    public void setEducation(String education) {
        user.put(EDUCATION_KEY, education);
    }

    public String getDescription() {
        return user.getString(DESCRIPTION_KEY);
    }

    public void setDescription(String description) {
        user.put(DESCRIPTION_KEY, description);
    }

    public List<ParseUser> getFavorites() {
        return (List<ParseUser>) user.get(FAVORITE_KEY);
    }

    public void addFavorite(ParseUser user) {
        this.user.addUnique(FAVORITE_KEY, user);
        favorites.add(new User(user));
    }

    public void removeFavorite(User user) {
        this.user.removeAll(FAVORITE_KEY, Collections.singleton(user));
    }

    public ParseGeoPoint getCurrentLoction() {
        return user.getParseGeoPoint(LOCATION_KEY);
    }

    public String getDistance() {
        return user.getString(DISTANCE_KEY);
    }

    public void setDistance(String distance) {
        user.put(DISTANCE_KEY, distance);
    }

    public Double getRelDistance() {
        return user.getDouble(REL_DISTANCE_KEY);
    }

    public void setRelDistance(Double distance) {
        user.put(REL_DISTANCE_KEY, distance);
    }

    public void saveInBackground() {
        user.saveInBackground();
    }

    public void fetch() throws ParseException { user.fetch(); }

    public String getRelativeTimeAgo() {
        String relativeDate;
        long dateMillis = user.getCreatedAt().getTime();
        relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        return relativeDate;
    }

    public static class Query extends ParseQuery<ParseUser> {
        public Query() {
            super(ParseUser.class);
        }

    }

}