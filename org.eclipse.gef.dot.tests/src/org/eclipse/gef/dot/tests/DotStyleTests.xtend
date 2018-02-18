/*******************************************************************************
 * Copyright (c) 2017, 2018 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tamas Miklossy (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef.dot.tests

import com.google.inject.Inject
import org.eclipse.gef.dot.internal.language.DotStyleInjectorProvider
import org.eclipse.gef.dot.internal.language.style.Style
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.eclipse.xtext.parser.antlr.IAntlrTokenFileProvider
import org.eclipse.xtext.parser.antlr.Lexer
import org.junit.Test
import org.junit.runner.RunWith

import static extension org.eclipse.gef.dot.tests.DotTestUtils.lex
import static extension org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(DotStyleInjectorProvider)
class DotStyleTests {
	
	@Inject extension ParseHelper<Style>
	@Inject extension ValidationTestHelper
	@Inject extension IAntlrTokenFileProvider

	@Inject Lexer lexer

	@Test def void test_valid_style() {
		"bold".parse.assertNoErrors
	}

	@Test def void testLexingNodeStyle01(){
		"bold".assertLexing('''
			RULE_NAME 'bold'
		''')
	}
	
	@Test def void testLexingNodeStyle02(){
		"dashed".assertLexing('''
			RULE_NAME 'dashed'
		''')
	}
	
	@Test def void testLexingNodeStyle03(){
		"diagonals".assertLexing('''
			RULE_NAME 'diagonals'
		''')
	}
	
	@Test def void testLexingNodeStyle04(){
		"dotted".assertLexing('''
			RULE_NAME 'dotted'
		''')
	}
	
	@Test def void testLexingNodeStyle05(){
		"filled".assertLexing('''
			RULE_NAME 'filled'
		''')
	}
	
	@Test def void testLexingNodeStyle06(){
		"invis".assertLexing('''
			RULE_NAME 'invis'
		''')
	}
	
	@Test def void testLexingNodeStyle07(){
		"radial".assertLexing('''
			RULE_NAME 'radial'
		''')
	}
	
	@Test def void testLexingNodeStyle08(){
		"rounded".assertLexing('''
			RULE_NAME 'rounded'
		''')
	}
	
	@Test def void testLexingNodeStyle09(){
		"solid".assertLexing('''
			RULE_NAME 'solid'
		''')
	}
	
	@Test def void testLexingNodeStyle10(){
		"striped".assertLexing('''
			RULE_NAME 'striped'
		''')
	}
	
	@Test def void testLexingNodeStyle11(){
		"wedged".assertLexing('''
			RULE_NAME 'wedged'
		''')
	}
	
	@Test def void testLexingNodeStyle12(){
		"bold, dashed".assertLexing('''
			RULE_NAME 'bold'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'dashed'
		''')
	}
	
	@Test def void testLexingNodeStyle13(){
		"dashed,bold".assertLexing('''
			RULE_NAME 'dashed'
			T__6 ','
			RULE_NAME 'bold'
		''')
	}
	
	@Test def void testLexingNodeStyle14(){
		"bold, dotted".assertLexing('''
			RULE_NAME 'bold'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'dotted'
		''')
	}
	
	@Test def void testLexingNodeStyle15(){
		"dotted, bold".assertLexing('''
			RULE_NAME 'dotted'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'bold'
		''')
	}
	
	@Test def void testLexingNodeStyle16(){
		"filled, dashed".assertLexing('''
			RULE_NAME 'filled'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'dashed'
		''')
	}
	
	@Test def void testLexingNodeStyle17(){
		"dashed, filled".assertLexing('''
			RULE_NAME 'dashed'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'filled'
		''')
	}
	
	@Test def void testLexingNodeStyle18(){
		"bold, filled".assertLexing('''
			RULE_NAME 'bold'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'filled'
		''')
	}
	
	@Test def void testLexingNodeStyle19(){
		"filled, bold".assertLexing('''
			RULE_NAME 'filled'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'bold'
		''')
	}
	
	@Test def void testLexingNodeStyle20(){
		"bold, diagonals".assertLexing('''
			RULE_NAME 'bold'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'diagonals'
		''')
	}
	
	@Test def void testLexingNodeStyle21(){
		"diagonals, bold".assertLexing('''
			RULE_NAME 'diagonals'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'bold'
		''')
	}
	
	@Test def void testLexingNodeStyle22(){
		"diagonals, filled".assertLexing('''
			RULE_NAME 'diagonals'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'filled'
		''')
	}
	
	@Test def void testLexingNodeStyle23(){
		"filled, diagonals".assertLexing('''
			RULE_NAME 'filled'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'diagonals'
		''')
	}
	
	@Test def void testLexingNodeStyle24(){
		"diagonals, filled, bold".assertLexing('''
			RULE_NAME 'diagonals'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'filled'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'bold'
		''')
	}
	
	@Test def void testLexingNodeStyle25(){
		"filled, bold, diagonals".assertLexing('''
			RULE_NAME 'filled'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'bold'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'diagonals'
		''')
	}
	
	@Test def void testLexingNodeStyle26(){
		"setlinewidth(4)".assertLexing('''
			RULE_NAME 'setlinewidth'
			T__7 '('
			RULE_NAME '4'
			T__8 ')'
		''')
	}
	
	@Test def void testLexingEdgeStyle01(){
		"bold".assertLexing('''
			RULE_NAME 'bold'
		''')
	}
	
	@Test def void testLexingEdgeStyle02(){
		"dashed".assertLexing('''
			RULE_NAME 'dashed'
		''')
	}
	
	@Test def void testLexingEdgeStyle03(){
		"dotted".assertLexing('''
			RULE_NAME 'dotted'
		''')
	}
	
	@Test def void testLexingEdgeStyle04(){
		"invis".assertLexing('''
			RULE_NAME 'invis'
		''')
	}
	
	@Test def void testLexingEdgeStyle05(){
		"solid".assertLexing('''
			RULE_NAME 'solid'
		''')
	}
	
	@Test def void testLexingEdgeStyle06(){
		"tapered".assertLexing('''
			RULE_NAME 'tapered'
		''')
	}
	
	@Test def void testLexingEdgeStyle07(){
		"bold, dashed".assertLexing('''
			RULE_NAME 'bold'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'dashed'
		''')
	}
	
	@Test def void testLexingEdgeStyle08(){
		"dashed, bold".assertLexing('''
			RULE_NAME 'dashed'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'bold'
		''')
	}
	
	@Test def void testLexingEdgeStyle09(){
		"bold, dotted".assertLexing('''
			RULE_NAME 'bold'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'dotted'
		''')
	}
	
	@Test def void testLexingEdgeStyle10(){
		"dotted, bold".assertLexing('''
			RULE_NAME 'dotted'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'bold'
		''')
	}
	
	@Test def void testLexingEdgeStyle11(){
		"bold, tapered".assertLexing('''
			RULE_NAME 'bold'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'tapered'
		''')
	}
	
	@Test def void testLexingEdgeStyle12(){
		"tapered, bold".assertLexing('''
			RULE_NAME 'tapered'
			T__6 ','
			RULE_WS ' '
			RULE_NAME 'bold'
		''')
	}

	private def assertLexing(CharSequence modelAsText, CharSequence expected) {
		val actual = modelAsText.lex(lexer, antlrTokenFile)
		expected.toString.trim.assertEquals(actual.toString.trim)
	}

}