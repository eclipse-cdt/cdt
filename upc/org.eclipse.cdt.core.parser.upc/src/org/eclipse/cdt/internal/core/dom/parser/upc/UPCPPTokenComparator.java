/*******************************************************************************
* Copyright (c) 2006, 2007 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.upc;

import static org.eclipse.cdt.core.dom.parser.c99.PPToken.*;

import org.eclipse.cdt.core.dom.c99.IPPTokenComparator;
import org.eclipse.cdt.core.dom.parser.c99.IToken;
import org.eclipse.cdt.core.dom.parser.c99.PPToken;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.SynthesizedToken;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.Token;

public class UPCPPTokenComparator implements IPPTokenComparator {

	public PPToken getKind(IToken token) {
		if(token == null)
			return null;
		
		switch(token.getKind()) {
			case UPCParsersym.TK_Hash              : return HASH;
			case UPCParsersym.TK_HashHash          : return HASHHASH;
			case UPCParsersym.TK_LeftParen         : return LPAREN;
			case UPCParsersym.TK_NewLine           : return NEWLINE;
			case UPCParsersym.TK_Comma             : return COMMA;
			case UPCParsersym.TK_RightParen        : return RPAREN;
			case UPCParsersym.TK_DotDotDot         : return DOTDOTDOT;
			case UPCParsersym.TK_EOF_TOKEN         : return EOF;
			case UPCParsersym.TK_stringlit         : return STRINGLIT;
			case UPCParsersym.TK_integer           : return INTEGER;
			case UPCParsersym.TK_LT                : return LEFT_ANGLE_BRACKET;
			case UPCParsersym.TK_GT                : return RIGHT_ANGLE_BRACKET;
			case UPCParsersym.TK_SingleLineComment : return SINGLE_LINE_COMMENT;
			case UPCParsersym.TK_MultiLineComment  : return MULTI_LINE_COMMENT;
			case UPCParsersym.TK_identifier        : return IDENT;
		}
		return null;
	}
	

	public IToken createToken(int tokenToMake, int startOffset, int endOffset, String image) {
		int kind;
		switch(tokenToMake) {
			case KIND_IDENTIFIER        : kind = UPCParsersym.TK_identifier; break;
			case KIND_EOF               : kind = UPCParsersym.TK_EOF_TOKEN; break;
			case KIND_COMPLETION        : kind = UPCParsersym.TK_Completion; break;
			case KIND_END_OF_COMPLETION : kind = UPCParsersym.TK_EndOfCompletion; break;
			case KIND_INTEGER           : kind = UPCParsersym.TK_integer; break;
			case KIND_STRINGLIT         : kind = UPCParsersym.TK_stringlit; break;
			case KIND_INVALID           : kind = UPCParsersym.TK_Invalid; break;
			default                     : kind = UPCParsersym.TK_Invalid; break;
		}
		
		return new SynthesizedToken(startOffset, endOffset, kind, image);
	}

	public IToken cloneToken(IToken token) {
		if(token instanceof Token) {
			return ((Token)token).clone();
		}
		throw new RuntimeException("don't know what kind of token that is"); //$NON-NLS-1$
	}

	public String[] getLPGOrderedTerminalSymbols() {
		return UPCParsersym.orderedTerminalSymbols;
	}
	
}
