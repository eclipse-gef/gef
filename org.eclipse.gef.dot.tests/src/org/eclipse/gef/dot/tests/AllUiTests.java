/*******************************************************************************
 * Copyright (c) 2015, 2018 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *     Tamas Miklossy  (itemis AG) - merge DotInterpreter into DotImport (bug #491261)
 *                                 - implement additional test cases
 *
 *******************************************************************************/
package org.eclipse.gef.dot.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ // JUnit Tests
		Dot2ZestGraphCopierTests.class,
		Dot2ZestEdgeAttributesConversionTests.class,
		Dot2ZestNodeAttributesConversionTests.class, DotArrowTypeTests.class,
		DotAstTests.class, DotAttributeActiveAnnotationTests.class,
		DotAttributesTests.class, DotBSplineInterpolatorTests.class,
		DotColorListTests.class, DotEscStringTests.class,
		DotExecutableUtilsTests.class, DotExportTests.class,
		DotExtractorTests.class, DotFormatterTests.class,
		DotHtmlLabelFormatterTests.class, DotHtmlLabelLexerTests.class,
		DotHtmlLabelParserTests.class, DotHtmlLabelValidatorTests.class,
		DotImportTests.class, DotLexerTests.class, DotParserTests.class,
		DotPortPosTests.class, DotRecordLabelTests.class, DotStyleTests.class,
		DotValidatorTests.class,

		// JUnit Plug-in Tests
		DotContentAssistTests.class, DotEditorDoubleClickingTests.class,
		DotFoldingTests.class, DotHighlightingCalculatorTests.class,
		DotHighlightingTests.class, DotHoverTests.class,
		DotHtmlLabelContentAssistLexerTests.class,
		DotHtmlLabelContentAssistTests.class,
		DotHtmlLabelHighlightingLexerTests.class,
		DotHtmlLabelTokenTypeToPartitionMapperTests.class,
		DotHyperlinkNavigationTests.class, DotMarkingOccurrencesTests.class,
		DotOutlineViewTests.class, DotQuickfixTests.class,
		DotRenameRefactoringTests.class,
		DotTokenTypeToPartitionMapperTests.class })
public class AllUiTests {

}
