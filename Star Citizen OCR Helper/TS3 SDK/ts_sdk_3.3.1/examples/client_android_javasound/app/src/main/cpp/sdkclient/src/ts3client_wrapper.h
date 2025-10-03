/*
 * TeamSpeak 3 sdk client JNI wrapper
 *
 * Copyright (c) 2007-2020 TeamSpeak-Systems
 *
 */
#pragma once

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

void ts3client_android_initJni(void* java_vm, void* context);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_startInit
 * Signature: (Landroid/content/pm/ApplicationInfo;)I;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1startInit
        (JNIEnv *, jobject, jobject/*, jobjectArray*/);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_destroyClientLib
 * Signature: ()I;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1destroyClientLib
        (JNIEnv *, jobject);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_spawnNewServerConnectionHandler
 * Signature: ()J;
 */
JNIEXPORT jlong
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1spawnNewServerConnectionHandler(JNIEnv * env, jobject);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_destroyServerConnectionHandler
 * Signature: (J)I;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1destroyServerConnectionHandler(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_startConnection
 * Signature: (J;Ljava/lang/String;Ljava/lang/String;I;Ljava/lang/String;[Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String)I;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1startConnection(JNIEnv * env, jobject obj, jlong serverConnectionHandlerID,
                                                                           jstring identity, jstring ip, jint port, jstring nickname, jobjectArray channel,
                                                                           jstring defaultChannelPassword, jstring serverPassword);
/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_stopConnection
 * Signature: (J;Ljava/lang/String)I;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1stopConnection(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jstring msg);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_createIdentity
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1createIdentity(JNIEnv * env, jobject);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_getClientLibVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getClientLibVersion(JNIEnv * env, jobject);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_registerCustomDevice
 * Signature: (Ljava/lang/String;Ljava/lang/String;IILjava/nio/ByteBuffer;IILjava/nio/ByteBuffer;)I;
 */
JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1registerCustomDevice(JNIEnv * env, jobject obj, jstring deviceID, jstring deviceDisplayName, jint capFrequency, jint capChannels, jobject capture_byte_buffer, jint playFrequency, jint playChannels, jobject playback_byte_buffer);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_unregisterCustomDevice
 * Signature: (Ljava/lang/String;)I;
 */
JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1unregisterCustomDevice(JNIEnv * env, jobject obj, jstring deviceID);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_unregisterCustomDevice
 * Signature: (Ljava/lang/String;I)I;
 */
JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1acquireCustomPlaybackData(JNIEnv * env, jobject obj, jstring deviceID, jint samples);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_processCustomCaptureData
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1processCustomCaptureData(JNIEnv *, jobject, jstring, jint);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_openCaptureDevice
 * Signature: (J;Ljava/lang/String;Ljava/lang/String;)I;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1openCaptureDevice(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jstring modeID, jstring captureDevice);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_openPlaybackDevice
 * Signature: (J;Ljava/lang/String;Ljava/lang/String;)I;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1openPlaybackDevice(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jstring modeID, jstring captureDevice);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_closeCaptureDevice
 * Signature: (J)jint;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1closeCaptureDevice(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_closePlaybackDevice
 * Signature: (J)jint;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1closePlaybackDevice(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_activateCaptureDevice
 * Signature: (J)jint;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1activateCaptureDevice(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID);


/*
 * Class:     com_teamspeak_ts3client_jni_Ts3Jni
 * Method:    ts3client_setClientSelfVariableAsInt
 * Signature: (JII)I
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1setClientSelfVariableAsInt(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jint flag, jint value);

/*
 * Class:     com_teamspeak_ts3client_jni_Ts3Jni
 * Method:    ts3client_flushClientSelfUpdates
 * Signature: (JLjava/lang/String;)I
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1flushClientSelfUpdates(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jstring returnCode);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_setPreProcessorConfigValue
 * Signature: (J;Ljava/lang/String;Ljava/lang/String)Ljava/lang/String;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1setPreProcessorConfigValue(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jstring ident, jstring value);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_getPreProcessorConfigValue
 * Signature: (J;Ljava/lang/String)Ljava/lang/String;
 */
JNIEXPORT jstring
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getPreProcessorConfigValue(JNIEnv * env, jobject obj, jlong serverConnectionHandlerID, jstring ident);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_getPlaybackConfigValueAsFloat
 * Signature: (J;Ljava/lang/String)F;
 */
JNIEXPORT jfloat
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getPlaybackConfigValueAsFloat(JNIEnv * env, jobject obj, jlong serverConnectionHandlerID, jstring ident);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_setPlaybackConfigValue
 * Signature: (J;Ljava/lang/String;Ljava/lang/String)I;
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1setPlaybackConfigValue(JNIEnv * env, jobject obj, jlong serverConnectionHandlerID, jstring ident, jstring value);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_getClientVariableAsString
 * Signature: (JII)Ljava/lang/String;
 */
JNIEXPORT jstring
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getClientVariableAsString(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jint clientID, jint flag);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_getChannelVariableAsString
 * Signature: (JJI)Ljava/lang/String;
 */
JNIEXPORT jstring
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getChannelVariableAsString(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jlong channelID, jint flag);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_getClientID
 * Signature: (J)I
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getClientID
        (JNIEnv *, jobject, jlong);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_getConnectionStatus
 * Signature: (J)I
 */
JNIEXPORT jint
JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getConnectionStatus
        (JNIEnv *, jobject, jlong);

/*
 * Class:     Java_com_teamspeak_ts3sdkclient_ts3sdk_Native
 * Method:    ts3client_getConnectionVariableAsDouble
 * Signature: (JII)D
 */
JNIEXPORT jdouble JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getConnectionVariableAsDouble
        (JNIEnv *, jobject, jlong, jint, jint);

#ifdef __cplusplus
}
#endif
