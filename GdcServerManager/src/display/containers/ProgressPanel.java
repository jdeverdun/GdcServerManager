package display.containers;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;

import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.Timeline.RepeatBehavior;
import org.pushingpixels.trident.interpolator.*;
import org.pushingpixels.trident.swing.SwingRepaintTimeline;

public class ProgressPanel extends JPanel implements MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8350770509778064144L;

	private static int INNER_HEIGHT = 60;

	private static int INNER_WIDTH = 350;

	private static final int HIGHLIGHTER_HEIGHT = 2;

	private static final int HIGHLIGHTER_WIDTH = 58;

	private int xPosition;

	private float alpha;
	
	private Timeline progressTimeline;

	private Popup popup;
	
	private String comment; //phrase definissant le status

	public ProgressPanel() {
		this.xPosition = 0;
		this.alpha = 0;
		comment = "";
		this.addMouseListener(this);
		new SwingRepaintTimeline(this).playLoop(RepeatBehavior.LOOP);
		setMaximumSize(new Dimension(500,2));
	}

	public void start() {
		progressTimeline = new Timeline(this);

		int startX = (getWidth() - INNER_WIDTH) / 2 + 18
				+ HIGHLIGHTER_WIDTH / 2;
		int endX = (getWidth() + INNER_WIDTH) / 2 - 18 - HIGHLIGHTER_WIDTH
				/ 2;
		progressTimeline
				.addPropertyToInterpolate("xPosition", startX, endX);

		KeyValues<Float> alphaValues = KeyValues.create(0.0f, 1.0f, 1.0f,
				0.0f);
		KeyTimes alphaTimes = new KeyTimes(0.0f, 0.3f, 0.7f, 1.0f);
		progressTimeline.addPropertyToInterpolate("alpha",
				new KeyFrames<Float>(alphaValues, alphaTimes));

		progressTimeline.setDuration(1500);
		progressTimeline.playLoop(RepeatBehavior.LOOP);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();

		// Full background
		g2d.setColor(new Color(12, 12, 12));
		g2d.fillRect(0, 0, w, h);

		// Inner gradient fill
		Shape innerContour = new RoundRectangle2D.Double(
				(w - INNER_WIDTH) / 2.0, (h - INNER_HEIGHT) / 2.0,
				INNER_WIDTH, INNER_HEIGHT, 10, 10);
		g2d.setPaint(new GradientPaint((w - INNER_WIDTH) / 2.0f,
				(h - INNER_HEIGHT) / 2.0f, new Color(47, 47, 47),
				(w - INNER_WIDTH) / 2.0f, (h + INNER_HEIGHT) / 2.0f,
				Color.black));
		g2d.fill(innerContour);

		// Inner contour
		g2d.setColor(new Color(67, 67, 67));
		g2d.draw(innerContour);

		// Progress track
		/*int trackWidth = INNER_WIDTH - 36;
		int trackHeight = 1;
		g2d.setColor(new Color(91, 91, 91));
		for (int i = 2; i >= 0; i--) {
			Shape trackContour = new RoundRectangle2D.Double(
					(w - INNER_WIDTH) / 2 + 18 - i, h / 2 - i - 1,
					trackWidth + i * 2, trackHeight + i * 2, 2 * i, 2 * i);
			float alpha = 1.0f;
			if (i == 1)
				alpha = 0.3f;
			if (i == 2)
				alpha = 0.1f;
			g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
			g2d.draw(trackContour);
		}*/

		// Highlighter
		g2d.setColor(new Color(13, 106, 206));
		g2d.setComposite(AlphaComposite.SrcOver.derive(0.1f * this.alpha));
		for (int i = 6; i >= 0; i--) {
			g2d.fillOval(this.xPosition - HIGHLIGHTER_WIDTH / 2 - i, h / 2
					- HIGHLIGHTER_HEIGHT / 2 - i-1,
					HIGHLIGHTER_WIDTH + 2 * i, HIGHLIGHTER_HEIGHT + 2 * i);
		}

		// Highlighter on track
		g2d.setComposite(AlphaComposite.SrcOver.derive(this.alpha));
		g2d.setColor(new Color(136, 182, 231));
		g2d.fillRect(this.xPosition - 28, h / 2 - 2, 56, 2);

		g2d.dispose();
	}

	@Override
	public void setVisible(boolean isVisible){
		if(isVisible)
			this.start();
		else
			this.stop();
		super.setVisible(isVisible);
	}
	private void stop() {
		if(progressTimeline!=null)
			progressTimeline.abort();
	}

	public void setXPosition(int position) {
		xPosition = position;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	public static void setINNER_HEIGHT(int h){
		INNER_HEIGHT = h;
	}

	public void setINNER_WIDTH(int i) {
		INNER_WIDTH = i;
	}

	public void setPopupWindow(Popup popup) {
		this.popup = popup;
	}


	public void mouseClicked(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		
	}

	public void mouseReleased(MouseEvent e) {

	}
}
