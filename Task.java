package model;

public class Task implements Comparable<Task> {
    private int ID;
    private int arrivalTime;
    private volatile int serviceTime; //pt actualizarea valorii in thread-ul managerului, altfel ar fi schimbat doar in cache

    public Task(int ID, int arrivalTime, int serviceTime) {
        this.ID = ID;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    //metoda pentru a scadea timpul pe masura ce e procesat
    public void decrementServiceTime() {
        if (this.serviceTime > 0) {
            this.serviceTime--;
        }
    }

    @Override
    public int compareTo(Task o) {
        return Integer.compare(this.arrivalTime, o.arrivalTime);
    }

    @Override
    public String toString() {
        return "(" + ID + ", " + arrivalTime + ", " + serviceTime + ")";
    }
}