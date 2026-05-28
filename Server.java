package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private BlockingQueue<Task> tasks;
    private AtomicInteger waitingPeriod;
    private AtomicInteger totalWaitTimeForThisServer;
    private volatile boolean isRunning;
    private volatile Task currentTask;

    public Server() {
        this.tasks = new LinkedBlockingQueue<>();
        this.waitingPeriod = new AtomicInteger(0);
        this.totalWaitTimeForThisServer = new AtomicInteger(0);
        this.isRunning = true;
        this.currentTask = null; //task-ul care se proceseaza in acest moment
    }

    //executata de SimulationManager
    public void addTask(Task newTask) {
        totalWaitTimeForThisServer.addAndGet(waitingPeriod.get());
        tasks.add(newTask);
        waitingPeriod.addAndGet(newTask.getServiceTime());
    }

    public void stopServer() {
        this.isRunning = false;
        //am adaugat golirea cozii si eliminarea task-ului curent pentru a opri instantaneu thread-urile ascunse
        this.tasks.clear();
        this.currentTask = null;
    }

    public void run() {
        while (isRunning || !tasks.isEmpty() || currentTask != null) {
            try {
                //daca nu procesam pe nimeni, asteptam sa apara un client in coada
                if (currentTask == null) {
                    currentTask = tasks.poll(10, TimeUnit.MILLISECONDS);
                    if (currentTask == null) {
                        continue; //coada e goala, verificam din nou conditia buclei
                    }
                }

                //cat timp serverul doarme 300ms, Managerul face poza (log-ul)
                Thread.sleep(300L);

                //la finalul secundei de simulare, scadem timpul de service
                currentTask.decrementServiceTime();
                waitingPeriod.updateAndGet(w -> Math.max(0, w - 1));

                //daca clientul a terminat, eliberam ghiseul
                if (currentTask.getServiceTime() <= 0) {
                    currentTask = null;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public List<Task> getTasks() {
        List<Task> tasksList = new ArrayList<>();
        //afisam clientul de la ghiseu primul, apoi restul cozii
        if (currentTask != null) {
            tasksList.add(currentTask);
        }
        tasksList.addAll(tasks);
        return tasksList;
    }

    public AtomicInteger getWaitingPeriod() {
        return waitingPeriod;
    }

    public AtomicInteger getTotalWaitTimeForThisServer() {
        return totalWaitTimeForThisServer;
    }
}