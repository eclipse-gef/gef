/*******************************************************************************
 * Copyright (c) 2014, 2017 itemis AG and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *     Tamas Miklossy  (itemis AG) - minor refactorings
 *
 *******************************************************************************/
package org.eclipse.gef.dot.internal.ui.language.highlighting;

import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper;

/**
 * A lexical highlighter that takes care of handling DOT lexer tokens properly.
 *
 * @author anyssen
 *
 */
public class DotAntlrTokenToAttributeIdMapper
		extends DefaultAntlrTokenToAttributeIdMapper {

	@Override
	protected String calculateId(String tokenName, int tokenType) {
		switch (tokenName) {
		// ensure CompassPt constants are lexically highlighted as STRING
		// tokens
		case "RULE_COMPASS_PT": //$NON-NLS-1$
		case "RULE_STRING": //$NON-NLS-1$
			return DotHighlightingConfiguration.STRING_ID;
		case "RULE_NUMERAL": //$NON-NLS-1$
			return DotHighlightingConfiguration.NUMERAL_ID;
		case "RULE_QUOTED_STRING": //$NON-NLS-1$
			return DotHighlightingConfiguration.QUOTED_STRING_ID;
		default:
			return super.calculateId(tokenName, tokenType);
		}
	}
}
