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
public class GraphViewer {
    
	private static final Dimension DEFAULT_SIZE = new Dimension( 1000, 700 );

	public static void drawRandomGraph() {
		
    	JFrame frame = new JFrame("E X C I T M E N T  |  g r a p h v i e w");
    	
    	try {

//    			FragmentGraph g = FragmentGraph.getSampleGraph();
    			EntailmentGraphRaw g = EntailmentGraphRaw.getSampleOuput(false);
//    			EntailmentGraphCollapsed g = new EntailmentGraphCollapsed(new File("./src/test/outputs/WP2_public_data_CAS_XMI/nice_email_1/collapsed_graph.xml"));
    		
    			System.out.println("GRAPH:\n\n" + g.toString() + "\n\n");
    			final GraphRenderer graphRenderer = new GraphRenderer(g);
    	
    			frame.setSize(DEFAULT_SIZE);
    			frame.setContentPane(graphRenderer);
    			frame.pack();
    			frame.setVisible(true);

//    			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    			
    			frame.addWindowListener(new WindowAdapter() {
    				public void windowActivated(WindowEvent e) {
    					graphRenderer.m_vis.run("draw");
    				}
    				public void windowDeactivated(WindowEvent e) {
    					graphRenderer.m_vis.cancel("layout");
    				}
    			});
    			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} catch (Exception e){ // (EntailmentGraphCollapsedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    

	@SuppressWarnings("rawtypes")
	public static void drawGraph(AbstractGraph g) {
		JFrame frame = new JFrame("E X C I T M E N T  |  g r a p h v i e w");
    	final GraphRenderer graphRenderer = new GraphRenderer(g);
    	
		frame.setSize(DEFAULT_SIZE);
		frame.setContentPane(graphRenderer);
		frame.pack();
		frame.setVisible(true);

//		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				graphRenderer.m_vis.run("draw");
			}
			public void windowDeactivated(WindowEvent e) {
				graphRenderer.m_vis.cancel("layout");
			}
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
 
	


    
	/**
	 * Creates visual elements.
	 */
/*	
	private static void equipMe(JFrame frame, GraphRenderer graphRenderer) {
		
		frame.setSize(DEFAULT_SIZE);
		frame.setContentPane(graphRenderer);
		frame.pack();
		frame.setVisible(true);

//		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				graphRenderer.m_vis.run("draw");
			}
			public void windowDeactivated(WindowEvent e) {
				graphRenderer.m_vis.cancel("layout");
			}
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}   
*/	
	
	public static void main(String[] argv) {

		// something just to try, without parameters
//		GraphViewer.drawRandomGraph();
		
		// visualize a specific graph (could be either FragmentGraph, EntailmentGraphRaw, or EntailmentGraphCollapsed
		try{
			EntailmentGraphCollapsed g = new EntailmentGraphCollapsed(new File("./src/test/outputs/WP2_public_data_CAS_XMI/ALMA_social_media_perFrag/EditDistanceEDA_NonLexRes_IT.xml0.9500000000000004_collapsedGraph.xml"));
			GraphViewer.drawGraph(g);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
