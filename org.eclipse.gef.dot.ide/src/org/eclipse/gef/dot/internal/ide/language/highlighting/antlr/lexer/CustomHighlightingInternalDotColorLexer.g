/*******************************************************************************
 * Copyright (c) 2016, 2020 itemis AG and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tamas Miklossy (itemis AG) - initial API and implementation (bug #461506)
 *
 *******************************************************************************/
lexer grammar CustomHighlightingInternalDotColorLexer;

@header {
package org.eclipse.gef.dot.internal.ide.language.highlighting.antlr.lexer;

// Hack: Use our own Lexer superclass by means of import.
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.parser.antlr.Lexer;
}

@members{
  private boolean isHexValue = false;
}

NumberSign : '#'{isHexValue = true;};

Comma : ',';

Solidus : '/';

RULE_COLOR_NUMBER : {!isHexValue}?=>('.' RULE_DIGITS+|RULE_ZERO_OR_ONE ('.' RULE_DIGITS+)?);

RULE_HEXADECIMAL_DIGIT : (RULE_DIGITS|'a'..'f'|'A'..'F');

RULE_COLOR_STRING : {!isHexValue}?=>('a'..'z'|'A'..'Z'|RULE_DIGITS)+;

fragment RULE_ZERO_OR_ONE : ('0'|'1');

fragment RULE_DIGITS : (RULE_ZERO_OR_ONE|'2'..'9');

RULE_WS : (' '|'\t'|'\r'|'\n')+;
