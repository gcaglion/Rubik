package my3d;
import java.awt.Color;
import org.opencv.core.Scalar;

public class MyColor{
		//-- Properties
		public int Id;
		public Scalar CV;
		public Color Java;
		public String Desc;
		public boolean isSet;
		private static Color myOrange=new Color(230,110,25);
		
		//-- Color Definitions
		public static final int RED=0;
		public static final int BLUE=1;
		public static final int ORANGE=2;
		public static final int GREEN=3;
		public static final int WHITE=4;
		public static final int YELLOW=5;
		public static final int BLACK=6;
		public static final int CYAN=7;
		public static final int MAGENTA=8;
		public static final int GRAY=9;

		//-- HSV Detection ranges
		public Scalar[] clrRangeMin=new Scalar[6];
		public Scalar[] clrRangeMax=new Scalar[6];
		//-- Color standard definition in BGR
		static Scalar[] clrStd=new Scalar[6+4];	// +1 for black

		//-- Constructors.
		public MyColor(){
			SetFixedValues();
			SetDefault();
		}
		public MyColor(int clr){
			SetFixedValues();
			this.Set(clr);
		}
		public void SetFixedValues(){
			//-- defines Color Ranges (HSV)
			clrRangeMin[BLUE]=new Scalar(100,50,50); clrRangeMax[BLUE]=new Scalar(120, 255, 120);
			clrRangeMin[ORANGE]=new Scalar(0,50,50);clrRangeMax[ORANGE]=new Scalar(15,255,200);
			clrRangeMin[RED]=new Scalar(160,100,100);clrRangeMax[RED]=new Scalar(170,255,255);
			clrRangeMin[YELLOW]=new Scalar(16,50,50);clrRangeMax[YELLOW]=new Scalar(30, 255, 200);
			clrRangeMin[GREEN]=new Scalar(60,50,50);clrRangeMax[GREEN]=new Scalar(95,255,200);
			clrRangeMin[WHITE]=new Scalar(10, 10, 50);clrRangeMax[WHITE]=new Scalar(170,100,255);
			//-- defines colors standard (BGR)
			clrStd[BLUE]=new Scalar(255,0,0); 
			clrStd[RED]=new Scalar(0,0,255); 
			clrStd[YELLOW]=new Scalar(0,255,255); 
			clrStd[ORANGE]=new Scalar(25,110,230); 
			clrStd[GREEN]=new Scalar(0,128,0); 
			clrStd[WHITE]=new Scalar(255,255,255);
			clrStd[BLACK]=new Scalar(0,0,0);
			clrStd[CYAN]=new Scalar(255,255,0);
			clrStd[MAGENTA]=new Scalar(255,0,255);
			clrStd[GRAY]=new Scalar(128,128,128);
			
		}
		public void SetDefault(){
			//-- set default color for a newly created object
			Id=BLACK;
			CV=clrStd[BLACK];
			Java=Color.BLACK;
			Desc="Black";
			isSet=false;
		}
		
		//-- Set methods, overloaded. Each sets all three properties
		public void Set(final MyColor clr){
			Id=clr.Id;
			CV=clr.CV;
			Java=clr.Java;
			Desc=clr.Desc;
		}
		public void Set(final int clr){
			Id=clr;
			CV=Id2CV(clr);
			Java=Id2J(clr);
			Desc=Id2Desc(clr);
		}
		public void Set(Scalar clr){
			Id=CV2Id(clr);
			CV=clr;
			Java=CV2J(clr);
			Desc="TBD!";	//clr2Desc(clr);
		}
		public void Set(Color clr){
			Id=J2Id(clr);
			CV=J2CV(clr);
			Java=clr;
			Desc="TBD!";	//J2Desc(clr);
		}
		
		//-- isSet handling (Lock, Toggle)
		public void Lock(){ isSet=true; }
		public void Free(){	SetDefault(); }
		public void Toggle(){ isSet=!isSet; }
		
		//-- Color conversion functions
		public static int CV2Id(Scalar clr){
			if(clr==clrStd[RED]) return RED;
			if(clr==clrStd[BLUE]) return BLUE;
			if(clr==clrStd[GREEN]) return GREEN;
			if(clr==clrStd[WHITE]) return WHITE;
			if(clr==clrStd[YELLOW]) return YELLOW;
			if(clr==clrStd[ORANGE]) return ORANGE;
			if(clr==clrStd[CYAN]) return CYAN;
			if(clr==clrStd[MAGENTA]) return MAGENTA;
			if(clr==clrStd[GRAY]) return GRAY;
			return 0;
		}
		public static Color CV2J(Scalar clr){
			if(clr==clrStd[RED]) return Color.RED;
			if(clr==clrStd[BLUE]) return Color.BLUE;
			if(clr==clrStd[GREEN]) return Color.GREEN;
			if(clr==clrStd[WHITE]) return Color.WHITE;
			if(clr==clrStd[YELLOW]) return Color.YELLOW;
			if(clr==clrStd[ORANGE]) return myOrange;
			if(clr==clrStd[CYAN]) return Color.CYAN;
			return Color.BLACK;
		}
		public static int J2Id(Color clr){
			if(clr.equals(Color.RED)) return RED;
			if(clr.equals(Color.BLUE)) return BLUE;
			if(clr.equals(Color.GREEN)) return GREEN;
			if(clr.equals(Color.WHITE)) return WHITE;
			if(clr.equals(Color.YELLOW)) return YELLOW;
			if(clr.equals(myOrange)) return ORANGE;
			if(clr.equals(Color.CYAN)) return CYAN;
			if(clr.equals(Color.MAGENTA)) return MAGENTA;
			if(clr.equals(Color.GRAY)) return GRAY;
			if(clr.equals(Color.BLACK)) return BLACK;
			return 0;
		}
		public static Scalar J2CV(Color clr){
			if(clr==Color.RED) return clrStd[RED];
			if(clr==Color.BLUE) return clrStd[BLUE];
			if(clr==Color.GREEN) return clrStd[GREEN];
			if(clr==Color.WHITE) return clrStd[WHITE];
			if(clr==Color.YELLOW) return clrStd[YELLOW];
			if(clr==myOrange) return clrStd[ORANGE];
			if(clr==Color.CYAN) return clrStd[CYAN];
			return clrStd[BLACK];
		}
		public static String Id2Desc(int clrId){
			switch(clrId){
			case RED: return "Red";
			case BLUE: return "Blue";
			case GREEN: return "Green";
			case WHITE: return "White";
			case YELLOW: return "Yellow";
			case ORANGE: return "Orange";
			default: return "Undefined Color";
			}
		}
		public static Scalar Id2CV(final int clrId){
			switch(clrId){
			case RED: return clrStd[RED];
			case BLUE: return clrStd[BLUE];
			case GREEN: return clrStd[GREEN];
			case WHITE: return clrStd[WHITE];
			case YELLOW: return clrStd[YELLOW];
			case ORANGE: return clrStd[ORANGE];
			case BLACK: return clrStd[BLACK];
			case CYAN: return clrStd[CYAN];
			case MAGENTA: return clrStd[MAGENTA];
			case GRAY: return clrStd[GRAY];
			default: return null;
			}		
		}
		public static Color Id2J(int clrId){
			switch(clrId){
			case RED: return Color.RED;
			case BLUE: return Color.BLUE;
			case GREEN: return Color.GREEN;
			case WHITE: return Color.WHITE;
			case YELLOW: return Color.YELLOW;
			case ORANGE: return myOrange;
			case CYAN: return Color.CYAN;
			case MAGENTA: return Color.MAGENTA;
			case GRAY: return Color.GRAY;
			default: return Color.BLACK;
			}		
		}

}

