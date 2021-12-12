package ludiiInterface.games.Hex;

import ludiiInterface.general.SystemConversion;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SystemConversionHex extends SystemConversion {

    Map<Integer,Integer> map3x3 = Stream.of(new Integer[][] {
            { 0, 2}, { 1, 1}, { 2, 5},
            { 3, 0}, { 4, 4}, { 5, 8},
            { 6, 3}, { 7, 7}, { 8, 6},
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer,Integer> map4x4 = Stream.of(new Integer[][] {
            { 0, 3}, { 1, 2}, { 2, 7}, { 3, 1},
            { 4, 6}, { 5,11}, { 6, 0}, { 7, 5},
            { 8,10}, { 9,15}, {10, 4}, {11, 9},
            {12,14}, {13, 8}, {14,13}, {15,13}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer,Integer> map5x5 = Stream.of(new Integer[][] {
            { 0, 4} ,{ 1, 3} ,{ 2, 9} ,{ 3, 2} ,{ 4, 8} ,
            { 5,14} ,{ 6, 1} ,{ 7, 7} ,{ 8,13} ,{ 9,19} ,
            {10, 0} ,{11, 6} ,{12,12} ,{13,18} ,{14,24} ,
            {15, 5} ,{16,11} ,{17,17} ,{18,23} ,{19,10} ,
            {20,16} ,{21,22} ,{22,15} ,{23,21} ,{24,20}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> map6x6 = Stream.of(new Integer[][] {
            { 0, 5},{ 1, 4},{ 2,11},{ 3, 3},{ 4,10},{ 5,17},
            { 6, 2},{ 7, 9},{ 8,16},{ 9,23},{10, 1},{11, 8},
            {12,15},{13,22},{14,29},{15, 0},{16, 7},{17,14},
            {18,21},{19,28},{20,35},{21, 6},{22,13},{23,20},
            {24,27},{25,34},{26,12},{27,19},{28,26},{29,33},
            {30,18},{31,25},{32,32},{33,24},{34,31},{35,30},
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer,Integer> map7x7 = Stream.of(new Integer[][] {
            { 0, 6},{ 1, 5},{ 2,13},{ 3, 4},{ 4,12},{ 5,20},{ 6, 3},
            { 7,11},{ 8,19},{ 9,27},{10, 2},{11,10},{12,18},{13,26},
            {14,34},{15, 1},{16, 9},{17,17},{18,25},{19,33},{20,41},
            {21, 0},{22, 8},{23,16},{24,24},{25,32},{26,40},{27,48},
            {28, 7},{29,15},{30,23},{31,31},{32,39},{33,47},{34,14},
            {35,22},{36,30},{37,39},{38,46},{39,21},{40,29},{41,37},
            {42,45},{43,28},{44,36},{45,44},{46,35},{47,43},{48,42},
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer,Integer> map8x8 = Stream.of(new Integer[][] {
            { 0, 7},{ 1, 6},{ 2,15},{ 3, 5},{ 4,14},{ 5,23},{ 6, 4},{ 7,13},
            { 8,22},{ 9,31},{10, 3},{11,12},{12,21},{13,30},{14,39},{15, 2},
            {16,11},{17,20},{18,29},{19,38},{20,47},{21, 1},{22,10},{23,19},
            {24,28},{25,37},{26,46},{27,55},{28, 0},{29, 9},{30,18},{31,27},
            {32,36},{33,45},{34,54},{35,63},{36, 8},{37,17},{38,26},{39,35},
            {40,44},{41,53},{42,62},{43,16},{44,25},{45,34},{46,43},{47,52},
            {48,61},{49,24},{50,33},{51,42},{52,51},{53,60},{54,32},{55,41},
            {56,50},{57,59},{58,40},{59,49},{60,58},{61,48},{62,57},{63,56},
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));


    public SystemConversionHex(int size){
        switch(size){
            case 3 -> indicesMap.putAll(map3x3);
            case 4 -> indicesMap.putAll(map4x4);
            case 5 -> indicesMap.putAll(map5x5);
            case 6 -> indicesMap.putAll(map6x6);
            case 7 -> indicesMap.putAll(map7x7);
            case 8 -> indicesMap.putAll(map8x8);
            default -> indicesMap.putAll(map6x6);
        }

        Map<Integer,Integer> playerMapping = Stream.of(new Integer[][] {
                {1,0}, {2,1},
        }).collect(Collectors.toMap(data ->data[0], data -> data[1]));
        playerMap.putAll(playerMapping);
    }

    @Override
    public boolean isValidGBGIndex(int index) {
        return false;
    }

    @Override
    public boolean isValidLudiiIndex(int index) {
        return false;
    }
}
