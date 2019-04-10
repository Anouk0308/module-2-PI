package OwnCode;

import java.io.File;
import java.net.DatagramPacket;

public class ProcessManager {

    private Process[] runningProcesses = new Process[1000];
    private Process[] pausedProcesses = new Process[1000];

    int processID = -1;

    private PacketWithOwnHeader packetWithOwnHeader;
    private NetworkUser networkUser;
    private SlidingWindow slidingWindow;


    public ProcessManager(NetworkUser networkUser, SlidingWindow slidingWindow){
        this.networkUser = networkUser;
        this.slidingWindow = slidingWindow;
        packetWithOwnHeader = new PacketWithOwnHeader();
    }

    public int getAProcessID(){
        processID++;
        return processID;
    }

    public void createUploadProcess(File file, NetworkUser networkUser, boolean isClient){
        int processID = getAProcessID();

        UploadProcess upload = new UploadProcess(processID, file, networkUser, isClient, slidingWindow);
        runningProcesses[processID] = upload;
        networkUser.getStatics().startingProcess(processID);
    }

    public void createDownloadProcess(String fileName, String filePath, NetworkUser networkUser, boolean isClient){
        int processID = getAProcessID();
        DownloadProcess download = new DownloadProcess(processID, fileName, networkUser, filePath, isClient, slidingWindow);
        runningProcesses[processID] = download;
        networkUser.getStatics().startingProcess(processID);
    }

    public void createUploadProcessWithProcessID(File file, NetworkUser networkUser, int processID, boolean isClient){
        UploadProcess upload = new UploadProcess(processID, file, networkUser, isClient, slidingWindow);
        runningProcesses[processID] = upload;
        networkUser.getStatics().startingProcess(processID);
    }

    public void createDownloadProcessWithProcessID(String fileName, String filePath, NetworkUser networkUser, int processID, boolean isClient){
        DownloadProcess download = new DownloadProcess(processID, fileName, networkUser, filePath, isClient, slidingWindow);
        runningProcesses[processID] = download;
        networkUser.getStatics().startingProcess(processID);
    }

    public void printRunningProcesses(){
        for(int i = 0; i < runningProcesses.length; i++){
            Process process = runningProcesses[i];
            int processID = process.getProcessID();
            String filename = process.getFileName();
            print("Process "+ processID+ " is uploading file "+ filename);
        }
    }

    public void printPausedProcesses(){
        for(int i = 0; i < pausedProcesses.length; i++){
            Process process = pausedProcesses[i];
            int processID = process.getProcessID();
            String filename = process.getFileName();
            print("Process "+ processID+ " is paused while uploading file "+ filename);
        }
    }

    public void printAllProcesses(){
        printRunningProcesses();
        printPausedProcesses();
    }

    public void pauseSpecificProcess(int userIDSugested){
        int IDfound = 0;
        int processID = 0;

        for(int i = 0; i < runningProcesses.length; i++){
            if(runningProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
            }
        }

        if(IDfound == 1){
            print("Process "+ processID + "is paused");

            runningProcesses[processID].setIsInterrupted(true);
            pausedProcesses[processID] = runningProcesses[processID];
            runningProcesses[processID] = null;

            //also send the server to pause the process
            byte[] buffer = packetWithOwnHeader.commandoTen(processID);
            DatagramPacket pausePacket = new DatagramPacket(buffer, buffer.length);
            networkUser.send(pausePacket);

        } else{
            print("That is not a correct processID, these are the processes to choose from:");
            printRunningProcesses();
            pauseSpecificProcess(userIDSugested);
        }
    }


    public void pauseAllProcesses(){
        for(int i = 0; i < runningProcesses.length; i++){
            if(runningProcesses[i] != null){
                int processID = runningProcesses[i].getProcessID();
                runningProcesses[i].setIsInterrupted(true);
                pausedProcesses[i] = runningProcesses[i];
                runningProcesses[i] = null;
                print("Process "+ processID + "is paused");

                //also send the server to pause the process
                byte[] buffer = packetWithOwnHeader.commandoTen(processID);
                DatagramPacket pausePacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(pausePacket);
            }
        }
    }

    public void continueSpecificProcess(int userIDSugested){
        int IDfound = 0;
        int processID = 0;

        for(int i = 0; i < pausedProcesses.length; i++){
            if(pausedProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
            }
        }


        if(IDfound == 1){
            print("Process "+ processID + "is continued");

            pausedProcesses[processID].setIsInterrupted(false);
            runningProcesses[processID] = pausedProcesses[processID];
            pausedProcesses[processID] = null;

            //also send the server to continue the process
            byte[] buffer = packetWithOwnHeader.commandoEleven(processID);
            DatagramPacket continuePacket = new DatagramPacket(buffer, buffer.length);
            networkUser.send(continuePacket);

        } else{
            print("That is not a correct processID, these are the processes to choose from:");
            printRunningProcesses();
            continueSpecificProcess(userIDSugested);
        }
    }

    public void continueAllProcesses(){
        for(int i = 0; i < pausedProcesses.length; i++){
            if(pausedProcesses[i] != null) {
                int processID = pausedProcesses[i].getProcessID();
                pausedProcesses[i].setIsInterrupted(false);
                runningProcesses[i] = pausedProcesses[i];
                pausedProcesses[i] = null;
                print("Process " + processID + "is continued");

                //also send the server to continue the process
                byte[] buffer = packetWithOwnHeader.commandoEleven(processID);
                DatagramPacket continuePacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(continuePacket);
            }
        }
    }

    public void stopSpecificProcess(int userIDSugested){
        int IDfound = 0;
        int processID = 0;
        String state = "";

        for(int i = 0; i < runningProcesses.length; i++){
            if(runningProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
                state = "RUNNING";
            }
        }
        for(int i = 0; i < pausedProcesses.length; i++){
            if(pausedProcesses[i].getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
                state = "PAUSED";
            }
        }

        if(IDfound == 1){
            print("Process "+ processID + "is stopped");
            if(state.equals("RUNNING")){
                runningProcesses[processID].kill();
                runningProcesses[processID] = null;

                //also send the server to stop the process
                byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
                DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(stopPacket);
            } else if(state.equals("PAUSED")){
                pausedProcesses[processID].kill();
                pausedProcesses[processID] = null;

                //also send the server to stop the process
                byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
                DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(stopPacket);
            }
        } else{
            print("That is not a correct processID, these are the processes to choose from:");
            printRunningProcesses();
            printPausedProcesses();
        }
    }

    public void stopAllProcesses(){
        for(int i = 0; i < runningProcesses.length; i++){
            if(runningProcesses[i] != null) {
                runningProcesses[i].kill();
                runningProcesses[processID] = null;
                print("Process " + processID + "is stopped");

                //also send the server to stop the process
                byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
                DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(stopPacket);
            }
        }

        for(int i = 0; i < pausedProcesses.length; i++){
            if(pausedProcesses[i] != null) {
                pausedProcesses[i].kill();
                pausedProcesses[processID] = null;
                print("Process " + processID + "is stopped");

                //also send the server to stop the process
                byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
                DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(stopPacket);
            }
        }
    }

    public void receiveUploadAcknowledgement(int processID){
        if(runningProcesses[processID] != null && runningProcesses[processID] instanceof UploadProcess){
            UploadProcess upload = (UploadProcess) runningProcesses[processID];
            upload.setAcknowledgementToStartTrue();
        }
    }

    public void receivePacketForProcess(int processID, DatagramPacket receivedPacked){
        if (runningProcesses[processID] != null && runningProcesses[processID] instanceof DownloadProcess){
            DownloadProcess downloadProcess = (DownloadProcess) runningProcesses[processID];
            downloadProcess.receivePacket(receivedPacked);
        }
    }

    public void receiveAcknowledgementPacketForProcess(int processID, DatagramPacket receivedPacked){
        if (runningProcesses[processID] != null && runningProcesses[processID] instanceof UploadProcess){
            UploadProcess uploadProcess = (UploadProcess) runningProcesses[processID];
            uploadProcess.receiveAcknowledgementPacket(receivedPacked);
        }
    }

    public void receiveLastPacketForProcess(int processID, DatagramPacket receivedPacked){
        if (runningProcesses[processID] != null && runningProcesses[processID] instanceof DownloadProcess){
            DownloadProcess downloadProcess = (DownloadProcess) runningProcesses[processID];
            downloadProcess.receiveLastPacket(receivedPacked);
        }
    }

    public void receiveAcknowledgementLastPacketForProcess(int processID){
        if(runningProcesses[processID] != null && runningProcesses[processID] instanceof UploadProcess){
            UploadProcess upload = (UploadProcess) runningProcesses[processID];
            upload.setAcknowledgementToStopTrue();
        }
    }

    private static void print (String message){
        System.out.println(message);
    }
}
