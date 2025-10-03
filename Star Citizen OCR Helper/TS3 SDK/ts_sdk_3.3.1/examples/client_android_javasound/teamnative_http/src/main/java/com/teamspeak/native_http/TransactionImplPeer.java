package com.teamspeak.native_http;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;

/**
 * An active class performing the HTTP transaction.
 */
public class TransactionImplPeer implements Runnable {
    /**
     * Constructor. Stores all parameters for later use by <code>run()</code>, then
     * runs the instance in a new thread.
     */
    public TransactionImplPeer(long peer, int method, String url, Map<byte[], byte[]> headers, byte[] userAgent,
            byte[] contentType, ByteBuffer body, boolean keepAlive, String proxyHost, int proxyPort, int timeout,
            long maxResponseSize) {
        mPeer = peer;

        mMethod = method;
        mUrl = url;
        mHeaders = headers;
        mUserAgent = userAgent;
        mContentType = contentType;
        mBody = body;
        mKeepAlive = keepAlive;
        mProxyHost = proxyHost;
        mProxyPort = proxyPort;
        mTimeout = timeout;
        mMaxResponseSize = maxResponseSize;

        mThread = new Thread(this);
        mThread.start();
    }

    /**
     * Cancels the ongoing transaction.
     */
    public void cancel() {
        mThread.interrupt();
    }

    //
    // Exception class used below to signal that an error occurred and the
    // completion routine
    // has already been called (with appropriate error information).
    //
    private static class TransactionDoneException extends Exception {
    }

    /**
     * Performs the actual transaction.
     */
    @Override
    public void run() {
        // Log.d(TAG, "Beginning transaction " + Long.toHexString(mPeer) + ": " +
        // mMethodNames[mMethod] + " " + mUrl);
        try {
            HttpURLConnection connection = getConnection();
            setRequestParametersAndHeaders(connection);

            // connect
            try {
                connection.connect();
            } catch (SocketTimeoutException e) {
                // XXX: Is this ever hit?
                callCompletion(ErrorCodes.CONNECT_TIMEOUT, "");
                return;
            }

            sendRequestBody(connection);

            long body = readResponseBody(connection);
            try {
                callCompletion(ErrorCodes.OK, "", connection.getResponseCode(), connection.getResponseMessage(),
                        connection.getHeaderFields(), body);
            } finally {
                BufferManager.destroyCxxString(body);
            }
        } catch (SocketTimeoutException e) {
            callCompletion(ErrorCodes.TIMEOUT, "");
        } catch (ClosedByInterruptException | InterruptedIOException e) {
            Thread.interrupted(); // clear interrupted flag
            callCompletion(ErrorCodes.CANCELED, "");
        } catch (UnknownHostException e) {
            callCompletion(ErrorCodes.RESOLVE_ERROR, e.toString());
        } catch (ConnectException e) {
            callCompletion(ErrorCodes.CONNECT_ERROR, e.toString());
        } catch (SSLException e) {
            callCompletion(ErrorCodes.SSL_ERROR, e.toString());
        } catch (IOException e) {
            callCompletion(ErrorCodes.IO_ERROR, e.toString());
        } catch (BufferManager.BufferTooLargeException e) {
            callCompletion(ErrorCodes.RESPONSE_TOO_LARGE, "");
        } catch (TransactionDoneException e) {
            // nothing to do, completion has already been called
        } catch (Exception e) {
            callCompletion(ErrorCodes.UNKNOWN, e.toString());
        }
    }

    //
    // Opens a HttpURLConnection using URL and proxy information.
    //
    private HttpURLConnection getConnection() throws IOException, TransactionDoneException {
        URL url = new URL(mUrl);

        // translate proxy
        Proxy proxy = Proxy.NO_PROXY;
        if (mProxyHost != null) {
            try {
                InetSocketAddress sa = new InetSocketAddress(mProxyHost, mProxyPort);
                proxy = new Proxy(Proxy.Type.HTTP, sa);
            } catch (Exception e) {
                callCompletion(ErrorCodes.CONNECT_ERROR, "Invalid proxy: " + e.toString());
                throw new TransactionDoneException();
            }
        }

        return (HttpURLConnection) url.openConnection(proxy);
    }

    //
    // Sets the request method, headers, and various other parameters.
    //
    private void setRequestParametersAndHeaders(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod(mMethodNames[mMethod]);

        for (Map.Entry<byte[], byte[]> h : mHeaders.entrySet()) {
            String key = new String(h.getKey(), ISO_8859_1);
            String value = new String(h.getValue(), ISO_8859_1);
            connection.addRequestProperty(key, value);
        }

        if (mUserAgent != null)
            connection.addRequestProperty("User-Agent", new String(mUserAgent, ISO_8859_1));

        if (mContentType != null)
            connection.addRequestProperty("Content-Type", new String(mContentType, ISO_8859_1));

        if (!mKeepAlive)
            connection.addRequestProperty("Connection", "close");

        connection.setConnectTimeout(mTimeout * 1000);
        connection.setReadTimeout(mTimeout * 1000);

        connection.setAllowUserInteraction(false);
        connection.setInstanceFollowRedirects(false);

        if (mBody != null)
            connection.setFixedLengthStreamingMode(mBody.limit());
    }

    //
    // Sends the body of the request, if present.
    //
    private void sendRequestBody(HttpURLConnection connection) throws IOException {
        if (mBody != null) {
            WritableByteChannel outputChannel = Channels.newChannel(connection.getOutputStream());
            try {
                while (mBody.hasRemaining())
                    outputChannel.write(mBody);
            } finally {
                outputChannel.close();
            }
        }
    }

    //
    // Reads the body of the response message and returns a pointer to a C++ string
    // (see BufferManager).
    //
    private long readResponseBody(HttpURLConnection connection)
            throws IOException, BufferManager.BufferTooLargeException {
        InputStream bodyStream = (connection.getResponseCode() < 400) ? connection.getInputStream()
                : connection.getErrorStream();
        if (bodyStream == null)
            return 0;
        ReadableByteChannel bodyChannel = Channels.newChannel(bodyStream);
        try {
            long initialBufferCapacity = connection.getContentLength();
            if (initialBufferCapacity <= 0)
                initialBufferCapacity = 8192;
            BufferManager bufferManager = new BufferManager(initialBufferCapacity);
            try {
                ByteBuffer bodyBuffer = bufferManager.getBuffer();
                while (bodyChannel.read(bodyBuffer) >= 0) {
                    if (!bodyBuffer.hasRemaining())
                        bodyBuffer = bufferManager.growBuffer(mMaxResponseSize);
                }

                return bufferManager.finalizeAndGetCxxString();

            } finally {
                bufferManager.finalizeAndCleanup();
            }
        } finally {
            bodyChannel.close();
        }
    }

    //
    // Calls the completion routine with Error and Response parameters as given.
    //
    private void callCompletion(int errorCode, String errorMessage, int statusCode, String statusMessage,
            Map<String, List<String>> headers, long body) {
        // Log.d(TAG, "Finishing transaction " + Long.toHexString(mPeer) + ": [" +
        // errorCode
        // + (errorMessage.isEmpty() ? "] " : " (" + errorMessage + ")] ") + statusCode
        // + " " + statusMessage);
        callCompletion(mPeer, errorCode, errorMessage.getBytes(), statusCode, statusMessage.getBytes(ISO_8859_1),
                translateResponseHeaders(headers), body);
    }

    //
    // Calls the completion routine with the given error information and a null
    // Response.
    //
    private void callCompletion(int errorCode, String errorMessage) {
        // Log.d(TAG, "Finishing transaction " + Long.toHexString(mPeer) + ": [" +
        // errorCode
        // + (errorMessage.isEmpty() ? "] " : " (" + errorMessage + ")] "));
        callCompletion(mPeer, errorCode, errorMessage.getBytes());
    }

    //
    // "Flattens" headers map to alternating sequence of keys and values, changing
    // keys to lowercase and joining
    // values list with commas as delimiters.
    //
    private byte[][] translateResponseHeaders(Map<String, List<String>> headers) {
        ArrayList<byte[]> sequence = new ArrayList<>();
        for (Map.Entry<String, List<String>> h : headers.entrySet()) {
            String key = h.getKey();
            if (key != null) {
                sequence.add(key.toLowerCase().getBytes(ISO_8859_1));
                String value = joinStringList(h.getValue());
                sequence.add(value.getBytes(ISO_8859_1));
            }
        }
        return sequence.toArray(RESPONSE_HEADERS_TEMPLATE);
    }

    //
    // Joins the given list of strings with commas as delimiters.
    //
    private String joinStringList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : list) {
            if (!first)
                sb.append(", ");
            sb.append(s);
            first = false;
        }
        return sb.toString();
    }

    private static native void callCompletion(long peer, int errorCode, byte[] errorMessage, int statusCode,
            byte[] statusMessage, byte[][] headers, long body);

    private static native void callCompletion(long peer, int errorCode, byte[] errorMessage);

    // logging tag
    private static final String TAG = "NativeHTTP";

    // possible method values
    // ATTENTION: must be in sync with enum Transaction::Impl::Method!
    private static final int GET = 0;
    private static final int POST = 1;
    private static final int PUT = 2;

    // string representations of the above constants
    private static final String[] mMethodNames = { "GET", "POST", "PUT" };

    // no java.nio.Charset.StandardCharsets in API < 19
    private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    // for passing to toArray
    private static final byte[][] RESPONSE_HEADERS_TEMPLATE = {};

    // a pointer to the corresponding Transaction::Impl C++ object, cast to long
    private long mPeer;

    // the thread running the actual transaction
    private Thread mThread;

    // stored transaction parameters
    private int mMethod;
    private String mUrl;
    private Map<byte[], byte[]> mHeaders;
    private byte[] mUserAgent;
    private byte[] mContentType;
    private ByteBuffer mBody;
    private boolean mKeepAlive;
    private String mProxyHost;
    private int mProxyPort;
    private int mTimeout;
    private long mMaxResponseSize;
}
