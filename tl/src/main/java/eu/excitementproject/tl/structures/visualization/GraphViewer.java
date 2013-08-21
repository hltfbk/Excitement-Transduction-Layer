package eu.excitementproject.tl.structures.visualization;


import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jgrapht.graph.AbstractGraph;

import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

@SuppressWarnings("unused")
public class GraphViewer extends JFrame {


    /**
	 * 
	 */
	private static final long serialVersionUID = 924027832829838299L;

//	private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 1000, 700 );
    
    private GraphRenderer graphRenderer = null;
    
    public GraphViewer() {

    	super("E X C I T M E N T  |  g r a p h v i e w");

    	try {
    		if (graphRenderer == null) {

 //    			FragmentGraph g = FragmentGraph.getSampleGraph();
    			EntailmentGraphRaw g = EntailmentGraphRaw.getSampleOuput(false);
 //   			EntailmentGraphCollapsed g = new EntailmentGraphCollapsed(new File("./src/test/outputs/WP2_public_data_CAS_XMI/nice_email_1/collapsed_graph.xml"));
    		
    			System.out.println("GRAPH:\n\n" + g.toString() + "\n\n");
    			graphRenderer = new GraphRenderer(g);
    		}
    	
    		equipMe();
		} catch (Exception e){ // (EntailmentGraphCollapsedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
 
    @SuppressWarnings("rawtypes")
	public GraphViewer(AbstractGraph g) {    	
    	graphRenderer = new GraphRenderer(g);
    	equipMe();    	
    }
    
    public GraphViewer(GraphRenderer gr) {
    	super("E X C I T M E N T  |  g r a p h v i e w");
    	graphRenderer = gr;
    	equipMe();
    }

    
	/**
	 * Creates visual elements.
	 */
	private void equipMe() {

		this.setSize(DEFAULT_SIZE);
		this.setContentPane(graphRenderer);
		this.pack();
		this.setVisible(true);

//		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				graphRenderer.m_vis.run("draw");
			}
			public void windowDeactivated(WindowEvent e) {
				graphRenderer.m_vis.cancel("layout");
			}
		});
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}   
	
	
	public static void main(String[] argv) {

		// something just to try, without parameters
		final GraphViewer tf = new GraphViewer();
		
		// visualize a specific graph (could be either FragmentGraph, EntailmentGraphRaw, or EntailmentGraphCollapsed
/*		try{
			EntailmentGraphCollapsed g = new EntailmentGraphCollapsed(new File("./src/test/outputs/WP2_public_data_CAS_XMI/nice_email_1/collapsed_graph.xml"));
			final GraphViewer tf = new GraphViewer(g);
		} catch (Exception e) {
			e.printStackTrace();
		}
*/		
	}
}
