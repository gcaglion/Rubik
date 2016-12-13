package rubik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import my3d.MyColor;

class Scene2D extends JPanel{
	private static final long serialVersionUID = 1L;

	Rubik cube;
	
	Scene2D(Rubik cube){
		this.cube=cube;
		
		setBackground(Color.CYAN);
		setLayout(new BorderLayout());
		int totW=4*(cube.cubeSize*cube.cellSize +cube.cellGap);
		int totH=3*(cube.cubeSize*cube.cellSize +cube.cellGap);
		setPreferredSize(new Dimension(totW, totH));
	}
	
	private void DrawFace(Graphics g, String FaceDesc, int posx, int posy, Ccell[][] cell){
		for(int y=0; y<cube.cubeSize; y++){
			for(int x=0; x<cube.cubeSize; x++){
				// body
				g.setColor(MyColor.Id2J(cell[y][x].clr));
				g.fillRect(posx+x*cube.cellSize, posy+y*cube.cellSize, cube.cellSize, cube.cellSize);
				// border
				g.setColor(Color.BLACK);
				g.drawRect(posx+x*cube.cellSize, posy+y*cube.cellSize, cube.cellSize, cube.cellSize);
			}
		}		
		// id
		g.drawString(FaceDesc, posx+cube.cellSize*cube.cubeSize/2, posy+cube.cellSize*cube.cubeSize/2);
	}

	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		if(cube==null) return;
		
		int totW=4*(cube.cubeSize*cube.cellSize +cube.cellGap);
		int totH=3*(cube.cubeSize*cube.cellSize +cube.cellGap);
		//setPreferredSize(new Dimension(totW, totH));
		int x0=(this.getWidth() -totW)/2;
		int y0=(this.getHeight()-totH)/2;
		
		DrawFace(g, "U", x0+2*cube.cellGap+cube.cubeSize*cube.cellSize, 	y0+cube.cellGap, 										cube.cell[CubeFace.U]);								// Draw U
		DrawFace(g, "L", x0+cube.cellGap,										y0+2*cube.cellGap+cube.cubeSize*cube.cellSize,	cube.cell[CubeFace.L]);								// Draw L
		DrawFace(g, "F", x0+2*cube.cellGap+cube.cubeSize*cube.cellSize,	y0+2*cube.cellGap+cube.cubeSize*cube.cellSize,	cube.cell[CubeFace.F]);	// Draw F
		DrawFace(g, "R", x0+3*cube.cellGap+2*cube.cubeSize*cube.cellSize,	y0+2*cube.cellGap+cube.cubeSize*cube.cellSize,	cube.cell[CubeFace.R]);	// Draw R
		DrawFace(g, "B", x0+4*cube.cellGap+3*cube.cubeSize*cube.cellSize,	y0+2*cube.cellGap+cube.cubeSize*cube.cellSize,	cube.cell[CubeFace.B]);	// Draw B
		DrawFace(g, "D", x0+2*cube.cellGap+cube.cubeSize*cube.cellSize,	y0+3*cube.cellGap+2*cube.cubeSize*cube.cellSize,	cube.cell[CubeFace.D]);	// Draw D
		
	}
}
