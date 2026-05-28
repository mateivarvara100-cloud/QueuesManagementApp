package business_logic;

import gui.SimulationFrame;
import model.Server;
import model.Task;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.io.File;

public class SimulationManager implements Runnable {
    private int timeLimit;
    private int maxProcessingTime;
    private int minProcessingTime;
    private int minArrivalTime;
    private int maxArrivalTime;
    private int numberOfServers;
    private int numberOfClients;

    private Scheduler scheduler;
    private List<Task> generatedTasks;
    private SimulationFrame frame;
    private SelectionPolicy selectionPolicy;

    private int totalServiceTime = 0;
    private int peakHour = 0;
    private int maxClientsInSystem = 0;

    //flag pentru oprirea simularii
    private volatile boolean isRunning = true;

    public SimulationManager(int clients, int queues, int timeLimit, int minArrival, int maxArrival, int minService, int maxService, SelectionPolicy policy, SimulationFrame frame) {
        this.numberOfClients = clients;
        this.numberOfServers = queues;
        this.timeLimit = timeLimit;
        this.minArrivalTime = minArrival;
        this.maxArrivalTime = maxArrival;
        this.minProcessingTime = minService;
        this.maxProcessingTime = maxService;
        this.selectionPolicy = policy;
        this.frame = frame;

        scheduler = new Scheduler(this.numberOfServers, 100);
        scheduler.changeStrategy(this.selectionPolicy);
        generateNRandomTasks();
    }

    private void generateNRandomTasks() {
        generatedTasks = new ArrayList<>();
        Random rand = new Random();

        for (int i = 1; i <= numberOfClients; i++) {
            int procTime = rand.nextInt(maxProcessingTime - minProcessingTime + 1) + minProcessingTime;
            int arrTime = rand.nextInt(maxArrivalTime - minArrivalTime + 1) + minArrivalTime;

            generatedTasks.add(new Task(i, arrTime, procTime));
            totalServiceTime += procTime;
        }

        Collections.sort(generatedTasks);
    }

    public void stopSimulation() {
        this.isRunning = false;
    }

    @Override
    public void run() {
        int currentTime = 0;
        try (FileWriter logWriter = new FileWriter(getFilePath())) {
            while (currentTime <= timeLimit && isRunning) {
                dispatchTasksForCurrentTime(currentTime);
                sleepFor(150L);

                updatePeakHour(currentTime);
                logCurrentStatus(currentTime, logWriter);

                if (generatedTasks.isEmpty() && getTotalClientsInQueues() == 0) break;

                currentTime++;
                sleepFor(150L);
            }
            //daca a fost oprit fortat, scriem asta in log
            logForceStop(currentTime, logWriter);
            finalizeSimulation(logWriter);

        } catch (IOException e) {
            System.out.println("Eroare la salvarea fisierului: " + e.getMessage());
        }
    }

    private String getFilePath() {
        String folderName = "Logs";
        File directory = new File(folderName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return folderName + "/log_test_N" + numberOfClients + ".txt";
    }

    private void logForceStop(int currentTime, FileWriter logWriter) throws IOException {
        if (!isRunning) {
            String stopMsg = "\n--- SIMULARE OPRITA FORTAT LA TIMPUL " + currentTime + " ---\n";
            frame.updateLog(stopMsg);

            logWriter.write(stopMsg);
            logWriter.flush();
        }
    }
    //mutam din lista de task-uri in cozi ale server-ului
    //daca faceam cu generatedTasks.remove aveam exceptie direct
    private void dispatchTasksForCurrentTime(int currentTime) {
        Iterator<Task> iterator = generatedTasks.iterator();
        while (iterator.hasNext()) {
            Task t = iterator.next();
            if (t.getArrivalTime() == currentTime) {
                scheduler.dispatchTask(t);
                iterator.remove();
            }
        }
    }

    private void updatePeakHour(int currentTime) {
        int currentClientsInQueues = getTotalClientsInQueues();
        if (currentClientsInQueues > maxClientsInSystem) {
            maxClientsInSystem = currentClientsInQueues;
            peakHour = currentTime;
        }
    }

    private void logCurrentStatus(int time, FileWriter logWriter) throws IOException {
        String status = buildStatusString(time);
        frame.updateLog(status);

        System.out.print(status);
        logWriter.write(status);
        logWriter.flush(); //forteaza scrierea imediata pe disc
    }

    private void finalizeSimulation(FileWriter logWriter) throws IOException {
        String finalMetrics = buildFinalMetrics();
        frame.updateLog(finalMetrics);

        System.out.print(finalMetrics);
        logWriter.write(finalMetrics);
        logWriter.flush();

        for (Server s : scheduler.getServers()) {
            s.stopServer();
        }
    }

    //wrapper pt acest bloc de try-catch
    private void sleepFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    //numarul de tasks in toate cozile
    private int getTotalClientsInQueues() {
        int total = 0;
        for (Server s : scheduler.getServers()) {
            total += s.getTasks().size();
        }
        return total;
    }

    private String buildStatusString(int time) {
        StringBuilder sb = new StringBuilder();
        sb.append("Time ").append(time).append("\nWaiting clients: ");

        //lista de tasks
        for (Task t : generatedTasks) {
            sb.append(t.toString()).append(" ");
        }
        sb.append("\n");

        //pt cozi
        int i = 1;
        for (Server s : scheduler.getServers()) {
            sb.append("Queue ").append(i++).append(": ");
            List<Task> serverTasks = s.getTasks();
            if (serverTasks.isEmpty()) {
                sb.append("closed\n");
            } else {
                for (Task t : serverTasks) sb.append(t.toString()).append(" ");
                sb.append("\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    private String buildFinalMetrics() {
        double avgServiceTime = (double) totalServiceTime / numberOfClients;
        int totalWait = 0;
        for (Server s : scheduler.getServers()) {
            totalWait += s.getTotalWaitTimeForThisServer().get();
        }
        double avgWaitingTime = (double) totalWait / numberOfClients;

        return "Average waiting time: " + String.format("%.2f", avgWaitingTime) + "\n" +
                "Average service time: " + String.format("%.2f", avgServiceTime) + "\n" +
                "Peak hour: " + peakHour + "\n";
    }
}