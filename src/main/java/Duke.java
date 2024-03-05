import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

class Task {
    protected String description;
    protected boolean isDone;

    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    public void markAsDone() {
        isDone = true;
    }

    public void markAsNotDone() {
        isDone = false;
    }

    @Override
    public String toString() {
        return "[T][" + (isDone ? "X" : " ") + "] " + description;
    }
}

class Deadline extends Task {
    protected String by;

    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public String toString() {
        return "[D][" + (isDone ? "X" : " ") + "] " + description + " (by: " + by + ")";
    }
}

class Event extends Task {
    protected String start;
    protected String end;

    public Event(String description, String start, String end) {
        super(description);
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "[E][" + (isDone ? "X" : " ") + "] " + description + " (from: " + start + " to: " + end + ")";
    }
}

class DukeException extends Exception {
    public DukeException(String message) {
        super(message);
    }
}

public class Duke {
    private static final String DATA_FILE_PATH = "./data/duke.txt";

    public static void main(String[] args) {
        String chatBotName = "Rose";
        Scanner scanner = new Scanner(System.in);
        ArrayList<Task> tasks = new ArrayList<>();

        // Ensure data directory exists
        File directory = new File("./data");
        if (!directory.exists()) {
            directory.mkdir(); // Create the directory if it does not exist
        }

        loadTasksFromFile(tasks);

        printLine();
        System.out.println("Hello! I'm " + chatBotName);
        System.out.println("What can I do for you today?");
        printLine();

        while (true) {
            String userInput = scanner.nextLine().trim();

            try {
                if (userInput.equalsIgnoreCase("bye")) {
                    printLine();
                    System.out.println("Bye. Hope to see you again soon!");
                    printLine();
                    break;
                } else if (userInput.equalsIgnoreCase("list")) {
                    printTaskList(tasks);
                } else if (userInput.toLowerCase().startsWith("todo")) {
                    addTask(userInput, tasks, "todo");
                    saveTasksToFile(tasks);
                } else if (userInput.toLowerCase().startsWith("deadline")) {
                    addTask(userInput, tasks, "deadline");
                    saveTasksToFile(tasks);
                } else if (userInput.toLowerCase().startsWith("event")) {
                    addTask(userInput, tasks, "event");
                    saveTasksToFile(tasks);
                } else if (userInput.toLowerCase().startsWith("mark")) {
                    markTask(userInput, tasks, true);
                    saveTasksToFile(tasks);
                } else if (userInput.toLowerCase().startsWith("unmark")) {
                    markTask(userInput, tasks, false);
                    saveTasksToFile(tasks);
                } else {
                    throw new DukeException("I'm sorry, but I don't know what that means :-(");
                }
            } catch (DukeException e) {
                printLine();
                System.out.println(e.getMessage());
                printLine();
            }
        }
        scanner.close();
    }

    private static void addTask(String userInput, ArrayList<Task> tasks, String type) throws DukeException {
        String taskInfo = userInput.substring(type.length()).trim();
        if (taskInfo.isEmpty()) {
            throw new DukeException("The description of a " + type + " cannot be empty.");
        }
        switch (type) {
            case "todo":
                tasks.add(new Task(taskInfo));
                break;
            case "deadline":
                String[] partsDeadline = taskInfo.split("/by", 2);
                if (partsDeadline.length < 2 || partsDeadline[1].trim().isEmpty()) {
                    throw new DukeException("The timing for a deadline cannot be empty.");
                }
                tasks.add(new Deadline(partsDeadline[0].trim(), partsDeadline[1].trim()));
                break;
            case "event":
                String[] partsEvent = taskInfo.split("/at", 2);
                if (partsEvent.length < 2 || partsEvent[1].trim().isEmpty()) {
                    throw new DukeException("The timing for an event cannot be empty.");
                }
                String[] times = partsEvent[1].trim().split("to", 2);
                tasks.add(new Event(partsEvent[0].trim(), times[0].trim(), times[1].trim()));
                break;
            default:
                throw new DukeException("Task type " + type + " is not recognized.");
        }
        printAddedTask(tasks);
    }

    private static void markTask(String userInput, ArrayList<Task> tasks, boolean isDone) throws DukeException {
        try {
            int taskNumber = Integer.parseInt(userInput.split(" ")[1]) - 1;
            if (taskNumber < 0 || taskNumber >= tasks.size()) {
                throw new DukeException("Task number is out of bounds.");
            }
            Task task = tasks.get(taskNumber);
            if (isDone) {
                task.markAsDone();
            } else {
                task.markAsNotDone();
            }
            printLine();
            System.out.println("OK, I've marked this task as " + (isDone ? "done" : "not done yet") + ":");
            System.out.println("  " + task);
            printLine();
        } catch (NumberFormatException e) {
            throw new DukeException("Invalid task number format.");
        }
    }

    private static void printTaskList(ArrayList<Task> tasks) {
        printLine();
        if (tasks.isEmpty()) {
            System.out.println("Your task list is empty.");
        } else {
            System.out.println("Here are the tasks in your list:");
            for (int i = 0; i < tasks.size(); i++) {
                System.out.println((i + 1) + ". " + tasks.get(i));
            }
        }
        printLine();
    }

    private static void printAddedTask(ArrayList<Task> tasks) {
        printLine();
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + tasks.get(tasks.size() - 1));
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
        printLine();
    }

    private static void saveTasksToFile(ArrayList<Task> tasks) {
        try (PrintWriter writer = new PrintWriter(DATA_FILE_PATH)) {
            for (Task task : tasks) {
                writer.println(taskToFileString(task));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + DATA_FILE_PATH);
        }
    }

    private static void loadTasksFromFile(ArrayList<Task> tasks) {
        File file = new File(DATA_FILE_PATH);
        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNext()) {
                String taskData = fileScanner.nextLine();
                String[] taskDetails = taskData.split(" \\| ");
                Task task = null;
                switch (taskDetails[0]) {
                    case "T":
                        task = new Task(taskDetails[2]);
                        break;
                    case "D":
                        task = new Deadline(taskDetails[2], taskDetails[3]);
                        break;
                    case "E":
                        task = new Event(taskDetails[2], taskDetails[3], taskDetails[4]);
                        break;
                }
                if (task != null) {
                    if (taskDetails[1].equals("1")) {
                        task.markAsDone();
                    }
                    tasks.add(task);
                }
            }
        } catch (FileNotFoundException e) {
            // It's okay if the file doesn't exist, we'll start with an empty task list.
        }
    }

    private static String taskToFileString(Task task) {
        String type = task instanceof Deadline ? "D" : task instanceof Event ? "E" : "T";
        String isDone = task.isDone ? "1" : "0";
        String description = task.description;
        String time = "";
        if (task instanceof Deadline) {
            time = " | " + ((Deadline) task).by;
        } else if (task instanceof Event) {
            time = " | " + ((Event) task).start + " | " + ((Event) task).end;
        }
        return type + " | " + isDone + " | " + description + time;
    }

    private static void printLine() {
        System.out.println("____________________________________________________________");
    }
}
