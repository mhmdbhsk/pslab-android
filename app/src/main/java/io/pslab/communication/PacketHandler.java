package io.pslab.communication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.pslab.others.ScienceLabCommon;

/**
 * Created by viveksb007 on 28/3/17.
 */

public class PacketHandler {

    private static final String TAG = "PacketHandler";
    private final int BUFSIZE = 10000;
    private byte[] buffer = new byte[BUFSIZE];
    private boolean loadBurst, connected;
    private int inputQueueSize = 0, BAUD = 1000000;
    private CommunicationHandler mCommunicationHandler = null;
    public static String version = "";
    private CommandsProto mCommandsProto;
    private int timeout = 500, VERSION_STRING_LENGTH = 8, FW_VERSION_LENGTH = 3;
    public static int PSLAB_FW_VERSION = 0;
    ByteBuffer burstBuffer = ByteBuffer.allocate(2000);
    private SocketClient socketClient;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    public PacketHandler(int timeout, CommunicationHandler communicationHandler) {
        this.loadBurst = false;
        this.connected = false;
        this.timeout = timeout;
        this.mCommandsProto = new CommandsProto();
        socketClient = SocketClient.getInstance();
        if (communicationHandler != null) {
            this.mCommunicationHandler = communicationHandler;
        }
        connected = (ScienceLabCommon.isWifiConnected() || mCommunicationHandler.isConnected());
    }

    public boolean isConnected() {
        connected = (mCommunicationHandler.isConnected() || ScienceLabCommon.isWifiConnected());
        return connected;
    }

    public String getVersion() {
        try {
            sendByte(mCommandsProto.COMMON);
            sendByte(mCommandsProto.GET_VERSION);
            // Read "<PSLAB Version String>\n"
            commonRead(VERSION_STRING_LENGTH + 1);
            // Only use first line, just like in the Python implementation.
            version = new BufferedReader(
                    new InputStreamReader(
                            new ByteArrayInputStream(buffer, 0, VERSION_STRING_LENGTH),
                            StandardCharsets.UTF_8))
                    .readLine();
        } catch (IOException e) {
            Log.e("Error in Communication", e.toString());
        }
        return version;
    }

    public int getFirmwareVersion() {
        try {
            sendByte(mCommandsProto.COMMON);
            sendByte(mCommandsProto.GET_FW_VERSION);
            int numByteRead = commonRead(FW_VERSION_LENGTH);
            if (numByteRead == 1) {
                return 2;
            } else {
                return buffer[0];
            }
        } catch (IOException e) {
            Log.e("Error in Communication", e.toString());
        }
        return 0;
    }

    public String readLine() {
        String line = "";
        try {
            commonRead(CommunicationHandler.DEFAULT_READ_BUFFER_SIZE);
            line = new BufferedReader(
                    new InputStreamReader(
                            new ByteArrayInputStream(buffer, 0, CommunicationHandler.DEFAULT_READ_BUFFER_SIZE),
                            StandardCharsets.UTF_8))
                    .readLine();
            return line;
        } catch (IOException e) {
            Log.e("Error in Communication", e.toString());
        }
        return line;
    }

    public void sendByte(int val) throws IOException {
        if (!connected) {
            throw new IOException("Device not connected");
        }
        if (!loadBurst) {
            try {
                commonWrite(new byte[]{(byte) (val & 0xff)});
            } catch (IOException | NullPointerException e) {
                Log.e("Error in sending byte", e.toString());
                e.printStackTrace();
            }
        } else {
            burstBuffer.put((byte) (val & 0xff));
        }
    }

    public void sendInt(int val) throws IOException {
        if (!connected) {
            throw new IOException("Device not connected");
        }
        if (!loadBurst) {
            try {
                commonWrite(new byte[]{(byte) (val & 0xff), (byte) ((val >> 8) & 0xff)});
            } catch (IOException e) {
                Log.e("Error in sending int", e.toString());
                e.printStackTrace();
            }
        } else {
            burstBuffer.put(new byte[]{(byte) (val & 0xff), (byte) ((val >> 8) & 0xff)});
        }
    }

    public int getAcknowledgement() {
        /*
        fetches the response byte
        1 SUCCESS
        2 ARGUMENT_ERROR
        3 FAILED
        used as a handshake
        */
        if (loadBurst) {
            inputQueueSize++;
            return 1;
        } else {
            try {
                commonRead(1);
                return buffer[0];
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                return 3;
            }
        }
    }

    public byte getByte() {
        try {
            int numByteRead = commonRead(1);
            if (numByteRead == 1) {
                return buffer[0];
            } else {
                Log.e(TAG, "Error in reading byte");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    int getVoltageSummation() {
        try {
            // Note : bytesToBeRead has to be +1 than the requirement
            int numByteRead = commonRead(3);
            if (numByteRead == 3) {
                return (buffer[0] & 0xff) | ((buffer[1] << 8) & 0xff00);
            } else {
                Log.e(TAG, "Error in reading byte");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getInt() {
        try {
            int numByteRead = commonRead(2);
            if (numByteRead == 2) {
                // LSB is read first
                return (buffer[0] & 0xff) | ((buffer[1] << 8) & 0xff00);
            } else {
                Log.e(TAG, "Error in reading byte");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long getLong() {
        try {
            int numByteRead = commonRead(4);
            if (numByteRead == 4) {
                // C++ has long of 4-bytes but in Java int has 4-bytes
                // refer "https://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vice-versa" for Endian
                return ByteBuffer.wrap(Arrays.copyOfRange(buffer, 0, 4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
            } else {
                Log.e(TAG, "Error in reading byte");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean waitForData() {
        return false;
    }

    public int read(byte[] dest, int bytesToRead) throws IOException {
        int numBytesRead = commonRead(bytesToRead);
        for (int i = 0; i < bytesToRead; i++) {
            dest[i] = buffer[i];
        }
        if (numBytesRead == bytesToRead) {
            return numBytesRead;
        } else {
            Log.e(TAG, "Error in packetHandler Reading");
        }
        return -1;
    }

    public byte[] sendBurst() {
        try {
            commonWrite(burstBuffer.array());
            burstBuffer.clear();
            loadBurst = false;
            int bytesRead = commonRead(inputQueueSize);
            inputQueueSize = 0;
            return Arrays.copyOfRange(buffer, 0, bytesRead);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[]{-1};
    }

    public int commonRead(int bytesToRead) throws IOException {
        final int[] bytesRead = {0};
        if (mCommunicationHandler != null && mCommunicationHandler.isConnected()) {
            bytesRead[0] = mCommunicationHandler.read(buffer, bytesToRead, timeout);
        } else if (ScienceLabCommon.isWifiConnected()) {
            Future<Void> future = executor.submit(() -> {
                try {
                    socketClient.read(bytesToRead);
                    System.arraycopy(socketClient.getReceivedData(), 0, buffer, 0, bytesToRead);
                    bytesRead[0] = bytesToRead;
                } catch (Exception e) {
                    Log.e(TAG, "Error reading data over ESP");
                }
                return null;
            });
            try {
                future.get();
            } catch (Exception e) {
                throw new IOException("Error reading data", e);
            }
        }
        return bytesRead[0];
    }

    public void commonWrite(byte[] data) throws IOException {
        if (mCommunicationHandler != null && mCommunicationHandler.isConnected()) {
            mCommunicationHandler.write(data, timeout);
        } else if (ScienceLabCommon.isWifiConnected()) {
            Future<Void> future = executor.submit(() -> {
                try {
                    socketClient.write(data);
                } catch (Exception e) {
                    Log.e(TAG, "Error writing data over ESP");
                }
                return null;
            });
            try {
                future.get();
            } catch (Exception e) {
                throw new IOException("Error writing data", e);
            }
        }
    }

    public void close() {
        try {
            if (mCommunicationHandler != null) {
                mCommunicationHandler.close();
            } else {
                socketClient.closeConnection();
            }
            executor.shutdown();
        } catch (Exception e) {
            Log.e(TAG, "Error closing connection");
        }
    }
}