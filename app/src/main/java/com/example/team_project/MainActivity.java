package com.example.team_project;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.example.team_project.fragments.ComposeFragment;
import com.example.team_project.fragments.NotificationFragment;
import com.example.team_project.fragments.PostsFragment;
import com.example.team_project.fragments.ProfileFragment;
import com.example.team_project.models.Notification;
import com.example.team_project.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    private static final int MESSENGER_REQUEST_CODE = 100;
    public BottomNavigationView mBottomNavigationView;

    public static View notificationBadge;

    public String mCurrentUserUid;

    Fragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // runAsync();

        final FragmentManager fragmentManager = getSupportFragmentManager();
        // handle bottom navigation selection
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        fragment = new PostsFragment();
                        break;
                    case R.id.action_compose:
                        fragment = new ComposeFragment();
                        break;
                    case R.id.action_notification:
                        fragment = new NotificationFragment();
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.container_flowlayout, fragment).commit();
                return true;
            }
        });
        // Set default selection
        mBottomNavigationView.setSelectedItemId(R.id.action_home);

        // Add badge view for notifications
        addBadgeView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        Drawable drawable = menu.findItem(R.id.miMessenger).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this,R.color.white));
        menu.findItem(R.id.miMessenger).setIcon(drawable);
        Drawable drawable2 = menu.findItem(R.id.miSearch).getIcon();
        drawable2 = DrawableCompat.wrap(drawable2);
        DrawableCompat.setTint(drawable2, ContextCompat.getColor(this,R.color.white));
        menu.findItem(R.id.miSearch).setIcon(drawable2);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.miMessenger:
                goToMessenger();
                return true;
            case R.id.miSearch:
                goToSearch();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToSearch() {
        Intent toSearch = new Intent(this, SearchActivity.class);
        startActivity(toSearch);
    }

    // open ComposeActivity to create a new tweet
    private void goToMessenger() {
        Intent toMessenger = new Intent(this, MainMessenger.class);
        startActivityForResult(toMessenger, MESSENGER_REQUEST_CODE);
    }

    private void addBadgeView() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) mBottomNavigationView.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(2);

        notificationBadge = LayoutInflater.from(this).inflate(R.layout.view_notification_badge, menuView, false);

        itemView.addView(notificationBadge);
    }

    private void runAsync() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                checkNotififcations();
            }
        });
    }

    private void checkNotififcations() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("user-notifications").child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String key = snapshot.getKey();
                            Notification notification = snapshot.getValue(Notification.class);
                            if (!notification.seen) {
                                MainActivity.notificationBadge.setVisibility(View.VISIBLE);
                            }
                            // TODO - show push notification
                            if (!notification.pushed) {
                                createPushNotification(notification.title, notification.body, key);
                            }
                        }
                        runAsync();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        runAsync();
                    }
                });
    }

    private void createPushNotification(String title, String body, String key) {
        // create notification
        // Configure the channel
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("myChannelId", "My Channel", importance);
            channel.setDescription("Reminders");
            // Register the channel with the notifications manager
            NotificationManager mNotificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder mBuilder =
                    // Builder class for devices targeting API 26+ requires a channel ID
                    new NotificationCompat.Builder(this, "myChannelId")
                            .setSmallIcon(R.drawable.instagram_user_outline_24)
                            .setContentTitle(title)
                            .setContentText(body);
            int id = createID();
            mNotificationManager.notify(id, mBuilder.build());
        }
        // update notification to show pushed=true
        updateNotificationPushedStatus(key);
        // if notification is clicked, then show correct activity
        // if notificiation is clicked, update so seen=true
    }

    private void updateNotificationPushedStatus(final String key) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("user-notifications")
                .child(mCurrentUserUid)
                .child(key)
                .child("pushed");
        ref.setValue(true);
    }

    public int createID(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        return id;
    }
}