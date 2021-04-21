# 20CS047 WifiPay

## Project Information
Project Topic: QR-code Payment System with Wi-Fi Direct Proximity Checking

Project Code: 20CS047

## Development Environment

1. Android Studio

2. Android Minimum SDK version 26 (8.0 Oreo)

3. Java JDK 1.8

4. Google Firebase Authentication and Firebase Cloud Firestore




## Minimum Android version to use this application:

Android 8.0 (API 26) or above





## Github link:

https://github.com/wwchan29/20CS047





## Installation and Setup Guide
There are two methods to install and setup the application:

Method (1):

1.Download the source code zip files from FYPMS Media / Clone the source code zip files from Github and unzip the files

2.Launch Android Studio and Click [Import the project] 

3.For the directory, go to the unzipped folder, select "MyNewApp" with an Android icon next to it, click [OK].

4.Go to [File] -> Click [Sync project with Gradle files]

5a. You can EITHER connect a real Android device to the computer by USB driver and run the 'app' on the device. (Need to turn on developer mode and allow USB debugging on the device)

5b. OR go to [Build] -> [Build APK] to generate the APK file of this application -> then transfer the APK file to your Android device -> Install the APK and launch this application.

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Method (2):

Download the APK file of this application from the below link and install the APK file on the Android devices (Note you may need to go to Settings of the device to enable installation of application from other sources)
https://drive.google.com/file/d/1PNV8K7JWOBkfxBa1gicVSmoXJPL6NkrG/view?usp=sharing





## Settings and Configurations required to use this application

1. You need to turn on Wi-fi and have an Internet connection when using this application.

2. You need to turn on Location mode on your device in order to search for nearby devices and connect with them by Wi-Fi direct.

3. At the first time launching the application, when trying to click Pay to start a payment or proceed to the Wi-Fi direct connection page, the system will prompt a dialog asking the in-app [Camera] and [Location] permission, click [Allow] in order to use the payment functions normally.

4. Both devices involved in the payment should have an Android version of 8.0 or above.

  
  
  

## Issues regarding the usage and possible solutions:

1.If payment authentication keeps loading and does not stop, this could be caused by improper sign out of the account, forced closing and re-opening of application, switch account without signing out the original account or past Wifi-direct connections not clear or disconnect properly. This will lead to an invalid login status on Firebase Authentication and the system could not identify the current user involved in the payment.

Solution: Sign out all the users involved in the payment, close and Restart the application, login and conduct the payment once again, the payment authentication loading issue should be solved.

2. When scanning QR Code, the error message "QR Code has expired.." pops up, even refresh and re-scan the QR Code does not solve this issue.

Solution: Ensure the camera is in focus with the QR code at the moment it is being captured. Also the camera direction should align with QR Code. The above issue happens when the camera is out of focus with the QR code and the QR Code is blurred at the moment it is being scanned, which the information of the QR code cannot be read and decode correctly and leads to the error message of "QR Code has expired".




## Devices used in testing and demonstration:
Samsung Galaxy S9 (Android 9)

Huawei Nexus 6P (Android 8.1)
