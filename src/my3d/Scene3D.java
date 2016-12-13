package my3d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

public class Scene3D extends JPanel{
	private static final long serialVersionUID = 1L;

	public Camera viewPoint;
	MyDimension scene2dSize;
	float AxisCoverage=0.8f;
	double[][] DepthBuffer;
	int[][] FrameBuffer;
	BufferedImage sceneImage;
	public static final int SCENE_MAXHEIGHT=800; 
	public static final int SCENE_MAXWIDTH=800; 
	
	//-- Z-Ordering
	public static final boolean ZORDER_ENABLE=true;
	public static final boolean ZORDER_DISABLE=false;
	boolean ZOrderType=ZORDER_ENABLE;
	public void setZOrderUsage(boolean use){ ZOrderType= use; }
	//--
	
	//-- Scene contents
	ArrayList<Object> sceneObject;

	//-- Projection Types
	public static final int ORTOGRAPHIC=0;
	public static final int PERSPECTIVE=1;
	int ProjectionType=PERSPECTIVE;
	int OffsetX, OffsetY, OffsetZ, Scale;
	public void setProjectionType(int p){ProjectionType=p;}
	
	//-- Draw Modes
	public static final int DRAWMODE_CONTOURS=0;
	public static final int DRAWMODE_FILL=1;	
	int drawMode=DRAWMODE_FILL;
	public void setDrawMode(int mode){ drawMode=mode; }
	
	//-- constructor
	public Scene3D(int camPosX, int camPosY, int camPosZ, int camAlpha, int camBeta, int camGamma, int speed){
		viewPoint=new Camera(this, camPosX, camPosY, camPosZ, camAlpha, camBeta, camGamma);
		initScene();
		sceneObject=new ArrayList<Object>();
		
		this.setBackground(Color.GRAY); 
		this.setPreferredSize(new Dimension(500,500));
	}
	
	public void add(Object o){
		sceneObject.add(o);
	}
	
	public void setAxisCoverage(float ac){ AxisCoverage= ac; }
	void DrawAxis(Graphics g){
		//-- x,y,z Len are expressed in 
		MyPoint x1=new MyPoint();
		MyPoint x2=new MyPoint();
		MyPoint y1=new MyPoint();
		MyPoint y2=new MyPoint();
		MyPoint z1=new MyPoint();
		MyPoint z2=new MyPoint();
		MyPoint origin=new MyPoint();
		if(AxisCoverage>0){
			x1.x_3d=(int)(-scene2dSize.x*AxisCoverage/2); x1.y_3d=0; x1.z_3d=0; 
			x2.x_3d=(int)(scene2dSize.x*AxisCoverage/2); x2.y_3d=0; x2.z_3d=0;
			y1.x_3d=0; y1.y_3d=(int)(scene2dSize.y*AxisCoverage/2); y1.z_3d=0;
			y2.x_3d=0; y2.y_3d=(int)(-scene2dSize.y*AxisCoverage/2); y2.z_3d=0;
			z1.x_3d=0; z1.y_3d=0; z1.z_3d=(int)(-scene2dSize.z*AxisCoverage/2);
			z2.x_3d=0; z2.y_3d=0; z2.z_3d=(int)(scene2dSize.z*AxisCoverage/2);
			origin.x_3d=0; origin.y_3d=0; origin.z_3d=0;
			
			Drawd3dLine(g, x1,x2, MyColor.RED);	// X axis
			Drawd3dLine(g, y1,y2, MyColor.BLUE);	// Y axis
			Drawd3dLine(g, z1,z2, MyColor.GREEN);	// Z axis
			Draw3dPoint(g, origin, 8, MyColor.BLACK); // origin
			
			Draw3dText(g, x1, "L", 10, MyColor.GREEN);
			Draw3dText(g, x2, "R", 10, MyColor.BLUE);
			Draw3dText(g, y1, "F", 10, MyColor.RED);
			Draw3dText(g, y2, "B", 10, MyColor.ORANGE);
			Draw3dText(g, z1, "D", 10, MyColor.YELLOW);
			Draw3dText(g, z2, "U", 10, MyColor.WHITE);
		}
		
	}
	
	void Draw3dPoint(Graphics g, MyPoint p, int psize, int c){
		p.Project(this);
		g.setColor(MyColor.Id2J(c));
		g.fillOval((int)p.x_2ds-psize/2, (int)p.y_2ds-psize/2, psize, psize);
	}
	void Drawd3dLine(Graphics g, MyPoint p1, MyPoint p2, int c){
		
		p1.Project(this);
		p2.Project(this);
		
		g.setColor(MyColor.Id2J(c));
		g.drawLine((int)p1.x_2ds,   (int)p1.y_2ds,   (int)p2.x_2ds, (int)p2.y_2ds);
		g.fillOval((int)p1.x_2ds-2, (int)p1.y_2ds-2, 4,4);
	}
	void Draw3dText(Graphics g, MyPoint p0, String txt, int tsize, int clr){
		
	}

	void initScene(){
		//-- 1. reset scene
		scene2dSize=new MyDimension(700,700,700);
		
		//-- 2. reset buffer values and scene image
		DepthBuffer=new double[scene2dSize.x][scene2dSize.y];
		FrameBuffer=new int[scene2dSize.x][scene2dSize.y];
		sceneImage=new BufferedImage(scene2dSize.x,scene2dSize.y, BufferedImage.TYPE_INT_RGB);
		for(int x=0; x<scene2dSize.x; x++){
			for(int y=0; y<scene2dSize.y; y++){
				DepthBuffer[x][y]=0;
				FrameBuffer[x][y]=MyColor.GRAY;
			}			
		}
	}
	//-- scene Draw
	void DrawScene(Graphics g){			
		int x,y;
		initScene();
		//-- draw scene boundary
		g.setColor(MyColor.Id2J(MyColor.BLACK));
		g.drawRect(+this.getWidth()/2-scene2dSize.x/2, this.getHeight()/2-scene2dSize.y/2, scene2dSize.x, scene2dSize.y);
		for(int i=0; i<sceneObject.size(); i++){
			
			//-- Common to all Object3d objects
			Object3d o=(Object3d)sceneObject.get(i);
			for(int pi=0; pi<o.pCount; pi++){	
				// calc Z
				o.p[pi].Project(this);
				// ignore point if it gets outside scene
				if(o.p[pi].x_2ds>=0 && o.p[pi].x_2ds<scene2dSize.x && o.p[pi].y_2ds>=0 && o.p[pi].y_2ds<scene2dSize.y){
					// evaluate cube's Z vs. Scene
					if(o.p[pi].z_2d > DepthBuffer[ o.p[pi].x_2ds ] [ o.p[pi].y_2ds ] ){
						DepthBuffer[ o.p[pi].x_2ds ] [ o.p[pi].y_2ds ] = o.p[pi].z_2d;
						FrameBuffer[ o.p[pi].x_2ds ] [ o.p[pi].y_2ds ] = o.p[pi].clr;
					}
				}
			}
			//-- then draw scene pixel by pixel
			for(x=0; x<scene2dSize.x; x++){
				for(y=0; y<scene2dSize.y; y++){
					sceneImage.setRGB(x, y, MyColor.Id2J(FrameBuffer[x][y]).getRGB());
				}
			}
			g.drawImage(sceneImage, 0,  0, this);
		}
		//-- draw axis at the end, so axis don't get eliminated by z-buffering
		DrawAxis(g);
/*		
		Object3d o0=(Object3d)sceneObject.get(0);
		Object3d o1=(Object3d)sceneObject.get(1);
		g.setFont(new Font("Serif", Font.PLAIN, 26));
		g.drawString("Object 0 Depth (z_2d) at pi=525 = "+o0.p[525].z_2d, 50,50);
		g.drawString("Object 1 Depth (z_2d) at pi=525 = "+o1.p[525].z_2d, 50,100);
*/
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		DrawScene(g);
	}

	//=== Camera has become a subclass of Scene3D
	public class Camera{
		//-- parameters for Perspective Projection
		Scene3D scene;
		public MyPoint Position;
		public int Alpha;
		public int Beta;
		public int Gamma;
		int FocalDistance;

		//-- constructor
		public Camera(Scene3D scene, int initX, int initY, int initZ, int rotX, int rotY, int rotZ){
			this.scene=scene;
			Position=new MyPoint(initX, initY, initZ);
			setCameraPosX(initX);
			setCameraPosY(initY);
			setCameraPosZ(initZ);
			setCameraAlpha(rotX);
			setCameraBeta(rotY);
			setCameraGamma(rotZ);
		}

		//-- camera parameters setters
		public void setCameraPosX(int posX){ Position.setX(posX); scene.repaint();}
		public void setCameraPosY(int posY){ Position.setY(posY); scene.repaint(); }
		public void setCameraPosZ(int posZ){ Position.setZ(posZ); scene.repaint(); }
		public void setCameraAlpha(int alpha){ Alpha= alpha; scene.repaint();}
		public void setCameraBeta(int beta){ Beta= beta; scene.repaint();}
		public void setCameraGamma(int gamma){ Gamma= gamma; scene.repaint();}
	}
}
	
