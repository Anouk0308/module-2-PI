package OwnCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;

public class ZZZtodoLijstVoorMezelf {

    //alle classes af


    //user input, wanneer dowloadprocess start, staat daar nu 3000. dit klopt niet. hier naar kijken

    //timer:
    //upload sent last packet
    //download zelfde probleem als upload. probleem met timer
    //op andere plekken ook naar kijken


    //alle print langs gaan of het is wat ik wil laten zien


    //statistics, kijken of start en stop lukt

    //na uploaden of dowloaden nog meer opties kunnen kiezen
    //dan kijken of je verschillende processen kan zien, en dingen kan pauzeren of niet

    //hardcoded zooi eruit slopen


    //broadcast


}

/*
    public void createFile(){
        System.out.println("Creating the file, wait a moment please");

        int newPacketsArrayLenght = downloadingPackets.length; //todo, als dowloadingpackets niet meer 1000000 is, hoeft dit allemaal niet
        for (int i = 0; i < downloadingPackets.length; i++) {
            if (downloadingPackets[i] == null) {
                newPacketsArrayLenght--;
            }
        }
        DatagramPacket[] newPacketArray = new DatagramPacket[newPacketsArrayLenght];
        System.arraycopy(downloadingPackets, 0, newPacketArray, 0, newPacketsArrayLenght);

        byte[] allBytesTogether = new byte[0];

        for(int i = 0; i < newPacketsArrayLenght; i++){
            byte[] rawData = utils.removeHeader(newPacketArray[i].getData());
            allBytesTogether = utils.combineByteArr(allBytesTogether, rawData);
        }

        String filePath = folderPath + "/" + fileName;
        File file = new File(filePath);

        try{
            OutputStream os = new FileOutputStream(file);
            os.write(allBytesTogether);
            print("file saved");
        } catch (IOException e){
            print(e.getMessage());
        }
    }
    */