package OwnCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.util.Arrays;

public class UserInputHandler {
    private BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

    private Client client;
    private static boolean isClient = true;

    private String[] filesClient;//todo get real files
    private static String filePath = "Macintosh HD/Users/anouk.schoenmakers/Documents/raspberry/nu-module-2/src/"; // where are the files placed
    private String[] filesPI;
    private boolean updatedFilesPI = false;

    private ProcessManager processManager;
    private PacketWithOwnHeader packetWithOwnHeader;
    private Statistics statistics;


    public UserInputHandler(Client client, ProcessManager processManager, Statistics statistics){
        this.client = client;
        this.processManager = processManager;
        this.statistics = statistics;
        packetWithOwnHeader = new PacketWithOwnHeader();

        //fake todo: echte erin
        filesClient = new String[2];
        filesClient[0] = "file.java";
        filesClient[1] = "text.txt";


        startMenu();
    }

    public void startMenu(){
        print("Welkom to this network");
        print("What would you like to do?");
        print("1. Get the filelist from this computer");
        print("2. Get the filelist from the PI");
        print("3. Upload a file to the PI");
        print("4. Download a file from the PI");
        print("5. See all running processes");
        print("6. See all paused processes");
        print("7. See all processes");
        print("8. Pause a specific process");
        print("9. Pause all processes");
        print("10. Continue a specific process");
        print("11. Continue all processes");
        print("12. Stop a specific process");
        print("13. Stop all processes");
        print("14. Close this program");
        userInputHandler();
    }

    public void userInputHandler(){
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                switch (thisLine){
                    case "1":           printOwnFiles();
                        break;
                    case "2":           printPIFiles();
                        break;
                    case "3":           uploadFile();
                        break;
                    case "4":           downloadFile();
                        break;
                    case "5":           processManager.printRunningProcesses();
                        break;
                    case "6":           processManager.printPausedProcesses();
                        break;
                    case "7":           processManager.printAllProcesses();
                        break;
                    case "8":           pauseSpecificProcess();
                        break;
                    case "9":           processManager.pauseAllProcesses();
                        break;
                    case "10":          continueSpecificProcess();
                        break;
                    case "11":          processManager.continueAllProcesses();
                        break;
                    case "12":          stopSpecificProcess();
                        break;
                    case "13":          processManager.stopAllProcesses();
                        break;
                    case "14":          stopProgram();
                        break;
                    case "15":          getStatistics();
                        break;
                    default:            startMenu();
                        break;
                }
            }

        } catch (IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void printOwnFiles(){
        for(int i = 0; i < filesClient.length; i++){
            print(filesClient[i]);
        }
    }

    public void printPIFiles(){
        byte[] buffer = packetWithOwnHeader.commandoOne();
        DatagramPacket askFiles = new DatagramPacket(buffer, buffer.length);
        try {
            client.send(askFiles);

            while (!updatedFilesPI) {//wait till FilesPI are received & updated
                Thread.sleep(10);
            }

            for (int i = 0; i < filesPI.length; i++) {
                print(filesPI[i]);
            }
            updatedFilesPI = false;

            print("Would you like to do something else?");
            startMenu(); //todo: kijken of dit niet mooier kan, dus bv met yes/no. yes = menu, no = close program

        } catch(InterruptedException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void uploadFile(){
        print("Which file would you like to upload?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                if(Arrays.asList(filesClient).contains(thisLine)){
                    String filename = thisLine;
                    String pathname = filePath + filename;//todo:kijken of dit zo werkt?
                    File file = new File(pathname);
                    processManager.createUploadProcess(file, client, isClient);
                } else{
                    print("That is not a correct filename, these are the files to choose from:");
                    printOwnFiles();
                    uploadFile();
                }
            }
        } catch (IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void downloadFile(){
        print("Which file would you like to download?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                if(Arrays.asList(filesClient).contains(thisLine)){
                    String filename = thisLine;
                    processManager.createDownloadProcess(filename, filePath, client, isClient);
                } else{
                    print("That is not a correct filename, these are the files to choose from:");
                    printPIFiles();
                    uploadFile();
                }
            }
        } catch (IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void pauseSpecificProcess(){
        print("Which process would you like to pause?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                int userIDSugested = Integer.parseInt(thisLine);
                processManager.pauseSpecificProcess(userIDSugested);
            }
        } catch(IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void continueSpecificProcess() {
        print("Which process would you like to continue?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                int userIDSugested = Integer.parseInt(thisLine);
                processManager.continueSpecificProcess(userIDSugested);
            }
        }catch(IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void stopSpecificProcess() {
        print("Which process would you like to continue?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                int userIDSugested = Integer.parseInt(thisLine);
                processManager.stopSpecificProcess(userIDSugested);
            }
        }catch(IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void stopProgram(){
        try {
            print("Trying to stop program");//todo weghalen
            processManager.stopAllProcesses();
            print("All processes are stopped");
            client.getSocket().close();
            print("There is no more connection with the server");
            userInput.close();
            print("The programm is now fully closed");
        } catch (IOException e) {
            print("Error when closing!");
            e.printStackTrace();
        }
    }

    public void getStatistics(){
        statistics.getStatistics();
    }

    public void setFilesPI(String[] filesPI) {
        this.filesPI = filesPI;
    }

    public void setUpdatedFilesPI(boolean updated){
        updatedFilesPI = updated;
    }

    private static void print (String message){
        System.out.println(message);
    }
}

