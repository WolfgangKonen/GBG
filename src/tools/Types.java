package tools;


import java.awt.Color;

import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.util.Vector;

/**
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Types {
	/*
    public static final int PHYSICS_NONE = -1;
    public static final int PHYSICS_GRID = 0;
    public static final int PHYSICS_CONT = 1;
    public static final int PHYSICS_NON_FRICTION = 2;
    public static final int PHYSICS_GRAVITY = 3;

    public static final int VGDL_GAME_DEF = 0;
    public static final int VGDL_SPRITE_SET = 1;
    public static final int VGDL_INTERACTION_SET = 2;
    public static final int VGDL_LEVEL_MAPPING = 3;
    public static final int VGDL_TERMINATION_SET = 4;
    */
    public static final Vector2d NIL = new Vector2d(-1, -1);

    public static final Vector2d NONE = new Vector2d(0, 0);
    public static final Vector2d RIGHT = new Vector2d(1, 0);
    public static final Vector2d LEFT = new Vector2d(-1, 0);
    public static final Vector2d UP = new Vector2d(0, -1);
    public static final Vector2d DOWN = new Vector2d(0, 1);

    public static final Vector2d[] BASEDIRS = new Vector2d[]{UP, LEFT, DOWN, RIGHT};

//    public static int[] ALL_ACTIONS = new int[]{KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN,
//                                                KeyEvent.VK_RIGHT, KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE};
    public static enum ACTIONS {
         ACTION_NIL(-1)
        ,ACTION_00(10)
        ,ACTION_01(11)
        ,ACTION_02(12)
        ,ACTION_03(13)
        ,ACTION_04(14)
        ,ACTION_05(15)
        ,ACTION_06(16)
        ,ACTION_07(17)
        ,ACTION_08(18)

        //TODO This is a quick fix to enable games with more than 9 possible actions. This enum should be replaced.

        ,ACTION_09(19)
        ,ACTION_10(20)
        ,ACTION_11(21)
        ,ACTION_12(22)
        ,ACTION_13(23)
        ,ACTION_14(24)
        ,ACTION_15(25)
        ,ACTION_16(26)
        ,ACTION_17(27)
        ,ACTION_18(28)
        ,ACTION_19(29)
        ,ACTION_20(30)
        ,ACTION_21(31)
        ,ACTION_22(32)
        ,ACTION_23(33)
        ,ACTION_24(34)
        ,ACTION_25(35)
        ,ACTION_26(36)
        ,ACTION_27(37)
        ,ACTION_28(38)
        ,ACTION_29(39)
        ,ACTION_30(40)
        ,ACTION_31(41)
        ,ACTION_32(42)
        ,ACTION_33(43)
        ,ACTION_34(44)
        ,ACTION_35(45)
        ,ACTION_36(46)
        ,ACTION_37(47)
        ,ACTION_38(48)
        ,ACTION_39(49)
        ,ACTION_40(50)
        ,ACTION_41(51)
        ,ACTION_42(52)
        ,ACTION_43(53)
        ,ACTION_44(54)
        ,ACTION_45(55)
        ,ACTION_46(56)
        ,ACTION_47(57)
        ,ACTION_48(58)
        ,ACTION_49(59)
        ,ACTION_50(60)
        ,ACTION_51(61)
        ,ACTION_52(62)
        ,ACTION_53(63)
        ,ACTION_54(64)
        ,ACTION_55(65)
        ,ACTION_56(66)
        ,ACTION_57(67)
        ,ACTION_58(68)
        ,ACTION_59(69)
        ,ACTION_60(70)
        ,ACTION_61(71)
        ,ACTION_62(72)
        ,ACTION_63(73)
        ,ACTION_64(74)
        ,ACTION_65(75)
        ,ACTION_66(76)
        ,ACTION_67(77)
        ,ACTION_68(78)
        ,ACTION_69(79)
        ,ACTION_70(80)
        ,ACTION_71(81)
        ,ACTION_72(82)
        ,ACTION_73(83)
        ,ACTION_74(84)
        ,ACTION_75(85)
        ,ACTION_76(86)
        ,ACTION_77(87)
        ,ACTION_78(88)
        ,ACTION_79(89)
        ,ACTION_80(90)
        ,ACTION_81(91)
        ,ACTION_82(92)
        ,ACTION_83(93)
        ,ACTION_84(94)
        ,ACTION_85(95)
        ,ACTION_86(96)
        ,ACTION_87(97)
        ,ACTION_88(98)
        ,ACTION_89(99)
        ,ACTION_90(100)
        ,ACTION_91(101)
        ,ACTION_92(102)
        ,ACTION_93(103)
        ,ACTION_94(104)
        ,ACTION_95(105)
        ,ACTION_96(106)
        ,ACTION_97(107)
        ,ACTION_98(108)
        ,ACTION_99(109)
        ,ACTION_100(110)
        ,ACTION_101(111)
        ,ACTION_102(112)
        ,ACTION_103(113)
        ,ACTION_104(114)
        ,ACTION_105(115)
        ,ACTION_106(116)
        ,ACTION_107(117)
        ,ACTION_108(118)
        ,ACTION_109(119)
        ,ACTION_110(120)
        ,ACTION_111(121)
        ,ACTION_112(122)
        ,ACTION_113(123)
        ,ACTION_114(124)
        ,ACTION_115(125)
        ,ACTION_116(126)
        ,ACTION_117(127)
        ,ACTION_118(128)
        ,ACTION_119(129)
        ,ACTION_120(130)
        ,ACTION_121(131)



//        ,ACTION_UP(new int[]{KeyEvent.VK_UP})
//        ,ACTION_LEFT(new int[]{KeyEvent.VK_LEFT})
//        ,ACTION_DOWN(new int[]{KeyEvent.VK_DOWN})
//        ,ACTION_RIGHT(new int[]{KeyEvent.VK_RIGHT})
//        ,ACTION_USE(new int[]{KeyEvent.VK_SPACE})
//        ,ACTION_ESCAPE(new int[]{KeyEvent.VK_ESCAPE})
        ;

        private int key;
        ACTIONS(int numVal) {			// constructor
            this.key = numVal;
        }

        public int toInt() {
            switch(this) {
                case ACTION_00: return 0;
                case ACTION_01: return 1;
                case ACTION_02: return 2;
                case ACTION_03: return 3;
                case ACTION_04: return 4;
                case ACTION_05: return 5;
                case ACTION_06: return 6;
                case ACTION_07: return 7;
                case ACTION_08: return 8;
                case ACTION_09: return 9;
                case ACTION_10: return 10;
                case ACTION_11: return 11;
                case ACTION_12: return 12;
                case ACTION_13: return 13;
                case ACTION_14: return 14;
                case ACTION_15: return 15;
                case ACTION_16: return 16;
                case ACTION_17: return 17;
                case ACTION_18: return 18;
                case ACTION_19: return 19;
                case ACTION_20: return 20;
                case ACTION_21: return 21;
                case ACTION_22: return 22;
                case ACTION_23: return 23;
                case ACTION_24: return 24;
                case ACTION_25: return 25;
                case ACTION_26: return 26;
                case ACTION_27: return 27;
                case ACTION_28: return 28;
                case ACTION_29: return 29;
                case ACTION_30: return 30;
                case ACTION_31: return 31;
                case ACTION_32: return 32;
                case ACTION_33: return 33;
                case ACTION_34: return 34;
                case ACTION_35: return 35;
                case ACTION_36: return 36;
                case ACTION_37: return 37;
                case ACTION_38: return 38;
                case ACTION_39: return 39;
                case ACTION_40: return 40;
                case ACTION_41: return 41;
                case ACTION_42: return 42;
                case ACTION_43: return 43;
                case ACTION_44: return 44;
                case ACTION_45: return 45;
                case ACTION_46: return 46;
                case ACTION_47: return 47;
                case ACTION_48: return 48;
                case ACTION_49: return 49;
                case ACTION_50: return 50;
                case ACTION_51: return 51;
                case ACTION_52: return 52;
                case ACTION_53: return 53;
                case ACTION_54: return 54;
                case ACTION_55: return 55;
                case ACTION_56: return 56;
                case ACTION_57: return 57;
                case ACTION_58: return 58;
                case ACTION_59: return 59;
                case ACTION_60: return 60;
                case ACTION_61: return 61;
                case ACTION_62: return 62;
                case ACTION_63: return 63;
                case ACTION_64: return 64;
                case ACTION_65: return 65;
                case ACTION_66: return 66;
                case ACTION_67: return 67;
                case ACTION_68: return 68;
                case ACTION_69: return 69;
                case ACTION_70: return 70;
                case ACTION_71: return 71;
                case ACTION_72: return 72;
                case ACTION_73: return 73;
                case ACTION_74: return 74;
                case ACTION_75: return 75;
                case ACTION_76: return 76;
                case ACTION_77: return 77;
                case ACTION_78: return 78;
                case ACTION_79: return 79;
                case ACTION_80: return 80;
                case ACTION_81: return 81;
                case ACTION_82: return 82;
                case ACTION_83: return 83;
                case ACTION_84: return 84;
                case ACTION_85: return 85;
                case ACTION_86: return 86;
                case ACTION_87: return 87;
                case ACTION_88: return 88;
                case ACTION_89: return 89;
                case ACTION_90: return 90;
                case ACTION_91: return 91;
                case ACTION_92: return 92;
                case ACTION_93: return 93;
                case ACTION_94: return 94;
                case ACTION_95: return 95;
                case ACTION_96: return 96;
                case ACTION_97: return 97;
                case ACTION_98: return 98;
                case ACTION_99: return 99;
                case ACTION_100: return 100;
                case ACTION_101: return 101;
                case ACTION_102: return 102;
                case ACTION_103: return 103;
                case ACTION_104: return 104;
                case ACTION_105: return 105;
                case ACTION_106: return 106;
                case ACTION_107: return 107;
                case ACTION_108: return 108;
                case ACTION_109: return 109;
                case ACTION_110: return 110;
                case ACTION_111: return 111;
                case ACTION_112: return 112;
                case ACTION_113: return 113;
                case ACTION_114: return 114;
                case ACTION_115: return 115;
                case ACTION_116: return 116;
                case ACTION_117: return 117;
                case ACTION_118: return 118;
                case ACTION_119: return 119;
                case ACTION_120: return 120;
                case ACTION_121: return 121;
                default: return -1;
            }
        }

        public static ACTIONS fromInt(int iAct) {
            switch (iAct) {
                case 0: return ACTION_00;
                case 1: return ACTION_01;
                case 2: return ACTION_02;
                case 3: return ACTION_03;
                case 4: return ACTION_04;
                case 5: return ACTION_05;
                case 6: return ACTION_06;
                case 7: return ACTION_07;
                case 8: return ACTION_08;
                case 9: return ACTION_09;
                case 10: return ACTION_10;
                case 11: return ACTION_11;
                case 12: return ACTION_12;
                case 13: return ACTION_13;
                case 14: return ACTION_14;
                case 15: return ACTION_15;
                case 16: return ACTION_16;
                case 17: return ACTION_17;
                case 18: return ACTION_18;
                case 19: return ACTION_19;
                case 20: return ACTION_20;
                case 21: return ACTION_21;
                case 22: return ACTION_22;
                case 23: return ACTION_23;
                case 24: return ACTION_24;
                case 25: return ACTION_25;
                case 26: return ACTION_26;
                case 27: return ACTION_27;
                case 28: return ACTION_28;
                case 29: return ACTION_29;
                case 30: return ACTION_30;
                case 31: return ACTION_31;
                case 32: return ACTION_32;
                case 33: return ACTION_33;
                case 34: return ACTION_34;
                case 35: return ACTION_35;
                case 36: return ACTION_36;
                case 37: return ACTION_37;
                case 38: return ACTION_38;
                case 39: return ACTION_39;
                case 40: return ACTION_40;
                case 41: return ACTION_41;
                case 42: return ACTION_42;
                case 43: return ACTION_43;
                case 44: return ACTION_44;
                case 45: return ACTION_45;
                case 46: return ACTION_46;
                case 47: return ACTION_47;
                case 48: return ACTION_48;
                case 49: return ACTION_49;
                case 50: return ACTION_50;
                case 51: return ACTION_51;
                case 52: return ACTION_52;
                case 53: return ACTION_53;
                case 54: return ACTION_54;
                case 55: return ACTION_55;
                case 56: return ACTION_56;
                case 57: return ACTION_57;
                case 58: return ACTION_58;
                case 59: return ACTION_59;
                case 60: return ACTION_60;
                case 61: return ACTION_61;
                case 62: return ACTION_62;
                case 63: return ACTION_63;
                case 64: return ACTION_64;
                case 65: return ACTION_65;
                case 66: return ACTION_66;
                case 67: return ACTION_67;
                case 68: return ACTION_68;
                case 69: return ACTION_69;
                case 70: return ACTION_70;
                case 71: return ACTION_71;
                case 72: return ACTION_72;
                case 73: return ACTION_73;
                case 74: return ACTION_74;
                case 75: return ACTION_75;
                case 76: return ACTION_76;
                case 77: return ACTION_77;
                case 78: return ACTION_78;
                case 79: return ACTION_79;
                case 80: return ACTION_80;
                case 81: return ACTION_81;
                case 82: return ACTION_82;
                case 83: return ACTION_83;
                case 84: return ACTION_84;
                case 85: return ACTION_85;
                case 86: return ACTION_86;
                case 87: return ACTION_87;
                case 88: return ACTION_88;
                case 89: return ACTION_89;
                case 90: return ACTION_90;
                case 91: return ACTION_91;
                case 92: return ACTION_92;
                case 93: return ACTION_93;
                case 94: return ACTION_94;
                case 95: return ACTION_95;
                case 96: return ACTION_96;
                case 97: return ACTION_97;
                case 98: return ACTION_98;
                case 99: return ACTION_99;
                case 100: return ACTION_100;
                case 101: return ACTION_101;
                case 102: return ACTION_102;
                case 103: return ACTION_103;
                case 104: return ACTION_104;
                case 105: return ACTION_105;
                case 106: return ACTION_106;
                case 107: return ACTION_107;
                case 108: return ACTION_108;
                case 109: return ACTION_109;
                case 110: return ACTION_110;
                case 111: return ACTION_111;
                case 112: return ACTION_112;
                case 113: return ACTION_113;
                case 114: return ACTION_114;
                case 115: return ACTION_115;
                case 116: return ACTION_116;
                case 117: return ACTION_117;
                case 118: return ACTION_118;
                case 119: return ACTION_119;
                case 120: return ACTION_120;
                case 121: return ACTION_121;
                default: return ACTION_NIL;
            }
        }

        public static ACTIONS fromString(String strKey)
        {
            if(strKey.equalsIgnoreCase("ACTION_00")) return ACTION_00;
            else if(strKey.equalsIgnoreCase("ACTION_01")) return ACTION_01;
            else if(strKey.equalsIgnoreCase("ACTION_02")) return ACTION_02;
            else if(strKey.equalsIgnoreCase("ACTION_03")) return ACTION_03;
            else if(strKey.equalsIgnoreCase("ACTION_04")) return ACTION_04;
            else if(strKey.equalsIgnoreCase("ACTION_05")) return ACTION_05;
            else if(strKey.equalsIgnoreCase("ACTION_06")) return ACTION_06;
            else if(strKey.equalsIgnoreCase("ACTION_07")) return ACTION_07;
            else if(strKey.equalsIgnoreCase("ACTION_08")) return ACTION_08;
            else if(strKey.equalsIgnoreCase("ACTION_09")) return ACTION_09;
            else if(strKey.equalsIgnoreCase("ACTION_10")) return ACTION_10;
            else if(strKey.equalsIgnoreCase("ACTION_11")) return ACTION_11;
            else if(strKey.equalsIgnoreCase("ACTION_12")) return ACTION_12;
            else if(strKey.equalsIgnoreCase("ACTION_13")) return ACTION_13;
            else if(strKey.equalsIgnoreCase("ACTION_14")) return ACTION_14;
            else if(strKey.equalsIgnoreCase("ACTION_15")) return ACTION_15;
            else if(strKey.equalsIgnoreCase("ACTION_16")) return ACTION_16;
            else if(strKey.equalsIgnoreCase("ACTION_17")) return ACTION_17;
            else if(strKey.equalsIgnoreCase("ACTION_18")) return ACTION_18;
            else if(strKey.equalsIgnoreCase("ACTION_19")) return ACTION_19;
            else if(strKey.equalsIgnoreCase("ACTION_20")) return ACTION_20;
            else if(strKey.equalsIgnoreCase("ACTION_21")) return ACTION_21;
            else if(strKey.equalsIgnoreCase("ACTION_22")) return ACTION_22;
            else if(strKey.equalsIgnoreCase("ACTION_23")) return ACTION_23;
            else if(strKey.equalsIgnoreCase("ACTION_24")) return ACTION_24;
            else if(strKey.equalsIgnoreCase("ACTION_25")) return ACTION_25;
            else if(strKey.equalsIgnoreCase("ACTION_26")) return ACTION_26;
            else if(strKey.equalsIgnoreCase("ACTION_27")) return ACTION_27;
            else if(strKey.equalsIgnoreCase("ACTION_28")) return ACTION_28;
            else if(strKey.equalsIgnoreCase("ACTION_29")) return ACTION_29;
            else if(strKey.equalsIgnoreCase("ACTION_30")) return ACTION_30;
            else if(strKey.equalsIgnoreCase("ACTION_31")) return ACTION_31;
            else if(strKey.equalsIgnoreCase("ACTION_32")) return ACTION_32;
            else if(strKey.equalsIgnoreCase("ACTION_33")) return ACTION_33;
            else if(strKey.equalsIgnoreCase("ACTION_34")) return ACTION_34;
            else if(strKey.equalsIgnoreCase("ACTION_35")) return ACTION_35;
            else if(strKey.equalsIgnoreCase("ACTION_36")) return ACTION_36;
            else if(strKey.equalsIgnoreCase("ACTION_37")) return ACTION_37;
            else if(strKey.equalsIgnoreCase("ACTION_38")) return ACTION_38;
            else if(strKey.equalsIgnoreCase("ACTION_39")) return ACTION_39;
            else if(strKey.equalsIgnoreCase("ACTION_40")) return ACTION_40;
            else if(strKey.equalsIgnoreCase("ACTION_41")) return ACTION_41;
            else if(strKey.equalsIgnoreCase("ACTION_42")) return ACTION_42;
            else if(strKey.equalsIgnoreCase("ACTION_43")) return ACTION_43;
            else if(strKey.equalsIgnoreCase("ACTION_44")) return ACTION_44;
            else if(strKey.equalsIgnoreCase("ACTION_45")) return ACTION_45;
            else if(strKey.equalsIgnoreCase("ACTION_46")) return ACTION_46;
            else if(strKey.equalsIgnoreCase("ACTION_47")) return ACTION_47;
            else if(strKey.equalsIgnoreCase("ACTION_48")) return ACTION_48;
            else if(strKey.equalsIgnoreCase("ACTION_49")) return ACTION_49;
            else if(strKey.equalsIgnoreCase("ACTION_50")) return ACTION_50;
            else if(strKey.equalsIgnoreCase("ACTION_51")) return ACTION_51;
            else if(strKey.equalsIgnoreCase("ACTION_52")) return ACTION_52;
            else if(strKey.equalsIgnoreCase("ACTION_53")) return ACTION_53;
            else if(strKey.equalsIgnoreCase("ACTION_54")) return ACTION_54;
            else if(strKey.equalsIgnoreCase("ACTION_55")) return ACTION_55;
            else if(strKey.equalsIgnoreCase("ACTION_56")) return ACTION_56;
            else if(strKey.equalsIgnoreCase("ACTION_57")) return ACTION_57;
            else if(strKey.equalsIgnoreCase("ACTION_58")) return ACTION_58;
            else if(strKey.equalsIgnoreCase("ACTION_59")) return ACTION_59;
            else if(strKey.equalsIgnoreCase("ACTION_60")) return ACTION_60;
            else if(strKey.equalsIgnoreCase("ACTION_61")) return ACTION_61;
            else if(strKey.equalsIgnoreCase("ACTION_62")) return ACTION_62;
            else if(strKey.equalsIgnoreCase("ACTION_63")) return ACTION_63;
            else if(strKey.equalsIgnoreCase("ACTION_64")) return ACTION_64;
            else if(strKey.equalsIgnoreCase("ACTION_65")) return ACTION_65;
            else if(strKey.equalsIgnoreCase("ACTION_66")) return ACTION_66;
            else if(strKey.equalsIgnoreCase("ACTION_67")) return ACTION_67;
            else if(strKey.equalsIgnoreCase("ACTION_68")) return ACTION_68;
            else if(strKey.equalsIgnoreCase("ACTION_69")) return ACTION_69;
            else if(strKey.equalsIgnoreCase("ACTION_70")) return ACTION_70;
            else if(strKey.equalsIgnoreCase("ACTION_71")) return ACTION_71;
            else if(strKey.equalsIgnoreCase("ACTION_72")) return ACTION_72;
            else if(strKey.equalsIgnoreCase("ACTION_73")) return ACTION_73;
            else if(strKey.equalsIgnoreCase("ACTION_74")) return ACTION_74;
            else if(strKey.equalsIgnoreCase("ACTION_75")) return ACTION_75;
            else if(strKey.equalsIgnoreCase("ACTION_76")) return ACTION_76;
            else if(strKey.equalsIgnoreCase("ACTION_77")) return ACTION_77;
            else if(strKey.equalsIgnoreCase("ACTION_78")) return ACTION_78;
            else if(strKey.equalsIgnoreCase("ACTION_79")) return ACTION_79;
            else if(strKey.equalsIgnoreCase("ACTION_80")) return ACTION_80;
            else if(strKey.equalsIgnoreCase("ACTION_81")) return ACTION_81;
            else if(strKey.equalsIgnoreCase("ACTION_82")) return ACTION_82;
            else if(strKey.equalsIgnoreCase("ACTION_83")) return ACTION_83;
            else if(strKey.equalsIgnoreCase("ACTION_84")) return ACTION_84;
            else if(strKey.equalsIgnoreCase("ACTION_85")) return ACTION_85;
            else if(strKey.equalsIgnoreCase("ACTION_86")) return ACTION_86;
            else if(strKey.equalsIgnoreCase("ACTION_87")) return ACTION_87;
            else if(strKey.equalsIgnoreCase("ACTION_88")) return ACTION_88;
            else if(strKey.equalsIgnoreCase("ACTION_89")) return ACTION_89;
            else if(strKey.equalsIgnoreCase("ACTION_90")) return ACTION_90;
            else if(strKey.equalsIgnoreCase("ACTION_91")) return ACTION_91;
            else if(strKey.equalsIgnoreCase("ACTION_92")) return ACTION_92;
            else if(strKey.equalsIgnoreCase("ACTION_93")) return ACTION_93;
            else if(strKey.equalsIgnoreCase("ACTION_94")) return ACTION_94;
            else if(strKey.equalsIgnoreCase("ACTION_95")) return ACTION_95;
            else if(strKey.equalsIgnoreCase("ACTION_96")) return ACTION_96;
            else if(strKey.equalsIgnoreCase("ACTION_97")) return ACTION_97;
            else if(strKey.equalsIgnoreCase("ACTION_98")) return ACTION_98;
            else if(strKey.equalsIgnoreCase("ACTION_99")) return ACTION_99;
            else if(strKey.equalsIgnoreCase("ACTION_100")) return ACTION_100;
            else if(strKey.equalsIgnoreCase("ACTION_101")) return ACTION_101;
            else if(strKey.equalsIgnoreCase("ACTION_102")) return ACTION_102;
            else if(strKey.equalsIgnoreCase("ACTION_103")) return ACTION_103;
            else if(strKey.equalsIgnoreCase("ACTION_104")) return ACTION_104;
            else if(strKey.equalsIgnoreCase("ACTION_105")) return ACTION_105;
            else if(strKey.equalsIgnoreCase("ACTION_106")) return ACTION_106;
            else if(strKey.equalsIgnoreCase("ACTION_107")) return ACTION_107;
            else if(strKey.equalsIgnoreCase("ACTION_108")) return ACTION_108;
            else if(strKey.equalsIgnoreCase("ACTION_109")) return ACTION_109;
            else if(strKey.equalsIgnoreCase("ACTION_110")) return ACTION_110;
            else if(strKey.equalsIgnoreCase("ACTION_111")) return ACTION_111;
            else if(strKey.equalsIgnoreCase("ACTION_112")) return ACTION_112;
            else if(strKey.equalsIgnoreCase("ACTION_113")) return ACTION_113;
            else if(strKey.equalsIgnoreCase("ACTION_114")) return ACTION_114;
            else if(strKey.equalsIgnoreCase("ACTION_115")) return ACTION_115;
            else if(strKey.equalsIgnoreCase("ACTION_116")) return ACTION_116;
            else if(strKey.equalsIgnoreCase("ACTION_117")) return ACTION_117;
            else if(strKey.equalsIgnoreCase("ACTION_118")) return ACTION_118;
            else if(strKey.equalsIgnoreCase("ACTION_119")) return ACTION_119;
            else if(strKey.equalsIgnoreCase("ACTION_120")) return ACTION_120;
            else if(strKey.equalsIgnoreCase("ACTION_121")) return ACTION_121;

//            if(strKey.equalsIgnoreCase("ACTION_UP")) return ACTION_UP;
//            else if(strKey.equalsIgnoreCase("ACTION_LEFT")) return ACTION_LEFT;
//            else if(strKey.equalsIgnoreCase("ACTION_DOWN")) return ACTION_DOWN;
//            else if(strKey.equalsIgnoreCase("ACTION_RIGHT")) return ACTION_RIGHT;
//            else if(strKey.equalsIgnoreCase("ACTION_USE")) return ACTION_USE;
//            else if(strKey.equalsIgnoreCase("ACTION_ESCAPE")) return ACTION_ESCAPE;
            else return ACTION_NIL;
        }

        public static ACTIONS fromVector(Vector2d move)
        {
            return ACTION_NIL;
        }

    } // enum ACTIONS


    public static enum WINNER {
        PLAYER_DISQ(-100),
        TIE(0),
        PLAYER_LOSES(-1),
        PLAYER_WINS(1);

        private int key;
        WINNER(int val) {key=val;}
        public int key() {return key;}
        public int toInt() {
            return this.key;
        }
    }

    public static final int[] PLAYER_PM = {+1,-1};

    public static final int SCORE_DISQ = -1000;

    // Default (startup) settings for Arena and ArenaTrain
    public static final String[] GUI_AGENT_LIST 	// list of available agents = list of choices in Agent Selectors
    	= {"TDS", "Minimax", "Random", "MCTS", "Human", "MC", "TD-Ntuple"};
    public static final String[] GUI_AGENT_INITIAL  // initial agent choice for P0, P1, ... (for up to 5 players)
    	//= {"TD-Ntuple", "MC", "Human", "Human", "Human"};
    	= {"MCTS", "MC", "Human", "Human", "Human"};
    public static final String[] GUI_PLAYER_NAME  	// player names for P0, P1, ... (for up to 5 players)
    	//= {"P0", "P1", "P2", "P3", "P4"};
    	= {"0", "1", "2", "3", "4"};
    public static final String[] GUI_2PLAYER_NAME  	// player names for 2-player game
		= {"X", "O"};

    // probably GUI_X_PLAYER and GUI_O_PLAYER is not necessary anymore:
    public static final String GUI_X_PLAYER = "TDS";  	// "MCTS" "TDS" "CMA-ES" "Minimax" 
    public static final String GUI_O_PLAYER = "MCTS";	// "Human";"MCTS";

	public static final int GUI_ARENATRAIN_WIDTH = 465;
	public static final int GUI_ARENATRAIN_HEIGHT = 420;
	public static final int GUI_WINCOMP_WIDTH = 350;
	public static final int GUI_WINCOMP_HEIGHT = 300;
	public static final int GUI_PARAMTABS_WIDTH = 464;	// wide enough to hold 6 tabs
	public static final int GUI_PARAMTABS_HEIGHT = 300;

	public static final int GUI_HELPFONTSIZE = 14;

	// Default directory for loading and saving PlayAgents
	public static final String GUI_DEFAULT_DIR_AGENT = "agents";
 }
