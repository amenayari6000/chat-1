# My Chat App

A feature-rich and scalable chat application developed using Kotlin for the frontend, Firebase for real-time database and authentication, and Ktor for building APIs. The app is deployed on AWS Elastic Beanstalk and utilizes several modern technologies and best practices for optimal performance and scalability.

## Features

My chat application offers a variety of features:

- **Authentication:** Secure login and registration using Firebase Authentication.
- **Send Messages:** Allows users to send audio, image, and text messages seamlessly.
- **Change Profile Picture:** Users can easily update their profile pictures.
- **View User Information:** Users can view the profiles and data of other users, including online status.
- **Online/Offline Status:** Real-time indication of whether users are online or offline.
- **Notifications:** Push notifications are sent to users for new messages and other important updates.
- **Message Status Indicators:**
  - **Red Indicator:** Shows if the recipient has not seen the message.
  - **Green Indicator:** Shows if the recipient has seen the message.
- **Logout Button:** Provides a secure way for users to log out of the application.
- **User Search:** Functionality to search for other users within the chat app.
- **Delete Messages:** Users have the option to delete any type of messages from both the database and the user interface.
- **Option to View Large Image:** Users can view an enlarged version of images.

## Technologies Used

The following technologies and tools are used in the development of this chat application:

- **Kotlin:** Used for developing the frontend of the Android application.
- **Firebase:**
  - **Realtime Database:** To store and sync chat messages and user data in real-time.
  - **Storage:** To store images, audio, and other files.
  - **Authentication:** To handle user sign-up, sign-in, and secure access.
  - **Cloud Messaging:** To manage and send notifications to users.
- **Ktor:** A framework for building asynchronous servers and clients in connected systems. Used for API development.
- **AWS Elastic Beanstalk:** For deploying and managing the backend services.
- **Retrofit:** A type-safe HTTP client for making API requests.
- **OOP (Object-Oriented Programming):** Implementing polymorphism to handle multiple types of messages (text, image, audio).
- **Dependency Injection:** To manage dependencies and improve code scalability and maintainability.
- **Coroutines:** For managing asynchronous tasks, ensuring a smooth user experience.
- **Credential Key Management:** For secure authorization and communication between client and server.

## Best Practices Implemented

- **Clean Architecture:** Organized codebase for easy maintenance and scalability.
- **Dependency Injection:** Leveraging libraries like Dagger/Hilt for managing dependencies.
- **Coroutines and Asynchronous Programming:** For non-blocking UI operations and efficient background processing.
- **Security:** Proper management of credentials and sensitive data.
- **Scalable Codebase:** Ensuring the app can handle increasing numbers of users and messages.

## YouTube Demo

Check out the demo of the app on YouTube: [Watch Demo](https://youtu.be/llaznWZZKGc)

## Screenshots

Here are some screenshots of the app:

![Open App](images/opning_app_%100_.jpg)
![Online/Offline Status](images/onligne_image_recycle%10.jpg)
![Signup](images/SIGNUP%20_3.jpg)
![Send Text Message](images/SEND%20_TEXT_MESSAGE%10_4_.jpg)
![Send Audio Message](images/SEND_AUDIO_MESSAGE_6_%10.jpg)
![Delete Message](images/DELETE%20MESSAGE%10_7_.jpg)
![Notification](images/NOTIFICATION%10_8_.jpg)
![Kotlin Classes Structure](images/kotline_classes_structure%10_chat_9_.png)

