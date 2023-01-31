package ludiiInterface.games.CFour;

import ludiiInterface.general.SystemConversion;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SystemConversionC4 extends SystemConversion {

    public SystemConversionC4() {
        Map<Integer, Integer> map = Stream.of(new Integer[][] {
                {-1,-1},{ 0, 0},{ 1, 1},{ 2,2},{ 3,3},{ 4,4},{ 5,5},{ 6,6}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        indicesMap.putAll(map);

        Map<Integer,Integer> playerMapping = Stream.of(new Integer[][] {
                {1,0}, {2,1}
        }).collect(Collectors.toMap(data ->data[0], data -> data[1]));
        playerMap.putAll(playerMapping);
    }

    @Override
    public boolean isValidGBGIndex(int index) {
        return (index>=0 && index <=6);
    }

    @Override
    public boolean isValidLudiiIndex(int index) {
        return (index>=0 && index <=6);
    }
}
