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

## App Screenshots

### 1. Open App
<img src="https://github.com/user-attachments/assets/c8dc8834-caa5-4fdb-ba6e-5edbadf8de58" width="150">

### 2. Online/Offline Status
<img src="https://github.com/user-attachments/assets/567aa194-e9ea-4965-a14a-ef0be2c90fac" width="150">

### 3. Signup
<img src="https://github.com/user-attachments/assets/e08b4999-23ab-4540-a44b-225aaa2abb0f" width="150">

### 4. Send Text Message
<img src="https://github.com/user-attachments/assets/b164a263-a29a-4d72-aa59-54aec9db4a1a" width="150">

### 5. Send Image Message
<img src="https://github.com/user-attachments/assets/03ac9290-90e3-4a0b-848a-26a7131053a6" width="150">

### 6. Send Audio Message
<img src="https://github.com/user-attachments/assets/03771ef5-87d3-49fb-8aa9-fe9cffe0a4ed" width="150">

### 7. Delete Message
<img src="https://github.com/user-attachments/assets/2c776743-7e06-49ca-81c6-e7ba65288e83" width="150">

### 8. Notification
<img src="https://github.com/user-attachments/assets/b62c124e-0188-4a74-b00f-8e05a99b6ea8" width="150">

### 9. Kotlin Classes Structure
<img src="https://github.com/user-attachments/assets/fb8b7a71-2050-4967-ae05-8dbb239939db" width="150">



