package eu.excitementproject.tl.structures.visualization;

import java.awt.Rectangle;

import javax.swing.JPanel;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.graph.AbstractGraph;

import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public class RawGraphPanel extends TLGraphPanel<EntailmentUnit,EntailmentRelation> {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3909032936246720776L;

	public RawGraphPanel(
			AbstractGraph<EntailmentUnit, EntailmentRelation> g) {
		super(g);
		// TODO Auto-generated constructor stub
	}
	
	public RawGraphPanel() {
		super();
		addGraph((AbstractGraph<EntailmentUnit,EntailmentRelation>) EntailmentGraphRaw.getSampleOuput(true));
	}
	
//    private ListenableDirectedGraph<EntailmentUnitMention,FragmentGraphEdge> makeSampleGraph() {


    protected void addGraphToPanel() {
      	graphPane = new JPanel();
    	graphPane.add( jgraph );
    	graphPane.setSize(DEFAULT_SIZE);
    	graphPane.setBounds(0, 0, DEFAULT_SIZE.width, DEFAULT_SIZE.height);

    	removeEdgeLabels(g.edgeSet());
    	
    	int skip = 200;
    	int c = 0, x, y;
   		for(EntailmentUnit eum: ((EntailmentGraphRaw) g).vertexSet()){
   			c++;
   			if (eum == null) {
   				System.out.println("error reading nodes");
   				System.exit(0);
   			}
   			x = (c % 5) * skip;
   			y = c / 5 * 400 + (c % 5) * 30;
   			positionVertexAt(eum, x, y);
    	}
    }
    
	@SuppressWarnings({"unchecked" })
    protected void positionVertexAt( Object vertex, int x, int y ) {
 
       	String text = ((EntailmentUnit) vertex).getText();
        
    	System.out.println("Positioning node: <" + text + "> at ("+ x + "," + y + ")");
    	
        DefaultGraphCell cell = gma.getVertexCell( vertex );
        if (cell != null) {
			AttributeMap   attr = cell.getAttributes(  );
        	GraphConstants.setBounds(attr, new Rectangle(x,y,text.length() * 8,20));
        	
        	AttributeMap cellAttr = new AttributeMap();
        	cellAttr.put( cell, attr );
        	
        	gma.edit( cellAttr, null, null, null);
        } else {
        	System.err.println("Null cell!");
        }
    }
        
	
}
