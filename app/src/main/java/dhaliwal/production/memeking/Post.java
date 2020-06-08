package dhaliwal.production.memeking;


import java.util.HashMap;
import java.util.Map;

public class Post {
    private String uid;
    private int lit=0;
    public Map<String,Boolean> stars=new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }


    public Post(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public int getLit() {
        return lit;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setLit(int lit) {
        this.lit = lit;
    }
}
