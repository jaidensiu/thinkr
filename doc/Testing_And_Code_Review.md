# Example M5: Testing and Code Review

## 1. Change History

| **Change Date**   | **Modified Sections** | **Rationale** |
| ----------------- | --------------------- | ------------- |
| _Nothing to show_ |

---

## 2. Back-end Test Specification: APIs

### 2.1. Locations of Back-end Tests and Instructions to Run Them

#### 2.1.1. Tests

| **Interface**                 | **Describe Group Location, No Mocks**                | **Describe Group Location, With Mocks**            | **Mocked Components**              |
| ----------------------------- | ---------------------------------------------------- | -------------------------------------------------- | ---------------------------------- |
| **POST /auth/login** | None | [`backend/src/tests/mocked/userAuthLogin.test.ts`](#) | MongoDB |
| **GET /chat** |                                               | ...                                                |                      |
| **POST /chat message**                      | ...                                                  | ...                                                | ...                                |
| **DELETE /chat/history**                           | ...                                                  | ...                                                | ...                                |
| **POST /document/upload**                           | ...                                                  | ...                                                | ...                                |
| **DELETE /document/delete**                           | ...                                                  | ...                                                | ...                                |
| **GET /document/retrieve**                           | ...                                                  | ...                                                | ...                                |
| **GET /study/quiz**                           | ...                                                  | ...                                                | ...                                |
| **GET /study/flashcards**                            | ...                                                  | ...                                                | ...                                |
| **GET /study/suggestedMaterials**                           | ...                                                  | ...                                                | ...                                |
| **POST /subscription**                           | ...                                                  | ...                                                | ...                                |
| **DELETE /subscription**                           | ...                                                  | ...                                                | ...                                |
| **GET /subscription**                           | ...                                                  | ...                                                | ...                                |


#### 2.1.2. Commit Hash Where Tests Run

`[Insert Commit SHA here]`

#### 2.1.3. Explanation on How to Run the Tests

1. **Clone the Repository**:

   - Open your terminal and run:
     ```
     git clone https://github.com/jaidensiu/thinkr.git
     ```

2. **Build and Run Backend tests**

   - `cd backend`
   - `npm install`
   - `npm run build`
   - `npm run test` OR `npm run test:coverage` (with code coverage)

### 2.2. GitHub Actions Configuration Location

`~/.github/workflows/backend-ci.yml`
`~/.github/workflows/frontend-ci.yml`

### 2.3. Jest Coverage Report Screenshots With Mocks

_(Placeholder for Jest coverage screenshot with mocks enabled)_

### 2.4. Jest Coverage Report Screenshots Without Mocks

_(Placeholder for Jest coverage screenshot without mocks)_

---

## 3. Back-end Test Specification: Tests of Non-Functional Requirements

### 3.1. Test Locations in Git

| **Non-Functional Requirement**  | **Location in Git**                              |
| ------------------------------- | ------------------------------------------------ |
| **Document Similarity Search Performance**          | [`backend\src\tests\nonfunctional\similaritySearchPerformance.test.ts`](#) |
| **Document Upload and Study Material Generation Performance** | [`backend\src\tests\nonfunctional\documentStudyMaterialPerformance.test.ts`](#) |

### 3.2. Test Verification and Logs

- **Document Similarity Search Performance**

  - **Verification:** This test simulates a single API call with Jest to the endpoint that performs a similarity search between different users' documents and returns the study materials that are most similar to what the caller has been studying. The focus is on the average time it takes for this similarity search to finish, which we identified as no more than 11.3 seconds based on our design specifications. The test logs capture the response time of the call to the endpoint and the performance margin (difference between response time and threshold time). We then analyze these logs to verify that performance standards are met and don't hinder user experience.

  - **Log Output**
    ```
    PERFORMANCE SUMMARY: Suggested Materials Request
           -----------------------------------------------
           ✧ Response Time:      0.35 seconds
           ✧ Threshold:          11.3 seconds
           ✧ Performance Margin: 10.95 seconds
           ✧ Status:             PASSED ✅
           -----------------------------------------------

      at src/tests/nonfunctional/similaritySearchPerformance.test.ts:29:17

    PASS  src/tests/nonfunctional/similaritySearchPerformance.test.ts (5.14 s)
    NFR Suggested Materials Performance Test
    √ should fetch suggested materials in less than 11.3 seconds (377 ms)
    ```

- **Chat Data Security**
  - **Verification:** ...
  - **Log Output**
    ```
    [Placeholder for chat security test logs]
    ```

---

## 4. Front-end Test Specification

### 4.1. Location in Git of Front-end Test Suite:

`frontend/src/androidTest/java/com/studygroupfinder/`

### 4.2. Tests

- **Use Case: Login**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. The user opens â€œAdd Todo Itemsâ€ screen. | Open â€œAdd Todo Itemsâ€ screen. |
    | 2. The app shows an input text field and an â€œAddâ€ button. The add button is disabled. | Check that the text field is present on screen.<br>Check that the button labelled â€œAddâ€ is present on screen.<br>Check that the â€œAddâ€ button is disabled. |
    | 3a. The user inputs an ill-formatted string. | Input â€œ_^_^^OQ#$â€ in the text field. |
    | 3a1. The app displays an error message prompting the user for the expected format. | Check that a dialog is opened with the text: â€œPlease use only alphanumeric charactersâ€. |
    | 3. The user inputs a new item for the list and the add button becomes enabled. | Input â€œbuy milkâ€ in the text field.<br>Check that the button labelled â€œaddâ€ is enabled. |
    | 4. The user presses the â€œAddâ€ button. | Click the button labelled â€œaddâ€. |
    | 5. The screen refreshes and the new item is at the bottom of the todo list. | Check that a text box with the text â€œbuy milkâ€ is present on screen.<br>Input â€œbuy chocolateâ€ in the text field.<br>Click the button labelled â€œaddâ€.<br>Check that two text boxes are present on the screen with â€œbuy milkâ€ on top and â€œbuy chocolateâ€ at the bottom. |
    | 5a. The list exceeds the maximum todo-list size. | Repeat steps 3 to 5 ten times.<br>Check that a dialog is opened with the text: â€œYou have too many items, try completing one firstâ€. |

  - **Test Logs:**
    ```
    [Placeholder for Espresso test execution logs]
    ```

- **Use Case: ...**

  - **Expected Behaviors:**

    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | ...                | ...                 |

  - **Test Logs:**
    ```
    [Placeholder for Espresso test execution logs]
    ```

- **...**

---

## 5. Automated Code Review Results

### 5.1. Commit Hash Where Codacy Ran

`[Insert Commit SHA here]`

### 5.2. Unfixed Issues per Codacy Category

_(Placeholder for screenshots of Codacyâ€™s Category Breakdown table in Overview)_

### 5.3. Unfixed Issues per Codacy Code Pattern

_(Placeholder for screenshots of Codacyâ€™s Issues page)_

### 5.4. Justifications for Unfixed Issues

- **Code Pattern: [Usage of Deprecated Modules](#)**

  1. **Issue**

     - **Location in Git:** [`src/services/chatService.js#L31`](#)
     - **Justification:** ...

  2. ...

- ...