package OwnCode;

public class Oefenen {
    Utils utils;
    Utils.Timer timer;
    Checksum checksum;

    public static void main(String[] args) {
        Oefenen oefenen = new Oefenen();
    }

    public Oefenen(){
        startUp();
        byte[] b = new byte[5];
        b[0]=1;
        b[1] = 2;
        b[2]=9;
        b[3]=10;


        String s = utils.fromByteArrToStringBit(b);

        print(s);


        String[] sarray = s.split("");

        String[] reminder = checksum.removingFirstZeros(sarray);
        print(Integer.toString(reminder.length));

        for(int i = 0; i < reminder.length; i++){
            print(reminder[i]);
        }




    }

    public void startUp(){
        utils = new Utils();
        checksum = new Checksum();
    }





    private static void print (String message){
        System.out.println(message);
    }
}
