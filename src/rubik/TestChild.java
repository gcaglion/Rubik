
package rubik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
@SuppressWarnings("unused")

public class TestChild extends JPanel{
	private static final long serialVersionUID = 1L;

	TestChild(){
		
		JPanel topPanel=new JPanel(); topPanel.setBackground(Color.GREEN);
		JPanel cmdPane=new JPanel(); cmdPane.setBackground(Color.RED);
		JPanel slnPane=new JPanel(); slnPane.setBackground(Color.BLUE);
		JPanel bottomPanel=new JPanel();
		//cubePane.add(new Scene2d());
		//cubePane.setPreferredSize(new Dimension(500,100));

/*
		GridBagConstraints cc=new GridBagConstraints(); cc.fill=GridBagConstraints.BOTH;
		setLayout(childLayout);
		cc.gridy=0; cc.gridx=0; cc.gridwidth=2; cc.weighty=0.8; cc.weightx=0.9; 	add(cubePane, cc);
		cc.gridy=1; cc.gridx=0; cc.gridwidth=1; cc.weighty=0.2; cc.weightx=0.5; 	add(cmdPane, cc);
		cc.gridy=1; cc.gridx=1; cc.gridwidth=1; cc.weighty=0.2; cc.weightx=0.5; 	add(slnPane, cc);
 */

/*		
		setLayout(new BorderLayout());
		add(cubePane, BorderLayout.NORTH);
		add(cmdPane, BorderLayout.SOUTH);
		add(slnPane, BorderLayout.SOUTH);
*/
		//topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		//topPanel.add(new Scene2d());
		
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.add(cmdPane);
		bottomPanel.add(slnPane);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new Scene2d());
		//add(topPanel);
		add(bottomPanel);
		
	}
	
	class Scene2d extends JPanel{
		private static final long serialVersionUID = 1L;

		Scene2d(){ this.setBackground(Color.CYAN); }
		
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			g.setColor(Color.BLACK);
			g.fillRect(10, 10, 100, 100);
		}
	}


}
