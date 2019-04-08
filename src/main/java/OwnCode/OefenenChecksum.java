package OwnCode;

public class OefenenChecksum {
    private Utils utils;
    int generator = 60;

    public OefenenChecksum(){
        utils = new Utils();
    }

    public static void main(String[] args) {
        OefenenChecksum oefenenChecksum = new OefenenChecksum();
        String s = "Aape";
        byte reminder = oefenenChecksum.functie(s);
        oefenenChecksum.check(s, reminder);
        byte i = (byte) ((50 & 0x000000ff));
        oefenenChecksum.check(s, i);




    }

    public byte functie(String s){
        System.out.println(s);
        byte[] bytes = utils.fromStringToByteArr(s);

        byte sum = 0;

        for(int i = 0; i < bytes.length; i++){
            System.out.println("byte "+i+ ":"+bytes[i]);
            sum += bytes[i];
        }

        System.out.println("sum in byte:"+sum);
        int sumInt = utils.fromByteToInteger(sum);
        System.out.println("sum in int:"+sumInt);

        int reminder = sumInt % generator;
        System.out.println("reminder:"+reminder);
        byte reminderByte = utils.fromIntegerToByte(reminder);
        System.out.println("reminder in bytes:"+reminderByte);

        return reminderByte;
    }

    public void check(String s, byte reminder){
        byte[] bytes = utils.fromStringToByteArr(s);

        byte sum = 0;

        for(int i = 0; i < bytes.length; i++){
            sum += bytes[i];
        }

        int sumInt = utils.fromByteToInteger(sum);
        int reminders = sumInt % generator;
        byte reminderByte = utils.fromIntegerToByte(reminders);

        if(reminder == reminderByte){
            System.out.println("correct");
        } else{
            System.out.println("Incorrect");
        }



    }



}
