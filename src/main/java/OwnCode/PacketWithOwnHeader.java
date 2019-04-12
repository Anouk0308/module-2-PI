package OwnCode;

import java.net.InetAddress;

public class PacketWithOwnHeader {
    private Utils utils = new Utils();
    private Checksum checksum = new Checksum();

    int checksumPosition = 0;
    private byte[] checksumBytes = new byte[4];
    int commandoPosition = checksumPosition + checksumBytes.length;
    private byte[] commandoByte = new byte[1];
    int processIDPosition = commandoPosition + commandoByte.length;
    private byte[] processIDbytes = new byte[2];
    int packetNumberPosition = processIDPosition + processIDbytes.length;
    private byte[] packetNumberBytes= new byte[2];
    int headerLength = packetNumberPosition + packetNumberBytes.length;

    public byte[] commandoZero(){ //client to PI: handshake
        commandoByte[0]= utils.fromIntegerToByte(100);//0 would not go well with creating a checksum

        checksumBytes = checksum.creatingChecksum(commandoByte);
        byte[] header = utils.combineByteArr(checksumBytes, commandoByte);
        return header;
    }

    public byte[] commandoOne(){ //client to PI: please give me your files
        commandoByte[0]= utils.fromIntegerToByte(1);

        checksumBytes = checksum.creatingChecksum(commandoByte);
        byte[] header = utils.combineByteArr(checksumBytes, commandoByte);
        return header;
    }

    public byte[] commandoTwo(byte[] PIFileNames){ // PI to client: here are my files
        commandoByte[0]= utils.fromIntegerToByte(2);
        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes, packetNumberBytes, PIFileNames);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoThree(int processID, String fileNameAndNumberOfBytesToLoad){//client to PI: I am going to download, so start an upload
        commandoByte[0]= utils.fromIntegerToByte(3);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        byte[] fileNamebytes = utils.fromStringToByteArr(fileNameAndNumberOfBytesToLoad);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes, packetNumberBytes, fileNamebytes);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoFour(int processID, String fileNameAndNumberOfBytesToLoad){//client to PI: I am going to upload, so start a download
        commandoByte[0]= utils.fromIntegerToByte(4);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        byte[] fileNamebytes = utils.fromStringToByteArr(fileNameAndNumberOfBytesToLoad);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes, packetNumberBytes, fileNamebytes);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoFive(int processID){//PI to client: I have started a download, you can start uploading
        commandoByte[0]= utils.fromIntegerToByte(5);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoSix(int processID, int packetNumber, byte[] rawData){//this packet is for this process
        commandoByte[0]= utils.fromIntegerToByte(6);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        packetNumberBytes[0] = utils.limitByteFirstByte(packetNumber);
        packetNumberBytes[1]= utils.limitByteSecondByte(packetNumber);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes, packetNumberBytes, rawData);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoSeven(int processID, int packetNumber){//this process has this packet as 'last received successive'
        commandoByte[0]= utils.fromIntegerToByte(7);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        packetNumberBytes[0] = utils.limitByteFirstByte(packetNumber);
        packetNumberBytes[1]= utils.limitByteSecondByte(packetNumber);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes, packetNumberBytes);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoEight(int processID, int packetNumber, byte[] rawData){//this is the last packet for this process
        commandoByte[0]= utils.fromIntegerToByte(8);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        packetNumberBytes[0] = utils.limitByteFirstByte(packetNumber);
        packetNumberBytes[1]= utils.limitByteSecondByte(packetNumber);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes, packetNumberBytes, rawData);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoNine(int processID, int packetNumber){//last packet for this process is received, I am going to close this process on my side
        commandoByte[0]= utils.fromIntegerToByte(9);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        packetNumberBytes[0] = utils.limitByteFirstByte(packetNumber);
        packetNumberBytes[1]= utils.limitByteSecondByte(packetNumber);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes, packetNumberBytes);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        for(int i = 0; i < header.length; i++){//todo weghalen
            print(Byte.toString(header[i]));
        }
        return header;
    }

    public byte[] commandoTen(int processID){//Client to PI: pause this process
        commandoByte[0]= utils.fromIntegerToByte(10);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoEleven(int processID){//Client to PI: continue this process
        commandoByte[0]= utils.fromIntegerToByte(11);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoTwelve(int processID){//Client to PI: stop this process
        commandoByte[0]= utils.fromIntegerToByte(12);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoThirteen(int processID){//PI to client: I have paused this process
        commandoByte[0]= utils.fromIntegerToByte(13);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoFourteen(int processID){//PI to client: I have continued this process
        commandoByte[0]= utils.fromIntegerToByte(14);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public byte[] commandoFiveteen(int processID){//PI to client: I have stopped this process
        commandoByte[0]= utils.fromIntegerToByte(15);

        processIDbytes[0] = utils.limitByteFirstByte(processID);
        processIDbytes[1]= utils.limitByteSecondByte(processID);

        byte[] headerTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(headerTemp);
        byte[] header = utils.combineByteArr(checksumBytes, headerTemp);
        return header;
    }

    public void print (String message){
        System.out.println(message);
    }
}
