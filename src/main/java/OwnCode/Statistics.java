package OwnCode;


import java.util.HashMap;
import java.util.Map;

public class Statistics {

    int corruptedPackets = 0;//didn't make the checksum
    int highestDownloadSpeed = 0;//miliseconds per byte
    int averageDownloadSpeed = 0;//miliseconds per byte

    Map<Integer, Integer> hmapRunning;//processID & startingTime
    Map<Integer, LoadingInformation> loadingInformations;//processID & timeToLoad & bytesToLoad

    public Statistics(){
        hmapRunning = new HashMap<>();
        loadingInformations = new HashMap();
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
        loadingInformations.put(processID, loadingInformationTemp);

        calculateHighestDownLoadSpeed();
        calculateAverageDownloadSpeed();
    }

    public void calculateHighestDownLoadSpeed(){
        if(loadingInformations.size()>0){
            int stoppedProcessed = loadingInformations.size();
            for(int i = 0; i < stoppedProcessed; i++){
                LoadingInformation loadingInformationTemp = loadingInformations.get(i);

                int downloadTimeProcessI = loadingInformationTemp.getTimeToLoad();
                int bytesToLoad = loadingInformationTemp.getBytesToLoad();
                int downloadSpeedProcessI = bytesToLoad/downloadTimeProcessI;

                if(downloadSpeedProcessI > highestDownloadSpeed){
                    highestDownloadSpeed = downloadSpeedProcessI;
                }
            }
        }
    }

    public void calculateAverageDownloadSpeed(){
        if(loadingInformations.size()>0){
            int stoppedProcessed = loadingInformations.size();
            int totalDownloadSpeed = 0;
            for(int i = 0; i < stoppedProcessed; i++){
                LoadingInformation loadingInformationTemp = loadingInformations.get(i);

                int downloadTimeProcessI = loadingInformationTemp.getTimeToLoad();
                int bytesToLoad = loadingInformationTemp.getBytesToLoad();
                int downloadSpeedProcessI = bytesToLoad/downloadTimeProcessI;

                totalDownloadSpeed = totalDownloadSpeed + downloadSpeedProcessI;
            }
            averageDownloadSpeed = totalDownloadSpeed/stoppedProcessed;
        }
    }
}
