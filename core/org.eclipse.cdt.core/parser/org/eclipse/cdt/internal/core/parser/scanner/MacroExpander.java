/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;
import java.util.IdentityHashMap;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;

/**
 * Utility class to perform macro expansion.
 * @since 5.0
 */
public class MacroExpander {
	private static final int ORIGIN = OffsetLimitReachedException.ORIGIN_MACRO_EXPANSION;	
	
	/** 
	 * Marks the beginning and the end of the scope of a macro expansion. Necessary to properly
	 * handle recursive expansions and to figure out whether spaces are required during a stringify
	 * operation across such boundaries.
	 */
	public static final class ExpansionBoundary extends Token {
		private PreprocessorMacro fScope;
		private boolean fIsStart;

		ExpansionBoundary(PreprocessorMacro scope, int offset, boolean isStart) {
			super(CPreprocessor.tSCOPE_MARKER, offset, offset);
			fScope= scope;
			fIsStart= isStart;
		}

		public char[] getCharImage() {
			return CharArrayUtils.EMPTY;
		}
		
		public String toString() {
			return "{" + (fIsStart ? '+' : '-') +  //$NON-NLS-1$
				(fScope == null ? String.valueOf(getOffset()) : fScope.getName()) + '}'; 
		}

		public void execute(IdentityHashMap forbidden) {
			if (fIsStart) {
				forbidden.put(fScope, fScope);
			}
			else {
				forbidden.remove(fScope);
			}
			fScope= null;
		}

		public Object clone() {
			// when cloned for the purpose of argument substitution, the boundaries no longer prevent a 
			// recursive macro expansion.
			ExpansionBoundary t= (ExpansionBoundary) super.clone();
			t.fScope= null;
			return t;
		}
	}
	
	/** 
	 * Combines a list of tokens with the preprocessor to form the input for macro expansion.
	 */
	private class TokenSource extends TokenList {
		private boolean fUseCpp;

		public TokenSource(boolean useCpp) {
			fUseCpp= true;
		}

		public Token fetchFirst() throws OffsetLimitReachedException {
			Token t= removeFirst();
			if (t == null && fUseCpp) {
				t= fCpp.fetchTokenFromPreprocessor();
				fEndOffset= t.getEndOffset();
			}
			return t;
		}

		public boolean findLParenthesis(IdentityHashMap forbidden) throws OffsetLimitReachedException {
			Token t= first();
			while (t != null) {
				switch (t.getType()) {
				case Lexer.tNEWLINE:
					break;
				case CPreprocessor.tSCOPE_MARKER:
					((ExpansionBoundary) t).execute(forbidden);
					break;
				case IToken.tLPAREN:
					return true;
				default:
					return false;
				}
				removeFirst();
				t= first();
			}

			if (fUseCpp) {
				return fCpp.findLParenthesisInContext();
			}
			return false;
		}
	}

	private final MacroDefinitionParser fDefinitionParser;
	private final CharArrayObjectMap fDictionary;
	private final LocationMap fLocationMap;
	private final CPreprocessor fCpp;
	private final LexerOptions fLexOptions;
	private int fEndOffset;	
	private ArrayList fImplicitMacroExpansions= new ArrayList();
	private boolean fCompletionMode;
	private int fStartOffset;

	public MacroExpander(CPreprocessor cpp, CharArrayObjectMap dict, LocationMap locationMap, MacroDefinitionParser mdp, LexerOptions lexOptions) {
		fCpp= cpp;
		fDictionary= dict;
		fLocationMap= locationMap;
		fDefinitionParser= mdp;
		fLexOptions= lexOptions;
	}
	
	/** 
	 * Expects that the identifier has been consumed, stores the result in the list provided and returns the
	 * end offset of the last token read from the preprocessor input.
	 */
	public int expand(PreprocessorMacro macro, Token identifier, boolean completionMode, TokenList expansion) throws OffsetLimitReachedException {
		fStartOffset= identifier.getOffset();
		fEndOffset= identifier.getEndOffset();
		fCompletionMode= completionMode;
		
		IdentityHashMap forbidden= new IdentityHashMap();
		
		// setup input sequence
		TokenSource input= new TokenSource(true);
		TokenList firstExpansion= expandOne(macro, forbidden, input, fStartOffset, fEndOffset);
		input.prepend(firstExpansion);
		
		expandAll(input, forbidden, expansion);
		return fEndOffset;
	}

	/**
	 * Expects that the identifier of the macro expansion has been consumed.
	 */
	private TokenList expandOne(PreprocessorMacro macro, IdentityHashMap forbidden, TokenSource input, int offset, int endOffset) 
			throws OffsetLimitReachedException {
		TokenList result= new TokenList();
		result.append(new ExpansionBoundary(macro, offset, true));
		if (macro.isFunctionStyle()) {
			final TokenSource[] argInputs= new TokenSource[macro.getParameterPlaceholderList().length];
			endOffset= parseArguments(input, (FunctionStyleMacro) macro, argInputs);
			TokenList[] clonedArgs= new TokenList[argInputs.length];
			TokenList[] expandedArgs= new TokenList[argInputs.length];
			for (int i = 0; i < argInputs.length; i++) {
				final TokenSource argInput = argInputs[i];
				clonedArgs[i]= argInput.cloneTokens();
				final TokenList expandedArg= new TokenList();
				expandAll(argInput, forbidden, expandedArg);
				expandedArgs[i]= expandedArg;
			}
			replaceArgs(macro, clonedArgs, expandedArgs, result);
		}
		else {
			objStyleTokenPaste(macro, macro.getTokens(fDefinitionParser, fLexOptions), result);
		}
		result.append(new ExpansionBoundary(macro, endOffset, false));
		return result;
	}

	private void expandAll(TokenSource input, IdentityHashMap forbidden, TokenList result) throws OffsetLimitReachedException {
		Token t= input.removeFirst();
		while(t != null) {
			switch(t.getType()) {
			case CPreprocessor.tSCOPE_MARKER:
				((ExpansionBoundary) t).execute(forbidden);
				result.append(t);
				break;
			case IToken.tIDENTIFIER:
				PreprocessorMacro macro= (PreprocessorMacro) fDictionary.get(t.getCharImage());
				if (macro != null && !forbidden.containsKey(macro)) {
					final boolean isFunctionStyle= macro.isFunctionStyle();
					if (!isFunctionStyle || input.findLParenthesis(forbidden)) {
						// mstodo- image location
						fImplicitMacroExpansions.add(fLocationMap.encounterImplicitMacroExpansion(macro, null));
						TokenList replacement= expandOne(macro, forbidden, input, t.getOffset(), t.getEndOffset());
						input.prepend(replacement);
						t= null;
					}
				}
				if (t != null) {
					t.setType(CPreprocessor.tEXPANDED_IDENTIFIER); // prevent any further expansion
					result.append(t); 
				}
				break;
			default:
				result.append(t); 
				break;
			}
			t= input.removeFirst();
		}
	}

	/**
	 * Expects that the identifier has been consumed.
	 * @throws OffsetLimitReachedException 
	 */
	private int parseArguments(TokenSource input, FunctionStyleMacro macro, TokenSource[] result) throws OffsetLimitReachedException {
		final int argCount= macro.getParameterPlaceholderList().length;
		final boolean hasVarargs= macro.hasVarArgs() != FunctionStyleMacro.NO_VAARGS;
		final int requiredArgs= hasVarargs ? argCount-1 : argCount;
		int endOffset= 0;
		int idx= 0;
		int nesting = -1;
		for (int i = 0; i < result.length; i++) {
			result[i]= new TokenSource(false);
		}
		
        loop: while (true) {
    		Token t= input.fetchFirst();
    		if (t == null) {
    			break loop;
    		}
			endOffset= t.getEndOffset();
        	switch(t.getType()) {
        	case Lexer.tEND_OF_INPUT:
        		if (fCompletionMode) {
        			throw new OffsetLimitReachedException(ORIGIN, null);
        		}
        		break loop;
        	case IToken.tCOMPLETION:
        		throw new OffsetLimitReachedException(ORIGIN, t);
        		
        	case Lexer.tNEWLINE:
        		assert false; // we should not get any newlines from macros or the preprocessor.
        		break;

        	case IToken.tLPAREN:
        		if (++nesting > 0) {
        			result[idx].append(t);
        		}
        		break;
        	case IToken.tRPAREN:
        		if (--nesting < 0) {
        			idx++;
        			break loop;
        		}
        		result[idx].append(t);
        		break;
        		
        	case IToken.tCOMMA:
        		if (nesting == 0) {
        			if (idx < argCount-1) { // next argument
        				idx++;
            			break;
        			}
        			else if (!hasVarargs) {
        				// too many arguments
        				handleProblem(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macro.getNameCharArray());
        				break loop;
        			}
        		}
        		// part of argument
        		result[idx].append(t);
        		break;
        		
        	default:
        		if (nesting < 0) {
        			assert false; // no leading parenthesis, which is checked before the method is called.
        			break loop;
        		}
        		result[idx].append(t);
        		break;
        	}
        }
		
        if (idx < requiredArgs) {
            handleProblem(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macro.getNameCharArray());
        }
        return endOffset;
	}
	
	private void handleProblem(int problemID, char[] arg) {
		fCpp.handleProblem(problemID, arg, fStartOffset, fEndOffset);
	}

	private void replaceArgs(PreprocessorMacro macro, TokenList[] args, TokenList[] expandedArgs, TokenList result) {
		TokenList input= macro.getTokens(fDefinitionParser, fLexOptions);
		
		Token n;       
		Token pasteArg1= null;  
		for (Token t= input.first(); t != null; t=n) {
			n= (Token) t.getNext();
			boolean pasteNext= n != null && n.getType() == IToken.tPOUNDPOUND;

			switch(t.getType()) {
			case CPreprocessor.tMACRO_PARAMETER:
				int idx= ((PlaceHolderToken) t).getIndex();
				if (idx < args.length) { // be defensive
					TokenList arg= pasteNext ? args[idx] : expandedArgs[idx];
					pasteArg1= cloneAndAppend(arg.first(), result, pasteNext);
				}
				break;
				
			case IToken.tPOUND:
				StringBuffer buf= new StringBuffer();
				buf.append('"');
				if (n != null && n.getType() == CPreprocessor.tMACRO_PARAMETER) {
					idx= ((PlaceHolderToken) n).getIndex();
					if (idx < args.length) { // be defensive
						stringify(args[idx], buf);
					}
					n= (Token) n.getNext();
					pasteNext= n != null && n.getType() == IToken.tPOUNDPOUND;
				}
				buf.append('"');
				final int length= buf.length(); 
				final char[] image= new char[length];
				buf.getChars(0, length, image, 0);
				
				pasteArg1= appendToResult(new ImageToken(IToken.tSTRING, 0, 0, image), result, pasteNext);
				break;
				
			case IToken.tPOUNDPOUND:
				if (pasteArg1 != null) {
					Token pasteArg2= null;
					Token rest= null;
					if (n != null) {
						if (n.getType() == CPreprocessor.tMACRO_PARAMETER) {
							idx= ((PlaceHolderToken) n).getIndex();
							if (idx < args.length) { // be defensive
								TokenList arg= args[idx];
								pasteArg2= arg.first();
								if (pasteArg2 != null) {
									rest= (Token) pasteArg2.getNext();
								}
							}
						}
						else {
							pasteArg2= n;
						}
						n= (Token) n.getNext();
						pasteNext= n != null && n.getType() == IToken.tPOUNDPOUND;
					}
					
					t= tokenpaste(pasteArg1, pasteArg2, macro);
					if (t != null) {
						pasteArg1= appendToResult((Token) t.clone(), result, pasteNext && rest == null);
					}
					if (rest != null) {
						pasteArg1= cloneAndAppend(rest, result, pasteNext);
					}
				}
				break;
				
			default:
				pasteArg1= appendToResult((Token) t.clone(), result, pasteNext);
				break;
			}
		}
	}

	private void objStyleTokenPaste(PreprocessorMacro macro, TokenList input, TokenList result) {
		Token n;       
		Token pasteArg1= null;  
		for (Token t= input.first(); t != null; t=n) {
			n= (Token) t.getNext();
			boolean pasteNext= n != null && n.getType() == IToken.tPOUNDPOUND;

			switch(t.getType()) {
			case IToken.tPOUNDPOUND:
				if (pasteArg1 != null) {
					Token pasteArg2= null;
					if (n != null) {
						pasteArg2= n;
						n= (Token) n.getNext();
						pasteNext= n != null && n.getType() == IToken.tPOUNDPOUND;
					}
					
					t= tokenpaste(pasteArg1, pasteArg2, macro);
					if (t != null) {
						pasteArg1= appendToResult((Token) t.clone(), result, pasteNext);
					}
				}
				break;
				
			default:
				pasteArg1= appendToResult((Token) t.clone(), result, pasteNext);
				break;
			}
		}
	}

	private Token appendToResult(Token t, TokenList result, boolean pasteNext) {
		if (pasteNext) {
			return t;
		}
		result.append(t);
		return null;
	}

	private Token cloneAndAppend(Token tokens, TokenList result, boolean pasteNext) {
		Token t= tokens;
		Token r= t == null ? null : (Token) t.getNext();
		while (r != null) {
			result.append((Token) t.clone());
			t= r;
			r= (Token) r.getNext();
		}
		if (t != null && !pasteNext) {
			result.append((Token) t.clone());
			return null;
		}
		return t;
	}

	private Token tokenpaste(Token arg1, Token arg2, PreprocessorMacro macro) {
		if (arg2 == null) {
			if (arg1.getType() == IToken.tCOMMA) {	// gcc-extension for variadic macros
				return null;
			}
			return arg1;
		}
		
		final char[] image1= arg1.getCharImage();
		final char[] image2= arg2.getCharImage();
		final int l1 = image1.length;
		final int l2 = image2.length;
		final char[] image= new char[l1+l2];
		System.arraycopy(image1, 0, image, 0, l1);
		System.arraycopy(image2, 0, image, l1, l2);
		Lexer lex= new Lexer(image, fLexOptions, ILexerLog.NULL);
		try {
			Token t1= lex.nextToken();
			Token t2= lex.nextToken();
			if (t1.getType() != Lexer.tEND_OF_INPUT && t2.getType() == Lexer.tEND_OF_INPUT) {
				t1.setOffset(arg1.getOffset(), arg2.getEndOffset());
				return t1;
			}
		} catch (OffsetLimitReachedException e) {
		}
		handleProblem(IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, macro.getNameCharArray());
		return null;
	}

	private void stringify(TokenList tokenList, StringBuffer buf) {
		Token t= tokenList.first();
		if (t == null) {
			return;
		}
		int endOffset= t.getOffset();
		for (; t != null; t= (Token) t.getNext()) {
			switch(t.getType()) {
			case IToken.tSTRING:
			case IToken.tLSTRING:
			case IToken.tCHAR:
			case IToken.tLCHAR:
				if (endOffset < t.getOffset()) {
					buf.append(' ');
				}
				endOffset= t.getEndOffset();
				final char[] image= t.getCharImage();
				for (int i = 0; i < image.length; i++) {
					final char c = image[i];
					if (c == '"' || c == '\\') {
						buf.append('\\');
					}
					buf.append(c);
				}
				break;
				
			case CPreprocessor.tSCOPE_MARKER:
				ExpansionBoundary sm= (ExpansionBoundary) t;
				if (sm.fIsStart) {
					if (endOffset < t.getOffset()) {
						buf.append(' ');
					}
					endOffset= Integer.MAX_VALUE;
				}
				else {
					endOffset= t.getEndOffset();
				}
				break;
				
			default:
				if (endOffset < t.getOffset()) {
					buf.append(' ');
				}
				endOffset= t.getEndOffset();
				buf.append(t.getCharImage());
				break;
			}

		}
	}
	
	public IASTName[] createImplicitExpansions() {
		IASTName[] result= (IASTName[]) fImplicitMacroExpansions.toArray(new IASTName[fImplicitMacroExpansions.size()]);
		fImplicitMacroExpansions.clear();
		return result;
	}

	public ImageLocationInfo[] createImageLocations(TokenList replacement) {
		// mstodo- image locations
		return ImageLocationInfo.NO_LOCATION_INFOS;
	}


	public int adjustOffsets(TokenList replacement) {
		int offset= 0;
		Token l= null;
		for (Token t= replacement.first(); t!=null; t= (Token) t.getNext()) {
			switch(t.getType()) {
			case CPreprocessor.tEXPANDED_IDENTIFIER:
				t.setType(IToken.tIDENTIFIER);
				break;
			case CPreprocessor.tSCOPE_MARKER:
				replacement.removeBehind(l);
				continue;
			}
			t.setOffset(offset, ++offset);
			l= t;
		}
		return offset;
	}
}
