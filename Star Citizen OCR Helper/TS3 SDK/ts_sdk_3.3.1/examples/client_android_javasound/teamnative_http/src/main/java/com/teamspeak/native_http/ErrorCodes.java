package com.teamspeak.native_http;

/**
 * Contains constants for all possible error codes.
 */
class ErrorCodes {
    // ATTENTION: These must be in sync with enum Android_Error_Code!
    public static final int OK                 = 0;
    public static final int UNKNOWN            = 1;
    public static final int RESOLVE_ERROR      = 2;
    public static final int CONNECT_ERROR      = 3;
    public static final int CONNECT_TIMEOUT    = 4;
    public static final int IO_ERROR           = 5;
    public static final int TIMEOUT            = 6;
    public static final int CANCELED           = 7;
    public static final int RESPONSE_TOO_LARGE = 8;
    public static final int SSL_ERROR          = 9;
}
