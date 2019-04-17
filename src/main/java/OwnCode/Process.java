package OwnCode;

public interface Process {
    void kill();
    int getProcessID();
    String getFileName();
    void setIsInterrupted(boolean b);
    void whenTimerWentOff();
}
