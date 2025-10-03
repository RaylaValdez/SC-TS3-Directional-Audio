package com.teamspeak.native_http;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;


@RunWith(AndroidJUnit4.class)
public class BufferManagerTest {
    // make sure native methods are loaded
    @BeforeClass
    public static void loadNativeLib() throws Exception {
        System.loadLibrary("teamnative_http_java_unittests_native_lib");
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testConstructionAndFinalization() throws Exception {
        BufferManager bufferManager = new BufferManager(10);
        try {
            ByteBuffer buffer = bufferManager.getBuffer();

            assertThat(buffer.capacity(), is(10));
            assertThat(buffer.limit(), is(10));
            assertThat(buffer.position(), is(0));
        } finally {
            bufferManager.finalizeAndCleanup();
        }
    }

    @Test
    public void testWriting() throws Exception {
        final byte[] data = { 1, 2, 3 };

        BufferManager bufferManager = new BufferManager(10);
        try {
            ByteBuffer buffer = bufferManager.getBuffer();

            buffer.put(data);
            long string = bufferManager.finalizeAndGetCxxString();

            byte[] result = BufferManager.toByteArray(string);
            assertThat(result, is(equalTo(data)));
        } finally {
            bufferManager.finalizeAndCleanup();
        }
    }

    @Test
    public void testGrowBuffer() throws Exception {
        final byte[] data = { 1, 2, 3, 4, 5 };
        final int initialCapacity = 4;

        BufferManager bufferManager = new BufferManager(initialCapacity);
        try {
            ByteBuffer buffer = bufferManager.getBuffer();
            buffer.put(data, 0, initialCapacity);

            buffer = bufferManager.growBuffer(0);

            assertThat(buffer.capacity(), is(equalTo(2 * initialCapacity)));
            assertThat(buffer.limit(), is(equalTo(buffer.capacity())));
            assertThat(buffer.position(), is(equalTo(initialCapacity)));

            buffer.put(data, initialCapacity, 1);
            long string = bufferManager.finalizeAndGetCxxString();

            byte[] result = BufferManager.toByteArray(string);
            assertThat(result, is(equalTo(data)));
        } finally {
            bufferManager.finalizeAndCleanup();
        }
    }

    @Test
    public void testGrowBufferMaxCapacity() throws Exception {
        final byte[] data = { 1, 2, 3, 4 };

        BufferManager bufferManager = new BufferManager(data.length);
        try {
            ByteBuffer buffer = bufferManager.getBuffer();
            buffer.put(data);

            buffer = bufferManager.growBuffer(data.length + 1);

            assertThat(buffer.capacity(), is(equalTo(data.length + 1)));
            assertThat(buffer.limit(), is(equalTo(buffer.capacity())));
            assertThat(buffer.position(), is(equalTo(data.length)));
        } finally {
            bufferManager.finalizeAndCleanup();
        }
    }

    @Test
    public void testGrowBufferTooLarge() throws Exception {
        final byte[] data = { 1, 2, 3, 4 };

        BufferManager bufferManager = new BufferManager(data.length);
        try {
            ByteBuffer buffer = bufferManager.getBuffer();
            buffer.put(data);

            expectedException.expect(BufferManager.BufferTooLargeException.class);
            bufferManager.growBuffer(data.length);
        } finally {
            bufferManager.finalizeAndCleanup();
        }
    }

    @Test
    public void testDestroyCxxString() throws Exception {
        BufferManager bufferManager = new BufferManager(4);
        try {
            long string = bufferManager.finalizeAndGetCxxString();
            BufferManager.destroyCxxString(string);
        } finally {
            bufferManager.finalizeAndCleanup();
        }
    }

    @Test
    public void testDestroyCxxStringNull() throws Exception {
        BufferManager.destroyCxxString(0);
    }

    @Test
    public void testGetMaxStringSize() throws Exception {
        assertThat(BufferManager.getMaxStringSize(), is(greaterThan(0L)));
    }
}
