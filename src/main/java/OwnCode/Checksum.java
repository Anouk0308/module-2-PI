package OwnCode;

import java.net.DatagramPacket;

public class Checksum { //https://www.slideshare.net/sandeep101026/crc-java-code
    private int generatorPolynomial = 1101;


    //todo crc32

    public byte [] creatingChecksum(byte[] data){





        int [] appendedMessage = new int[data.length];

        //todo

        byte[] checksum = new byte [1];

        return checksum;
    }

    public DatagramPacket checkingChecksum(DatagramPacket packet){
        //todo: in header kijken wat checksum zegt. kijken of het klopt

        //als klopt, haal checksum uit header
        //als niet klopt, verwijder dan (en statistics krijgt voor foutive packetje ++)

        return packet;

    }
}
