/*******************************************************************************
 * Copyright (c) 2017 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Zoey Gerrit Prigge  - initial API and implementation (bug #454629)
 *    
 *******************************************************************************/
package org.eclipse.gef.dot.tests

import com.google.inject.Inject
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.gef.dot.internal.language.DotRecordLabelInjectorProvider
import org.eclipse.gef.dot.internal.language.recordlabel.Field
import org.eclipse.gef.dot.internal.language.recordlabel.RLabel
import org.eclipse.gef.dot.internal.language.recordlabel.RecordlabelFactory
import org.eclipse.gef.dot.internal.language.recordlabel.RecordlabelPackage
import org.eclipse.gef.dot.internal.language.validation.DotRecordLabelJavaValidator
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(DotRecordLabelInjectorProvider)
public class DotRecordLabelTests {
	@Inject extension ParseHelper<RLabel> parseHelper
	@Inject extension ValidationTestHelper

	// good Syntax
	@Test def void emptyString() {
		''''''.assertNoErrors.assertTreeEquals(
			rlabel(fieldIDinField(null))
		)
	}

	@Test def void singleLetter() {
		'''F'''.assertNoErrors.assertTreeEquals(
			rlabel(fieldIDinField("F"))
		)
	}

	@Test def void specialSign() {
		'''§'''.assertNoErrors.assertTreeEquals(
			rlabel(fieldIDinField("§"))
		)
	}

	@Test def void word() {
		'''Hello'''.assertNoErrors.assertTreeEquals(
			rlabel(fieldIDinField("Hello"))
		)
	}

	@Test def void escapedCharacter() {
		'''Please\ read\ §146'''.assertNoErrors.assertTreeEquals(
			rlabel(fieldIDinField('''Please\ read\ §146'''))
		)
	}

	@Test def void escapedBraceInText() {
		'''Ple\}se146read'''.assertNoErrors.assertTreeEquals(
			rlabel(fieldIDinField('''Ple\}se146read'''))
		)
	}

	@Test def void escapedBackslash() {
		'''\\'''.assertNoErrors.assertTreeEquals(
			rlabel(fieldIDinField('''\\'''))
		)
	}

	@Test def void whiteSpaceBetweenLetters() {
		'''k D'''.assertNoErrors.assertTreeEquals(
			rlabel(fieldIDinField('''k D'''))
		)
	}

	@Test def void separatorSign() {
		'''abc|def'''.assertNoErrors.assertTreeEquals(
			rlabel(
				fieldIDinField("abc"),
				fieldIDinField("def")
			)
		)
	}

	@Test def void threeFields() {
		'''abc | def | gh4i'''.assertNoErrors.assertTreeEquals(
			rlabel(
				fieldIDinField("abc"),
				fieldIDinField("def"),
				fieldIDinField("gh4i")
			)
		)
	}

	@Test def void simpleFourFields() {
		'''A | B | C | D'''.assertNoErrors.assertTreeEquals(
			rlabel(fieldIDinField("A"), fieldIDinField("B"), fieldIDinField("C"), fieldIDinField("D")))
	}

	@Test def void emptyRotatedLabel() {
		'''{}'''.assertNoErrors.assertTreeEquals(
			rlabel(rotationWrapper(rlabel(
				fieldIDinField(null)
			)))
		)
	}

	@Test def void simpleRotation() {
		'''{ Hi }'''.assertNoErrors.assertTreeEquals(
			rlabel(rotationWrapper(rlabel(
				fieldIDinField("Hi")
			)))
		)
	}

	@Test def void rotatedFourFieldsLabel() {
		'''{ Hi | This | Is | Awesome }'''.assertNoErrors.assertTreeEquals(
			rlabel(rotationWrapper(rlabel(
				fieldIDinField("Hi"),
				fieldIDinField("This"),
				fieldIDinField("Is"),
				fieldIDinField("Awesome")
			)))
		)
	}

	@Test def void rotatedMoreComplexLabel() {
		'''Hi | {Test | Section 2 } | xyz'''.assertNoErrors.assertTreeEquals(
			rlabel(
				fieldIDinField("Hi"),
				rotationWrapper(rlabel(
					fieldIDinField("Test"),
					fieldIDinField("Section 2")
				)),
				fieldIDinField("xyz")
			)
		)
	}

	@Test def void fieldId() {
		'''<fgh> someField'''.assertNoErrors.assertTreeEquals(
			rlabel(fieldIDinField("fgh", "someField"))
		)
	}

	@Test def void emptyPortname() {
		'''<>'''.assertNoErrors.assertTreeEquals(
			rlabel(
				fieldIDinField("", null)
			)
		)
	}

	@Test def void emptyPortnameWithText() {
		'''<> kids'''.assertNoErrors.assertTreeEquals(
			rlabel(
				fieldIDinField("", "kids")
			)
		)
	}

	@Test def void namedPort() {
		'''<Label>'''.assertNoErrors.assertTreeEquals(
			rlabel(fieldIDinField("Label", null))
		)
	}

	@Test def void portInHField() {
		'''{<Label>}'''.assertNoErrors.assertTreeEquals(
			rlabel(rotationWrapper(
				rlabel(fieldIDinField("Label", null))
			))
		)
	}

	@Test def void portInHFieldWithText() {
		'''{<Label> Coolstuff!}'''.assertNoErrors.assertTreeEquals(
			rlabel(rotationWrapper(
				rlabel(fieldIDinField("Label", "Coolstuff!"))
			))
		)
	}

	@Test def void portWithEscapedCharInName() {
		'''<some_weans\{>'''.assertNoErrors.assertTreeEquals(
			rlabel(
				fieldIDinField('''some_weans\{''', null)
			)
		)
	}

	// complex Parse Tests
	@Test def void parseTreeSimple() {
		'''hello word | <port> cool stuff going on '''.assertTreeEquals(rlabel(
			fieldIDinField("hello word"),
			fieldIDinField("port", "cool stuff going on")
		))
	}

	@Test def void parseTreeComplex() {
		'''
		hello word | cool stuff going on | { <free> free beer here |
		wine there } | sad it's just a test'''.assertTreeEquals(
			rlabel(fieldIDinField("hello word"), fieldIDinField("cool stuff going on"), rotationWrapper(
				rlabel(
					fieldIDinField("free", "free beer here"),
					fieldIDinField("wine there")
				)
			), fieldIDinField("sad it's just a test"))
		)
	}

	@Test def void documentationExampleLine1() {
		'''<f0> left|<f1> mid&#92; dle|<f2> right'''.assertNoErrors.assertTreeEquals(
			rlabel(
				fieldIDinField("f0", "left"),
				fieldIDinField("f1", "mid&#92; dle"),
				fieldIDinField("f2", "right")
			)
		)
	}

	@Test def void documentationExampleLine3() {
		'''hello&#92;nworld |{ b |{c|<here> d|e}| f}| g | h'''.assertNoErrors.assertTreeEquals(
			rlabel(
				fieldIDinField("hello&#92;nworld"),
				rotationWrapper(rlabel(
					fieldIDinField("b"),
					rotationWrapper(rlabel(
						fieldIDinField("c"),
						fieldIDinField("here", "d"),
						fieldIDinField("e")
					)),
					fieldIDinField("f")
				)),
				fieldIDinField("g"),
				fieldIDinField("h")
			)
		)
	}

	@Test def void complexExampleLineBreak() {
		'''
		hello&#92;nworld |{ b |{c|<here>
		 d
		 |e}| f}|
		g | h'''.assertNoErrors.assertTreeEquals(
			rlabel(
				fieldIDinField("hello&#92;nworld"),
				rotationWrapper(rlabel(
					fieldIDinField("b"),
					rotationWrapper(rlabel(
						fieldIDinField("c"),
						fieldIDinField("here", "d"),
						fieldIDinField("e")
					)),
					fieldIDinField("f")
				)),
				fieldIDinField("g"),
				fieldIDinField("h")
			)
		)
	}

	@Test def void complexLineBreakInString() {
		'''
		hello
		world |{ b |{c|<here>
		 d|e}| f}|
		g | h'''.assertNoErrors.assertTreeEquals(
			rlabel(
				fieldIDinField('''hello world'''), //this deviates from graphviz rendering
//				fieldID(''' // this should be the assertion, if rendering identical to graphviz
//				hello
//				world'''),
				rotationWrapper(rlabel(
					fieldIDinField("b"),
					rotationWrapper(rlabel(
						fieldIDinField("c"),
						fieldIDinField("here", "d"),
						fieldIDinField("e")
					)),
					fieldIDinField("f")
				)),
				fieldIDinField("g"),
				fieldIDinField("h")
			)
		)
	}

	@Test def void complexExampleUsingSpecialSignsRotated() {
		'''{Animal|+ name : string\l+ age : int\l|+ die() : void\l}'''.assertNoErrors.assertTreeEquals(
			rlabel(rotationWrapper(rlabel(
				fieldIDinField("Animal"),
				fieldIDinField('''+ name : string\l+ age : int\l'''),
				fieldIDinField('''+ die() : void\l''')
			)))
		)

	}

	@Test def void fieldIDsWithNoEntry() {
		'''<f0> (nil)| | |-1'''.assertNoErrors.assertTreeEquals(
			rlabel(
				fieldIDinField("f0", "(nil)"),
				fieldIDinField(null),
				fieldIDinField(null),
				fieldIDinField("-1")
			)
		)
	}

	// bad Syntax
	@Test def void singleClosePortFails() { '''>'''.assertSyntaxErrorLabel(">") }

	@Test def void singleCloseBraceFails() { '''}'''.assertSyntaxErrorLabel("}") }

	@Test def void missingOpenBraceFails() { '''}asas'''.assertSyntaxErrorLabel("}") }

	@Test def void escapedOpeningBraceFails() { '''\{ Hello }'''.assertSyntaxErrorLabel("}") }

	@Test def void escapedClosingBraceFails() { '''{ Hello \}'''.assertSyntaxErrorfieldIDinField("<EOF>") }

	@Test def void escapedOpeningPortFails() { '''\< Hello >'''.assertSyntaxErrorLabel(">") }

	@Test def void escapedClosingPortFails() { '''< Hello \>'''.assertSyntaxErrorPort("<EOF>") }

	@Test def void missingClosingPortFails() { '''< Hello'''.assertSyntaxErrorPort("<EOF>") }

	@Test def void portWithBraceFails() { '''< Hello }>'''.assertSyntaxErrorPort(">") }

	@Test def void braceUnclosedFirstFieldFails() { '''{ Hello | MoreHi'''.assertSyntaxErrorfieldIDinField("<EOF>") }

	@Test def void braceUnclosedSecondFieldFails() { '''hello|{ hslnh'''.assertSyntaxErrorfieldIDinField("<EOF>") }

	@Test def void wrongPosLabelFails() { '''sdsdsdsd<>'''.assertSyntaxErrorLabel("<") }

	@Test def void bracesInFieldFail() { '''This{Is}Illegal'''.assertSyntaxErrorLabel("{") }

	@Test def void bracesInMiddleFail() { '''This{Is}Illegal'''.assertSyntaxErrorLabel("{") }

	@Test def void bracesAfterPortNameFail() { '''<Port1>{Stuff}'''.assertSyntaxErrorLabel("{") }

	@Test def void complexBracesMistaken() { '''<f0> left|{ middle|<f2> right} boo'''.assertSyntaxErrorLabel("boo") }

	@Test def void missingABraceMiddle() {
		'''
		hello word | cool stuff going on | { <free> free beer here |
		<expensive wine there } | sad its just a test'''.assertSyntaxErrorRotationWrapper(">")
	}

	// validation tests
	@Test
	def void sameNamePortsSameLevel() {
		'''<here>|<here>'''.assertValidationErrorfieldIDinField(DotRecordLabelJavaValidator.PORT_NAME_DUPLICATE)
	}

	@Test
	def void sameNamePortsDifferentLevel() {
		'''a | <b> c | { <d> f | <b> f } | x'''.assertValidationErrorfieldID(
			DotRecordLabelJavaValidator.PORT_NAME_DUPLICATE,
			5,
			1
		).assertValidationErrorfieldID(
			DotRecordLabelJavaValidator.PORT_NAME_DUPLICATE,
			23,
			1
		)
	}

	@Test
	def void twoEmptyPortNamesNoError() {
		'''<> a | <> b'''.assertNoErrors()
	}

	@Test
	def void emptyPortNameWarning() {
		'''<>'''.parse.assertWarning(
			RecordlabelPackage.eINSTANCE.fieldID,
			DotRecordLabelJavaValidator.PORT_NAME_NOT_SET
		)
	}

	@Test
	def void complexEmptyPortNameWarning() {
		'''a | <b> c | { <d> f | <> f } | x'''.parse.assertWarning(
			RecordlabelPackage.eINSTANCE.fieldID,
			DotRecordLabelJavaValidator.PORT_NAME_NOT_SET
		)
	}

	@Test
	def void noWhitespaceWarning() {
		'''a | <b> coolstuff | { <d> f\ kinds | <f> f\nbut } | x'''.assertNoIssues
	}

	private def CharSequence assertValidationErrorfieldID(CharSequence content, String error, int offset,
		int length) {
		assertError(parse(content), RecordlabelPackage.eINSTANCE.fieldID, error, offset, length)
		content
	}

	private def CharSequence assertValidationErrorfieldIDinField(CharSequence content, String error) {
		assertError(parse(content), RecordlabelPackage.eINSTANCE.fieldID, error)
		return content
	}

	private def CharSequence assertNoIssues(CharSequence sequence) {
		sequence.parse.assertNoIssues
		return sequence
	}

	private def CharSequence assertNoErrors(CharSequence sequence) {
		sequence.parse.assertNoErrors
		return sequence
	}

	private def CharSequence assertSyntaxErrorLabel(CharSequence content, String character) {
		return assertSyntaxError(content, RecordlabelPackage.eINSTANCE.RLabel, "'" + character + "'")
	}

	private def CharSequence assertSyntaxErrorRotationWrapper(CharSequence content, String character) {
		return assertSyntaxError(content, RecordlabelPackage.eINSTANCE.field, "'" + character + "'")
	}

	private def CharSequence assertSyntaxErrorfieldIDinField(CharSequence content, String character) {
		return assertSyntaxError(content, RecordlabelPackage.eINSTANCE.fieldID, "'" + character + "'")
	}

	private def CharSequence assertSyntaxErrorPort(CharSequence content, String character) {
		return assertSyntaxError(content, RecordlabelPackage.eINSTANCE.fieldID, "'" + character + "'")
	}

	private def CharSequence assertSyntaxError(CharSequence content, EClass eClass, String message) {
		assertError(parse(content), eClass, "org.eclipse.xtext.diagnostics.Diagnostic.Syntax", message)
		return content
	}

	private def void assertTreeEquals(CharSequence sequenceForParsing, EObject expected) {
		sequenceForParsing.parse.assertTreeEquals(expected)
	}

	private def EObject assertTreeEquals(EObject actual, EObject expected) {
		assertEquals("Objects of different classtype ", expected.eClass, actual.eClass)
		for (attribute : expected.eClass.EAllAttributes) {
			assertEquals("Attribute " + attribute.name + " of class " + expected.eClass.name, expected.eGet(attribute),
				actual.eGet(attribute))
		}
		assertEquals("Number of Child Nodes", expected.eContents.size, actual.eContents.size)
		for (var i = 0; i < expected.eContents.size; i++) {
			actual.eContents.get(i).assertTreeEquals(expected.eContents.get(i))
		}
		return actual
	}

	private def RLabel rlabel(Field... fields) {
		val label = RecordlabelFactory.eINSTANCE.createRLabel
		label.fields.addAll(fields)
		return label
	}

	private def Field fieldIDinField(String port, String name) {
		val fieldID = RecordlabelFactory.eINSTANCE.createFieldID
		fieldID.name = name
		if (port !== null) {
			fieldID.portNamed = true
			if (port.length > 0)
				fieldID.port = port
		}
		val field = RecordlabelFactory.eINSTANCE.createField
		field.fieldID = fieldID
		return field
	}

	private def Field fieldIDinField(String name) {
		fieldIDinField(null, name)
	}


	private def Field rotationWrapper(RLabel label) {
		val wrapper = RecordlabelFactory.eINSTANCE.createField
		wrapper.label = label
		return wrapper
	}
}
