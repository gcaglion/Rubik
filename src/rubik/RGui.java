package rubik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import my3d.Scene3D;

public class RGui extends JPanel{
	private static final long serialVersionUID = 1L;

	static int cubeSize=7;
	int cellGap=3;
	int cellThickness=10;
	static Rubik myCube;
	static Scene2D gui2d;
	static Scene3D gui3d;
	static RConsole2D cmd2d;
	static RConsole3D cmd3d;
	static SolutionBox slnBox;
	
	static JFrame frmMain;
	static JPanel p0;

	//-- perspective projection stuff
	static final int PosXMin=-1000; static final int PosXMax=1000; static final int PosXInit=0;
	static final int PosYMin=-1000; static final int PosYMax=1000; static final int PosYInit=0;
	static final int PosZMin=-1000; static final int PosZMax=5000; static final int PosZInit=5000;
	static final int AlphaMin=0; static final int AlphaMax=360; static final int AlphaInit=30;
	static final int BetaMin=0; static final int BetaMax=360; static final int BetaInit=20;
	static final int GammaMin=0; static final int GammaMax=360; static final int GammaInit=0;
	static final int SpeedMin=0; static final int SpeedMax=10; static final int SpeedInit=5;

	static void init(int iCubeSize, int iCellGap, int iCellThickness, int iPosXInit, int iPosYInit, int iPosZInit, int iAlphaInit, int iBetaInit, int iGammaInit, int iSpeedInit){
		// cellSize should be dynamic. total face size should be approximately 240, so let's override cellSize:
		int cellSize=240/iCubeSize;
		
		myCube=new Rubik(iCubeSize, cellSize, iCellGap, iCellThickness);
		double cubeDiag=(int)Math.sqrt((2*Math.pow(iCubeSize*cellSize, 2)));
		gui2d=new Scene2D(myCube);
		gui3d=new Scene3D(iPosXInit, iPosYInit, iPosZInit, iAlphaInit, iBetaInit, iGammaInit, iSpeedInit);
		slnBox=new SolutionBox();
		myCube.setGui(gui2d, gui3d, slnBox);
		myCube.set3Dspeed(iSpeedInit);
		gui3d.add(myCube);

		cmd2d=new RConsole2D(myCube); cmd2d.setGui(gui2d, gui3d);
		cmd3d=new RConsole3D(myCube, gui3d, PosXMin, PosXMax, PosXInit, PosYMin, PosYMax, PosYInit, PosZMin, PosZMax, PosZInit, AlphaMin, AlphaMax, AlphaInit, BetaMin, BetaMax, BetaInit, GammaMin, GammaMax, GammaInit, SpeedMin, SpeedMax, SpeedInit);

		if(p0!=null) frmMain.remove(p0);
		
		p0=new JPanel();
		p0.setBackground(Color.BLACK); p0.setLayout(new BoxLayout(p0, BoxLayout.X_AXIS)); 
		JPanel pL=new JPanel(); pL.setBackground(Color.GREEN); 
		JPanel pR=new JPanel();	pR.setBackground(Color.RED  );  pR.setMinimumSize(new Dimension((int)(cubeDiag*1.2), (int)(cubeDiag*1.2)));		
		p0.add(pL);
		p0.add(pR);
		
		pL.setLayout(new BoxLayout(pL, BoxLayout.Y_AXIS));
		JPanel pLU=new JPanel(); pLU.setBackground(Color.CYAN);
		JPanel pLD=new JPanel(); pLD.setBackground(Color.MAGENTA);
		pL.add(pLU);
		pL.add(pLD);
		
		pLD.setLayout(new BoxLayout(pLD, BoxLayout.X_AXIS));
		JPanel pLDL=new JPanel(); pLDL.setBackground(Color.BLUE);
		JPanel pLDR=new JPanel(); pLDR.setBackground(Color.YELLOW);
		pLD.add(pLDL);
		pLD.add(pLDR);
		
		pLU.add(gui2d);
		pLDL.add(cmd2d);
		pLDR.setLayout(new BorderLayout());
		pLDR.add(slnBox, BorderLayout.CENTER);
		
		pR.setLayout(new BoxLayout(pR, BoxLayout.Y_AXIS));
		JPanel pRU=new JPanel(); pRU.setBackground(Color.CYAN); 	pRU.setLayout(new BorderLayout());
		JPanel pRD=new JPanel(); pRD.setBackground(Color.MAGENTA);	pRD.setLayout(new BorderLayout());
		pR.add(pRU);
		pR.add(pRD);
		
		pRU.add(gui3d, BorderLayout.CENTER);
		pRD.add(cmd3d, BorderLayout.CENTER);

		frmMain.add(p0);
		frmMain.pack();
	}
	
	RGui(){
		
		
		frmMain=new JFrame();
		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		init(cubeSize, cellGap, cellThickness, PosXInit, PosYInit, PosZInit, AlphaInit, BetaInit, GammaInit, SpeedInit);
		
		frmMain.setVisible(true);
		
	}
	
	public static void main(String[] args){
    	new RGui();
	}
	
}
