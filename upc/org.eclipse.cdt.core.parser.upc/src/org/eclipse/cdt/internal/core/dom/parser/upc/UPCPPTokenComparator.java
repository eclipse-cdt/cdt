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

import static org.eclipse.cdt.core.dom.parser.c99.PPToken.AND;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.ANDAND;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.BANG;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.CARET;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.CHARCONST;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.COLON;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.COMMA;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.DOTDOTDOT;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.EOF;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.EQ;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.GE;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.GT;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.HASH;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.HASHHASH;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.IDENT;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.INTEGER;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.LE;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.LEFTSHIFT;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.LPAREN;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.LT;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.MINUS;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.MULTI_LINE_COMMENT;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.NE;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.NEWLINE;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.OR;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.OROR;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.PERCENT;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.PLACEMARKER;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.PLUS;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.QUESTION;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.RIGHTSHIFT;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.RPAREN;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.SINGLE_LINE_COMMENT;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.SLASH;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.STAR;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.STRINGLIT;
import static org.eclipse.cdt.core.dom.parser.c99.PPToken.TILDE;
import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.c99.IPPTokenComparator;
import org.eclipse.cdt.core.dom.parser.c99.PPToken;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.SynthesizedToken;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.Token;

public class UPCPPTokenComparator implements IPPTokenComparator<IToken> {

private static final int PLACEMARKER_VALUE = Integer.MAX_VALUE;
	
	
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
			case UPCParsersym.TK_SingleLineComment : return SINGLE_LINE_COMMENT;
			case UPCParsersym.TK_MultiLineComment  : return MULTI_LINE_COMMENT;
			case UPCParsersym.TK_identifier        : return IDENT;
			case UPCParsersym.TK_charconst         : return CHARCONST;
			
			case UPCParsersym.TK_And        : return AND;
			case UPCParsersym.TK_Star       : return STAR;
			case UPCParsersym.TK_Plus       : return PLUS;
			case UPCParsersym.TK_Minus      : return MINUS;
			case UPCParsersym.TK_Tilde      : return TILDE;
			case UPCParsersym.TK_Bang       : return BANG;
			case UPCParsersym.TK_Slash      : return SLASH;
			case UPCParsersym.TK_Percent    : return PERCENT;
			case UPCParsersym.TK_RightShift : return RIGHTSHIFT;
			case UPCParsersym.TK_LeftShift  : return LEFTSHIFT;
			case UPCParsersym.TK_LT         : return LT;
			case UPCParsersym.TK_GT         : return GT;
			case UPCParsersym.TK_LE         : return LE;
			case UPCParsersym.TK_GE         : return GE;
			case UPCParsersym.TK_EQ         : return EQ;
			case UPCParsersym.TK_NE         : return NE;
			case UPCParsersym.TK_Caret      : return CARET;
			case UPCParsersym.TK_Or         : return OR;
			case UPCParsersym.TK_AndAnd     : return ANDAND;
			case UPCParsersym.TK_OrOr       : return OROR;
			case UPCParsersym.TK_Question   : return QUESTION;
			case UPCParsersym.TK_Colon      : return COLON;
			
			// TODO: will removing this case cause the switch to compile into a tableswitch bytecode?
			// tableswitch is faster than lookupswitch
			case PLACEMARKER_VALUE : return PLACEMARKER;
		}
		return null;
	}
	

	public IToken createToken(int tokenToMake, int startOffset, int endOffset, String image) {
		int kind;
		switch(tokenToMake) {
			case KIND_IDENTIFIER        : kind = UPCParsersym.TK_identifier; break;
			case KIND_COMPLETION        : kind = UPCParsersym.TK_Completion; break;
			case KIND_END_OF_COMPLETION : kind = UPCParsersym.TK_EndOfCompletion; break;
			case KIND_INTEGER           : kind = UPCParsersym.TK_integer; break;
			case KIND_STRINGLIT         : kind = UPCParsersym.TK_stringlit; break;
			case KIND_INVALID           : kind = UPCParsersym.TK_Invalid; break;
			case KIND_PLACEMARKER       : kind = PLACEMARKER_VALUE; break;
			default                     : kind = UPCParsersym.TK_Invalid; break;
		}
		
		return new SynthesizedToken(startOffset, endOffset, kind, image);
	}

	public IToken cloneToken(IToken token) {
		if(token instanceof Token) {
			return (IToken)((Token)token).clone();
		}
		throw new RuntimeException("don't know what kind of token that is"); //$NON-NLS-1$
	}


	public int getEndOffset(IToken token) {
		return token.getEndOffset();
	}

	public int getStartOffset(IToken token) {
		return token.getStartOffset();
	}

	public void setEndOffset(IToken token, int offset) {
		token.setEndOffset(offset);
	}

	public void setStartOffset(IToken token, int offset) {
		token.setStartOffset(offset);
	}
}
