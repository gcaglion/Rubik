package rubik;

import my3d.MyColor;

public class CubeFace{
		public static final int F=0;
		public static final int R=1;
		public static final int B=2;
		public static final int L=3;
		public static final int U=4;
		public static final int D=5;
		
		public int Id;
		
		//-- Face default color
		public static int GetDefaultColor(int pFace){
			int ret=MyColor.BLACK;
			switch (pFace){
			case F: ret=MyColor.RED; break;
			case R: ret=MyColor.BLUE; break;
			case B: ret=MyColor.ORANGE; break;
			case L: ret=MyColor.GREEN; break;
			case U: ret=MyColor.WHITE; break;
			case D: ret=MyColor.YELLOW; break;
			}
			return ret;
		}
		//-- conversion functions
 		public static int Desc2Id(String pDesc){
			if(pDesc.equals("F")) return 0;
			if(pDesc.equals("R")) return 1;
			if(pDesc.equals("B")) return 2;
			if(pDesc.equals("L")) return 3;
			if(pDesc.equals("U")) return 4;
			if(pDesc.equals("D")) return 5;
			if(pDesc.equals("X")) return 6;
			if(pDesc.equals("Y")) return 7;
			if(pDesc.equals("Z")) return 8;
			return -1;
		}
		public String Id2SetDesc(int id){
			switch(id){
				case F: return "Set F";
				case R: return "Set R";
				case B: return "Set B";
				case L: return "Set L";
				case U: return "Set U";
				case D: return "Set D";
				default: return null;
			}
		}
		public static String Id2Desc(int id){
			switch(id){
				case F: return "F";
				case R: return "R";
				case B: return "B";
				case L: return "L";
				case U: return "U";
				case D: return "D";
				default: return null;
			}
		}
		
		//-- Face order for cube solution
		public static int[] getOrderedFace(){
			int[] ret=new int[6];
			ret[0]=U;
			ret[1]=D;
			ret[2]=R;
			ret[3]=L;
			ret[4]=F;
			ret[5]=B;
			return ret;
		}
}

