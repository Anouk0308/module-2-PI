# Module 2 Final Assignment

###### Nedap University 4.0 

###### Authors

 - @anoukschoenmakers

## Getting started
- Download this project and place it in a folder of your choosing.
- Create a new folder at a convenient place. In this folder, files will be stored to upload en downloaded files will be placed. Check the path to this folder (for example:"/Users/anouk.schoenmakers/Desktop/ClientFiles").

## Connecting with the PI
- Connect via wifi with the PI (my PI is called nu-pi-anouk).
- In your terminal, type: ssh pi@172.16.0.0
- password: raspberry
(don't close the terminal, which will from now on pe referred to as the PI terminal)

If your computer cannot find the PI wifi:
- Go to your wifi menu
- Choose Join Other Network...
   - SSID: `nu-pi-anouk`
   - Security: `WPA2` (when needed, choose WPA/WPA2 Personal)
   - WiFi password: `nedap1234`
   - Static IP address: `172.16.1.1`
 
## Creating a new folder on the PI
Just like with the first folder, this folder will hold files for uploading & downloading.
- Go to the terminal where you're connected with the PI
- type: mkdir name_your_folder (for example: mkdir ServerFiles)
- type: cd name_your_folder
- type: pwd
This will give you the path to the folder (for example:"/home/pi/ServerFiles")
(don't close the terminal)

## Checking if hardcoded variables are correct
The folderpaths you have checked, need to be added in de project code.
- Go to the project via a IDE
- Go to the class UserInputHandler
- Change the String folderPath to your folder path (for example: private static String folderPath ="/Users/anouk.schoenmakers/Desktop/ClientFiles";
- Go to the class Server
- Change the String folderPathPI to your folder path (for example:  private static String folderPathPI = "/home/pi/ServerFiles";)
- Go to the class Hardcoded and run it
- Change the String computerString to what is printed on the IDE terminal (for example: String computerString = "192.168.178.27");)

## Using the Gradle project
- In your IDE, go to your Gradle tasks, and choose BUILD
- when it is finished, choose DEPLOY

## Execute the program
- In your PI terminal, type: sudo systemctl stop num2.service
- than type: java -jar NUM2.jar
The server is now running on the PI
- Go to your IDE, and run the class MainClient
The client is now running on your computer

## Using the program
- In the IDE terminal, a menu appears
- Choose the number of the action you want to perform

## Stopping the program
- In the IDE terminal, type: 14
The client is now stopped
- In the PI terminal, type: sudo shutdown -P now
The server is now stopped, it is now safe to disconnect the power to your PI
