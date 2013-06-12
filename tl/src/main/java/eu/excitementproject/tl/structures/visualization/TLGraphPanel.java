package eu.excitementproject.tl.structures.visualization;

import java.awt.Dimension;
import java.util.Set;

import javax.swing.JPanel;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
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
        gma = new JGraphModelAdapter<V,E>(g);

        AttributeMap am = gma.getDefaultEdgeAttributes();
        System.out.println("Label edge value: " + am.get(GraphConstants.LABELALONGEDGE));
        am.applyValue(GraphConstants.LABELALONGEDGE, false);
        
        for (Object x: am.keySet()){
        	System.out.println("Attribute " + x.toString() + "\t" + am.get(x).toString());
        }
        
        gma.setDefaultEdgeAttributes(am);
        
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

    @SuppressWarnings("unchecked")
	protected void removeEdgeLabels(Set<E> edges) {
    	for (E edge: edges) {
    		DefaultGraphCell cell = gma.getEdgeCell(edge);
    		if (cell != null) {
    			AttributeMap attr = cell.getAttributes();
    			attr.applyValue(GraphConstants.LABELENABLED, false);
    			AttributeMap cellAttr = new AttributeMap();
    			cellAttr.put(cell, attr);
    			gma.edit(cellAttr, null, null, null);
    		}
    	}
    }
}
