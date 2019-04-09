package OwnCode;

public interface Process {

    public void kill();
    public int getProcessID();
    public String getFileName();
    public void setIsInterrupted(boolean b);
}
