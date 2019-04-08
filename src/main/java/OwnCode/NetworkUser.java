package OwnCode;

import java.net.DatagramPacket;

public interface NetworkUser {

    public void connect();
    public void inputHandler(DatagramPacket p);
    public void send(DatagramPacket p);
    public ProcessManager getProcessManager();

}
