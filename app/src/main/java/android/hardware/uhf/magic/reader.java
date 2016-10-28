package android.hardware.uhf.magic;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.regex.Pattern;


public class reader {
    static public Handler m_handler = null;
    static public String m_strPCEPC = "";
    static Boolean m_bASYC = false, m_bLoop = false, m_bOK = false;
    static byte[] m_buf = new byte[10240];
    static int m_nCount = 0, m_nReSend = 0, m_nread = 0;
    static int msound = 0;
    ;
    static SoundPool mSoundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);

    static {
        System.loadLibrary("uhf-tools");
        msound = mSoundPool.load("/system/media/audio/notifications/Argon.ogg",
                1);
    }

    static public native void init(String strpath);

    static public native int Open(String strpath);

    static public native int Read(byte[] pout, int nStart, int nCount);

    static public native void Close();

    static public native void Clean();

    static public native int Inventory();

    static public native int MultiInventory(int ntimes);

    static public native int StopMultiInventory();

    static public native int Select(byte selPa, int nPTR, byte nMaskLen,
                                    byte turncate, byte[] mask);

    static public native int SetSelect(byte data);

    static public native int ReadLable(byte[] password, int nUL,
                                       byte[] PCandEPC, byte membank, int nSA, int nDL);

    static public native int WriteLable(byte[] password, int nUL,
                                        byte[] PCandEPC, byte membank, int nSA, int nDL, byte[] data);

    static public native int Lock(byte[] password, int nUL, byte[] PCandEPC,
                                  int nLD);

    static public native int Kill(byte[] password, int nUL, byte[] EPC);

    static public int KillLables(byte[] password, int nUL, byte[] EPC) {
        Clean();
        int nret = Kill(password, nUL, EPC);
        if (!m_bASYC) {
            StartASYCKilllables();
        }
        return nret;
    }

    static public native int Query();

    static public native int SetQuery(int nParam);

    static public native int SetFrequency(byte region);

    static public native int SetChannel(byte channel);

    static public native int GetChannel();

    static public native int SetAutoFrequencyHopping(byte auto);

    static public native int GetTransmissionPower();

    static public native int SetTransmissionPower(int nPower);

    static public native int SetContinuousCarrier(byte bOn);

    static public native int GetParameter(byte[] bufout);

    static public native int SetParameter(byte bMixer, byte bIF, int nThrd);

    static public native int ScanJammer(byte[] bufout);

    static public native int TestRssi(byte[] bufout);

    static public native int SetIOParameter(byte p1, byte p2, byte p3,
                                            byte[] bufout);

    static public int InventoryLables() {
        int nret = Inventory();
        if (!m_bASYC) {
            StartASYClables();
        }
        return nret;
    }

    static public int InventoryLablesLoop() {
        int nret = Inventory();
        m_bLoop = true;
        if (!m_bASYC) {
            StartASYClables();
        }
        return nret;
    }

    static public void StopLoop() {
        m_bLoop = false;
    }

    static public int MultInventoryLables() {
        int nret = MultiInventory(65535);
        if (!m_bASYC) {
            StartASYClables();
        }
        return nret;
    }

    static public int ReadLables(byte[] password, int nUL, byte[] PCandEPC,
                                 byte membank, int nSA, int nDL) {
        int nret = 0;
        if (!m_bASYC) {
            Clean();
            nret = ReadLable(password, nUL, PCandEPC, membank, nSA, nDL);
            m_bOK = false;
            m_nReSend = 0;
            StartASYCReadlables();
            while ((!m_bOK) && (m_nReSend < 20)) {
                m_nReSend++;
                ReadLable(password, nUL, PCandEPC, membank, nSA, nDL);
            }
        }
        return nret;
    }

    static public int LockLables(byte[] password, int nUL, byte[] PCandEPC,
                                 int nLD) {
        Clean();
        int nret = Lock(password, nUL, PCandEPC, nLD);
        if (!m_bASYC) {
            StartASYCLocklables();
        }
        return nret;
    }

    static public int Writelables(byte[] password, int nUL, byte[] PCandEPC,
                                  byte membank, int nSA, int nDL, byte[] data) {
        Clean();
        int nret = WriteLable(password, nUL, PCandEPC, membank, nSA, nDL, data);
        if (!m_bASYC) {
            m_bOK = false;
            m_nReSend = 0;
            StartASYCWritelables();
            while ((!m_bOK) && (m_nReSend < 20)) {
                m_nReSend++;
                WriteLable(password, nUL, PCandEPC, membank, nSA, nDL, data);
            }
        }
        return nret;
    }

    static public int GetLockPayLoad(byte membank, byte Mask) {
        int nret = 0;
        switch (Mask) {
            case 0:
                switch (membank) {
                    case 0:
                        nret = 0x80000;
                        break;
                    case 1:
                        nret = 0x80200;
                        break;
                    case 2:
                        nret = 0xc0100;
                        break;
                    case 3:
                        nret = 0xc0300;
                        break;
                }
                break;
            case 1:
                switch (membank) {
                    case 0:
                        nret = 0x20000;
                        break;
                    case 1:
                        nret = 0x20080;
                        break;
                    case 2:
                        nret = 0x30040;
                        break;
                    case 3:
                        nret = 0x300c0;
                        break;
                }
                break;
            case 2:
                switch (membank) {
                    case 0:
                        nret = 0x8000;
                        break;
                    case 1:
                        nret = 0x8020;
                        break;
                    case 2:
                        nret = 0xc010;
                        break;
                    case 3:
                        nret = 0xc030;
                        break;
                }
                break;
            case 3:
                switch (membank) {
                    case 0:
                        nret = 0x2000;
                        break;
                    case 1:
                        nret = 0x2008;
                        break;
                    case 2:
                        nret = 0x3004;
                        break;
                    case 3:
                        nret = 0x300c;
                        break;
                }
                break;
            case 4:
                switch (membank) {
                    case 0:
                        nret = 0x0800;
                        break;
                    case 1:
                        nret = 0x0802;
                        break;
                    case 2:
                        nret = 0x0c01;
                        break;
                    case 3:
                        nret = 0x0c03;
                        break;
                }
                break;
        }
        return nret;
    }

    static void StartASYCKilllables() {
        m_bASYC = true;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                int nTemp = 0;
                m_nCount = 0;
                while (m_handler != null) {

                    nTemp = Read(m_buf, m_nCount, 1024);
                    m_nCount += nTemp;
                    if (nTemp == 0)
                        break;
                    String str = reader.BytesToString(m_buf, 0, m_nCount);
                    String[] substr = Pattern.compile("BB0165").split(str);
                    for (int i = 0; i < substr.length; i++) {
                        if (substr[i].length() >= 10) {
                            if (substr[i].substring(0, 10).equals("000100677E")) {
                                Message msg = new Message();
                                msg.what = 2;
                                msg.obj = "OK";
                                m_handler.sendMessage(msg);
                            }
                        }

                    }

                }

                m_bASYC = false;
            }
        });
        thread.start();
    }

    static void StartASYCLocklables() {
        m_bASYC = true;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                int nTemp = 0;
                m_nCount = 0;
                while (m_handler != null) {

                    nTemp = Read(m_buf, m_nCount, 1024);
                    m_nCount += nTemp;
                    if (nTemp == 0)
                        break;
                    String str = reader.BytesToString(m_buf, 0, m_nCount);
                    String[] substr = Pattern.compile("BB0182").split(str);
                    for (int i = 0; i < substr.length; i++) {
                        if (substr[i].length() >= 10) {
                            if (substr[i].substring(0, 10).equals("000100847E")) {
                                Message msg = new Message();
                                msg.what = 2;
                                msg.obj = "OK";
                                m_handler.sendMessage(msg);
                            }
                        }

                    }

                }

                m_bASYC = false;
            }
        });
        thread.start();
    }

    static void StartASYCWritelables() {
        m_bASYC = true;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                int nTemp = 0;
                m_nCount = 0;
                m_nread = 0;
                while (m_handler != null) {

                    nTemp = Read(m_buf, m_nCount, 1024);
                    m_nCount += nTemp;
                    if (nTemp == 0) {
                        m_nread++;
                        if (m_nread > 5)
                            break;
                    }
                    String str = reader.BytesToString(m_buf, 0, m_nCount);
                    Log.e("write", str);
                    String[] substr = Pattern.compile("BB0149").split(str);
                    for (int i = 0; i < substr.length; i++) {
                        if (substr[i].length() >= 10) {
                            if (substr[i].substring(0, 10).equals("0001004B7E")) {
                                m_bOK = true;
                                Message msg = new Message();
                                msg.what = 2;
                                msg.obj = "OK";
                                m_handler.sendMessage(msg);
                            }
                        }

                    }

                }

                m_bASYC = false;
            }
        });
        thread.start();
    }

    static void StartASYCReadlables() {
        m_bASYC = true;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                int nTemp = 0;
                m_nCount = 0;
                m_nread = 0;
                while (m_handler != null) {

                    nTemp = Read(m_buf, m_nCount, 1024);
                    m_nCount += nTemp;
                    if (nTemp == 0) {
                        m_nread++;
                        if (m_nread > 5)
                            break;
                    }
                    String str = reader.BytesToString(m_buf, 0, m_nCount);
                    Log.e("1111111", str);
                    String[] substr = Pattern.compile("BB0139").split(str);
                    for (int i = 0; i < substr.length; i++) {
                        // Log.e("222222",substr[i]);
                        if (substr[i].length() > 10) {
                            if (!substr[i].substring(0, 2).equals("BB")) {
                                m_bOK = true;
                                Message msg = new Message();
                                msg.what = (substr[i].length() - 8) / 2;
                                msg.obj = substr[i].substring(4,
                                        substr[i].length() - 4);
                                m_handler.sendMessage(msg);
                            }
                        }
                    }

                }

                m_bASYC = false;
            }
        });
        thread.start();
    }

    static void StartASYClables() {
        m_bASYC = true;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                int nTemp = 0, nIndex = 0;
                boolean tag_find = false;
                m_nCount = 0;
                m_nReSend = 0;
                nIndex = 0;
                while (m_handler != null) {
                    //nIndex = m_nCount;
                    nTemp = Read(m_buf, m_nCount, 10240 - m_nCount);
                    m_nCount += nTemp;
                    Log.e("777777777777777777", "count=" + m_nCount);
                    if (nTemp == 0) {

                        String str = reader.BytesToString(m_buf, nIndex,
                                m_nCount - nIndex);
                        Log.e("77777777777777", str);
                        String[] substr = Pattern.compile("BB0222").split(str);
                        // Log.e("9999999", "len=" + substr.length);
                        for (int i = 0; i < substr.length; i++) {
                            Log.e("777777", substr[i]);
                            if (substr[i].length() > 16) {
                                if (!substr[i].substring(0, 2).equals("BB")) {
                                    int nlen = Integer.valueOf(
                                            substr[i].substring(0, 4), 16);
                                    // Log.e("777777",substr[i].substring(0,4));
                                    if ((nlen > 3)
                                            && (nlen < (substr[i].length() - 6) / 2)) {
                                        Message msg = new Message();
                                        msg.what = (substr[i].length() - 12) / 2;
                                        msg.obj = substr[i].substring(6,
                                                nlen * 2);
                                        m_handler.sendMessage(msg);
                                        tag_find = true;
                                        m_bOK = true;
                                    }
                                }
                            }
                        }
                        if (tag_find) {

                            mSoundPool.play(msound, 1.0f, 1.0f, 0, 0, 1.0f);
                        }
                        if (m_bLoop) {
                            m_nCount = 0;
                            InventoryLablesLoop();
                            tag_find = false;
                        } else {
                            if ((m_nReSend < 20) && (!tag_find)) {
                                Inventory();
                                m_nReSend++;
                            } else
                                break;
                            tag_find = false;
                            Log.e("efsfsd", "m_nReSend=" + m_nReSend);
                        }

                        if (m_nCount >= 1024)
                            m_nCount = 0;
                    }
                }
                // Log.e("end", "quit");
                m_bASYC = false;
            }
        });
        thread.start();
    }

    public static byte[] stringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String BytesToString(byte[] b, int nS, int ncount) {
        String ret = "";
        int nMax = ncount > (b.length - nS) ? b.length - nS : ncount;
        for (int i = 0; i < nMax; i++) {
            String hex = Integer.toHexString(b[i + nS] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    public static int byteToInt(byte[] b) // byteToInt
    {
        int t2 = 0, temp = 0;
        for (int i = 3; i >= 0; i--) {
            t2 = t2 << 8;
            temp = b[i];
            if (temp < 0) {
                temp += 256;
            }
            t2 = t2 + temp;

        }
        return t2;

    }

    public static int byteToInt(byte[] b, int nIndex, int ncount) // byteToInt
    {
        int t2 = 0, temp = 0;
        for (int i = 0; i < ncount; i++) {
            t2 = t2 << 8;
            temp = b[i + nIndex];
            if (temp < 0) {
                temp += 256;
            }
            t2 = t2 + temp;

        }
        return t2;

    }

    /****
     * int to byte
     ******/
    public static byte[] intToByte(int content, int offset) {

        byte result[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int j = offset; j < result.length; j += 4) {
            result[j + 3] = (byte) (content & 0xff);
            result[j + 2] = (byte) ((content >> 8) & 0xff);
            result[j + 1] = (byte) ((content >> 16) & 0xff);
            result[j] = (byte) ((content >> 24) & 0xff);
        }
        return result;
    }

}
