package OwnCode;

public class Oefenen {
    Utils utils;
    Utils.Timer timer;

    public static void main(String[] args) {
        Oefenen oefenen = new Oefenen();
    }

    public Oefenen(){
        startUp();
        timer =utils.new Timer(10);
        try{
            while (!timer.isTooLate()) {//wait till PI tells that the uploading process can start
                print("not to late");
                Thread.sleep(1);
            }
            print("too late");
        } catch (InterruptedException e) {
            print("Client error: " + e.getMessage());
        }


    }

    public void startUp(){
        utils = new Utils();
    }

    public void intbitint(int integer){
        print(Integer.toString(integer));
        byte bytetje = utils.fromIntegerToByte(integer);
        print(Byte.toString(bytetje));
        int integertje = utils.fromByteToInteger(bytetje);
        print(Integer.toString(integertje));
    }




    private static void print (String message){
        System.out.println(message);
    }
}
