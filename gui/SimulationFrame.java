package gui;

import business_logic.SelectionPolicy;
import business_logic.SimulationManager;

import javax.swing.*;
import java.awt.*;

public class SimulationFrame extends JFrame {
    private JTextField clientsField;
    private JTextField serversField;
    private JTextField timeLimitField;
    private JTextField minArrivalField;
    private JTextField maxArrivalField;
    private JTextField minServiceField;
    private JTextField maxServiceField;
    private JComboBox<SelectionPolicy> policyComboBox;
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private SimulationManager currentManager;

    public SimulationFrame() {
        setTitle("Queues Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel); //setam acest panou ca baza a ferestrei

        JPanel inputPanel = new JPanel(new GridLayout(8, 2, 5, 5));

        inputPanel.add(new JLabel("Number of Clients (N):"));
        clientsField = new JTextField("");
        inputPanel.add(clientsField);

        inputPanel.add(new JLabel("Number of Queues (Q):"));
        serversField = new JTextField("");
        inputPanel.add(serversField);

        inputPanel.add(new JLabel("Simulation Time Limit:"));
        timeLimitField = new JTextField("");
        inputPanel.add(timeLimitField);

        inputPanel.add(new JLabel("Min Arrival Time:"));
        minArrivalField = new JTextField("");
        inputPanel.add(minArrivalField);

        inputPanel.add(new JLabel("Max Arrival Time:"));
        maxArrivalField = new JTextField("");
        inputPanel.add(maxArrivalField);

        inputPanel.add(new JLabel("Min Service Time:"));
        minServiceField = new JTextField("");
        inputPanel.add(minServiceField);

        inputPanel.add(new JLabel("Max Service Time:"));
        maxServiceField = new JTextField("");
        inputPanel.add(maxServiceField);

        inputPanel.add(new JLabel("Selection Policy:"));
        policyComboBox = new JComboBox<>(SelectionPolicy.values());
        inputPanel.add(policyComboBox);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        //adaugam padding interior pt textul din JTextArea
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start Simulation");
        stopButton = new JButton("Stop Simulation");
        stopButton.setEnabled(false);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        //functionalitate butoane
        startButton.addActionListener(e -> startSimulation());
        stopButton.addActionListener(e -> stopSimulation());
    }

    //
    public void updateLog(String text) {
        SwingUtilities.invokeLater(() -> logArea.append(text));
    }

    private void startSimulation() {
        logArea.setText("");
        startButton.setEnabled(false);
        stopButton.setEnabled(true); //activam butonul de stop

        try {
            int clients = Integer.parseInt(clientsField.getText());
            int queues = Integer.parseInt(serversField.getText());
            int timeLimit = Integer.parseInt(timeLimitField.getText());
            int minArrival = Integer.parseInt(minArrivalField.getText());
            int maxArrival = Integer.parseInt(maxArrivalField.getText());
            int minService = Integer.parseInt(minServiceField.getText());
            int maxService = Integer.parseInt(maxServiceField.getText());

            //edge cases
            if (clients <= 0) throw new IllegalArgumentException("Numarul de clienti (N) trebuie sa fie cel putin 1.");
            if (queues <= 0) throw new IllegalArgumentException("Trebuie sa existe cel puțin 1 coada (Q).");
            if (timeLimit <= 0) throw new IllegalArgumentException("Timpul limitei de simulare trebuie sa fie strict pozitiv.");
            if (minArrival < 0 || minService < 0) throw new IllegalArgumentException("Timpii nu pot avea valori negative.");
            if (minArrival > maxArrival) throw new IllegalArgumentException("Min Arrival Time NU poate fi mai mare decat Max Arrival Time.");
            if (minService > maxService) throw new IllegalArgumentException("Min Service Time NU poate fi mai mare decat Max Service Time.");

            SelectionPolicy policy = (SelectionPolicy) policyComboBox.getSelectedItem();

            //atribuim noul manager instantei curente
            currentManager = new SimulationManager(
                    clients, queues, timeLimit, minArrival, maxArrival, minService, maxService, policy, this);

            Thread t = new Thread(() -> {
                currentManager.run();
                //cand thread-ul isi termina treaba (inclusiv daca a fost oprit)
                //restabilim starea butoanelor din thread-ul de UI
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                });
            });
            t.start();
        } catch (NumberFormatException ex) {
            //afisaj personalizat in caz ca utilizatorul introduce litere in loc de numere
            JOptionPane.showMessageDialog(this, "Date de intrare invalide! Asigura-te ca ai completat toate campurile cu numere.", "Eroare Format", JOptionPane.ERROR_MESSAGE);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        } catch (IllegalArgumentException ex) {
            //afisaj pentru mesajele de eroare logica de mai sus
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Eroare Logica", JOptionPane.WARNING_MESSAGE);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    private void stopSimulation() {
        if (currentManager != null) {
            currentManager.stopSimulation();
            stopButton.setEnabled(false); //dezactivam instant sa nu se apese de mai multe ori
        }
    }
}
