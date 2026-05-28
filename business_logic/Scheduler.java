package business_logic;

import model.Server;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    private List<Server> servers;
    private int maxNoServers;
    private int maxTasksPerServer;
    private Strategy strategy;

    public Scheduler(int maxNoServers, int maxTasksPerServer) {
        this.maxNoServers = maxNoServers;
        this.maxTasksPerServer = maxTasksPerServer;
        this.servers = new ArrayList<>();

        //multithreading
        for (int i = 0; i < maxNoServers; i++) {
            Server server = new Server();
            servers.add(server);
            Thread t = new Thread(server);
            t.start();
        }
    }

    public void changeStrategy(SelectionPolicy policy) {
        if (policy == SelectionPolicy.SHORTEST_QUEUE) {
            strategy = new ShortestQueueStrategy();
        }
        if (policy == SelectionPolicy.SHORTEST_TIME) {
            strategy = new ShortestTimeStrategy();
        }
    }

    public void dispatchTask(Task t) {
        strategy.addTask(servers, t);
    }

    public List<Server> getServers() {
        return servers;
    }
}
