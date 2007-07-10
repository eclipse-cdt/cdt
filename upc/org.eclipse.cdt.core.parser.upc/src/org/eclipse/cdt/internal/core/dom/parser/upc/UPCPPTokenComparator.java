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

import org.eclipse.cdt.core.dom.c99.IPPTokenComparator;
import org.eclipse.cdt.core.dom.parser.c99.IToken;
import org.eclipse.cdt.core.dom.parser.c99.PPToken;

public class UPCPPTokenComparator implements IPPTokenComparator {

	public boolean compare(PPToken pptoken, IToken token) {
		if(token == null)
			return false;
		
		switch(token.getKind()) {
			case UPCParsersym.TK_Hash              : return pptoken == PPToken.HASH;
			case UPCParsersym.TK_HashHash          : return pptoken == PPToken.HASHHASH;
			case UPCParsersym.TK_LeftParen         : return pptoken == PPToken.LPAREN;
			case UPCParsersym.TK_NewLine           : return pptoken == PPToken.NEWLINE;
			case UPCParsersym.TK_Comma             : return pptoken == PPToken.COMMA;
			case UPCParsersym.TK_RightParen        : return pptoken == PPToken.RPAREN;
			case UPCParsersym.TK_DotDotDot         : return pptoken == PPToken.DOTDOTDOT;
			case UPCParsersym.TK_EOF_TOKEN         : return pptoken == PPToken.EOF;
			case UPCParsersym.TK_stringlit         : return pptoken == PPToken.STRINGLIT;
			case UPCParsersym.TK_integer           : return pptoken == PPToken.INTEGER;
			case UPCParsersym.TK_LT                : return pptoken == PPToken.LEFT_ANGLE_BRACKET;
			case UPCParsersym.TK_GT                : return pptoken == PPToken.RIGHT_ANGLE_BRACKET;
			case UPCParsersym.TK_SingleLineComment : return pptoken == PPToken.SINGLE_LINE_COMMENT;
			case UPCParsersym.TK_MultiLineComment  : return pptoken == PPToken.MULTI_LINE_COMMENT;
			// an identifier might be a preprocessing directive like #if or #include
			case UPCParsersym.TK_identifier : 
				PPToken result = PPToken.getDirective(token.toString());
				return pptoken == ((result == null) ? PPToken.IDENT : result);
		}
		return false;
	}
	

	public int getKind(int tokenToMake) {
		switch(tokenToMake) {
			case KIND_IDENTIFIER        : return UPCParsersym.TK_identifier;
			case KIND_EOF               : return UPCParsersym.TK_EOF_TOKEN;
			case KIND_COMPLETION        : return UPCParsersym.TK_Completion;
			case KIND_END_OF_COMPLETION : return UPCParsersym.TK_EndOfCompletion;
			case KIND_INTEGER           : return UPCParsersym.TK_integer;
			case KIND_STRINGLIT         : return UPCParsersym.TK_stringlit;
			case KIND_INVALID           : return UPCParsersym.TK_Invalid;
			default                     : return UPCParsersym.TK_Invalid;
		}
	}


	public String[] getLPGOrderedTerminalSymbols() {
		return UPCParsersym.orderedTerminalSymbols;
	}
}
