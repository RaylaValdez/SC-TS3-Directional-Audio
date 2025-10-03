package com.teamspeak.ts3sdkclient;

/**
 * TeamSpeak SDK
 *
 * Copyright (c) 2007-2020 TeamSpeak-Systems
 *
 * Collected constants of general utility.
 */
public final class Constants {

    public static final int TS3_DEFAULT_SERVERPORT = 9987;

    /** Preprocessor constants **/

    // Enable or disable noise suppression. Value can be “true” or “false”. Enabled by default.
    public static final String PRE_PROCESSOR_VALUE_DENOISE = "denoise";

    // Set the level of noise suppression. Value can be "0" (low) to "3" (very high)
    public static final String PRE_PROCESSOR_VALUE_DENOISER_LEVEL = "denoiser_level";

    // Enable or disable Voice Activity Detection. Value can be “true” or “false”. Enabled by default.
    public static final String PRE_PROCESSOR_VALUE_VAD = "vad";

    // Set the mode of Voice Activity Detection. Value can be
    // "0": Likelihood,
    // "1": Power,
    // "2": LikelihoodAndPower,
    // "3": LikelihoodAndPowerFuzzy
    public static final String PRE_PROCESSOR_VALUE_VAD_MODE = "vad_mode";

    // Enable or disable Typing Suppression. Value can be “true” or “false”. Disabled by default.
    public static final String PRE_PROCESSOR_VALUE_TYPING_SUPPRESSION = "typing_suppression";

    // Voice Activity Detection level in decibel. Numeric value converted to string.
    // A high voice activation level means you have to speak louder into the microphone in order to start transmitting.
    // Reasonable values range from -50 to 50. Default is 0.
    // To adjust the VAD level in your client, you can call ts3client_getPreProcessorInfoValueFloat with the identifier “decibel_last_period”
    // over a period of time to query the current voice input level.
    public static final String PRE_PROCESSOR_VALUE_VOICE_ACTIVATION_LEVEL_DB = "voiceactivation_level";
    public static final String PRE_PROCESSOR_INFO_VALUE_CURRENT_VOICE_ACTIVATION_LEVEL_DB = "decibel_last_period";

    public static final String PRE_PROCESSOR_INFO_VALUE_RMS = "rms";

    // Voice Activity Detection extrabuffer size. Numeric value converted to string.
    // Should be “0” to “8”, defaults to “2”. Lower value means faster transmission, higher value means better VAD quality but higher latency.
    public static final String PRE_PROCESSOR_VALUE_VAD_EXTRA_BUFFERSIZE = "vad_extrabuffersize";

    // Enable or disable Automatic Gain Control. Value can be “true” or “false”. Enabled by default.
    public static final String PRE_PROCESSOR_VALUE_AGC = "agc";

    // AGC level. Numeric value converted to string. Default is “16000”.
    // http://lists.xiph.org/pipermail/speex-dev/2005-June/003362.html
    public static final String PRE_PROCESSOR_VALUE_AGC_LEVEL = "agc_level";

    // AGC max gain. Numeric value converted to string. Default is “30”.
    public static final String PRE_PROCESSOR_VALUE_AGC_MAX_GAIN_DB = "agc_max_gain";

    // Enable or disable echo canceling. Value can be “true” or “false”. Disabled by default.
    public static final String PRE_PROCESSOR_VALUE_ECHO_CANCELLING = "echo_canceling";

    // PLAYBACK

    public static final String PLAYBACK_VOLUME_MODIFIER = "volume_modifier";
    public static final String PLAYBACK_ECHO_REDUCTION_DUCKING = "echo_reduction_ducking";
    public static final String PLAYBACK_VOLUME_FACTOR_WAVE = "volume_factor_wave";
    public static final String PLAYBACK_MONO_SPEAKER_DESTINATION = "mono_speaker_destination";
    public static final String PLAYBACK_AGC = "agc";
    public static final String PLAYBACK_COMFORT_NOISE_ENABLED = "comfort_noise_enabled";
    public static final String PLAYBACK_COMFORT_NOISE_VOLUME = "comfort_noise_volume_db";

    private Constants(){
        //this prevents even the native class from
        //calling this ctor as well :
        throw new AssertionError();
    }
}
