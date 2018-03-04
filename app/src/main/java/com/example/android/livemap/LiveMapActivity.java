package com.example.android.livemap;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.livemap.Animation.DragLayout;
import com.example.android.livemap.Animation.ResizeAnimation;
import com.example.android.livemap.Database.DatabaseIntermediate;
import com.example.android.livemap.Database.Messages;
import com.example.android.livemap.Database.Objectives;
import com.example.android.livemap.Database.ObjectivesContract;
import com.example.android.livemap.Database.TestObjectivesData;
import com.example.android.livemap.Database.Users;
import com.example.android.livemap.Widget.MyJobService;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Trigger;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
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

import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_DESCRIPTION;
import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_LAT;
import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_LNG;
import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_TITLE;

public class LiveMapActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ObjectivesAdapter.OnClickHandler,
        OnMapReadyCallback {

    // Firebase sign in
    private String mUsername;
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Firebase data
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private ChildEventListener mChildEventListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReferences;
    private DatabaseReference mUserLocationsDatabaseReferences;
    private DatabaseReference mObjectivesDatabaseReferences;
    private DatabaseIntermediate mDatabaseIntermediate;

    private Cursor mObjectivesCursor;
    private ImageButton mAddButton;
    private ImageButton mDeleteButton;

    private ContentResolver mObjectivesContentResolver;

    // Frame manipulation
    private DragHandle mHandle;
    private RelativeLayout mBottomFrame;
    private int mBottomFrameSize;

    // UI
    private Scroller numberPicker;
    private Bitmap markerBitmap;
    private Button mSendButton;
    private Button mCancelButton;
    private RelativeLayout theWholeFrame;

    private ObjectivesAdapter mObjectivesAdapter;
    private RecyclerView mObjectivesRecyclerView;

    private EditText mMessageEditText;
    private StringBuilder mMessagesText = new StringBuilder("Welcome! Connect to the internet " +
            "to experience the full app");
    private MessageDisplay mMessageDisplay;
    private FusedLocationProviderApi mFusedLocationClient;
    private GoogleApiClient mClient;
    private GoogleMap mGoogleMap;
    private LocationListener mLocationListener;
    private Uri mContentResolverUri = ObjectivesContract.ObjectivesEntry.CONTENT_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_map);

        mHandle = (DragHandle) findViewById(R.id.drag_handle);
        mBottomFrame = (RelativeLayout) findViewById(R.id.bottom_frame);
        mHandle.setOnTouchListener(new DragLayout(mBottomFrame, LiveMapActivity.this));
        mBottomFrameSize = mBottomFrame.getHeight();
        changeSizeAnimation(mBottomFrame, 2000, 600); //(ViewGroup rView, int GForce, int newSize)
        numberPicker = (Scroller) findViewById(R.id.scroller);
        initScroller(numberPicker);
        mSendButton = (Button) findViewById(R.id.sendButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageDisplay = (MessageDisplay) findViewById(R.id.message_display_box);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mCancelButton.setVisibility(View.GONE);

        theWholeFrame = (RelativeLayout) findViewById(R.id.the_whole_frame);

        mObjectivesRecyclerView = (RecyclerView) this.findViewById(R.id.objectives_recycler_view);
        mObjectivesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Uri uri = ObjectivesContract.ObjectivesEntry.CONTENT_URI;
        mObjectivesContentResolver = LiveMapActivity.this.getContentResolver();
        // Insert Fake data
        mObjectivesContentResolver.delete(uri, null, null);
        for(ContentValues c:new TestObjectivesData().insertFakeData()){
            mObjectivesContentResolver.insert(uri, c);
        }
        mObjectivesCursor = mObjectivesContentResolver.query(uri, null, null, null, null, null);
        mObjectivesAdapter = new ObjectivesAdapter(LiveMapActivity.this,
                mObjectivesCursor, this);
        mObjectivesRecyclerView.setAdapter(mObjectivesAdapter);

        mAddButton = (ImageButton) findViewById(R.id.add_objectives);
        mDeleteButton = (ImageButton) findViewById(R.id.delete_objectives);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mObjectivesAdapter.newObjective) return;
                if (mObjectivesAdapter.deleteObjectives) {
                    mObjectivesAdapter.deleteObjectives = false;
                }
                mObjectivesAdapter.newObjective = true;
                mObjectivesAdapter.newPosition = mObjectivesAdapter.getItemCount() - 1;
                //Toast.makeText(LiveMapActivity.this, mObjectivesAdapter.getItemCount(), Toast.LENGTH_SHORT).show();
                mObjectivesAdapter.notifyDataSetChanged();
                mObjectivesRecyclerView.scrollToPosition(mObjectivesAdapter.newPosition);
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mObjectivesAdapter.deleteObjectives = true;
                mObjectivesAdapter.notifyDataSetChanged();
                mMessageDisplay.setText(R.string.deleting_objectives_message);
                mMessageEditText.setVisibility(View.GONE);
                mSendButton.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.VISIBLE);
                mCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMessageDisplay.setText(mMessagesText);
                        mMessageEditText.setVisibility(View.VISIBLE);
                        mSendButton.setVisibility(View.VISIBLE);
                        mCancelButton.setVisibility(View.GONE);
                        mObjectivesAdapter.deleteObjectives = false;
                        mObjectivesAdapter.notifyDataSetChanged();
                        mCancelButton.setOnClickListener(null);
                    }
                });
            }
        });

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
        mDatabaseIntermediate = new DatabaseIntermediate();

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

        mFusedLocationClient = LocationServices.FusedLocationApi;

        if (savedInstanceState != null) {
            mObjectivesAdapter.deleteObjectives = savedInstanceState.getBoolean(DELETE_OBJ);
            mObjectivesAdapter.newPosition = savedInstanceState.getInt(NEW_POS);
            mObjectivesAdapter.newObjective = savedInstanceState.getBoolean(NEW_OBJ);
            if (mObjectivesAdapter.deleteObjectives) {
                mCancelButton.setVisibility(View.VISIBLE);
                mCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMessageDisplay.setText(mMessagesText);
                        mMessageEditText.setVisibility(View.VISIBLE);
                        mSendButton.setVisibility(View.VISIBLE);
                        mCancelButton.setVisibility(View.GONE);
                        mObjectivesAdapter.deleteObjectives = false;
                        mObjectivesAdapter.notifyDataSetChanged();
                        mCancelButton.setOnClickListener(null);
                    }
                });
            }
            mObjectivesAdapter.notifyDataSetChanged();
        }
    }

    private final String DELETE_OBJ = "delete_objectives";
    private final String NEW_POS = "new_position";
    private final String NEW_OBJ = "new_objective";
    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putBoolean(DELETE_OBJ, mObjectivesAdapter.deleteObjectives);
        outState.putInt(NEW_POS, mObjectivesAdapter.newPosition);
        outState.putBoolean(NEW_OBJ, mObjectivesAdapter.newObjective);
    }

    public void initScroller(Scroller scroller) {
        String[] options = {"STOP REQUESTING", "20sec/req", "40sec/req", "1min/req", "2min/req", "5min/req"};
        int max = options.length;
        scroller.setDisplayedValues(options);
        scroller.setMinValue(0);
        scroller.setMaxValue(max -1);
        scroller.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                if (mLocationListener == null) return;
                mFusedLocationClient.removeLocationUpdates(mClient, mLocationListener);
                int t = delayedLocationRequest(newVal);
                if (t != -1) {
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(t);

                    startLocationUpdates(locationRequest, mLocationListener);
                    return;
                }
                mFusedLocationClient.removeLocationUpdates(mClient, mLocationListener);
            }
        });
        jobDispacherWidgetUpdate();
    }

    private void startLocationUpdates(LocationRequest request, LocationListener listener) {
        if (ActivityCompat.checkSelfPermission(LiveMapActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(LiveMapActivity.this, "You will not be able to broadcast your location",
                    Toast.LENGTH_LONG).show();
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mClient, request, listener, null/* Looper */);
    }

    private int delayedLocationRequest(int scroller_position) {
        int delay;
        switch (scroller_position) {
            case 0:
                return -1;
            case 1:
                delay = 20 * 1000;
                break;
            case 2:
                delay = 40 * 1000;
                break;
            case 3:
                delay = 60 * 1000;
                break;
            case 4:
                delay = 2 * 60 * 1000;
                break;
            case 5:
                delay = 5 * 60 * 1000;
                break;
            default:
                return -1;
        }
        return delay;
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
        if (mLocationListener != null && mClient.isConnected()) {
            mFusedLocationClient.removeLocationUpdates(mClient, mLocationListener);
        }
        detachDatabaseReadListener();
    }

    Marker userLocation;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        // Current Location
        ActivityCompat.requestPermissions(LiveMapActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_FINE_LOCATION);

        if (ActivityCompat.checkSelfPermission(LiveMapActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(LiveMapActivity.this, "You will not be able to broadcast your location",
                    Toast.LENGTH_LONG).show();
        } else {
            mClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .enableAutoManage(this, this)
                    .build();

            mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    mDatabaseIntermediate.uploadUserData(new Users(mUsername, lat, lng), mUserLocationsDatabaseReferences);
                    LatLng position = new LatLng( lat, lng);
                    userLocation = mGoogleMap.addMarker(new MarkerOptions()
                            .position(position)
                            .icon(BitmapDescriptorFactory.fromBitmap(new HelperClass().
                                    _getBitmap(R.drawable.ic_green_android, LiveMapActivity.this)))
                            .title("your location"));
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                }
            };
        }

        // Icons
        markerBitmap = new HelperClass()._getBitmap(R.drawable.ic_android_black_24dp, this);

        // Coordinates
        LatLng sydney = new LatLng(-33.852, 151.211);

        // Markers
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(sydney)
                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                .title("Ninja Lair"));

        // Objectives Markers
        loadObjectiveMarkers();

        marker.setAlpha(1.0f);
        //marker.remove();

        //Map Style
        MapStyleOptions style1 = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json);
        MapStyleOptions style2 = MapStyleOptions.loadRawResourceStyle(this, R.raw.style2_json);
        googleMap.setMapStyle(style1);
    }

    private void loadObjectiveMarkers() {
        mObjectivesCursor.moveToFirst();
        while (mObjectivesCursor.moveToNext()) {
            LatLng objectivePosition = new LatLng(mObjectivesCursor.getDouble
                    (mObjectivesCursor.getColumnIndex
                            (ObjectivesContract.ObjectivesEntry.OBJECTIVES_LAT)),
                    mObjectivesCursor.getDouble
                            (mObjectivesCursor.getColumnIndex
                                    (ObjectivesContract.ObjectivesEntry.OBJECTIVES_LNG)));

            mGoogleMap.addMarker(new MarkerOptions()
                    .position(objectivePosition)
                    .title(mObjectivesCursor.getString(mObjectivesCursor.getColumnIndex
                            (ObjectivesContract.ObjectivesEntry.OBJECTIVES_TITLE))));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                boolean notFirst = false;
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    //Toast.makeText(LiveMapActivity.this, s + "\n" + dataSnapshot.getRef().getParent().getRoot().toString(), Toast.LENGTH_LONG).show();

                    // Message
                    if (dataSnapshot.getRef().getParent().toString().equals
                            (dataSnapshot.getRef().getParent().getRoot().toString() + "/messages")) {
                        Messages message = dataSnapshot.getValue(Messages.class);
                        if (notFirst) {
                            mMessagesText.append("\n");
                        } else {
                            mMessagesText = new StringBuilder("");
                        }
                        mMessagesText.append(message.getUser_id() + ": " + message.getMessage() );
                        notFirst = true;
                        mMessageDisplay.setText(mMessagesText);
                    }

                    // Objectives
                    if (dataSnapshot.getRef().getParent().toString().equals
                            (dataSnapshot.getRef().getParent().getRoot().toString() + "/objectives")) {
                        Objectives objective = dataSnapshot.getValue(Objectives.class);
                        if (objective.getTitle() != null) {
                            mObjectivesContentResolver.insert(mContentResolverUri,
                                    mDatabaseIntermediate.transferBackObjectivesToCv(objective));
                            mObjectivesCursor = mObjectivesContentResolver.query
                                    (mContentResolverUri, null, null, null, null, null);
                            mObjectivesAdapter.newObjective = false;
                            mObjectivesAdapter.updateCursor(mObjectivesCursor);
                            mObjectivesAdapter.notifyDataSetChanged();
                            if (mGoogleMap != null) {
                                loadObjectiveMarkers();
                            }
                        }
                    }

                    // Users Data
                    if (dataSnapshot.getRef().getParent().toString().equals
                            (dataSnapshot.getRef().getParent().getRoot().toString() + "/users")) {
                        Users user = dataSnapshot.getValue(Users.class);
                        int iconID;
                        String title;
                        if (user.getUser_id().equals(mUsername)) {
                            return;
                        } else {
                            iconID = R.drawable.ic_yellow_android;
                            title = user.getUser_id();
                        }
                        LatLng position = new LatLng( user.getLatitude(), user.getLongitude());
                        userLocation = mGoogleMap.addMarker(new MarkerOptions()
                                .position(position)
                                .icon(BitmapDescriptorFactory.fromBitmap(new HelperClass().
                                        _getBitmap(iconID, LiveMapActivity.this)))
                                .title(title));
                    }
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            mMessagesDatabaseReferences.addChildEventListener(mChildEventListener);
            mObjectivesDatabaseReferences.addChildEventListener(mChildEventListener);
            mUserLocationsDatabaseReferences.addChildEventListener(mChildEventListener);
        }
    }

    private void jobDispacherWidgetUpdate() {

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(LiveMapActivity.this));

        //Scheduling a simple job
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class) // the JobService that will be called
                .setTag("unique-tag")        // uniquely identifies the job
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow(0, 60))
                .build();

        dispatcher.mustSchedule(myJob);
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReferences.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(LiveMapActivity.this, "API Client Connection Successful!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(LiveMapActivity.this, "API Client Connection Suspended!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(LiveMapActivity.this, "API Client Connection Failed!", Toast.LENGTH_SHORT).show();
    }

    public void changeSizeAnimation(ViewGroup rView, int GForce, int newSize) {
        rView.getParent();
        ViewGroup.LayoutParams layoutParams = rView.getLayoutParams();
        ResizeAnimation animation = new ResizeAnimation(rView);
        animation.setDuration(GForce);
        animation.setParams(layoutParams.height, newSize);
        rView.startAnimation(animation);
    }

    @Override
    public void onClick(int VIEW_ID, ContentValues cv, int position) {
        if (VIEW_ID == R.id.add_this_objective_button) {
            newObjectiveData = cv;
            requestObjectiveLocation();
        } else if (VIEW_ID == R.id.cancel_this_objective_button ) {
            mObjectivesAdapter.newObjective = false;
            mObjectivesAdapter.notifyDataSetChanged();
        } else if (VIEW_ID == R.id.recycler_view_delete_button) {
            mObjectivesCursor.moveToPosition(position);
            String id = mObjectivesCursor.getString(mObjectivesCursor.getColumnIndex
                    (ObjectivesContract.ObjectivesEntry._ID));
            String s = mObjectivesCursor.getString(mObjectivesCursor.getColumnIndex
                    (ObjectivesContract.ObjectivesEntry.OBJECTIVES_TITLE));
            final ContentValues saveDataInCase = new ContentValues();
            saveDataInCase.put(OBJECTIVES_TITLE, s);
            saveDataInCase.put(OBJECTIVES_DESCRIPTION, mObjectivesCursor.getString(mObjectivesCursor.getColumnIndex
                    (ObjectivesContract.ObjectivesEntry.OBJECTIVES_DESCRIPTION)));
            saveDataInCase.put(OBJECTIVES_LAT, mObjectivesCursor.getString(mObjectivesCursor.getColumnIndex
                    (ObjectivesContract.ObjectivesEntry.OBJECTIVES_LAT)));
            saveDataInCase.put(OBJECTIVES_LNG, mObjectivesCursor.getString(mObjectivesCursor.getColumnIndex
                    (ObjectivesContract.ObjectivesEntry.OBJECTIVES_LNG)));
            mObjectivesContentResolver.delete
                    (ObjectivesContract.ObjectivesEntry.CONTENT_URI,
                            ObjectivesContract.ObjectivesEntry._ID + " = " + id, null);
            mObjectivesCursor = mObjectivesContentResolver.query
                    (ObjectivesContract.ObjectivesEntry.CONTENT_URI, null, null, null, null, null);
            mObjectivesAdapter.updateCursor(mObjectivesCursor);
            mObjectivesAdapter.notifyDataSetChanged();
            Snackbar.make( theWholeFrame, "deleting " + s, Snackbar.LENGTH_LONG).setAction
                    ("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mObjectivesContentResolver.insert
                            (ObjectivesContract.ObjectivesEntry.CONTENT_URI, saveDataInCase);
                    mObjectivesCursor = mObjectivesContentResolver.query
                            (ObjectivesContract.ObjectivesEntry.CONTENT_URI, null, null, null, null, null);
                    mObjectivesAdapter.updateCursor(mObjectivesCursor);
                    mObjectivesAdapter.notifyDataSetChanged();
                }
            }).setActionTextColor(Color.parseColor("#D50000")).show();
        }
    }

    LatLng selectedObjectivePlace;
    ContentValues newObjectiveData;
    private boolean requestObjectiveLocation() {

        mMessageDisplay.setText(R.string.map_choose_location);
        mMessageEditText.setVisibility(View.GONE);
        mSendButton.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.VISIBLE);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessageDisplay.setText(mMessagesText);
                mMessageEditText.setVisibility(View.VISIBLE);
                mSendButton.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.GONE);
                mObjectivesAdapter.newObjective = false;
                mObjectivesAdapter.notifyDataSetChanged();
                mCancelButton.setOnClickListener(null);
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (!mObjectivesAdapter.newObjective) return;
                selectedObjectivePlace = latLng;
                newObjectiveData.put(ObjectivesContract.ObjectivesEntry.OBJECTIVES_LAT,
                        selectedObjectivePlace.latitude);
                newObjectiveData.put(ObjectivesContract.ObjectivesEntry.OBJECTIVES_LNG,
                        selectedObjectivePlace.longitude);
//                mObjectivesContentResolver.insert
//                        (ObjectivesContract.ObjectivesEntry.CONTENT_URI, newObjectiveData);
//                mObjectivesCursor = mObjectivesContentResolver.query
//                        (ObjectivesContract.ObjectivesEntry.CONTENT_URI, null, null, null, null, null);

                // TODO Server side new objective handler
                mDatabaseIntermediate.uploadObjective(newObjectiveData, mUsername, mObjectivesDatabaseReferences);
                mMessageDisplay.setText(mMessagesText);
                mMessageEditText.setVisibility(View.VISIBLE);
                mSendButton.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.GONE);
                mCancelButton.setOnClickListener(null);
                mObjectivesAdapter.newObjective = false;
                //mObjectivesAdapter.updateCursor(mObjectivesCursor);
                //mObjectivesAdapter.notifyDataSetChanged();
                //loadObjectiveMarkers();
            }
        });

        Toast.makeText(LiveMapActivity.this, mMessagesText.toString(),
                Toast.LENGTH_LONG).show();

        return true;
    }
}
