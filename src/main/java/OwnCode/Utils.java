package OwnCode;

import java.io.File;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Base64;

public class Utils {

    public byte[] fromStringToByteArr (String s){
        byte[] decodedBytes = Base64.getDecoder().decode(s);
        return decodedBytes;
    }

    public Integer[] fromByteArrToIntegerArr (byte[] b){
        Integer[] decodedIntegers = new Integer[b.length];
        for (int i = 0; i < b.length; i++) {
            decodedIntegers[i] = (b[i] & 0x000000ff);
        }
        return decodedIntegers;
    }

    public Integer fromByteToInteger (byte b){
        Integer decodedIntegers =  (b & 0x000000ff);
        return decodedIntegers;
    }

    public byte[] fromIntegerArrToByteArr (Integer[] intarray) throws IllegalArgumentException {
        if (intarray == null) {
            throw new IllegalArgumentException("packet == null");
        }
        for (int i = 0; i < intarray.length; i++) {
            if (intarray[i] == null) {
                throw new IllegalArgumentException("packet[" + i + "] == null");
            }
        }

        byte[] encodedBytes = new byte[intarray.length];
        for (int i = 0; i < intarray.length; i++) {
            encodedBytes[i] = (byte) ((intarray[i] & 0x000000ff));
        }
        return encodedBytes;
    }

    public byte fromIntegerToByte (Integer i) throws IllegalArgumentException {
        byte encodedByte = (byte) ((i & 0x000000ff));
        return encodedByte;
    }

    public String fromByteArrToString (byte[] b) throws IllegalArgumentException {
        String s = Base64.getEncoder().encodeToString(b);
        return s;
    }

    public String[] fromByteArrToStringArr (byte[] b) throws IllegalArgumentException {
        String s = Base64.getEncoder().encodeToString(b);
        String stringArr[] = s.split("\\+");
        return stringArr;
    }

    public byte[] removeHeader(byte[] b){ //get only the raw data
        byte[] rawData = new byte[b.length-6]; //header is 6 bytes long
        System.arraycopy(b,6,rawData,0,b.length);
        return rawData;
    }

    public File packetsToFile(DatagramPacket[] packets, String FilePath, int rawDataSpace){
        byte[] bytesFile = new byte[packets.length*rawDataSpace];
        for(int i = 0; i < packets.length; i++){
            byte[] packetBytes = packets[i].getData();
            byte[] rawPacketBytes = removeHeader(packetBytes);
            System.arraycopy(rawPacketBytes,0,bytesFile,i*rawDataSpace, rawDataSpace);
        }
        //todo, byte array to file



        File file = new File(FilePath);
        return file;
    }

    public byte[] combineByteArr(byte[] a, byte[] b){
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public byte[] combineByteArr(byte[] a, byte[] b, byte[] c){
        byte[] d = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, d, 0, a.length);
        System.arraycopy(b, 0, d, a.length, b.length);
        System.arraycopy(b, 0, d, b.length, c.length);
        return d;
    }

    public byte[] combineByteArr(byte[] a, byte[] b, byte[] c, byte[] d){
        byte[] e = new byte[a.length + b.length + c.length + d.length];
        System.arraycopy(a, 0, e, 0, a.length);
        System.arraycopy(b, 0, e, a.length, b.length);
        System.arraycopy(b, 0, e, b.length, c.length);
        System.arraycopy(b, 0, e, c.length, d.length);
        return e;
    }

    public byte limitByteFirstByte(int i){
        int divided = Math.floorDiv(i,256);
        byte b = fromIntegerToByte(divided);
        return b;
    }

    public byte limitByteSecondByte(int i){
        int modulo = i%256;
        byte b = fromIntegerToByte(modulo);
        return b;
    }

    public int limitBytesToInteger(byte firstByte, byte secondByte){
        int firstInt = fromByteToInteger(firstByte);
        int secondInt = fromByteToInteger(secondByte);
        int number = firstInt*secondInt;

        return number;
    }

    public class Timer{
        private int startingTime;
        private int tooLate;

        public Timer(int miliSeconds){
            startingTime = (int) System.currentTimeMillis();
            tooLate = startingTime + miliSeconds;
        }

        public boolean isTooLate(){
            int now = (int) System.currentTimeMillis();
            if(now > tooLate){
                return true;
            }else{
                return  false;
            }
        }

    }
}