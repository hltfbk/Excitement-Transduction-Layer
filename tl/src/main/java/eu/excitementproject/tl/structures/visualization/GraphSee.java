package eu.excitementproject.tl.structures.visualization;


import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

@SuppressWarnings("unused")
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
//    	FragmentGraph fg = FragmentGraph.getSampleGraph();
//    	equipMe(new FragmentGraphPanel(fg));
    	
    	EntailmentGraphRaw egr = EntailmentGraphRaw.getSampleOuput(true);
//    	equipMe(new RawGraphPanel(egr));    	
    }
    
    public GraphSee(TLGraphPanel<?,?> tlgp) {
    	super("Graph visualization");
    	equipMe(tlgp);
    }

    
	/**
	 * Creates visual elements.
	 * @param <E>
	 * @param <V>
	 */
	private void equipMe(TLGraphPanel<?,?> tlgp) {
		JTabbedPane main_pane = new JTabbedPane();

		main_pane.addTab("Fragment graph", tlgp);
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
