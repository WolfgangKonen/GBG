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
                {5,10},{6,9},{7,8},{8,7},{9,6},{10,5},
                {11,17},{12,16},{13,15},{14,14},{15,13},{16,12},{17,11},
                {18,25},{19,24},{20,23},{21,22},{22,21},{23,20},{24,19},{25,18},
                {26,34},{27,33},{28,32},{29,31},{30,30},{31,29},{32,28},{33,27},{34,26},
                {35,42},{36,41},{37,40},{38,39},{39,38},{40,37},{41,36},{42,35},
                {43,49},{44,48},{45,47},{46,46},{47,45},{48,44},{49,43},
                {50,55},{51,54},{52,53},{53,52},{54,51},{55,50},
                {56,60},{57,59},{58,58},{59,57},{60,56},
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
        return (index >= 0 && index <= 60 && (Math.abs(y-x)<ConfigYavalath.getBoardSize()));
    }

    @Override
    public boolean isValidLudiiIndex(int index){

        return (index >= 0 && index <= 60);
    }
}
