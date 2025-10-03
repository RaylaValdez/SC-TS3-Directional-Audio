/*
 * TeamSpeak SDK client whisperer sample
 *
 * Copyright (c) TeamSpeak-Systems
 */

#ifdef _WIN32
#define _CRT_SECURE_NO_WARNINGS
#include <Windows.h>
#else
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#endif
#include <stdio.h>

#include <teamspeak/public_definitions.h>
#include <teamspeak/public_errors.h>
#include <teamspeak/clientlib.h>

#include <algorithm>
#include <string>
#include <vector>

#ifdef _WIN32
#define SLEEP(x) Sleep(x)
#define strdup(x) _strdup(x)
#else
#define SLEEP(x) usleep(x*1000)
#endif

namespace {
    auto my_id = anyID{ 0 };
    constexpr const auto* kWhisperApp = "Whisperer";

    // for logging
    auto client_id_array_to_string(const std::vector<anyID>& client_ids)->std::string
    {
        auto result = std::string();
        for (const auto client_id : client_ids)
            result += std::to_string(client_id);

        return result;
    }

    void print_error(uint32_t error, const std::string& msg, uint64 connection_id = 0)
    {
        if (error == ERROR_ok)
            return;

        char* errormsg = nullptr;
        if (ts3client_getErrorMessage(error, &errormsg) == ERROR_ok)
        {
            auto error_msg = msg + " " + std::string(errormsg);
            ts3client_freeMemory(errormsg);
            ts3client_logMessage(error_msg.c_str(), LogLevel_ERROR, kWhisperApp, connection_id);
            return;
        }
        ts3client_logMessage(msg.c_str(), LogLevel_ERROR, kWhisperApp, connection_id);
    }

    std::vector<anyID> get_client_ids(uint64 connection_id)
    {
        anyID* client_ids = nullptr;
        if (auto error = ts3client_getClientList(connection_id, &client_ids); error != ERROR_ok)
        {
            print_error(error, "Couldn't get client list.", connection_id);
            return {};
        }
        auto result = std::vector<anyID>{};
        for (auto i = size_t{ 0 }; client_ids[i]; i++)
            result.push_back(client_ids[i]);

        ts3client_freeMemory(client_ids);
        return result;
    }

    void filter_own_client_id(uint64 connection_id, std::vector<anyID>& client_ids)
    {
        if (auto it = std::find(std::begin(client_ids), std::end(client_ids), my_id); it != std::end(client_ids))
            client_ids.erase(it);
    }

    void set_whisper_to_all_visible(uint64 connection_id)
    {
        auto target_client_ids = get_client_ids(connection_id);
        filter_own_client_id(connection_id, target_client_ids);
        if (target_client_ids.size() > 0)
        {
            target_client_ids.push_back(0);
            auto whisper_log = std::string("Whisperlist set to ") + client_id_array_to_string(target_client_ids);
            if (auto error = ts3client_requestClientSetWhisperList(connection_id, 0, nullptr, target_client_ids.data(), nullptr); error != ERROR_ok)
            {
                print_error(error, whisper_log + ", but FAILED", connection_id);
                return;
            }
            ts3client_logMessage(whisper_log.c_str(), LogLevel_INFO, kWhisperApp, connection_id);

            if (auto error = ts3client_setClientSelfVariableAsInt(connection_id, CLIENT_INPUT_DEACTIVATED, InputDeactivationStatus::INPUT_ACTIVE); error != ERROR_ok && error != ERROR_ok_no_update)
            {
                print_error(error, "Couldn't activate input.", connection_id);
                return;
            }
            else if (error == ERROR_ok)
                ts3client_logMessage("Activated Input for whispering.", LogLevel_INFO, kWhisperApp, connection_id);

            return;
        }

        if (auto error = ts3client_setClientSelfVariableAsInt(connection_id, CLIENT_INPUT_DEACTIVATED, InputDeactivationStatus::INPUT_DEACTIVATED); error != ERROR_ok && error != ERROR_ok_no_update)
        {
            print_error(error, "Couldn't deactivate input.", connection_id);
        }
        else
        {
            ts3client_logMessage("Deactivated Input: No whisper targets.", LogLevel_INFO, kWhisperApp, connection_id);
        }
    }

    void on_client_move_common(uint64 connection_id, anyID client_id, uint64 oldChannelID, uint64 newChannelID, int visibility)
    {
        if (visibility == Visibility::ENTER_VISIBILITY || visibility == Visibility::LEAVE_VISIBILITY)
        {
            if (!(newChannelID == 0 && client_id == my_id))
                set_whisper_to_all_visible(connection_id);
        }
    }
}


/*
 * Callback for connection status change.
 * Connection status switches through the states STATUS_DISCONNECTED, STATUS_CONNECTING, STATUS_CONNECTED and STATUS_CONNECTION_ESTABLISHED.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   newStatus                 - New connection status, see the enum ConnectStatus in clientlib_publicdefinitions.h
 *   errorNumber               - Error code. Should be zero when connecting or actively disconnection.
 *                               Contains error state when losing connection.
 */
void onConnectStatusChangeEvent(uint64 serverConnectionHandlerID, int newStatus, unsigned int errorNumber) {
    printf("Connect status changed: %llu %d %u\n", (unsigned long long)serverConnectionHandlerID, newStatus, errorNumber);
    /* Failed to connect ? */
    if(newStatus == STATUS_DISCONNECTED && errorNumber == ERROR_failed_connection_initialisation) {
        printf("Looks like there is no server running.\n");
    }
    if (newStatus == STATUS_CONNECTED)
    {
        if (auto error = ts3client_getClientID(serverConnectionHandlerID, &my_id); error != ERROR_ok)
        {
            print_error(error, "Couldn't get own client id.", serverConnectionHandlerID);
            return;
        }
    }
    else if (newStatus == STATUS_DISCONNECTED)
        my_id = 0;
}

/*
 * Called when a client joins, leaves or moves to another channel.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   clientID                  - ID of the moved client
 *   oldChannelID              - ID of the old channel left by the client
 *   newChannelID              - ID of the new channel joined by the client
 *   visibility                - Visibility of the moved client. See the enum Visibility in clientlib_publicdefinitions.h
 *                               Values: ENTER_VISIBILITY, RETAIN_VISIBILITY, LEAVE_VISIBILITY
 */
void onClientMoveEvent(uint64 connection_id, anyID clientID, uint64 oldChannelID, uint64 newChannelID, int visibility, const char* moveMessage) {
    printf("ClientID %u moves from channel %llu to %llu with message %s\n", clientID, (unsigned long long)oldChannelID, (unsigned long long)newChannelID, moveMessage);
    on_client_move_common(connection_id, clientID, oldChannelID, newChannelID, visibility);
}

/*
 * Callback for other clients in current and subscribed channels being announced to the client.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   clientID                  - ID of the announced client
 *   oldChannelID              - ID of the subscribed channel where the client left visibility
 *   newChannelID              - ID of the subscribed channel where the client entered visibility
 *   visibility                - Visibility of the announced client. See the enum Visibility in clientlib_publicdefinitions.h
 *                               Values: ENTER_VISIBILITY, RETAIN_VISIBILITY, LEAVE_VISIBILITY
 */
void onClientMoveSubscriptionEvent(uint64 connection_id, anyID clientID, uint64 oldChannelID, uint64 newChannelID, int visibility) {
    char* name;

    /* Query client nickname from ID */
    if(ts3client_getClientVariableAsString(connection_id, clientID, CLIENT_NICKNAME, &name) != ERROR_ok)
        return;
    printf("New client: %s\n", name);
    ts3client_freeMemory(name);  /* Release dynamically allocated memory only if function succeeded */
    on_client_move_common(connection_id, clientID, oldChannelID, newChannelID, visibility);
}

/*
 * Called when a client drops his connection.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   clientID                  - ID of the moved client
 *   oldChannelID              - ID of the channel the leaving client was previously member of
 *   newChannelID              - 0, as client is leaving
 *   visibility                - Always LEAVE_VISIBILITY
 *   timeoutMessage            - Optional message giving the reason for the timeout
 */
void onClientMoveTimeoutEvent(uint64 connection_id, anyID clientID, uint64 oldChannelID, uint64 newChannelID, int visibility, const char* timeoutMessage) {
    printf("ClientID %u timeouts with message %s\n",clientID, timeoutMessage);
    on_client_move_common(connection_id, clientID, oldChannelID, newChannelID, visibility);
}

void onClientMoveMovedEvent(uint64 connection_id, anyID clientID, uint64 oldChannelID, uint64 newChannelID, int visibility, anyID /*moverID*/, const char* /*moverName*/, const char* /*moverUniqueIdentifier*/, const char* /*moveMessage*/)
{
    on_client_move_common(connection_id, clientID, oldChannelID, newChannelID, visibility);
}
void onClientKickFromChannelEvent(uint64 connection_id, anyID clientID, uint64 oldChannelID, uint64 newChannelID, int visibility, anyID /*kickerID*/, const char* /*kickerName*/ , const char* /*kickerUniqueIdentifier*/ , const char* /*kickMessage*/)
{
    on_client_move_common(connection_id, clientID, oldChannelID, newChannelID, visibility);
}
void onClientKickFromServerEvent(uint64 connection_id, anyID clientID, uint64 oldChannelID, uint64 newChannelID, int visibility, anyID /*kickerID */ , const char* /*kickerName*/ , const char* /*kickerUniqueIdentifier*/ , const char* /*kickMessage*/)
{
    on_client_move_common(connection_id, clientID, oldChannelID, newChannelID, visibility);
}

/*
 * This event is called when a client starts or stops talking.
 *
 * Parameters:
 *   serverConnectionHandlerID - Server connection handler ID
 *   status                    - 1 if client starts talking, 0 if client stops talking
 *   isReceivedWhisper         - 1 if this event was caused by whispering, 0 if caused by normal talking
 *   clientID                  - ID of the client who announced the talk status change
 */
void onTalkStatusChangeEvent(uint64 serverConnectionHandlerID, int status, int isReceivedWhisper, anyID clientID) {
    char* name;

    /* Query client nickname from ID */
    if(ts3client_getClientVariableAsString(serverConnectionHandlerID, clientID, CLIENT_NICKNAME, &name) != ERROR_ok)
        return;
    if(status == STATUS_TALKING) {
        printf("Client \"%s\" starts talking.\n", name);
    } else {
        printf("Client \"%s\" stops talking.\n", name);
    }
    ts3client_freeMemory(name);  /* Release dynamically allocated memory only if function succeeded */
}

void onServerErrorEvent(uint64 serverConnectionHandlerID, const char* errorMessage, unsigned int error, const char* returnCode, const char* extraMessage) {
    printf("Error for server %llu: %s %s\n", (unsigned long long)serverConnectionHandlerID, errorMessage, extraMessage);
}

char* programPath(char* programInvocation){
    char* path;
    char* end;
    int length;
    char pathsep;

    if (programInvocation == NULL) return strdup("");

#ifdef _WIN32
    pathsep = '\\';
#else
    pathsep = '/';
#endif

    end = strrchr(programInvocation, pathsep);
    if (!end) return strdup("");

    length = (end - programInvocation)+2;
    path = (char*) malloc(length);
    strncpy(path, programInvocation, length-1);
    path[length-1] = 0;

    return path;
}

int main(int argc, char **argv) {
    uint64 scHandlerID;
    char *version;
    char *identity;
    char * path;

    /* Create struct for callback function pointers */
    struct ClientUIFunctions funcs;

    /* Initialize all callbacks with NULL */
    memset(&funcs, 0, sizeof(struct ClientUIFunctions));

    /* Callback function pointers */
    /* It is sufficient to only assign those callback functions you are using. When adding more callbacks, add those function pointers here. */
    funcs.onConnectStatusChangeEvent    = onConnectStatusChangeEvent;
    funcs.onClientMoveEvent             = onClientMoveEvent;
    funcs.onClientMoveSubscriptionEvent = onClientMoveSubscriptionEvent;
    funcs.onClientMoveTimeoutEvent      = onClientMoveTimeoutEvent;
    funcs.onClientMoveMovedEvent        = onClientMoveMovedEvent;
    funcs.onClientKickFromChannelEvent  = onClientKickFromChannelEvent;
    funcs.onClientKickFromServerEvent   = onClientKickFromServerEvent;
    funcs.onTalkStatusChangeEvent       = onTalkStatusChangeEvent;
    funcs.onServerErrorEvent            = onServerErrorEvent;
    funcs.onIgnoredWhisperEvent         = [](uint64 connection_id, anyID client_id) {
        print_error(ts3client_allowWhispersFrom(connection_id, client_id), "Error allowing whisper", connection_id);
    };

    /* Initialize client lib with callbacks */
    /* Resource path points to the SDK\bin directory to locate the soundbackends*/
    path = programPath(argv[0]);
    {
        auto error = ts3client_initClientLib(&funcs, NULL, LogType_FILE | LogType_CONSOLE | LogType_USERLOGGING, NULL, path);
        free(path);

        if(error != ERROR_ok) {
            char* errormsg;
            if(ts3client_getErrorMessage(error, &errormsg) == ERROR_ok) {
                printf("Error initialzing serverlib: %s\n", errormsg);
                ts3client_freeMemory(errormsg);
            }
            return 1;
        }
    }

    /* Spawn a new server connection handler using the default port and store the server ID */
    if(auto error = ts3client_spawnNewServerConnectionHandler(0, &scHandlerID); error != ERROR_ok) {
        printf("Error spawning server connection handler: %d\n", error);
        return 1;
    }

    /* Open default capture device */
    /* Passing empty string for mode and NULL or empty string for device will open the default device */
    if(auto error = ts3client_openCaptureDevice(scHandlerID, "", NULL); error != ERROR_ok) {
        printf("Error opening capture device: %d\n", error);
    }

    /* Open default playback device */
    /* Passing empty string for mode and NULL or empty string for device will open the default device */
    if(auto error = ts3client_openPlaybackDevice(scHandlerID, "", NULL); error != ERROR_ok) {
        printf("Error opening playback device: %d\n", error);
    }

    /* turn on vad */
    if (auto error = ts3client_setPreProcessorConfigValue(scHandlerID, "vad", "true"); error != ERROR_ok)
        print_error(error, "Couldn't turn on VAD.", scHandlerID);

    /* Adjust "vad_mode" value to use hybrid by default */
    if (auto error = ts3client_setPreProcessorConfigValue(scHandlerID, "vad_mode", "2"); error != ERROR_ok) {
        printf("Error setting vad_mode value to hybrid: %d\n", error);
    }

    /* Adjust "voiceactivation_level" value */
    if (auto error = ts3client_setPreProcessorConfigValue(scHandlerID, "voiceactivation_level", "-20"); error != ERROR_ok) {
        printf("Error setting voiceactivation_level: %d\n", error);
    }

    /* Create a new client identity */
    /* In your real application you should do this only once, store the assigned identity locally and then reuse it. */
    if(auto error = ts3client_createIdentity(&identity); error != ERROR_ok) {
        printf("Error creating identity: %d\n", error);
        return 1;
    }

    /* Connect to server on localhost:9987 with nickname "client", no default channel, no default channel password and server password "secret" */
    if(auto error = ts3client_startConnection(scHandlerID, identity, "localhost", 9987, "client", NULL, "", "secret"); error != ERROR_ok) {
        printf("Error connecting to server: %d\n", error);
        return 1;
    }

    ts3client_freeMemory(identity);  /* Release dynamically allocated memory */
    identity = NULL;

    printf("Client lib initialized and running\n");

    /* Query and print client lib version */
    if(auto error = ts3client_getClientLibVersion(&version); error != ERROR_ok) {
        printf("Failed to get clientlib version: %d\n", error);
        return 1;
    }
    printf("Client lib version: %s\n", version);
    ts3client_freeMemory(version);  /* Release dynamically allocated memory */
    version = NULL;

    SLEEP(500);

    /* Wait for user input */
    printf("\n--- Press Return to disconnect from server and exit ---\n");
    getchar();

    /* Disconnect from server */
    if(auto error = ts3client_stopConnection(scHandlerID, "leaving"); error != ERROR_ok) {
        printf("Error stopping connection: %d\n", error);
        return 1;
    }

    SLEEP(200);

    /* Destroy server connection handler */
    if(auto error = ts3client_destroyServerConnectionHandler(scHandlerID); error != ERROR_ok) {
        printf("Error destroying clientlib: %d\n", error);
        return 1;
    }

    /* Shutdown client lib */
    if(auto error = ts3client_destroyClientLib(); error != ERROR_ok) {
        printf("Failed to destroy clientlib: %d\n", error);
        return 1;
    }

    return 0;
}
