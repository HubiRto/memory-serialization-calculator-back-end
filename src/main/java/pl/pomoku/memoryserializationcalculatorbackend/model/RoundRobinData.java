package pl.pomoku.memoryserializationcalculatorbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class RoundRobinData {
    private int index;
    private int arrivalTime;
    private int executionTime;
    private int quantum;
    private String order;
}
