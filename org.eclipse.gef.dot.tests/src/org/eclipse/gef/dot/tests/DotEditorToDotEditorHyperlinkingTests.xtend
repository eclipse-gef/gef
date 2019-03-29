/*******************************************************************************
 * Copyright (c) 2018, 2019 itemis AG and others.
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
import org.eclipse.gef.dot.internal.language.DotUiInjectorProvider
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.text.hyperlink.IHyperlink
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.ui.editor.XtextEditorInfo
import org.eclipse.xtext.ui.editor.hyperlinking.XtextHyperlink
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(DotUiInjectorProvider)
class DotEditorToDotEditorHyperlinkingTests extends AbstractHyperlinkingTest {

	@Inject XtextEditorInfo editorInfo

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

	override protected hyperlinkIsOffered(IHyperlink[] hyperlinks, IRegion expectedRegion, String expectedHyperlinkTarget) {
		super.hyperlinkIsOffered(hyperlinks.filter(XtextHyperlink), expectedRegion, expectedHyperlinkTarget)
	}

	override protected getEditorId() {
		editorInfo.getEditorId
	}
}
