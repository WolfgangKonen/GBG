package ludiiInterface.games.yavalath;

import games.Yavalath.ConfigYavalath;
import ludiiInterface.general.SystemConversion;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SystemConversionYavalath extends SystemConversion {

    public SystemConversionYavalath(){
        Map<Integer, Integer> map = Stream.of(new Integer[][] {
                {0,4},{1,3},{2,2},{3,1},{4,0},
                {5,14},{6,13},{7,12},{8,11},{9,10},{10,9},
                {11,24},{12,23},{13,22},{14,21},{15,20},{16,19},{17,18},
                {18,34},{19,33},{20,32},{21,31},{22,30},{23,29},{24,28},{25,27},
                {26,44},{27,43},{28,42},{29,41},{30,40},{31,39},{32,38},{33,37},{34,36},
                {35,53},{36,52},{37,51},{38,50},{39,49},{40,48},{41,47},{42,46},
                {43,62},{44,61},{45,60},{46,59},{47,58},{48,57},{49,56},
                {50,71},{51,70},{52,69},{53,68},{54,67},{55,66},
                {56,80},{57,79},{58,78},{59,77},{60,76},
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        indicesMap.putAll(map);

        Map<Integer,Integer> playerMapping = Stream.of(new Integer[][] {
                {1,0}, {2,1},{3,2}
        }).collect(Collectors.toMap(data ->data[0], data -> data[1]));
        playerMap.putAll(playerMapping);

    }

    @Override
    public boolean isValidGBGIndex(int index){
        int y = index % ConfigYavalath.getMaxRowLength();
        int x = (index-y)/ConfigYavalath.getMaxRowLength();
        return (index >= 0 && index < 80 && (Math.abs(y-x)<ConfigYavalath.getBoardSize()));
    }

    @Override
    public boolean isValidLudiiIndex(int index){

        return (index >= 0 && index <= 60);
    }
}
