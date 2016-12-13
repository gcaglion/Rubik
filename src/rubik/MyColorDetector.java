package rubik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;		// v.3.1.0

import my3d.MyColor;

//import org.opencv.highgui.VideoCapture;	// v.2.4.3
import org.opencv.imgproc.Imgproc;

public class MyColorDetector {
	
	Rubik cube;
	JFrame frmMyColorDetector;
	MyColorPicker MyCP;
	MyColor MyClr=new MyColor();
	
 	int imgSizeX=200;
 	int imgSizeY=150;
	int cellSize;
 	Size imgSize=new Size(imgSizeX, imgSizeY);
	Point p1=new Point();
	Point p2=new Point();

	VideoCapture feed;
	static MyColor cellColor[][][];	// [Panel][y][x]
    
	Scalar gridColor=new Scalar(100,100,100);
	Scalar gridTextColor=new Scalar(100,50,50);
	
    //-- Panels Id
	public static final int DECODED=7;
	//private static final int FEED=8;

	//-- Faces
	private CubeFace cubeFace[]=new CubeFace[6];

	//static boolean isFrozen[]=new boolean[6];
	
	//-- panels and Mats
	Mat imgFeed, imgHSV, imgNorm;
	Mat imgClr[]=new Mat[6];		// Color panels
	retPanel retp[]  = new retPanel[6]; // return panels
	imgPanel detp[]= new imgPanel[6]; // detection panels
	imgPanel FeedPanel, NormPanel;
	JPanel contD, contA, contR, contM;	// container panels (detection, action, return, main)
	JLabel lfaceDesc[]=new JLabel[6];	// ret panel description label

	public MyColorDetector(Rubik cube){
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

		this.cube=cube;
		cellSize=imgSizeY/cube.cubeSize;
		
		//-- Initialize Class arrays
		cellColor=new MyColor[14][cube.cubeSize][cube.cubeSize];	// 14 panels ( 6 colors +Feed +Norm +6 faces )
		for(int p=0;p<14;p++){
			for(int y=0;y<cube.cubeSize;y++){
				for(int x=0;x<cube.cubeSize;x++){
					cellColor[p][y][x]=new MyColor();
				}
			}
		}
		for(int f=0;f<6;f++) cubeFace[f]=new CubeFace();
		
	    //-- Video capture structure
	    feed=new VideoCapture(0);
	    //-- Mat structures
	    imgFeed=new Mat();
		imgHSV=new Mat();
		imgNorm=new Mat();
		for(int f=0;f<6;f++) imgClr[f]=new Mat();
			
		for(int f=0;f<6;f++) 
			
		//-- child frame
		frmMyColorDetector=new JFrame("My Child");
		frmMyColorDetector.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    frmMyColorDetector.setSize(1300, 800);
		frmMyColorDetector.setVisible(true);
		
	    //-- Instantiate ColorPicker panel, keep it invisible
	    MyCP= new MyColorPicker(frmMyColorDetector);
	    
		//-- clear resources on exit
		frmMyColorDetector.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				feed.release();
			    frmMyColorDetector.dispose();
			    MyCP.frmCP.dispose();
			}
		});

	    //-- commands sub-panels for Decoded Image panel
		JPanel cmdsubPanelR=new JPanel();
	    cmdsubPanelR.setLayout(new BoxLayout(cmdsubPanelR, BoxLayout.Y_AXIS)); cmdsubPanelR.setOpaque(false);
	    
		//-- Main container panel
		contM=new JPanel();
	    //double size[][]={ {0.22, 0.22, 0.12, 0.22, 0.22}, {900,100} };
	    //contM.setLayout(new TableLayout(size));
		contM.setLayout(new GridLayout(1,3));
		//-- sub-container for Detection panels
		contD=new JPanel();
		contD.setLayout(new GridLayout(3,2));
		//-- sub-container for Action panels
		contA=new JPanel();
		contA.setLayout(new GridLayout(4,1));
		//-- sub-container for Return panels
		contR=new JPanel();
		contR.setLayout(new GridLayout(3,2));
		
		//-- add Color panels to Detection sub-container
		for(int f=0;f<6;f++){
			detp[f]=new imgPanel(); detp[f].setBackground(MyColor.Id2J(f)); contD.add(detp[f]);
		}
		
		//-- add Return panels , and relative labels, to Return sub-container
		for(int f=0; f<6;f++){
			retp[f]=new retPanel(); retp[f].setBackground(MyColor.Id2J(f)); contR.add(retp[f]);
			lfaceDesc[f]=new JLabel(); lfaceDesc[f].setText(CubeFace.Id2Desc(f)); lfaceDesc[f].setVisible(true); retp[f].add(lfaceDesc[f]);
		}

		//-- Add Action panels to Action sub-container
		FeedPanel = new imgPanel();		FeedPanel.setBackground(Color.CYAN);	contA.add(FeedPanel);
		NormPanel= new imgPanel();		NormPanel.setBackground(Color.MAGENTA);	NormPanel.setLayout(new BorderLayout());				
	    //-- Buttons to set Faces
			JToggleButton btnFace[]=new JToggleButton[6];
			CustomItemListener   MyItmList=new CustomItemListener();
			for(int f=0; f<6;f++){
				btnFace[f]=new JToggleButton(cubeFace[f].Id2SetDesc(f));
				btnFace[f].addItemListener(MyItmList);
				//cmdsubPanelR.add(Box.createVerticalStrut(5));
				cmdsubPanelR.add(btnFace[f]);
			}
		    NormPanel.add(cmdsubPanelR, BorderLayout.EAST);
		    //-- Unlock button
		    JButton btnUnlock=new JButton("Unlock"); btnUnlock.addActionListener(new CustomActionListener()); NormPanel.add(btnUnlock, BorderLayout.SOUTH);
			//-- Mouse Listener on PanelNorm, to override cell colors
		    NormPanel.addMouseListener(new java.awt.event.MouseAdapter(){

				public void mouseClicked(java.awt.event.MouseEvent evt){
					MyColor PickedColor=new MyColor();
					Point mousePos=new Point(0,0);
					Point cell=new Point(0,0);
					mousePos.y=MouseInfo.getPointerInfo().getLocation().y - contA.getY()-NormPanel.getY()-31;
					mousePos.x=MouseInfo.getPointerInfo().getLocation().x - contA.getX()-NormPanel.getX()-8;
					if(Mouse2CellPos(mousePos, cell)){
						if(SwingUtilities.isRightMouseButton(evt)){		
							mousePos.y=MouseInfo.getPointerInfo().getLocation().y;
							mousePos.x=MouseInfo.getPointerInfo().getLocation().x;
							PickedColor=MyCP.GetColor(mousePos);
							cellColor[DECODED][(int)cell.y][(int)cell.x].Set(PickedColor.Id);
							cellColor[DECODED][(int)cell.y][(int)cell.x].Lock();
						} else{
							cellColor[DECODED][(int)cell.y][(int)cell.x].Free();
						}
					}
				}
			});	
		contA.add(NormPanel);

		//-- add Exit and Debug buttons to Action sub-container
		JButton btnSolve=new JButton("Proceed!"); btnSolve.addActionListener(new CustomActionListener()); contA.add(btnSolve);
		JButton btnExit=new JButton("Quit")	 ; btnExit.addActionListener(new CustomActionListener());  contA.add(btnExit);
		
		//-- add Detection, Action, Return sub-containers to main container
		contM.add(contD);
		contM.add(contA);
		contM.add(contR);
		
		//-- add Main container panel to frame
	    frmMyColorDetector.setContentPane(contM);

		//-- Loop to display the feed in the window
	     if(feed.isOpened()){
	 		new Thread(new Runnable(){
			    @Override
			    public void run(){
					VideoFeedLoop(frmMyColorDetector);
			    }
			}).start();
	     }
	}
	
	private void VideoFeedLoop(JFrame containerFrame){
    	while(true){
			 if(!feed.isOpened()) break;
			 feed.read(imgFeed);	    	
			 if(imgFeed.size().width==0) break;
			 
			 //-- resize
			 Imgproc.resize(imgFeed,  imgFeed,  imgSize); 
			
			 //-- equalize
			 Imgproc.cvtColor(imgFeed, imgNorm, Imgproc.COLOR_BGR2Lab );
			 
			 //-- increase brightness
			 //imgFeed.convertTo(imgFeed, -1, 2,50);
			 
			 //-- HSV conversion
			 Imgproc.cvtColor(imgFeed,  imgHSV,  Imgproc.COLOR_BGR2HSV);
			 
			 //-- Gaussian filtering to blur out noise
			 Imgproc.GaussianBlur(imgHSV, imgHSV, new Size(9,9),0,0);
			
			 //-- filter colors
			 for(int f=0;f<6;f++) Core.inRange(imgHSV,  MyClr.clrRangeMin[f],  MyClr.clrRangeMax[f], imgClr[f]);
			 
			 //-- add cell grid based on CubeSize	    
			 AddGrid( 0, imgFeed, false);
			 AddGrid( 0, imgNorm, false);
			 for(int f=0;f<6;f++) AddGrid(f, imgClr[f], true);
		
			 //-- write color codes in each cell of imgDecoded
			 DecodeColors( imgNorm, false);
			
			 //-- add captions (v.2.4.13)
			 //Core.putText(imgFeed, "CAMERA FEED", 	new Point(30,30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(100,10,10,255));
			 //Core.putText(imgNorm, "Norm", 			new Point(30,30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(100,10,10,255));
			 //for(int f=0;f<6;f++) Core.putText(imgClr[f],  MyClr.Id2Desc(f), new Point(30,30), Core.FONT_HERSHEY_PLAIN, 2, gridTextColor);
			 //-- add captions (v.3.1.0)
			 Imgproc.putText(imgFeed, "CAMERA FEED", 	new Point(30,30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(100,10,10,255));
			 Imgproc.putText(imgNorm, "Norm", 			new Point(30,30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(100,10,10,255));
			 for(int f=0;f<6;f++) Imgproc.putText(imgClr[f],  MyColor.Id2Desc(f), new Point(30,30), Core.FONT_HERSHEY_PLAIN, 2, gridTextColor);
			 
			 //-- display images in panels (Colors +Feed +Norm)
			 FeedPanel.setimagewithMat(imgFeed);
			 NormPanel.setimagewithMat(imgNorm);
			 for(int f=0;f<6;f++) detp[f].setimagewithMat(imgClr[f]);
			 //-- display images in panels (return)
			 for(int f=0;f<6;f++) retp[f].DrawFrozenFace(f);
	
						
			containerFrame.repaint();
		}
	}

	private void AddGrid(int PanelId, Mat pFeed, boolean ReadColors){

		for(int ny=0;ny<cube.cubeSize;ny++){
			for(int nx=0;nx<cube.cubeSize;nx++){
				//-- for each cell, determine top-left and bottom-right corners of the rectangle
				p1.y=ny*cellSize; p1.x=(imgSizeX-imgSizeY)/2+nx*cellSize;
				p2.y=p1.y+cellSize;p2.x=p1.x+cellSize;
				//-- draw rectangle
				//-- Core.rectangle(pFeed, p1,  p2, gridColor );	// v.2.4.13
				Imgproc.rectangle(pFeed, p1,  p2, gridColor );		// v.3.1.0

				//-- write average color value in each cell
				if(ReadColors){
					cellColor[PanelId+8][ny][nx].Set( Core.mean(pFeed.submat((int)p1.y, (int)p2.y, (int)p1.x, (int)p2.x)) );;
					//Core.putText(pFeed, String.format("%.0f", cellColor[PanelId][ny][nx].CV.val[0]), new Point(p1.x+5, p1.y+10), 3, 0.5, new Scalar(100,100,100));		// v.2.4.13				
					Imgproc.putText(pFeed, String.format("%.0f", cellColor[PanelId+8][ny][nx].CV.val[0]), new Point(p1.x+5, p1.y+10), 3, 0.4, new Scalar(100,100,100));		// v.3.1.0
				}
			}			
		}
	}
	
	private void DecodeColors(Mat DisplayP, boolean setColor){
		 for(int ny=0;ny<cube.cubeSize;ny++){
			for(int nx=0;nx<cube.cubeSize;nx++){
	    	     for(int clrP=MyColor.RED; clrP<=MyColor.ORANGE; clrP++){
					if(cellColor[DECODED][ny][nx].isSet || cellColor[clrP+8][ny][nx].CV.val[0] > 100){	
						p1.y=ny*imgSizeY/cube.cubeSize; p1.x=(imgSizeX-imgSizeY)/2+nx*imgSizeY/cube.cubeSize;
						p2.y=p1.y+imgSizeY/cube.cubeSize;p2.x=p1.x+imgSizeY/cube.cubeSize;
						//-- set cell color to clrP
						if(!cellColor[DECODED][ny][nx].isSet){
							cellColor[DECODED][ny][nx].Set(clrP);
						}
						//-- draw rectangle
						//Core.rectangle(DisplayP, p1,  p2, cellColor[DECODED][ny][nx].CV, -1);		// v.2.4.13
						//Core.rectangle(DisplayP, p1,  p2, MyClr.Id2CV(MyColor.BLACK));			// v.2.4.13
						Imgproc.rectangle(DisplayP, p1,  p2, cellColor[DECODED][ny][nx].CV, -1);	// v.3.1.0
						Imgproc.rectangle(DisplayP, p1,  p2, MyColor.Id2CV(MyColor.BLACK));			// v.3.1.0
					}
				}
			 }
	     }
	}
	
	private boolean Mouse2CellPos(Point mousePos, Point cellPos){
		for(int ny=0;ny<cube.cubeSize;ny++){
			for(int nx=0;nx<cube.cubeSize;nx++){
				//-- for each cell, determine top-left and bottom-right corners of the rectangle
				p1.y=ny*cellSize; p1.x=(imgSizeX-imgSizeY)/2+nx*cellSize;
				p2.y=p1.y+cellSize;p2.x=p1.x+cellSize;
				//System.out.println("Mouse2CellPos: mX="+mousePos.x+" ; mY="+mousePos.y+" ; p1.x="+p1.x+" ; p1.y="+p1.y+" ; p2.x="+p2.x+" ; p2.y="+p2.y);
				if(mousePos.x>=p1.x && mousePos.y>=p1.y && mousePos.x<=p2.x && mousePos.y<=p2.y){
					cellPos.x=nx; cellPos.y=ny;
					//System.out.println("Mouse2CellPos: cX="+cellPos.x+" ; cY="+cellPos.y);
					return true;
				}
			}
		}
		return false;
	}
	
	class MyColorPicker{
		JDialog frmCP;
		//JFrame frmCP;
		private Point colorCell=new Point();
		public MyColor PickedColor=new MyColor();
		public boolean isPicked=false;

		public MyColor GetColor(Point p0){
			frmCP.setLocation((int)p0.x, (int)p0.y);
			frmCP.setVisible(true);
			//frmCP.setModal(true);
			return PickedColor;
		}
		public void Show(Point p0){
			frmCP.setVisible(true);
			frmCP.setLocation((int)p0.x, (int)p0.y);
		}
		public MyColorPicker(JFrame frmParent){
			frmCP=new JDialog(frmParent, "Color Picker", true);
			//frmCP=new JFrame("Color Picker");
			frmCP.setUndecorated(true);
		    frmCP.setSize(100, 150);
			frmCP.setVisible(false);

			JPanel container=new JPanel();
		    container.setLayout(new GridLayout(3,2));
		    JPanel panelW=new JPanel(); panelW.setBackground(Color.WHITE); container.add(panelW);
		    JPanel panelY=new JPanel(); panelY.setBackground(Color.YELLOW); container.add(panelY);
		    JPanel panelB=new JPanel(); panelB.setBackground(Color.BLUE); container.add(panelB);
		    JPanel panelG=new JPanel(); panelG.setBackground(Color.GREEN); container.add(panelG);
		    JPanel panelR=new JPanel(); panelR.setBackground(Color.RED); container.add(panelR);
		    JPanel panelO=new JPanel(); panelO.setBackground(Color.ORANGE); container.add(panelO);
		    frmCP.setContentPane(container);

			//-- Mouse Listener on ColorPicker panel
			container.addMouseListener(new java.awt.event.MouseAdapter(){
				public void mouseClicked(java.awt.event.MouseEvent evt){
					int currX=MouseInfo.getPointerInfo().getLocation().x - frmCP.getX();
					int currY=MouseInfo.getPointerInfo().getLocation().y - frmCP.getY();
					if(!SwingUtilities.isRightMouseButton(evt)){
						colorCell.x=Math.floor(currX/50);
						colorCell.y=Math.floor(currY/50);
						if(colorCell.x==0 && colorCell.y==0) PickedColor.Set(MyColor.WHITE);
						if(colorCell.x==1 && colorCell.y==0) PickedColor.Set(MyColor.YELLOW);
						if(colorCell.x==0 && colorCell.y==1) PickedColor.Set(MyColor.BLUE);
						if(colorCell.x==1 && colorCell.y==1) PickedColor.Set(MyColor.GREEN);
						if(colorCell.x==0 && colorCell.y==2) PickedColor.Set(MyColor.RED);
						if(colorCell.x==1 && colorCell.y==2) PickedColor.Set(MyColor.ORANGE);
						//System.out.println("ColorPicker: mouseClicked at ("+currY+","+currX+") => "+PickedColor.Desc);	
						isPicked=true;
						frmCP.setVisible(false);
					}
				}
			});	
		}
		
	}	

	class CustomItemListener implements ItemListener{
		public void itemStateChanged(ItemEvent ev){
			JToggleButton btn = (JToggleButton)ev.getSource();
			//System.out.println(btn.getText()+" ; "+btn.isSelected() + ev.getStateChange());
			if(btn.getText()=="Set F"){
				FreezeFace(CubeFace.F);
			} else if(btn.getText()=="Set R"){
				FreezeFace(CubeFace.R);
			} else if(btn.getText()=="Set B"){
				FreezeFace(CubeFace.B);
			} else if(btn.getText()=="Set L"){
				FreezeFace(CubeFace.L);
			} else if(btn.getText()=="Set U"){
				FreezeFace(CubeFace.U);
			} else if(btn.getText()=="Set D"){
				FreezeFace(CubeFace.D);
			}
		}
	}
	
	class CustomActionListener implements ActionListener{
		public void actionPerformed(ActionEvent evt){
			if (evt.getActionCommand()=="Quit"){
				System.out.println("Exiting...");
			    //-- free resources
				feed.release();
			    frmMyColorDetector.dispose();
			    MyCP.frmCP.dispose();
			} else if (evt.getActionCommand()=="Proceed"){
				cube.setCells(cellColor);
			} else if(evt.getActionCommand()=="Unlock"){
				for(int y=0;y<cube.cubeSize;y++){
					for(int x=0;x<cube.cubeSize;x++){
						cellColor[DECODED][y][x].Free();
					}
				}
			}
		}
	}

	public void FreezeFace(int pFace){
		//-- Freeze can only happen if all cells have a color
		boolean freezeable=true;
		for(int y=0;y<cube.cubeSize;y++){
			for(int x=0;x<cube.cubeSize;x++){
				if(cellColor[DECODED][y][x].Id==MyColor.BLACK) freezeable=false;
			}
		}
		
		if(freezeable){
			//-- first, copy cell colors from DECODED panel to <pFace> panel
			for(int y=0;y<cube.cubeSize;y++){
				for(int x=0;x<cube.cubeSize;x++){
					cellColor[pFace][y][x].Set(cellColor[DECODED][y][x].Id);
					System.out.println("cellColor["+pFace+"]["+y+"]["+x+"]="+cellColor[pFace][y][x].Desc);
				}
			}
			//-- then, freeze panel
			retp[pFace].isFrozen=!retp[pFace].isFrozen;
		} else{
			JOptionPane.showMessageDialog(new JFrame(),  "Cannot Freeze. Please check that all cells are set.", "Error", JOptionPane.ERROR_MESSAGE);
		}

	}

	class retPanel extends JPanel{
		private static final long serialVersionUID = 1L;

		private int Face;
		String FaceDesc;
		boolean isFrozen;
		
		public void DrawFrozenFace(int pFace){	
			Face=pFace;
			FaceDesc=CubeFace.Id2Desc(pFace);
		}

		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			
			int cellSize=Math.min(this.getWidth(), this.getHeight())/cube.cubeSize;

			if(this.isFrozen){
				for(int y=0;y<cube.cubeSize;y++){
					for(int x=0;x<cube.cubeSize;x++){
						// paint body
						g.setColor(cellColor[Face][y][x].Java);
						g.fillRect(x*cellSize, y*cellSize, cellSize, cellSize);
						// draw border
						g.setColor(Color.BLACK);
						g.drawRect(x*cellSize, y*cellSize, cellSize, cellSize);
					}
				}
			}
		}
	}
	
	class imgPanel extends JPanel{
		private static final long serialVersionUID=1L;
		private BufferedImage image;
		//public imgPanel(){ super(); }
		private BufferedImage getimage(){ return image; }
		public void setimage(BufferedImage newimage){image=newimage; return; }
		//-- called method
		public void setimagewithMat(Mat newimage){
			image=matToBufferedImage(newimage); 
			return; 
		}		
		//--
		//--
		public BufferedImage matToBufferedImage(Mat matrix) {  
		     int cols = matrix.cols();  
		     int rows = matrix.rows();  
		     int elemSize = (int)matrix.elemSize();  
		     byte[] data = new byte[cols * rows * elemSize];  
		     int type;  
		     matrix.get(0, 0, data);  
		     switch (matrix.channels()) {  
		       case 1:  
		         type = BufferedImage.TYPE_BYTE_GRAY;  
		         break;  
		       case 3:  
		         type = BufferedImage.TYPE_3BYTE_BGR;  
		         // bgr to rgb  
		         byte b;  
		         for(int i=0; i<data.length; i=i+3) {  
		           b = data[i];  
		           data[i] = data[i+2];  
		           data[i+2] = b;  
		         }  
		         break;  
		       default:  
		         return null;  
		     }  
		     BufferedImage image2 = new BufferedImage(cols, rows, type);  
		     image2.getRaster().setDataElements(0, 0, cols, rows, data);
		     return image2;  
		   }
		
		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			BufferedImage temp=getimage();
			if(temp!=null){
				g.drawImage(temp,  0,  0,  temp.getWidth(), temp.getHeight(), this);
			}
		}
	}
	
	public void paintComponent(Graphics g){
		this.paintComponent(g);
	}
}
