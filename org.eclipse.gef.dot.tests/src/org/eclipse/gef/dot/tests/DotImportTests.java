/*******************************************************************************
 * Copyright (c) 2009, 2018 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabian Steeg                   - initial API and implementation (see bug #277380)
 *     Tamas Miklossy  (itemis AG)    - implement additional test cases (bug #493136)
 *                                    - merge DotInterpreter into DotImport (bug #491261)
 *     Zoey Gerrit Prigge (itemis AG) - implement additional dot attributes (bug #461506)
 *
 *******************************************************************************/
package org.eclipse.gef.dot.tests;

import static org.eclipse.gef.dot.tests.DotTestUtils.RESOURCES_TESTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.eclipse.gef.dot.internal.DotAttributes;
import org.eclipse.gef.dot.internal.DotImport;
import org.eclipse.gef.dot.internal.language.DotInjectorProvider;
import org.eclipse.gef.dot.internal.language.dot.GraphType;
import org.eclipse.gef.dot.internal.language.layout.Layout;
import org.eclipse.gef.dot.internal.language.rankdir.Rankdir;
import org.eclipse.gef.dot.internal.language.terminals.ID;
import org.eclipse.gef.dot.internal.language.terminals.ID.Type;
import org.eclipse.gef.graph.Edge;
import org.eclipse.gef.graph.Graph;
import org.eclipse.gef.graph.Node;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the {@link DotImport} class.
 * 
 * @author Fabian Steeg (fsteeg)
 */
@RunWith(XtextRunner.class)
@InjectWith(DotInjectorProvider.class)
public final class DotImportTests {

	@Rule
	public DotSubgrammarPackagesRegistrationRule rule = new DotSubgrammarPackagesRegistrationRule();

	private final DotImport dotImport = new DotImport();
	private final DotGraphPrettyPrinter prettyPrinter = new DotGraphPrettyPrinter();

	/**
	 * Test valid graphs can be imported without exceptions.
	 */
	@Test
	public void sampleGraphsFileImport() {
		// simple graphs
		Graph graph = importFile(
				new File(RESOURCES_TESTS + "simple_graph.dot")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotTestUtils.getSimpleGraph().toString(),
				graph.toString());

		graph = importFile(new File(RESOURCES_TESTS + "simple_digraph.dot")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotTestUtils.getSimpleDiGraph().toString(),
				graph.toString());

		graph = importFile(new File(RESOURCES_TESTS + "labeled_graph.dot")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotTestUtils.getLabeledGraph().toString(),
				graph.toString());

		graph = importFile(new File(RESOURCES_TESTS + "styled_graph.dot")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotTestUtils.getStyledGraph().toString(),
				graph.toString());

		graph = importString(DotTestGraphs.GLOBAL_EDGE_NODE_COLORSCHEME);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
	}

	/**
	 * Test error handling for invalid graph.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidGraphFileImport() {
		importString("graph Sample{");
	}

	@Test(expected = IllegalArgumentException.class)
	public void faultyLayout() {
		importString("graph Sample{graph[layout=cool];1;}"); //$NON-NLS-1$
	}

	@Test
	public void digraphType() {
		Graph graph = importString(DotTestGraphs.TWO_NODES_ONE_DIRECTED_EDGE);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(GraphType.DIGRAPH, DotAttributes._getType(graph));
	}

	@Test
	public void graphType() {
		Graph graph = importString(DotTestGraphs.TWO_NODES_ONE_EDGE);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(GraphType.GRAPH, DotAttributes._getType(graph));
	}

	@Test
	public void nodeDefaultLabel() {
		Graph graph = importString(DotTestGraphs.ONE_NODE);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("1", //$NON-NLS-1$
				DotAttributes._getName(graph.getNodes().get(0)));
	}

	@Test
	public void nodeCount() {
		Graph graph = importString(DotTestGraphs.TWO_NODES);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(2, graph.getNodes().size());
	}

	@Test
	public void edgeCount() {
		Graph graph = importString(DotTestGraphs.TWO_NODES_AND_THREE_EDGES);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(3, graph.getEdges().size());
	}

	@Test
	public void layoutDot() {
		Graph.Builder graph = new Graph.Builder()
				.attr(DotAttributes::_setType, GraphType.DIGRAPH)
				.attr(DotAttributes::setLayoutParsed, Layout.DOT);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1]).buildEdge();
		Edge e2 = new Edge.Builder(nodes[1], nodes[2]).buildEdge();
		Edge e3 = new Edge.Builder(nodes[1], nodes[3]).buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2, e3).build();
		testStringImport(expected, DotTestGraphs.GRAPH_LAYOUT_DOT);
	}

	@Test
	public void layoutFdp() {
		Graph.Builder graph = new Graph.Builder()
				.attr(DotAttributes::_setType, GraphType.DIGRAPH)
				.attr(DotAttributes::setLayoutParsed, Layout.FDP);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1]).buildEdge();
		Edge e2 = new Edge.Builder(nodes[1], nodes[2]).buildEdge();
		Edge e3 = new Edge.Builder(nodes[1], nodes[3]).buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2, e3).build();
		testStringImport(expected, DotTestGraphs.GRAPH_LAYOUT_FDP);
	}

	@Test
	public void layoutOsage() {
		Graph.Builder graph = new Graph.Builder()
				.attr(DotAttributes::_setType, GraphType.DIGRAPH)
				.attr(DotAttributes::setLayoutParsed, Layout.OSAGE);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1]).buildEdge();
		Edge e2 = new Edge.Builder(nodes[1], nodes[2]).buildEdge();
		Edge e3 = new Edge.Builder(nodes[1], nodes[3]).buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2, e3).build();
		testStringImport(expected, DotTestGraphs.GRAPH_LAYOUT_OSAGE);
	}

	@Test
	public void layoutTwopi() {
		Graph.Builder graph = new Graph.Builder()
				.attr(DotAttributes::_setType, GraphType.DIGRAPH)
				.attr(DotAttributes::setLayoutParsed, Layout.TWOPI);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1]).buildEdge();
		Edge e2 = new Edge.Builder(nodes[1], nodes[2]).buildEdge();
		Edge e3 = new Edge.Builder(nodes[1], nodes[3]).buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2, e3).build();
		testStringImport(expected, DotTestGraphs.GRAPH_LAYOUT_TWOPI);
	}

	@Test
	public void layoutDotHorizontal() {
		Graph graph = importString(DotTestGraphs.GRAPH_LAYOUT_DOT_HORIZONTAL);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(Layout.DOT.toString(),
				DotAttributes.getLayout(graph));
		Assert.assertEquals(Rankdir.LR, DotAttributes.getRankdirParsed(graph));
	}

	@Test
	public void layoutHorizontalTreeViaAttribute() {
		Graph graph = importString(DotTestGraphs.GRAPH_RANKDIR_LR);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(Rankdir.LR, DotAttributes.getRankdirParsed(graph));
	}

	@Test
	public void globalNodeAttributeAdHocNodes() {
		Graph graph = importString(
				DotTestGraphs.GLOBAL_NODE_LABEL_AD_HOC_NODES);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("TEXT", //$NON-NLS-1$
				DotAttributes.getLabel(graph.getNodes().get(0)));
	}

	@Test
	public void globalEdgeAttributeAdHocNodes() {
		Graph graph = importString(
				DotTestGraphs.GLOBAL_EDGE_LABEL_AD_HOC_NODES);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("TEXT", DotAttributes.getLabel(graph.getEdges() //$NON-NLS-1$
				.get(0)));
	}

	@Test
	public void headerCommentGraph() {
		Graph graph = importString(DotTestGraphs.HEADER_COMMENT);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(2, graph.getNodes().size());
		Assert.assertEquals(1, graph.getEdges().size());
	}

	@Test
	public void nodesBeforeEdges() {
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1]).buildEdge();
		Edge e2 = new Edge.Builder(nodes[1], nodes[2]).buildEdge();
		Edge e3 = new Edge.Builder(nodes[1], nodes[3]).buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2, e3).build();
		testStringImport(expected, DotTestGraphs.NODES_BEFORE_EDGES);
	}

	@Test
	public void nodesBeforeEdgesWithAttributes() {
		Graph.Builder graph = new Graph.Builder()
				.attr(DotAttributes::_setName, "AttributesGraph")
				.attr(DotAttributes::_setType, GraphType.DIGRAPH)
				.attr(DotAttributes::setRankdirParsed, Rankdir.LR)
				.attr(DotAttributes::setLabel, "Left-to-Right");
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1]).buildEdge();
		Edge e2 = new Edge.Builder(nodes[0], nodes[2]).buildEdge();
		Edge e3 = new Edge.Builder(nodes[1], nodes[3]).buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2, e3).build();
		testStringImport(expected,
				DotTestGraphs.NODES_BEFORE_EDGES_WITH_ATTRIBUTES);
	}

	@Test
	public void directedStyledGraph() {
		Graph.Builder graph = new Graph.Builder()
				.attr(DotAttributes::_setName, "DirectedStyledGraph")
				.attr(DotAttributes::_setType, GraphType.DIGRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setLabel, "Edge")
				.attr(DotAttributes::setStyle, "dashed").buildEdge();
		Edge e2 = new Edge.Builder(nodes[1], nodes[2])
				.attr(DotAttributes::setStyle, "dotted").buildEdge();
		// set the label attribute to the expected ID object (with value Dotted
		// and type quoted string)
		DotAttributes.setLabelRaw(e2,
				ID.fromValue("Dotted", Type.QUOTED_STRING));

		Edge e3 = new Edge.Builder(nodes[1], nodes[3])
				.attr(DotAttributes::setLabel, "Edge")
				.attr(DotAttributes::setStyle, "dashed").buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2, e3).build();
		testStringImport(expected, DotTestGraphs.DIRECTED_STYLED_GRAPH);
	}

	@Test
	public void nodesAfterEdges() {
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		DotAttributes.setLabel(nodes[0], "node");
		Edge e1 = new Edge.Builder(nodes[0], nodes[1]).buildEdge();
		Edge e2 = new Edge.Builder(nodes[1], nodes[2]).buildEdge();
		Edge e3 = new Edge.Builder(nodes[1], nodes[3]).buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2, e3).build();
		testStringImport(expected, DotTestGraphs.NODES_AFTER_EDGES);
	}

	@Test
	public void useDotImporterTwice() {
		String dot = DotTestGraphs.NODES_AFTER_EDGES;
		Graph graph = importString(dot);
		graph = importString(dot);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(4, graph.getNodes().size());
		Assert.assertEquals(3, graph.getEdges().size());
	}

	@Test
	public void idsWithQuotes() {
		Graph graph = importString(DotTestGraphs.IDS_WITH_QUOTES);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		List<Node> list = graph.getNodes();
		Assert.assertEquals("node 1", //$NON-NLS-1$
				DotAttributes._getName(list.get(0)));
		Assert.assertEquals("node 2", //$NON-NLS-1$
				DotAttributes._getName(list.get(1)));
	}

	@Test
	public void escapedQuotes() {
		Graph graph = importString(DotTestGraphs.ESCAPED_QUOTES_LABEL);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("node \"1\"", //$NON-NLS-1$
				DotAttributes.getLabel(graph.getNodes().get(0)));
	}

	@Test
	public void multilineQuotedId() {
		Graph graph = importString(DotTestGraphs.MULTILINE_QUOTED_IDS);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("node 1", //$NON-NLS-1$
				DotAttributes.getLabel(graph.getNodes().get(0)));
	}

	@Test
	public void fullyQuoted() {
		Graph graph = importString(DotTestGraphs.FULLY_QUOTED_IDS);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(2, graph.getNodes().size());
		Assert.assertEquals(1, graph.getEdges().size());
		List<Node> list = graph.getNodes();
		Assert.assertEquals("n1", //$NON-NLS-1$
				DotAttributes._getName(list.get(0)));
		Assert.assertEquals("n2", //$NON-NLS-1$
				DotAttributes._getName(list.get(1)));
	}

	@Test
	public void labelsWithQuotes() {
		Graph graph = importString(DotTestGraphs.QUOTED_LABELS);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		List<Node> list = graph.getNodes();
		Assert.assertEquals("node 1", //$NON-NLS-1$
				DotAttributes.getLabel(list.get(0)));
		Assert.assertEquals("node 2", //$NON-NLS-1$
				DotAttributes.getLabel(list.get(1)));
		Assert.assertEquals("edge 1",
				DotAttributes.getLabel(graph.getEdges().get(0)));
	}

	@Test
	public void labelsWithQuotes2() {
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		DotAttributes.setLabel(nodes[0], "node");
		DotAttributes.setXlabel(nodes[0], "Node");
		DotAttributes.setLabel(nodes[1], "foo bar");
		DotAttributes.setLabel(nodes[2], "foo");

		// set the label attribute to the expected ID object (with value foo
		// and type quoted string)
		DotAttributes.setLabelRaw(nodes[3],
				ID.fromValue("foo", Type.QUOTED_STRING));

		Graph expected = graph.nodes(nodes).build();
		testStringImport(expected, DotTestGraphs.QUOTED_LABELS2);
	}

	@Test
	public void newLinesInLabels() {
		Graph graph = importString(DotTestGraphs.NEW_LINES_IN_LABELS);
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("node" + System.lineSeparator() + "1", //$NON-NLS-1$
				DotAttributes.getLabel(graph.getNodes().get(0)));
	}

	@Test
	public void multiEdgeStatements() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setArrowhead, "ornormal") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[1], nodes[2])
				.attr(DotAttributes::setArrowhead, "ornormal") //$NON-NLS-1$
				.buildEdge();
		Edge e3 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setArrowhead, "ornormal") //$NON-NLS-1$
				.buildEdge();
		Edge e4 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setArrowhead, "ornormal") //$NON-NLS-1$
				.buildEdge();
		Edge e5 = new Edge.Builder(nodes[1], nodes[2])
				.attr(DotAttributes::setArrowhead, "ornormal") //$NON-NLS-1$
				.buildEdge();
		Edge e6 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setArrowhead, "ornormal") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2, e3, e4, e5, e6)
				.build();
		testStringImport(expected, DotTestGraphs.MULTI_EDGE_STATEMENTS_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setArrowhead(e4, "olnormal");
		DotAttributes.setArrowhead(e5, "olnormal");
		DotAttributes.setArrowhead(e6, "olnormal");
		expected = graph.nodes(nodes).edges(e1, e2, e3, e4, e5, e6).build();
		testStringImport(expected, DotTestGraphs.MULTI_EDGE_STATEMENTS_LOCAL);

		// test override attribute
		testStringImport(expected,
				DotTestGraphs.MULTI_EDGE_STATEMENTS_OVERRIDE);
	}

	@Test
	public void compassPointsAsNodeNames() {
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "n") //$NON-NLS-1$
				.buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "ne") //$NON-NLS-1$
				.buildNode();
		Node n3 = new Node.Builder().attr(DotAttributes::_setName, "e") //$NON-NLS-1$
				.buildNode();
		Node n4 = new Node.Builder().attr(DotAttributes::_setName, "se") //$NON-NLS-1$
				.buildNode();
		Node n5 = new Node.Builder().attr(DotAttributes::_setName, "s") //$NON-NLS-1$
				.buildNode();
		Node n6 = new Node.Builder().attr(DotAttributes::_setName, "sw") //$NON-NLS-1$
				.buildNode();
		Node n7 = new Node.Builder().attr(DotAttributes::_setName, "w") //$NON-NLS-1$
				.buildNode();
		Node n8 = new Node.Builder().attr(DotAttributes::_setName, "nw") //$NON-NLS-1$
				.buildNode();
		Node n9 = new Node.Builder().attr(DotAttributes::_setName, "c") //$NON-NLS-1$
				.buildNode();
		Node n10 = new Node.Builder().attr(DotAttributes::_setName, "_") //$NON-NLS-1$
				.buildNode();
		Graph expected = graph.nodes(n1, n2, n3, n4, n5, n6, n7, n8, n9, n10)
				.build();
		testStringImport(expected, DotTestGraphs.COMPASS_POINTS_AS_NODE_NAMES);
	}

	@Ignore
	@Test
	public void nodeGroups() {
		// TODO: implement as soon as the EdgeStmtNode is properly imported
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.buildNode();
		Node n3 = new Node.Builder().attr(DotAttributes::_setName, "3") //$NON-NLS-1$
				.buildNode();
		Node n4 = new Node.Builder().attr(DotAttributes::_setName, "foo") //$NON-NLS-1$
				.attr(DotAttributes::setShape, "box").buildNode();
		Node n5 = new Node.Builder().attr(DotAttributes::_setName, "bar") //$NON-NLS-1$
				.attr(DotAttributes::setShape, "box").buildNode();
		Node n6 = new Node.Builder().attr(DotAttributes::_setName, "baz") //$NON-NLS-1$
				.attr(DotAttributes::setShape, "box").buildNode();
		Graph expected = graph.nodes(n1, n2, n3, n4, n5, n6).build();
		testStringImport(expected, DotTestGraphs.NODE_GROUPS);
	}

	@Test
	public void edgeStyleInvis() {
		Graph graph = importString(DotTestGraphs.EDGE_STYLE_INVIS);
		assertEquals(2, graph.getNodes().size());
		assertEquals(1, graph.getEdges().size());
	}

	@Test
	public void edge_arrowhead() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setArrowhead, "crow") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setArrowhead, "crow") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_ARROWHEAD_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setArrowhead(e1, "diamond");
		DotAttributes.setArrowhead(e2, "dot");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_ARROWHEAD_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setArrowhead(e1, "vee");
		DotAttributes.setArrowhead(e2, "tee");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_ARROWHEAD_OVERRIDE);
	}

	@Test
	public void edge_arrowsize() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setArrowsize, "1.5") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setArrowsize, "1.5") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_ARROWSIZE_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setArrowsize(e1, "2.0");
		DotAttributes.setArrowsize(e2, "2.1");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_ARROWSIZE_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setArrowsize(e1, "2.3");
		DotAttributes.setArrowsize(e2, "2.2");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_ARROWSIZE_OVERRIDE);
	}

	@Test
	public void edge_arrowtail() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setArrowtail, "box") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setArrowtail, "box") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_ARROWTAIL_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setArrowtail(e1, "lbox");
		DotAttributes.setArrowtail(e2, "rbox");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_ARROWTAIL_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setArrowtail(e1, "olbox");
		DotAttributes.setArrowtail(e2, "obox");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_ARROWTAIL_OVERRIDE);
	}

	@Test
	public void edge_color() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setColor, "0.000 0.000 1.000") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setColor, "0.000 0.000 1.000") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_COLOR_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setColor(e1, "0.000 0.000 1.000");
		DotAttributes.setColor(e2, "white");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_COLOR_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setColor(e1, "white");
		DotAttributes.setColor(e2, "0.000 0.000 1.000");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_COLOR_OVERRIDE);
	}

	@Test
	public void edge_colorscheme() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setColorscheme, "accent3") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setColorscheme, "accent3") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_COLORSCHEME_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setColorscheme(e1, "accent3");
		DotAttributes.setColorscheme(e2, "accent4");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_COLORSCHEME_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setColorscheme(e1, "accent4");
		DotAttributes.setColorscheme(e2, "accent3");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_COLORSCHEME_OVERRIDE);
	}

	@Test
	public void edge_dir() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setDir, "forward") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setDir, "forward") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_DIR_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setDir(e1, "forward");
		DotAttributes.setDir(e2, "back");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_DIR_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setDir(e1, "both");
		DotAttributes.setDir(e2, "back");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_DIR_OVERRIDE);
	}

	@Test
	public void edge_edgetooltip() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setEdgetooltip, "a") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setEdgetooltip, "a") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_EDGETOOLTIP_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setEdgetooltip(e1, "b");
		DotAttributes.setEdgetooltip(e2, "c");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_EDGETOOLTIP_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setEdgetooltip(e1, "e");
		DotAttributes.setEdgetooltip(e2, "d");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_EDGETOOLTIP_OVERRIDE);
	}

	@Test
	public void edge_fillcolor() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setFillcolor, "0.000 0.000 0.000") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setFillcolor, "0.000 0.000 0.000") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FILLCOLOR_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFillcolor(e1, "0.000 0.000 0.000");
		DotAttributes.setFillcolor(e2, "black");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FILLCOLOR_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFillcolor(e1, "black");
		DotAttributes.setFillcolor(e2, "0.000 0.000 0.000");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FILLCOLOR_OVERRIDE);
	}

	@Test
	public void edge_fontcolor() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setFontcolor, "0.000 1.000 1.000") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setFontcolor, "0.000 1.000 1.000") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FONTCOLOR_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontcolor(e1, "0.000 1.000 1.000");
		DotAttributes.setFontcolor(e2, "red");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FONTCOLOR_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontcolor(e1, "red");
		DotAttributes.setFontcolor(e2, "0.000 1.000 1.000");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FONTCOLOR_OVERRIDE);
	}

	@Test
	public void edge_fontname() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setFontname, "Font1") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setFontname, "Font1") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FONTNAME_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontname(e1, "Font1");
		DotAttributes.setFontname(e2, "Font2");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FONTNAME_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontname(e1, "Font3");
		DotAttributes.setFontname(e2, "Font4");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FONTNAME_OVERRIDE);
	}

	@Test
	public void edge_fontsize() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setFontsize, "1.1") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setFontsize, "1.1") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FONTSIZE_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontsize(e1, "1.1");
		DotAttributes.setFontsize(e2, "1.2");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FONTSIZE_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontsize(e1, "1.3");
		DotAttributes.setFontsize(e2, "1.4");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_FONTSIZE_OVERRIDE);
	}

	@Test
	public void edge_headlabel() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setHeadlabel, "EdgeHeadLabel1") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setHeadlabel, "EdgeHeadLabel1") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_HEADLABEL_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setHeadlabel(e1, "EdgeHeadLabel2");
		DotAttributes.setHeadlabel(e2, "EdgeHeadLabel3");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_HEADLABEL_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setHeadlabel(e1, "EdgeHeadLabel5");
		DotAttributes.setHeadlabel(e2, "EdgeHeadLabel4");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_HEADLABEL_OVERRIDE);
	}

	@Test
	public void edge_headlp() {
		// no global/override attribute tests, since they do not make sense
		// test local attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setHeadLp, "2.2,3.3") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setHeadLp, "-2.2,-3.3") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_HEAD_LP_LOCAL);
	}

	@Test
	public void edge_headport() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setHeadport, "port5:nw") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setHeadport, "port5:nw") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_HEADPORT_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setHeadport(e1, "port1:w");
		DotAttributes.setHeadport(e2, "port2:e");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_HEADPORT_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setHeadport(e1, "port1:w");
		DotAttributes.setHeadport(e2, "port5:nw");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_HEADPORT_OVERRIDE);
	}

	@Test
	public void edge_headtooltip() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setHeadtooltip, "a") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setHeadtooltip, "a") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_HEADTOOLTIP_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setHeadtooltip(e1, "b");
		DotAttributes.setHeadtooltip(e2, "c");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_HEADTOOLTIP_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setHeadtooltip(e1, "e");
		DotAttributes.setHeadtooltip(e2, "d");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_HEADTOOLTIP_OVERRIDE);
	}

	@Test
	public void edge_id() {
		// no global/override attribute tests, since they do not make sense
		// test local attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setId, "edgeID2") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setId, "edgeID3") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_ID_LOCAL);
	}

	@Test
	public void edge_label() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setLabel, "Edge1") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setLabel, "Edge1") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABEL_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabel(e1, "Edge1");
		DotAttributes.setLabel(e2, "Edge2");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABEL_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabel(e1, "Edge4");
		DotAttributes.setLabel(e2, "Edge3");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABEL_OVERRIDE);
	}

	@Test
	public void edge_labelfontcolor() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setLabelfontcolor, "0.482 0.714 0.878") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setLabelfontcolor, "0.482 0.714 0.878") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELFONTCOLOR_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabelfontcolor(e1, "0.482 0.714 0.878");
		DotAttributes.setLabelfontcolor(e2, "turquoise");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELFONTCOLOR_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabelfontcolor(e1, "turquoise");
		DotAttributes.setLabelfontcolor(e2, "0.482 0.714 0.878");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELFONTCOLOR_OVERRIDE);
	}

	@Test
	public void edge_labelfontname() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setLabelfontname, "Font1") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setLabelfontname, "Font1") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELFONTNAME_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabelfontname(e1, "Font1");
		DotAttributes.setLabelfontname(e2, "Font2");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELFONTNAME_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabelfontname(e1, "Font3");
		DotAttributes.setLabelfontname(e2, "Font4");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELFONTNAME_OVERRIDE);
	}

	@Test
	public void edge_labelfontsize() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setLabelfontsize, "1.1") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setLabelfontsize, "1.1") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELFONTSIZE_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabelfontsize(e1, "1.1");
		DotAttributes.setLabelfontsize(e2, "1.2");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELFONTSIZE_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabelfontsize(e1, "1.3");
		DotAttributes.setLabelfontsize(e2, "1.4");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELFONTSIZE_OVERRIDE);
	}

	@Test
	public void edge_labeltooltip() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setLabeltooltip, "a") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setLabeltooltip, "a") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELTOOLTIP_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabeltooltip(e1, "b");
		DotAttributes.setLabeltooltip(e2, "c");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELTOOLTIP_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabeltooltip(e1, "e");
		DotAttributes.setLabeltooltip(e2, "d");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LABELTOOLTIP_OVERRIDE);
	}

	@Test
	public void edge_lp() {
		// no global/override attribute tests, since they do not make sense
		// test local attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setLp, "0.3,0.4") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setLp, "0.5,0.6") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_LP_LOCAL);
	}

	@Test
	public void edge_pos() {
		// no global/override attribute tests, since they do not make sense
		// test local attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setPos, "0.0,0.0 1.0,1.0 2.0,2.0 3.0,3.0") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setPos, "4.0,4.0 5.0,5.0 6.0,6.0 7.0,7.0") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_POS_LOCAL);
	}

	@Test
	public void edge_style() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setStyle, "dashed") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setStyle, "dashed") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_STYLE_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setStyle(e1, "dashed");
		DotAttributes.setStyle(e2, "dotted");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_STYLE_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setStyle(e1, "bold, dotted");
		DotAttributes.setStyle(e2, "bold");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_STYLE_OVERRIDE);
	}

	@Test
	public void edge_taillabel() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setTaillabel, "EdgeTailLabel1") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setTaillabel, "EdgeTailLabel1") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TAILLABEL_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setTaillabel(e1, "EdgeTailLabel2");
		DotAttributes.setTaillabel(e2, "EdgeTailLabel3");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TAILLABEL_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setTaillabel(e1, "EdgeTailLabel5");
		DotAttributes.setTaillabel(e2, "EdgeTailLabel4");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TAILLABEL_OVERRIDE);
	}

	@Test
	public void edge_taillp() {
		// no global/override attribute tests, since they do not make sense
		// test local attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setTailLp, "-4.5,-6.7") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setTailLp, "-8.9,-10.11") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TAIL_LP_LOCAL);
	}

	@Test
	public void edge_tailport() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setTailport, "port5:nw") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setTailport, "port5:nw") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TAILPORT_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setTailport(e1, "port1:w");
		DotAttributes.setTailport(e2, "port2:e");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TAILPORT_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setTailport(e1, "port1:w");
		DotAttributes.setTailport(e2, "port5:nw");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TAILPORT_OVERRIDE);
	}

	@Test
	public void edge_tailtooltip() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setTailtooltip, "a") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setTailtooltip, "a") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TAILTOOLTIP_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setTailtooltip(e1, "b");
		DotAttributes.setTailtooltip(e2, "c");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TAILTOOLTIP_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		DotAttributes.setTailtooltip(e1, "e");
		DotAttributes.setTailtooltip(e2, "d");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TAILTOOLTIP_OVERRIDE);
	}

	@Test
	public void edge_tooltip() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setTooltip, "a") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setTooltip, "a") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TOOLTIP_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setTooltip(e1, "b");
		DotAttributes.setTooltip(e2, "c");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TOOLTIP_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setTooltip(e1, "e");
		DotAttributes.setTooltip(e2, "d");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_TOOLTIP_OVERRIDE);
	}

	@Test
	public void edge_xlabel() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setXlabel, "EdgeExternalLabel1") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setXlabel, "EdgeExternalLabel1") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_XLABEL_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setXlabel(e1, "EdgeExternalLabel2");
		DotAttributes.setXlabel(e2, "EdgeExternalLabel3");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_XLABEL_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setXlabel(e1, "EdgeExternalLabel5");
		DotAttributes.setXlabel(e2, "EdgeExternalLabel4");
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_XLABEL_OVERRIDE);
	}

	@Test
	public void edge_xlp() {
		// no global/override attribute tests, since they do not make sense
		// test local attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		Edge e1 = new Edge.Builder(nodes[0], nodes[1])
				.attr(DotAttributes::setXlp, ".3,.4") //$NON-NLS-1$
				.buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3])
				.attr(DotAttributes::setXlp, ".5,.6") //$NON-NLS-1$
				.buildEdge();
		Graph expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.EDGE_XLP_LOCAL);
	}

	@Test
	public void graph_bgcolor() {
		// test local attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		graph.attr(DotAttributes::setBgcolor, "gray");
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1")
				.buildNode();
		Graph expected = graph.nodes(n1).build();
		testStringImport(expected, DotTestGraphs.GRAPH_BGCOLOR_LOCAL);
	}

	@Test
	public void graph_fontcolor() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		graph.attr(DotAttributes::setFontcolor, "aquamarine");
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1")
				.buildNode();
		Graph expected = graph.nodes(n1).build();
		testStringImport(expected, DotTestGraphs.GRAPH_FONTCOLOR_GLOBAL);

		// test local attribute
		DotAttributes.setFontcolor(expected, "red");
		testStringImport(expected, DotTestGraphs.GRAPH_FONTCOLOR_LOCAL);
	}

	@Test
	public void node_color() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1")
				// $NON-NLS-1$
				.attr(DotAttributes::setColor, "#ffffff").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2")
				// $NON-NLS-1$
				.attr(DotAttributes::setColor, "#ffffff").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_COLOR_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setColor(n1, "#ff0000");
		DotAttributes.setColor(n2, "#00ffff");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_COLOR_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setColor(n1, "#00ff00");
		DotAttributes.setColor(n2, "#ff0000");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_COLOR_OVERRIDE);
	}

	@Test
	public void node_colorscheme() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder()
				.attr(DotAttributes::_setNameRaw,
						ID.fromValue("1", Type.STRING))
				// $NON-NLS-1$
				.attr(DotAttributes::setColorscheme, "accent5").buildNode();
		Node n2 = new Node.Builder()
				.attr(DotAttributes::_setNameRaw,
						ID.fromValue("2", Type.STRING))
				// $NON-NLS-1$
				.attr(DotAttributes::setColorscheme, "accent5").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_COLORSCHEME_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setColorscheme(n1, "accent5");
		DotAttributes.setColorscheme(n2, "accent6");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_COLORSCHEME_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setColorscheme(n1, "accent6");
		DotAttributes.setColorscheme(n2, "accent5");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_COLORSCHEME_OVERRIDE);
	}

	@Test
	public void node_distortion() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setDistortion, "1.1").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setDistortion, "1.1").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_DISTORTION_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setDistortion(n1, "1.2");
		DotAttributes.setDistortion(n2, "1.3");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_DISTORTION_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setDistortion(n1, "1.5");
		DotAttributes.setDistortion(n2, "1.4");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_DISTORTION_OVERRIDE);
	}

	@Test
	public void node_fillcolor() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1")
				// $NON-NLS-1$
				.attr(DotAttributes::setFillcolor, "0.3 .8 .7").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2")
				// $NON-NLS-1$
				.attr(DotAttributes::setFillcolor, "0.3 .8 .7").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FILLCOLOR_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFillcolor(n1, "0.3 .8 .7");
		DotAttributes.setFillcolor(n2, "/bugn9/7");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FILLCOLOR_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFillcolor(n1, "/bugn9/7");
		DotAttributes.setFillcolor(n2, "0.3 .8 .7");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FILLCOLOR_OVERRIDE);
	}

	@Test
	public void node_fixedsize() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes.FIXEDSIZE__N, "true").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes.FIXEDSIZE__N, "true").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FIXEDSIZE_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFixedsizeParsed(n1, true);
		DotAttributes.setFixedsizeParsed(n2, false);
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FIXEDSIZE_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFixedsizeParsed(n1, false);
		DotAttributes.setFixedsizeParsed(n2, true);
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FIXEDSIZE_OVERRIDE);
	}

	@Test
	public void node_fontcolor() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1")
				// $NON-NLS-1$
				.attr(DotAttributes::setFontcolor, "0.3, .8, .7").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2")
				// $NON-NLS-1$
				.attr(DotAttributes::setFontcolor, "0.3, .8, .7").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FONTCOLOR_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontcolor(n1, "0.3, .8, .7");
		DotAttributes.setFontcolor(n2, "/brbg11/10");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FONTCOLOR_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontcolor(n1, "/brbg11/10");
		DotAttributes.setFontcolor(n2, "0.3, .8, .7");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FONTCOLOR_OVERRIDE);
	}

	@Test
	public void node_fontname() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1")
				// $NON-NLS-1$
				.attr(DotAttributes::setFontname, "Font1") //$NON-NLS-1$
				.buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2")
				// $NON-NLS-1$
				.attr(DotAttributes::setFontname, "Font1") //$NON-NLS-1$
				.buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FONTNAME_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontname(n1, "Font1");
		DotAttributes.setFontname(n2, "Font2");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FONTNAME_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontname(n1, "Font3");
		DotAttributes.setFontname(n2, "Font4");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FONTNAME_OVERRIDE);
	}

	@Test
	public void node_fontsize() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1")
				// $NON-NLS-1$
				.attr(DotAttributes::setFontsize, "1.1") //$NON-NLS-1$
				.buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2")
				// $NON-NLS-1$
				.attr(DotAttributes::setFontsize, "1.1") //$NON-NLS-1$
				.buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FONTSIZE_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontsize(n1, "1.1");
		DotAttributes.setFontsize(n2, "1.2");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FONTSIZE_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setFontsize(n1, "1.3");
		DotAttributes.setFontsize(n2, "1.4");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_FONTSIZE_OVERRIDE);
	}

	@Test
	public void node_height() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setHeight, "1.2").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setHeight, "1.2").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_HEIGHT_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setHeightParsed(n1, 3.4);
		DotAttributes.setHeightParsed(n2, 5.6);
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_HEIGHT_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setHeightParsed(n1, 9.11);
		DotAttributes.setHeightParsed(n2, 7.8);
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_HEIGHT_OVERRIDE);
	}

	@Test
	public void node_id() {
		// no global/override attribute tests, since they do not make sense
		// test local attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setId, "NodeID1").buildNode(); //$NON-NLS-1$ .buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setId, "NodeID2").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_ID_LOCAL);
	}

	@Test
	public void node_label() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setLabel, "Node1").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setLabel, "Node1").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_LABEL_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabel(n1, "Node1");
		DotAttributes.setLabel(n2, "Node2");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_LABEL_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setLabel(n1, "Gültig");
		DotAttributes.setLabel(n2, "Käse");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_LABEL_OVERRIDE);

		// test override attribute2
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Edge e = new Edge.Builder(n1, n2).buildEdge();
		expected = graph.nodes(n1, n2).edges(e).build();
		testStringImport(expected, DotTestGraphs.NODE_LABEL_OVERRIDE2);

		// test override attribute3
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node[] nodes = createNodes();
		DotAttributes.setLabel(nodes[1], "Node1");
		DotAttributes.setLabel(nodes[2], "Node2");
		DotAttributes.setLabel(nodes[3], "Node3");
		Edge e1 = new Edge.Builder(nodes[0], nodes[1]).buildEdge();
		Edge e2 = new Edge.Builder(nodes[2], nodes[3]).buildEdge();
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.NODE_LABEL_OVERRIDE3);

		// test override attribute4
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.DIGRAPH);
		nodes = createNodes();
		DotAttributes.setLabel(nodes[0], "Node");
		DotAttributes.setLabel(nodes[1], "Node");

		// set the label attribute to the expected ID object (with value Leaf
		// and type quoted string)
		DotAttributes.setLabelRaw(nodes[2],
				ID.fromValue("Leaf", Type.QUOTED_STRING));

		DotAttributes.setLabel(nodes[3], "Node");
		e1 = new Edge.Builder(nodes[0], nodes[1]).buildEdge();
		e2 = new Edge.Builder(nodes[1], nodes[2]).buildEdge();
		expected = graph.nodes(nodes).edges(e1, e2).build();
		testStringImport(expected, DotTestGraphs.NODE_LABEL_OVERRIDE4);
	}

	@Test
	public void node_pos() {
		// no global/override attribute tests, since they do not make sense
		// test local attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setPos, ".1,.2!").buildNode(); //$NON-NLS-1$ .buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setPos, "-0.1,-2.3!").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_POS_LOCAL);
	}

	@Test
	public void node_shape() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setShape, "box").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setShape, "box").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_SHAPE_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setShape(n1, "oval");
		DotAttributes.setShape(n2, "house");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_SHAPE_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setShape(n1, "circle");
		DotAttributes.setShape(n2, "pentagon");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_SHAPE_OVERRIDE);
	}

	@Test
	public void node_sides() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setSides, "3").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setSides, "3").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_SIDES_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setSidesParsed(n1, 4);
		DotAttributes.setSidesParsed(n2, 5);
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_SIDES_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setSidesParsed(n1, 7);
		DotAttributes.setSidesParsed(n2, 6);
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_SIDES_OVERRIDE);
	}

	@Test
	public void node_skew() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setSkew, "1.2").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setSkew, "1.2").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_SKEW_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setSkewParsed(n1, 3.4);
		DotAttributes.setSkewParsed(n2, 5.6);
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_SKEW_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setSkewParsed(n1, -7.8);
		DotAttributes.setSkewParsed(n2, 7.8);
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_SKEW_OVERRIDE);
	}

	@Test
	public void node_style() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setStyle, "solid, dashed").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setStyle, "solid, dashed").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_STYLE_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setStyle(n1, "bold");
		DotAttributes.setStyle(n2, "dotted");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_STYLE_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setStyle(n1, "rounded");
		DotAttributes.setStyle(n2, "bold, filled");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_STYLE_OVERRIDE);
	}

	@Test
	public void node_tooltip() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1")
				.attr(DotAttributes::setTooltip, "a").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2")
				.attr(DotAttributes::setTooltip, "a").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_TOOLTIP_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setTooltip(n1, "b");
		DotAttributes.setTooltip(n2, "c");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_TOOLTIP_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setTooltip(n1, "e");
		DotAttributes.setTooltip(n2, "d");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_TOOLTIP_OVERRIDE);
	}

	@Test
	public void node_width() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setWidth, "1.2").buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setWidth, "1.2").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_WIDTH_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setWidthParsed(n1, 3.4);
		DotAttributes.setWidthParsed(n2, 5.6);
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_WIDTH_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setWidthParsed(n1, 9.11);
		DotAttributes.setWidthParsed(n2, 7.8);
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_WIDTH_OVERRIDE);
	}

	@Test
	public void node_xlabel() {
		// test global attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setXlabel, "NodeExternalLabel1")
				.buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setXlabel, "NodeExternalLabel1")
				.buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_XLABEL_GLOBAL);

		// test local attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setXlabel(n1, "NodeExternalLabel2");
		DotAttributes.setXlabel(n2, "NodeExternalLabel3");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_XLABEL_LOCAL);

		// test override attribute
		graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		DotAttributes.setXlabel(n1, "NodeExternalLabel5");
		DotAttributes.setXlabel(n2, "NodeExternalLabel4");
		expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_XLABEL_OVERRIDE);
	}

	@Test
	public void node_xlp() {
		// no global/override attribute tests, since they do not make sense
		// test local attribute
		Graph.Builder graph = new Graph.Builder().attr(DotAttributes::_setType,
				GraphType.GRAPH);
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.attr(DotAttributes::setXlp, "-0.3,-0.4").buildNode(); //$NON-NLS-1$ .buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.attr(DotAttributes::setXlp, "-1.5,-1.6").buildNode();
		Graph expected = graph.nodes(n1, n2).build();
		testStringImport(expected, DotTestGraphs.NODE_XLP_LOCAL);
	}

	@Test
	public void clusters() {
		// test cluster subgraph
		Graph graph = importString(DotTestGraphs.CLUSTERS);
		assertNotNull(graph);
		assertEquals(GraphType.DIGRAPH, DotAttributes._getType(graph));
		// two clusters
		assertEquals(2, graph.getNodes().size());
		Node cluster1 = graph.getNodes().get(0);
		assertNotNull(cluster1.getNestedGraph());
		assertEquals("cluster1",
				DotAttributes._getName(cluster1.getNestedGraph()));
		assertEquals(cluster1, cluster1.getNestedGraph().getNestingNode());
		// two nested nodes and one nested edge (between these nodes) in small
		// cluster
		assertEquals(2, cluster1.getNestedGraph().getNodes().size());
		assertEquals(1, cluster1.getNestedGraph().getEdges().size());

		Node cluster2 = graph.getNodes().get(1);
		assertNotNull(cluster2.getNestedGraph());
		assertEquals("cluster2",
				DotAttributes._getName(cluster2.getNestedGraph()));
		// five nested nodes and five nested edges (between these nodes) in big
		// cluster
		assertEquals(5, cluster2.getNestedGraph().getNodes().size());
		assertEquals(5, cluster2.getNestedGraph().getEdges().size());

		assertEquals(2, graph.getEdges().size());
		Edge e1 = graph.getEdges().get(0);
		assertEquals("b", DotAttributes._getName(e1.getSource()));
		assertEquals("q", DotAttributes._getName(e1.getTarget()));
		Edge e2 = graph.getEdges().get(1);
		assertEquals("t", DotAttributes._getName(e2.getSource()));
		assertEquals("a", DotAttributes._getName(e2.getTarget()));

		// ensure DotImport can be used multiple times in succession
		graph = importString(DotTestGraphs.CLUSTERS);
		assertEquals(2, graph.getNodes().size());
	}

	@Test
	public void clusterMerge() {
		Graph graph = importString(DotTestGraphs.CLUSTER_MERGE);
		assertNotNull(graph);
		assertEquals(GraphType.DIGRAPH, DotAttributes._getType(graph));
		// one (merged) cluster
		assertEquals(1, graph.getNodes().size());
		Node cluster1 = graph.getNodes().get(0);
		assertNotNull(cluster1.getNestedGraph());
		assertEquals(4, cluster1.getNestedGraph().getNodes().size());
		assertEquals(2, cluster1.getNestedGraph().getEdges().size());
		assertEquals(2, graph.getEdges().size());
	}

	@Test
	public void subraphScoping() {
		// Input:
		// node [shape="hexagon", style="filled", fillcolor="blue"];
		// { node [shape="box"]; a; b; }
		// { node [fillcolor="red"]; b; c; }
		Graph graph = importString(DotTestGraphs.CLUSTER_SCOPE);

		// Expected result:
		// a [shape="box", style="filled", fillcolor="blue"];
		// b [shape="box", style="filled", fillcolor="blue"];
		// c [shape="hexagon", style="filled", fillcolor="red"];

		assertEquals(2, graph.getNodes().size());
		Node subgraph1 = graph.getNodes().get(0);
		Node subgraph2 = graph.getNodes().get(1);

		assertEquals(2, subgraph1.getNestedGraph().getNodes().size());
		assertEquals(1, subgraph2.getNestedGraph().getNodes().size());

		Node a = subgraph1.getNestedGraph().getNodes().get(0);
		assertEquals("a", DotAttributes._getName(a));
		assertEquals("box", DotAttributes.getShape(a));
		assertEquals("filled", DotAttributes.getStyle(a));
		assertEquals("blue", DotAttributes.getFillcolor(a));

		// b is defined in first subgraph, so it should be contained there
		Node b = subgraph1.getNestedGraph().getNodes().get(1);
		assertEquals("b", DotAttributes._getName(b));
		assertEquals("box", DotAttributes.getShape(b));
		assertEquals("filled", DotAttributes.getStyle(b));
		assertEquals("blue", DotAttributes.getFillcolor(b));

		Node c = subgraph2.getNestedGraph().getNodes().get(0);
		assertEquals("c", DotAttributes._getName(c));
		assertEquals("hexagon", DotAttributes.getShape(c));
		assertEquals("filled", DotAttributes.getStyle(c));
		assertEquals("red", DotAttributes.getFillcolor(c));
	}

	private Node[] createNodes() {
		Node n1 = new Node.Builder().attr(DotAttributes::_setName, "1") //$NON-NLS-1$
				.buildNode();
		Node n2 = new Node.Builder().attr(DotAttributes::_setName, "2") //$NON-NLS-1$
				.buildNode();
		Node n3 = new Node.Builder().attr(DotAttributes::_setName, "3") //$NON-NLS-1$
				.buildNode();
		Node n4 = new Node.Builder().attr(DotAttributes::_setName, "4") //$NON-NLS-1$
				.buildNode();
		return new Node[] { n1, n2, n3, n4 };
	}

	// TODO: Generalize to multiple graphs
	private Graph importFile(final File dotFile) {
		Assert.assertTrue("DOT input file must exist: " + dotFile, //$NON-NLS-1$
				dotFile.exists());
		List<Graph> graphs = dotImport.importDot(dotFile);
		return graphs.isEmpty() ? null : graphs.get(0);
	}

	// TODO: Generalize to multiple graphs
	private Graph importString(final String dotString) {
		List<Graph> graphs = dotImport.importDot(dotString);
		return graphs.isEmpty() ? null : graphs.get(0);
	}

	private void testStringImport(Graph expected, String dot) {
		List<Graph> graphs = dotImport.importDot(dot);
		Assert.assertEquals("Expected one graph", 1, graphs.size()); //$NON-NLS-1$

		String expectedFormattedText = prettyPrinter.prettyPrint(expected);
		String actualFormattedText = prettyPrinter.prettyPrint(graphs.get(0));

		Assert.assertEquals(expectedFormattedText, actualFormattedText);
	}
}
