package OwnCode;

public class LoadingInformation {
    int processID;
    int timeToLoad;
    int bytesToLoad;

    public LoadingInformation(int processID, int timeToLoad, int bytesToLoad){
        this.processID = processID;
        this.timeToLoad = timeToLoad;
        this.bytesToLoad = bytesToLoad;
    }

    public int getProcessID(){
        return processID;
    }

    public int getTimeToLoad(){
        return timeToLoad;
    }

    public int getBytesToLoad() {
        return bytesToLoad;
    }
}
