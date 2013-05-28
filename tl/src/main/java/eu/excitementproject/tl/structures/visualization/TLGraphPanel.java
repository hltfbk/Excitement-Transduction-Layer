package eu.excitementproject.tl.structures.visualization;

import java.awt.Dimension;

import javax.swing.JPanel;

import org.jgraph.JGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.AbstractGraph;

public abstract class TLGraphPanel<V,E> extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3909032936246720776L;
	JPanel graphPane;
	JGraph jgraph;
	
	AbstractGraph<V,E> g;
	
    protected JGraphModelAdapter<V,E> gma;

    protected static final Dimension DEFAULT_SIZE = new Dimension( 1000, 700 );

    public TLGraphPanel() {
    }
		
    public TLGraphPanel(AbstractGraph<V,E> g) {
    	this.g = g;
    	init();
    }

/*    addGraphToPanel();
    	this.add(graphPane);
	}
*/	
    public void init() {
    	
        // create a visualization using JGraph, via an adapter
        gma = new JGraphModelAdapter<V,E>( g );
        jgraph = new JGraph( gma );
        
        addGraphToPanel();
    	this.add(graphPane);
    }

    
    public void addGraph(AbstractGraph<V,E> g) {
    	this.g = g;
    	init();
    }
    
    protected abstract void addGraphToPanel() ;
    
    abstract void positionVertexAt( Object vertex, int x, int y ) ;    
	
}
