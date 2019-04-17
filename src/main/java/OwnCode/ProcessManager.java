package OwnCode;

import java.io.File;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

public class ProcessManager {
    private Map<Integer, Process> runningProcesses;
    private Map<Integer, Process> pausedProcesses;
    private Map<Integer, Thread> threads;

    int processID = -1;

    private PacketWithOwnHeader packetWithOwnHeader;
    private NetworkUser networkUser;
    private SlidingWindow slidingWindow;

    public ProcessManager(NetworkUser networkUser, SlidingWindow slidingWindow){
        this.networkUser = networkUser;
        this.slidingWindow = slidingWindow;
        packetWithOwnHeader = new PacketWithOwnHeader();

        runningProcesses = new HashMap<>();
        pausedProcesses = new HashMap<>();
        threads = new HashMap<>();
    }

    public int getAProcessID(){
        processID++;
        return processID;
    }

    public void createUploadProcess(File file, NetworkUser networkUser, boolean isClient, int numberOfBytesToLoad){
        int processID = getAProcessID();
        UploadProcess upload = new UploadProcess(processID, file, networkUser, isClient, slidingWindow, numberOfBytesToLoad);
        Thread thread = new Thread(upload);
        runningProcesses.put(processID, upload);
        threads.put(processID,thread);
        thread.start();
        networkUser.getStatics().startingProcess(processID);
    }

    public void createDownloadProcess(String fileName, String folderPath, NetworkUser networkUser, boolean isClient, int numberOfBytesToLoad){
        int processID = getAProcessID();
        DownloadProcess download = new DownloadProcess(processID, fileName, networkUser, folderPath, isClient, numberOfBytesToLoad);
        Thread thread = new Thread(download);
        runningProcesses.put(processID, download);
        threads.put(processID,thread);
        thread.start();
        networkUser.getStatics().startingProcess(processID);
    }

    public void createUploadProcessWithProcessID(File file, NetworkUser networkUser, int processID, boolean isClient, int numberOfBytesToLoad){
        UploadProcess upload = new UploadProcess(processID, file, networkUser, isClient, slidingWindow, numberOfBytesToLoad);
        Thread thread = new Thread(upload);
        runningProcesses.put(processID, upload);
        threads.put(processID,thread);
        thread.start();
        networkUser.getStatics().startingProcess(processID);
    }

    public void createDownloadProcessWithProcessID(String fileName, String folderPath, NetworkUser networkUser, int processID, boolean isClient, int numberOfBytesToLoad){
        DownloadProcess download = new DownloadProcess(processID, fileName, networkUser, folderPath, isClient, numberOfBytesToLoad);
        Thread thread = new Thread(download);
        runningProcesses.put(processID, download);
        threads.put(processID,thread);
        thread.start();
        networkUser.getStatics().startingProcess(processID);
    }

    public void printRunningProcesses(){
        for(int i = 0; i < runningProcesses.size(); i++){
            Process process = runningProcesses.get(i);
            if(process != null){
                int processID = process.getProcessID();
                String filename = process.getFileName();
                print("Process "+ processID+ " is uploading file "+ filename);
            }
        }
    }

    public void printPausedProcesses(){
        for(int i = 0; i < pausedProcesses.size(); i++){
            Process process = pausedProcesses.get(i);
            if(process != null){
                int processID = process.getProcessID();
                String filename = process.getFileName();
                print("Process "+ processID+ " is paused while uploading file "+ filename);
            }
        }
    }

    public void printAllProcesses(){
        printRunningProcesses();
        printPausedProcesses();
    }

    public void pauseSpecificProcess(int userIDSugested){
        int IDfound = 0;
        int processID = 0;

        for(int i = 0; i < runningProcesses.size(); i++){
            if(runningProcesses.get(i) != null && runningProcesses.get(i).getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
            }
        }

        if(IDfound == 1){
            print("Process "+ processID + "is paused");

            runningProcesses.get(processID).setIsInterrupted(true);
            pausedProcesses.put(processID,runningProcesses.get(processID));
            runningProcesses.put(processID, null);

            //send the server to also pause the process
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
        for(int i = 0; i < runningProcesses.size(); i++){
            if(runningProcesses.get(i) != null){
                int processID = runningProcesses.get(i).getProcessID();
                runningProcesses.get(i).setIsInterrupted(true);
                pausedProcesses.put(i,runningProcesses.get(i));
                runningProcesses.put(i,null);
                print("Process "+ processID + "is paused");

                //send the server to also pause the process
                byte[] buffer = packetWithOwnHeader.commandoTen(processID);
                DatagramPacket pausePacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(pausePacket);
            }
        }
    }

    public void continueSpecificProcess(int userIDSugested){
        int IDfound = 0;
        int processID = 0;

        for(int i = 0; i < pausedProcesses.size(); i++){
            if(pausedProcesses.get(i) != null && pausedProcesses.get(i).getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
            }
        }

        if(IDfound == 1){
            print("Process "+ processID + "is continued");

            Process process = pausedProcesses.get(processID);
            process.setIsInterrupted(false);
            if(process instanceof UploadProcess){
                ((UploadProcess) process).continueProcess();
            }
            runningProcesses.put(processID,pausedProcesses.get(processID));
            pausedProcesses.put(processID,null);

            //send the server to also continue the process
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
        for(int i = 0; i < pausedProcesses.size(); i++){
            if(pausedProcesses.get(i) != null) {
                int processID = pausedProcesses.get(i).getProcessID();
                pausedProcesses.get(i).setIsInterrupted(false);
                runningProcesses.put(i, pausedProcesses.get(i));
                pausedProcesses.put(i, null);
                print("Process " + processID + "is continued");

                //send the server to also continue the process
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

        for(int i = 0; i < runningProcesses.size(); i++){
            if(runningProcesses.get(i) != null && runningProcesses.get(i).getProcessID() == userIDSugested) {
                    IDfound = 1;
                    processID = i;
                    state = "RUNNING";
            }
        }
        for(int i = 0; i < pausedProcesses.size(); i++){
            if(pausedProcesses.get(i) != null && pausedProcesses.get(i).getProcessID() == userIDSugested){
                IDfound = 1;
                processID = i;
                state = "PAUSED";
            }
        }

        if(IDfound == 1){
            print("Process "+ processID + " is stopped");
            if(state.equals("RUNNING")){
                runningProcesses.get(processID).kill();
                runningProcesses.put(processID, null);

                //send the server to also stop the process
                byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
                DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(stopPacket);
            } else if(state.equals("PAUSED")){
                pausedProcesses.get(processID).kill();
                pausedProcesses.put(processID,null);

                //send the server to also stop the process
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
        for(int i = 0; i < runningProcesses.size(); i++){
            if(runningProcesses.get(i) != null) {
                runningProcesses.get(i).kill();
                runningProcesses.put(processID,null);
                print("Process " + processID + "is stopped");

                //send the server to also stop the process
                byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
                DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(stopPacket);
            }
        }

        for(int i = 0; i < pausedProcesses.size(); i++){
            if(pausedProcesses.get(i) != null) {
                pausedProcesses.get(i).kill();
                pausedProcesses.put(processID,null);
                print("Process " + processID + "is stopped");

                //send the server to also stop the process
                byte[] buffer = packetWithOwnHeader.commandoTwelve(processID);
                DatagramPacket stopPacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(stopPacket);
            }
        }
    }

    public void receiveUploadAcknowledgement(int processID){
        if(runningProcesses.get(processID) != null && runningProcesses.get(processID) instanceof UploadProcess){
            UploadProcess upload = (UploadProcess) runningProcesses.get(processID);
            upload.setAcknowledgementToStartTrue();
        }
    }

    public void receivePacketForProcess(int processID, DatagramPacket receivedPacked){
        if (runningProcesses.get(processID) != null && runningProcesses.get(processID) instanceof DownloadProcess){
            DownloadProcess downloadProcess = (DownloadProcess) runningProcesses.get(processID);
            downloadProcess.receivePacket(receivedPacked);
        }
    }

    public void receiveAcknowledgementPacketForProcess(int processID, DatagramPacket receivedPacked){
        if (runningProcesses.get(processID) != null && runningProcesses.get(processID) instanceof UploadProcess){
            UploadProcess uploadProcess = (UploadProcess) runningProcesses.get(processID);
            uploadProcess.receiveAcknowledgementPacket(receivedPacked);
        }
    }

    public void receiveLastPacketForProcess(int processID, DatagramPacket receivedPacked){
        if (runningProcesses.get(processID) != null && runningProcesses.get(processID) instanceof DownloadProcess){
            DownloadProcess downloadProcess = (DownloadProcess) runningProcesses.get(processID);
            downloadProcess.receiveLastPacket(receivedPacked);
        }
    }

    public void receiveAcknowledgementLastPacketForProcess(int processID){
        if(runningProcesses.get(processID) != null && runningProcesses.get(processID) instanceof UploadProcess){
            UploadProcess upload = (UploadProcess) runningProcesses.get(processID);
            upload.setAcknowledgementToStopTrue();
        }
    }

    public boolean containsProcess(int processID){
        int longestList;
        if(runningProcesses.size()>pausedProcesses.size()){
            longestList = runningProcesses.size();
        } else{
            longestList = pausedProcesses.size();
        }

        for(int i = 0; i < longestList; i ++){
            if(runningProcesses.get(i) != null && runningProcesses.get(i).getProcessID() == processID || pausedProcesses.get(i) != null && pausedProcesses.get(i).getProcessID() == processID) {
                return true;
            }
        }
        return false;
    }

    private void print (String message){
        networkUser.print(message);
    }
}
