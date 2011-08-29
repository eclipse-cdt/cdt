/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.parser.scanner;

import static org.eclipse.cdt.core.parser.OffsetLimitReachedException.ORIGIN_PREPROCESSOR_DIRECTIVE;

import org.eclipse.cdt.core.parser.IPreprocessorDirective;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Helper class for detecting include guards 
 */
public class IncludeGuardDetection {
	public static char[] detectIncludeGuard(AbstractCharArray content, Lexer.LexerOptions lexOptions, CharArrayIntMap ppKeywords) {
		Lexer l= new Lexer(content, lexOptions, ILexerLog.NULL, null);
		char[] guard= findIncludeGuard(l, ppKeywords);
		if (guard != null && currentIfSpansFile(l, ppKeywords)) {
			return guard;
		}
		return null;
	}

	private static char[] findIncludeGuard(Lexer l, CharArrayIntMap ppKeywords) {
 		try {
  			if (skipAll(l, Lexer.tNEWLINE).getType() == IToken.tPOUND) { 
				Token t = l.nextToken();
				if (t.getType() == IToken.tIDENTIFIER) {
					char[] guard= null;
					switch(ppKeywords.get(t.getCharImage())) {
					case IPreprocessorDirective.ppIfndef:
						// #ifndef GUARD
						t= l.nextToken();
						if (t.getType() == IToken.tIDENTIFIER) {
							guard= t.getCharImage();
						}
						break;
					case IPreprocessorDirective.ppIf:
						// #if !defined GUARD
						// #if ((!((defined (GUARD)))))
						
						if (skipAll(l, IToken.tLPAREN).getType() == IToken.tNOT
								&& CharArrayUtils.equals(Keywords.cDEFINED, skipAll(l, IToken.tLPAREN).getCharImage())) {
							t= l.nextToken(); // only a single parenthesis is allowed
							if (t.getType() == IToken.tLPAREN)
								t= l.nextToken();
							if (t.getType() == IToken.tIDENTIFIER) {
								guard= t.getCharImage();
							}
							if (skipAll(l, IToken.tRPAREN).getType() != Lexer.tNEWLINE)
								guard= null;
						} 
						break;
					}
					if (guard != null) {
						// #define GUARD
						l.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
						if (skipAll(l, Lexer.tNEWLINE).getType() == IToken.tPOUND
								&& CharArrayUtils.equals(Keywords.cDEFINE, l.nextToken().getCharImage())
								&& CharArrayUtils.equals(guard, l.nextToken().getCharImage())) {
							l.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
							return guard;
						}
					}
				}
  			}
 		} catch (OffsetLimitReachedException e) {
 		}
 		return null;
 	}
	
	private static boolean currentIfSpansFile(Lexer l, CharArrayIntMap ppKeywords) {
		// Check if the #ifndef spans the entire file
		try {
			int nesting= 1;
			while (nesting > 0) {
				Token t= l.nextDirective();
				if (t.getType() == IToken.tEND_OF_INPUT)
					return true;
				switch(ppKeywords.get(l.nextToken().getCharImage())) {
				case IPreprocessorDirective.ppIf:
				case IPreprocessorDirective.ppIfdef:
				case IPreprocessorDirective.ppIfndef:
					nesting++;
					break;
				case IPreprocessorDirective.ppEndif:
					nesting--;
					break;
				}
			}
			l.consumeLine(ORIGIN_PREPROCESSOR_DIRECTIVE);
			return skipAll(l, Lexer.tNEWLINE).getType() == IToken.tEND_OF_INPUT;
		} catch (OffsetLimitReachedException e) {
		}
		return true;
	}
	
	private static Token skipAll(Lexer l, int kind) throws OffsetLimitReachedException {
		// Skip empty lines
		Token t= l.nextToken();
		while (t.getType() == kind)
			t= l.nextToken();
		return t;
	}
}
