package eu.excitementproject.tl.structures.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jgrapht.graph.AbstractGraph;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.force.ForceSimulator;
import prefuse.util.io.IOLib;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JValueSlider;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import eu.excitementproject.tl.structures.visualization.utils.TLGraphMLExporter;

public class GraphRenderer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5893706476969287833L;
	
	private static final String graph = "graph";
	private static final String nodes = "graph.nodes";
	private static final String edges = "graph.edges";
	
	private static final String tmpFile = "D:/temp/graphMLfile.xml";
	
	public Visualization m_vis;

	
	public GraphRenderer() {
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("rawtypes")
	public GraphRenderer(AbstractGraph g){
		super(new BorderLayout());
		
		TLGraphMLExporter.exportGraph(g, tmpFile);
		
		try {
			initializeGraphRenderer(new GraphMLReader().readGraph(tmpFile), "vertex_label");
		} catch (DataIOException e) {
			System.err.println("Could not load a prefuse graph from the graphML file " + tmpFile);
			e.printStackTrace();
		}
		
	}

	
	public GraphRenderer(Graph g) {
		super(new BorderLayout());

		initializeGraphRenderer(g, "vertex_label");
	}
	
	private void initializeGraphRenderer(Graph g, String label) {

		inspectGraph(g);
		
		// create a new, empty visualization for our data
		m_vis = new Visualization();

		// --------------------------------------------------------------------
		// set up the renderers

		LabelRenderer tr = new LabelRenderer();
		tr.setRoundedCorner(8, 8);
		m_vis.setRendererFactory(new DefaultRendererFactory(tr));

		// --------------------------------------------------------------------
		// register the data with a visualization

		// adds graph to visualization and sets renderer label field
		setGraph(g, label);

		// fix selected focus nodes
		TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS);
		focusGroup.addTupleSetListener(new TupleSetListener() {
			public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
				for (int i = 0; i < rem.length; ++i)
					((VisualItem) rem[i]).setFixed(false);
				for (int i = 0; i < add.length; ++i) {
					((VisualItem) add[i]).setFixed(false);
					((VisualItem) add[i]).setFixed(true);
				}
				if (ts.getTupleCount() == 0) {
					ts.addTuple(rem[0]);
					((VisualItem) rem[0]).setFixed(false);
				}
				m_vis.run("draw");
			}
		});

		// --------------------------------------------------------------------
		// create actions to process the visual data

		int hops = 30;
		final GraphDistanceFilter filter = new GraphDistanceFilter(graph, hops);

		ColorAction fill = new ColorAction(nodes, VisualItem.FILLCOLOR,
				ColorLib.rgb(200, 200, 255));
		fill.add(VisualItem.FIXED, ColorLib.rgb(255, 100, 100));
		fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));

		ActionList draw = new ActionList();
		draw.add(filter);
		draw.add(fill);
		draw.add(new ColorAction(nodes, VisualItem.STROKECOLOR, 0));
		draw.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(0,
				0, 0)));
		draw.add(new ColorAction(edges, VisualItem.FILLCOLOR, ColorLib
				.gray(200)));
		draw.add(new ColorAction(edges, VisualItem.STROKECOLOR, ColorLib
				.gray(200)));

		ActionList animate = new ActionList(Activity.INFINITY);
		animate.add(new ForceDirectedLayout(graph));
		animate.add(fill);
		animate.add(new RepaintAction());

		// finally, we register our ActionList with the Visualization.
		// we can later execute our Actions by invoking a method on our
		// Visualization, using the name we've chosen below.
		m_vis.putAction("draw", draw);
		m_vis.putAction("layout", animate);

		m_vis.runAfter("draw", "layout");

		// --------------------------------------------------------------------
		// set up a display to show the visualization

		Display display = new Display(m_vis);
		display.setSize(700, 700);
		display.pan(350, 350);
		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);

		// main display controls
		display.addControlListener(new FocusControl(1));
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());

		// overview display
		// Display overview = new Display(vis);
		// overview.setSize(290,290);
		// overview.addItemBoundsListener(new FitOverviewListener());

		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);

		// --------------------------------------------------------------------
		// launch the visualization

		// create a panel for editing force values
		ForceSimulator fsim = ((ForceDirectedLayout) animate.get(0))
				.getForceSimulator();
		JForcePanel fpanel = new JForcePanel(fsim);

		// JPanel opanel = new JPanel();
		// opanel.setBorder(BorderFactory.createTitledBorder("Overview"));
		// opanel.setBackground(Color.WHITE);
		// opanel.add(overview);

		final JValueSlider slider = new JValueSlider("Distance", 0, hops, hops);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				filter.setDistance(slider.getValue().intValue());
				m_vis.run("draw");
			}
		});
		slider.setBackground(Color.WHITE);
		slider.setPreferredSize(new Dimension(300, 30));
		slider.setMaximumSize(new Dimension(300, 30));

		Box cf = new Box(BoxLayout.Y_AXIS);
		cf.add(slider);
		cf.setBorder(BorderFactory.createTitledBorder("Connectivity Filter"));
		fpanel.add(cf);

		// fpanel.add(opanel);

		fpanel.add(Box.createVerticalGlue());

		// create a new JSplitPane to present the interface
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(display);
		split.setRightComponent(fpanel);
		split.setOneTouchExpandable(true);
		split.setContinuousLayout(false);
		split.setDividerLocation(700);

		// now we run our action list
		m_vis.run("draw");

		add(split);
	}

	// just to check if the graph was read properly
	@SuppressWarnings("rawtypes")
	private void inspectGraph(Graph g) {		
		Iterator it = g.nodes();
		Node n;
		
		while(it.hasNext()) {
			n = (Node) it.next(); 
			System.out.println(n.toString());
		}
	}

	
	public void setGraph(Graph g, String label) {
		// update labeling
		DefaultRendererFactory drf = (DefaultRendererFactory) m_vis
				.getRendererFactory();
		((LabelRenderer) drf.getDefaultRenderer()).setTextField(label);

		// update graph
		m_vis.removeGroup(graph);
		VisualGraph vg = m_vis.addGraph(graph, g);
		m_vis.setValue(edges, null, VisualItem.INTERACTIVE, Boolean.FALSE);
		VisualItem f = (VisualItem) vg.getNode(0);
		m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
		f.setFixed(false);
	}



	// ------------------------------------------------------------------------

	/**
	 * Swing menu action that loads a graph into the graph viewer.
	 */
	public abstract static class GraphMenuAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -937752895976786607L;
		private GraphRenderer m_view;

		public GraphMenuAction(String name, String accel, GraphRenderer view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, name);
			this.putValue(AbstractAction.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(accel));
		}

		public void actionPerformed(ActionEvent e) {
			m_view.setGraph(getGraph(), "label");
		}

		protected abstract Graph getGraph();
	}

	public static class OpenGraphAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6274034128859625445L;
		private GraphRenderer m_view;

		public OpenGraphAction(GraphRenderer view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Open File...");
			this.putValue(AbstractAction.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke("ctrl O"));
		}

		public void actionPerformed(ActionEvent e) {
			Graph g = IOLib.getGraphFile(m_view);
			if (g == null)
				return;
			String label = getLabel(m_view, g);
			if (label != null) {
				m_view.setGraph(g, label);
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static String getLabel(Component c, Graph g) {
			// get the column names
			Table t = g.getNodeTable();
			int cc = t.getColumnCount();
			String[] names = new String[cc];
			for (int i = 0; i < cc; ++i)
				names[i] = t.getColumnName(i);

			// where to store the result
			final String[] label = new String[1];

			// -- build the dialog -----
			// we need to get the enclosing frame first
			while (c != null && !(c instanceof JFrame)) {
				c = c.getParent();
			}
			final JDialog dialog = new JDialog((JFrame) c,
					"Choose Label Field", true);

			// create the ok/cancel buttons
			final JButton ok = new JButton("OK");
			ok.setEnabled(false);
			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
				}
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					label[0] = null;
					dialog.setVisible(false);
				}
			});

			// build the selection list
			final JList list = new JList(names);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent e) {
							int sel = list.getSelectedIndex();
							if (sel >= 0) {
								ok.setEnabled(true);
								label[0] = (String) list.getModel()
										.getElementAt(sel);
							} else {
								ok.setEnabled(false);
								label[0] = null;
							}
						}
					});
			JScrollPane scrollList = new JScrollPane(list);

			JLabel title = new JLabel("Choose a field to use for node labels:");

			// layout the buttons
			Box bbox = new Box(BoxLayout.X_AXIS);
			bbox.add(Box.createHorizontalStrut(5));
			bbox.add(Box.createHorizontalGlue());
			bbox.add(ok);
			bbox.add(Box.createHorizontalStrut(5));
			bbox.add(cancel);
			bbox.add(Box.createHorizontalStrut(5));

			// put everything into a panel
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(title, BorderLayout.NORTH);
			panel.add(scrollList, BorderLayout.CENTER);
			panel.add(bbox, BorderLayout.SOUTH);
			panel.setBorder(BorderFactory.createEmptyBorder(5, 2, 2, 2));

			// show the dialog
			dialog.setContentPane(panel);
			dialog.pack();
			dialog.setLocationRelativeTo(c);
			dialog.setVisible(true);
			dialog.dispose();

			System.out.println("Label " + label[0] + " / " + c.getName());
			
			// return the label field selection
			return label[0];
		}
	}

	public static class FitOverviewListener implements ItemBoundsListener {
		private Rectangle2D m_bounds = new Rectangle2D.Double();
		private Rectangle2D m_temp = new Rectangle2D.Double();
		private double m_d = 15;

		public void itemBoundsChanged(Display d) {
			d.getItemBounds(m_temp);
			GraphicsLib.expand(m_temp, 25 / d.getScale());

			double dd = m_d / d.getScale();
			double xd = Math.abs(m_temp.getMinX() - m_bounds.getMinX());
			double yd = Math.abs(m_temp.getMinY() - m_bounds.getMinY());
			double wd = Math.abs(m_temp.getWidth() - m_bounds.getWidth());
			double hd = Math.abs(m_temp.getHeight() - m_bounds.getHeight());
			if (xd > dd || yd > dd || wd > dd || hd > dd) {
				m_bounds.setFrame(m_temp);
				DisplayLib.fitViewToBounds(d, m_bounds, 0);
			}
		}
	}
	
}
