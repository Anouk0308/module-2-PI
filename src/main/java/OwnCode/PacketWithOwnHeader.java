package OwnCode;

import java.util.Arrays;
import java.util.stream.Stream;

public class PacketWithOwnHeader {
    private byte[] commandoByte = new byte[1];
    private byte[] checksumBytes;//todo: kijken wat ik hiermee ga doen
    private Utils utils;
    private SlidingWindow slidingWindow;
    private Checksum checksum;

    //todo: headers zijn nu flexibel??


    public byte[] commandoOne(){ //client to PI: please give me your files
        commandoByte[0]= utils.fromIntegerToByte(1);

        checksumBytes = checksum.creatingChecksum(commandoByte);
        byte[] byteArr = utils.combineByteArr(checksumBytes, commandoByte);
        return byteArr;
    }

    public byte[] commandoTwo(byte[] PIFileNames){ // PI to client: here are my files
        commandoByte[0]= utils.fromIntegerToByte(2);
        byte[] byteArrTemp = utils.combineByteArr(commandoByte, PIFileNames);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoThree(int processID, String fileName){//client to PI: I am going to download, so start an upload
        commandoByte[0]= utils.fromIntegerToByte(3);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        byte[] fileNamebytes = utils.fromStringToByteArr(fileName);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes, fileNamebytes);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoFour(int processID, String fileName){//client to PI: I am going to upload, so start a download
        commandoByte[0]= utils.fromIntegerToByte(4);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        byte[] fileNamebytes = utils.fromStringToByteArr(fileName);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes, fileNamebytes);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoFive(int processID){//PI to client: I have started a download, you can start uploading
        commandoByte[0]= utils.fromIntegerToByte(5);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoSix(int processID, int packetNumber, byte[] rawData){//this packet is for this process
        commandoByte[0]= utils.fromIntegerToByte(6);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        Integer[] packetNumberi = new Integer[1];
        packetNumberi[0] = packetNumber;
        byte[] packetNumberBytes = utils.fromIntegerArrToByteArr(packetNumberi);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes, packetNumberBytes, rawData);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoSeven(int processID, int packetNumber){//this process has this packet as 'last received successive'
        commandoByte[0]= utils.fromIntegerToByte(7);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        Integer[] packetNumberi = new Integer[1];
        packetNumberi[0] = packetNumber;
        byte[] packetNumberBytes = utils.fromIntegerArrToByteArr(packetNumberi);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes, packetNumberBytes);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoEight(int processID, int packetNumber, byte[] rawData){//this is the last packet for this process
        commandoByte[0]= utils.fromIntegerToByte(8);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        Integer[] packetNumberi = new Integer[1];
        packetNumberi[0] = packetNumber;
        byte[] packetNumberBytes = utils.fromIntegerArrToByteArr(packetNumberi);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes, packetNumberBytes, rawData);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoNine(int processID, int packetNumber){//last packet for this process is received, I am going to close this process on my side
        commandoByte[0]= utils.fromIntegerToByte(9);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        Integer[] packetNumberi = new Integer[1];
        packetNumberi[0] = packetNumber;
        byte[] packetNumberBytes = utils.fromIntegerArrToByteArr(packetNumberi);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes, packetNumberBytes);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoTen(int processID){//Client to PI: pause this process
        commandoByte[0]= utils.fromIntegerToByte(10);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoEleven(int processID){//Client to PI: continue this process
        commandoByte[0]= utils.fromIntegerToByte(11);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoTwelve(int processID){//Client to PI: stop this process
        commandoByte[0]= utils.fromIntegerToByte(12);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoThirteen(int processID){//PI to client: I have paused this process
        commandoByte[0]= utils.fromIntegerToByte(13);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoFourteen(int processID){//PI to client: I have continued this process
        commandoByte[0]= utils.fromIntegerToByte(14);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }

    public byte[] commandoFiveteen(int processID){//PI to client: I have stopped this process
        commandoByte[0]= utils.fromIntegerToByte(15);

        Integer[] processIDi = new Integer[1];
        processIDi[0] = processID;
        byte[] processIDbytes = utils.fromIntegerArrToByteArr(processIDi);

        byte[] byteArrTemp = utils.combineByteArr(commandoByte, processIDbytes);

        checksumBytes = checksum.creatingChecksum(byteArrTemp);
        byte[] byteArr = utils.combineByteArr(checksumBytes, byteArrTemp);
        return byteArr;
    }
}
