package OwnCode;

public class Oefenen {
    Utils utils;
    Utils.Timer timer;

    public static void main(String[] args) {
        Oefenen oefenen = new Oefenen();
    }

    public Oefenen(){
        startUp();
        byte[] b = new byte[1];
        b[0]=1;


        String s = utils.fromByteArrToStringBit(b);

        print(s);

        String[] sarray = s.split("");

        for(int i = 0; i < sarray.length; i++){
            print(sarray[i]);
        }




    }

    public void startUp(){
        utils = new Utils();
    }





    private static void print (String message){
        System.out.println(message);
    }
}
