package android.hardware.uhf;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.regex.Pattern;


public class reader {
    static public Handler m_handler = null;
    static Boolean m_bASYC = false;
    static byte[] m_buf = new byte[10240];
    static int m_nCount = 0;

    static {
        System.loadLibrary("uhf-tools");
    }

    static public native int Open(String strpath);

    static public native int Read(byte[] pout, int nStart, int nCount);

    static public native void Close();

    static public native void Clean();

    static public native int ResetDevice(byte readID);

    static public native int GetFirmwareVersion(byte readID, byte[] pout);

    static public native int SetReaderAddress(byte readID, byte newID);

    static public native int SetWorkAntenna(byte readID, byte WorkAntenna);

    static public native int GetWorkAntenna(byte readID);

    static public native int SetOutputPower(byte readID, byte outputPower);

    static public native int GetOutputPower(byte readID);

    static public native int GetRFReturnLoss(byte readID, byte Frequency);

    static public native int SetFrequencyRegion(byte readID, byte Region,
                                                byte StartRegion, byte EndRegion);

    static public native int SetUserDefineFrequency(byte readID,
                                                    int nStartFreq, byte btFreqSpace, byte btRreqQuantity);

    static public native int GetFrequencyRegion(byte readID, byte[] pout);

    static public native int SetBeepMode(byte readID, byte mode);

    static public native int GetReaderTemperature(byte readID, byte[] pout);

    static public native int SetDrmMode(byte readID, byte btDrmMode);

    static public native int GetDrmMode(byte readID);

    static public native int ReadGpioValue(byte readID, byte[] pout);

    static public native int WriteGpioValue(byte readID, byte btChooseGpio,
                                            byte btGpioValue);

    static public native int SetAntDetector(byte readID, byte DetectorStatus);

    static public native int GetImpinjFastTid(byte readID);

    static public native int SetImpinjFastTid(byte readID, byte FastTid);

    static public native int SetRfProfile(byte readID, byte ProfileId);

    static public native int GetRfProfile(byte readID);

    static public native int GetReaderIdentifier(byte readID, byte[] pout);

    static public native int SetReaderIdentifier(byte readID, byte[] pin);

    static public native int Inventory(byte readID, byte btRepeat, byte[] pout);

    static public native int SetAccessEpcMatch(byte readID, byte btMode,
                                               byte btEpcLen, byte[] pin);

    static public native int GetAccessEpcMatch(byte readID, byte[] pout);

    static public native int GetInventoryBufferTagCount(byte readID);

    static public native int LockTagISO18000(byte readID, byte[] AryUID,
                                             byte btWordAdd, byte[] pout);

    static public native int QueryTagISO18000(byte readID, byte[] AryUID,
                                              byte WordAdd, byte[] pout);

    static public native int ReadTag(byte btReadId, byte btMemBank,
                                     byte btWordAdd, byte btWordCnt, byte[] pPassword);

    static public native int WriteTag(byte btReadId, byte[] AryPassWord,
                                      byte btMemBank, byte btWordAdd, byte btWordCnt, byte[] jAryData);

    static public native int LockTag(byte btReadId, byte[] pbtAryPassWord,
                                     byte btMembank, byte btLockType);

    static public native int KillTag(byte btReadId, byte[] pbtAryPassWord);

    static public native int GetInventoryBuffer(byte btReadId);

    static public native int GetAndResetInventoryBuffer(byte btReadId);

    static public native int InventoryReal(byte btReadId, byte byRound);

    static public int KillLables(byte btReadId, byte[] pbtAryPassWord) {
        Clean();
        int nret = KillTag(btReadId, pbtAryPassWord);
        if (!m_bASYC) {
            StartASYCKilllables();
        }
        return nret;
    }

    static public int LockLables(byte btReadId, byte[] pbtAryPassWord,
                                 byte btMembank, byte btLockType) {
        Clean();
        int nret = LockTag(btReadId, pbtAryPassWord, btMembank, btLockType);
        if (!m_bASYC) {
            StartASYCLocklables();
        }
        return nret;
    }

    static public int SearchLables(byte btReadId, byte byRound) {
        Clean();
        int nret = InventoryReal(btReadId, byRound);
        if (!m_bASYC) {
            StartASYClables();
        }
        return nret;
    }

    static public int ReadLables(byte btReadId, byte btMemBank, byte btWordAdd,
                                 byte btWordCnt, byte[] pPassword) {
        int nret = 0;
        if (!m_bASYC) {
            Clean();
            nret = ReadTag(btReadId, btMemBank, btWordAdd, btWordCnt, pPassword);

            StartASYCReadlables();
        }
        return nret;
    }

    static public int Writelables(byte btReadId, byte[] AryPassWord,
                                  byte btMemBank, byte btWordAdd, byte btWordCnt, byte[] jAryData) {
        Clean();
        int nret = WriteTag(btReadId, AryPassWord, btMemBank, btWordAdd,
                btWordCnt, jAryData);
        if (!m_bASYC) {
            StartASYCWritelables();
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
                    String[] substr = Pattern.compile("A0(.*?)84").split(str);
                    for (int i = 0; i < substr.length; i++) {
                        if (substr[i].length() > 4) {
                            Message msg = new Message();
                            msg.what = (substr[i].length() - 14) / 2;
                            msg.obj = substr[i].substring(6, substr[i].length() - 8);
                            m_handler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = substr[i].length() == 4 ? -1 : 0;
                            msg.obj = "";
                            m_handler.sendMessage(msg);
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
                    String[] substr = Pattern.compile("A0(.*?)83").split(str);
                    for (int i = 0; i < substr.length; i++) {
                        if (substr[i].length() > 4) {
                            Message msg = new Message();
                            msg.what = (substr[i].length() - 14) / 2;
                            msg.obj = substr[i].substring(6, substr[i].length() - 8);
                            m_handler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = substr[i].length() == 4 ? -1 : 0;
                            msg.obj = "" + i;
                            m_handler.sendMessage(msg);
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
                while (m_handler != null) {

                    nTemp = Read(m_buf, m_nCount, 1024);
                    m_nCount += nTemp;
                    if (nTemp == 0)
                        break;
                    String str = reader.BytesToString(m_buf, 0, m_nCount);
                    String[] substr = Pattern.compile("A0(.*?)82").split(str);
                    for (int i = 0; i < substr.length; i++) {
                        if (substr[i].length() > 4) {
                            Message msg = new Message();
                            msg.what = (substr[i].length() - 14) / 2;
                            msg.obj = substr[i].substring(6, substr[i].length() - 8);
                            m_handler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = substr[i].length() == 4 ? -1 : 0;
                            msg.obj = "";
                            m_handler.sendMessage(msg);
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
                while (m_handler != null) {

                    nTemp = Read(m_buf, m_nCount, 1024);
                    m_nCount += nTemp;
                    if (nTemp == 0)
                        break;
                    String str = reader.BytesToString(m_buf, 0, m_nCount);
                    String[] substr = Pattern.compile("A0(.*?)81").split(str);
                    for (int i = 0; i < substr.length; i++) {
                        if (substr[i].length() > 4) {
                            Message msg = new Message();
                            msg.what = (substr[i].length() - 14) / 2;
                            msg.obj = substr[i].substring(6, substr[i].length() - 8);
                            m_handler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = substr[i].length() == 4 ? -1 : 0;
                            msg.obj = "";
                            m_handler.sendMessage(msg);
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
                while (m_handler != null) {
                    nIndex = m_nCount;
                    nTemp = Read(m_buf, m_nCount, 10240 - m_nCount);
                    m_nCount += nTemp;
                    if (nTemp == 0)
                        break;
                    String str = reader.BytesToString(m_buf, nIndex, m_nCount - nIndex);
                    String[] substr = Pattern.compile("A0(.*?)89").split(str);
                    for (int i = 0; i < substr.length; i++) {
                        Log.e("777777777777777777777777777", substr[i]);
                        if (substr[i].length() > 16) {
                            tag_find = true;
                            Message msg = new Message();
                            msg.what = (substr[i].length() - 10) / 2;
                            msg.obj = substr[i].substring(6, substr[i].length() - 4);
                            m_handler.sendMessage(msg);
                        } else {

                            Message msg = new Message();
                            msg.what = substr[i].length() == 4 ? -1 : 0;
                            msg.obj = tag_find ? "1" : "0";
                            //msg.obj = "";
                            m_handler.sendMessage(msg);
                            Log.e("end", "tertretretert");
                            tag_find = false;
                        }

                    }
                    if (m_nCount >= 1024)
                        m_nCount = 0;

                }

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

    public static int byteToInt(byte[] b, int nIndex) // byteToInt
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
