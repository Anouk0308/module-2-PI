package OwnCode;

import java.net.DatagramPacket;

public interface NetworkUser {

    void connect();
    void inputHandler(DatagramPacket p);
    void send(DatagramPacket p);
    ProcessManager getProcessManager();
    void print(String s);
    Statistics getStatics();

}
