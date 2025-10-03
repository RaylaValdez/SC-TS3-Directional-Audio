package com.teamspeak.ts3sdkclient;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.teamspeak.common.AudioDevices;
import com.teamspeak.ts3sdkclient.connection.ConnectionHandler;
import com.teamspeak.ts3sdkclient.connection.ConnectionParams;
import com.teamspeak.ts3sdkclient.eventsystem.Callbacks;
import com.teamspeak.ts3sdkclient.eventsystem.IEvent;
import com.teamspeak.ts3sdkclient.eventsystem.IEventListener;
import com.teamspeak.ts3sdkclient.helper.Info;
import com.teamspeak.ts3sdkclient.helper.InfoMessage;
import com.teamspeak.ts3sdkclient.helper.MyInfoItemRecyclerViewAdapter;
import com.teamspeak.ts3sdkclient.service.ConnectionService;
import com.teamspeak.ts3sdkclient.service.ServiceUtils;
import com.teamspeak.ts3sdkclient.ts3sdk.events.ConnectStatusChange;
import com.teamspeak.ts3sdkclient.ts3sdk.events.TalkStatusChange;
import com.teamspeak.ts3sdkclient.ts3sdk.states.ConnectStatus;
import com.teamspeak.ts3sdkclient.ts3sdk.states.PublicError;
import com.teamspeak.ts3sdkclient.ts3sdk.states.TalkStatus;

import java.util.List;

import static com.teamspeak.ts3sdkclient.Constants.PRE_PROCESSOR_VALUE_AGC;
import static com.teamspeak.ts3sdkclient.Constants.PRE_PROCESSOR_VALUE_DENOISE;
import static com.teamspeak.ts3sdkclient.Constants.PRE_PROCESSOR_VALUE_VOICE_ACTIVATION_LEVEL_DB;

/**
 * TeamSpeak 3 sdk client sample
 *
 * Copyright (c) 2007-2017 TeamSpeak-Systems
 *
 * @author Anna
 * Creation date: 08.02.17
 *
 * Example Fragment to demonstrate the usage of the ts3client library methods and ts3 events
 */
public class ExampleFragment extends Fragment implements IEventListener, Info.OnInfoListener {

    private static final String PREF_SERVERADRESS = "Pref_Serveradress";
    private static final String PREF_PORT = "Pref_Port";
    private static final String PREF_NICKNAME = "Pref_Nickname";

    /**
     * This button is used to connect and disconnect to the server. It will change its text depending
     * on the current state of the server connection (e.g. show disconnect when already connected
     * to the server)
     */
    private Button mConnectButton;
    /**
     * Input for the server address
     */
    private EditText mInputServer;
    /**
     * Input for the server port
     */
    private EditText mInputPort;
    /**
     * Input for the nickname of the user
     */
    private EditText mInputNickname;
    private TextInputLayout mLayoutServer;
    private TextInputLayout mLayoutNickname;
    private MyInfoItemRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    /**
     * This imageview is showing the current talk status of the user.
     */
    private ImageView mTalkStatusImageView;
    /**
     * ImageButton to mute and unmute the microphone. This ImageButton is only enabled when connected to a
     * server.
     */
    private ImageButton mMicMuteButton;
    /**
     * Toggle to enable and disable phone speakers. This Toggle is only enabled when connected to a
     * server.
     */
    private ToggleButton mSpeakerToggle;
    /**
     * ImageButton to mute and unmute the sound (playback and capture). This ImageButton is only
     * enabled when connected to a server.
     */
    private ImageButton mSoundMuteButton;

    private boolean mServiceBound = false;
    private boolean mStartConnection = false;
    private ConnectionParams mParams;
    private ConnectionService mConnectionService;
    private ConnectionHandler mConnectionHandler;
    private List<InfoMessage> mMessages = Info.getInstance().getMessages();
    private AudioManager mAudioManager;
    private boolean mTalkStatusImageChangeable = true;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
//            if (mConnectionHandler != null) {
//                mConnectionHandler.printConnectionInfo();
//            }
            timerHandler.postDelayed(this, 5000);
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Info.getInstance().addMessage("Service Connected", Color.parseColor("#DC143C"));
            mConnectionService = ((ConnectionService.LocalBinder) iBinder).getService();
            mConnectionHandler = mConnectionService.getConnectionHandler();
            /*
             * now that we bound to the the service, we can call the TS3ConnectionHandler to
             * connect to the server
             */
            if (mStartConnection) {
                mStartConnection = false;
                if (mConnectionHandler != null) {
                    int error = mConnectionHandler.startConnection(mParams);
                    if (error != PublicError.ERROR_ok) {
                        Info.getInstance().addMessage(PublicError.getPublicErrorString(error), Color.parseColor("#DC143C"));
                        if (getActivity() != null) {
                            ServiceUtils.unBindService(getActivity(),this);
                            ServiceUtils.stopService(getActivity());
                        }
                    }
                }
            }

            // update ui depending on connection status
            if (mConnectionHandler != null) {
                updateButtonStates(mConnectionHandler.getConnectionStatus());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Info.getInstance().addMessage("ConnectionService Disconnected.", Color.parseColor("#DC143C"));
            setStatesForUnbound();
        }
    };

    public static ExampleFragment newInstance(String identity, boolean isEmulator)
    {
        ExampleFragment fragment = new ExampleFragment();
        final Bundle args = new Bundle(1);
        args.putString("ident", identity);
        args.putBoolean("emulator", isEmulator);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() != null) {
            mAudioManager = (AudioManager) getActivity().getSystemService(Service.AUDIO_SERVICE);
            AudioDevices audioDevices = new AudioDevices(getActivity(), mAudioManager);
            audioDevices.start();

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_example, container, false);
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        // initialize UI
        Button clearButton = view.findViewById(R.id.button_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clears the log
                mMessages.clear();
                mAdapter.notifyDataSetChanged();
            }
        });

        mConnectButton = view.findViewById(R.id.button_connect);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // connects to or disconnects from the server
                if (mConnectionHandler != null
                        && mConnectionHandler.getConnectionStatus() != ConnectStatus.STATUS_DISCONNECTED) {
                    onClickDisconnect();
                } else onClickConnect();
            }
        });

        mTalkStatusImageView = view.findViewById(R.id.imageview_is_talking);

        mMicMuteButton = view.findViewById(R.id.button_mic_mute);
        mMicMuteButton.setEnabled(false);
        mMicMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  mute/unmute the microphone
                if (mConnectionHandler != null) {
                    if (!mConnectionHandler.isCaptureMuted()) {
                        mConnectionHandler.muteCaptureDevice();
                        mMicMuteButton.setImageResource(R.drawable.ic_bright_input_muted);
                    } else {
                        mConnectionHandler.unmuteCaptureDevice();
                        mMicMuteButton.setImageResource(R.drawable.ic_bright_capture);
                        // reapply settings, after capture devices is unmuted
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        boolean agcValue = prefs.getBoolean(SettingsActivity.PREF_AGC, SettingsActivity.DEFAULT_AGC);
                        boolean denoiseValue = prefs.getBoolean(SettingsActivity.PREF_DENOISE, SettingsActivity.DEFAULT_DENOISE);
                        int vadValue = prefs.getInt(SettingsActivity.PREF_VAD, SettingsActivity.DEFAULT_VAD);
                        mConnectionHandler.setPreProcessorConfigValue(
                                PRE_PROCESSOR_VALUE_DENOISE, String.valueOf(denoiseValue));
                        mConnectionHandler.setPreProcessorConfigValue(
                                PRE_PROCESSOR_VALUE_AGC, String.valueOf(agcValue));
                        mConnectionHandler.setPreProcessorConfigValue(
                                PRE_PROCESSOR_VALUE_VOICE_ACTIVATION_LEVEL_DB, String.valueOf(vadValue));
                    }
                    // show appropriate talk status image
                    updateTalkStatusImage();
                }
            }
        });

        mSpeakerToggle = view.findViewById(R.id.toggle_sepaker);
        mSpeakerToggle.setEnabled(false);
        mSpeakerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // use phone speaker instead of earphone speaker
                if (mAudioManager != null) {
                    if (mAudioManager.isSpeakerphoneOn()) {
                        mAudioManager.setSpeakerphoneOn(false);
                        mSpeakerToggle.setChecked(false);
                    } else {
                        mAudioManager.setSpeakerphoneOn(true);
                        mSpeakerToggle.setChecked(true);
                    }
                }
            }
        });

        mSoundMuteButton = view.findViewById(R.id.button_sound_mute);
        mSoundMuteButton.setEnabled(false);
        mSoundMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mute/unmute the client (capture and playback)
                if (mConnectionHandler != null) {
                    if (mConnectionHandler.isClientMuted()) {
                        mConnectionHandler.unmuteClient();
                        mSoundMuteButton.setImageResource(R.drawable.ic_bright_volume);
                    } else {
                        mConnectionHandler.muteClient();
                        mSoundMuteButton.setImageResource(R.drawable.ic_bright_output_muted);
                    }
                    // show appropriate talk status image
                    updateTalkStatusImage();
                }
            }
        });

        mInputServer = view.findViewById(R.id.input_server);
        mInputPort = view.findViewById(R.id.input_port);
        mInputNickname = view.findViewById(R.id.input_nickname);
        mLayoutServer = view.findViewById(R.id.input_layout_server);
        mLayoutNickname = view.findViewById(R.id.input_layout_nickname);

        mRecyclerView = view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mAdapter = new MyInfoItemRecyclerViewAdapter(mMessages, getContext());
        mRecyclerView.setAdapter(mAdapter);

        // Restore input from preferences if existing
        String serverAddress = PreferenceManager.getDefaultSharedPreferences(
                this.getActivity()).getString(PREF_SERVERADRESS, null);
        if (serverAddress != null && !serverAddress.isEmpty()) mInputServer.setText(serverAddress);
        int port = PreferenceManager.getDefaultSharedPreferences(
                this.getActivity()).getInt(PREF_PORT, -1);
        if (port != -1) mInputPort.setText(String.valueOf(port));
        String nickname = PreferenceManager.getDefaultSharedPreferences(
                this.getActivity()).getString(PREF_NICKNAME, null);
        if (nickname != null && !nickname.isEmpty()) mInputNickname.setText(nickname);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // bind to TS3ConnectionService if the service is running
        if (getActivity() != null && ServiceUtils.isServiceRunning(getActivity())) {
            mServiceBound = ServiceUtils.bindService(getActivity(), mServiceConnection);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // register callbacks to receive TS3Events
        Callbacks.getInstance().registerCallbacks(this);
        // set listener to log messages
        Info.getInstance().setOnInfoListener(this);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        // unregister callbacks
        Callbacks.getInstance().unregisterCallbacks(this);
        Info.getInstance().setOnInfoListener(null);
        super.onPause();
    }

    @Override
    public void onStop() {
        // unbind TS3ConnectionService if the service was bound before
        if (getActivity() != null && mServiceBound) {
            ServiceUtils.unBindService(getActivity(), mServiceConnection);
            setStatesForUnbound();
        }
        super.onStop();
    }

    /**
     * Helper method to retrieve the entered text of an {@link EditText} or the hint.
     * @param editText the {@link EditText} to retrieve the text or hint from
     * @return the text or hint of the passed {@link EditText}
     */
    static CharSequence getTextOrHint(EditText editText) {
        return(TextUtils.isEmpty(editText.getText()) ? editText.getHint() : editText.getText());
    }

    /**
     * Start the {@link ConnectionService} and bind to service
     */
    public void onClickConnect() {
        if (validateInset()) {
            String identity = "";
            if (getArguments() != null) identity = getArguments().getString("ident");
            String server = getTextOrHint(mInputServer).toString();
            String nickname = mInputNickname.getText().toString();
            if (nickname.isEmpty()) {
                // use a default username if none is entered
                nickname = "TeamSpeakUser";
            }
            int port = mInputPort.getText().length() > 0 ?
                    Integer.parseInt(mInputPort.getText().toString()) : Constants.TS3_DEFAULT_SERVERPORT;

            // bind to service if insets are valid
            if (identity != null) {
                mParams = new ConnectionParams(identity, server, port, nickname, "secret");
                mStartConnection = true;
                if (getActivity() != null && !ServiceUtils.isServiceRunning(getActivity())) {
                    ServiceUtils.startService(getActivity());
                }
                if (!mServiceBound) {
                    mServiceBound = ServiceUtils.bindService(getActivity(), mServiceConnection);
                }
                // remember last input
                PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit()
                        .putString(PREF_SERVERADRESS, server)
                        .putInt(PREF_PORT, port)
                        .putString(PREF_NICKNAME, nickname)
                        .apply();
            }
        }
    }

    /**
     * tell the {@link ConnectionHandler} to disconnect from server
     */
    public void onClickDisconnect() {
        if (mConnectionService.getConnectionHandler() != null)
            mConnectionService.getConnectionHandler().disconnect();
    }

    /**
     * Check for empty insets
     *
     * @return false if serverip or nickname is empty
     */
    private boolean validateInset() {
        boolean serverSet = mInputServer.getText() != null && mInputServer.getText().length() != 0;
        boolean nicknameSet = mInputNickname.getText() != null
                && mInputNickname.getText().length() != 0;
        setErrorForTextInputLayout(mLayoutServer, !serverSet, getString(R.string.input_required));
        setErrorForTextInputLayout(mLayoutNickname, !nicknameSet, getString(R.string.input_required));
        return serverSet && nicknameSet;
    }

    /**
     * Updates the buttonstate depending on the current connection status
     *
     * @param connectionStatus - the current state of the connection
     */
    private void updateButtonStates(@ConnectStatus int connectionStatus) {
        switch (connectionStatus) {
            case ConnectStatus.STATUS_CONNECTING:
                mConnectButton.setEnabled(false);
                mConnectButton.setText(R.string.button_server_connecting);
                mMicMuteButton.setEnabled(false);
                mSpeakerToggle.setEnabled(false);
                mSoundMuteButton.setEnabled(false);
                break;
            case ConnectStatus.STATUS_CONNECTED:
                mConnectButton.setEnabled(true);
                mConnectButton.setText(R.string.button_server_disconnect);
                mMicMuteButton.setEnabled(true);
                mSpeakerToggle.setEnabled(true);
                mSoundMuteButton.setEnabled(true);
                // allow talk status image changes when connected
                mTalkStatusImageChangeable = true;
                break;
            case ConnectStatus.STATUS_CONNECTION_ESTABLISHING:
                mConnectButton.setEnabled(true);
                mConnectButton.setText(R.string.button_server_disconnect);
                mMicMuteButton.setEnabled(true);
                mSpeakerToggle.setEnabled(true);
                mSoundMuteButton.setEnabled(true);
                break;
            case ConnectStatus.STATUS_CONNECTION_ESTABLISHED:
                mConnectButton.setEnabled(true);
                mConnectButton.setText(R.string.button_server_disconnect);
                mMicMuteButton.setEnabled(true);
                mSpeakerToggle.setEnabled(true);
                mSoundMuteButton.setEnabled(true);
                updateToggleAndImageStates();
                break;
            case ConnectStatus.STATUS_DISCONNECTED:
                mConnectButton.setEnabled(true);
                mConnectButton.setText(R.string.button_server_connect);
                mMicMuteButton.setEnabled(false);
                mSpeakerToggle.setEnabled(false);
                mSoundMuteButton.setEnabled(false);
                // do not allow talk status image changes when disconnected
                mTalkStatusImageChangeable = false;
                updateToggleAndImageStates();
                break;
        }
    }

    /**
     * This method will handle the correct states for the toggles and imagebuttons
     */
    private void updateToggleAndImageStates() {
        if (mConnectionHandler != null) {
            // check if capture is muted
            if (!mConnectionHandler.isCaptureMuted())
                mMicMuteButton.setImageResource(R.drawable.ic_bright_capture);
            else mMicMuteButton.setImageResource(R.drawable.ic_bright_input_muted);

            // check if client is muted
            if (mConnectionHandler.isClientMuted())
                mSoundMuteButton.setImageResource(R.drawable.ic_bright_output_muted);
            else mSoundMuteButton.setImageResource(R.drawable.ic_bright_volume);
        }

        // update talk status image
        updateTalkStatusImage();

        //  check if phone speaker are on
        if (mAudioManager != null) {
            if (mAudioManager.isSpeakerphoneOn()) mSpeakerToggle.setChecked(true);
            else mSpeakerToggle.setChecked(false);
        }
    }

    /**
     * This method will check if the sound or capture is muted and will set the appropriate image
     * to the mTalkStatusImageView
     */
    private void updateTalkStatusImage() {
        if (mConnectionHandler != null) {
            if (mConnectionHandler.isClientMuted()) {
                mTalkStatusImageChangeable = false;
                mTalkStatusImageView.setImageResource(R.drawable.ic_bright_output_muted);
            } else if(mConnectionHandler.isCaptureMuted()) {
                mTalkStatusImageChangeable = false;
                mTalkStatusImageView.setImageResource(R.drawable.ic_bright_input_muted);
            } else {
                mTalkStatusImageChangeable = true;
                mTalkStatusImageView.setImageResource(R.drawable.ic_bright_player_off);
            }
        }
    }

    private void setStatesForUnbound() {
        mServiceBound = false;
        mConnectionService = null;
        mConnectionHandler = null;
    }

    private void setErrorForTextInputLayout(TextInputLayout layout, boolean showError, String messages) {
        layout.setErrorEnabled(showError);
        layout.setError(showError ? messages : null);
    }

    /**
     * With the {@link IEventListener} we can receive callbacks to update the UI
     *
     * @param event - the incoming event
     */
    @Override
    public void onTS3Event(@NonNull final IEvent event) {
        if (event instanceof ConnectStatusChange) {
            // incoming ConnectStatusChange event
            final ConnectStatusChange statusChangeEvent = (ConnectStatusChange) event;
            @ConnectStatus
            final int connectStatus = statusChangeEvent.getNewStatus();
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateButtonStates(connectStatus);
                        if (connectStatus == ConnectStatus.STATUS_CONNECTION_ESTABLISHED) {
                            timerHandler.postDelayed(timerRunnable, 5000);
                        } else if (connectStatus == ConnectStatus.STATUS_DISCONNECTED) {
                            timerHandler.removeCallbacks(timerRunnable);
                        }
                    }
                });
                if (connectStatus == ConnectStatus.STATUS_DISCONNECTED) {
                    ServiceUtils.unBindService(getActivity(), mServiceConnection);
                    mServiceBound = false;
                }
            }
        } else if (event instanceof TalkStatusChange) {
            // return if connection handler is not available
            if(mConnectionHandler == null) return;

            //  change talk status icon depending on TalkStatusChange event
            TalkStatusChange talkStatusChangeEvent = (TalkStatusChange) event;
            switch (talkStatusChangeEvent.getStatus()) {
                case TalkStatus.STATUS_TALKING: {
                    // user started talking, show the player_on icon
                    if (talkStatusChangeEvent.getClientID() == mConnectionHandler.getClientId()) {
                        mTalkStatusImageView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mTalkStatusImageChangeable)
                                mTalkStatusImageView.setImageDrawable(
                                        getResources().getDrawable(R.drawable.ic_bright_player_on));
                            }
                        });
                    }
                    break;
                }
                case TalkStatus.STATUS_NOT_TALKING: {
                    // user stopped talking, show the player_off icon
                    if (talkStatusChangeEvent.getClientID() == mConnectionHandler.getClientId()) {
                        mTalkStatusImageView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mTalkStatusImageChangeable)
                                mTalkStatusImageView.setImageDrawable(
                                        getResources().getDrawable(R.drawable.ic_bright_player_off));
                            }
                        });
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onUpdateGUI() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.smoothScrollToPosition(0);
                }
            });
        }
    }
}
