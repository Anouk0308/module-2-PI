package OwnCode;

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

    public Integer fromByteToInteger (byte b){
        Integer decodedIntegers =  (b & 0x000000ff);
        return decodedIntegers;
    }

    public byte fromIntegerToByte (Integer i) throws IllegalArgumentException {
        byte encodedByte = (byte) ((i & 0x000000ff));
        return encodedByte;
    }

    //packets related
    public byte[] removeHeader(byte[] b){ //get only the raw data
        byte[] rawData = null;
        int headerLength = 5;//always checksum and commandonumber
        if(b.length>5){//has processID
            headerLength = 7;
            if(b.length>7){//has packetNumber
                headerLength = 9;
            }
        }

        rawData = new byte[b.length - headerLength];
        System.arraycopy(b, headerLength, rawData, 0, rawData.length);
        return rawData;
    }

    //combining byte arrays
    public byte[] combineByteArr(byte[] a, byte[] b){
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public byte[] combineByteArr(byte[] a, byte[] b, byte[] c){
        byte[] temp = combineByteArr(a,b);
        byte[] d = combineByteArr(temp,c);
        return d;
    }

    public byte[] combineByteArr(byte[] a, byte[] b, byte[] c, byte[] d){
        byte[] temp = combineByteArr(a,b);
        byte[] tempp = combineByteArr(c,d);
        byte[] e = combineByteArr(temp, tempp);
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
        int number = (firstInt*256 )+secondInt;

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
}