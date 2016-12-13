package my3d;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Crectangle extends Object3d{
	int width;
	int height;
	int clr;
	String label;
	BufferedImage bi;
	
	// constructor
	public Crectangle(int h, int w, int c){
		Crectangle_Constructor(h, w, c, null);
	}
	public Crectangle(int h, int w, int c, String plabel){
		Crectangle_Constructor(h, w, c, plabel);
	}
	// actual constructor routine
	void Crectangle_Constructor(int h, int w, int c, String plabel){
		// Rectangle lies flat on xz plane, starts at 0,0, extends in positive values space. Points are ordered counterclockwise from 0,0
		width=w;
		height=h;
		clr=c;
		label=plabel;
		
		drawLabel(label);		
		drawPoints();
	}
	
	public void Rotate(int alpha, int beta, int gamma) {
		for(int i=0; i<pCount; i++) Core.RotatePoint(p[i], alpha, beta, gamma);
		//for(int i=0; i<pCount; i++) p[i].Rotate(alpha, beta, gamma);
	}

	public void Traslate(int tx, int ty, int tz) {
		//for(int i=0; i<pCount; i++) Core.TraslatePoint(p[i], tx, ty, tz);
		for(int i=0; i<pCount; i++) p[i].Traslate(tx, ty, tz);
	}
	
	public void setColor(int pclr){
		clr=pclr; 
		for(int i=0; i<pCount; i++){
			if(p[i].clr!=MyColor.BLACK) p[i].setColor(clr);		
		}
	}
	
	public void Project(Scene3D s){
		for(int i=0; i<pCount; i++) p[i].Project(s);
	}
	
	private void drawPoints(){
		pCount=0;
		p=new MyPoint[width*height];
		int pclr; int prgb; Color jclr; 
		for(int x=0; x<width; x++){
			for(int z=0; z<height; z++){
				p[pCount]=new MyPoint(x,0,z);
				prgb=bi.getRGB(x, z);
				jclr=new Color(prgb);
				pclr=(label!=null)?MyColor.J2Id(jclr):clr;
				p[pCount].setColor(pclr);
				pCount++;
			}
		}		
	}
	
	private void drawLabel(String lbl){
		// insert label into rectangle, before taking its points
		bi=new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d=bi.createGraphics();
		g2d.drawImage(bi, 0, 0, null);
		g2d.setColor(MyColor.Id2J(clr));
		g2d.fillRect(0, 0, width, height);
		if(lbl!=null){
			g2d.setPaint(Color.BLACK);
			g2d.setFont(new Font("Serif", Font.BOLD, 18));
			g2d.drawString(lbl, (width-30)/2,height/2);
			g2d.dispose();
		}
	}
}
