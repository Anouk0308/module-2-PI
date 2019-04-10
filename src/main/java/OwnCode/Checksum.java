package OwnCode;

import java.net.DatagramPacket;

public class Checksum {
    private int polynomial = 100000111;//CRC-8
    private Utils utils;
    private String[] polynomialStringArray = utils.fromIntToStringArr(polynomial);



    public Checksum(){
        utils = new Utils();
    }

    public byte[] creatingChecksum(byte[] data){
        String bitString = utils.fromByteArrToStringBit(data);
        String[] bitStringArray = bitString.split("");
        String[] reminderArr = gettingReminderArr(bitStringArray);
        String reminder = "";

        for(int i = 0; i < reminderArr.length; i++){
            reminder = reminder + reminderArr[i];
        }

        byte[] checksum = new byte [1];
        checksum = utils.fromStringToByteArr(reminder);

        return checksum;
    }

    public String[] gettingReminderArr(String[] bitStringArray){
        String[] reminderArr = null;
        String[] noFrontZerosBitStringArray = removingFirstZeros(bitStringArray);

        if(noFrontZerosBitStringArray.length > polynomialStringArray.length){
            String[] newBitStringArray = binaryRules(noFrontZerosBitStringArray, polynomialStringArray);
            gettingReminderArr(newBitStringArray);
        } else{
            reminderArr = bitStringArray;
        }

        return reminderArr;
    }

    public String[] removingFirstZeros(String[] bitStringArray){
        String[] newBitStringArray = null;

        if(bitStringArray[0].equals("0")){
            String[] bitStringArrayTemp = new String[bitStringArray.length-1];
            System.arraycopy(bitStringArray, 1, bitStringArrayTemp, 0, bitStringArray.length-1);
            removingFirstZeros(bitStringArrayTemp);
        } else{
            newBitStringArray = bitStringArray;
        }

        return newBitStringArray;
    }

    public String[] binaryRules(String[] bitStringArray, String[] polynomialStringArray){
        String[] newBitStringArray = new String[bitStringArray.length];

        for(int i = 0; i < polynomialStringArray.length; i++){
            if(bitStringArray[i].equals(polynomialStringArray[i])){
                newBitStringArray[i] = "0";
            } else{
                newBitStringArray[i] = "1";
            }
        }
        System.arraycopy(bitStringArray, polynomialStringArray.length, newBitStringArray, polynomialStringArray.length, bitStringArray.length-polynomialStringArray.length);

        return newBitStringArray;
    }

    public DatagramPacket checkingChecksum(DatagramPacket packet){
        DatagramPacket checkedPacket = null;

        byte[] data = packet.getData();
        byte[] checksum = new byte[1];//todo kijken of dit mooier kan
        checksum[0] = data[0];
        byte[] dataWithOutChecksum = new byte[data.length-1];
        System.arraycopy(data,1,dataWithOutChecksum,0,data.length-1);

        byte[] ownCalculatedChecksum = creatingChecksum(dataWithOutChecksum);

        if(checksum == ownCalculatedChecksum){
            checkedPacket = packet;
        }
        return checkedPacket;

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
