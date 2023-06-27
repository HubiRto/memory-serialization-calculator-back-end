package pl.pomoku.memoryserializationcalculatorbackend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pomoku.memoryserializationcalculatorbackend.model.Data;
import pl.pomoku.memoryserializationcalculatorbackend.model.RoundRobinData;

import java.util.*;

@RestController
@RequestMapping("/api")
public class CalculatorController {

    @PostMapping("/round-robin")
    public Map<String, Object> calculateRoundRobin(@RequestBody RoundRobinData[] data) {
        List<Data> list = new ArrayList<>();
        for(RoundRobinData rrd : data){
            list.add(new Data(rrd.getIndex(), rrd.getArrivalTime(), rrd.getExecutionTime()));
        }

        int[][] result =  rrSchedulingAlgorithm(list, data[0].getQuantum());
        double[][] timeResult = rrTime(result);

        Map<String, Object> response = new HashMap<>();
        response.put("ints", result);
        response.put("doubles", timeResult);
        return response;
    }

    @PostMapping("/fifo")
    public Map<String, Object> calculateFIFO(@RequestBody Data[] data) {
        int[][] result = fifoSchedulingAlgorithm(data);
        double[][] timeResult = fifoTime(result);

        Map<String, Object> response = new HashMap<>();
        response.put("ints", result);
        response.put("doubles", timeResult);

        return response;
    }

    @PostMapping("/sjf")
    public Map<String, Object> calculateSJF(@RequestBody Data[] data) {
        int[][] result = sjfSchedulingAlgorithm(data);
        double[][] timeResult = fifoTime(result);

        Map<String, Object> response = new HashMap<>();
        response.put("ints", result);
        response.put("doubles", timeResult);

        return response;
    }

    @PostMapping("/srtf")
    public Map<String, Object> calculateSRTF(@RequestBody Data[] data) {
        int[][] result = srtfSchedulingAlgorithm(data);
        double[][] timeResult = fifoTime(result);

        Map<String, Object> response = new HashMap<>();
        response.put("ints", result);
        response.put("doubles", timeResult);

        return response;
    }


    private static double[][] fifoTime(int[][] memory){
        if(memory == null) return null;
        double[][] time = new double[memory.length + 1][3];

        int waitCounter = 0;
        int burstCounter = 0;
        for(int y = 0; y < memory.length; y++){
            for(int x = 0; x < memory[0].length; x++){
                if(memory[y][x] == 1){
                    waitCounter++;
                }else if(memory[y][x] == 2){
                    burstCounter++;
                }
            }
            time[y][0] = waitCounter + burstCounter;
            time[y][1] = waitCounter;
            time[y][2] = waitCounter;
            waitCounter = 0;
            burstCounter = 0;
        }

        for(int x = 0; x < time[0].length; x++){
            int counter = 0;
            for(int y = 0; y < time.length - 1; y++){
                counter += time[y][x];
            }
            time[time.length - 1][x] = ((double) counter /(time.length - 1));
        }
        return time;
    }
    private static double[][] rrTime(int[][] memory){
        if(memory == null) return null;
        double[][] time = new double[memory.length + 1][3];

        int waitCounter = 0;
        int burstCounter = 0;
        for(int y = 0; y < memory.length; y++){
            for(int x = 0; x < memory[0].length; x++){
                if(memory[y][x] == 1){
                    waitCounter++;
                }else if(memory[y][x] == 2){
                    burstCounter++;
                }
            }
            time[y][0] = waitCounter + burstCounter;
            time[y][1] = waitCounter;
            waitCounter = 0;
            burstCounter = 0;
        }

        for(int y = 0; y < memory.length; y++) {
            int countOne = 0;
            for (int x = 0; x < memory[0].length; x++) {
                if(memory[y][x] == 2) break;
                if(memory[y][x] == 1) countOne++;
            }
            time[y][2] = countOne;
        }

        for(int x = 0; x < time[0].length; x++){
            int counter = 0;
            for(int y = 0; y < time.length - 1; y++){
                counter += time[y][x];
            }
            time[time.length - 1][x] = ((double) counter /(time.length - 1));
        }
        return time;
    }

    private static int[][] rrSchedulingAlgorithm(List<Data> data, int kwant) {
        List<Data> copy = new ArrayList<>(data);
        int operations = data.stream().mapToInt(Data::getExecutionTime).sum();
        int[][] memory = new int[data.size()][operations];
        Queue<Data> dataQueue = new ArrayDeque<>();

        data.sort(Comparator.comparingInt(Data::getArrivalTime).thenComparingInt(Data::getExecutionTime));
        int zeroIndex = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getArrivalTime() == 0) {
                zeroIndex = i;
                break;
            }
        }
        if (zeroIndex != -1) {
            Data zeroData = data.remove(zeroIndex);
            data.add(0, zeroData);
        }


        dataQueue.add(data.get(0));
        data.remove(0);

        int process = 0;

        do {
            Data currentData = dataQueue.poll();
            if (currentData == null) return memory;

            int executionTime = currentData.getExecutionTime();
            int iterations = Math.min(executionTime, kwant);

            for (int i = 0; i < iterations; i++) {
                memory[currentData.getIndex()][process] = 2;
                data.sort(Comparator.comparingInt(Data::getArrivalTime).thenComparingInt(Data::getExecutionTime));
                Iterator<Data> iterator = data.iterator();
                while (iterator.hasNext()) {
                    Data d = iterator.next();
                    if (d.getArrivalTime() == process) {
                        dataQueue.add(d);
                        iterator.remove();
                    }
                }
                currentData.setExecutionTime(currentData.getExecutionTime() - 1);
                process++;
            }

            if (executionTime > kwant || iterations == executionTime) {
                dataQueue.add(currentData);
            }

        } while (process != operations);

        for (int i = 0; i < copy.size(); i++) {
            int arrivalTime = copy.get(i).getArrivalTime();
            int lastIndex = -1;
            for (int j = operations - 1; j >= 0; j--) {
                if (memory[i][j] == 2) {
                    lastIndex = j;
                    break;
                }
            }
            for (int j = arrivalTime; j <= lastIndex; j++) {
                if (memory[i][j] == 0) {
                    memory[i][j] = 1;
                }
            }
        }

        return memory;
    }

    private static int[][] fifoSchedulingAlgorithm(Data[] data){
        int[][] memory = new int[data.length][countOperation(data)];
        int currentStage = 0;

        Arrays.sort(data, Comparator.comparingInt(Data::getArrivalTime)
                .thenComparingInt(Data::getExecutionTime));

        for(Data currentData : data){
            for(int i = currentStage; i < currentStage + currentData.getExecutionTime(); i++){
                memory[currentData.getIndex()][i] = 2;
            }
            currentStage += currentData.getExecutionTime();

            for(int i = currentData.getArrivalTime(); i < currentStage; i++){
                if(memory[currentData.getIndex()][i] != 2) memory[currentData.getIndex()][i] = 1;
            }
        }
        return memory;
    }
    private static int countOperation(Data[] dataArr){
        int counter = 0;
        for(Data data : dataArr){
            counter += data.getExecutionTime();
        }
        return counter;
    }
    private static void printMemory(int[][] memory) {
        for (int i = 0; i < memory.length; i++) {
            System.out.print("P" + i + " | ");
            for (int j = 0; j < memory[i].length; j++) {
                if(j == memory[i].length - 3) System.out.print("| ");
                System.out.print(memory[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static int[][] srtfSchedulingAlgorithm(Data[] data) {
        int operations = countOperation(data);
        int[][] memory = new int[data.length][operations + 3];
        sortWithZero(data);
        for (int o = 0; o < operations; o++) {
            memory[data[0].getIndex()][o] = 2;
            for (int i = 1; i < data.length; i++) {
                Data d = data[i];
                if (d.getArrivalTime() <= o) {
                    memory[d.getIndex()][o] = 1;
                }
            }
            if (!(data[0].getExecutionTime() - 1 <= 0)) {
                data[0].setExecutionTime(data[0].getExecutionTime() - 1);
            } else {
                Data[] newData = new Data[data.length - 1];
                System.arraycopy(data, 1, newData, 0, data.length - 1);
                data = newData;
            }
            Arrays.sort(data, getCustomComparator(o + 1));
        }

        for (int y = 0; y < memory.length; y++) {
            int occurrencesOne = 0;
            int occurrencesTwo = 0;
            int[] memoryRow = memory[y];

            for (int x = 0, length = memoryRow.length - 3; x < length; x++) {
                if (memoryRow[x] == 1) {
                    occurrencesOne++;
                } else if (memoryRow[x] == 2) {
                    occurrencesTwo++;
                }
            }
            memoryRow[memoryRow.length - 3] = occurrencesOne + occurrencesTwo;
            memoryRow[memoryRow.length - 2] = occurrencesOne;
            memoryRow[memoryRow.length - 1] = occurrencesOne;
        }
        return memory;
    }

    public static Comparator<Data> getCustomComparator(int a) {
        return (d1, d2) -> {
            if (d1.getArrivalTime() > a && d2.getArrivalTime() <= a) {
                return 1; // d2 na początku
            } else if (d1.getArrivalTime() <= a && d2.getArrivalTime() > a) {
                return -1; // d1 na początku
            } else {
                // Sortowanie wg arrivalTime
                return Integer.compare(d1.getExecutionTime(), d2.getExecutionTime());
            }
        };
    }

    private static void sortWithZero(Data[] data) {
        Arrays.sort(data, Comparator.comparingInt(Data::getExecutionTime).thenComparingInt(Data::getArrivalTime));
        Data zeroData = null;
        int index = -1;
        for (int i = 0; i < data.length; i++) {
            if (data[i].getArrivalTime() == 0) {
                zeroData = data[i];
                index = i;
                break;
            }
        }
        if (index != -1) {
            for (int i = index; i > 0; i--) {
                data[i] = data[i - 1];
            }
            data[0] = zeroData;
        }
    }

    private static int[][] sjfSchedulingAlgorithm(Data[] data) {
        if (data == null) return null;
        int[][] memory = new int[data.length][countOperation(data)];
        int currentStage = 0;

        Arrays.sort(data, Comparator.comparingInt(Data::getExecutionTime).thenComparingInt(Data::getArrivalTime));

        int index = 0;
        while (index < data.length && data[index].getArrivalTime() != 0) {
            index++;
        }

        if (index > 0) {
            Data firstData = data[index];
            System.arraycopy(data, 0, data, 1, index);
            data[0] = firstData;
        }

        for (Data currentData : data) {
            int executionTime = currentData.getExecutionTime();
            int arrivalTime = currentData.getArrivalTime();
            int dataIndex = currentData.getIndex();

            for (int i = currentStage; i < currentStage + executionTime; i++) {
                memory[dataIndex][i] = 2;
            }
            currentStage += executionTime;

            for (int i = arrivalTime; i < currentStage; i++) {
                if (memory[dataIndex][i] != 2) {
                    memory[dataIndex][i] = 1;
                }
            }
        }
        return memory;
    }
}