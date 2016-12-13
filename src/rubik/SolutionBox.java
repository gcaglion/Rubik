package rubik;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

public class SolutionBox extends JPanel {
	private static final long serialVersionUID = 1L;

	JTextArea slnTxtArea;
	
	SolutionBox(){
		//JPanel slnPane=new JPanel(); slnPane.setLayout(new BorderLayout());
		//this.setPreferredSize(new Dimension(400,120));
		TitledBorder bpaneborder = new TitledBorder("Solution");
		bpaneborder.setTitleJustification(TitledBorder.CENTER);
		bpaneborder.setTitlePosition(TitledBorder.TOP);
		this.setBorder(bpaneborder);
	    
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS)); 
		slnTxtArea=new JTextArea();
		slnTxtArea.setLineWrap(true);
		//slnTxtArea.setPreferredSize(new Dimension(80,10));
		JScrollPane scroll=new JScrollPane(slnTxtArea);
		scroll.setPreferredSize(new Dimension(80,10));
		
		this.add(scroll);
	}
	
	void addText(String txt){ slnTxtArea.setText(slnTxtArea.getText().concat(txt)); }
	void reset(){ slnTxtArea.setText(""); }
}
