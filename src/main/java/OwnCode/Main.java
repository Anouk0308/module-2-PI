package OwnCode;

public class Main {

    private Main() {}

    public static void main(String[] args) {
        Client client = new Client("172.16.1.1", 8888);
        Thread clientThread = new Thread(client);
        clientThread.start();
    }


}