package ludiiInterface.games.othello;

import ludiiInterface.general.SystemConversion;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SystemConversionOthello extends SystemConversion {

    public SystemConversionOthello(){
        Map<Integer, Integer> map = Stream.of(new Integer[][] {
                { 0,56},{ 1,57},{ 2,58},{ 3,59},{ 4,60},{ 5,61},{ 6,62},{ 7,63},
                { 8,48},{ 9,49},{10,50},{11,51},{12,52},{13,53},{14,54},{15,55},
                {16,40},{17,41},{18,42},{19,43},{20,44},{21,45},{22,46},{23,47},
                {24,32},{25,33},{26,34},{27,35},{28,36},{29,37},{30,38},{31,39},
                {32,24},{33,25},{34,26},{35,27},{36,28},{37,29},{38,30},{39,31},
                {40,16},{41,17},{42,18},{43,19},{44,20},{45,21},{46,22},{47,23},
                {48, 8},{49, 9},{50,10},{51,11},{52,12},{53,13},{54,14},{55,15},
                {56, 0},{57, 1},{58, 2},{59, 3},{60,4 },{61, 5},{62, 6},{63, 7},
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        indicesMap.putAll(map);

        Map<Integer,Integer> playerMapping = Stream.of(new Integer[][] {
                {1,0}, {2,1},
        }).collect(Collectors.toMap(data ->data[0], data -> data[1]));
        playerMap.putAll(playerMapping);
    }

    @Override
    public boolean isValidLudiiIndex(int index){
        return (index>=0 && index <=63);
    }

    @Override
    public boolean isValidGBGIndex(int index){
        return (index >= 0 && index <= 63);
    }

}
