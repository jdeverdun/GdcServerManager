package display.containers;

import javax.swing.JPanel;

import model.ProjectStatistics;
import net.miginfocom.swing.MigLayout;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JSplitPane;
import java.awt.ComponentOrientation;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.UIManager;

public class StatisticsPanel extends JPanel{

	private ProjectStatistics stats;
	
	
	private JLabel lblProjectname;
	private JTextField txtNpatients;
	private JTextField textMeanage;
	private JTextField textStdAge;
	private JTextField textMaxAge;
	private JTextField textMinAge;
	private JTextField textFirstAcq;
	private JTextField textLastAcq;
	private JTextField textAcqNumber;
	
	public StatisticsPanel(ProjectStatistics stats){
		this.stats = stats;
		setLayout(new MigLayout("", "[grow]", "[][grow][grow]"));
		
		JSeparator separator = new JSeparator();
		add(separator, "flowx,cell 0 0,growx");
		
		lblProjectname = new JLabel(stats.getProject().getNom());
		add(lblProjectname, "cell 0 0,alignx center");
		
		JSeparator separator_1 = new JSeparator();
		add(separator_1, "cell 0 0,growx");
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, "cell 0 1,grow");
		
		JPanel panelPopulation = new JPanel();
		splitPane.setLeftComponent(panelPopulation);
		panelPopulation.setLayout(new MigLayout("", "[grow][grow][grow]", "[][grow]"));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "General", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelPopulation.add(panel_1, "cell 0 0,grow");
		panel_1.setLayout(new MigLayout("", "[][]", "[][][][]"));
		
		JLabel lblSubjectsNumber = new JLabel("Subjects number");
		panel_1.add(lblSubjectsNumber, "cell 0 0");
		
		txtNpatients = new JTextField();
		panel_1.add(txtNpatients, "cell 1 0");
		txtNpatients.setEditable(false);
		txtNpatients.setHorizontalAlignment(SwingConstants.CENTER);
		txtNpatients.setText(""+stats.getNpatients());
		txtNpatients.setColumns(10);
		
		JLabel labelNAcq = new JLabel("Acquisition number");
		panel_1.add(labelNAcq, "cell 0 1");
		
		textAcqNumber = new JTextField();
		panel_1.add(textAcqNumber, "cell 1 1");
		textAcqNumber.setEditable(false);
		textAcqNumber.setText(""+stats.getNacquisitions());
		textAcqNumber.setHorizontalAlignment(SwingConstants.CENTER);
		textAcqNumber.setColumns(10);
		
		JLabel lblFirstAcq = new JLabel("First Acquisition");
		panel_1.add(lblFirstAcq, "cell 0 2");
		
		textFirstAcq = new JTextField();
		panel_1.add(textFirstAcq, "cell 1 2");
		textFirstAcq.setEditable(false);
		textFirstAcq.setText(stats.getFirstAcquisitionDate().toString());
		textFirstAcq.setHorizontalAlignment(SwingConstants.CENTER);
		textFirstAcq.setColumns(10);
		
		JLabel lblLastAcquisition = new JLabel("Last Acquisition");
		panel_1.add(lblLastAcquisition, "cell 0 3");
		
		textLastAcq = new JTextField();
		panel_1.add(textLastAcq, "cell 1 3");
		textLastAcq.setEditable(false);
		textLastAcq.setText(stats.getLastAcquisitionDate().toString());
		textLastAcq.setHorizontalAlignment(SwingConstants.CENTER);
		textLastAcq.setColumns(10);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Age", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelPopulation.add(panel, "cell 1 0,grow");
		panel.setLayout(new MigLayout("", "[][]", "[][][][]"));
		
		JLabel lblMeanAge = new JLabel("Mean");
		panel.add(lblMeanAge, "cell 0 0");
		
		textMeanage = new JTextField();
		textMeanage.setEditable(false);
		panel.add(textMeanage, "cell 1 0");
		textMeanage.setHorizontalAlignment(SwingConstants.CENTER);
		textMeanage.setText(""+stats.getMeanAge());
		textMeanage.setColumns(10);
		
		JLabel lblStdAge = new JLabel("Std");
		panel.add(lblStdAge, "cell 0 1");
		
		textStdAge = new JTextField();
		textStdAge.setEditable(false);
		panel.add(textStdAge, "cell 1 1");
		textStdAge.setHorizontalAlignment(SwingConstants.CENTER);
		textStdAge.setText(""+stats.getStdAge());
		textStdAge.setColumns(10);
		
		JLabel lblMaxAge = new JLabel("Max");
		panel.add(lblMaxAge, "cell 0 2");
		
		textMaxAge = new JTextField();
		textMaxAge.setEditable(false);
		panel.add(textMaxAge, "cell 1 2");
		textMaxAge.setHorizontalAlignment(SwingConstants.CENTER);
		textMaxAge.setText(""+stats.getMaxAge());
		textMaxAge.setColumns(10);
		
		JLabel lblMinAge = new JLabel("Min");
		panel.add(lblMinAge, "cell 0 3");
		
		textMinAge = new JTextField();
		textMinAge.setEditable(false);
		panel.add(textMinAge, "cell 1 3");
		textMinAge.setHorizontalAlignment(SwingConstants.CENTER);
		textMinAge.setText(""+stats.getMinAge());
		textMinAge.setColumns(10);
		
		JPanel panelData = new JPanel();
		splitPane.setRightComponent(panelData);
		panelData.setBorder(new TitledBorder(null, "Sequence count", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelData.setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		JPanel panel_2 = new JPanel();
		//panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Sequences count", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setLayout(new MigLayout("", "[]", "[]"));
		JScrollPane jsp = new JScrollPane(panel_2);
		TreeMap<String,Integer> countPerSeq = stats.getCountPerSequence();
		int line = 0;
		int bcol = 0;
		int itemPerCol = countPerSeq.size()/3; 
		int count = 0;
		for(String s:countPerSeq.keySet()){
			if(count>itemPerCol){
				bcol+=3;
				line=0;
				count=0;
				JSeparator separator_2 = new JSeparator();
				separator_2.setOrientation(SwingConstants.VERTICAL);
				panel_2.add(separator_2, "cell "+(bcol-1)+" 0 "+(bcol-1)+" "+itemPerCol+",growy");
				panel_2.revalidate();
				
			}
			JLabel cjl = new JLabel(s);
			panel_2.add(cjl, "cell "+bcol+" "+line);
			panel_2.revalidate();
			JTextField cjt = new JTextField(""+countPerSeq.get(s));
			cjt.setColumns(10);
			cjt.setEditable(false);
			panel_2.add(cjt, "cell "+(bcol+1)+" "+line);
			panel_2.revalidate();
			line++;
			count++;
			
		}
		panelData.add(jsp, "cell 0 0,grow");
	}
}
