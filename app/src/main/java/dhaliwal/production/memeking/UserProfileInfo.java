package dhaliwal.production.memeking;

import java.util.HashMap;
import java.util.Map;

public class UserProfileInfo {
    private int audience;
    private int following;
    private int points;
    private String profile_photo;
    private String username;
    public Map<String,Boolean> followingId=new HashMap<>();
    public Map<String,Boolean> AudienceId=new HashMap<>();
    public UserProfileInfo(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)

    }



    public UserProfileInfo(String profile_photo, String username) {
        audience=0;
        following=0;
        points=0;
        this.profile_photo = profile_photo;
        this.username = username;

    }

    public int getAudience() {
        return audience;
    }

    public void setAudience(int audience) {
        this.audience = audience;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
