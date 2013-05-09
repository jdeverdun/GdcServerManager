package display.containers;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

public abstract class PopupPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8354078835544022052L;
	protected Popup popup;
	protected static String HEADERTXT = "<b>Generic text</b><br /> none.";
	protected JLabel descriptLabel;
	protected ProgressPanel progressPanel;
	protected JLabel lblWarning;
	protected JPanel mainPanel;
	
	protected PopupPanel(){
		setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new MigLayout("", "[89.00,grow][117.00,grow]", "[44.00,grow][][][][10.00]"));
		
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(mainPanel, "cell 0 0 2 1,grow");
		mainPanel.setLayout(new MigLayout("", "[100.00,grow,fill]", "[65.00,grow,fill]"));
		
	}
	protected PopupPanel(String header){
		setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new MigLayout("", "[89.00,grow][117.00,grow]", "[44.00,grow][][][][10.00]"));
		
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder(null, header, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(mainPanel, "cell 0 0 2 1,grow");
		mainPanel.setLayout(new MigLayout("", "[100.00,grow,fill]", "[65.00,grow,fill]"));
		
	}
	public void setPopupWindow(Popup popup) {
		this.popup = popup;
	}
	public Popup getPopupWindow(){
		return this.popup;
	}
	
	public JLabel getLblWarning() {
		return lblWarning;
	}
	public void setLblWarning(JLabel lblWarning) {
		this.lblWarning = lblWarning;
	}
	
	
	// affiche un warning dans lblWarning
	protected void setWarning(String txt){
		getLblWarning().setVisible(true);
		getLblWarning().setText("<html><font color=\"red\">"+txt+"</font></html>");
	}
	public JLabel getDescriptLabel() {
		return descriptLabel;
	}
	public void setDescriptLabel(JLabel descriptLabel) {
		this.descriptLabel = descriptLabel;
	}
	public ProgressPanel getProgressPanel() {
		return progressPanel;
	}
	public void setProgressPanel(ProgressPanel progressPanel) {
		this.progressPanel = progressPanel;
	}
	public JPanel getMainPanel() {
		return mainPanel;
	}
	public void setMainPanel(JPanel mainPanel) {
		this.mainPanel = mainPanel;
	}
}
