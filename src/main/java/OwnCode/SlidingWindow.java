package OwnCode;

public class SlidingWindow {
    private int windowSize = 10;
    private int packetSize = 512;
    private int headerSpace = 20; //todo kijken naar wat hier mee te doen
    private int rawDataSpace = packetSize - headerSpace;

    public void setWindowSize(int size){
        this.windowSize = size;
    }

    public void setPacketSize(int size){
        this.packetSize = size;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getRawDataSpace() {
        return rawDataSpace;
    }
}
