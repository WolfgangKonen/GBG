package ludiiInterface.general;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ludiiInterface.games.othello.SystemConversionOthello;
import ludiiInterface.games.yavalath.SystemConversionYavalath;

/**
 * This class is used to convert board positions and player numbering between Ludii and GBG and needs to be implemented,
 * if you want to add another game as a possibility for Ludii-GBG playing.
 * Check {@link SystemConversionYavalath} or {@link SystemConversionOthello} for an example.
 */
abstract public class SystemConversion {

    /**
     * Maps the Ludii index of a board position to the respective GBG position and vice versa.
     * Ludii indices can be enabled to be shown in the Ludii player via the "View" menu -> "Show Indices" (or CTRL-I).
     */
    protected BiMap<Integer,Integer> indicesMap = HashBiMap.create();
    /**
     * Maps the Ludii player numbering to the respective GBG player number.
     * Usually GBG is Ludii -1, as GBG numbering starts from 0 and Ludii from 1.
     */
    protected BiMap<Integer,Integer> playerMap = HashBiMap.create();

    public abstract boolean isValidGBGIndex(int index);

    public abstract boolean isValidLudiiIndex(int index);

    /**
     * @param index The Ludii board position index
     * @return The GBG board position index
     */
    public int getGBGIndexFromLudii(int index){
        return indicesMap.get(index);
    }

    /**
     * @param index The GBG board position index
     * @return The Ludii board position index
     */
    public int getLudiiIndexFromGBG(int index){
        return indicesMap.inverse().get(index);
    }

    /**
     * @param player The Ludii player number
     * @return The GBG player number
     */
    public int getGBGPlayerFromLudii(int player){
        return playerMap.get(player);
    }

    /**
     * @param player The GBG player number
     * @return The Ludii player number
     */
    public int getLudiiPlayerFromGBG(int player){
        return playerMap.inverse().get(player);
    }

}
