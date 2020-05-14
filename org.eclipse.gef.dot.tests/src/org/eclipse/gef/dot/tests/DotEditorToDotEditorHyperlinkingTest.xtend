/*******************************************************************************
 * Copyright (c) 2018, 2020 itemis AG and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tamas Miklossy (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef.dot.tests

import org.eclipse.gef.dot.tests.ui.DotUiInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.ui.testing.AbstractHyperlinkingTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(DotUiInjectorProvider)
class DotEditorToDotEditorHyperlinkingTest extends AbstractHyperlinkingTest {

	@Test def hyperlink_on_the_left_side_of_an_edge() {
		'''
			graph {
				1;2
				«c»1«c»--2
			}
		'''.hasHyperlinkTo("1")
	}

	@Test def hyperlink_on_the_right_side_of_an_edge() {
		'''
			graph {
				1;2
				1--«c»2«c»
			}
		'''.hasHyperlinkTo("2")
	}

	@Test def hyperlink_on_the_left_side_of_an_edge_quoted_node() {
		'''
			digraph {
				"org.eclipse.gef.dot.ui"
				"org.eclipse.gef.dot"
				"«c»org.eclipse.gef.dot.ui«c»" -> "org.eclipse.gef.dot"
			}
		'''.hasHyperlinkTo("org.eclipse.gef.dot.ui")
	}

	@Test def hyperlink_on_the_right_side_of_an_edge_quoted_node() {
		'''
			digraph {
				"org.eclipse.gef.dot.ui"
				"org.eclipse.gef.dot"
				"org.eclipse.gef.dot.ui" -> "«c»org.eclipse.gef.dot«c»"
			}
		'''.hasHyperlinkTo("org.eclipse.gef.dot")
	}

	@Test def hyperlink_on_the_left_side_of_an_edge_to_a_node_in_subgraph() {
		'''
			digraph {
				{
					1
					2
				}
				«c»1«c»->2
			}
		'''.hasHyperlinkTo("1")
	}

	@Test def hyperlink_on_the_right_side_of_an_edge_to_a_node_in_subgraph() {
		'''
			digraph {
				{
					1
					2
				}
				1->«c»2«c»
			}
		'''.hasHyperlinkTo("2")
	}

	@Test def hyperlink_on_the_left_side_of_an_edge_to_a_node_in_nested_subgraph() {
		'''
			digraph {
				{
					1 2
					{
						3 4
					}
				}
				
				1->2
				«c»3«c»->4
			}
		'''.hasHyperlinkTo("3")
	}

	@Test def hyperlink_on_the_right_side_of_an_edge_to_a_node_in_nested_subgraph() {
		'''
			digraph {
				{
					1 2
					{
						3 4
					}
				}
				
				1->2
				3->«c»4«c»
			}
		'''.hasHyperlinkTo("4")
	}
}
