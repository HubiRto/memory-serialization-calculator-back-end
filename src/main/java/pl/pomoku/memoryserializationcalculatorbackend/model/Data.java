package pl.pomoku.memoryserializationcalculatorbackend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@lombok.Data
@AllArgsConstructor
@ToString
public class Data {
    private int index;
    private int arrivalTime;
    private int executionTime;
}
