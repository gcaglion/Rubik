package rubik;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import my3d.Scene3D;

public class RConsole3D extends JPanel{
	private static final long serialVersionUID = 1L;

	Scene3D myScene;
	Rubik   myCube;

	JLabel lAlpha=new JLabel();
	JLabel lBeta=new JLabel();
	JLabel lGamma=new JLabel();
	JLabel lSpeed=new JLabel();
	
	JPanel pCenter=new JPanel();
	
	public RConsole3D (Rubik pCube, Scene3D pScene, int PosXMin, int PosXMax, int PosXInit, int PosYMin, int PosYMax, int PosYInit, int PosZMin, int PosZMax, int PosZInit, int AlphaMin, int AlphaMax, int AlphaInit, int BetaMin, int BetaMax, int BetaInit, int GammaMin, int GammaMax, int GammaInit, int SpeedMin, int SpeedMax, int SpeedInit){
		myCube=pCube;
		myScene=pScene;
		
		setBackground(Color.YELLOW);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JSlider slAlpha=new JSlider(JSlider.HORIZONTAL, AlphaMin, AlphaMax, AlphaInit); 
		JSlider slBeta=new JSlider(JSlider.HORIZONTAL, BetaMin, BetaMax, BetaInit); 
		JSlider slGamma=new JSlider(JSlider.HORIZONTAL, GammaMin, GammaMax, GammaInit); 
		JSlider slSpeed=new JSlider(JSlider.HORIZONTAL, SpeedMin, SpeedMax, SpeedInit); 

		MyChangeListener sll=new MyChangeListener();
		slAlpha.setName("slAlpha"); slAlpha.addChangeListener(sll);
		slBeta.setName("slBeta");   slBeta.addChangeListener(sll);
		slGamma.setName("slGamma"); slGamma.addChangeListener(sll);
		slSpeed.setName("slSpeed"); slSpeed.addChangeListener(sll);
		
		lAlpha.setText("rot X: "+AlphaInit);lBeta.setText("rot Y: "+BetaInit); lGamma.setText("rot Z: "+GammaInit); lSpeed.setText("3D speed: "+SpeedInit);
/*		
		JCheckBox cbShowlbl=new JCheckBox("cell labels"); cbShowlbl.setSelected(false);
		cbShowlbl.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				myCube.setShowlbl(cbShowlbl.isSelected());
				if(cbShowlbl.isSelected()) myCube.Sync2D3D();
				myCube.gui3d.repaint();
			}
		});
*/

		this.add(lAlpha); this.add(slAlpha); 
		this.add(lBeta);  this.add(slBeta);
		this.add(lGamma); this.add(slGamma);
		this.add(lSpeed); this.add(slSpeed);
		//this.add(cbShowlbl);
	}

	//-- change event handler (for sliders)
	class MyChangeListener implements ChangeListener{
		public void stateChanged(ChangeEvent e) {
		    JSlider source = (JSlider)e.getSource();
		    String slName=source.getName();
		    
		    if(slName.equals("slAlpha")){
		    	myScene.viewPoint.setCameraAlpha(source.getValue());
		    	lAlpha.setText("rot X: "+source.getValue());
		    } else if(slName.equals("slBeta")){
		    	myScene.viewPoint.setCameraBeta(source.getValue());
		    	lBeta.setText("rot Y: "+source.getValue());
		    } else if(slName.equals("slGamma")){
		    	myScene.viewPoint.setCameraGamma(source.getValue());
		    	lGamma.setText("rot Z: "+source.getValue());
		    } else if(slName.equals("slSpeed")){
		    	myCube.set3Dspeed(source.getValue());
		    	lSpeed.setText("3D speed: "+source.getValue());
		    }
		    repaint();
		}
}
	
}
