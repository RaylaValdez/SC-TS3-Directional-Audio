/*
 * Star Citizen Directional Audio – TS3 Plugin (safe logging variant)
 * Based on your working plugin.c; minimal changes:
 *  - use ts3Functions.logMessage instead of printf
 *  - safe fallback in ts3plugin_name
 *  - infoData ignores by setting *data = NULL
 */

#include "pch.h"  // first line in every .cpp

#if defined(WIN32) || defined(__WIN32__) || defined(_WIN32)
#pragma warning(disable : 4100) /* Disable Unreferenced parameter warning */
#include <Windows.h>
#endif

#include <cstdlib>
#include <cstdio>
#include <cstring>

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

 /* === TeamSpeak SDK headers (match your original order) === */
#include "teamspeak/public_definitions.h"
#include "teamspeak/public_errors.h"
#include "teamspeak/public_errors_rare.h"
#include "teamspeak/public_rare_definitions.h"
#include "ts3_functions.h"
#include "plugin_definitions.h"

/* Your project’s plugin.h (exports) */
#include "plugin.h"

/* ===== Local defines (keep as in your working original) ===== */
static struct TS3Functions ts3Functions;

#ifdef _WIN32
#define _strcpy(dest, destSize, src) strcpy_s(dest, destSize, src)
#define snprintf sprintf_s
#else
#define _strcpy(dest, destSize, src) \
    { strncpy(dest, src, destSize - 1); (dest)[destSize - 1] = '\0'; }
#endif

#define PLUGIN_API_VERSION 26

#define PATH_BUFSIZE      512
#define COMMAND_BUFSIZE   128
#define INFODATA_BUFSIZE  128
#define SERVERINFO_BUFSIZE 256
#define CHANNELINFO_BUFSIZE 512
#define RETURNCODE_BUFSIZE 128

static char* pluginID = NULL;

/* --------- logging helpers (no printf anywhere) --------- */
static void logTS(uint64 sch, enum LogLevel lvl, const char* msg) {
    if (ts3Functions.logMessage) ts3Functions.logMessage(msg, lvl, "SC-DA", sch);
}
static void logInfo(const char* msg, uint64 sch = 0) { logTS(sch, LogLevel_INFO, msg); }
static void logWarn(const char* msg, uint64 sch = 0) { logTS(sch, LogLevel_WARNING, msg); }
static void logError(const char* msg, uint64 sch = 0) { logTS(sch, LogLevel_ERROR, msg); }

#ifdef _WIN32
/* Helper: wchar_t -> UTF-8 (same as your original, but safer fallback) */
static int wcharToUtf8(const wchar_t* str, char** result)
{
    int outlen = WideCharToMultiByte(CP_UTF8, 0, str, -1, 0, 0, 0, 0);
    if (outlen <= 0) { *result = NULL; return -1; }
    *result = (char*)malloc(outlen);
    if (!*result) return -1;
    if (WideCharToMultiByte(CP_UTF8, 0, str, -1, *result, outlen, 0, 0) == 0) {
        free(*result); *result = NULL; return -1;
    }
    return 0;
}
#endif


// --- chat message helper ---
static void chatf(const char* fmt, ...) {
    if (!ts3Functions.printMessageToCurrentTab) return;
    char buf[512];
    va_list ap;
    va_start(ap, fmt);
#if defined(_WIN32)
    vsnprintf_s(buf, sizeof(buf), _TRUNCATE, fmt, ap);
#else
    vsnprintf(buf, sizeof(buf), fmt, ap);
#endif
    va_end(ap);
    ts3Functions.printMessageToCurrentTab(buf);
}


/*********************************** Required functions ************************************/

/* Unique name identifying this plugin */
const char* ts3plugin_name()
{
#ifdef _WIN32
    static char* result = NULL; /* allocate once and keep */
    if (!result) {
        const wchar_t* name = L"Star Citizen Directional Audio";
        if (wcharToUtf8(name, &result) == -1 || !result) {
            /* Conversion failed -> return a static literal (no stack buffer!) */
            return "Star Citizen Directional Audio";
        }
    }
    return result;
#else
    return "Star Citizen Directional Audio";
#endif
}

/* Plugin version */
const char* ts3plugin_version() { return "1.2"; }

/* Must match client API major */
int ts3plugin_apiVersion() { return PLUGIN_API_VERSION; }

/* Author */
const char* ts3plugin_author() { return "RaylaValdez"; }

/* Description */
const char* ts3plugin_description() {
    return "This plugin takes positional data from SCTS3DA.exe and places clients in 3D audio coordinates for Star Citizen.";
}

/* Set TeamSpeak 3 callback functions (passed by value per SDK) */
void ts3plugin_setFunctionPointers(const struct TS3Functions funcs)
{
    ts3Functions = funcs;
}

/* Called after loading the plugin. Return 0 on success. */
int ts3plugin_init()
{
    char appPath[PATH_BUFSIZE] = { 0 };
    char resourcesPath[PATH_BUFSIZE] = { 0 };
    char configPath[PATH_BUFSIZE] = { 0 };
    char pluginPath[PATH_BUFSIZE] = { 0 };
    char buf[1024];

    logInfo("PLUGIN: init");

    ts3Functions.getAppPath(appPath, PATH_BUFSIZE);
    ts3Functions.getResourcesPath(resourcesPath, PATH_BUFSIZE);
    ts3Functions.getConfigPath(configPath, PATH_BUFSIZE);
    ts3Functions.getPluginPath(pluginPath, PATH_BUFSIZE, pluginID);

    snprintf(buf, sizeof(buf),
        "PLUGIN paths -> App: %s | Resources: %s | Config: %s | Plugin: %s",
        appPath, resourcesPath, configPath, pluginPath);
    logInfo(buf);

    chatf("[color=green][b]SC Directional Audio[/b] loaded and initialized.[/color]");
    chatf("[color=green]Version: %s[/color]", ts3plugin_version());


    return 0;
}

/* Called before unloading the plugin */
void ts3plugin_shutdown()
{
    logInfo("PLUGIN: shutdown");

    if (pluginID) {
        free(pluginID);
        pluginID = NULL;
    }
}

/****************************** Optional functions ********************************/

void ts3plugin_registerPluginID(const char* id)
{
    const size_t sz = strlen(id) + 1;
    pluginID = (char*)malloc(sz * sizeof(char));
    _strcpy(pluginID, sz, id);
    char buf[256];
    snprintf(buf, sizeof(buf), "PLUGIN: registerPluginID: %s", pluginID);
    logInfo(buf);
}

const char* ts3plugin_commandKeyword() { return "scda"; }

void ts3plugin_currentServerConnectionChanged(uint64 serverConnectionHandlerID)
{
    char buf[256];
    snprintf(buf, sizeof(buf), "PLUGIN: currentServerConnectionChanged %llu",
        (unsigned long long)serverConnectionHandlerID);
    logInfo(buf, serverConnectionHandlerID);
}

/* Info panel title */
const char* ts3plugin_infoTitle() { return "Star Citizen Directional Audio info"; }

/* Info panel content; set *data=NULL to ignore */
void ts3plugin_infoData(uint64 sch, uint64 id, enum PluginItemType type, char** data)
{
    char* name;

    switch (type) {
    case PLUGIN_SERVER:
        if (ts3Functions.getServerVariableAsString(sch, VIRTUALSERVER_NAME, &name) != ERROR_ok) {
            logError("Error getting virtual server name", sch);
            *data = NULL; return;
        }
        break;
    case PLUGIN_CHANNEL:
        if (ts3Functions.getChannelVariableAsString(sch, id, CHANNEL_NAME, &name) != ERROR_ok) {
            logError("Error getting channel name", sch);
            *data = NULL; return;
        }
        break;
    case PLUGIN_CLIENT:
        if (ts3Functions.getClientVariableAsString(sch, (anyID)id, CLIENT_NICKNAME, &name) != ERROR_ok) {
            logError("Error getting client nickname", sch);
            *data = NULL; return;
        }
        break;
    default:
        /* IMPORTANT: tell TS3 to ignore by setting *data, not data */
        *data = NULL; return;
    }

    *data = (char*)malloc(INFODATA_BUFSIZE * sizeof(char));
    if (*data) {
        snprintf(*data, INFODATA_BUFSIZE, "The nickname is [I]\"%s\"[/I]", name);
    }
    ts3Functions.freeMemory(name);
}

/* Client will call this to free memory from infoData/initMenus */
void ts3plugin_freeMemory(void* data) { free(data); }

/* 1 = request autoload, 0 = not */
int ts3plugin_requestAutoload() { return 0; }

/************************** TeamSpeak callbacks (subset) ***************************/

void ts3plugin_onConnectStatusChangeEvent(uint64 sch, int newStatus, unsigned int errorNumber)
{
    if (newStatus == STATUS_CONNECTION_ESTABLISHED) {
        char* s;
        char  msg[1024];
        anyID myID;
        uint64* ids;
        size_t i;
        unsigned int error;

        if (ts3Functions.getClientLibVersion(&s) == ERROR_ok) {
            snprintf(msg, sizeof(msg), "PLUGIN: Client lib version: %s", s);
            logInfo(msg, sch);
            ts3Functions.freeMemory(s);
        }
        else {
            ts3Functions.logMessage("Error querying client lib version", LogLevel_ERROR, "Plugin", sch);
            return;
        }

        snprintf(msg, sizeof(msg), "Plugin %s, Version %s, Author: %s", ts3plugin_name(), ts3plugin_version(), ts3plugin_author());
        ts3Functions.logMessage(msg, LogLevel_INFO, "Plugin", sch);

        if ((error = ts3Functions.getServerVariableAsString(sch, VIRTUALSERVER_NAME, &s)) != ERROR_ok) {
            if (error != ERROR_not_connected) {
                ts3Functions.logMessage("Error querying server name", LogLevel_ERROR, "Plugin", sch);
            }
            return;
        }
        snprintf(msg, sizeof(msg), "PLUGIN: Server name: %s", s);
        logInfo(msg, sch);
        ts3Functions.freeMemory(s);

        if (ts3Functions.getServerVariableAsString(sch, VIRTUALSERVER_WELCOMEMESSAGE, &s) != ERROR_ok) {
            ts3Functions.logMessage("Error querying server welcome message", LogLevel_ERROR, "Plugin", sch);
            return;
        }
        snprintf(msg, sizeof(msg), "PLUGIN: Server welcome: %s", s);
        logInfo(msg, sch);
        ts3Functions.freeMemory(s);

        if (ts3Functions.getClientID(sch, &myID) != ERROR_ok) {
            ts3Functions.logMessage("Error querying client ID", LogLevel_ERROR, "Plugin", sch);
            return;
        }
        if (ts3Functions.getClientSelfVariableAsString(sch, CLIENT_NICKNAME, &s) != ERROR_ok) {
            ts3Functions.logMessage("Error querying client nickname", LogLevel_ERROR, "Plugin", sch);
            return;
        }
        snprintf(msg, sizeof(msg), "PLUGIN: My client ID = %d, nickname = %s", myID, s);
        logInfo(msg, sch);
        ts3Functions.freeMemory(s);

        if (ts3Functions.getChannelList(sch, &ids) != ERROR_ok) {
            ts3Functions.logMessage("Error getting channel list", LogLevel_ERROR, "Plugin", sch);
            return;
        }
        logInfo("PLUGIN: Available channels:", sch);
        for (i = 0; ids[i]; i++) {
            if (ts3Functions.getChannelVariableAsString(sch, ids[i], CHANNEL_NAME, &s) != ERROR_ok) {
                ts3Functions.logMessage("Error querying channel name", LogLevel_ERROR, "Plugin", sch);
                ts3Functions.freeMemory(ids);
                return;
            }
            snprintf(msg, sizeof(msg), "PLUGIN: Channel ID = %llu, name = %s", (unsigned long long)ids[i], s);
            logInfo(msg, sch);
            ts3Functions.freeMemory(s);
        }
        ts3Functions.freeMemory(ids);

        if (ts3Functions.getServerConnectionHandlerList(&ids) != ERROR_ok) {
            ts3Functions.logMessage("Error getting server list", LogLevel_ERROR, "Plugin", sch);
            return;
        }
        logInfo("PLUGIN: Existing server connection handlers:", sch);
        for (i = 0; ids[i]; i++) {
            if ((error = ts3Functions.getServerVariableAsString(ids[i], VIRTUALSERVER_NAME, &s)) != ERROR_ok) {
                if (error != ERROR_not_connected) {
                    ts3Functions.logMessage("Error querying server name", LogLevel_ERROR, "Plugin", sch);
                }
                continue;
            }
            snprintf(msg, sizeof(msg), "- %llu - %s", (unsigned long long)ids[i], s);
            logInfo(msg, sch);
            ts3Functions.freeMemory(s);
        }
        ts3Functions.freeMemory(ids);
    }
}

int ts3plugin_onServerErrorEvent(uint64 sch, const char* errorMessage, unsigned int error, const char* returnCode, const char* extraMessage)
{
    char buf[512];
    snprintf(buf, sizeof(buf), "PLUGIN: onServerErrorEvent %llu %s %u %s",
        (unsigned long long)sch, errorMessage ? errorMessage : "", error, returnCode ? returnCode : "");
    logWarn(buf, sch);
    if (returnCode) {
        return 1; /* tell client we handled it (same as your original) */
    }
    return 0;
}

int ts3plugin_onTextMessageEvent(uint64 sch, anyID targetMode, anyID toID, anyID fromID, const char* fromName, const char* fromUID, const char* message, int ffIgnored)
{
    if (ffIgnored) return 0;
    char buf[512];
    snprintf(buf, sizeof(buf), "PLUGIN: onTextMessageEvent %llu %d %d %s %s %d",
        (unsigned long long)sch, targetMode, fromID, fromName ? fromName : "", message ? message : "", ffIgnored);
    logInfo(buf, sch);
    return 0;
}

void ts3plugin_onTalkStatusChangeEvent(uint64 sch, int status, int isReceivedWhisper, anyID clientID)
{
    char name[512];
    if (ts3Functions.getClientDisplayName(sch, clientID, name, 512) == ERROR_ok) {
        char buf[600];
        snprintf(buf, sizeof(buf), "--> %s %s talking", name, (status == STATUS_TALKING) ? "starts" : "stops");
        logInfo(buf, sch);
    }
}

/* Keep your remaining callbacks as-is or empty stubs */
