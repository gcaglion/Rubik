package my3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Tester extends JPanel{
	private static final long serialVersionUID = 1L;

	Scene3D gui;

	DisplayPanel pnl;
	MyPoint[] p;
	int[] pxF;
	int[] pyF;
	int[] pxB;
	int[] pyB;
	int csize=100;
	int rotX=0; int rotY=0; int rotZ=0;
	int camX=0; int camY=0; int camZ=1000;
	Thread th;
	boolean paused;
	
	Tester(){
		JFrame f=new JFrame("Rgui3D client");
		f.setSize(800,800);
		f.setLayout(new BorderLayout());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		gui=new Scene3D(0,0,-3000,90,0,0, 25);
		Cbox b1=new Cbox(50,100,200,5, "Cbox"); 
		//b1.Traslate(100, 100, 20);
		//Crectangle r1=new Crectangle(100,200,MyColor.BLUE,"Crectangle");
		ObjectGroup g=new ObjectGroup();
		g.add(b1); 
		//g.add(r1);
		//Cbox b1=new Cbox(50,50,10, 4, null);
		//b1.setColor(MyColor.BLUE);
		gui.add(g);
		f.add(gui, BorderLayout.CENTER);
	
/*
		//Rubik cube=new Rubik(5,60,5,10,f,null);
		//Rgui3D gui=new Rgui3D(cube);

		
		gui=new Rgui3D(null);
		ObjectGroup g1=new ObjectGroup();
		
		Crectangle r1=new Crectangle(150, 150, MyColor.YELLOW);
		r1.Traslate(0, 0, 0);
		r1.Rotate(90, 0, 0);
		//g1.add(r1);
		//gui.myScene.add(r1);
		
		Crectangle r2=new Crectangle(150, 150, MyColor.RED); 
		//r2.Rotate(60, 0, 0);
		r2.Traslate(0, 0, 0); 
		g1.add(r2);
		//gui.myScene.add(r2);
		
		gui.myScene.add(g1);				
		f.add(gui, BorderLayout.CENTER);
*/		
		
//		for(int i=0; i<360; i++) System.out.println("cos("+i+")="+Math.cos(Math.toRadians(i)));
//		if(true) return;
/*		
		p  = new MyPoint[8];
		pxF = new int[8];
		pyF = new int[8];
		pxB = new int[8];
		pyB = new int[8];
		p[0]=new MyPoint(0,		0,		0);
		p[1]=new MyPoint(csize,	0,		0);
		p[2]=new MyPoint(csize,	-csize,	0);
		p[3]=new MyPoint(0,		-csize,	0);
		p[4]=new MyPoint(0,		0,		csize);
		p[5]=new MyPoint(csize,	0,		csize);
		p[6]=new MyPoint(csize,	-csize,	csize);
		p[7]=new MyPoint(0,		-csize,	csize);
		
		
		for(int i=0; i<8; i++){
			p  [i]=Rotate(p[i], rotX, rotY, rotZ);		// p[i].Rotate(rotX, rotY, rotZ)
			p  [i]=Project(p[i]);
		}
		
		pnl=new DisplayPanel(); pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		f.add(pnl);
		
		JButton button;
		button=new JButton("doMove R"); pnl.add(button);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				th=new Thread(){
					public void run(){
						paused=false;
						for(int r=0; r<=360; r++){
							rotZ++; if(rotZ>360) rotZ=0;
							for(int i=0; i<8; i++){
								p  [i]=Rotate(p[i], 0, 0, 1);
								p  [i]=Project(p[i]);
							}
							try{Thread.sleep(100);} catch (InterruptedException e){ e.printStackTrace();}
							pnl.repaint();
							
						}
					}
				}; th.start();
			}
		});
		button=new JButton("stepped"); pnl.add(button);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for(int i=0; i<8; i++){
					p  [i]=Rotate(p[i], rotX, rotY, 30);		// p[i].Rotate(rotX, rotY, rotZ)
					p  [i]=Project(p[i]);
				}
				pnl.repaint();
			}
		});
		
*/		
		
		f.setVisible(true);
	}

	
	MyPoint Rotate(MyPoint p, int alpha, int beta, int gamma){
		MyPoint ret=new MyPoint();
		double cx=Math.cos(Math.toRadians(alpha));
		double cy=Math.cos(Math.toRadians(beta));
		double cz=Math.cos(Math.toRadians(gamma));
		double sx=Math.sin(Math.toRadians(alpha));
		double sy=Math.sin(Math.toRadians(beta));
		double sz=Math.sin(Math.toRadians(gamma));
		
		ret.x_3d=(cy*(sz*p.y_3d+cz*p.x_3d)-sy*p.z_3d);
		ret.y_3d=(sx*(cy*p.z_3d+sy*(sz*p.y_3d+cz*p.x_3d))+cx*(cz*p.y_3d-sz*p.x_3d));
		ret.z_3d=(cx*(cy*p.z_3d+sy*(sz*p.y_3d+cz*p.x_3d))-sx*(cz*p.y_3d-sz*p.x_3d));
		
		return ret;
	}
	
	MyPoint Project(MyPoint p){
			int fd=1000;
			p.x_2d=p.x_3d*fd/(fd+p.z_3d);
			p.y_2d=p.y_3d*fd/(fd+p.z_3d);

			return p;
	}
	
	public static void main(String[] args){
		new Tester();
	}
	
	class DisplayPanel extends JPanel{
		private static final long serialVersionUID = 1L;

		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			
			for(int i=0; i<8; i++){
				p[i].x_2ds=(int)p[i].x_2d +getWidth()/2;
				p[i].y_2ds=getHeight()/2-(int)p[i].y_2d;
			}
			for(int i=0; i<4; i++){
				pxF[i]=p[i].x_2ds;
				pyF[i]=p[i].y_2ds;
			}
			for(int i=0; i<4; i++){
				pxB[i]=p[i+4].x_2ds;
				pyB[i]=p[i+4].y_2ds;
			}

			g.setColor(Color.BLACK);
			g.drawRect(this.getWidth()/2-csize+ (int)p[0].x_3d, this.getHeight()/2+ (int)p[0].y_3d, csize, csize);
			g.setColor(Color.RED);
			g.drawPolygon(pxF,  pyF, 4);
			//g.setColor(Color.BLUE);
			//g.drawPolygon(pxB,  pyB, 4);
			
			g.setFont(new Font("Serif", Font.PLAIN, 20));
			g.drawString("rotX="+rotX, 250, 20);
			g.drawString("p[2] .x_3d="+p[2].x_3d+" .y_3d="+p[2].y_3d+" .z_3d="+p[2].z_3d, 250,50);
			g.drawString("p[2] .x_2d="+p[2].x_2d+" .y_2d="+p[2].y_2d, 250,80);
			g.drawString("p[2] .x_2ds="+p[2].x_2ds+" .y_2ds="+p[2].y_2ds, 250,110);
		}
		
	}

}
