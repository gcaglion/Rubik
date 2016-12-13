package rubik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

import my3d.MyColor;

public class Rgui2D extends JPanel{
	private static final long serialVersionUID = 1L;

	static Rubik myCube;
	JPanel gui2d;
	JPanel gui3d;
	
	// constructor
	public Rgui2D(Rubik pmyCube) {
		myCube=pmyCube;
	}

	class Scene2d extends JPanel{
		private static final long serialVersionUID = 1L;

		Scene2d(int csize){
			setBackground(Color.CYAN);
			setLayout(new BorderLayout());
			int totW=4*(myCube.cubeSize*myCube.cellSize +myCube.cellGap);
			int totH=3*(myCube.cubeSize*myCube.cellSize +myCube.cellGap);
			setPreferredSize(new Dimension(totW, totH));
		}
		
		private void DrawFace(Graphics g, String FaceDesc, int posx, int posy, Ccell[][] cell){
			for(int y=0; y<myCube.cubeSize; y++){
				for(int x=0; x<myCube.cubeSize; x++){
					// body
					g.setColor(MyColor.Id2J(cell[y][x].clr));
					g.fillRect(posx+x*myCube.cellSize, posy+y*myCube.cellSize, myCube.cellSize, myCube.cellSize);
					// border
					g.setColor(Color.BLACK);
					g.drawRect(posx+x*myCube.cellSize, posy+y*myCube.cellSize, myCube.cellSize, myCube.cellSize);
				}
			}		
			// id
			g.drawString(FaceDesc, posx+myCube.cellSize*myCube.cubeSize/2, posy+myCube.cellSize*myCube.cubeSize/2);
		}

		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			if(myCube==null) return;
			
			int totW=4*(myCube.cubeSize*myCube.cellSize +myCube.cellGap);
			int totH=3*(myCube.cubeSize*myCube.cellSize +myCube.cellGap);
			//setPreferredSize(new Dimension(totW, totH));
			int x0=(this.getWidth() -totW)/2;
			int y0=(this.getHeight()-totH)/2;
			
			DrawFace(g, "U", x0+2*myCube.cellGap+myCube.cubeSize*myCube.cellSize, 	y0+myCube.cellGap, 										myCube.cell[CubeFace.U]);								// Draw U
			DrawFace(g, "L", x0+myCube.cellGap,										y0+2*myCube.cellGap+myCube.cubeSize*myCube.cellSize,	myCube.cell[CubeFace.L]);								// Draw L
			DrawFace(g, "F", x0+2*myCube.cellGap+myCube.cubeSize*myCube.cellSize,	y0+2*myCube.cellGap+myCube.cubeSize*myCube.cellSize,	myCube.cell[CubeFace.F]);	// Draw F
			DrawFace(g, "R", x0+3*myCube.cellGap+2*myCube.cubeSize*myCube.cellSize,	y0+2*myCube.cellGap+myCube.cubeSize*myCube.cellSize,	myCube.cell[CubeFace.R]);	// Draw R
			DrawFace(g, "B", x0+4*myCube.cellGap+3*myCube.cubeSize*myCube.cellSize,	y0+2*myCube.cellGap+myCube.cubeSize*myCube.cellSize,	myCube.cell[CubeFace.B]);	// Draw B
			DrawFace(g, "D", x0+2*myCube.cellGap+myCube.cubeSize*myCube.cellSize,	y0+3*myCube.cellGap+2*myCube.cubeSize*myCube.cellSize,	myCube.cell[CubeFace.D]);	// Draw D
		}
	}
}
