package display.containers;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

public class WarningPanel extends PopupPanel {
	protected static final String HEADER = "Warning";
	private String message;
	
	public WarningPanel(String txt){
		super(HEADER);
		
		lblWarning = new JLabel("");
		mainPanel.add(lblWarning, "cell 0 0,aligny center");
		setWarning(txt);
		lblWarning.setVisible(true);
		
		JButton btnOk = new JButton("Ok");
		btnOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getPopupWindow().hide();
			}
		});
		add(btnOk, "cell 1 1,growx");
		
	}
}
