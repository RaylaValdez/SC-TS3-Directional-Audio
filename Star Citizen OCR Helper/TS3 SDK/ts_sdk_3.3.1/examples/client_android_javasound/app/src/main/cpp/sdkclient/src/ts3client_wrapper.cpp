#include "ts3client_wrapper.h"
#include "teamspeak/clientlib.h"
#include "teamspeak/public_errors.h"

#include <android/log.h>
#include <cstdio>
#include <algorithm>
#include <utility>
#include <string>
#include <unordered_map>
#include <vector>

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "TS3 LIB",__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "TS3 LIB",__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , "TS3 LIB",__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , "TS3 LIB",__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "TS3 LIB",__VA_ARGS__)

static JavaVM *gJavaVM;
static std::pair<jobject, jmethodID> Android_Event_ConnectStatusChange;
static std::pair<jobject, jmethodID> Android_Event_NewChannel;
static std::pair<jobject, jmethodID> Android_Event_NewChannelCreated;
static std::pair<jobject, jmethodID> Android_Event_DelChannel;
static std::pair<jobject, jmethodID> Android_Event_ClientMove;
static std::pair<jobject, jmethodID> Android_Event_ClientMoveSubscription;
static std::pair<jobject, jmethodID> Android_Event_ClientMoveTimeout;
static std::pair<jobject, jmethodID> Android_Event_ClientMoveMoved;
static std::pair<jobject, jmethodID> Android_Event_TalkStatusChange;
static std::pair<jobject, jmethodID> Android_Event_ServerError;
static std::pair<jobject, jmethodID> Android_Event_UserLoggingMessage;

//static std::pair<jobject, jmethodID> byte_buffer_limit_function;

static jobject StringClass;

static std::unordered_map<std::string, std::pair<std::size_t, void*>> playByteBufferCache;
static std::unordered_map<std::string, std::pair<std::size_t, void*>> capByteBufferCache;

bool connectVM(JNIEnv *&env) {
    int status;
    bool isAttached = false;

    status = gJavaVM->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {

        JavaVMAttachArgs args = { JNI_VERSION_1_6, "sdkclient.src.teamspeak", NULL };
        status = gJavaVM->AttachCurrentThread(&env, &args);
        if (status < 0) {
            LOGE("callback_handler: failed to attach "
                         "current thread");
            return isAttached;
        }
        isAttached = true;
    }
#ifdef DEBUG_BUILD
    if(isAttached)
		LOGD("isAttached true");
	else
		LOGD("isAttached false");
#endif
    return isAttached;
}


///////////////////////////////////////////////////////////////////////////
// JNI Methods
///////////////////////////////////////////////////////////////////////////

int init(const char*/*, const std::vector<std::string>& events_to_register*/);

jstring get_native_library_dir(JNIEnv* env, jobject application_context)
{
    jclass context_class = env->GetObjectClass(application_context);
    jmethodID app_info_method_id = env->GetMethodID(context_class, "getApplicationInfo", "()Landroid/content/pm/ApplicationInfo;");
    jobject app_info_object = env->CallObjectMethod(application_context, app_info_method_id);
    jclass app_info_class = env->GetObjectClass(app_info_object);
    jfieldID native_lib_field_id = env->GetFieldID(app_info_class, "nativeLibraryDir", "Ljava/lang/String;");
    return (jstring)env->GetObjectField(app_info_object, native_lib_field_id);
}

/*namespace {
    std::vector<std::string> parseEventRegister(JNIEnv *env, jobjectArray object_array)
    {
        decltype(parseEventRegister(nullptr, {})) result;
        const auto object_array_size = env->GetArrayLength(object_array);

        for (auto i = decltype(object_array_size){0}; i < object_array_size; ++i)
        {
            const auto j_string = (jstring) (env->GetObjectArrayElement(object_array, i));
            const auto* raw_string = env->GetStringUTFChars(j_string, 0);
            result.emplace_back(raw_string);
            // Don't forget to call `ReleaseStringUTFChars` when you're done.
            env->ReleaseStringUTFChars(j_string, raw_string);
        }
        return result;
    }
}*/

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1startInit(JNIEnv *env, jobject /*obj*/, jobject application_context/*, jobjectArray events*/) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif

    ts3client_android_initJni(gJavaVM, application_context);

    //const auto events_to_register = parseEventRegister(env, events);

    jstring nativeLibPath = get_native_library_dir(env, application_context);
    const auto* native_lib_path = env->GetStringUTFChars(nativeLibPath, 0);
    LOGV("Sound backend path: %s\n", native_lib_path);
    int err = init(native_lib_path/*, events_to_register*/);
    env->ReleaseStringUTFChars(nativeLibPath, native_lib_path);
    LOGD("init() returned: %u", err);
    return err;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1destroyClientLib(JNIEnv *env, jobject obj) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;

    if ((error = ts3client_destroyClientLib()) != ERROR_ok) {
        LOGE("Failed to destroy clientlib: %d\n", error);
        return 1;
    }
    LOGD("Clientlib Closed");
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1spawnNewServerConnectionHandler(JNIEnv * env, jobject obj) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    uint64 scHandlerID;

    if ((error = ts3client_spawnNewServerConnectionHandler(0, &scHandlerID)) != ERROR_ok) {
        char* errormsg;
        if (ts3client_getErrorMessage(error, &errormsg) == ERROR_ok) {
            LOGE("Error spawning server conection handler: %s\n", errormsg);
            ts3client_freeMemory(errormsg);
        } else {
            LOGE("Error spawning server connection handler.\n");
        }
        return 1;
    }

    return (jlong)scHandlerID;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1destroyServerConnectionHandler(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    if ((error = ts3client_destroyServerConnectionHandler((uint64)serverConnectionHandlerID)) != ERROR_ok) {
        LOGE("Error destroying ServerConnectionHandler: %d\n", error);
        return 1;
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1startConnection(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jstring identity, jstring ip, jint port, jstring nickname, jobjectArray channel, jstring defaultChannelPassword, jstring serverPassword) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;

    int counter = env->GetArrayLength(channel);
    const char *dchannel[counter + 1];
    for (int i = 0; i < counter; i++) {
        jstring string = (jstring) env->GetObjectArrayElement(channel, i);
        const char *rawString = env->GetStringUTFChars(string, 0);
        dchannel[i] = rawString;
    }
    dchannel[counter] = "";

    const char* _identity = env->GetStringUTFChars(identity, 0);
    const char* _ip = env->GetStringUTFChars(ip, 0);
    const char* _nickname = env->GetStringUTFChars(nickname, 0);
    const char* _serverPassword = env->GetStringUTFChars(serverPassword, 0);
    const char* _defaultChannelPassword = env->GetStringUTFChars(defaultChannelPassword, 0);

    if ((error = ts3client_startConnection((uint64)serverConnectionHandlerID, _identity,
                                          _ip, (u_int)port, _nickname, dchannel, _defaultChannelPassword,
                                          _serverPassword)) != ERROR_ok) {
        char* errormsg;
        if(ts3client_getErrorMessage(error, &errormsg) == ERROR_ok) {
            LOGE("Failed ts3client_startConnection: %s\n", errormsg);
            ts3client_freeMemory(errormsg); /* Release dynamically allocated memory only if function succeeded */
        }
        return error;
    }

    env->ReleaseStringUTFChars(identity, _identity);
    env->ReleaseStringUTFChars(ip, _ip);
    env->ReleaseStringUTFChars(nickname, _nickname);
    env->ReleaseStringUTFChars(serverPassword, _serverPassword);
    env->ReleaseStringUTFChars(defaultChannelPassword, _defaultChannelPassword);

    for (int i = 0; i < counter; i++) {
        env->ReleaseStringUTFChars(
                (jstring) env->GetObjectArrayElement(channel, i), dchannel[i]);
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1stopConnection(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jstring msg) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    const char* _msg = env->GetStringUTFChars(msg, 0);
    if ((error = ts3client_stopConnection((uint64)serverConnectionHandlerID, _msg))
        != ERROR_ok) {
        LOGE("Error stopping connection: %d\n", error);
        return 1;
    }
    env->ReleaseStringUTFChars(msg, _msg);
    return 0;
}

JNIEXPORT jstring JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1createIdentity(JNIEnv * env, jobject obj) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    char* identity;
    jstring ret = 0;

    error = ts3client_createIdentity(&identity);
    if (error != ERROR_ok) {
        LOGE("Error creating identity: %d\n", error);
        ret = (jstring) env->NewStringUTF("ERROR");
        return ret;
    }
    ret = env->NewStringUTF(identity);
    ts3client_freeMemory(identity); /* Release string */
    return ret;
}

JNIEXPORT jstring JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getClientLibVersion(JNIEnv * env, jobject obj) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    jstring ret = 0;
    unsigned int error;
    char* version;
    error = ts3client_getClientLibVersion(&version);
    if (error != ERROR_ok) {
        LOGE("Error querying clientlib version: %d\n", error);
        return NULL;
    }
    LOGV("Client library version: %s\n", version);
    ret = env->NewStringUTF(version);
    ts3client_freeMemory(version);

    return ret;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1registerCustomDevice(
        JNIEnv * env,
        jobject obj,
        jstring deviceID,
        jstring deviceDisplayName,
        jint capFrequency,
        jint capChannels,
        jobject cap_byte_buffer,
        jint playFrequency,
        jint playChannels,
        jobject play_byte_buffer)
{
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif

    const char* _deviceID = env->GetStringUTFChars(deviceID, 0);
    const char* _deviceDisplayName = env->GetStringUTFChars(deviceDisplayName, 0);

    unsigned int error;
    //
    // Register our custom sound device
    //
    if ((error = ts3client_registerCustomDevice(_deviceID,
                                                _deviceDisplayName,
                                                capFrequency,
                                                capChannels,
                                                playFrequency,
                                                playChannels)) != ERROR_ok) {
        char *errormsg;
        if (ts3client_getErrorMessage(error, &errormsg) == ERROR_ok) {
            LOGE("Error registering custom sound device: %s\n", errormsg);
            ts3client_freeMemory(errormsg);
        } else {
            LOGE("Error registering custom sound device.\n");
        }
    }

    /*const auto cls = env->GetObjectClass(byte_buffer);
    const auto method_id = env->GetMethodID(cls, "limit", "(I)Ljava/nio/Buffer;");
    byte_buffer_limit_function = std::make_pair(env->NewGlobalRef(byte_buffer), method_id);*/

    if (error == ERROR_ok)
    {
        /*playbackFrequency = playFrequency;
        playbackChannelCount = playChannels;*/
        if (play_byte_buffer) {
            playByteBufferCache.emplace(
                    _deviceID,
                    std::make_pair<std::size_t, void*>(
                            static_cast<size_t>(env->GetDirectBufferCapacity(play_byte_buffer)),
                            env->GetDirectBufferAddress(play_byte_buffer))
            );
        }
        if (cap_byte_buffer) {
            capByteBufferCache.emplace(
                    _deviceID,
                    std::make_pair<std::size_t, void*>(
                            static_cast<size_t>(env->GetDirectBufferCapacity(cap_byte_buffer)),
                            env->GetDirectBufferAddress(cap_byte_buffer))
            );
        }
    }

    env->ReleaseStringUTFChars(deviceID, _deviceID);
    env->ReleaseStringUTFChars(deviceDisplayName, _deviceDisplayName);

    return error;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1unregisterCustomDevice(JNIEnv * env, jobject obj, jstring deviceID) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif

    const char* _deviceID = env->GetStringUTFChars(deviceID, 0);

    unsigned int error;
    LOGD("Unregistering custom sound device\n");
    //
    // Unregister our custom sound device
    //
    if ((error = ts3client_unregisterCustomDevice(_deviceID)) != ERROR_ok) {
        char* errormsg;
        if (ts3client_getErrorMessage(error, &errormsg) == ERROR_ok) {
            LOGE("Error unregistering custom sound device: %s\n", errormsg);
            ts3client_freeMemory(errormsg);
        } else {
            LOGE("Error unregistering custom sound device.\n");
        }
    }

    {
        auto it = playByteBufferCache.find(_deviceID);
        if (it != playByteBufferCache.end())
            playByteBufferCache.erase(it);
    }
    {
        auto it = capByteBufferCache.find(_deviceID);
        if (it != capByteBufferCache.end())
            capByteBufferCache.erase(it);
    }

    env->ReleaseStringUTFChars(deviceID, _deviceID);
    /*env->DeleteGlobalRef(byte_buffer_limit_function.first);
    byte_buffer_limit_function.second = 0;*/
    return error;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1acquireCustomPlaybackData(JNIEnv * env, jobject obj, jstring deviceID, jint samples)
{
    const auto* _deviceID = env->GetStringUTFChars(deviceID, 0);
    auto it = playByteBufferCache.find(_deviceID);
    if (it == playByteBufferCache.end())
        return ERROR_parameter_invalid;

    auto* byte_buffer = (*it).second.second;
    const auto& byte_buffer_size = (*it).second.first;
    if (samples * sizeof(short) > byte_buffer_size)
        return ERROR_parameter_invalid_count;

    const auto error = ts3client_acquireCustomPlaybackData(_deviceID, static_cast<short*>(byte_buffer), samples);

    env->ReleaseStringUTFChars(deviceID, _deviceID);

    return error;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1processCustomCaptureData(JNIEnv* env, jobject obj, jstring deviceID, jint samples)
{
#ifdef DEBUG_BUILD_AUDIO
    LOGD(__FUNCTION__);
#endif
    const auto* _deviceID = env->GetStringUTFChars(deviceID, 0);
    auto it = capByteBufferCache.find(_deviceID);
    if (it == capByteBufferCache.end())
        return ERROR_parameter_invalid;

    auto* byte_buffer = (*it).second.second;
    const auto& byte_buffer_size = (*it).second.first;
    if (samples * sizeof(short) > byte_buffer_size)
        return ERROR_parameter_invalid_count;

    const auto error = ts3client_processCustomCaptureData(_deviceID, static_cast<short*>(byte_buffer), samples);
    if (error != ERROR_ok)
    {
        char* errormsg;
        if (ts3client_getErrorMessage(error, &errormsg) == ERROR_ok)
        {
            LOGE("Failed to process capture data: %s\n", errormsg);
            ts3client_freeMemory(errormsg);
        }
    }

    env->ReleaseStringUTFChars(deviceID, _deviceID);

    return error;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1openCaptureDevice(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jstring modeID, jstring captureDevice)
{
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    const char* _modeID = env->GetStringUTFChars(modeID, 0);
    const char* _captureDevice = env->GetStringUTFChars(captureDevice, 0);

    unsigned int error;

    if ((error = ts3client_openCaptureDevice((uint64)serverConnectionHandlerID, _modeID, _captureDevice)) != ERROR_ok)
    {
        char* errormsg;
        if (ts3client_getErrorMessage(error, &errormsg) == ERROR_ok)
        {
            LOGE("Error opening capture device: %s\n", errormsg);
            ts3client_freeMemory(errormsg);
        }
        else
            LOGE("Error opening capture device.\n");
    }
    env->ReleaseStringUTFChars(modeID, _modeID);
    env->ReleaseStringUTFChars(captureDevice, _captureDevice);

    // TODO return proper error (esp. on failure)
    return 0;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1openPlaybackDevice(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jstring modeID, jstring captureDevice)
{
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    const char* _modeID = env->GetStringUTFChars(modeID, 0);
    const char* _captureDevice = env->GetStringUTFChars(captureDevice, 0);
    unsigned int error;

    if ((error = ts3client_openPlaybackDevice((uint64)serverConnectionHandlerID, _modeID, _captureDevice)) != ERROR_ok)
    {
        char* errormsg;
        if (ts3client_getErrorMessage(error, &errormsg) == ERROR_ok)
        {
            LOGE("Error opening playback device: %s\n", errormsg);
            ts3client_freeMemory(errormsg);
        }
        else
            LOGE("Error opening playback device.\n");
    }

    env->ReleaseStringUTFChars(modeID, _modeID);
    env->ReleaseStringUTFChars(captureDevice, _captureDevice);

    // TODO return proper error (esp. on failure)
    return 0;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1closeCaptureDevice(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID)
{
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;

    if ((error = ts3client_closeCaptureDevice((uint64)serverConnectionHandlerID)) != ERROR_ok)
    {
        char* errormsg;
        if (ts3client_getErrorMessage(error, &errormsg) == ERROR_ok)
        {
            LOGE("Error closing capture device: %s\n", errormsg);
            ts3client_freeMemory(errormsg);
        }
        else
            LOGE("Error closing capture device.\n");
    }

    // TODO return proper error (esp. on failure)
    return 0;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1closePlaybackDevice(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID)
{
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif

    unsigned int error;

    if ((error = ts3client_closePlaybackDevice((uint64)serverConnectionHandlerID)) != ERROR_ok)
    {
        char* errormsg;
        if (ts3client_getErrorMessage(error, &errormsg) == ERROR_ok)
        {
            LOGE("Error closing playback device: %s\n", errormsg);
            ts3client_freeMemory(errormsg);
        }
        else
            LOGE("Error closing playback device.\n");
    }

    // TODO return proper error (esp. on failure)
    return 0;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1activateCaptureDevice(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID)
{
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;

    if ((error = ts3client_activateCaptureDevice((uint64)serverConnectionHandlerID)) != ERROR_ok)
    {
        char* errormsg;
        if (ts3client_getErrorMessage(error, &errormsg) == ERROR_ok)
        {
            LOGE("Error activating capture device: %s\n", errormsg);
            ts3client_freeMemory(errormsg);
        }
        else
            LOGE("Error activating capture device.\n");
    }

    // TODO return proper error (esp. on failure)
    return 0;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1setClientSelfVariableAsInt(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jint flag, jint value) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    //__android_log_print(ANDROID_LOG_INFO, "INFO", "%d:%d ",flag,value);
    if ((error = ts3client_setClientSelfVariableAsInt((uint64) serverConnectionHandlerID,
                                                      flag, value)) != ERROR_ok) {
        LOGE("Error setting client variable\n");
        return error;
    }
    return error;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1flushClientSelfUpdates(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jstring returnCode) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    const char* _returnCode = env->GetStringUTFChars(returnCode, 0);
    error = ts3client_flushClientSelfUpdates(serverConnectionHandlerID,
                                             _returnCode);
    if (error != ERROR_ok && error != ERROR_ok_no_update) {
        LOGE("Error flushing client updates %d\n", error);
        return 1;
    }
    env->ReleaseStringUTFChars(returnCode, _returnCode);
    return 0;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1setPreProcessorConfigValue(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jstring ident, jstring value) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;

    const char* _ident = env->GetStringUTFChars(ident, 0);
    const char* _value = env->GetStringUTFChars(value, 0);

    if (((error = ts3client_setPreProcessorConfigValue((uint64)serverConnectionHandlerID, _ident, _value)) != ERROR_ok)) {
        LOGE("Failed ts3client_setPreProcessorConfigValue: %d\n", error);
    }
    env->ReleaseStringUTFChars(ident, _ident);
    env->ReleaseStringUTFChars(value, _value);
    return error;
}

JNIEXPORT jstring JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getPreProcessorConfigValue(JNIEnv * env, jobject obj, jlong serverConnectionHandlerID, jstring ident) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    jstring ret = 0;
    const char* _ident = env->GetStringUTFChars(ident, 0);
    char *result;
    if (((error = ts3client_getPreProcessorConfigValue((uint64)serverConnectionHandlerID, _ident, &result)) != ERROR_ok)) {
        LOGE("Failed ts3client_setPreProcessorConfigValue: %d\n", error);
    }
    env->ReleaseStringUTFChars(ident, _ident);
    ret = env->NewStringUTF(result);
    ts3client_freeMemory(result); /* Release string */
    return ret;
}

JNIEXPORT jfloat JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getPlaybackConfigValueAsFloat(JNIEnv * env, jobject obj, jlong serverConnectionHandlerID, jstring ident) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    float value;
    const char* _ident = env->GetStringUTFChars(ident, 0);
    if ((error = ts3client_getPlaybackConfigValueAsFloat((uint64)serverConnectionHandlerID, _ident, &value)) != ERROR_ok) {
        LOGE("Failed ts3client_getPlaybackConfigValueAsFloat: %d\n", error);
        return 0;
    }
    env->ReleaseStringUTFChars(ident, _ident);
    return value;

}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1setPlaybackConfigValue(JNIEnv * env, jobject obj, jlong serverConnectionHandlerID, jstring ident, jstring value) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    const char* _ident = env->GetStringUTFChars(ident, 0);
    const char* _value = env->GetStringUTFChars(value, 0);

    if ((error = ts3client_setPlaybackConfigValue((uint64)serverConnectionHandlerID,
                                                  _ident, _value)) != ERROR_ok) {
        LOGE("Failed ts3client_setPlaybackConfigValue: %d\n", error);
    }

    env->ReleaseStringUTFChars(ident, _ident);
    env->ReleaseStringUTFChars(value, _value);
    return error;
}

JNIEXPORT jstring JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getClientVariableAsString(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jint clientID, jint flag) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    char* result;
    jstring ret = 0;
    if ((error = ts3client_getClientVariableAsString((uint64)serverConnectionHandlerID,
                                                     clientID, flag, &result)) != ERROR_ok) {
        char* errormsg;
        if(ts3client_getErrorMessage(error, &errormsg) == ERROR_ok) {
            LOGE("Failed ts3client_getClientVariableAsString: %s\n", errormsg);
            ts3client_freeMemory(errormsg); /* Release dynamically allocated memory only if function succeeded */
        }
    } else {
        ret = env->NewStringUTF(result);
        ts3client_freeMemory(result);
    }
    return ret;
}

JNIEXPORT jstring JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getChannelVariableAsString(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jlong channelID, jint flag) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    char* result;
    jstring ret = 0;
    if ((error = ts3client_getChannelVariableAsString((uint64)serverConnectionHandlerID,(uint64)channelID, flag, &result)) != ERROR_ok) {
        char* errormsg;
        if(ts3client_getErrorMessage(error, &errormsg) == ERROR_ok) {
            LOGE("Failed ts3client_getChannelVariableAsString: %s\n", errormsg);
            ts3client_freeMemory(errormsg); /* Release dynamically allocated memory only if function succeeded */
        }
        ret = (jstring) env->NewStringUTF("ERROR");
    } else {
        ret = env->NewStringUTF(result);
        ts3client_freeMemory(result);
    }
    return ret;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getClientID(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    anyID result;
    if ((error = ts3client_getClientID(serverConnectionHandlerID, &result)) != ERROR_ok) {
        LOGE("Failed to get own ID: %d\n", error);
    }
    return (int) result;
}

JNIEXPORT jint JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getConnectionStatus(JNIEnv *, jobject, jlong connection_id)
{
    int connection_status;
    const auto error = ts3client_getConnectionStatus(connection_id, &connection_status);
    if (error != ERROR_ok)
    {
        LOGE("Failed ts3client_getConnectionStatus: %d\n", error);
        return -1;
    }
    return connection_status;
}

JNIEXPORT jdouble JNICALL Java_com_teamspeak_ts3sdkclient_ts3sdk_Native_ts3client_1getConnectionVariableAsDouble(JNIEnv *env, jobject obj, jlong serverConnectionHandlerID, jint clientID, jint flag) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;
    double result;
    if ((error = ts3client_getConnectionVariableAsDouble(
            serverConnectionHandlerID, clientID, flag, &result)) != ERROR_ok) {
        LOGE("Failed ts3client_getConnectionVariableAsDouble: %d\n", error);
        return -1;
    }
    return result;
}

///////////////////////////////////////////////////////////////////////////
// Events
///////////////////////////////////////////////////////////////////////////

void onConnectStatusChangeEvent(uint64 serverConnectionHandlerID, int newStatus, unsigned int errorNumber) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    JNIEnv *env;
    bool isAttached = connectVM(env);
    LOGI("ConnectStatusChange");
    const auto& cache = Android_Event_ConnectStatusChange;
    jclass interfaceClass = env->GetObjectClass(cache.first);
    jmethodID method = env->GetMethodID(interfaceClass, "<init>", "(JII)V");
    const auto new_object = env->NewObject(interfaceClass, method,
                   serverConnectionHandlerID, newStatus, errorNumber);

    env->CallVoidMethod(new_object, cache.second);

    if (isAttached)
        gJavaVM->DetachCurrentThread();
}

void onNewChannelEvent(uint64 serverConnectionHandlerID, uint64 channelID, uint64 channelParentID) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    // Connect //
    JNIEnv *env;
    bool isAttached = connectVM(env);

    const auto& cache = Android_Event_NewChannel;

    jclass interfaceClass = env->GetObjectClass(cache.first);
    jmethodID method = env->GetMethodID(interfaceClass, "<init>", "(JJJ)V");
    const auto new_object = env->NewObject(interfaceClass, method, serverConnectionHandlerID, channelID, channelParentID);

    env->CallVoidMethod(new_object, cache.second);

    if (isAttached)
        gJavaVM->DetachCurrentThread();
}

void onNewChannelCreatedEvent(uint64 serverConnectionHandlerID, uint64 channelID, uint64 channelParentID, anyID invokerID, const char* invokerName, const char* invokerUniqueIdentifier) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    // Connect //
    JNIEnv *env;
    bool isAttached = connectVM(env);

    const auto& cache = Android_Event_NewChannelCreated;
    jclass interfaceClass = env->GetObjectClass(cache.first);
    jmethodID method = env->GetMethodID(interfaceClass, "<init>",
                                        "(JJJILjava/lang/String;Ljava/lang/String;)V");
    jobject temp1 = env->NewLocalRef(env->NewStringUTF(invokerName));
    jobject temp2 = env->NewLocalRef(
            env->NewStringUTF(invokerUniqueIdentifier));
    const auto new_object = env->NewObject(interfaceClass, method,
                   serverConnectionHandlerID, channelID, channelParentID, invokerID,
                   temp1, temp2);

    env->CallVoidMethod(new_object, cache.second);

    if (isAttached)
        gJavaVM->DetachCurrentThread();

}

void onDelChannelEvent(uint64 serverConnectionHandlerID, uint64 channelID, anyID invokerID, const char* invokerName, const char* invokerUniqueIdentifier) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    // Connect //
    JNIEnv *env;
    bool isAttached = connectVM(env);

    const auto& cache = Android_Event_DelChannel;
    jclass interfaceClass = env->GetObjectClass(cache.first);
    jmethodID method = env->GetMethodID(interfaceClass, "<init>", "(JJILjava/lang/String;Ljava/lang/String;)V");
    jobject temp1 = env->NewLocalRef(env->NewStringUTF(invokerName));
    jobject temp2 = env->NewLocalRef(
            env->NewStringUTF(invokerUniqueIdentifier));
    const auto new_object = env->NewObject(interfaceClass, method,
                   serverConnectionHandlerID, channelID, invokerID, temp1, temp2);

    env->CallVoidMethod(new_object, cache.second);
    if (isAttached)
        gJavaVM->DetachCurrentThread();

}

void onClientMoveEvent(uint64 serverConnectionHandlerID, anyID clientID, uint64 oldChannelID, uint64 newChannelID, int visibility, const char* moveMessage) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    // Connect //
    JNIEnv *env;
    bool isAttached = connectVM(env);

    const auto& cache = Android_Event_ClientMove;

    jclass interfaceClass = env->GetObjectClass(cache.first);
    jmethodID method = env->GetMethodID(interfaceClass, "<init>",
                                        "(JIJJILjava/lang/String;)V");
    jobject temp1 = env->NewLocalRef(env->NewStringUTF(moveMessage));
    const auto new_object = env->NewObject(interfaceClass, method,
                   serverConnectionHandlerID, clientID, oldChannelID, newChannelID,
                   visibility, temp1);

    env->CallVoidMethod(new_object, cache.second);
    if (isAttached)
        gJavaVM->DetachCurrentThread();

}

void onClientMoveSubscriptionEvent(uint64 serverConnectionHandlerID, anyID clientID, uint64 oldChannelID, uint64 newChannelID, int visibility) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    // Connect //
    JNIEnv *env;
    bool isAttached = connectVM(env);

    const auto& cache = Android_Event_ClientMoveSubscription;

    jclass interfaceClass = env->GetObjectClass(cache.first);
    jmethodID method = env->GetMethodID(interfaceClass, "<init>", "(JIJJI)V");
    const auto new_object = env->NewObject(interfaceClass, method,
                   serverConnectionHandlerID, clientID, oldChannelID, newChannelID,
                   visibility);

    env->CallVoidMethod(new_object, cache.second);

    if (isAttached)
        gJavaVM->DetachCurrentThread();
}

void onClientMoveTimeoutEvent(uint64 serverConnectionHandlerID, anyID clientID, uint64 oldChannelID, uint64 newChannelID, int visibility, const char* timeoutMessage) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    // Connect //
    JNIEnv *env;
    bool isAttached = connectVM(env);

    const auto& cache = Android_Event_ClientMoveTimeout;

    jclass interfaceClass = env->GetObjectClass(cache.first);
    jmethodID method = env->GetMethodID(interfaceClass, "<init>",
                                        "(JIJJILjava/lang/String;)V");
    jobject temp1 = env->NewLocalRef(env->NewStringUTF(timeoutMessage));
    const auto new_object = env->NewObject(interfaceClass, method,
                   serverConnectionHandlerID, clientID, oldChannelID, newChannelID,
                   visibility, temp1);

    env->CallVoidMethod(new_object, cache.second);

    if (isAttached)
        gJavaVM->DetachCurrentThread();

}

void onClientMoveMovedEvent(uint64 serverConnectionHandlerID, anyID clientID, uint64 oldChannelID, uint64 newChannelID, int visibility, anyID moverID, const char* moverName, const char* moverUniqueIdentifier, const char* moveMessage) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    // Connect //
    JNIEnv *env;
    bool isAttached = connectVM(env);

    const auto& cache = Android_Event_ClientMoveMoved;

    jclass interfaceClass = env->GetObjectClass(cache.first);
    jmethodID method = env->GetMethodID(interfaceClass, "<init>",
                                        "(JIJJIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    jobject temp1 = env->NewLocalRef(env->NewStringUTF(moverName));
    jobject temp2 = env->NewLocalRef(env->NewStringUTF(moverUniqueIdentifier));
    jobject temp3 = env->NewLocalRef(env->NewStringUTF(moveMessage));
    const auto new_object = env->NewObject(interfaceClass, method,
                   serverConnectionHandlerID, clientID, oldChannelID, newChannelID,
                   visibility, moverID, temp1, temp2, temp3);

    env->CallVoidMethod(new_object, cache.second);

    if (isAttached)
        gJavaVM->DetachCurrentThread();

}

void onTalkStatusChangeEvent(uint64 serverConnectionHandlerID, int status, int isReceivedWhisper, anyID clientID) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    // Connect //
    JNIEnv *env;
    bool isAttached = connectVM(env);

    const auto& cache = Android_Event_TalkStatusChange;
    jclass interfaceClass = env->GetObjectClass(cache.first);
    jmethodID method = env->GetMethodID(interfaceClass, "<init>", "(JIII)V");
    const auto new_object = env->NewObject(interfaceClass, method,
                   serverConnectionHandlerID, status, isReceivedWhisper, clientID);

    env->CallVoidMethod(new_object, cache.second);

    if (isAttached)
        gJavaVM->DetachCurrentThread();
}

void onServerErrorEvent(uint64 serverConnectionHandlerID, const char* errorMessage, unsigned int error, const char* returnCode, const char* extraMessage) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    // Connect //
    JNIEnv *env;
    bool isAttached = connectVM(env);

    const auto& cache = Android_Event_ServerError;

    jclass interfaceClass = env->GetObjectClass(cache.first);
    jmethodID method = env->GetMethodID(interfaceClass, "<init>",
                                        "(JLjava/lang/String;ILjava/lang/String;Ljava/lang/String;)V");
    jobject temp1 = env->NewLocalRef(env->NewStringUTF(errorMessage));
    jobject temp2 = env->NewLocalRef(env->NewStringUTF(returnCode));
    jobject temp3 = env->NewLocalRef(env->NewStringUTF(extraMessage));
    const auto new_object = env->NewObject(interfaceClass, method,
                   serverConnectionHandlerID, temp1, error, temp2, temp3);

    env->CallVoidMethod(new_object, cache.second);

    if (isAttached)
        gJavaVM->DetachCurrentThread();

}

void onUserLoggingMessageEvent(const char* logMessage, int logLevel, const char* logChannel, uint64 logID, const char* logTime, const char* completeLogString) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
#ifdef DEBUG_CLIENTLIB
    __android_log_print(ANDROID_LOG_DEBUG, "DEBUG", "%s",completeLogString);
#endif
    // Connect //
    JNIEnv *env;
    bool isAttached = connectVM(env);

    const auto& cache = Android_Event_UserLoggingMessage;

    jclass interfaceClass = env->GetObjectClass(cache.first);
    jmethodID method =
            env->GetMethodID(interfaceClass, "<init>",
                             "(Ljava/lang/String;ILjava/lang/String;JLjava/lang/String;Ljava/lang/String;)V");
    jobject temp1 = env->NewLocalRef(env->NewStringUTF(logMessage));
    jobject temp2 = env->NewLocalRef(env->NewStringUTF(logChannel));
    jobject temp3 = env->NewLocalRef(env->NewStringUTF(logTime));
    jobject temp4 = env->NewLocalRef(env->NewStringUTF(completeLogString));

    const auto new_object = env->NewObject(interfaceClass, method, temp1, logLevel, temp2,
                   logID, temp3, temp4);

    env->CallVoidMethod(new_object, cache.second);

    if (isAttached)
        gJavaVM->DetachCurrentThread();

}

///////////////////////////////////////////////////////////////////////////
// Internals
///////////////////////////////////////////////////////////////////////////

/* Initialize client lib with callbacks */
int init(const char* native_lib_path/*, const std::vector<std::string>& events_to_register*/) {
#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif
    unsigned int error;

    /* Create struct for callback function pointers */
    struct ClientUIFunctions clUIFuncs;

    /* Initialize all function pointers with NULL */
    memset(&clUIFuncs, 0, sizeof(struct ClientUIFunctions));

    /* Callback function pointers */
    /* It is sufficient to only assign those callback functions you are using. When adding more callbacks, add those function pointers here. */
    clUIFuncs.onConnectStatusChangeEvent    = onConnectStatusChangeEvent;
    clUIFuncs.onNewChannelEvent             = onNewChannelEvent;
    clUIFuncs.onNewChannelCreatedEvent      = onNewChannelCreatedEvent;
    clUIFuncs.onDelChannelEvent             = onDelChannelEvent;
    clUIFuncs.onClientMoveEvent             = onClientMoveEvent;
    clUIFuncs.onClientMoveSubscriptionEvent = onClientMoveSubscriptionEvent;
    clUIFuncs.onClientMoveTimeoutEvent      = onClientMoveTimeoutEvent;
    clUIFuncs.onClientMoveMovedEvent        = onClientMoveMovedEvent;
    clUIFuncs.onTalkStatusChangeEvent       = onTalkStatusChangeEvent;
    clUIFuncs.onServerErrorEvent            = onServerErrorEvent;
    clUIFuncs.onUserLoggingMessageEvent     = onUserLoggingMessageEvent;

    /* Initialize client lib with callbacks */
    error = ts3client_initClientLib(&clUIFuncs, NULL, LogType_USERLOGGING, NULL, native_lib_path);
#ifdef DEBUG_CLIENTLIB
    ts3client_setLogVerbosity(LogLevel_DEVEL);
#endif
    if (error != ERROR_ok) {
        char* errormsg;
        if (ts3client_getErrorMessage(error, &errormsg) == ERROR_ok) {
            LOGE("Error initializing clientlib: %s\n", errormsg);
            ts3client_freeMemory(errormsg);
        }
        return error;
    }
    return error;
}

void initClassHelper(JNIEnv *env, const char *path, jobject *objptr, jmethodID* fire_method_id = nullptr) {
    jclass cls = env->FindClass(path);
    if (!cls) {
        LOGE("initClassHelper: failed to get %s class reference", path);
        return;
    }
    jmethodID constr = env->GetMethodID(cls, "<init>", "()V");
    if (!constr) {
        LOGE("initClassHelper: failed to get %s constructor", path);
        return;
    }
    jobject obj = env->NewObject(cls, constr);
    if (!obj) {
        LOGE("initClassHelper: failed to create a %s object", path);
        return;
    }
    (*objptr) = env->NewGlobalRef(obj);

    if (fire_method_id)
    {
        (*fire_method_id) = env->GetMethodID(cls, "Post", "()V");
        if (!(*fire_method_id)) {
            LOGE("initClassHelper: failed to get %s post method", path);
        }
    }
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
#if defined(__aarch64__)
    #define ABI "arm64-v8a"
#elif defined(__arm__)
    #if defined(__ARM_ARCH_7A__)
    #if defined(__ARM_NEON__)
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a/NEON (hard-float)"
      #else
        #define ABI "armeabi-v7a/NEON"
      #endif
    #else
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a (hard-float)"
      #else
        #define ABI "armeabi-v7a"
      #endif
    #endif
  #else
   #define ABI "armeabi"
  #endif
#elif defined(__i386__)
#define ABI "x86"
#elif defined(__x86_64__)
#define ABI "x86_64"
#elif defined(__mips64)  /* mips64el-* toolchain defines __mips__ too */
#define ABI "mips64"
#elif defined(__mips__)
#define ABI "mips"
#else
#define ABI "unknown"
#endif

    LOGD("Loaded ABI: %s",ABI);

#ifdef DEBUG_BUILD
    LOGD(__FUNCTION__);
#endif

    JNIEnv *env;

    LOGI("JNI_OnLoad called");
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("Failed to get the environment using GetEnv()");
        return -1;
    }
    env->GetJavaVM(&gJavaVM);

    initClassHelper(env, "java/lang/String", &StringClass);
    initClassHelper(env, "com/teamspeak/ts3sdkclient/ts3sdk/events/ConnectStatusChange", &Android_Event_ConnectStatusChange.first, &Android_Event_ConnectStatusChange.second);
    initClassHelper(env, "com/teamspeak/ts3sdkclient/ts3sdk/events/NewChannel", &Android_Event_NewChannel.first, &Android_Event_NewChannel.second);
    initClassHelper(env, "com/teamspeak/ts3sdkclient/ts3sdk/events/NewChannelCreated", &Android_Event_NewChannelCreated.first, &Android_Event_NewChannelCreated.second);
    initClassHelper(env, "com/teamspeak/ts3sdkclient/ts3sdk/events/DelChannel", &Android_Event_DelChannel.first, &Android_Event_DelChannel.second);
    initClassHelper(env, "com/teamspeak/ts3sdkclient/ts3sdk/events/ClientMove", &Android_Event_ClientMove.first, &Android_Event_ClientMove.second);
    initClassHelper(env, "com/teamspeak/ts3sdkclient/ts3sdk/events/ClientMoveSubscription", &Android_Event_ClientMoveSubscription.first, &Android_Event_ClientMoveSubscription.second);
    initClassHelper(env, "com/teamspeak/ts3sdkclient/ts3sdk/events/ClientMoveTimeout", &Android_Event_ClientMoveTimeout.first, &Android_Event_ClientMoveTimeout.second);
    initClassHelper(env, "com/teamspeak/ts3sdkclient/ts3sdk/events/ClientMoveMoved",&Android_Event_ClientMoveMoved.first, &Android_Event_ClientMoveMoved.second);
    initClassHelper(env, "com/teamspeak/ts3sdkclient/ts3sdk/events/TalkStatusChange", &Android_Event_TalkStatusChange.first, &Android_Event_TalkStatusChange.second);
    initClassHelper(env, "com/teamspeak/ts3sdkclient/ts3sdk/events/ServerError", &Android_Event_ServerError.first, &Android_Event_ServerError.second);
    initClassHelper(env, "com/teamspeak/ts3sdkclient/ts3sdk/events/UserLoggingMessage", &Android_Event_UserLoggingMessage.first, &Android_Event_UserLoggingMessage.second);

    LOGD("JNI_OnLoad done.");

    return JNI_VERSION_1_6;
}




