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
 *     Zoey Gerrit Prigge (itemis AG) - initial API and implementation (bug #461506)
 *     Tamas Miklossy     (itemis AG) - conversion from Java to Xtend
 *
 *******************************************************************************/
package org.eclipse.gef.dot.tests

import com.google.inject.Inject
import org.eclipse.gef.dot.internal.language.portpos.PortPos
import org.eclipse.gef.dot.internal.language.portpos.PortposPackage
import org.eclipse.xtext.diagnostics.Diagnostic
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static extension org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(DotPortPosInjectorProvider)
class DotPortPosTest {

	@Inject extension ParseHelper<PortPos>
	@Inject extension ValidationTestHelper

	@Test def compass_point_as_name() {
		"w:sw".hasNoErrors
	}

	@Test def no_compass_point() {
		"hello".hasNoErrors
	}

	@Test def just_compass_point() {
		"ne".hasNoErrors
	}

	@Test def two_colons() {
		"port:w:w".hasOneSyntaxErrorOn("':'")
	}

	@Test def invalid_compass_point() {
		"king:r".hasOneSyntaxErrorOn("'r'")
	}

	private def hasNoErrors(String text) {
		val ast = text.parse
		ast.assertNotNull
		ast.assertNoErrors
	}

	private def hasOneSyntaxErrorOn(String text, String errorProneText) {
		val ast = text.parse
		ast.assertNotNull
		ast.assertError(PortposPackage.eINSTANCE.portPos, Diagnostic.SYNTAX_DIAGNOSTIC, errorProneText)

		// verify that this is the only reported issue
		1.assertEquals(ast.validate.size)
	}
}
