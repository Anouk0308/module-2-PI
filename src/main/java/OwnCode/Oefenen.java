package OwnCode;

public class Oefenen {
    Utils utils;
    Utils.Timer timer;

    public static void main(String[] args) {
        Oefenen oefenen = new Oefenen();
    }

    public Oefenen(){
        startUp();
        byte[] a = new byte[10];
        byte[] b = new byte[8];
        byte[] c = new byte[75];
        byte[] d = new byte[3];
        byte[] e = combineByteArrayTesten(a,b,c,d);


    }

    public void startUp(){
        utils = new Utils();
    }

    public byte[] combineByteArrayTesten(byte[]a,byte[]b,byte[]c,byte[]d){
        byte[] by = null;
        if(d!=null){
            by = utils.combineByteArr(a,b,c,d);
        } else if(c!=null){
            by = utils.combineByteArr(a,b,c);
        } else{
           by =  utils.combineByteArr(a,b);
        }
        return by;
    }




    private static void print (String message){
        System.out.println(message);
    }
}
