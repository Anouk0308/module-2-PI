package OwnCode;

public class Oefenen {
    Utils utils;

    public static void main(String[] args) {
        Oefenen oefenen = new Oefenen();
    }

    public Oefenen(){
        startUp();
        int i = Math.floorDiv( 20, 255);
        print(Integer.toString(i));
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
