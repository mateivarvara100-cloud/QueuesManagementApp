package business_logic;

import model.Server;
import model.Task;

import java.util.List;

public class ShortestTimeStrategy implements Strategy {
    @Override
    public void addTask(List<Server> servers, Task t) {
        Server bestServer = servers.get(0);
        for (Server s : servers) {
            if (s.getWaitingPeriod().get() < bestServer.getWaitingPeriod().get()) {
                bestServer = s;
            }
        }
        bestServer.addTask(t);
    }
}