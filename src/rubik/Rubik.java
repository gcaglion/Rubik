package rubik;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import my3d.Cbox;
import my3d.MyColor;
import my3d.ObjectGroup;
import my3d.Object3d;
import my3d.Scene3D;

public class Rubik extends Object3d {
	
	public void Rotate(int alpha, int beta, int gamma){}
	public void Traslate(int tx, int ty, int tz){}
	public void Project(Scene3D s){}

	int cellSize;
	int cellGap;
	int cellThickness;
		
	// refreshing parameters
	Container gui2d;
	Container gui3d;
	SolutionBox slnBox;
	boolean showMove;
	
	// Threading stuff
	Thread lmt;	// last move thread
	int movesTotCount;
	JOptionPane ShuffleWait;
	
	//-- definitions, Cube elements 
	int cubeSize;
	Ccell cell[][][];
	Ccell cellTmp[][];
	cEdge edge[];
	int cubeDepth;
	boolean isOddSize;
	
	//-- face order for solution of cubes >3
	int[] orderedFace={CubeFace.U, CubeFace.D, CubeFace.R, CubeFace.L, CubeFace.F, CubeFace.B};

	
	int currX, currY, currZ;	// Cube position in space

	// 3d main parameters
	Cbox cell3d[];		//Cbox cell3dTmp[][];
	ObjectGroup face3d[];
	int move3Ddelay;
	boolean showCellLabels=false;
	
	String Solution=new String();
		
	static final int X_AXIS=0;
	static final int Y_AXIS=1;
	static final int Z_AXIS=2;
	
	final int[] MidEdge={cEdge.FL, cEdge.FR, cEdge.BR, cEdge.BL};	// Middle edges sequence
	final int[] MidEdge_clr1={MyColor.RED, MyColor.BLUE, MyColor.ORANGE, MyColor.GREEN};
	final int[] MidEdge_clr2={MyColor.GREEN, MyColor.RED, MyColor.BLUE, MyColor.ORANGE};
	final int[] TopCorner={1,4,5,0};
	final int[] TopCorner_clr1={MyColor.RED,	MyColor.BLUE,	MyColor.ORANGE,	MyColor.GREEN };
	final int[] TopCorner_clr2={MyColor.WHITE, 	MyColor.WHITE, 	MyColor.WHITE,	MyColor.WHITE };
	final int[] TopCorner_clr3={MyColor.BLUE, 	MyColor.ORANGE,	MyColor.GREEN,	MyColor.RED };
	int[] bcPos=new int[4];	// bottom corners positions
	int[] bePos=new int[4];	// bottom edges positions
	final int[] BottomCorner_clr1={MyColor.YELLOW,	MyColor.YELLOW,	MyColor.YELLOW,	MyColor.YELLOW };
	final int[] BottomCorner_clr2={MyColor.GREEN, 	MyColor.RED, 	MyColor.BLUE,	MyColor.ORANGE };
	final int[] BottomCorner_clr3={MyColor.RED, 	MyColor.BLUE,	MyColor.ORANGE,	MyColor.GREEN };
	
	//============== START INITIALIZATION STUFF =============================	
	//-- constructor	
	public Rubik(int pcubeSize,  int pcellSize, int pcellGap, int pcellThickness){
		// Constructor for specific status cube. Needs cell[][][] to be provided, either by  color detector or manual setting
		cubeSize=pcubeSize;		
		cellSize=pcellSize;
		cellGap=pcellGap;
		cellThickness=pcellThickness;		
		cubeDepth=Math.floorDiv(cubeSize, 2);	//-- cubeDepth is needed for generalization across different sizes
		isOddSize=((float)cubeSize/2!=Math.floorDiv(cubeSize, 2));
		
		currX=0; currY=0; currZ=0;				//-- init Cube Position, to keep track of traslations
		showMove=false;
		
		// Init cube (2D,3D)
		initCube();
	}

	void setGui(Container g2d, Container g3d, SolutionBox sBox){
		gui3d=g3d;
		gui2d=g2d;
		slnBox=sBox;
	}

	void initCube0(){
		int i;
		//-- 2D
 		cell=new Ccell[6][cubeSize][cubeSize];  
		cellTmp=new Ccell[cubeSize][cubeSize+1];	// +1 is to avoid conflicts when Traslate() calls mrotateFace()
		for(int f=0;f<6;f++){
			for(int y=0;y<cubeSize;y++){
				for(int x=0;x<cubeSize;x++){
					cell[f][y][x]=new Ccell(CubeFace.GetDefaultColor(f));
				}
				for(int x=0;x<(cubeSize+1);x++){
					if(f==0) cellTmp[y][x]=new Ccell();
				}
			}
		}
		
		//-- 3D
		ensemble=new ObjectGroup();	// this is the main ensemble, i.e. the one holding the whole rubik together
		cell3d=new Cbox[6*cubeSize*cubeSize];
		face3d=new ObjectGroup[6];

		i=0;
		for(int f=0; f<6; f++){
			face3d[f]=new ObjectGroup();
			//-- add cells
			for(int y=0; y<cubeSize; y++){
				for(int x=0; x<cubeSize; x++){
					//cell3d[i]=new Cbox(cellSize, cellSize, cellThickness, cellGap, (CubeFace.Id2Desc(f)+y+x));
					cell3d[i]=new Cbox(cellSize, cellSize, cellThickness, cellGap);
					i++;
				}
			}
		}
		
		//-- edges
		edge=new cEdge[24+1];
		for(int e=(0+1); e<(24+1); e++) edge[e]=new cEdge(e);
	}
	void initCube1(){
		int i;
		//-- 3D		
		ensemble.clear();
		i=0;
		for(int f=0;f<6;f++){
			for(int y=0;y<cubeSize;y++){
				for(int x=0;x<cubeSize;x++){
					cell3d[i].resetPos();
					cell3d[i].Traslate(x*cellSize, y*cellSize, 0);
					face3d[f].add(cell3d[i]);					
					i++;
				}
			}
			//-- center the face on the xy plane at z=0
			face3d[f].Traslate(-cubeSize*cellSize/2, -cubeSize*cellSize/2, -cellThickness/2);
			switch(f){
			case CubeFace.F:
				face3d[f].Traslate(0,0,cubeSize*cellSize/2+cellThickness/2);
				break;
			case CubeFace.R:
				face3d[f].Rotate(0, -90, 0);
				face3d[f].Traslate(cubeSize*cellSize/2+cellThickness/2,0,0);
				break;
			case CubeFace.B:
				face3d[f].Rotate(0,180, 0);
				face3d[f].Traslate(0,0,-cubeSize*cellSize/2-cellThickness/2);
				break;
			case CubeFace.L:
				face3d[f].Rotate(0, 90, 0);
				face3d[f].Traslate(-cubeSize*cellSize/2-cellThickness/2,0,0);
				break;
			case CubeFace.U:
				face3d[f].Rotate(-90, 0, 0);
				face3d[f].Traslate(0,-cubeSize*cellSize/2-cellThickness/2,0);
				break;
			case CubeFace.D:
				face3d[f].Rotate(90, 0, 0);
				face3d[f].Traslate(0,cubeSize*cellSize/2+cellThickness/2,0);
				break;
			}
			ensemble.add(face3d[f]);
		}
		this.pCount=ensemble.pCount;
		this.p=ensemble.p;	
		
		if(slnBox!=null) slnBox.reset();
	}
	void initCube2(){
		int i=0;
		for(int f=0;f<6;f++){
			for(int y=0;y<cubeSize;y++){
				for(int x=0;x<cubeSize;x++){
					cell3d[i].setColor(CubeFace.GetDefaultColor(f));
					cell[f][y][x].setCboxId(i);	//-- link this Cbox to its cell
					i++;
				}
			}
		}
	}
	void initCube(){
		initCube0();
		initCube1();
		initCube2();
	}
	void set3Dspeed(int s){
		move3Ddelay=-2*s+20;
	}
	void setCells(MyColor cellColor[][][]){
		// Copy Cells
		for(int f=0;f<6;f++){
			for(int y=0;y<cubeSize;y++){
				for(int x=0;x<cubeSize;x++){
					cell[f][y][x].setColor(cellColor[f][y][x].Id);
				}
			}
		}
	}
	//============== END INITIALIZATION STUFF =============================	

	//============== START MOVES STUFF =============================	
	String[] InitMoves(int cSize){
		//-- Defines all possible moves

		// MoveCodesCount depends on cubeSize: 6 faces, 3 base moves(F,F',F2), for each available slice, 
		int sc=Math.floorDiv(cSize-1, 2)+1;
		movesTotCount=6*3*sc;
		String[] ret=new String[movesTotCount];

		int i=0;
		for(int f=0;f<6;f++){
			for(int s=0;s<sc;s++){
				// f
				ret[i]=CubeFace.Id2Desc(f)+new String(new char[s]).replace("\0", "*");
				// f'
				ret[i+1]=ret[i]+"'";
				// f2
				ret[i+2]=ret[i]+"2";
				i+=3;
			}
		}	
		return ret;
	}
 	private ArrayList<String> parseCmd(String fullcmd){
 		// returns a list of strings with the moves 
 		ArrayList<String> ret=new ArrayList<String>();
		int i=0;		
		String c;
		StringBuilder cmd=new StringBuilder();
		while(i<fullcmd.length()){
			c=fullcmd.substring(i, i+1);
			if(c.equals("F") || c.equals("R") || c.equals("B") || c.equals("L") || c.equals("U") || c.equals("D")|| c.equals("X")|| c.equals("Y")|| c.equals("Z")){
				if(i>0){
					ret.add(cmd.toString());
					cmd=new StringBuilder();
				}
				cmd.append(c);
			} else{
				cmd.append(c);
			}
			i++;
		}
		// Last cmd has not been saved. need to close previous cmd, add it to Move[]
		ret.add(cmd.toString());
		
		return ret;
	}
 	boolean doMove(String cmd){
		if(lmt!=null) try{lmt.join();} catch (InterruptedException e) {e.printStackTrace();}
		
		new cMoveFull(this, cmd).run();
		
		//-- before returning, wait for the move to be completed
		if(lmt!=null) try{lmt.join();} catch (InterruptedException e) {e.printStackTrace();}

		return true;
 	}
	class cMoveFull implements Runnable{
	    Rubik cube;
	    String cmd;
	    String singleMove;
	    
	    cMoveFull(Rubik cube, String cmd){
	    	this.cube=cube;
	    	this.cmd=cmd.trim();
	    }
	    
		public void run(){
			//System.out.println("Rubik::cMove() called with "+cmd);
			Solution=Solution.concat(cmd);
			
			if(cmd.length()==0) return;
			
	 		ArrayList<String> move=parseCmd(cmd);
	 		
	 		Thread mt[]=new Thread[move.size()];
	 		
			for(int i=0; i<move.size();i++){
				if(ShuffleInterrupted) break;
				singleMove=move.get(i);
				if(i>0) try { mt[i-1].join(); } catch (InterruptedException e) {e.printStackTrace();}	// waits for the previous move to be completed
				mt[i]= new Thread( new cMoveSingle( cube, singleMove) ) ;
				mt[i].setName("Move_"+singleMove);
				mt[i].start();
				slnBox.addText(singleMove);					
			}
			lmt=mt[move.size()-1];	// thread for last move
	    }
	}
	class cMoveSingle implements Runnable{
		Rubik cube;
		String moveText;	// this should be a single move
		int face;
		int depth;
		int dir;	// (1 for clockwise, 2 for 2, 3 for counterclockwise)
		ObjectGroup movingCells;
		Thread th;

		// constructor
		cMoveSingle(Rubik Rcube, String cmd){
			cube=Rcube;
			initCommon(cmd);
		}
		// actual constructors' routine
		void initCommon(String cmd){
			moveText=cmd.trim();
			face=CubeFace.Desc2Id(moveText.substring(0, 1));
			depth=(face>5)?0:( moveText.length()-moveText.replace("*", "").length() );	// counts asterisks in m
			String scount=moveText.substring(moveText.length() - 1);
			if(scount.equals("'")){
				dir=3;
			} else if(scount.equals("2")){
				dir=2;
			} else{
				dir=1;
			}
			if(showMove) movingCells=getMovingCells();
		}

		public void run(){
			//System.out.println("Move::run(): Thread="+th+" ; face="+face+" ; depth="+depth+" ; dir="+dir);
				int delay=move3Ddelay; //System.out.println("cMoveSingle(): delay="+delay);
				// 3D move
				int change3d=0;			
				if(dir==1){change3d=-1;}
				if(dir==2){change3d=-2;}
				if(dir==3){change3d=1;}

				// delay adjustments
				//System.out.println("cMoveSingle(): delay BEFORE ="+delay);
				if(depth>0) delay*=4;
				if(depth==0 && face<6) delay*=3;
				//System.out.println("cMoveSingle(): delay AFTER  ="+delay);

				if(showMove){
					for(int i=0; i<90; i++){
						switch(face){
						case CubeFace.F:
							movingCells.Rotate(0, 0, change3d);
							break;
						case CubeFace.R:
							movingCells.Rotate(change3d, 0, 0);
							break;
						case CubeFace.B:
							movingCells.Rotate(0, 0, -change3d);
							break;
						case CubeFace.L:
							movingCells.Rotate(-change3d, 0, 0);
							break;
						case CubeFace.U:
							movingCells.Rotate(0, -change3d, 0);
							break;
						case CubeFace.D:
							movingCells.Rotate(0, change3d, 0);
							break;
						case 6:		/* Rotate X */
							movingCells.Rotate(change3d, 0, 0);
							break;
						case 7:		/* Rotate Y */
							movingCells.Rotate(0, 0, change3d);
							break;
						case 8:		/* Rotate Z */
							movingCells.Rotate(0, -change3d, 0);
							break;
						}
						cube.gui3d.repaint();
						try{Thread.sleep(delay);} catch (InterruptedException e){ e.printStackTrace();}
					}
				}
				// 2D move
				if(face>5){
					cube.TraslateCube(face-6, dir);	// face-6 becomes X_AXIS (0) , Y_AXIS (1), or Z_AXIS (2)
				} else{
					cube.mRotateSlice(face, depth, dir);
				}
				cube.gui2d.repaint();
		}
		
		private ObjectGroup getMovingCells(){
			ObjectGroup ret=new ObjectGroup();

			if(face>5){
				// cube rotation. get ALL the cells
				for(int f=0; f<6; f++){
					for(int y=0; y<cube.cubeSize; y++){
						for(int x=0; x<cube.cubeSize; x++){
							ret.add(cube.cell3d[cube.cell[f][y][x].CboxId]);
						}
					}
				}
			} else{
				// 1. surrounding cells
				for(int i=0; i<cube.cubeSize; i++){
					switch(face){
					case CubeFace.F:
						ret.add(cube.cell3d[cube.cell[CubeFace.U][cube.cubeSize-1-depth][i].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.R][i][depth].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.D][depth][cube.cubeSize-1-i].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.L][i][cube.cubeSize-1-depth].CboxId]);
						break;
					case CubeFace.R:
						ret.add(cube.cell3d[cube.cell[CubeFace.U][cube.cubeSize-i-1][cube.cubeSize-1-depth].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.B][i][depth].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.D][cube.cubeSize-1-i][cube.cubeSize-1-depth].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.F][cube.cubeSize-i-1][cube.cubeSize-1-depth].CboxId]);
						break;
					case CubeFace.B:
						ret.add(cube.cell3d[cube.cell[CubeFace.U][depth][cube.cubeSize-i-1].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.L][i][depth].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.D][cube.cubeSize-1-depth][i].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.R][cube.cubeSize-1-i][cube.cubeSize-1-depth].CboxId]);
						break;
					case CubeFace.L:
						ret.add(cube.cell3d[cube.cell[CubeFace.U][i][depth].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.F][i][depth].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.D][i][depth].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.B][cube.cubeSize-1-i][cube.cubeSize-1-depth].CboxId]);
						break;
					case CubeFace.U:
						ret.add(cube.cell3d[cube.cell[CubeFace.F][depth][i].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.R][depth][i].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.B][depth][i].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.L][depth][i].CboxId]);
						break;
					case CubeFace.D: 
						ret.add(cube.cell3d[cube.cell[CubeFace.F][cube.cubeSize-1-depth][i].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.R][cube.cubeSize-1-depth][i].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.B][cube.cubeSize-1-depth][i].CboxId]);
						ret.add(cube.cell3d[cube.cell[CubeFace.L][cube.cubeSize-1-depth][i].CboxId]);
						break;
					}
				}
				if(depth==0){
					// 2. face cells
					for(int y=0; y<cube.cubeSize; y++){
						for(int x=0; x<cube.cubeSize; x++){
							ret.add(cube.cell3d[cube.cell[face][y][x].CboxId]);
						}
					}
				}
			}
			return ret;
		}
	
	}
	void mrotateFace(Ccell[][] faceCell, int rcount) {
		int i, l;
		int lmax=Math.floorDiv(cubeSize-1, 2)+1;
		
		for(int r=0;r<rcount;r++){
			for(l=0;l<lmax;l++){
				// corners
				cellTmp[0][0]							.setColor(faceCell[l][l].clr						);
				faceCell[l][l]							.setColor(faceCell[cubeSize-1-l][l].clr				);
				faceCell[cubeSize-1-l][l]				.setColor(faceCell[cubeSize-1-l][cubeSize-1-l].clr	);
				faceCell[cubeSize-1-l][cubeSize-1-l]	.setColor(faceCell[l][cubeSize-1-l].clr				);
				faceCell[l][cubeSize-1-l]				.setColor(cellTmp[0][0].clr							);
				cellTmp[0][0]							.setCboxId(faceCell[l][l].CboxId						);
				faceCell[l][l]							.setCboxId(faceCell[cubeSize-1-l][l].CboxId				);
				faceCell[cubeSize-1-l][l]				.setCboxId(faceCell[cubeSize-1-l][cubeSize-1-l].CboxId	);
				faceCell[cubeSize-1-l][cubeSize-1-l]	.setCboxId(faceCell[l][cubeSize-1-l].CboxId				);
				faceCell[l][cubeSize-1-l]				.setCboxId(cellTmp[0][0].CboxId							);
				// within corners
				for(i=l+1;i<(cubeSize-1-l);i++){

					cellTmp[i][0]						.setColor( faceCell[i][l].clr						);
					faceCell[i][l]						.setColor( faceCell[cubeSize-1-l][i].clr			);
					faceCell[cubeSize-1-l][i]			.setColor( faceCell[cubeSize-1-i][cubeSize-1-l].clr	);
					faceCell[cubeSize-1-i][cubeSize-1-l].setColor( faceCell[l][cubeSize-1-i].clr			);
					faceCell[l][cubeSize-1-i]			.setColor( cellTmp[i][0].clr						);
					
					cellTmp[i][0]						.setCboxId( faceCell[i][l].CboxId						);
					faceCell[i][l]						.setCboxId( faceCell[cubeSize-1-l][i].CboxId			);
					faceCell[cubeSize-1-l][i]			.setCboxId( faceCell[cubeSize-1-i][cubeSize-1-l].CboxId	);
					faceCell[cubeSize-1-i][cubeSize-1-l].setCboxId( faceCell[l][cubeSize-1-i].CboxId			);
					faceCell[l][cubeSize-1-i]			.setCboxId( cellTmp[i][0].CboxId						);
				}
			}
		}
	}
	boolean mRotateSlice(int pFace, int depth, int rcount){
		// rcount: 1/2/3
		boolean ret=true;
		
		switch(cubeSize){
		case 3: 
			if(depth>1) ret=false; break;
		case 4: 
			if(depth>1) ret=false; break;
		case 5: 
			if(depth>2) ret=false; break;
		case 6: 
			if(depth>2) ret=false; break;
		case 7: 
			if(depth>3) ret=false; break;
		}
		//--
		if(ret){
			for(int r=0;r<rcount;r++){
				switch(pFace){
				case CubeFace.F:
					// F*, F**, F***
					for(int i=0;i<cubeSize;i++){
						cellTmp[i][0]										.setColor(cell[CubeFace.L][i][cubeSize-1-depth].clr);
						cell[CubeFace.L][i][cubeSize-1-depth]				.setColor(cell[CubeFace.D][depth][i].clr);
						cell[CubeFace.D][depth][i]							.setColor(cell[CubeFace.R][cubeSize-1-i][depth].clr);
						cell[CubeFace.R][cubeSize-1-i][depth]				.setColor(cell[CubeFace.U][cubeSize-1-depth][cubeSize-1-i].clr);
						cell[CubeFace.U][cubeSize-1-depth][cubeSize-1-i]	.setColor(cellTmp[i][0].clr);
						cellTmp[i][0]										.setCboxId(cell[CubeFace.L][i][cubeSize-1-depth].CboxId);
						cell[CubeFace.L][i][cubeSize-1-depth]				.setCboxId(cell[CubeFace.D][depth][i].CboxId);
						cell[CubeFace.D][depth][i]							.setCboxId(cell[CubeFace.R][cubeSize-1-i][depth].CboxId);
						cell[CubeFace.R][cubeSize-1-i][depth]				.setCboxId(cell[CubeFace.U][cubeSize-1-depth][cubeSize-1-i].CboxId);
						cell[CubeFace.U][cubeSize-1-depth][cubeSize-1-i]	.setCboxId(cellTmp[i][0].CboxId);
					}
					break;
				case CubeFace.R:
					// R*, R**, R***
					for(int i=0;i<cubeSize;i++){
						cellTmp[i][0]								.setColor(cell[CubeFace.F][i][cubeSize-1-depth].clr);
						cell[CubeFace.F][i][cubeSize-1-depth].setColor(cell[CubeFace.D][i][cubeSize-1-depth].clr);
						cell[CubeFace.D][i][cubeSize-1-depth].setColor(cell[CubeFace.B][cubeSize-1-i][depth].clr);
						cell[CubeFace.B][cubeSize-1-i][depth].setColor(cell[CubeFace.U][i][cubeSize-1-depth].clr);
						cell[CubeFace.U][i][cubeSize-1-depth].setColor(cellTmp[i][0].clr);
						cellTmp[i][0]						 .setCboxId(cell[CubeFace.F][i][cubeSize-1-depth].CboxId);
						cell[CubeFace.F][i][cubeSize-1-depth].setCboxId(cell[CubeFace.D][i][cubeSize-1-depth].CboxId);
						cell[CubeFace.D][i][cubeSize-1-depth].setCboxId(cell[CubeFace.B][cubeSize-1-i][depth].CboxId);
						cell[CubeFace.B][cubeSize-1-i][depth].setCboxId(cell[CubeFace.U][i][cubeSize-1-depth].CboxId);
						cell[CubeFace.U][i][cubeSize-1-depth].setCboxId(cellTmp[i][0].CboxId);
					}
					break;
				case CubeFace.B:
					// B*, B**, B***
					for(int i=0;i<cubeSize;i++){
						cellTmp[i][0]									.setColor(cell[CubeFace.L][i][depth].clr);
						cell[CubeFace.L][i][depth]						.setColor(cell[CubeFace.U][depth][cubeSize-1-i].clr);
						cell[CubeFace.U][depth][cubeSize-1-i]			.setColor(cell[CubeFace.R][cubeSize-1-i][cubeSize-1-depth].clr);
						cell[CubeFace.R][cubeSize-1-i][cubeSize-1-depth].setColor(cell[CubeFace.D][cubeSize-1-depth][i].clr);
						cell[CubeFace.D][cubeSize-1-depth][i]			.setColor(cellTmp[i][0].clr);
						cellTmp[i][0]									.setCboxId(cell[CubeFace.L][i][depth].CboxId);
						cell[CubeFace.L][i][depth]						.setCboxId(cell[CubeFace.U][depth][cubeSize-1-i].CboxId);
						cell[CubeFace.U][depth][cubeSize-1-i]			.setCboxId(cell[CubeFace.R][cubeSize-1-i][cubeSize-1-depth].CboxId);
						cell[CubeFace.R][cubeSize-1-i][cubeSize-1-depth].setCboxId(cell[CubeFace.D][cubeSize-1-depth][i].CboxId);
						cell[CubeFace.D][cubeSize-1-depth][i]			.setCboxId(cellTmp[i][0].CboxId);
					}
					break;
				case CubeFace.L:
					// L*, L**, L***
					for(int i=0;i<cubeSize;i++){
						cellTmp[i][0]						 .setColor(cell[CubeFace.B][i][cubeSize-1-depth].clr);
						cell[CubeFace.B][i][cubeSize-1-depth].setColor(cell[CubeFace.D][cubeSize-1-i][depth].clr);
						cell[CubeFace.D][cubeSize-1-i][depth].setColor(cell[CubeFace.F][cubeSize-1-i][depth].clr);
						cell[CubeFace.F][cubeSize-1-i][depth].setColor(cell[CubeFace.U][cubeSize-1-i][depth].clr);
						cell[CubeFace.U][cubeSize-1-i][depth].setColor(cellTmp[i][0].clr);
						cellTmp[i][0]						 .setCboxId(cell[CubeFace.B][i][cubeSize-1-depth].CboxId);
						cell[CubeFace.B][i][cubeSize-1-depth].setCboxId(cell[CubeFace.D][cubeSize-1-i][depth].CboxId);
						cell[CubeFace.D][cubeSize-1-i][depth].setCboxId(cell[CubeFace.F][cubeSize-1-i][depth].CboxId);
						cell[CubeFace.F][cubeSize-1-i][depth].setCboxId(cell[CubeFace.U][cubeSize-1-i][depth].CboxId);
						cell[CubeFace.U][cubeSize-1-i][depth].setCboxId(cellTmp[i][0].CboxId);
					}
					break;
				case CubeFace.U:
					// U*, U**, U***
					for(int i=0;i<cubeSize;i++){
						cellTmp[i][0]				.setColor(cell[CubeFace.L][depth][i].clr);
						cell[CubeFace.L][depth][i]	.setColor(cell[CubeFace.F][depth][i].clr);
						cell[CubeFace.F][depth][i]	.setColor(cell[CubeFace.R][depth][i].clr);
						cell[CubeFace.R][depth][i]	.setColor(cell[CubeFace.B][depth][i].clr);
						cell[CubeFace.B][depth][i]	.setColor(cellTmp[i][0].clr);
						cellTmp[i][0]				.setCboxId(cell[CubeFace.L][depth][i].CboxId);
						cell[CubeFace.L][depth][i]	.setCboxId(cell[CubeFace.F][depth][i].CboxId);
						cell[CubeFace.F][depth][i]	.setCboxId(cell[CubeFace.R][depth][i].CboxId);
						cell[CubeFace.R][depth][i]	.setCboxId(cell[CubeFace.B][depth][i].CboxId);
						cell[CubeFace.B][depth][i]	.setCboxId(cellTmp[i][0].CboxId);
					}
					break;
				case CubeFace.D:
					// D*, D**, D***
					for(int i=0;i<cubeSize;i++){
						cellTmp[i][0]							.setColor(cell[CubeFace.L][cubeSize-1-depth][i].clr);
						cell[CubeFace.L][cubeSize-1-depth][i]	.setColor(cell[CubeFace.B][cubeSize-1-depth][i].clr);
						cell[CubeFace.B][cubeSize-1-depth][i]	.setColor(cell[CubeFace.R][cubeSize-1-depth][i].clr);
						cell[CubeFace.R][cubeSize-1-depth][i]	.setColor(cell[CubeFace.F][cubeSize-1-depth][i].clr);
						cell[CubeFace.F][cubeSize-1-depth][i]	.setColor(cellTmp[i][0].clr);
						cellTmp[i][0]							.setCboxId(cell[CubeFace.L][cubeSize-1-depth][i].CboxId);
						cell[CubeFace.L][cubeSize-1-depth][i]	.setCboxId(cell[CubeFace.B][cubeSize-1-depth][i].CboxId);
						cell[CubeFace.B][cubeSize-1-depth][i]	.setCboxId(cell[CubeFace.R][cubeSize-1-depth][i].CboxId);
						cell[CubeFace.R][cubeSize-1-depth][i]	.setCboxId(cell[CubeFace.F][cubeSize-1-depth][i].CboxId);
						cell[CubeFace.F][cubeSize-1-depth][i]	.setCboxId(cellTmp[i][0].CboxId);
					}
					break;
				}
			}
			//-- if this is a face (not a slice), then rotate centers, too		
			if(depth==0) mrotateFace(cell[pFace], rcount);
		}
		return ret;
	}
	void TraslateCube(int axis, int cnt){
		int x,y;
		for (int i=0; i<cnt;i++){
			switch(axis){
			case X_AXIS: 
				//-- first, traslate F,D,B,U
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cellTmp[y][x+1].setColor( cell[CubeFace.F][y][x].clr );
						cellTmp[y][x+1].setCboxId( cell[CubeFace.F][y][x].CboxId );
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cell[CubeFace.F][y][x].setColor( cell[CubeFace.D][y][x].clr );
						cell[CubeFace.F][y][x].setCboxId( cell[CubeFace.D][y][x].CboxId );
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){ 
						cell[CubeFace.D][y][x].setColor( cell[CubeFace.B][cubeSize-1-y][cubeSize-1-x].clr );
						cell[CubeFace.D][y][x].setCboxId( cell[CubeFace.B][cubeSize-1-y][cubeSize-1-x].CboxId );
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cell[CubeFace.B][y][x].setColor( cell[CubeFace.U][cubeSize-1-y][cubeSize-1-x].clr );
						cell[CubeFace.B][y][x].setCboxId( cell[CubeFace.U][cubeSize-1-y][cubeSize-1-x].CboxId );
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cell[CubeFace.U][y][x].setColor( cellTmp[y][x+1].clr );
						cell[CubeFace.U][y][x].setCboxId( cellTmp[y][x+1].CboxId );
					}
				}			
				//-- then, rotate R (Clockwise) and L (counterclockwise)
				mrotateFace(cell[CubeFace.R], 1);
				mrotateFace(cell[CubeFace.L], 3);
				//--
				currX++;  if(currX>3) currX=0;
				Solution=Solution.concat("X");
				break;
			case Y_AXIS: 
				//-- first, traslate U,L,D,R
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cellTmp[y][x+1].setColor( cell[CubeFace.U][y][x].clr);
						cellTmp[y][x+1].setCboxId( cell[CubeFace.U][y][x].CboxId);
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cell[CubeFace.U][y][x].setColor( cell[CubeFace.L][cubeSize-1-x][y].clr);
						cell[CubeFace.U][y][x].setCboxId( cell[CubeFace.L][cubeSize-1-x][y].CboxId);
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){ 
						cell[CubeFace.L][y][x].setColor( cell[CubeFace.D][cubeSize-1-x][y].clr);
						cell[CubeFace.L][y][x].setCboxId( cell[CubeFace.D][cubeSize-1-x][y].CboxId);
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cell[CubeFace.D][y][x].setColor( cell[CubeFace.R][cubeSize-1-x][y].clr);
						cell[CubeFace.D][y][x].setCboxId( cell[CubeFace.R][cubeSize-1-x][y].CboxId);
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cell[CubeFace.R][y][x].setColor( cellTmp[cubeSize-1-x][(y+1)].clr);
						cell[CubeFace.R][y][x].setCboxId( cellTmp[cubeSize-1-x][(y+1)].CboxId);
					}
				}			
				//-- then, rotate R (Clockwise) and L (counterclockwise)
				mrotateFace(cell[CubeFace.F], 1);
				mrotateFace(cell[CubeFace.B], 3);
				//--
				currY++;  if(currY>3) currY=0;
				break;
			case Z_AXIS:
				//-- first, traslate F,R,B,L
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cellTmp[y][x+1].setColor( cell[CubeFace.F][y][x].clr);
						cellTmp[y][x+1].setCboxId( cell[CubeFace.F][y][x].CboxId);
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cell[CubeFace.F][y][x].setColor( cell[CubeFace.R][y][x].clr);
						cell[CubeFace.F][y][x].setCboxId( cell[CubeFace.R][y][x].CboxId);
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){ 
						cell[CubeFace.R][y][x].setColor( cell[CubeFace.B][y][x].clr);
						cell[CubeFace.R][y][x].setCboxId( cell[CubeFace.B][y][x].CboxId);
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cell[CubeFace.B][y][x].setColor( cell[CubeFace.L][y][x].clr);
						cell[CubeFace.B][y][x].setCboxId( cell[CubeFace.L][y][x].CboxId);
					}
				}
				for(y=0;y<cubeSize;y++){
					for(x=0;x<cubeSize;x++){
						cell[CubeFace.L][y][x].setColor( cellTmp[y][x+1].clr);
						cell[CubeFace.L][y][x].setCboxId( cellTmp[y][x+1].CboxId);
					}
				}			
				//-- then, rotate U (Clockwise) and D (counterclockwise)
				mrotateFace(cell[CubeFace.U], 1);
				mrotateFace(cell[CubeFace.D], 3);
				//--
				Solution=Solution.concat("Z");
				currZ++; if(currZ>3) currZ=0;
				break;
			}
		}
		//-- update edge[][] and corner[] from updated cell[][]
		//c2e();
	}
	void Sync2D3D(){
		//-- save current cell colors
		Ccell cellBkp[][][]=new Ccell[6][cubeSize][cubeSize];  
		for(int f=0; f<6; f++){
			for(int y=0; y<cubeSize; y++){
				for(int x=0; x<cubeSize; x++){
					cellBkp[f][y][x]=new Ccell();
					cellBkp[f][y][x].setColor(cell[f][y][x].clr);
				}
			}
		}
		
		//-- reinit cube
		initCube0();
		initCube1();
		
		//-- restore cell colors
		for(int f=0; f<6; f++){
			for(int y=0; y<cubeSize; y++){
				for(int x=0; x<cubeSize; x++){
					cell[f][y][x].setColor(cellBkp[f][y][x].clr);
				}
			}
		}
		
		//-- repaint cube
		int i=0;
		for(int f=0; f<6; f++){
			for(int y=0; y<cubeSize; y++){
				for(int x=0; x<cubeSize; x++){
					cell3d[i].setColor(cell[f][y][x].clr);
					cell[f][y][x].setCboxId(i);
					i++;
				}
			}
		}
	}
	
	static String switchMoveDir(String m){
		String ret;
		if(m.endsWith("'")){
			ret=m.substring(0,m.length()-1);
		} else{
			ret=m+"'";
		}
		return ret;
	}

	//============== END MOVES STUFF ===============================

	//============== START SOLUTION STUFF =============================	
	void Solve(){
		cEdge UedgeLoc;
		
		// General Method:
		//-- 1.  Build Centers
		//-- 2.  Build Edges
		//-- 3.  Fix Top Edges
		//-- 4.  Fix Top Corners
		//-- 5.  Fix Mid Edges
		//-- 6.  Build yellow cross
		//-- 7.  Position bottom corners		
		//-- 8.  Orient   bottom corners
		//-- 9.  Position bottom edges
		//-- 10. Orient bottom edges
		
		System.out.println("Solve started.");
		slnBox.addText("------");
		//-- get to it! (Repeat 3 times to cover for bugs!!!)

		// Only for cubeSize>3, Build Centers and Edges
		if(cubeSize>3){
			//-- 1. Build Centers
			BuildCenters();			
			//-- 2. Build Edges
			BuildEdges(MyColor.RED, MyColor.BLUE);		doMove("Z'");
			BuildEdges(MyColor.GREEN, MyColor.RED);
			//System.out.println("RED-BLUE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.BLUE) );
			//System.out.println("RED-GREEN edge isDone()="+isEdgeDone(MyColor.RED, MyColor.GREEN) );
			doMove("Z'");
			BuildEdges(MyColor.ORANGE, MyColor.GREEN);	doMove("Z'");
			//System.out.println("RED-BLUE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.BLUE) );
			//System.out.println("RED-GREEN edge isDone()="+isEdgeDone(MyColor.RED, MyColor.GREEN) );
			//System.out.println("ORANGE-GREEN edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.GREEN) );
			BuildEdges(MyColor.BLUE, MyColor.ORANGE);
			//System.out.println("RED-BLUE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.BLUE) );
			//System.out.println("RED-GREEN edge isDone()="+isEdgeDone(MyColor.RED, MyColor.GREEN) );
			//System.out.println("ORANGE-GREEN edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.GREEN) );
			//System.out.println("ORANGE-BLUE edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.BLUE) );
			doMove("X");
			BuildEdges(MyColor.YELLOW, MyColor.ORANGE);	doMove("Z'");
			//System.out.println("RED-BLUE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.BLUE) );
			//System.out.println("RED-GREEN edge isDone()="+isEdgeDone(MyColor.RED, MyColor.GREEN) );
			//System.out.println("ORANGE-GREEN edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.GREEN) );
			//System.out.println("ORANGE-BLUE edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.BLUE) );
			//System.out.println("ORANGE-YELLOW edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.YELLOW) );
			BuildEdges(MyColor.RED, MyColor.YELLOW);	doMove("Z'");
			//System.out.println("RED-BLUE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.BLUE) );
			//System.out.println("RED-GREEN edge isDone()="+isEdgeDone(MyColor.RED, MyColor.GREEN) );
			//System.out.println("ORANGE-GREEN edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.GREEN) );
			//System.out.println("ORANGE-BLUE edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.BLUE) );
			//System.out.println("ORANGE-YELLOW edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.YELLOW) );
			//System.out.println("RED-YELLOW edge isDone()="+isEdgeDone(MyColor.RED, MyColor.YELLOW) );
			BuildEdges(MyColor.WHITE, MyColor.RED);		doMove("Z'");
			//System.out.println("RED-BLUE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.BLUE) );
			//System.out.println("RED-GREEN edge isDone()="+isEdgeDone(MyColor.RED, MyColor.GREEN) );
			//System.out.println("ORANGE-GREEN edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.GREEN) );
			//System.out.println("ORANGE-BLUE edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.BLUE) );
			//System.out.println("ORANGE-YELLOW edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.YELLOW) );
			//System.out.println("RED-YELLOW edge isDone()="+isEdgeDone(MyColor.RED, MyColor.YELLOW) );
			//System.out.println("RED-WHITE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.WHITE) );
			BuildEdges(MyColor.ORANGE, MyColor.WHITE);
			//System.out.println("RED-BLUE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.BLUE) );
			//System.out.println("RED-GREEN edge isDone()="+isEdgeDone(MyColor.RED, MyColor.GREEN) );
			//System.out.println("ORANGE-GREEN edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.GREEN) );
			//System.out.println("ORANGE-BLUE edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.BLUE) );
			//System.out.println("ORANGE-YELLOW edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.YELLOW) );
			//System.out.println("RED-YELLOW edge isDone()="+isEdgeDone(MyColor.RED, MyColor.YELLOW) );
			//System.out.println("RED-WHITE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.WHITE) );
			//System.out.println("ORANGE-WHITE edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.WHITE) );
			doMove("X");
			BuildEdges(MyColor.GREEN, MyColor.WHITE);	doMove("Z'");
			//System.out.println("RED-BLUE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.BLUE) );
			//System.out.println("RED-GREEN edge isDone()="+isEdgeDone(MyColor.RED, MyColor.GREEN) );
			//System.out.println("ORANGE-GREEN edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.GREEN) );
			//System.out.println("ORANGE-BLUE edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.BLUE) );
			//System.out.println("ORANGE-YELLOW edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.YELLOW) );
			//System.out.println("RED-YELLOW edge isDone()="+isEdgeDone(MyColor.RED, MyColor.YELLOW) );
			//System.out.println("RED-WHITE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.WHITE) );
			//System.out.println("ORANGE-WHITE edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.WHITE) );
			//System.out.println("GREEN-WHITE edge isDone()="+isEdgeDone(MyColor.GREEN, MyColor.WHITE) );
			BuildEdges(MyColor.YELLOW, MyColor.GREEN);	doMove("Z'");
			//System.out.println("RED-BLUE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.BLUE) );
			//System.out.println("RED-GREEN edge isDone()="+isEdgeDone(MyColor.RED, MyColor.GREEN) );
			//System.out.println("ORANGE-GREEN edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.GREEN) );
			//System.out.println("ORANGE-BLUE edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.BLUE) );
			//System.out.println("ORANGE-YELLOW edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.YELLOW) );
			//System.out.println("RED-YELLOW edge isDone()="+isEdgeDone(MyColor.RED, MyColor.YELLOW) );
			//System.out.println("RED-WHITE edge isDone()="+isEdgeDone(MyColor.RED, MyColor.WHITE) );
			//System.out.println("ORANGE-WHITE edge isDone()="+isEdgeDone(MyColor.ORANGE, MyColor.WHITE) );
			//System.out.println("GREEN-WHITE edge isDone()="+isEdgeDone(MyColor.GREEN, MyColor.WHITE) );
			//System.out.println("GREEN-YELLOW edge isDone()="+isEdgeDone(MyColor.GREEN, MyColor.YELLOW) );
			
			BuildEdges(MyColor.BLUE, MyColor.YELLOW);	doMove("Z'");
		}
		
		//-- Traslate cube so we have RED center on F and WHITE center on U
		SetCubePos(MyColor.RED, MyColor.WHITE);
		
		//== 3. Only for cubeSize>2 , Find and fix top edges (FU, RU, BU, LU).
		if(cubeSize>2){
			for(int i=0; i<4; i++){
				UedgeLoc=FindEdge(i, MyColor.WHITE, 0); System.out.println("Face "+CubeFace.Id2Desc(i)+" ; UedgeLoc="+UedgeLoc.getDesc());
				FixTopEdge(UedgeLoc);
				doMove("Z");
			}
		}
		//-- 4. Fix Top Corners
		for(int tc=0; tc<4; tc++){
			FixTopCorner(tc);
			doMove("Z");
		}

		//-- 5. Find and fix mid edges (FL, FR, BR, BL)
		for(int i=0; i<4; i++){
			UedgeLoc=FindEdge(MidEdge_clr1[i], MidEdge_clr2[i], 0); System.out.println("MidEdge "+i+" ; UedgeLoc="+UedgeLoc.getDesc());
			FixMidEdge(UedgeLoc);
			doMove("Z");
		}

		//-- 6. Turn the cube upside-down, and build the Yellow cross
		doMove("X2");	// now we have Yellow UP, Orange Front
		FixYellowCross();

		//-- 7. Position bottom corners	
		PositionBottomCorners();

		//-- 8. Orient   bottom corners
		OrientBottomCorners();
		
		//-- 9. Position bottom edges
		PositionBottomEdges();
		
		//-- 10. Orient bottom edges
		OrientBottomEdges();
		
		//-- Finally, back to original position
		SetCubePos(MyColor.RED, MyColor.WHITE);
			
		System.out.println("Solve finished.");
	}
	
	boolean isEdgeDone(int clr1, int clr2){
		boolean ret=false;
		
		// first, find any edge with clr1/clr2 in pos 1
		for(int e=(0+1); e<(12+1); e++){
			if(edge[e].isDone() ){
				if( (edge[e].clr1==clr1 && edge[e].clr2==clr2) || (edge[e].clr1==clr2 && edge[e].clr2==clr1) ) {
					ret=true;
				}
			}
		}
		
		return ret;
	}

	void SetCubePos(int FrontColor, int UpColor){
		//-- this can only work for odd cubes
		//-- for even cubes, we consider the top-left of the 4 center cells
		int c=Math.floorDiv(cubeSize, 2);
		//-- first, traslate cube so we have Red on F
		if(cell[CubeFace.R][c][c].clr==FrontColor){
			doMove("Z");
			//TraslateCube(Z_AXIS, 1);
		} else if(cell[CubeFace.B][c][c].clr==FrontColor){
			doMove("Z2");
			//TraslateCube(Z_AXIS, 2);
		} else if(cell[CubeFace.L][c][c].clr==FrontColor){
			doMove("Z'");
			//TraslateCube(Z_AXIS, 3);
		} else if(cell[CubeFace.U][c][c].clr==FrontColor){
			doMove("X'");
			//TraslateCube(X_AXIS, 3);
		} else if(cell[CubeFace.D][c][c].clr==FrontColor){
			doMove("X");
			//TraslateCube(X_AXIS, 1);
		}
		//-- then, traslate cube so we have  White on U
		if(cell[CubeFace.R][c][c].clr==UpColor){
			doMove("Y'");
			//TraslateCube(Y_AXIS, 3);
		} else if(cell[CubeFace.D][c][c].clr==UpColor){
			doMove("Y2");
			//TraslateCube(Y_AXIS, 2);
		} else if(cell[CubeFace.L][c][c].clr==UpColor){
			doMove("Y");
			//TraslateCube(Y_AXIS, 1);
		}
	}
	
	cEdge FindEdge(int pclr1, int pclr2, int edgeid){
		// this must be run twice: once with edgeid, the other with (cubeSize-1-edgeid-1)
		cEdge ret=null;
		int e;
		for(int i=0; i<2; i++){
			e=(i>0?(cubeSize-1-edgeid-1):edgeid+1);
			//-- e0 (FU)
			if(cell[CubeFace.F][0][e].clr==pclr1 && cell[CubeFace.U][cubeSize-1][e].clr==pclr2){
				edge[cEdge.FU].setPos(e);
				ret=edge[cEdge.FU];
			}
			else if(cell[CubeFace.F][0][e].clr==pclr2 && cell[CubeFace.U][cubeSize-1][e].clr==pclr1){
				edge[cEdge.UF].setPos(e);
				ret=edge[cEdge.UF];
			}
			//-- e2 (FD)
			else if(cell[CubeFace.F][cubeSize-1][e].clr==pclr1 && cell[CubeFace.D][0][e].clr==pclr2){
				edge[cEdge.FD].setPos(e);
				ret=edge[cEdge.FD];
			}
			else if(cell[CubeFace.F][cubeSize-1][e].clr==pclr2 && cell[CubeFace.D][0][e].clr==pclr1){
				edge[cEdge.DF].setPos(e);
				ret=edge[cEdge.DF];
			}
			//-- e4 (BU)
			else if(cell[CubeFace.B][0][cubeSize-1-e].clr==pclr1 && cell[CubeFace.U][0][e].clr==pclr2){
				edge[cEdge.BU].setPos(e);
				ret=edge[cEdge.BU];
			}
			else if(cell[CubeFace.B][0][cubeSize-1-e].clr==pclr2 && cell[CubeFace.U][0][e].clr==pclr1){
				edge[cEdge.UB].setPos(e);
				ret=edge[cEdge.UB];
			}
			//-- e5 (BL)
			else if(cell[CubeFace.B][e][cubeSize-1].clr==pclr1 && cell[CubeFace.L][e][0].clr==pclr2){
				edge[cEdge.BL].setPos(e);
				ret=edge[cEdge.BL];
			}
			else if(cell[CubeFace.B][e][cubeSize-1].clr==pclr2 && cell[CubeFace.L][e][0].clr==pclr1){
				edge[cEdge.LB].setPos(e);
				ret=edge[cEdge.LB];
			}
			//-- e6 (BD)
			else if(cell[CubeFace.B][cubeSize-1][e].clr==pclr1 && cell[CubeFace.D][cubeSize-1][cubeSize-1-e].clr==pclr2){
				edge[cEdge.BD].setPos(e);
				ret=edge[cEdge.BD];
			}
			else if(cell[CubeFace.B][cubeSize-1][e].clr==pclr2 && cell[CubeFace.D][cubeSize-1][cubeSize-1-e].clr==pclr1){
				edge[cEdge.DB].setPos(e);
				ret=edge[cEdge.DB];
			}
			//-- e7 (BR)
			else if(cell[CubeFace.B][e][0].clr==pclr1 && cell[CubeFace.R][e][cubeSize-1].clr==pclr2){
				edge[cEdge.BR].setPos(e);
				ret=edge[cEdge.BR];
			}
			else if(cell[CubeFace.B][e][0].clr==pclr2 && cell[CubeFace.R][e][cubeSize-1].clr==pclr1){
				edge[cEdge.RB].setPos(e);
				ret=edge[cEdge.RB];
			}
			//-- e8 (UL)
			else if(cell[CubeFace.U][e][0].clr==pclr1 && cell[CubeFace.L][0][e].clr==pclr2){
				edge[cEdge.UL].setPos(e);
				ret=edge[cEdge.UL];
			}
			else if(cell[CubeFace.U][e][0].clr==pclr2 && cell[CubeFace.L][0][e].clr==pclr1){
				edge[cEdge.LU].setPos(e);
				ret=edge[cEdge.LU];
			}
			//-- e9 (UR)
			else if(cell[CubeFace.U][e][cubeSize-1].clr==pclr1 && cell[CubeFace.R][0][cubeSize-1-e].clr==pclr2){
				edge[cEdge.UR].setPos(e);
				ret=edge[cEdge.UR];
			}
			else if(cell[CubeFace.U][e][cubeSize-1].clr==pclr2 && cell[CubeFace.R][0][cubeSize-1-e].clr==pclr1){
				edge[cEdge.RU].setPos(e);
				ret=edge[cEdge.RU];
			}
			//-- e10 (LD
			else if(cell[CubeFace.L][cubeSize-1][e].clr==pclr1 && cell[CubeFace.D][cubeSize-1-e][0].clr==pclr2){
				edge[cEdge.LD].setPos(e);
				ret=edge[cEdge.LD];
			}
			else if(cell[CubeFace.L][cubeSize-1][e].clr==pclr2 && cell[CubeFace.D][cubeSize-1-e][0].clr==pclr1){
				edge[cEdge.DL].setPos(e);
				ret=edge[cEdge.DL];
			}
			//-- e11 (RD)
			else if(cell[CubeFace.R][cubeSize-1][e].clr==pclr1 && cell[CubeFace.D][e][cubeSize-1].clr==pclr2){
				edge[cEdge.RD].setPos(e);
				ret=edge[cEdge.RD];
			}
			else if(cell[CubeFace.R][cubeSize-1][e].clr==pclr2 && cell[CubeFace.D][e][cubeSize-1].clr==pclr1){
				edge[cEdge.DR].setPos(e);
				ret=edge[cEdge.DR];
			}
			if(ret!=null) return ret;
		}

		// FR and FL are moved here because of BE_movetoFL()
		for(int i=0; i<2; i++){
			e=(i>0?(cubeSize-1-edgeid-1):edgeid+1);

			//-- e1 (FR)
			if(cell[CubeFace.F][e][cubeSize-1].clr==pclr1 && cell[CubeFace.R][e][0].clr==pclr2		&& e>edgeid){
				edge[cEdge.FR].setPos(e);
				ret=edge[cEdge.FR];
			}
			else if(cell[CubeFace.F][e][cubeSize-1].clr==pclr2 && cell[CubeFace.R][e][0].clr==pclr1	&& e>edgeid){
				edge[cEdge.RF].setPos(e);
				ret=edge[cEdge.RF];
			}
			if(ret!=null) return ret;
		}

		for(int i=0; i<2; i++){
			e=(i>0?(cubeSize-1-edgeid-1):edgeid+1);

			//-- e3 (FL)
			if(cell[CubeFace.F][e][0].clr==pclr1 && cell[CubeFace.L][e][cubeSize-1].clr==pclr2){
				edge[cEdge.FL].setPos(e);
				ret=edge[cEdge.FL];
			}
			else if(cell[CubeFace.F][e][0].clr==pclr2 && cell[CubeFace.L][e][cubeSize-1].clr==pclr1){
				edge[cEdge.LF].setPos(e);
				ret=edge[cEdge.LF];
			}
			if(ret!=null) return ret;
		}
		return ret;
	}		
	
	void FixTopEdge(cEdge srcEdge){
		String m="";
		switch(srcEdge.id){
		case cEdge.FU: m=""; break;
		case cEdge.UF: m="FR'D'RF2"; break;
		case cEdge.FR: m="F'"; break;
		case cEdge.RF: m="R'D'RF2"; break;
		case cEdge.FD: m="F2"; break;
		case cEdge.DF: m="F'R'D'RF2"; break;
		case cEdge.FL: m="F"; break;
		case cEdge.LF: m="LDL'F2"; break;
		case cEdge.BU: m="B2D2F2"; break;
		case cEdge.UB: m="BL'DLF2"; break;
		case cEdge.BR: m="B'D2F2B"; break;
		case cEdge.RB: m="RD'R'F2"; break;
		case cEdge.BD: m="D2F2"; break;
		case cEdge.DB: m="DL'FL"; break;
		case cEdge.BL: m="BD2B'F2"; break;
		case cEdge.LB: m="L'DLF2"; break;
		case cEdge.UL: m="LFL'"; break;
		case cEdge.LU: m="L2DF2"; break;
		case cEdge.UR: m="R'F'"; break;
		case cEdge.RU: m="R2D'F2"; break;
		case cEdge.RD: m="D'F2"; break;
		case cEdge.DR: m="R'B'D2BRF2"; break;
		case cEdge.LD: m="DF2"; break;
		case cEdge.DL: m="LBD2B'L'F2"; break;
		}
		doMove(m);
	}
	void FixTopCorner(int c){
		//--- 4.2.1 need RWB/WBR/BRW
		// There are 3 possible moves , according to c0 orientation

		/* sought corner is in 0*/
		if(cell[CubeFace.F][0][0].clr				==TopCorner_clr1[c] && cell[CubeFace.L][0][cubeSize-1].clr==TopCorner_clr2[c] && cell[CubeFace.U][cubeSize-1][0].clr==TopCorner_clr3[c] /*orientation 1*/){
			doMove("LDL'FDF'");
		} else if(cell[CubeFace.F][0][0].clr		==TopCorner_clr2[c] && cell[CubeFace.L][0][cubeSize-1].clr==TopCorner_clr3[c] && cell[CubeFace.U][cubeSize-1][0].clr==TopCorner_clr1[c] /*orientation 2*/){
			doMove("L2D2FDF'DL2");
		} else if(		cell[CubeFace.F][0][0].clr	==TopCorner_clr3[c] && cell[CubeFace.L][0][cubeSize-1].clr==TopCorner_clr1[c] && cell[CubeFace.U][cubeSize-1][0].clr==TopCorner_clr2[c] /*orientation 3*/){
			doMove("F'D2FD'FDF'");

		/* sought corner is in 1*/
		} else if(cell[CubeFace.F][0][cubeSize-1].clr	==TopCorner_clr1[c] && cell[CubeFace.U][cubeSize-1][cubeSize-1].clr==TopCorner_clr2[c] && cell[CubeFace.R][0][0].clr==TopCorner_clr3[c] /*orientation 1*/){
			// this is where it should be. do nothing
		} else if(cell[CubeFace.F][0][cubeSize-1].clr	==TopCorner_clr2[c] && cell[CubeFace.U][cubeSize-1][cubeSize-1].clr==TopCorner_clr3[c] && cell[CubeFace.R][0][0].clr==TopCorner_clr1[c] /*orientation 2*/){
			doMove("FDF'D'FDF'");
		} else if(cell[CubeFace.F][0][cubeSize-1].clr	==TopCorner_clr3[c] && cell[CubeFace.U][cubeSize-1][cubeSize-1].clr==TopCorner_clr1[c] && cell[CubeFace.R][0][0].clr==TopCorner_clr2[c] /*orientation 3*/){
			doMove("R'D'RDR'D'R");
		
		/* sought corner is in 2*/			
		} else if(cell[CubeFace.F][cubeSize-1][cubeSize-1].clr==TopCorner_clr1[c] && cell[CubeFace.R][cubeSize-1][0].clr==TopCorner_clr2[c] && cell[CubeFace.D][0][cubeSize-1].clr==TopCorner_clr3[c] /*orientation 1*/){
			doMove("R'D2RDFDF'");
		} else if(cell[CubeFace.F][cubeSize-1][cubeSize-1].clr==TopCorner_clr2[c] && cell[CubeFace.R][cubeSize-1][0].clr==TopCorner_clr3[c] && cell[CubeFace.D][0][cubeSize-1].clr==TopCorner_clr1[c] /*orientation 2*/){
			doMove("FDF'");
		} else if(cell[CubeFace.F][cubeSize-1][cubeSize-1].clr==TopCorner_clr3[c] && cell[CubeFace.R][cubeSize-1][0].clr==TopCorner_clr1[c] && cell[CubeFace.D][0][cubeSize-1].clr==TopCorner_clr2[c] /*orientation 3*/){
			doMove("R'D2RDR'D'R");

		/* sought corner is in 3*/			
		} else if(cell[CubeFace.F][cubeSize-1][0].clr==TopCorner_clr1[c] && cell[CubeFace.D][0][0].clr==TopCorner_clr2[c] && cell[CubeFace.L][cubeSize-1][cubeSize-1].clr==TopCorner_clr3[c] /*orientation 1*/){
			doMove("DR'D2RDR'D'R");
		} else if(cell[CubeFace.F][cubeSize-1][0].clr==TopCorner_clr2[c] && cell[CubeFace.D][0][0].clr==TopCorner_clr3[c] && cell[CubeFace.L][cubeSize-1][cubeSize-1].clr==TopCorner_clr1[c] /*orientation 2*/){
			doMove("DR'D'R");
		} else if(cell[CubeFace.F][cubeSize-1][0].clr==TopCorner_clr3[c] && cell[CubeFace.D][0][0].clr==TopCorner_clr1[c] && cell[CubeFace.L][cubeSize-1][cubeSize-1].clr==TopCorner_clr2[c] /*orientation 3*/){
			doMove("DFDF'");

		/* sought corner is in 4*/			
		} else if(cell[CubeFace.B][0][0].clr==TopCorner_clr1[c] && cell[CubeFace.R][0][cubeSize-1].clr==TopCorner_clr2[c] && cell[CubeFace.U][0][cubeSize-1].clr==TopCorner_clr3[c] /*orientation 1*/){
			doMove("RD'FDF'R'FDF'");
		} else if(cell[CubeFace.B][0][0].clr==TopCorner_clr2[c] && cell[CubeFace.R][0][cubeSize-1].clr==TopCorner_clr3[c] && cell[CubeFace.U][0][cubeSize-1].clr==TopCorner_clr1[c] /*orientation 2*/){
			doMove("B'D'BR'D'R");
		} else if(cell[CubeFace.B][0][0].clr==TopCorner_clr3[c] && cell[CubeFace.R][0][cubeSize-1].clr==TopCorner_clr1[c] && cell[CubeFace.U][0][cubeSize-1].clr==TopCorner_clr2[c] /*orientation 3*/){
			doMove("B'D'BFDF'");

		/* sought corner is in 5*/			
		} else if(cell[CubeFace.B][0][cubeSize-1].clr==TopCorner_clr1[c] && cell[CubeFace.U][0][0].clr==TopCorner_clr2[c] && cell[CubeFace.L][0][0].clr==TopCorner_clr3[c] /*orientation 1*/){
			doMove("BD2B'R'D'R");
		} else if(cell[CubeFace.B][0][cubeSize-1].clr==TopCorner_clr2[c] && cell[CubeFace.U][0][0].clr==TopCorner_clr3[c] && cell[CubeFace.L][0][0].clr==TopCorner_clr1[c] /*orientation 2*/){
			doMove("BD2B'FDF'");
		} else if(cell[CubeFace.B][0][cubeSize-1].clr==TopCorner_clr3[c] && cell[CubeFace.U][0][0].clr==TopCorner_clr1[c] && cell[CubeFace.L][0][0].clr==TopCorner_clr2[c] /*orientation 3*/){
			doMove("L'D2LR'D'R");

		/* sought corner is in 6*/			
		} else if(cell[CubeFace.B][cubeSize-1][cubeSize-1].clr==TopCorner_clr1[c] && cell[CubeFace.L][cubeSize-1][0].clr==TopCorner_clr2[c] && cell[CubeFace.D][cubeSize-1][0].clr==TopCorner_clr3[c] /*orientation 1*/){
			doMove("D2R'D'R");
		} else if(cell[CubeFace.B][cubeSize-1][cubeSize-1].clr==TopCorner_clr2[c] && cell[CubeFace.L][cubeSize-1][0].clr==TopCorner_clr3[c] && cell[CubeFace.D][cubeSize-1][0].clr==TopCorner_clr1[c] /*orientation 2*/){
			doMove("D2FDF'");
		} else if(cell[CubeFace.B][cubeSize-1][cubeSize-1].clr==TopCorner_clr3[c] && cell[CubeFace.L][cubeSize-1][0].clr==TopCorner_clr1[c] && cell[CubeFace.D][cubeSize-1][0].clr==TopCorner_clr2[c] /*orientation 3*/){
			doMove("D2R'D2RDR'D'R");

		/* sought corner is in 7*/			
		} else if(cell[CubeFace.B][cubeSize-1][0].clr==TopCorner_clr1[c] && cell[CubeFace.D][cubeSize-1][cubeSize-1].clr==TopCorner_clr2[c] && cell[CubeFace.R][cubeSize-1][cubeSize-1].clr==TopCorner_clr3[c] /*orientation 1*/){
			//doMove("B'D2BR'D'R");
			doMove("D'FD2F'D'FDF'");
		} else if(cell[CubeFace.B][cubeSize-1][0].clr==TopCorner_clr2[c] && cell[CubeFace.D][cubeSize-1][cubeSize-1].clr==TopCorner_clr3[c] && cell[CubeFace.R][cubeSize-1][cubeSize-1].clr==TopCorner_clr1[c] /*orientation 1*/){
			doMove("D'R'D'R");
		} else if(cell[CubeFace.B][cubeSize-1][0].clr==TopCorner_clr3[c] && cell[CubeFace.D][cubeSize-1][cubeSize-1].clr==TopCorner_clr1[c] && cell[CubeFace.R][cubeSize-1][cubeSize-1].clr==TopCorner_clr2[c] /*orientation 1*/){
			doMove("D'FDF'");
		}
		
	}
	void FixMidEdge(cEdge srcEdge){
		//-- destination is always FL
		String baseMove="DLD'L'D'F'DF";
		String m="";
		switch(srcEdge.id){
		case (cEdge.FR):
			m="Z".concat(baseMove).concat("ZZZD").concat(baseMove);
			break;
		case (cEdge.RF):
			m="Z".concat(baseMove).concat("ZZZD").concat(baseMove).concat(baseMove).concat("D2").concat(baseMove);
			break;
		case (cEdge.BR): 
			m="ZZ".concat(baseMove).concat("Z2").concat(baseMove).concat(baseMove).concat("D2").concat(baseMove);
			break;
		case (cEdge.RB): 
			m="ZZ".concat(baseMove).concat("ZZ").concat(baseMove);
			break;
		case (cEdge.BL): 
			m="ZZZ".concat(baseMove).concat("Z").concat(baseMove).concat(baseMove);
			break;
		case (cEdge.LB): 
			m="ZZZ".concat(baseMove).concat("Z").concat(baseMove).concat(baseMove).concat(baseMove).concat("D'").concat(baseMove).concat(baseMove);
			break;
		case (cEdge.FL): 
			m="";
			break;
		case (cEdge.LF): 
			m=baseMove.concat("D'").concat(baseMove).concat(baseMove);
			break;
		case (cEdge.FD):
			m=baseMove;
			break;
		case (cEdge.DF):
			m=baseMove.concat(baseMove).concat("D2").concat(baseMove);
			break;
		case (cEdge.LD):
			m="D".concat(baseMove);
			break;
		case (cEdge.DL):
			m="D".concat(baseMove).concat(baseMove).concat("D2").concat(baseMove);
			break;
		case (cEdge.BD):
			m="D2".concat(baseMove);
			break;
		case (cEdge.DB):
			m="D2".concat(baseMove).concat(baseMove).concat("D2").concat(baseMove);
			break;
		case (cEdge.RD):
			m="D'".concat(baseMove);
			break;
		case (cEdge.DR):
			m="D'".concat(baseMove).concat(baseMove).concat("D2").concat(baseMove);
			break;
		case 99: m=""; break;
		}
		doMove(m);				
	}
	void FixYellowCross(){
		String b="FRUR'U'F'";	// Leaves W unchanged ; N->S(+flip) ; S->E(+flip) ; E->N
		//String bi="FURU'R'F'";	// inverse of b;
		// so, basically, N and S get flipped
		String m="";

		//			for(int i=1; i<cubeDepth; i++) m=m+BE_getInvertEdgeMove(i);

		
		// Check N,W,S,E
		if(			cell[CubeFace.U][0][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][0].clr!=MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr!=MyColor.YELLOW){
			// 0000
			m=b+b+"Z"+b;
		} else if(	cell[CubeFace.U][0][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][0].clr!=MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr==MyColor.YELLOW){
			// 0001
			m="Z";
			for(int i=1; i<cubeDepth; i++) m=m+BE_getInvertEdgeMove(i);
			m=m+b+b+"Z"+b;
		} else if(	cell[CubeFace.U][0][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][0].clr!=MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr!=MyColor.YELLOW){
			// 0010
			m="";
			for(int i=1; i<cubeDepth; i++) m=m+BE_getInvertEdgeMove(i);
			m=m+b+b+"Z"+b;
		} else if(	cell[CubeFace.U][0][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][0].clr!=MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr==MyColor.YELLOW){
			// 0011
			m=b+"Z"+b;
		} else if(	cell[CubeFace.U][0][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][0].clr==MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr!=MyColor.YELLOW){
			// 0100
			m="Z'";
			for(int i=1; i<cubeDepth; i++) m=m+BE_getInvertEdgeMove(i);
			m=m+b+b+"Z"+b;
		} else if(	cell[CubeFace.U][0][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][0].clr==MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr==MyColor.YELLOW){
			// 0101
			m=b;
		} else if(	cell[CubeFace.U][0][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][0].clr==MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr!=MyColor.YELLOW){
			// 0110
			m="Z'"+b+"Z"+b;
		} else if(	cell[CubeFace.U][0][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][0].clr==MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr==MyColor.YELLOW){
			// 0111
			m="Z2";
			for(int i=1; i<cubeDepth; i++) m=m+BE_getInvertEdgeMove(i);
		} else if(	cell[CubeFace.U][0][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][0].clr!=MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr!=MyColor.YELLOW){
			// 1000
			m="Z2";
			for(int i=1; i<cubeDepth; i++) m=m+BE_getInvertEdgeMove(i);
			m=m+b+b+"Z"+b;
		} else if(	cell[CubeFace.U][0][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][0].clr!=MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr==MyColor.YELLOW){
			// 1001
			m="Z"+b+"Z"+b;
		} else if(	cell[CubeFace.U][0][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][0].clr!=MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr!=MyColor.YELLOW){
			// 1010
			m="Z"+b;
		} else if(	cell[CubeFace.U][0][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][0].clr!=MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr==MyColor.YELLOW){
			// 1011
			m="Z'";
			for(int i=1; i<cubeDepth; i++) m=m+BE_getInvertEdgeMove(i);
		} else if(	cell[CubeFace.U][0][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][0].clr==MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr!=MyColor.YELLOW){
			// 1100
			m="Z2"+b+"Z"+b;
		} else if(	cell[CubeFace.U][0][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][0].clr==MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr!=MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr==MyColor.YELLOW){
			// 1101
			m="";
			for(int i=1; i<cubeDepth; i++) m=m+BE_getInvertEdgeMove(i);			
		} else if(	cell[CubeFace.U][0][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][0].clr==MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr!=MyColor.YELLOW){
			// 1110
			m="Z";
			for(int i=1; i<cubeDepth; i++) m=m+BE_getInvertEdgeMove(i);
		} else if(	cell[CubeFace.U][0][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][0].clr==MyColor.YELLOW && cell[CubeFace.U][cubeSize-1][1].clr==MyColor.YELLOW && cell[CubeFace.U][1][cubeSize-1].clr==MyColor.YELLOW){
			// 1111
			// do nothing
		}
		
		doMove(m);
	}
	void LocateBottomCorners(){
		//-- start with RGY (should be in top-left)
		for(int i=0;i<4;i++){
			if	(
					cell[CubeFace.U][0][0].clr==BottomCorner_clr1[i] && cell[CubeFace.L][0][0].clr==BottomCorner_clr2[i] && cell[CubeFace.B][0][cubeSize-1].clr==BottomCorner_clr3[i] ||
					cell[CubeFace.U][0][0].clr==BottomCorner_clr2[i] && cell[CubeFace.L][0][0].clr==BottomCorner_clr3[i] && cell[CubeFace.B][0][cubeSize-1].clr==BottomCorner_clr1[i] ||
					cell[CubeFace.U][0][0].clr==BottomCorner_clr3[i] && cell[CubeFace.L][0][0].clr==BottomCorner_clr1[i] && cell[CubeFace.B][0][cubeSize-1].clr==BottomCorner_clr2[i]
				){
				bcPos[i]=0;
			} else if(
					cell[CubeFace.U][0][cubeSize-1].clr==BottomCorner_clr1[i] && cell[CubeFace.B][0][0].clr==BottomCorner_clr2[i] && cell[CubeFace.R][0][cubeSize-1].clr==BottomCorner_clr3[i] ||
					cell[CubeFace.U][0][cubeSize-1].clr==BottomCorner_clr2[i] && cell[CubeFace.B][0][0].clr==BottomCorner_clr3[i] && cell[CubeFace.R][0][cubeSize-1].clr==BottomCorner_clr1[i] ||
					cell[CubeFace.U][0][cubeSize-1].clr==BottomCorner_clr3[i] && cell[CubeFace.B][0][0].clr==BottomCorner_clr1[i] && cell[CubeFace.R][0][cubeSize-1].clr==BottomCorner_clr2[i]
					){
				bcPos[i]=1;
			} else if(
					cell[CubeFace.U][cubeSize-1][cubeSize-1].clr==BottomCorner_clr1[i] && cell[CubeFace.R][0][0].clr==BottomCorner_clr2[i] && cell[CubeFace.F][0][cubeSize-1].clr==BottomCorner_clr3[i] ||
					cell[CubeFace.U][cubeSize-1][cubeSize-1].clr==BottomCorner_clr2[i] && cell[CubeFace.R][0][0].clr==BottomCorner_clr3[i] && cell[CubeFace.F][0][cubeSize-1].clr==BottomCorner_clr1[i] ||
					cell[CubeFace.U][cubeSize-1][cubeSize-1].clr==BottomCorner_clr3[i] && cell[CubeFace.R][0][0].clr==BottomCorner_clr1[i] && cell[CubeFace.F][0][cubeSize-1].clr==BottomCorner_clr2[i]
					){
				bcPos[i]=2;							
			} else{
				bcPos[i]=3;
			}
			System.out.println("bcPos["+i+"]="+bcPos[i]);
		}
	}
	void PositionBottomCorners(){
		String baseMove="LU'R'UL'U'RU2";
		// Start by making sure we have Yellow in UP, Orange in FRONT
		SetCubePos(MyColor.ORANGE, MyColor.YELLOW);

		//-- first, Locate Bottom corners. This sets global bcPos[]
		LocateBottomCorners();	
		//-- now, align bcPos[0] with top-left corner, by rotating U
		for(int i=0; i<bcPos[0]; i++) doMove("U'");
		//-- then, update bcPos[] by re-locating bottom corners:
		LocateBottomCorners();

		//-- then, it's just one out of six possible moves ( 0123, 0132, 0213, 0231, 0312, 0321 )
		if(			bcPos[0]==0 && bcPos[1]==1 && bcPos[2]==2 && bcPos[3]==3){
			//-- 0123: do nothing.
		} else if(	bcPos[0]==0 && bcPos[1]==1 && bcPos[2]==3 && bcPos[3]==2){
			//-- 0132
			doMove("Z'"); 
			doMove(baseMove); 
			doMove("Z");
		} else if(	bcPos[0]==0 && bcPos[1]==2 && bcPos[2]==1 && bcPos[3]==3){
			// 0213: switch right side
			doMove(baseMove);
		} else if(	bcPos[0]==0 && bcPos[1]==2 && bcPos[2]==3 && bcPos[3]==1){
			// 0231: 
			//doMove("Z2"); 
			doMove(baseMove); 
			doMove("Z'");
			doMove(baseMove); 
		} else if(	bcPos[0]==0 && bcPos[1]==3 && bcPos[2]==1 && bcPos[3]==2){
			// 0312:
			doMove("Z'");
			doMove(baseMove); 
			doMove("Z"); 
			doMove(baseMove); 
		} else if(	bcPos[0]==0 && bcPos[1]==3 && bcPos[2]==2 && bcPos[3]==1){
			// 0321: 
			doMove(baseMove); 
			doMove("Z2"); 
			doMove(baseMove); 
			doMove("U");
		} else{
			System.out.println("PositionBottomCorners();: Unhandled case!");
			//-- ALL OTHER CASES !!!
		}
	}

	void OrientBottomCorners(){
		String b="RUR'URU2R'U2";
		int i;
		boolean step1done=false;
		
		//-- 1. First, we need to end up with only one yellow in 0
		while(!OBC_isDone()){
			
			// special case: only one yellow in (3), and none of the others are Next()	=> basemove, then go on with standard routine
			if(OBC_isDone(3) && !OBC_isNext(0) && !OBC_isNext(1) && !OBC_isNext(2)) doMove(b);

			// if we already have an existing unique yellow, we can skip the following
			if(!(OBC_isDone(0) && !OBC_isDone(1) && !OBC_isDone(2) && !OBC_isDone(3))){
				i=0;			
				while(i<4 && !(OBC_isNext(0) && !OBC_isNext(1) && !OBC_isNext(2) && !OBC_isDone(3))){
					doMove("Z");
					i++;
				}
			}
			if(step1done) break;
			
			if((OBC_isNext(0) && !OBC_isNext(1) && !OBC_isNext(2) && !OBC_isDone(3))){
				step1done=true;
			} else{
				doMove(b);					// just to break the deadlock(?)
				if(OBC_isDone()) break;		// because it may happen that b used to break the deadlock accidentally solves it!
			}
		
			// then move
			doMove(b);
	
			// 2 now, the final move. The final move and starting point for the yellow cube depend on where the yellow in 1 is
			if(cell[CubeFace.B][0][0].clr==MyColor.YELLOW){
				doMove("R'U'RU'R'U2RU2");
			}else{
				doMove("Z'"+b);
			}
		}
		
	}
	boolean OBC_isDone(){
		boolean ret=true;
		for(int i=0; i<4; i++){
			if(!OBC_isDone(i)) ret=false;
		}
		return ret;
	}
	boolean OBC_isDone(int c){
		boolean ret=false;
		switch (c){
		case 0:
			ret=(cell[CubeFace.U][0][0].clr==MyColor.YELLOW); break;	
		case 1:
			ret=(cell[CubeFace.U][0][cubeSize-1].clr==MyColor.YELLOW); break;
		case 2:
			ret=(cell[CubeFace.U][cubeSize-1][cubeSize-1].clr==MyColor.YELLOW); break;
		case 3:
			ret=(cell[CubeFace.U][cubeSize-1][0].clr==MyColor.YELLOW); break;
		}
		return ret;
	}
	boolean OBC_isNext(int c){
		boolean ret=false;
		switch (c){
		case 0:
			ret=(cell[CubeFace.B][0][cubeSize-1].clr==MyColor.YELLOW); break;	
		case 1:
			ret=(cell[CubeFace.R][0][cubeSize-1].clr==MyColor.YELLOW); break;
		case 2:
			ret=(cell[CubeFace.F][0][cubeSize-1].clr==MyColor.YELLOW); break;
		case 3:
			ret=false; break;
		}
		return ret;
	}

	void PositionBottomEdges(){

		String mbase="R2UFB'R2F'BUR2";
		String minv ="R2U'B'FR2BF'U'R2";
		String mSwitchFB=PBE_getSwitchFBmove();
		String mSwitchFR=minv+mSwitchFB;
		
		int i=0;
		while(i<4 && PBE_countDone()<4){
			doMove(mbase);
			if(PBE_CheckAdjacentEdges()){
				doMove(mSwitchFR);
				return;
			}
			if(PBE_CheckOppositeEdges()){
				doMove(mSwitchFB);
				return;
			}
			if( PBE_setLdone() ){
				doMove(mbase);
				if(PBE_countDone()<4) doMove(mbase);
			}
			doMove("Z");
		}

		
	}
	boolean PBE_CheckAdjacentEdges(){
		int i=0;
		while(i<4 && !(PBE_isLdone() && PBE_isBdone() && !PBE_isRdone() && !PBE_isFdone())){
			doMove("Z");
			i++;
		}
		return(PBE_isLdone() && PBE_isBdone() && !PBE_isRdone() && !PBE_isFdone());
	}
	boolean PBE_CheckOppositeEdges(){
		int i=0;
		while(i<4 && !(PBE_isLdone() && !PBE_isBdone() && PBE_isRdone() && !PBE_isFdone())){
			doMove("Z");
			i++;
		}
		return(!PBE_isLdone() && PBE_isBdone() && !PBE_isRdone() && PBE_isFdone());
	}
	boolean PBE_setLdone(){
		int i=0;
		while(i<4 && !PBE_isLdone()){
			doMove("Z");
			i++;
		}
		return(PBE_isLdone());
	}
	String PBE_getSwitchFBmove(){
		String ret="";
		for(int x=cubeSize-2; x>=cubeDepth; x--){
			ret=ret+x2Slice(x, false)+"2";
		}
		ret=ret+"U2";
		for(int x=cubeSize-2; x>=cubeDepth; x--) ret=ret+x2Slice(x, false)+"2";
		ret=ret+"U2";

		for(int y=cubeSize-2; y>=cubeDepth; y--) ret=ret+y2Slice(y, false)+"2";

		for(int x=cubeSize-2; x>=cubeDepth; x--) ret=ret+x2Slice(x, false)+"2";
		for(int y=cubeSize-2; y>=cubeDepth; y--) ret=ret+y2Slice(y, false)+"2";
		
		
		//="R*2U2R*2U2 U*2R*2U*2";
		
		return ret;
	}
	boolean PBE_isFdone(){
		return( (cell[CubeFace.F][0][1].clr==cell[CubeFace.F][1][1].clr) || (cell[CubeFace.U][cubeSize-1][1].clr==cell[CubeFace.F][1][1].clr));
	}
	boolean PBE_isRdone(){
		return( (cell[CubeFace.R][0][1].clr==cell[CubeFace.R][1][1].clr) || (cell[CubeFace.U][1][cubeSize-1].clr==cell[CubeFace.R][1][1].clr) );
	}
	boolean PBE_isBdone(){
		return( (cell[CubeFace.B][0][1].clr==cell[CubeFace.B][1][1].clr) || (cell[CubeFace.U][0][1].clr==cell[CubeFace.B][1][1].clr) );
	}
	boolean PBE_isLdone(){
		return( (cell[CubeFace.L][0][1].clr==cell[CubeFace.L][1][1].clr) || (cell[CubeFace.U][1][0].clr==cell[CubeFace.L][1][1].clr) );
	}
	int PBE_countDone(){
		int ret=0;
		if(PBE_isFdone()) ret++;
		if(PBE_isRdone()) ret++;
		if(PBE_isBdone()) ret++;
		if(PBE_isLdone()) ret++;
		return ret;
	}
	
	void OrientBottomEdges(){
		String m, sl, sr; // left and right slices	
		// R*2B2U2L*U2R*'U2R*U2F2R*F2L*'B2R*2
		
		for(int e=0; e<4; e++){			
			for(int s=1; s<cubeDepth; s++){
				sl=x2Slice(s, false);
				sr=x2Slice(cubeSize-1-s, false);
				m=sr+"2B2U2"+sl+"U2"+sr+"'U2"+sr+"U2F2"+sr+"F2"+sl+"'B2"+sr+"2";
				if(cell[CubeFace.F][0][0].clr!=cell[CubeFace.F][0][1].clr) doMove(m);
				doMove("Z");
			}
		}
	}

	void BuildEdges(int clrF, int clrR){
		ArrayList<cEdge> UndoneEdges=BE_UndoneEdgesCount();
		int clrL=cell[CubeFace.L][1][1].clr;
		
		if(			UndoneEdges.size()==0){
			return; 
		} else if(	UndoneEdges.size()==1){
			//-- first, move it to FU
			String m=UndoneEdges.get(0).toFLmove(1)+"F";
			doMove(m);
			//-- then fix it
			BE_OrientLastEdge();
			return; 
		} else{		
			for(int y=1; y<(cubeSize-1); y++){
				// do the whole thing unless the edge is already in FR, and properly oriented
				if(!(cell[CubeFace.F][y][cubeSize-1].clr==clrF && cell[CubeFace.R][y][0].clr==clrR)){
	
					// make sure FL is not done
					if(BE_freeFL()){
						while (!(cell[CubeFace.F][y][0].clr==clrR && cell[CubeFace.L][y][cubeSize-1].clr==clrF)){
							// 1. look for edge to be put in (F,y,cubeSize-1) , and move it to (F,y,0)
							BE_movetoFL(clrF, clrR, y);
						}
						// 2. before doing the move, make sure top edge is not complete!
						if(BE_freeFU() && BE_freeFR()){
							// 3. do the standard edge move on(F,y,0);
							doMove( BE_getStdEdgeMove(y) );
						} else{
							// all the edges except F and R are complete. need last-edge move
							BE_FixLast2Edges(clrF, clrR, clrL);
							break;
						}
					} else{
						// all the edges except F and R are complete. need last-edge move
						BE_FixLast2Edges(clrF, clrR, clrL);
					}
				}
			}
		}
	}

	ArrayList<cEdge> BE_UndoneEdgesCount(){
		ArrayList<cEdge> ret=new ArrayList<cEdge>();
		
		for(int e=1; e<=12; e++){
			if(!edge[e].isDone()) ret.add(edge[e]);
		}
		return ret;
	}
	void	BE_movetoFL(int clr1, int clr2, int y){
		cEdge e;
		String m;
		int my;
		
		e=FindEdge(clr1,clr2,y-1);
		// if edge in FR, make sure we move the right cell
		if(e.id==cEdge.FR||e.id==cEdge.RF){
			my=e.pos;	// this way, toFLmove extracts the correct cell from FR
		} else{
			my=y;
		}
		m=e.toFLmove(my);
		if(m.length()>5){
			if(!BE_freeFU()){
				m=BE_getLastEdgeMove(y);	// last-edge move
			}
		}
		doMove(m);

		// need to invert L ?
		if( cell[CubeFace.F][cubeSize-1-y][0].clr==clr1 && cell[CubeFace.L][cubeSize-1-y][cubeSize-1].clr==clr2 ) doMove(cEdge.mInvertFL);
	}
	boolean BE_freeFU(){

		boolean done=BE_isFUDone();
		
		if(done){ doMove("U");		done=BE_isFUDone(); }
		if(done){ doMove("U");		done=BE_isFUDone(); }
		if(done){ doMove("U");		done=BE_isFUDone(); }
		if(done){ doMove("BU2");	done=BE_isFUDone(); }	
		if(done){ doMove("BU2");	done=BE_isFUDone(); }	
		if(done){ doMove("BU2");	done=BE_isFUDone(); }	
		if(done){ doMove("DB2U2");	done=BE_isFUDone(); }	
		if(done){ doMove("DB2U2");	done=BE_isFUDone(); }	
		if(done){ doMove("DB2U2");	done=BE_isFUDone(); }	
		
		return(!done);
	}
	boolean BE_freeFR(){

		boolean done=BE_isFRDone();
		
		if(done){ doMove("R");		done=BE_isFRDone(); }
		if(done){ doMove("R");		done=BE_isFRDone(); }
		if(done){ doMove("R");		done=BE_isFRDone(); }
		if(done){ doMove("DR");		done=BE_isFRDone(); }	
		if(done){ doMove("D2R");	done=BE_isFRDone(); }	
		if(done){ doMove("D'R");	done=BE_isFRDone(); }	
		if(done){ doMove("UR'U'");	done=BE_isFRDone(); }	
		if(done){ doMove("U2R'U2");	done=BE_isFRDone(); }	
		if(done){ doMove("B2R2");	done=BE_isFRDone(); }	
		
		return(!done);
	}
	boolean BE_freeFL(){

		boolean done=BE_isFLDone();
		
		if(done){ doMove("L");		done=BE_isFLDone(); }
		if(done){ doMove("L");		done=BE_isFLDone(); }
		if(done){ doMove("L");		done=BE_isFLDone(); }
		if(done){ doMove("DL");		done=BE_isFLDone(); }	
		if(done){ doMove("D2L");	done=BE_isFLDone(); }	
		if(done){ doMove("D'L");	done=BE_isFLDone(); }	
		if(done){ doMove("U'LU");	done=BE_isFLDone(); }	
		if(done){ doMove("U2LU2");	done=BE_isFLDone(); }	
		if(done){ doMove("B2L2");	done=BE_isFLDone(); }	
		
		return(!done);
	}
	boolean BE_isFUDone(){
		boolean ret=true;
		
		for(int x=2; x<(cubeSize-1); x++){
			if(cell[CubeFace.F][0][x].clr!=cell[CubeFace.F][0][x-1].clr) ret=false;
			if(cell[CubeFace.U][cubeSize-1][x].clr!=cell[CubeFace.U][cubeSize-1][x-1].clr) ret=false;
			if(!ret) break;
		}
		
		return ret;
	}
	boolean BE_isFRDone(){
		boolean ret=true;
		
		for(int x=2; x<(cubeSize-1); x++){
			if(cell[CubeFace.F][x][cubeSize-1].clr!=cell[CubeFace.F][x-1][cubeSize-1].clr) ret=false;
			if(cell[CubeFace.R][x][0].clr!=cell[CubeFace.F][x-1][cubeSize-1].clr) ret=false;
			if(!ret) break;
		}
		
		return ret;
	}
	boolean BE_isFLDone(){
		boolean ret=true;
		
		for(int x=2; x<(cubeSize-1); x++){
			if(cell[CubeFace.F][x][0].clr!=cell[CubeFace.F][x-1][0].clr) ret=false;
			if(cell[CubeFace.L][x][cubeSize-1].clr!=cell[CubeFace.L][x-1][cubeSize-1].clr) ret=false;
			if(!ret) break;
		}
		
		return ret;
	}
	void 	BE_FixLast2Edges(int clrF, int clrR, int clrL){

		// the (cubeDepth) cubelet determines the color of the whole FL edge (so we make sure not to get inverted central edges on odd cubes)
		//clrL= (cell[CubeFace.F][1][0].clr==clrL || cell[CubeFace.L][1][cubeSize-1].clr==clrL)?clrL:clrR;
		clrL= (cell[CubeFace.F][cubeDepth][0].clr==clrL || cell[CubeFace.L][cubeDepth][cubeSize-1].clr==clrL)?clrL:clrR;
		
		// make sure each cubelet on FL ends up with the same color as the first 
		for(int yl=1; yl<(cubeSize-1); yl++){
			if(!(cell[CubeFace.L][yl][cubeSize-1].clr==clrL || cell[CubeFace.F][yl][0].clr==clrL )){
				// here we need to move cell from FR to FL[yl]. First, we need to verify that cell is in FR at the expected pos
				// If it's not, we need to invert FR first
				if(!((cell[CubeFace.F][cubeSize-1-yl][cubeSize-1].clr==clrL || cell[CubeFace.R][cubeSize-1-yl][0].clr==clrL))){
					doMove(cEdge.mInvertFR);
				}
				// If Invert didn't do it, then cell may be in FL, so we need do do move+invert, instead
				if(!((cell[CubeFace.F][cubeSize-1-yl][cubeSize-1].clr==clrL || cell[CubeFace.R][cubeSize-1-yl][0].clr==clrL))){
					System.out.println("BE_FixLast2Edges(): KAZ !!!!!");
				}
				// finally, the actual move
				doMove(BE_getLastEdgeMove(yl));
			}
		}
		
		// for odd-sized cubes, we may have to swap the two edges' central cubelets
		//if(isOddSize && cell[CubeFace.F][cubeDepth][0].clr!=cell[CubeFace.F][cubeDepth-1][0].clr){
		//	doMove("D2D*2R2 D'D*'F2 D'D*'F2 R2D'D*' R2DD* R2D'D*' R2D2D*2 R2F2");
		//}
		
		// Finally, for each of the two edges, check that all cubes are properly oriented
		//clrL=(isOddSize)?cell[CubeFace.F][0][cubeDepth].clr:cell[CubeFace.F][0][1].clr;
		doMove("F");	// simply because I've learnt this move on FU edge
		BE_OrientLastEdge();
		doMove("F2");	// simply because I've learnt this move on FU edge
		BE_OrientLastEdge();
	}
	void 	BE_OrientLastEdge(){
		// if oddSize, we must take the central edge as reference color, as it cannot be flipped; if not, we take edge 1
		int clrref=(isOddSize)?cubeDepth:1;	
		
		for(int i=1; i<(cubeSize-1); i++){
			if(cell[CubeFace.F][0][i].clr!=cell[CubeFace.F][0][clrref].clr){
				doMove(BE_getInvertEdgeMove(i));
			}
		}
	}
	String	BE_getStdEdgeMove(int y){
		String m=y2Slice(y, true);
		m=m+"RU'R'";
		m=m+switchMoveDir(y2Slice(y, true));
		m=m+"UR'";
		return m;
	}
	String	BE_getLastEdgeMove(int y){
		String m=y2Slice(y, true);
		m=m+"RF'UR'F";
		m=m+switchMoveDir(y2Slice(y, true));
		return m;
	}
	String	BE_getInvertEdgeMove(int y){
		//R*2B2U2L*U2R*'U2R*U2F2R*F2L*'B2R*2
		String r=x2Slice(cubeSize-1-y, false);
		String l=x2Slice(y, false);
		
		String ret=r+"2B2U2"+l+"U2"+r+"'U2"+r+"U2F2"+r+"F2"+l+"'B2"+r+"2";

		return ret;
	}

	void BuildCenters(){

		if(isOddSize) SetCubePos(MyColor.WHITE, MyColor.RED);

		for(int d=1; d<cubeDepth; d++) BC_setWYCenters(MyColor.WHITE, d);
		doMove("Z2");
		for(int d=1; d<cubeDepth; d++) BC_setWYCenters(MyColor.YELLOW, d);

		if(isOddSize){
			SetCubePos(MyColor.BLUE, MyColor.WHITE);
		} else{
			doMove("X'");
		}
		
		// set BLUE centers
		BC_setBCenters();		
		doMove("Z2");	// so we start with BLUE in B, WHITE in U
		// set GREEN centers
		BC_setGcenter();		
		doMove("Z");	// so we have RED in F (and still WHITE in U)
		// set RED and ORANGE centers
		BC_setROCenters();

	}	

	boolean BC_isColorCenterinFace(int clr, int face, int depth, int centerid){
		int i=0;
		boolean ret=(cell[face][depth][depth+centerid].clr==clr);
		
		while(i<4 && !ret){
			doMove(CubeFace.Id2Desc(face));
			ret=(cell[face][depth][depth+centerid].clr==clr);
			i++;
		}
		return ret;
/*
		return(
				cell[face][depth][depth+centerid].clr==clr 
			|| 	cell[face][depth+centerid][cubeSize-1-depth].clr==clr 
			||	cell[face][cubeSize-1-depth][cubeSize-1-depth-centerid].clr==clr 
			||	cell[face][cubeSize-1-depth-centerid][depth].clr==clr
				);
*/
		}
	void BC_setWYCenters(int clr, int depth){
		for(int e=0; e<4; e++){
			doMove("Y");
			// for each internal edge, we have <cubeSize-2*(depth+1)> edge cells to set. Each edge cell has a different set of possible positions
			for(int c=0; c<(cubeSize-2*depth); c++){
				//System.out.println("BuildCenters-SetCenters() depth="+depth+" ; edge="+e+"-"+c);
				if(cell[CubeFace.F][depth][depth+c].clr!=clr){
					if(			BC_isColorCenterinFace(clr, CubeFace.R, depth, c)){	BC_WY_R2F(clr, depth, c);						
					} else if(	BC_isColorCenterinFace(clr, CubeFace.D, depth, c)){	BC_WY_D2F(clr, depth, c);
					} else if(	BC_isColorCenterinFace(clr, CubeFace.L, depth, c)){	BC_WY_L2F(clr, depth, c);
					} else if(	BC_isColorCenterinFace(clr, CubeFace.U, depth, c)){	BC_WY_U2F(clr, depth, c);
					} else if(	BC_isColorCenterinFace(clr, CubeFace.B, depth, c)){	BC_WY_B2F(clr, depth, c);
					}
				}
			}
		}
	}
	void BC_setBCenters(){
		int clr=MyColor.BLUE;
		
		int dy=cubeDepth+((isOddSize)?1:0);
		int dx=1;
		
		for(int q=0; q<4; q++){
			doMove("F");
			// for each quadrant, we have a grid of size (cubeDepth-1)X(cubeDepth-1) to scan.

			// 1. Look in B
			for(int y=0; y<(cubeDepth-1+((isOddSize)?1:0)); y++){
				for(int x=0; x<(cubeDepth-1+((isOddSize)?1:0)); x++){
					//-- main move B->F (only if needed)
					if(cell[CubeFace.F][y+dy][x+dx].clr!=clr){
						BC_B2F(clr, y+dy, x+dx);
					}
				}
			}
			
			// 2. if quadrant still not complete, look in R
			for(int y=0; y<(cubeDepth-1+((isOddSize)?1:0)); y++){
				for(int x=0; x<(cubeDepth-1+((isOddSize)?1:0)); x++){
					//-- do the whole thing only if it's needed for this cell
					if(cell[CubeFace.F][y+dy][x+dx].clr!=clr){
						//-- move appropriate row from R to B
						BC_BG_R2B(clr, y+dy, x+dx);
						//-- main move B->F
						BC_B2F(clr, y+dy, x+dx);
					}
				}
			}
			// 3. if quadrant still not complete, look in L
			for(int y=0; y<(cubeDepth-1+((isOddSize)?1:0)); y++){
				for(int x=0; x<(cubeDepth-1+((isOddSize)?1:0)); x++){
					//-- do the whole thing only if it's needed for this cell
					if(cell[CubeFace.F][y+dy][x+dx].clr!=clr){
						//-- move appropriate row from L to B
						BC_BG_L2B(clr, y+dy, x+dx);
						//-- main move B->F
						BC_B2F(clr, y+dy, x+dx);
					}
				}
			}
		}
	}
	void BC_setGcenter(){
		// we start with BLUE in B, WHITE in U
		int clr=MyColor.GREEN;
		
		for(int y=1; y<(cubeSize-1); y++){
			for(int x=1; x<(cubeSize-1); x++){
				if(cell[CubeFace.F][y][x].clr!=clr){
					if(			BC_isColorCenterinFace(clr, CubeFace.R, y, x-y)){	BC_G_RL2F(CubeFace.R, y, x);						
					} else if(	BC_isColorCenterinFace(clr, CubeFace.L, y, x-y)){	BC_G_RL2F(CubeFace.L, y, x);
					}
				}
			}
		}
	}
	void BC_setROCenters(){
		int clr=MyColor.RED;
		int dy=cubeDepth+((isOddSize)?1:0);
		int dx=1;
		
		for(int q=0; q<4; q++){
			doMove("F");
			// for each quadrant, we have a grid of size (cubeDepth-1)X(cubeDepth-1) to scan.

			// 1. Look in B
			for(int y=0; y<(cubeDepth-1); y++){
				for(int x=0; x<(cubeDepth-1+((isOddSize)?1:0)); x++){
					//-- main move B->F (only if needed)
					if(cell[CubeFace.F][y+dy][x+dx].clr!=clr){
						BC_B2F(clr, y+dy, x+dx);
					}
				}
			}
		}
	}
	
	//-- BC_* routines set face internal centers
	void 	BC_WY_D2F(int clr, int depth, int edgeid){
		BC_WY_D2R(clr, depth, edgeid);
		BC_WY_R2F(clr, depth, edgeid);
	}
	void 	BC_WY_L2F(int clr, int depth, int edgeid){
		BC_WY_L2D(clr, depth, edgeid);
		BC_WY_D2R(clr, depth, edgeid);
		BC_WY_R2F(clr, depth, edgeid);
	}
	void 	BC_WY_U2F(int clr, int depth, int centerid){
		BC_WY_U2L(clr, depth, centerid);
		BC_WY_L2D(clr, depth, centerid);
		BC_WY_D2R(clr, depth, centerid);
		BC_WY_R2F(clr, depth, centerid);
	}
	void 	BC_WY_B2F(int clr, int depth, int edgeid){
		BC_WY_B2U(clr, depth, edgeid);
		BC_WY_U2L(clr, depth, edgeid);
		BC_WY_L2D(clr, depth, edgeid);
		BC_WY_D2R(clr, depth, edgeid);
		BC_WY_R2F(clr, depth, edgeid);
	}
	void 	BC_WY_R2F(int clr, int depth, int centerid){
		String m;
		int astCnt;
		// first, rotate R until sought edge is in target (2,4)
		while(cell[CubeFace.R][depth+centerid][cubeSize-1-depth].clr!=clr) doMove("R");
		//-- then, move it to F12
		int sliceId=depth+centerid;
		if(sliceId<cubeDepth){
			astCnt = sliceId;
			m="L".concat(repeat("*", astCnt)).concat("'DB").concat(repeat("*", depth)).concat("D'L").concat(repeat("*", astCnt));
		} else{
			astCnt=cubeSize-1-sliceId;
			m="R".concat(repeat("*", astCnt)).concat("D'B").concat(repeat("*", depth)).concat("DR").concat(repeat("*", astCnt)).concat("'");
		}
		doMove(m);
	}
	void	BC_WY_D2R(int clr, int depth, int centerid){
		// first, rotate D until sought edge is in target (4,3)
		while(cell[CubeFace.D][cubeSize-1-depth][cubeSize-1-depth-centerid].clr!=clr) doMove("D");
		// then, move it to R24 (for depth=1)
		String m="B".concat(repeat("*",depth));
		doMove(m);
	}
	void	BC_WY_L2D(int clr, int depth, int centerid){
		// first, rotate L until sought corner is in target (3,1)
		while(cell[CubeFace.L][cubeSize-1-depth-centerid][depth].clr!=clr) doMove("L");
		// then, move it to D43 (for depth=1)
		String m="B".concat(repeat("*",depth));
		doMove(m);
	}
	void	BC_WY_U2L(int clr, int depth, int centerid){
		// first, rotate U until sought corner is in target (1,2)
		while(cell[CubeFace.U][depth][depth+centerid].clr!=clr){
			doMove("U");
		}
		// then, move it to L31 (for depth=1)
		String m="B".concat(repeat("*",depth));
		doMove(m);
	}
	void	BC_WY_B2U(int clr, int depth, int centerid){
		String m;
		int astCnt;
		// first, rotate B until sought corner is in target (4,3)
		while(cell[CubeFace.B][cubeSize-1-depth][cubeSize-1-depth-centerid].clr!=clr) doMove("B");
		// then, move it to U11 (for depth=1)
		int sliceId=depth+centerid;
		if(sliceId<cubeDepth){
			m="B2L".concat(repeat("*",depth+centerid)).concat("'B2L").concat(repeat("*",depth+centerid));
		} else{
			astCnt=cubeSize-1-sliceId;
			m="B'R".concat(repeat("*",astCnt)).concat("BR").concat(repeat("*",astCnt)).concat("'");
		}
		doMove(m);
	}
	boolean BC_BG_setBpos(int clr, int Fy, int Fx){
		int By=(cubeSize-1)-Fy;
		int Bx=(cubeSize-1)-Fx;
		int i=0;
		boolean ret=(cell[CubeFace.B][By][Bx].clr==clr);
		while(i<4 && !ret){
			doMove("B");
			ret=(cell[CubeFace.B][By][Bx].clr==clr);
			i++;
		}
		return ret;
	}
	boolean BC_BG_R2B(int clr, int y, int x){
		// looks for <clr> cell in R[y][x]; if found, send row to B
		
		boolean ret=(cell[CubeFace.R][y][x].clr==clr);
		String m;
		int i=0;
		
		while(i<4 && !ret){
			doMove("R");
			ret=(cell[CubeFace.R][y][x].clr==clr);
			i++;
		}
		if(ret){
			if(y<(cubeDepth)){
				m="U"+repeat("*",y)+"'B2U"+repeat("*",y)+"B2";
			} else{
				m="D"+repeat("*",cubeSize-1-y)+"B2D"+repeat("*",cubeSize-1-y)+"'B2";
			}
			doMove(m);
		}
		return ret;
	}	
	boolean BC_BG_L2B(int clr, int y, int x){
		// looks for <clr> cell in L[y][x]; if found, send row to B
		
		boolean ret=(cell[CubeFace.L][y][x].clr==clr);
		String m;
		int i=0;
		
		while(i<4 && !ret){
			doMove("L");
			ret=(cell[CubeFace.L][y][x].clr==clr);
			i++;
		}
		if(ret){
			if(y<(cubeDepth)){
				m="U"+repeat("*",y)+"B2U"+repeat("*",y)+"'B2";
			} else{
				m="D"+repeat("*",cubeSize-1-y)+"'B2D"+repeat("*",cubeSize-1-y)+"B2";
			}
			doMove(m);
		}
		return ret;
	}
	boolean BC_BG_R2F(int clr, int y, int x){
		boolean ret=false;
		if( BC_BG_R2B(clr, y, x) ){
			ret=BC_B2F(clr, y, x);
		}
		return ret;
	}
	boolean BC_BG_L2F(int clr, int y, int x){
		boolean ret=false;
		if( BC_BG_L2B(clr, y, x) ){
			ret=BC_B2F(clr, y, x);
		}
		return ret;
	}
	boolean BC_B2F(int clr, int y, int x){
		// this is used for both BLUE and RED face centers
		int astCntL, astCntR;
		boolean ret=false;
		
		//-- rotate B until we have a BLUE in the right pos
		if (BC_BG_setBpos(clr, y, x)){
			// if successful, do the move
			astCntR=cubeSize-1-(y);
			astCntL=x;
			String m="L"+repeat("*",astCntL)+"2F'R"+repeat("*",astCntR)+"2F";
			doMove(m);doMove(m);
			ret=true;
		}
		return ret;
	}
	void	BC_G_RL2F(int face, int y, int x){
		// see excel file
		String f, o;
		if(face==CubeFace.R){
			f="R"; o="L";		// f stands for "face from which to move" (blue) cell
		} else{
			f="L"; o="R";		// o stands for "opposite to f"
		}
		boolean NEDiag			= (x==(cubeSize-1-y));
		String RTurnForth		= (NEDiag)?f:f+"'";		
		String CurrentRowF2L	= (y>(cubeDepth-((isOddSize)?0:1)))?("D"+repeat("*",cubeSize-1-y)+(f.equals("R")?"'":"")) : ("U"+repeat("*",y)+(f.equals("R")?"":"'") );
		String LTurnForth		= (NEDiag)?o:o+"'";
		String MissingColR2L	= ((NEDiag)?y2Slice(x, false):y2MirrorSlice(x))+"2"; 
		String LTurnBack		= (NEDiag)?o+"'":o;
		String CurrentRowL2F	= switchMoveDir(CurrentRowF2L);
		String MissingColL2R	= MissingColR2L;
		
		// rotate "from" face
		doMove(RTurnForth);
		// move affected row to L
		doMove(CurrentRowF2L);
		// rotate "to" face
		doMove(LTurnForth);
		// move opposite F row from F to L
		doMove(MissingColR2L);
		// rotate "to" face back
		doMove(LTurnBack);
		// move affected row back to F
		doMove(CurrentRowL2F);
		// move opposite row back to F
		doMove(MissingColL2R);
	}
//--

	String y2Slice(int y, boolean adjustDir){
		String f, dir;
		int ast;
		if(isOddSize){
			if (y>cubeDepth){
				f="D";
				ast=cubeSize-1-y;
				dir="";
			} else{
				f="U";
				ast=y;
				dir="'";
			}
		} else{
			if (y>(cubeDepth-1)){
				f="D";
				ast=cubeSize-1-y;
				dir="";
			} else{
				f="U";
				ast=y;
				dir="'";
			}
		}
		return ( f+repeat("*",ast)+((adjustDir)?dir:"") );
	}
	String x2Slice(int x, boolean adjustDir){
		String f, dir;
		int ast;
		if(isOddSize){
			if (x>cubeDepth){
				f="R";
				ast=cubeSize-1-x;
				dir="";
			} else{
				f="L";
				ast=x;
				dir="'";
			}
		} else{
			if (x>(cubeDepth-1)){
				f="R";
				ast=cubeSize-1-x;
				dir="";
			} else{
				f="L";
				ast=x;
				dir="'";
			}
		}
		return ( f+repeat("*",ast)+((adjustDir)?dir:"") );
	}
	String x2MirrorSlice(int x){
		String f;
		int ast;
		if(isOddSize){
			if (x>cubeDepth){
				f="L";
				ast=cubeSize-1-x;
			} else{
				f="R";
				ast=x;
			}
		} else{
			if (x>(cubeDepth-1)){
				f="L";
				ast=cubeSize-1-x;
			} else{
				f="R";
				ast=x;
			}
		}
		return ( f+repeat("*",ast) );
	}
	String y2MirrorSlice(int y){
		String f;
		int ast;
		if(isOddSize){
			if (y>cubeDepth){
				f="U";
				ast=cubeSize-1-y;
			} else{
				f="D";
				ast=y;
			}
		} else{
			if (y>(cubeDepth-1)){
				f="U";
				ast=cubeSize-1-y;
			} else{
				f="D";
				ast=y;
			}
		}
		return ( f+repeat("*",ast) );
	}
	//============== END SOLUTION STUFF =============================
	

	//============== START SHUFFLE STUFF =============================	
	String ShuffleMove(int cnt){
			
			String[] singleMove=InitMoves(cubeSize);
			String ret="";
			int m;
			
			//----- then, do the shuffling ------------------------------
			for(int mc=0;mc<cnt;mc++){
				m= 0 + (int)(Math.random() * ((movesTotCount-1) + 1));
				ret=ret.concat(singleMove[m]);
			}
			return ret;
		}

	JDialog dialog;
	boolean ShuffleInterrupted;
	
	void doShuffle(int cnt){
		ShuffleInterrupted=false;
		dialog=null;
		JButton btnCancel=new JButton("Cancel");		
		Thread dt = new Thread(new Runnable(){
		    public void run(){
		    	btnCancel.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						ShuffleInterrupted=true;
						dialog.dispose();
					}
				});
				ShuffleWait=new JOptionPane();
				ShuffleWait.setMessage("Shuffling Cube...");
				ShuffleWait.setMessageType(JOptionPane.CANCEL_OPTION);
				ShuffleWait.setOptions(new Object[]{btnCancel});
				dialog=ShuffleWait.createDialog(null, "Wait");
				dialog.setVisible(true);
		    }
		});
		dt.start();

		String sm=ShuffleMove(cnt);
		System.out.println("Shuffling cube with: "+sm);
    	doMove(sm);

		try{Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();} 
		if (dialog!=null) dialog.dispose();

	}
	//================ END SHUFFLE STUFF =============================	
		
	
	//===================== OTHER STUFF ==================================	

	void Blacken(){
		for(int f=0;f<6;f++){	
			for(int y=0;y<cubeSize;y++){
				for(int x=0;x<cubeSize;x++){
					// initialize each face with its default color
					cell[f][y][x]=new Ccell();
					cell[f][y][x].setColor(MyColor.BLACK);
				}
			}
			cell[f][0][1].setColor(MyColor.YELLOW); cell[f][1][0].setColor(MyColor.YELLOW); cell[f][2][1].setColor(MyColor.YELLOW); cell[f][1][2].setColor(MyColor.YELLOW);
		}
	}
	void MoveTester(JFrame caller){
		
/*		Blacken(); cell[CubeFace.F][0][2].Set(MyColor.RED); cell[CubeFace.U][2][2].Set(MyColor.WHITE); cell[CubeFace.R][0][0].Set(MyColor.BLUE); caller.repaint(); JOptionPane.showMessageDialog(caller, "1Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][0][2].Set(MyColor.BLUE); cell[CubeFace.U][2][2].Set(MyColor.RED); cell[CubeFace.R][0][0].Set(MyColor.WHITE); caller.repaint(); JOptionPane.showMessageDialog(caller, "2Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][0][2].Set(MyColor.WHITE); cell[CubeFace.U][2][2].Set(MyColor.BLUE); cell[CubeFace.R][0][0].Set(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "3Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][2][2].Set(MyColor.RED); cell[CubeFace.R][2][0].Set(MyColor.WHITE); cell[CubeFace.D][0][2].Set(MyColor.BLUE); caller.repaint(); JOptionPane.showMessageDialog(caller, "4Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][2][2].Set(MyColor.WHITE); cell[CubeFace.R][2][0].Set(MyColor.BLUE); cell[CubeFace.D][0][2].Set(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "5Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][2][2].Set(MyColor.BLUE); cell[CubeFace.R][2][0].Set(MyColor.RED); cell[CubeFace.D][0][2].Set(MyColor.WHITE); caller.repaint(); JOptionPane.showMessageDialog(caller, "6Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][2][0].Set(MyColor.RED); cell[CubeFace.D][0][0].Set(MyColor.WHITE); cell[CubeFace.L][2][2].Set(MyColor.BLUE); caller.repaint(); JOptionPane.showMessageDialog(caller, "7Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][2][0].Set(MyColor.WHITE); cell[CubeFace.D][0][0].Set(MyColor.BLUE); cell[CubeFace.L][2][2].Set(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "8Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][2][0].Set(MyColor.BLUE); cell[CubeFace.D][0][0].Set(MyColor.RED); cell[CubeFace.L][2][2].Set(MyColor.WHITE); caller.repaint(); JOptionPane.showMessageDialog(caller, "9Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][0][0].Set(MyColor.RED); cell[CubeFace.L][0][2].Set(MyColor.WHITE); cell[CubeFace.U][2][0].Set(MyColor.BLUE); caller.repaint(); JOptionPane.showMessageDialog(caller, "10Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][0][0].Set(MyColor.WHITE); cell[CubeFace.L][0][2].Set(MyColor.BLUE); cell[CubeFace.U][2][0].Set(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "11Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][0][0].Set(MyColor.BLUE); cell[CubeFace.L][0][2].Set(MyColor.RED); cell[CubeFace.U][2][0].Set(MyColor.WHITE); caller.repaint(); JOptionPane.showMessageDialog(caller, "12Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][0][0].Set(MyColor.RED); cell[CubeFace.R][0][2].Set(MyColor.WHITE); cell[CubeFace.U][0][2].Set(MyColor.BLUE); caller.repaint(); JOptionPane.showMessageDialog(caller, "13Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][0][0].Set(MyColor.WHITE); cell[CubeFace.R][0][2].Set(MyColor.BLUE); cell[CubeFace.U][0][2].Set(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "14Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][0][0].Set(MyColor.BLUE); cell[CubeFace.R][0][2].Set(MyColor.RED); cell[CubeFace.U][0][2].Set(MyColor.WHITE); caller.repaint(); JOptionPane.showMessageDialog(caller, "15Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][0][2].Set(MyColor.RED); cell[CubeFace.U][0][0].Set(MyColor.WHITE); cell[CubeFace.L][0][0].Set(MyColor.BLUE); caller.repaint(); JOptionPane.showMessageDialog(caller, "16Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][0][2].Set(MyColor.WHITE); cell[CubeFace.U][0][0].Set(MyColor.BLUE); cell[CubeFace.L][0][0].Set(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "17Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][0][2].Set(MyColor.BLUE); cell[CubeFace.U][0][0].Set(MyColor.RED); cell[CubeFace.L][0][0].Set(MyColor.WHITE); caller.repaint(); JOptionPane.showMessageDialog(caller, "18Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][2][2].Set(MyColor.RED); cell[CubeFace.L][2][0].Set(MyColor.WHITE); cell[CubeFace.D][2][0].Set(MyColor.BLUE); caller.repaint(); JOptionPane.showMessageDialog(caller, "19Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][2][2].Set(MyColor.WHITE); cell[CubeFace.L][2][0].Set(MyColor.BLUE); cell[CubeFace.D][2][0].Set(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "20Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][2][2].Set(MyColor.BLUE); cell[CubeFace.L][2][0].Set(MyColor.RED); cell[CubeFace.D][2][0].Set(MyColor.WHITE); caller.repaint(); JOptionPane.showMessageDialog(caller, "21Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][2][0].Set(MyColor.RED); cell[CubeFace.D][2][2].Set(MyColor.WHITE); cell[CubeFace.R][2][2].Set(MyColor.BLUE); caller.repaint(); JOptionPane.showMessageDialog(caller, "22Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][2][0].Set(MyColor.WHITE); cell[CubeFace.D][2][2].Set(MyColor.BLUE); cell[CubeFace.R][2][2].Set(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "23Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][2][0].Set(MyColor.BLUE); cell[CubeFace.D][2][2].Set(MyColor.RED); cell[CubeFace.R][2][2].Set(MyColor.WHITE); caller.repaint(); JOptionPane.showMessageDialog(caller, "24Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
*/		
		
		Blacken(); cell[CubeFace.F][1][2].setColor(MyColor.RED); cell[CubeFace.R][1][0].setColor(MyColor.GREEN); caller.repaint(); JOptionPane.showMessageDialog(caller, "a1Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][1][0].setColor(MyColor.RED); cell[CubeFace.R][1][2].setColor(MyColor.GREEN);caller.repaint(); JOptionPane.showMessageDialog(caller, "a2Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][1][2].setColor(MyColor.RED); cell[CubeFace.L][1][0].setColor(MyColor.GREEN);caller.repaint(); JOptionPane.showMessageDialog(caller, "a3Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][1][0].setColor(MyColor.RED); cell[CubeFace.L][1][2].setColor(MyColor.GREEN);caller.repaint(); JOptionPane.showMessageDialog(caller, "a4Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][2][1].setColor(MyColor.RED); cell[CubeFace.D][0][1].setColor(MyColor.GREEN);caller.repaint(); JOptionPane.showMessageDialog(caller, "a5Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.R][2][1].setColor(MyColor.RED); cell[CubeFace.D][1][2].setColor(MyColor.GREEN);caller.repaint(); JOptionPane.showMessageDialog(caller, "a6Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][2][1].setColor(MyColor.RED); cell[CubeFace.D][2][1].setColor(MyColor.GREEN);caller.repaint(); JOptionPane.showMessageDialog(caller, "a7Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.L][2][1].setColor(MyColor.RED); cell[CubeFace.D][1][0].setColor(MyColor.GREEN);caller.repaint(); JOptionPane.showMessageDialog(caller, "a8Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");

		Blacken(); cell[CubeFace.F][1][2].setColor(MyColor.GREEN); cell[CubeFace.R][1][0].setColor(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "b1Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][1][0].setColor(MyColor.GREEN); cell[CubeFace.R][1][2].setColor(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "b2Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][1][2].setColor(MyColor.GREEN); cell[CubeFace.L][1][0].setColor(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "b3Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][1][0].setColor(MyColor.GREEN); cell[CubeFace.L][1][2].setColor(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "b4Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.F][2][1].setColor(MyColor.GREEN); cell[CubeFace.D][0][1].setColor(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "b5Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.R][2][1].setColor(MyColor.GREEN); cell[CubeFace.D][1][2].setColor(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "b6Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.B][2][1].setColor(MyColor.GREEN); cell[CubeFace.D][2][1].setColor(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "b7Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");
		Blacken(); cell[CubeFace.L][2][1].setColor(MyColor.GREEN); cell[CubeFace.D][1][0].setColor(MyColor.RED); caller.repaint(); JOptionPane.showMessageDialog(caller, "b8Solve!"); Solve(); caller.repaint(); JOptionPane.showMessageDialog(caller, "Solved");

		//Blacken(); cell[CubeFace.F][0][1].setColor(MyColor.RED); cell[CubeFace.U][2][1].setColor(MyColor.WHITE); System.out.println("FU:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.F][1][2].setColor(MyColor.RED); cell[CubeFace.R][1][0].setColor(MyColor.WHITE); System.out.println("FR:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.F][2][1].setColor(MyColor.RED); cell[CubeFace.D][0][1].setColor(MyColor.WHITE); System.out.println("FD:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.F][1][0].setColor(MyColor.RED); cell[CubeFace.L][1][2].setColor(MyColor.WHITE); System.out.println("FL:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.B][0][1].setColor(MyColor.RED); cell[CubeFace.U][0][1].setColor(MyColor.WHITE); System.out.println("BU:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.B][1][2].setColor(MyColor.RED); cell[CubeFace.L][1][0].setColor(MyColor.WHITE); System.out.println("BL:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.B][2][1].setColor(MyColor.RED); cell[CubeFace.D][2][1].setColor(MyColor.WHITE); System.out.println("BD:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.B][1][0].setColor(MyColor.RED); cell[CubeFace.R][1][2].setColor(MyColor.WHITE); System.out.println("BR:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.U][1][0].setColor(MyColor.RED); cell[CubeFace.L][0][1].setColor(MyColor.WHITE); System.out.println("UL:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.U][1][2].setColor(MyColor.RED); cell[CubeFace.R][0][1].setColor(MyColor.WHITE); System.out.println("UR:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.L][2][1].setColor(MyColor.RED); cell[CubeFace.D][1][0].setColor(MyColor.WHITE); System.out.println("LD:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.R][2][1].setColor(MyColor.RED); cell[CubeFace.D][1][2].setColor(MyColor.WHITE); System.out.println("RD:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
//
		//Blacken(); cell[CubeFace.F][0][1].setColor(MyColor.WHITE); cell[CubeFace.U][2][1].setColor(MyColor.RED); System.out.println("FU:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.F][1][2].setColor(MyColor.WHITE); cell[CubeFace.R][1][0].setColor(MyColor.RED); System.out.println("FR:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.F][2][1].setColor(MyColor.WHITE); cell[CubeFace.D][0][1].setColor(MyColor.RED); System.out.println("FD:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.F][1][0].setColor(MyColor.WHITE); cell[CubeFace.L][1][2].setColor(MyColor.RED); System.out.println("FL:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.B][0][1].setColor(MyColor.WHITE); cell[CubeFace.U][0][1].setColor(MyColor.RED); System.out.println("BU:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.B][1][2].setColor(MyColor.WHITE); cell[CubeFace.L][1][0].setColor(MyColor.RED); System.out.println("BL:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.B][2][1].setColor(MyColor.WHITE); cell[CubeFace.D][2][1].setColor(MyColor.RED); System.out.println("BD:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.B][1][0].setColor(MyColor.WHITE); cell[CubeFace.R][1][2].setColor(MyColor.RED); System.out.println("BR:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.U][1][0].setColor(MyColor.WHITE); cell[CubeFace.L][0][1].setColor(MyColor.RED); System.out.println("UL:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.U][1][2].setColor(MyColor.WHITE); cell[CubeFace.R][0][1].setColor(MyColor.RED); System.out.println("UR:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.L][2][1].setColor(MyColor.WHITE); cell[CubeFace.D][1][0].setColor(MyColor.RED); System.out.println("LD:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
		//Blacken(); cell[CubeFace.R][2][1].setColor(MyColor.WHITE); cell[CubeFace.D][1][2].setColor(MyColor.RED); System.out.println("RD:"+getEdgeDesc(FindEdge(MyColor.RED, MyColor.WHITE)));	FixTopEdge(FindEdge(MyColor.RED, MyColor.WHITE));
}
	void setShow3D(boolean s){ showMove=s; }

	void setShowlbl(boolean s){showCellLabels=s; }
	
/*	
	void PauseExecution(String txt){
		JOptionPane op=new JOptionPane();
		op.setMessage(txt);
		op.setMessageType(JOptionPane.OK_OPTION);
		JButton btnContinue=new JButton("Continue");
		btnContinue.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				dialog.dispose();
			}
		});
		JButton btnStop=new JButton("Stop");
		btnStop.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(-1);
			}
		});
		op.setOptions(new Object[]{btnContinue, btnStop});
		dialog=op.createDialog(null, "Pause");
		dialog.setVisible(true);
	}
*/	
	String repeat(String str, int times){
		return new String(new char[times]).replace("\0",  str);
	}
	
	class cEdge{
		int id;
		int pos;
		int clr1, clr2;		// these are only set by isDone()
		
		static final int FU=1;
		static final int FR=2;
		static final int FD=3;
		static final int FL=4;
		static final int BU=5;
		static final int BL=6;
		static final int BD=7;
		static final int BR=8;
		static final int UL=9;
		static final int UR=10;
		static final int LD=11;
		static final int RD=12;

		static final int UF=13;
		static final int RF=14;
		static final int DF=15;
		static final int LF=16;
		static final int UB=17;
		static final int LB=18;
		static final int DB=19;
		static final int RB=20;
		static final int LU=21;
		static final int RU=22;
		static final int DL=23;
		static final int DR=24;
		
		static final String mInvertFL="L'FU'LF'";
		static final String mInvertFR="R'FD'RF'";

		// constructor
		cEdge(int edgeid){
			this.id=edgeid;
		}
		
		void setPos(int p){ pos=p; }
		
		String getDesc(){
			return (getDesc(id));
		}
		String getDesc(int id){
			String ret="";
			switch (id){
			case FU: ret= "FU"; break;
			case FR: ret= "FR"; break;
			case FD: ret= "FD"; break;
			case FL: ret= "FL"; break;
			case BU: ret= "BU"; break;
			case BR: ret= "BR"; break;
			case BD: ret= "BD"; break;
			case BL: ret= "BL"; break;
			case UL: ret= "UL"; break;
			case UR: ret= "UR"; break;
			case RD: ret= "RD"; break;
			case LD: ret= "LD"; break;
			case UF: ret= "UF"; break;
			case RF: ret= "RF"; break;
			case DF: ret= "DF"; break;
			case LF: ret= "LF"; break;
			case UB: ret= "UB"; break;
			case RB: ret= "RB"; break;
			case DB: ret= "DB"; break;
			case LB: ret= "LB"; break;
			case LU: ret= "LU"; break;
			case RU: ret= "RU"; break;
			case DR: ret= "DR"; break;
			case DL: ret= "DL"; break;
			}
			return ret;
		}

		String toFLmove(int y){
			String ret="";
			switch(id){
			case FU: ret= "RF'R'"; break;
			case FR: ret=BE_getStdEdgeMove(y) + "DB'L2"; break;
			case FD: ret= "D'L'"; break;
			case FL: ret= ""; break;
			case BU: ret= "U'L"; break;
			case BR: ret= "B2L2"; break;
			case BD: ret= "B'L2"; break;
			case BL: ret= "L2"; break;
			case UL: ret= "L"; break;
			case UR: ret= "U2L"; break;
			case RD: ret= "D2L'"; break;
			case LD: ret= "L'"; break;

			case UF: ret= "UL"; break;
			case RF: ret=BE_getStdEdgeMove(y) + "DB'L2"; break;
			case DF: ret= "D'L'"; break;
			case LF: ret= ""; break;
			case UB: ret= "U'L"; break;
			case RB: ret= "B2L2"; break;
			case DB: ret= "B'L2"; break;
			case LB: ret= "L2"; break;
			case LU: ret= "L"; break;
			case RU: ret= "U2L"; break;
			case DR: ret= "D2L'"; break;
			case DL: ret= "L'"; break;
			}
			return ret;
		}

		boolean free(){
			boolean ret=true;
			
			return ret;
		}
		boolean isDone(){
			boolean ret=true;
			
			if(id==FU ||id==UF){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.F][0][i].clr!=cell[CubeFace.F][0][i-1].clr) ret=false;
					if(cell[CubeFace.U][cubeSize-1][i].clr!=cell[CubeFace.U][cubeSize-1][i-1].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.F][0][2].clr;
					clr2=cell[CubeFace.U][cubeSize-1][2].clr;
				}
			} else if(id==FR||id==RF){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.F][i][cubeSize-1].clr!=cell[CubeFace.F][i-1][cubeSize-1].clr) ret=false;
					if(cell[CubeFace.R][i][0].clr!=cell[CubeFace.R][i-1][0].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.F][2][cubeSize-1].clr;
					clr2=cell[CubeFace.R][2][0].clr;
				}
			} else if(id==FD||id==DF){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.F][cubeSize-1][i].clr!=cell[CubeFace.F][cubeSize-1][i-1].clr) ret=false;
					if(cell[CubeFace.D][0][i].clr!=cell[CubeFace.D][0][i-1].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.F][cubeSize-1][2].clr;
					clr2=cell[CubeFace.D][0][2].clr;
				}
			} else if(id==FL||id==LF){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.F][i][0].clr!=cell[CubeFace.F][i-1][0].clr) ret=false;
					if(cell[CubeFace.L][i][cubeSize-1].clr!=cell[CubeFace.L][i-1][cubeSize-1].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.F][2][0].clr;
					clr2=cell[CubeFace.L][2][cubeSize-1].clr;
				}
			} else if(id==BU||id==UB){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.B][0][i].clr!=cell[CubeFace.B][0][i-1].clr) ret=false;
					if(cell[CubeFace.U][0][cubeSize-i].clr!=cell[CubeFace.U][0][cubeSize-i-1].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.B][0][2].clr;
					clr2=cell[CubeFace.U][0][cubeSize-2].clr;
				}
			} else if(id==BR||id==RB){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.B][i][0].clr!=cell[CubeFace.B][i-1][0].clr) ret=false;
					if(cell[CubeFace.R][i][cubeSize-1].clr!=cell[CubeFace.R][i-1][cubeSize-1].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.B][2][0].clr;
					clr2=cell[CubeFace.R][2][cubeSize-1].clr;
				}
			} else if(id==BD||id==DB){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.B][cubeSize-1][i].clr!=cell[CubeFace.B][cubeSize-1][i-1].clr) ret=false;
					if(cell[CubeFace.D][cubeSize-1][i].clr!=cell[CubeFace.D][cubeSize-1][i-1].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.B][cubeSize-1][2].clr;
					clr2=cell[CubeFace.D][cubeSize-1][2].clr;
				}
			} else if(id==BL||id==LB){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.B][i][cubeSize-1].clr!=cell[CubeFace.B][i-1][cubeSize-1].clr) ret=false;
					if(cell[CubeFace.L][i][0].clr!=cell[CubeFace.L][i-1][0].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.B][2][cubeSize-1].clr;
					clr2=cell[CubeFace.L][2][0].clr;
				}
			} else if(id==UL||id==LU){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.U][i][0].clr!=cell[CubeFace.U][i-1][0].clr) ret=false;
					if(cell[CubeFace.L][0][i].clr!=cell[CubeFace.L][0][i-1].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.U][2][0].clr;
					clr2=cell[CubeFace.L][0][2].clr;
				}
			} else if(id==UR||id==RU){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.U][i][cubeSize-1].clr!=cell[CubeFace.U][i-1][cubeSize-1].clr) ret=false;
					if(cell[CubeFace.R][0][i].clr!=cell[CubeFace.R][0][i-1].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.U][2][cubeSize-1].clr;
					clr2=cell[CubeFace.R][0][2].clr;
				}
			} else if(id==RD||id==DR){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.R][cubeSize-1][i].clr!=cell[CubeFace.R][cubeSize-1][i-1].clr) ret=false;
					if(cell[CubeFace.D][i][cubeSize-1].clr!=cell[CubeFace.D][i-1][cubeSize-1].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.R][cubeSize-1][2].clr;
					clr2=cell[CubeFace.D][2][cubeSize-1].clr;
				}
			} else if(id==LD||id==DL){
				for(int i=2; i<(cubeSize-1); i++){
					if(cell[CubeFace.L][cubeSize-1][i].clr!=cell[CubeFace.L][cubeSize-1][i-1].clr) ret=false;
					if(cell[CubeFace.D][i][0].clr!=cell[CubeFace.D][i-1][0].clr) ret=false;
				}				
				// if edge is complete (done), set edge colors
				if(ret){
					clr1=cell[CubeFace.L][cubeSize-1][2].clr;
					clr2=cell[CubeFace.D][2][0].clr;
				}
			}
			if(!ret){
				clr1=-99;
				clr2=-99;
			}
			return ret;
		}
	}
	//===================== OTHER STUFF ==================================	


}
