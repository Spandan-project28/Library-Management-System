# 📚 Smart Library Issue and Return System

![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)
![Swing](https://img.shields.io/badge/GUI-Java_Swing-orange.svg)
![Multithreading](https://img.shields.io/badge/Feature-Multithreading-success.svg)

A modern, thread-safe desktop application built in Java for managing library operations. This project was built to demonstrate advanced Object-Oriented Programming (OOP) concepts, including **Multithreading**, **Custom Exception Handling**, and the **Java Collections Framework**, all wrapped in a custom dark-themed Swing GUI.

## 🌟 Key Features & Concepts Applied

### 1. Multithreading & Synchronization
- **Background Overdue Checker:** A daemon thread runs silently in the background every 30 seconds to scan the entire library for overdue books and automatically updates the GUI's live counter.
- **Concurrent Borrow Simulator:** A dedicated simulation tool that launches multiple student threads simultaneously. All threads attempt to borrow books at the exact same millisecond to prove the system's robust thread safety.
- **Data Synchronization:** Critical methods utilizing the `synchronized` keyword prevent race conditions, ensuring that a single book cannot be borrowed by two students simultaneously.

### 2. Custom Exception Handling
The application gracefully handles business logic errors without crashing by utilizing `try-catch-finally` blocks and throwing custom exceptions:
- `BookNotAvailableException`: Thrown when a requested book doesn't exist or is already checked out.
- `InvalidStudentIdException`: Thrown when a student ID format is invalid or unregistered.
- `LimitExceededException`: Thrown when a student attempts to borrow more than their maximum allowed limit (3 books).

### 3. Java Collections Framework
High-performance data structures manage the system's memory:
- **`ArrayList`**: Maintains the ordered catalogue of books.
- **`HashSet`**: Ensures student IDs remain strictly unique.
- **`PriorityQueue`**: Automatically prioritizes overdue books so the most severely overdue items are processed first.
- **`ArrayDeque`**: Powers the fast, Last-In-First-Out (LIFO) Activity Log.
- **`Hashtable`**: Provides instant, thread-safe ID lookups.

---

## 💻 How It Works (User Guide)

The application features 7 distinct tabs to manage the library seamlessly:

1. **📖 Catalogue**: View the live status of all books in the library. Use the search bar to filter by Title, Author, ID, or Genre.
2. **➕ Add Book**: Librarians can register new books into the system by entering an ID, Title, Author, and selecting a Genre.
3. **🎓 Register Student**: Students must be registered with an alphanumeric ID before they can borrow items.
4. **📤 Borrow**: Assigns a book to a student, changing its status to "BORROWED" and setting a 14-day due date. Fails gracefully with a popup if the student has reached their limit.
5. **📥 Return**: Processes returned books, calculating if they are overdue and resetting their status to "AVAILABLE".
6. **📋 Activity Log**: A live feed of every action taken in the system (registrations, additions, borrows, and returns).
7. **⚡ Simulate**: Click "Run Simulation" to fire off multiple threads at once and watch the system handle heavy concurrent traffic safely.

---

## 🚀 How to Run the Project

You do not need any external libraries to run this project. It runs on pure, standard Java.

### Method 1: Using an IDE (Recommended)
1. Clone the repository: `git clone https://github.com/Spandan-project28/Library-Management-System.git`
2. Open the project folder in **IntelliJ IDEA**, **Eclipse**, or **VS Code**.
3. Navigate to `src/Main.java`.
4. Click **Run**. The application will automatically pre-load dummy data (10 books, 4 students) so you can test it immediately.

### Method 2: Using the Command Line (Windows)
If you have the Java JDK installed and added to your system `PATH`:
1. Double-click the included `run.bat` file in the root directory.
2. It will automatically compile all `.java` files into an `out` folder and launch the graphical interface.

---

*This project was completed for Assignment 2 (Set A): Smart Library Issue and Return System.*
