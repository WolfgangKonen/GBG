package ludiiInterface.games.Nim;

import ludiiInterface.general.SystemConversion;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SystemConversionNim extends SystemConversion {

    private int number_heaps;

    Map<Integer, Integer> map3 = Stream.of(new Integer[][] {
            {0,0}, {1,3}, {2,6}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> map5 = Stream.of(new Integer[][] {
            {0,0}, {1,5}, {2,10}, {3,15}, {4,20}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> map7 = Stream.of(new Integer[][] {
            {0,0}, {1,7}, {2,14}, {3,21}, {4,28}, {5,35}, {6,42}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> map9 = Stream.of(new Integer[][] {
            {0,0}, {1,9}, {2,18}, {3,27}, {4,36}, {5,45}, {6,54}, {7,63}, {8,72}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> map11 = Stream.of(new Integer[][] {
            {0,0}, {1,11}, {2,22}, {3,33}, {4,44}, {5,55},
            {6,66}, {7,77}, {8,88}, {9,99}, {10,110}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> map13 = Stream.of(new Integer[][] {
            {0,0}, {1,13}, {2,26}, {3,39}, {4,52}, {5,65}, {6,78},
            {7,91}, {8,104}, {9,117}, {10,130}, {11,143}, {12,156}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> map15 = Stream.of(new Integer[][] {
            {0,0}, {1,15}, {2,30}, {3,45}, {4,60}, {5,75}, {6,90},
            {7,105}, {8,120}, {9,135}, {10,150}, {11,165}, {12,180}, {13,195}, {14,210}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> map17 = Stream.of(new Integer[][] {
            {0,0}, {1,17}, {2,34}, {3,51}, {4,68}, {5,85}, {6,102}, {7,119}, {8,136}, {9,153},
            {10,170}, {11,187}, {12,204}, {13,221}, {14,238}, {15,255}, {16,272}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> map19 = Stream.of(new Integer[][] {
            {0,0}, {1,19}, {2,38}, {3,57}, {4,76}, {5,95}, {6,114}, {7,133}, {8,152}, {9,171},
            {10,190}, {11,209}, {12,228}, {13,247}, {14,266}, {15,285}, {16,304}, {17,323}, {18,342}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public SystemConversionNim(int number_heaps){
        this.number_heaps = number_heaps;

        switch(number_heaps){
            case 3 -> indicesMap.putAll(map3);
            case 5 -> indicesMap.putAll(map5);
            case 7 -> indicesMap.putAll(map7);
            case 9 -> indicesMap.putAll(map9);
            case 11 -> indicesMap.putAll(map11);
            case 13 -> indicesMap.putAll(map13);
            case 15 -> indicesMap.putAll(map15);
            case 17 -> indicesMap.putAll(map17);
            case 19 -> indicesMap.putAll(map19);
        }

        Map<Integer,Integer> playerMapping = Stream.of(new Integer[][] {
                {1,0}, {2,1},
        }).collect(Collectors.toMap(data ->data[0], data -> data[1]));
        playerMap.putAll(playerMapping);
    }

    @Override
    public boolean isValidGBGIndex(int index) {
        return (index >= 0 && index <= number_heaps);
    }

    @Override
    public boolean isValidLudiiIndex(int index) {
        return (index >= 0 && index <= number_heaps);
    }
}
