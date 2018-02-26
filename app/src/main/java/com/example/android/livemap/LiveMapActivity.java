package com.example.android.livemap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.livemap.Animation.ResizeAnimation;
import com.example.android.livemap.Database.Messages;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class LiveMapActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    // Firebase sign in
    private String mUsername;
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Firebase data
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private ChildEventListener mChildEventListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReferences;
    private DatabaseReference mUserLocationsDatabaseReferences;
    private DatabaseReference mObjectivesDatabaseReferences;


    // Frame manipulation
    private DragHandle mHandle;
    private RelativeLayout mBottomFrame;
    private int mBottomFrameSize;

    // UI
    private Scroller numberPicker;
    private Bitmap markerBitmap;
    private Button mSendButton;

    private EditText mMessageEditText;
    private StringBuilder mMessagesText = new StringBuilder("");
    private MessageDisplay mMessageDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_map);

        mHandle = (DragHandle) findViewById(R.id.drag_handle);
        mHandle.setOnTouchListener(new MyTouchListener());
        mBottomFrame = (RelativeLayout) findViewById(R.id.bottom_frame);
        mBottomFrameSize = mBottomFrame.getHeight();
        numberPicker = (Scroller) findViewById(R.id.scroller);
        initScroller(numberPicker);
        mSendButton = (Button) findViewById(R.id.sendButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageDisplay = (MessageDisplay) findViewById(R.id.message_display_box);

        // Authentication
        FirebaseApp.initializeApp(getApplicationContext());
        mUsername = ANONYMOUS;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(
                                            AuthUI.GOOGLE_PROVIDER,
                                            AuthUI.FACEBOOK_PROVIDER,
                                            AuthUI.EMAIL_PROVIDER)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        // DATABASE
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReferences = mFirebaseDatabase.getReference().child("messages");
        mUserLocationsDatabaseReferences = mFirebaseDatabase.getReference().child("users");
        mObjectivesDatabaseReferences = mFirebaseDatabase.getReference().child("objectives");

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Messages messages = new Messages(mUsername, mMessageEditText.getText().toString());
                mMessagesDatabaseReferences.push().setValue(messages);

                mMessageEditText.setText("");
            }
        });

        // MAP CALL
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.Menu_map);
        mapFragment.getMapAsync(this);
    }

    public void initScroller(Scroller scroller) {
        String[] options = {"hello", "jeanfilip", "fff", "bbbb"};
        int max = options.length;
        scroller.setDisplayedValues(options);
        scroller.setMinValue(0);
        scroller.setMaxValue(max -1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        mMessagesText = new StringBuilder("");
        detachDatabaseReadListener();
    }

    private void onSignedInInitialize(String username) {
        mUsername = username;
        attachDatabaseReadListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
        mMessagesText = new StringBuilder("");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Icons
        markerBitmap = new HelperClass()._getBitmap(R.drawable.ic_android_black_24dp, this);

        // Coordinates
        LatLng sydney = new LatLng(-33.852, 151.211);

        // Markers
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(sydney)
                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));



        marker.setAlpha(1.0f);
        //marker.remove();

        //Map Style
        MapStyleOptions style1 = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json);
        MapStyleOptions style2 = MapStyleOptions.loadRawResourceStyle(this, R.raw.style2_json);
        googleMap.setMapStyle(style1);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Messages message = dataSnapshot.getValue(Messages.class);
                    mMessagesText.append(message);
                    mMessageDisplay.setText(mMessagesText);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            mMessagesDatabaseReferences.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReferences.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    /**************************************************************************
     * Management of the layout manipulations
     */
    //Reaction to the button being pressed
    private final class MyTouchListener implements View.OnTouchListener {
        // TODO Handle Handler ;)

        private int mActivePointerId = INVALID_POINTER_ID;

        float mLastTouchY;
        float mPosY;


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int Y = (int) event.getY();
            int MaxHeight = 1000;
            int MinHeight = 600;
            int GForce = 5;

            switch (MotionEventCompat.getActionMasked(event)) {

                case MotionEvent.ACTION_DOWN: {
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    final float y = MotionEventCompat.getY(event, pointerIndex);

                    // Remember where we started (for dragging)
                    mLastTouchY = y;
                    // Save the ID of this pointer (for dragging)
                    mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    final int pointerIndex =
                            MotionEventCompat.findPointerIndex(event, mActivePointerId);

                    final float y = MotionEventCompat.getY(event, pointerIndex);

                    // Calculate the distance moved
                    final float dy = y - mLastTouchY;

                    mPosY += dy;


                    // Remember this touch position for the next move event
                    mLastTouchY = y;

                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {

                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);

                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mLastTouchY = MotionEventCompat.getY(event, newPointerIndex);
                        mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
                    }
                    break;
                }
            }

            mBottomFrame.getParent();
            ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) mBottomFrame.getLayoutParams();

            int newHeight = layoutParams.height - (int) mPosY;
            if (newHeight <= MaxHeight && newHeight >= MinHeight) {
                ResizeAnimation animation = new ResizeAnimation(mBottomFrame);
                animation.setDuration(200);
                animation.setParams(layoutParams.height, newHeight);
                mBottomFrame.startAnimation(animation);
            }


//            //toastMessage(String.format("%s:%s",layoutParams.height,mSubViewSize));
//            v.requestLayout();
            return true;
        }
    }
}
