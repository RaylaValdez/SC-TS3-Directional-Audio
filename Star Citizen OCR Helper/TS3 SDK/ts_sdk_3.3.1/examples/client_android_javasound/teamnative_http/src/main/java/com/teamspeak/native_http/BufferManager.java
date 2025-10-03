package com.teamspeak.native_http;

import java.nio.ByteBuffer;

/**
 * Maintains a direct <code>ByteBuffer</code> backed by a native C++ string,
 * which can be grown on demand.
 */
class BufferManager {
    /**
     * Exception thrown by various methods if the requested buffer size is too large.
     */
    public static class BufferTooLargeException extends RuntimeException {
        public BufferTooLargeException(String message) {
            super(message);
        }
    }

    /**
     * Constructor specifying the initial buffer capacity.
     *
     * @param initialCapacity  the initial buffer capacity and backing string size.
     *                         Must be positive and not larger than <code>getMaxStringSize()</code>.
     *
     * @throws IllegalArgumentException if <code>initialCapacity</code> is not positive
     * @throws BufferTooLargeException  if <code>initialCapacity</code> is too large
     *                                  or allocation of the backing string fails
     */
    public BufferManager(long initialCapacity) {
        if (initialCapacity <= 0)
            throw new IllegalArgumentException("initialCapacity not positive");
        else if (initialCapacity > getMaxStringSize())
            throw new BufferTooLargeException("initialCapacity too large");
        mCxxString = createString(initialCapacity);
        mBuffer = getNewBuffer(mCxxString);
    }

    /**
     * Returns the current buffer. This buffer becomes invalid and may not be used anymore
     * after the next call to <code>growBuffer</code> or <code>finalizeAndGetCxxString</code>.
     *
     * @return the current buffer
     */
    public ByteBuffer getBuffer() {
        return mBuffer;
    }

    /**
     * Increases the size of the backing C++ string and creates a new <code>ByteBuffer</code>,
     * partially filled with the old string contents, ready for writing.
     * Should be called only when the buffer is full. The old buffer becomes invalid
     * and may not be used after the call, the new buffer must be obtained with <code>getBuffer</code>.
     * The new buffer has twice the capacity of the old one, unless this would be larger than
     * <code>getMaxStringSize()</code> or any upper bound specified by <code>maxCapacity</code>.
     * This method must not be called after <code>finalizeAndGetCxxString</code> or
     * <code>finalizeAndCleanup</code> have been called.
     *
     * @param maxCapacity  an upper bound to the new buffer capacity. May be 0, in which case it
     *                     imposes no bound.
     *
     * @return the new buffer
     *
     * @throws BufferTooLargeException  if the buffer cannot be grown at all without violating the
     *                                  size restrictions, or if reallocation of the backing string fails
     * @throws IllegalArgumentException if <code>maxCapacity</code> is negative
     * @throws IllegalStateException    if one of the finalization methods has already been called
     */
    public ByteBuffer growBuffer(long maxCapacity) {
        if (mCxxString == 0)
            throw new IllegalStateException("already finalized");
        if (maxCapacity < 0)
            throw new IllegalArgumentException("maxCapacity is negative");
        long maxStringSize = getMaxStringSize();
        if (maxCapacity > maxStringSize || maxCapacity == 0)
            maxCapacity = maxStringSize;
        if (mBuffer.capacity() >= maxCapacity)
            throw new BufferTooLargeException("maximum buffer capacity reached");
        int oldSize = mBuffer.capacity();
        growString(mCxxString, maxCapacity);
        mBuffer = getNewBuffer(mCxxString);
        mBuffer.limit(mBuffer.capacity());
        mBuffer.position(oldSize);
        return mBuffer;
    }

    /**
     * Resizes the backing C++ string according to the current position of the buffer and
     * returns a pointer to it. The buffer becomes invalid and may not be used after the call.
     * The <code>BufferManager</code> object effectively can not be used anymore after the call.
     *
     * @return a pointer to the backing C++ string, cast to a <code>long</code> value
     *
     * @throws IllegalStateException if this method or <code>finalizeAndCleanup</code> have
     *                               already been called
     */
    public long finalizeAndGetCxxString() {
        if (mCxxString == 0)
            throw new IllegalStateException("already finalized");
        resizeString(mCxxString, mBuffer.position());
        long string = mCxxString;
        mCxxString = 0;
        return string;
    }

    /**
     * Releases the resources used by the <code>BufferManager</code> object. The buffer becomes
     * invalid and may not be used anymore, and the object effectively can not be used anymore
     * after the call.
     * This method may be called even when it or <code>finalizeAndGetCxxString</code> have been
     * called before.
     */
    public void finalizeAndCleanup() {
        if (mCxxString != 0)
            destroyCxxString(mCxxString);
        mCxxString = 0;
    }

    /**
     * Deallocates the given C++ string.
     *
     * @param string a pointer to the C++ string to deallocate, cast to a <code>long</code> value;
     *               may be 0, in which case the call is a no-op.
     */
    public static native void destroyCxxString(long string);

    /**
     * The maximum size of the backing C++ string the implementation supports.
     *
     * @return the maximum backing string size
     */
    public static native long getMaxStringSize();


    // Allocates a string of given size and returns a pointer to it.
    // Assumes that size <= getMaxStringSize().
    private static native long createString(long size);

    // Creates a new (empty) direct ByteBuffer backed by the contents
    // of the given C++ string.
    private static native ByteBuffer getNewBuffer(long string);

    // Increases the size of the given C++ string.
    // Assumes that old size < maxSize <= getMaxStringSize().
    private static native void growString(long string, long maxSize);

    // Resizes the given C++ string to the given size.
    // Assumes that size <= getMaxStringSize().
    private static native void resizeString(long string, long size);

    // Helper for testing.
    // Copies the contents of the given C++ string
    // to a byte array and deallocates the string.
    static native byte[] toByteArray(long string);

    private long       mCxxString; // native pointer to C++ string
    private ByteBuffer mBuffer;    // current buffer
}
