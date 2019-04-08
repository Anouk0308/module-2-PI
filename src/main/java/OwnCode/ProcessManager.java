package OwnCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ProcessManager {
    private DatagramSocket socket;

    private UploadProcess[] runningUploadProcesses = new UploadProcess[256];
    private UploadProcess[] pausedUploadProcesses = new UploadProcess[256];

    private DownloadProcess[] runningDownloadProcesses = new DownloadProcess[256];
    private DownloadProcess[] pausedDownloadProcesses = new DownloadProcess[256];
    int processID = -1;

    private PacketWithOwnHeader packetWithOwnHeader;
    private int destinationPort;
    private InetAddress destinationAddress;


    public ProcessManager(DatagramSocket socket, int destinationPort, InetAddress destinationAddress){
        this.socket = socket;
        this.destinationPort = destinationPort;
        this.destinationAddress = destinationAddress;
        packetWithOwnHeader = new PacketWithOwnHeader();
    }

    public int getAProcessID(){
        processID++;
        return processID;
    }

    public void createUploadProcess(File file, Client client){
        int processID = getAProcessID();
        UploadProcess upload = new UploadProcess(processID, file, client);
        runningUploadProcesses[processID] = upload;
    }

    public void createDownloadProcess(String fileName, String filePath, Client client){
        int processID = getAProcessID();
        DownloadProcess download = new DownloadProcess(processID, fileName, client, filePath);
        runningDownloadProcesses[processID] = download;
    }

    public void createUploadProcessWithProcessID(File file, Client client, int processID){
        UploadProcess upload = new UploadProcess(processID, file, client);
        runningUploadProcesses[processID] = upload;
    }

    public void createDownloadProcessWithProcessID(String fileName, String filePath, Client client, int processID){
        DownloadProcess download = new DownloadProcess(processID, fileName, client, filePath);
        runningDownloadProcesses[processID] = download;
    }

    public void printRunningProcesses(){
        for(int i = 0; i < runningUploadProcesses.length; i++){
            UploadProcess process = runningUploadProcesses[i];
            int processID = process.getProcessID();
            String filename = process.getFileName();
            print("Process "+ processID+ " is uploading file "+ filename);
        }
        for(int i = 0; i < runningDownloadProcesses.length; i++){
            DownloadProcess process = runningDownloadProcesses[i];
            int processID = process.getProcessID();
            String filename = process.getFileName();
            print("Process "+ processID+ " is downloading file "+ filename);
        }
    }

    public void printPausedProcesses(){
        for(int i = 0; i < pausedUploadProcesses.length; i++){
            UploadProcess process = runningUploadProcesses[i];
            int processID = process.getProcessID();
            String filename = process.getFileName();
            print("Process "+ processID+ " is paused while uploading file "+ filename);
        }
        for(int i = 0; i < pausedDownloadProcesses.length; i++){
            DownloadProcess process = runningDownloadProcesses[i];
            int processID = process.getProcessID();
            String filename = process.getFileName();
            print("Process "+ processID+ " is paused while downloading file "+ filename);
        }
    }

    public void printAllProcesses(){
        printRunningProcesses();
        printPausedProcesses();
    }

    public void pauseSpecificProcess(int userIDSugested){
        int IDfound = 0;
        int processID = 0;
        String process = "";

        for(int i = 0; i < runningUploadProcesses.length; i++){
            if(runningUploadProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
                process = "UP";
            }
        }
        for(int i = 0; i < runningDownloadProcesses.length; i++){
            if(runningDownloadProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
                process = "DOWN";
            }
        }

        if(IDfound == 1){
            print("Process "+ processID + "is paused");
            if(process.equals("UP")){
                runningUploadProcesses[processID].getThread().interrupt();
                pausedUploadProcesses[processID] = runningUploadProcesses[processID];
                runningUploadProcesses[processID] = null;

                //also send the server to pause the process
                byte[] buffer = packetWithOwnHeader.commandoTen(processID);
                DatagramPacket pausePacket = new DatagramPacket(buffer, buffer.length);
                send(pausePacket);

            }else if (process.equals("DOWN")){
                runningDownloadProcesses[processID].getThread().interrupt();
                pausedDownloadProcesses[processID] = runningDownloadProcesses[processID];
                runningDownloadProcesses[processID] = null;

                //also send the server to pause the process
                byte[] buffer = packetWithOwnHeader.commandoTen(processID);
                DatagramPacket pausePacket = new DatagramPacket(buffer, buffer.length);
                send(pausePacket);
            }
        } else{
            print("That is not a correct processID, these are the processes to choose from:");
            printRunningProcesses();
            pauseSpecificProcess(userIDSugested);
        }
    }


    public void pauseAllProcesses(){
        for(int i = 0; i < runningUploadProcesses.length; i++){
            int processID = runningUploadProcesses[i].getProcessID();
            runningUploadProcesses[i].getThread().interrupt();
            pausedUploadProcesses[i] = runningUploadProcesses[i];
            runningUploadProcesses[i] = null;
            print("Process "+ processID + "is paused");

            //also send the server to pause the process
            byte[] buffer = packetWithOwnHeader.commandoTen(processID);
            DatagramPacket pausePacket = new DatagramPacket(buffer, buffer.length);
            send(pausePacket);
        }

        for(int i = 0; i < pausedUploadProcesses.length; i++){
            int processID = runningDownloadProcesses[i].getProcessID();
            runningDownloadProcesses[i].getThread().interrupt();
            pausedDownloadProcesses[i] = runningDownloadProcesses[i];
            runningDownloadProcesses[i] = null;
            print("Process "+ processID + "is paused");

            //also send the server to pause the process
            byte[] buffer = packetWithOwnHeader.commandoTen(processID);
            DatagramPacket pausePacket = new DatagramPacket(buffer, buffer.length);
            send(pausePacket);
        }
    }

    public void continueSpecificProcess(int userIDSugested){
        int IDfound = 0;
        int processID = 0;
        String process = "";

        for(int i = 0; i < pausedUploadProcesses.length; i++){
            if(pausedUploadProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
                process = "UP";
            }
        }
        for(int i = 0; i < pausedDownloadProcesses.length; i++){
            if(pausedDownloadProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
                process = "DOWN";
            }
        }

        if(IDfound == 1){
            print("Process "+ processID + "is continued");
            if(process.equals("UP")){
                pausedUploadProcesses[processID].getThread().run();
                runningUploadProcesses[processID] = pausedUploadProcesses[processID];
                pausedUploadProcesses[processID] = null;

                //also send the server to continue the process
                byte[] buffer = packetWithOwnHeader.commandoEleven(processID);
                DatagramPacket continuePacket = new DatagramPacket(buffer, buffer.length);
                send(continuePacket);


            }else if (process.equals("DOWN")){
                pausedDownloadProcesses[processID].getThread().run();
                runningDownloadProcesses[processID] = pausedDownloadProcesses[processID];
                pausedDownloadProcesses[processID] = null;

                //also send the server to continue the process
                byte[] buffer = packetWithOwnHeader.commandoEleven(processID);
                DatagramPacket continuePacket = new DatagramPacket(buffer, buffer.length);
                send(continuePacket);
            }
        } else{
            print("That is not a correct processID, these are the processes to choose from:");
            printRunningProcesses();
            continueSpecificProcess(userIDSugested);
        }
    }

    public void continueAllProcesses(){
        for(int i = 0; i < pausedUploadProcesses.length; i++){
            int processID = pausedUploadProcesses[i].getProcessID();
            pausedUploadProcesses[i].getThread().run();
            runningUploadProcesses[i] = pausedUploadProcesses[i];
            pausedUploadProcesses[i] = null;
            print("Process "+ processID + "is continued");

            //also send the server to continue the process
            byte[] buffer = packetWithOwnHeader.commandoEleven(processID);
            DatagramPacket continuePacket = new DatagramPacket(buffer, buffer.length);
            send(continuePacket);
        }

        for(int i = 0; i < runningUploadProcesses.length; i++){
            int processID = pausedDownloadProcesses[i].getProcessID();
            pausedDownloadProcesses[i].getThread().run();
            runningDownloadProcesses[i] = pausedDownloadProcesses[i];
            pausedDownloadProcesses[i] = null;
            print("Process "+ processID + "is continued");

            //also send the server to continue the process
            byte[] buffer = packetWithOwnHeader.commandoEleven(processID);
            DatagramPacket continuePacket = new DatagramPacket(buffer, buffer.length);
            send(continuePacket);
        }
    }

    public void stopSpecificProcess(int userIDSugested){
        int IDfound = 0;
        int processID = 0;
        String process = "";
        String state = "";

        for(int i = 0; i < runningUploadProcesses.length; i++){
            if(runningUploadProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
                process = "UP";
                state = "RUNNING";
            }
        }
        for(int i = 0; i < runningDownloadProcesses.length; i++){
            if(runningDownloadProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
                process = "DOWN";
                state = "RUNNING";
            }
        }
        for(int i = 0; i < pausedUploadProcesses.length; i++){
            if(pausedUploadProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
                process = "UP";
                state = "PAUSED";
            }
        }
        for(int i = 0; i < pausedDownloadProcesses.length; i++){
            if(pausedDownloadProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
                process = "DOWN";
                state = "PAUSED";
            }
        }

        if(IDfound == 1){
            print("Process "+ processID + "is stopped");
            if(state.equals("RUNNING")){
                if(process.equals("UP")){
                    runningUploadProcesses[processID].kill();
                    runningUploadProcesses[processID] = null;

                    //also send the server to stop the process
                    byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
                    DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
                    send(stopPacket);

                }else if (process.equals("DOWN")){
                    runningDownloadProcesses[processID].kill();
                    runningDownloadProcesses[processID] = null;

                    //also send the server to stop the process
                    byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
                    DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
                    send(stopPacket);
                }
            } else if(state.equals("PAUSED")){
                if(process.equals("UP")){
                    pausedUploadProcesses[processID].kill();
                    pausedUploadProcesses[processID] = null;

                    //also send the server to stop the process
                    byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
                    DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
                    send(stopPacket);

                }else if (process.equals("DOWN")){
                    pausedDownloadProcesses[processID].kill();
                    pausedDownloadProcesses[processID] = null;

                    //also send the server to stop the process
                    byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
                    DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
                    send(stopPacket);
                }
            }
        } else{
            print("That is not a correct processID, these are the processes to choose from:");
            printRunningProcesses();
            printPausedProcesses();
        }
    }

    public void stopAllProcesses(){
        for(int i = 0; i < runningUploadProcesses.length; i++){
            runningUploadProcesses[i].kill();
            runningUploadProcesses[i] = null;
            print("Process "+ processID + "is stopped");

            //also send the server to stop the process
            byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
            DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
            send(stopPacket);
        }

        for(int i = 0; i < runningDownloadProcesses.length; i++){
            runningDownloadProcesses[i].kill();
            runningDownloadProcesses[i] = null;
            print("Process "+ processID + "is stopped");

            //also send the server to stop the process
            byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
            DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
            send(stopPacket);
        }

        for(int i = 0; i < pausedUploadProcesses.length; i++){
            pausedUploadProcesses[i].kill();
            pausedUploadProcesses[i] = null;
            print("Process "+ processID + "is stopped");

            //also send the server to stop the process
            byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
            DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
            send(stopPacket);
        }

        for(int i = 0; i < pausedDownloadProcesses.length; i++){
            pausedDownloadProcesses[i].kill();
            pausedDownloadProcesses[i] = null;
            print("Process "+ processID + "is stopped");

            //also send the server to stop the process
            byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
            DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
            send(stopPacket);
        }
    }

    public void receiveUploadAcknowledgement(int processID){
        UploadProcess upload = runningUploadProcesses[processID];
        upload.setAcknowledgementToStartTrue();
    }

    public void receivePacketForProcess(int processID, DatagramPacket receivedPacked){
        if (runningDownloadProcesses[processID] != null){
            runningDownloadProcesses[processID].receivePacket(receivedPacked);
        }
    }

    public void receiveAcknowledgementPacketForProcess(int processID, DatagramPacket receivedPacked){
        if (runningUploadProcesses[processID] != null){
            runningUploadProcesses[processID].receiveAcknowledgementPacket(receivedPacked);
        }
    }

    public void receiveLastPacketForProcess(int processID, DatagramPacket receivedPacked){
        if (runningDownloadProcesses[processID] != null){
            runningDownloadProcesses[processID].receiveLastPacket(receivedPacked);
        }
    }

    public void receiveAcknowledgementLastPacketForProcess(int processID, DatagramPacket receivedPacked){
        UploadProcess upload = runningUploadProcesses[processID];
        upload.setAcknowledgementToStopTrue();
    }

    private static void print (String message){
        System.out.println(message);
    }

    public void send(DatagramPacket p){
        byte[] buf = p.getData();
        int lenght = p.getLength();
        DatagramPacket packet = new DatagramPacket(buf, lenght, destinationAddress, destinationPort);

        try {
            socket.send(packet);
        } catch (IOException e) {
            print("Client error: " + e.getMessage());
        }
    }
}
