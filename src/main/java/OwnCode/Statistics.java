package OwnCode;

import java.util.HashMap;

public class Statistics {

    int corruptedPackets = 0;//didn't make the checksum
    int highestDownloadSpeed;//miliseconds per byte
    int averageDownloadSpeed;//miliseconds per byte

    HashMap<Integer, Integer> hmapRunning = new HashMap<Integer, Integer>();//processID & startingTime
    LoadingInformation[] loadingInformations = new LoadingInformation[1000];//processID & timeToLoad & bytesToLoad //todo kijken of dit netter kan

    public Statistics(){

    }

    public int[] getStatistics(){
        int[] statistics = new int[3];
        statistics[0]=corruptedPackets;
        statistics[1]=highestDownloadSpeed;
        statistics[2]=averageDownloadSpeed;

        return statistics;
    }

    public void foundCorruptedPacket(){
        corruptedPackets++;
    }

    public void startingProcess(int processID){
        int startingTime = (int) System.currentTimeMillis();
        hmapRunning.put(processID, startingTime);
    }

    public void stoppingProcess(int processID, int bytesToLoad){
        int stoppingTime = (int) System.currentTimeMillis();
        int startingTime = hmapRunning.get(processID);
        hmapRunning.remove(processID);
        int timeToLoad = stoppingTime - startingTime;
        LoadingInformation loadingInformationTemp = new LoadingInformation(processID, timeToLoad, bytesToLoad);
        calculateHighestDownLoadSpeed();
        calculateAverageDownloadSpeed();
    }

    public void calculateHighestDownLoadSpeed(){
        int stoppedProcessed = loadingInformations.length;
        for(int i = 0; i < stoppedProcessed; i++){
            LoadingInformation loadingInformationTemp = loadingInformations[i];

            int downloadTimeProcessI = loadingInformationTemp.getTimeToLoad();
            int bytesToLoad = loadingInformationTemp.getBytesToLoad();
            int downloadSpeedProcessI = downloadTimeProcessI/bytesToLoad;

            if(downloadSpeedProcessI > highestDownloadSpeed){
                highestDownloadSpeed = downloadSpeedProcessI;
            }
        }
    }

    public void calculateAverageDownloadSpeed(){
        int stoppedProcessed = loadingInformations.length;
        int totalDownloadSpeed = 0;
        for(int i = 0; i < stoppedProcessed; i++){
            LoadingInformation loadingInformationTemp = loadingInformations[i];

            int downloadTimeProcessI = loadingInformationTemp.getTimeToLoad();
            int bytesToLoad = loadingInformationTemp.getBytesToLoad();
            int downloadSpeedProcessI = downloadTimeProcessI/bytesToLoad;

            totalDownloadSpeed = totalDownloadSpeed + downloadSpeedProcessI;
        }
        averageDownloadSpeed = totalDownloadSpeed/stoppedProcessed;
    }
}
