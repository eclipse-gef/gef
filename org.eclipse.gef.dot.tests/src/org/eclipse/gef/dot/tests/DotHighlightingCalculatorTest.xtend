/*******************************************************************************
 * Copyright (c) 2017, 2020 itemis AG and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Tamas Miklossy (itemis AG) - initial API and implementation
 *    Zoey Prigge    (itemis AG) - strikethrough/deprecation (bug #552993)
 *
 *******************************************************************************/
package org.eclipse.gef.dot.tests

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.inject.Inject
import org.eclipse.gef.dot.internal.language.dot.DotAst
import org.eclipse.gef.dot.internal.ui.language.highlighting.DotSemanticHighlightingCalculator
import org.eclipse.gef.dot.tests.ui.DotUiInjectorProvider
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.util.TextRegion
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static org.eclipse.gef.dot.internal.ui.language.highlighting.DotHighlightingConfiguration.*

import static extension org.junit.Assert.*

/**
 * The implementation of this class is mainly taken from the
 * org.eclipse.xtend.ide.tests.highlighting.XtendHighlightingCalculatorTest java
 * class.
 *
 * @author miklossy
 *
 */
@RunWith(XtextRunner)
@InjectWith(DotUiInjectorProvider)
class DotHighlightingCalculatorTest implements IHighlightedPositionAcceptor {

	@Inject extension DotSemanticHighlightingCalculator
	@Inject extension ParseHelper<DotAst>

	Multimap<TextRegion, String> expectedRegions

	@Before def void setUp() {
		expectedRegions = HashMultimap.create
	}

	@After def void tearDown() {
		expectedRegions = null
	}

	// semantic highlighting test cases
	@Test def null_guard() {
		null.provideHighlightingFor(this, null)
	}

	@Test def graph_name() {
		DotTestGraphs.GLOBAL_EDGE_NODE_COLORSCHEME.assertHightlightingIDs(
			"G" -> GRAPH_NAME_ID
		)
	}

	@Test def node_name() {
		DotTestGraphs.ONE_NODE.assertHightlightingIDs(
			"1" -> NODE_NAME_ID
		)
	}

	@Test def port() {
		DotTestGraphs.PORTS.assertHightlightingIDs(
			"portID" -> PORT_NAME_ID,
			"portID2" -> PORT_NAME_ID
		)
	}

	@Test def attribute_name() {
		DotTestGraphs.GRAPH_LAYOUT_DOT.assertHightlightingIDs(
			"layout" -> ATTRIBUTE_NAME_ID
		)
	}

	@Test def edge_operator_directed() {
		DotTestGraphs.ONE_DIRECTED_EDGE.assertHightlightingIDs(
			"->" -> EDGE_OP_ID
		)
	}

	@Test def edge_operator_undirected() {
		DotTestGraphs.ONE_EDGE.assertHightlightingIDs(
			"--" -> EDGE_OP_ID
		)
	}

	@Test def html_label() {
		DotTestGraphs.NODE_LABEL_HTML_LIKE(DotTestHtmlLabels.FONT_TAG_CONTAINS_TABLE_TAG).assertHightlightingIDs(
			"<" -> HTML_TAG,
			'''
			<!--
								Html label with custom font
							-->''' -> HTML_COMMENT ,
			"color" -> HTML_ATTRIBUTE_NAME,
			'"green"' -> HTML_ATTRIBUTE_VALUE,
			"table" -> HTML_TAG,
			"tr" -> HTML_TAG,
			"td" -> HTML_TAG,
			"text" -> HTML_CONTENT,
			"</" -> HTML_TAG
		)
	}

	@Test def deprecation() {
		DotTestGraphs.DEPRECATED_ARROWTYPES.assertHightlightingIDs(
			"ediamond" -> DEPRECATED_ATTRIBUTE_VALUE,
			"open" -> DEPRECATED_ATTRIBUTE_VALUE_QUOTED,
			"halfopen" -> DEPRECATED_ATTRIBUTE_VALUE,
			"empty" -> DEPRECATED_ATTRIBUTE_VALUE,
			"invempty" -> DEPRECATED_ATTRIBUTE_VALUE,
			"ediamond" -> DEPRECATED_ATTRIBUTE_VALUE,
			"invempty" -> DEPRECATED_ATTRIBUTE_VALUE,
			"open" -> DEPRECATED_ATTRIBUTE_VALUE //openbox
		)
	}

	private def assertHightlightingIDs(CharSequence it, Pair<String, String>... expectedHighlightingIDs) {
		expect(expectedHighlightingIDs)
		highlight
		assertAllExpectedRegionsHasBeenFound
	}

	/**
	 * Method to populate expectedRegions with expected highlighting regions;
	 * as keywords can appear multiple times, these need to be specified in order
	 */
	private def expect(CharSequence it, Pair<String, String>... expectedHighlightingIDs) {
		var offset = 0;
		for(pair : expectedHighlightingIDs) {
			val substring = pair.key
			val highlightingID = pair.value

			offset = toString.indexOf(substring, offset)
			val length = substring.length
			expectedRegions.put(new TextRegion(offset, length), highlightingID)
		}
	}

	private def highlight(CharSequence it) {
		val resource = parse.eResource as XtextResource
		resource.provideHighlightingFor(this, null)
	}

	private def assertAllExpectedRegionsHasBeenFound() {
		expectedRegions.toString.assertTrue(expectedRegions.isEmpty)
	}

	override addPosition(int offset, int length, String... actualIDs) {
		Assert.assertTrue('''length = «length»''', length >= 0)
		val actualRegion = new TextRegion(offset, length)
		val expectedIds = expectedRegions.get(actualRegion)

		1.assertEquals(actualIDs.length)
		val actualID = actualIDs.get(0)

		if (expectedIds.contains(actualID)) {
			expectedRegions.remove(actualRegion, actualID)
		}
	}
}
