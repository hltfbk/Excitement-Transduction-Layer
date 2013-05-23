package eu.excitementproject.tl.structures.visualization;


import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class GraphSee extends JFrame {


    /**
	 * 
	 */
	private static final long serialVersionUID = 924027832829838299L;
//	private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 1000, 700 );

    // 

    public GraphSee() {
    	super("Graph visualization");
    	equipMe();
    }
    

    
	/**
	 * Creates visual elements.
	 */
	private void equipMe(String... param) {
		JTabbedPane main_pane = new JTabbedPane();
		FragmentGraphPanel fgPane = new FragmentGraphPanel();

		main_pane.addTab("Fragment graph", fgPane);
		main_pane.setBounds(0, 0, DEFAULT_SIZE.width, DEFAULT_SIZE.height);
		
		this.add(main_pane);
//		this.setSize(DEFAULT_SIZE);
		
	}   
    
	public static void main(String[] argv) {
		final GraphSee tf = new GraphSee();
		tf.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
			}
		});
		tf.setVisible(true);
		tf.pack();
		tf.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
}
