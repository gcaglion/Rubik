package rubik;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import my3d.Scene3D;

public class RConsole2D extends JPanel{
	private static final long serialVersionUID = 1L;

	Rubik cube;
	Scene2D gui2d;
	Scene3D gui3d;

	// constructor
	RConsole2D(Rubik pCube){
		cube=pCube;		
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints(); c.fill=GridBagConstraints.BOTH;
		
		JTextField cSizeTxt=new JTextField(Integer.toString(cube.cubeSize));
		c.gridwidth=1; c.gridy=1; c.gridx=0; this.add(cSizeTxt, c);

		JButton button=new JButton("Set Cube Size"); 
		c.gridwidth=1; c.gridy=1; c.gridx=1; this.add(button, c);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				RGui.init(
						Integer.parseInt(cSizeTxt.getText()), 
						//cube.cellSize,
						cube.cellGap, 
						cube.cellThickness, 
						(int)gui3d.viewPoint.Position.x_3d, 
						(int)gui3d.viewPoint.Position.y_3d, 
						(int)gui3d.viewPoint.Position.z_3d, 
						gui3d.viewPoint.Alpha, 
						gui3d.viewPoint.Beta, 
						gui3d.viewPoint.Gamma,
						5
						);
			}
		});

		JCheckBox cbShow3d=new JCheckBox("3D sync"); cbShow3d.setSelected(false);
		c.gridwidth=2; c.gridy=1; c.gridx=2; this.add(cbShow3d, c);
		cbShow3d.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				cube.setShow3D(cbShow3d.isSelected());
				if(cbShow3d.isSelected()) cube.Sync2D3D();
				cube.gui3d.repaint();
			}
		});

		button = new JButton("Read Cube");
		c.gridwidth=1; c.gridy=2; c.gridx=0; this.add(button, c);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new MyColorDetector(cube);				
			}
		});
		 
		button = new JButton("Solve!");
		c.gridwidth=1; c.gridy=2; c.gridx=2; this.add(button, c);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new Thread(new Runnable(){
				    public void run(){
				    	cube.Solve();
				    }
				}).start();
				
				repaintBoth();
			}
		});
		
		JTextField cmdTxt=new JTextField("FRBLUDXYZ"); cmdTxt.setPreferredSize(new Dimension(50,2));
		c.gridwidth=3; c.gridy=3; c.gridx=0; this.add(cmdTxt, c);

		button=new JButton("Move"); 
		c.gridwidth=1; c.gridy=3; c.gridx=3; this.add(button, c);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new Thread(new Runnable(){
				    public void run(){
						cube.doMove(cmdTxt.getText());
				    }
				}).start();
				
				repaintBoth();
			}
		});
		 
		JTextField ShfCntTxt=new JTextField("100");
		c.gridwidth=1; c.gridy=4; c.gridx=0; this.add(ShfCntTxt, c);
	
		button = new JButton("Shuffle");
		c.gridwidth=1; c.gridy=4; c.gridx=1; this.add(button, c);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new Thread(new Runnable(){
				    public void run(){
						cube.doShuffle(Integer.parseInt(ShfCntTxt.getText()));
				    }
				}).start();
			}
		});
		
		button = new JButton("Reset");
		c.gridwidth=1; c.gridy=4; c.gridx=2; this.add(button, c);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				cube.initCube();
				repaintBoth();
			}
		});
		
		button = new JButton("Exit");
		c.gridwidth=1; c.gridy=4; c.gridx=3; this.add(button, c);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});
	
		button = new JButton("KAZ");
		c.gridwidth=1; c.gridy=2; c.gridx=3; this.add(button, c);
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Thread t=new Thread(new Runnable(){
				    public void run(){
				    	// Kaz actions should go here!
			    		//for(int x=0; x<cube.cubeSize; x++) System.out.println("x="+x+" -> slice="+cube.x2Slice(x)+"\t ; mirror="+cube.x2MirrorSlice(x));
			    		//for(int y=0; y<cube.cubeSize; y++) System.out.println("y="+y+" -> slice="+cube.y2Slice(y, true)+"\t ; mirror="+cube.y2MirrorSlice(y));
				    	//cube.BuildEdges();
				    	//cube.doMove( cube.PBE_getSwitchFBmove() );
				    	//cube.OrientBottomEdges();
				    	//cube.doMove("R2R*2B2 R'R*'U2 R'R*'U2 B2R'R*' B2RR* B2R'R*' B2R2R*2 B2U2");	// SWAPS CENTRAL EDGES BETWEEN FU AND BU
				    	//cube.doMove("U2U*2B2 U'U*'L2 U'U*'L2 B2U'U*' B2UU* B2U'U*' B2U2U*2 B2L2");		// SWAPS CENTRAL EDGES BETWEEN FL AND BL
				    	cube.doMove("U2U*2B2 U'U*'L2 U'U*'L2 B2U'U*' B2UU* B2U'U*' B2U2U*2 B2L2");		// SWAPS CENTRAL EDGES BETWEEN 
				    }
				});
				t.setName("KAZ");
				t.start();
			}
		});
	}

	void setGui(Scene2D g2d, Scene3D g3d){
		gui2d=g2d;
		gui3d=g3d;
	}
	
	void repaintBoth(){
		gui2d.repaint();
		gui3d.repaint();
	}
	
}
