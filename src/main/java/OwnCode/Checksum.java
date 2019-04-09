package OwnCode;

import java.net.DatagramPacket;

public class Checksum { //https://www.slideshare.net/sandeep101026/crc-java-code
    private int polynomial = 100000111;//CRC-8
    private Utils utils;
    private Statistics statistics;


    public Checksum(Statistics statistics){
        this.statistics = statistics;
        utils = new Utils();
    }

    public byte [] creatingChecksum(byte[] data){
        String bitString = utils.fromByteArrToStringBit(data);
        String[] bitStringArray = bitString.split("");
        String reminder = "";

        //remove 0 vooraan, tot eerste een 1 is

        //als groter is dan lengte generator:
            //pak eerste aantal van bitStringArray (even veel als dat generatorPolynimial groot is)
            //voor elk van deze, controleer met elkaar:
                //zijn ze zelfde?, verander bitStringArray die plek naar 0
                //zijn ze niet zelfde? verander bitStringArray die plek naar 1

        //als lengte dan kleiner is dan generatorPolynomial, stuur door als reminder
            //for (int i = 0; i < overgeblevenBitStringArray; i++){
                //reminder = reminder + overgeblevenBitStringArray[i];
            //}


        byte[] checksum = new byte [1];
        checksum = utils.fromStringToByteArr(reminder);

        return checksum;
    }

    public DatagramPacket checkingChecksum(DatagramPacket packet){
        DatagramPacket checkedPacket = null;
        //lees checkssum uit packet (checksum a)
        //stuur packet zonder checkssum door creatingChechsum(packet zonder checksum) (checksum b)

        //als a en b gelijk zijn:
            //stuur packetje zoncer checksum a door
        //als a en b niet gelijk zijn:
            //checkedPacket = =null
            //statistics.foundCorruptedPacket();


        return packet;

    }
}

/*
EXAMPLE CRC-8:(https://www.analog.com/en/analog-dialogue/articles/cyclic-redundancy-and-correct-data-communications.html#)
initial value = 01100101010000110010000100000000
polynomial for CRC-8 = x^8 + x^2 + x^1 + 1 = 100000111

binary-rules: 1+1 = 0; 0+0 = 0; 1+0 = 1; 0+1 = 1;
more-rules: zero's on the front are not used

calculating checksum:

01100101010000110010000100000000
 100000111
--------------------------------
 0100100100000110010000100000000
  100000111
--------------------------------
  000100011000110010000100000000
  100000111
--------------------------------
     100011000110010000100000000
     100000111
--------------------------------
     000011111110010000100000000
         100000111
--------------------------------
         11111110010000100000000
         100000111
--------------------------------
         01111101110000100000000
          100000111
--------------------------------
          0111110000000100000000
           100000111
--------------------------------
           011111111000100000000
            100000111
--------------------------------
            01111100100100000000
             100000111
--------------------------------
             0111101010100000000
              100000111
--------------------------------
              011101101100000000
               100000111
--------------------------------
               01101110000000000
                100000111
--------------------------------
                0101111110000000
                 100000111
--------------------------------
                 001111000000000
                   100000111
--------------------------------
                   0111001110000
                    100000111
--------------------------------
                    011001001000
                     100000111
--------------------------------
                     01001010100
                      100000111
--------------------------------
                      0001011010
                         100000111 // the polynomial is greater than the value, so it wil not be subtracted

               checksum: 1011010


 */
