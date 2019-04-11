package OwnCode;

import java.io.File;
import java.net.DatagramPacket;
import java.util.Base64;

public class Utils {

    //from a to b

    public byte[] fromStringToByteArr (String s){
        String encodedString = Base64.getEncoder().encodeToString(s.getBytes());
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return decodedBytes;
    }

    public String fromByteArrToString (byte[] b) throws IllegalArgumentException {
        String s = new String(b);
        return s;
    }

    public String fromByteArrToStringBit(byte[] b){
        String s = "";
        for(int i = 0; i < b.length; i++) {
            s = s + Integer.toBinaryString((b[i] & 0xFF) + 0x100).substring(1);
        }
        return s;
    }

    public Integer fromByteToInteger (byte b){
        Integer decodedIntegers =  (b & 0x000000ff);
        return decodedIntegers;
    }

    public byte fromIntegerToByte (Integer i) throws IllegalArgumentException {
        byte encodedByte = (byte) ((i & 0x000000ff));
        return encodedByte;
    }

    public String[] fromByteArrToStringArr (byte[] b) throws IllegalArgumentException {
        String s = Base64.getEncoder().encodeToString(b);
        String stringArr[] = s.split("\\+");
        return stringArr;
    }

    public Integer[] fromByteArrToIntegerArr (byte[] b){
        Integer[] decodedIntegers = new Integer[b.length];
        for (int i = 0; i < b.length; i++) {
            decodedIntegers[i] = (b[i] & 0x000000ff);
        }
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

    //packets related
    public byte[] removeHeader(byte[] b){ //get only the raw data
        byte[] rawData = null;
        int headerLength = 5;//always checksum and commandonumber//todo controleren
        if(b.length>5){//has processID
            headerLength = 7;//todo controleren
            if(b.length>7){//has packetNumber
                headerLength = 9;//todo controleren
            }
        }

        rawData = new byte[b.length - headerLength];
        System.arraycopy(b, headerLength, rawData, 0, rawData.length);
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

    //combining byte arrays
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
        System.arraycopy(c, 0, d, b.length, c.length);
        return d;
    }

    public byte[] combineByteArr(byte[] a, byte[] b, byte[] c, byte[] d){
        byte[] e = new byte[a.length + b.length + c.length + d.length];
        System.arraycopy(a, 0, e, 0, a.length);
        System.arraycopy(b, 0, e, a.length, b.length);
        System.arraycopy(c, 0, e, b.length, c.length);
        System.arraycopy(d, 0, e, c.length, d.length);
        return e;
    }

    //dealing with the limitation of a byte (from 0 till 255)
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

    //a timer
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

    private static void print (String message){
        System.out.println(message);
    }
}