package OwnCode;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class Checksum {
    private String polynomial = "100000111";//CRC-8
    private String[] polynomialStringArray = polynomial.split("");
    private Utils utils;

    public Checksum(){
        utils = new Utils();
    }

    public byte[] creatingChecksum(byte[] data){
        CRC32 crc = new CRC32();
        crc.update(data);
        long value = crc.getValue();

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        byte[] tooLong =  buffer.array();

        /*
        byte[] correct = new byte[4];
        System.arraycopy(tooLong,4,correct,0,4);
        */
        return tooLong;
    }

    private static void print (String message){
        System.out.println(message);
    }//todo weghalen


    public DatagramPacket checkingChecksum(DatagramPacket packet){
        DatagramPacket checkedPacket = null;

        byte[] data = packet.getData();
        byte[] checksum = new byte[4];
        System.arraycopy(data,0,checksum,0,4);//todo hier naar kijken

        byte[] dataWithOutChecksum = new byte[data.length-4];
        System.arraycopy(data,4,dataWithOutChecksum,0,data.length-4);

        byte[] ownCalculatedChecksum = creatingChecksum(dataWithOutChecksum);

        if(checksum[0] == ownCalculatedChecksum[0] && checksum[1] == ownCalculatedChecksum[1] && checksum[2] == ownCalculatedChecksum[2] && checksum[3] == ownCalculatedChecksum[3]){
            checkedPacket = packet;
        }
        return checkedPacket;

    }
}