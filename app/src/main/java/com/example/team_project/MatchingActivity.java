package com.example.team_project;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.team_project.models.Match;
import com.example.team_project.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MatchingActivity extends Activity {

    private ArrayList<String> mMatches;
    private ArrayAdapter<String> arrayAdapter;
    private int i;
    public HashMap<String, Object> mCurrentUserNewCalendar;
    public HashMap<String, Object> mOtherUserNewCalendar;
    public HashMap<String, Object> mCurrentUserData;
    public HashMap<String, Object> mOtherNewMatch;
    DatabaseReference mReference;
    private HashMap<String, Boolean> mCurrentUserTime;
    private HashMap<String, Boolean> mOtherUserTime;
    private String mUserId;
    private FirebaseAuth mAuth;
    private HashMap<String, Boolean> mCurrentFriends;
    private String mCurrentFullName;
    private HashMap<String, String> userFreeMatchBefore;
    private HashMap<String, String> userFreeMatchFinal;
    private HashMap<String, String> status;
    private HashMap mOtherUserStatus;
    String mOtherUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);
        mReference = FirebaseDatabase.getInstance().getReference();
        mCurrentUserTime = new HashMap<String, Boolean>();
        mOtherUserTime = new HashMap<String, Boolean>();
        status = new HashMap<String, String>();
        userFreeMatchBefore = new HashMap<String, String>();
        userFreeMatchFinal = new HashMap<String, String>();
        mCurrentUserData = new HashMap<String, Object>();
        mOtherNewMatch = new HashMap<String, Object>();
        mAuth = FirebaseAuth.getInstance();
        mUserId = mAuth.getCurrentUser().getUid();
        mOtherUserId = this.mOtherUserId;

        getCurrentUserData();
        /**
         * Adding to mMatches is the name card,
         * i should keep the userId here and
         * add a new box with info about the user
         * maybe profile image
         */

        //getOtherUserMatch("OIFAGk70sfPS81IjUNvsOwZTTmf2");
        mMatches = new ArrayList<>();

        arrayAdapter = new ArrayAdapter<>(this, R.layout.item_choice, R.id.helloText, mMatches);

        /**
         * Removes the cards from the array here
         */
        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                mMatches.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            /**
             *If it comes out left then the user does not want to
             * hangout with them so nothing needs to be done
             */

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                //makeToast, "Left!");
                Toast.makeText(MatchingActivity.this, "swiped left", Toast.LENGTH_SHORT).show();
                status.put("deny", mOtherUserId);

            }

            /**
             * If swiped right then add it to somewhere on database
             * and check if other user also swiped right
             * if they do hit them both with a notification
             * if not let them know later
             */
            @Override
            public void onRightCardExit(Object dataObject) {
                Toast.makeText(MatchingActivity.this, "swiped right", Toast.LENGTH_SHORT).show();
                status.put(mOtherUserId, "oneUser");
                //makeToast(MyActivity.this, "Right!");
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                mMatches.add("XML ".concat(String.valueOf(i)));
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });


        /**
         * Can implement a closer look if wanted here
         */
        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

            }
        });
    }

    /**
     * In this method I just want to get the current users free time
     * and the one they are communicating with, this calls make complete
     * which sets the times they are not free to false to not get
     * a null pointer, checkFunc checks overlapping times and adds to
     * the array to display
     */

    private void getUserCalendar(String otherUserId) {
        final Query otherQuery = mReference.child("user-calendar/" + otherUserId);
        otherQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mOtherUserNewCalendar = (HashMap<String, Object>) dataSnapshot.getValue();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final Query currentQuery = mReference.child("user-calendar/" + mUserId);
        currentQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //for (DataSnapshot data : dataSnapshot.getChildren()) {
                mCurrentUserNewCalendar = (HashMap<String, Object>) dataSnapshot.getValue();
                //}
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mCurrentUserNewCalendar != null && mOtherUserNewCalendar != null) {
                    mCurrentUserTime = (HashMap<String, Boolean>) mCurrentUserNewCalendar.get("mFreeTime");
                    mOtherUserTime = (HashMap<String, Boolean>) mOtherUserNewCalendar.get("mFreeTime");
                    makeComplete();
                    checkFunc();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    /**
     * This puts false on the times the users is not free,
     * so there is no null pointer, this is called by getUserCalendar
     */

    private void makeComplete() {
        if (!mCurrentUserTime.containsKey("fridayMorning")) {
            mCurrentUserTime.put("fridayMorning", false);
        }
        if (!mCurrentUserTime.containsKey("fridayAfternoon")) {
            mCurrentUserTime.put("fridayAfternoon", false);
        }
        if (!mCurrentUserTime.containsKey("fridayEvening")) {
            mCurrentUserTime.put("fridayEvening", false);
        }
        if (!mCurrentUserTime.containsKey("saturdayMorning")) {
            mCurrentUserTime.put("saturdayMorning", false);
        }
        if (!mCurrentUserTime.containsKey("saturdayAfternoon")) {
            mCurrentUserTime.put("saturdayAfternoon", false);
        }
        if (!mCurrentUserTime.containsKey("saturdayEvening")) {
            mCurrentUserTime.put("saturdayEvening", false);
        }
        if (!mCurrentUserTime.containsKey("sundayMorning")) {
            mCurrentUserTime.put("sundayMorning", false);
        }
        if (!mCurrentUserTime.containsKey("sundayAfternoon")) {
            mCurrentUserTime.put("sundayAfternoon", false);
        }
        if (!mCurrentUserTime.containsKey("sundayEvening")) {
            mCurrentUserTime.put("sundayEvening", false);
        }

        //I could not add an and because the users data is different
        if (!mOtherUserTime.containsKey("fridayMorning")) {
            mOtherUserTime.put("fridayMorning", false);
        }
        if (!mOtherUserTime.containsKey("fridayAfternoon")) {
            mOtherUserTime.put("fridayAfternoon", false);
        }
        if (!mOtherUserTime.containsKey("fridayEvening")) {
            mOtherUserTime.put("fridayEvening", false);
        }
        if (!mOtherUserTime.containsKey("saturdayMorning")) {
            mOtherUserTime.put("saturdayMorning", false);
        }
        if (!mOtherUserTime.containsKey("saturdayAfternoon")) {
            mOtherUserTime.put("saturdayAfternoon", false);
        }
        if (!mOtherUserTime.containsKey("saturdayEvening")) {
            mOtherUserTime.put("saturdayEvening", false);
        }
        if (!mOtherUserTime.containsKey("sundayMorning")) {
            mOtherUserTime.put("sundayMorning", false);
        }
        if (!mOtherUserTime.containsKey("sundayAfternoon")) {
            mOtherUserTime.put("sundayAfternoon", false);
        }
        if (!mOtherUserTime.containsKey("sundayEvening")) {
            mOtherUserTime.put("sundayEvening", false);
        }
    }

    /**
     * anyMatchCheck is an int that checks if they have any times they can hang
     * if they can't then it puts the status as denied, this helps because it cuts
     * loops short and helps performance, so the first if checks if they have that
     * matching time, if they do i add the user to an array, the next loop checks
     * if it is in it for that time already, if it is not then it wont add it,
     * this is important bc we only want it the first time it runs and catches it
     */

    private void checkFunc() {
        Log.i("CalendarActivity", "!!!Map: " + mCurrentUserTime);
        int anyMatchCheck = 0;
        if (mCurrentUserTime.get("fridayMorning") == mOtherUserTime.get("fridayMorning")) {
            if (!mMatches.contains(mOtherUserId)) {
                mMatches.add(mOtherUserId);
                anyMatchCheck++;
            }
        }
        if (mCurrentUserTime.get("fridayAfternoon") == mOtherUserTime.get("fridayAfternoon")) {
            if (!mMatches.contains(mOtherUserId)) {
                mMatches.add(mOtherUserId);
                anyMatchCheck++;
            }
        }
        if (mCurrentUserTime.get("fridayEvening") == mOtherUserTime.get("fridayEvening")) {
            if (!mMatches.contains(mOtherUserId)) {
                mMatches.add(mOtherUserId);
                anyMatchCheck++;
            }
        }
        if (mCurrentUserTime.get("saturdayMorning") == mOtherUserTime.get("saturdayMorning")) {
            if (!mMatches.contains(mOtherUserId)) {
                mMatches.add(mOtherUserId);
                anyMatchCheck++;
            }
        }
        if (mCurrentUserTime.get("saturdayAfternoon") == mOtherUserTime.get("saturdayAfternoon")) {
            if (!mMatches.contains(mOtherUserId)) {
                mMatches.add(mOtherUserId);
                anyMatchCheck++;
            }
        }
        if (mCurrentUserTime.get("saturdayEvening") == mOtherUserTime.get("saturdayEvening")) {
            if (!mMatches.contains(mOtherUserId)) {
                mMatches.add(mOtherUserId);
                anyMatchCheck++;
            }
        }
        if (mCurrentUserTime.get("sundayMorning") == mOtherUserTime.get("sundayMorning")) {
            if (!mMatches.contains(mOtherUserId)) {
                mMatches.add(mOtherUserId);
                anyMatchCheck++;
            }
        }
        if (mCurrentUserTime.get("sundayAfternoon") == mOtherUserTime.get("sundayAfternoon")) {
            if (!mMatches.contains(mOtherUserId)) {
                mMatches.add(mOtherUserId);
                anyMatchCheck++;
            }
        }
        if (mCurrentUserTime.get("sundayEvening") == mOtherUserTime.get("sundayEvening")) {
            if (!mMatches.contains(mOtherUserId)) {
                mMatches.add(mOtherUserId);
                anyMatchCheck++;
            }
        }
        if (anyMatchCheck > 0) {
            if (status.containsKey(mOtherUserId)) {
                status.remove(mOtherUserId);
            }
            status.put(mOtherUserId, "oneUser");
        } else {
            status.put(mOtherUserId, "denied");
        }
    }

    /**
     *
     * @param userId - checks user
     * @param status - what is going on with other users and there match status
     *
     */

    private void writeNewPost(String userId, HashMap status) {
        String matchKey = mReference.child("match").push().getKey();
        Match match = new Match(userId, status);
        Map<String, Object> postValues = match.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/match/" + matchKey, postValues);
        childUpdates.put("/user-match/" + userId + "/" + matchKey, postValues);

        Log.i("CalendarActivity", "Key: " + matchKey);
        Log.i("CalendarActivity", "Key: " + userId);
        mReference.updateChildren(childUpdates);
        //Toast.makeText(this, "Post Successful!", Toast.LENGTH_LONG).show();
        //Intent launchPosts = new Intent(this, MainActivity.class);
        //startActivity(launchPosts);
    }

    /**
     * mOtherUserStatus would be a hash map of their user comparing with status with all users
     * @param userId - this is the other users id, this should be called from friends loop
     */

    private void getOtherUserMatch (final String userId) {
        final Query queryOther = mReference.child("user-match/" + userId);
        queryOther.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mOtherNewMatch = (HashMap<String, Object>) dataSnapshot.getValue();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mOtherUserNewCalendar != null) {
                    mOtherUserStatus = (HashMap) mOtherUserNewCalendar.get("status");
                }
                getUserCalendar(userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This goes through checking the status if this user swiped right it checks if
     * the other one did too.
     */
    private void getUserMatch() {
        final Query queryCurrent = mReference.child("user-match/" + mUserId);
        queryCurrent.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mCurrentUserData = (HashMap<String, Object>) dataSnapshot.getValue();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //overlap, oneUser, denied, match
                //check for denied or overlap
                if (!status.get(mOtherUserId).equals("denied")) {
                    if (status.get(mOtherUserId).equals("oneUser")) {
                        if(mOtherUserStatus.get(mUserId).equals(status.get(mOtherUserId))) {
                            status.remove(mOtherUserId);
                            status.put(mOtherUserId, "match");
                        }
                    }
                    if (status.get(mOtherUserId).equals("match")) {
                        Log.d("status", "current status " + status);
                        // notification
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This is where I can get current user info like friends
     */
    private void getCurrentUserData() {
        final Query query = mReference.child("user" + mUserId);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mCurrentUserData = (HashMap<String, Object>) dataSnapshot.getValue();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mCurrentUserData != null) {
                    mCurrentFullName = (String) mCurrentUserData.get("fullname");
                    mCurrentFriends = (HashMap<String, Boolean>) mCurrentUserData.get("friendList");
                    mCurrentUserData.get("mFreeTime");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mReference.child("users").child(mUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Map<String, Boolean> friendList = (Map<String, Boolean>) dataSnapshot.child("friendList").getValue();
                writeNewPost(mUserId, status);
                for (String friendUid : mCurrentUserData.keySet()) {
                   if (status.get(friendUid).isEmpty()) {
                       mOtherUserId = friendUid;
                       getOtherUserMatch(friendUid);
                       status.put(friendUid, "noStart");
                   }
                    //User user = snapshot.getValue(User.class);
//                    if (status.get(user.friends.keySet()).isEmpty()) {
//                        status.put((user.friends.keySet(), "")
//                    }
                }
                //Log.i("CalendarActivity", "mPosts: " + mPosts);
                //checkData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}

