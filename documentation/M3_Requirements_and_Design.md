# M3 - Requirements and Design

## 1. Change History
<!-- Leave blank for M3 -->

## 2. Project Description

Thinkr is an Android mobile app that is geared towards helping students study through AI-generated multiple-choice quizzes and notes. Students can create and share quizzes/notes with other users, and teachers can also generate quizzes for a group of students.

## 3. Requirements Specification
### **3.1. Use-Case Diagram**


### **3.2. Actors Description**
1. **[WRITE_NAME_HERE]**: ...
2. **[WRITE_NAME_HERE]**: ...


### **3.3. Functional Requirements**
<a name="fr1"></a>

1. **[WRITE_FUNCTIONAL_REQUIREMENT_1_NAME_HERE]** 
    - **Overview**:
        1. [WRITE_FUNCTIONAL_REQUIREMENT_1_1_NAME_HERE]
        2. ...
    
    - **Detailed Flow for Each Independent Scenario**: 
        1. **[WRITE_FUNCTIONAL_REQUIREMENT_1_1_NAME_HERE]**:
            - **Description**: ...
            - **Primary actor(s)**: ... 
            - **Main success scenario**:
                1. ...
                2. ...
            - **Failure scenario(s)**:
                - 1a. ...
                    - 1a1. ...
                    - 1a2. ...
                - 1b. ...
                    - 1b1. ...
                    - 1b2. ...
                - 2a. ...
                    - 2a1. ...
                    - 2a2. ...

        2. ...
    
2. ...


### **3.4. Screen Mockups**


### **3.5. Non-Functional Requirements**
<a name="nfr1"></a>

1. **[WRITE_NAME_HERE]**
    - **Description**: ...
    - **Justification**: ...
2. ...


## 4. Designs Specification
### **4.1. Main Components**
1. **User**
    - **Purpose**: Encapsulates functionalities related to users such as account creation, authentication, and login.
    - **Interfaces**: 
        1. `Result signUp(String username, String password)`
            - **Purpose**: Invoked when user signs up to create a new account.
        2. `Result signIn(String username, String password)`
            - **Purpose**: Invoked when user signs in with existing account.
2. **Document**
    - **Purpose**: Encapsulates functionalities related to documents such as adding, modifying, or deleting.
    - **Interfaces**:
        1. `Result createDocument(Document document)`
            - **Purpose**: Invoked when user adds a new document.
        2. `Result deleteDocument(Document document)`
            - **Purpose**: Invoked when user deletes an existing document.
        3. `Result editDocument(Document document)`
            - **Purpose**: Invoked when user edits an existing document.
        4. `Document viewDocument(int id)`
            - **Purpose**: Invoked when user views an existing document.
3. **Chat**
    - **Purpose**: Encapsulates functionalities related to message streaming with the chatbot trained on the selected document's context such as creating a session and receiving/sending messages.
    - **Interfaces**:
        1. `Result createChat()`
            - **Purpose**: Invoked when user creates a chat session.
        2. `Result sendMessage(String message)`
            - **Purpose**: Invoked when user sends a message/prompt.
        3. `Result receiveMessage(String message)`
            - **Purpose**: Invoked when chatbot streams a message to the user.


### **4.2. Databases**
1. **User**
    - **Purpose**: Used to persist and relate user information such as account information and its related documents.
2. **Document**
    - **Purpose**: Used to persist documents related to a user.
3. **Chat**
    - **Purpose**: Used to persist chat sessions for each user and related documents.


### **4.3. External Modules**
1. **Google Sign In** 
    - **Purpose**: Account creation, login, and user authentication.


### **4.4. Frameworks**
1. **Jetpack Compose**
    - **Purpose**: Used for Android UI development.
    - **Reason**: Jetpack Compose allows for declarative implementation of UI in Kotlin for better maintainability, ease of UI testing with Compose testing, and intuitive use of Kotlin APIs.
2. **Express.js**
    - **Purpose**: Used for backend development in the Node.js runtime environment.
    - **Reason**: Express.js allows for efficient development of REST APIs in the Node.js runtime environment.
3. TODO: maybe call this section the tech stack and include cloud providers (AWS EC2?) and libraries (Retrofit?, Ktor WebSockets?) we will use?


### **4.5. Dependencies Diagram**


### **4.6. Functional Requirements Sequence Diagram**
1. [**[WRITE_NAME_HERE]**](#fr1)\
[SEQUENCE_DIAGRAM_HERE]
2. ...


### **4.7. Non-Functional Requirements Design**
1. [**[WRITE_NAME_HERE]**](#nfr1)
    - **Validation**: ...
2. ...


### **4.8. Main Project Complexity Design**
**[WRITE_NAME_HERE]**
- **Description**: ...
- **Why complex?**: ...
- **Design**:
    - **Input**: ...
    - **Output**: ...
    - **Main computational logic**: ...
    - **Pseudo-code**: ...
        ```
        
        ```


## 5. Contributions
- ...
- ...
- ...
- ...