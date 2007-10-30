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
		private boolean fIsStart;
		private final PreprocessorMacro fMacro;

		ExpansionBoundary(PreprocessorMacro scope, boolean isStart) {
			super(CPreprocessor.tSCOPE_MARKER, null, 0, 0);
			fMacro= scope;
			fIsStart= isStart;
		}

		public char[] getCharImage() {
			return CharArrayUtils.EMPTY;
		}
		
		public String toString() {
			return "{" + (fIsStart ? '+' : '-') + fMacro.getName() + '}';  //$NON-NLS-1$
		}

		public void execute(IdentityHashMap forbidden) {
			if (fIsStart) {
				forbidden.put(fMacro, fMacro);
			}
			else {
				forbidden.remove(fMacro);
			}
		}
	}
		
	/** 
	 * Combines a list of tokens with the preprocessor to form the input for macro expansion.
	 */
	private class TokenSource extends TokenList {
		private boolean fUseCpp;

		public TokenSource(boolean useCpp) {
			fUseCpp= useCpp;
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
				case CPreprocessor.tSPACE:
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
		TokenList firstExpansion= new TokenList();
		expandOne(identifier, macro, forbidden, input, firstExpansion);
		input.prepend(firstExpansion);
		
		expandAll(input, forbidden, expansion);
		return fEndOffset;
	}

	/**
	 * Expects that the identifier of the macro expansion has been consumed.
	 * Returns the last token of the expansion.
	 */
	private Token expandOne(Token lastConsumed, PreprocessorMacro macro, IdentityHashMap forbidden, TokenSource input, TokenList result) 
			throws OffsetLimitReachedException {
		result.append(new ExpansionBoundary(macro, true));
		if (macro.isFunctionStyle()) {
			final TokenSource[] argInputs= new TokenSource[macro.getParameterPlaceholderList().length];
			lastConsumed= parseArguments(input, (FunctionStyleMacro) macro, forbidden, argInputs);
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
		result.append(new ExpansionBoundary(macro, false));
		return lastConsumed;
	}

	private void expandAll(TokenSource input, IdentityHashMap forbidden, TokenList result) throws OffsetLimitReachedException {
		Token l= null;
		Token t= input.removeFirst();
		while(t != null) {
			switch(t.getType()) {
			case CPreprocessor.tSCOPE_MARKER:
				((ExpansionBoundary) t).execute(forbidden);
				break;
			case IToken.tIDENTIFIER:
				PreprocessorMacro macro= (PreprocessorMacro) fDictionary.get(t.getCharImage());
				if (macro != null && !forbidden.containsKey(macro)) {
					final boolean isFunctionStyle= macro.isFunctionStyle();
					if (!isFunctionStyle || input.findLParenthesis(forbidden)) {
						// mstodo- image location
						fImplicitMacroExpansions.add(fLocationMap.encounterImplicitMacroExpansion(macro, null));
						TokenList replacement= new TokenList();
						if (l != null && l.hasGap(t)) {
							replacement.append(space());
						}
						Token last= expandOne(t, macro, forbidden, input, replacement); 
						Token n= input.first();
						if (n != null && last.hasGap(n)) {
							replacement.append(space());
						}
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
			l= t;
			t= input.removeFirst();
		}
	}

	/**
	 * Expects that the identifier has been consumed.
	 * @param forbidden 
	 * @throws OffsetLimitReachedException 
	 */
	private Token parseArguments(TokenSource input, FunctionStyleMacro macro, IdentityHashMap forbidden, TokenSource[] result) throws OffsetLimitReachedException {
		final int argCount= macro.getParameterPlaceholderList().length;
		final boolean hasVarargs= macro.hasVarArgs() != FunctionStyleMacro.NO_VAARGS;
		final int requiredArgs= hasVarargs ? argCount-1 : argCount;
		int idx= 0;
		int nesting= 0;
		for (int i = 0; i < result.length; i++) {
			result[i]= new TokenSource(false);
		}
		
		Token lastToken= input.fetchFirst();
		assert lastToken != null && lastToken.getType() == IToken.tLPAREN;
		
		boolean complete= false;
		boolean isFirstOfArg= true;
		Token space= null;
        loop: while (true) {
    		Token t= input.fetchFirst();
    		if (t == null) {
    			break loop;
    		}
			lastToken= t;
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
        		continue loop;

        	case IToken.tLPAREN:
        		++nesting;
        		break;
        		
        	case IToken.tRPAREN:
        		if (--nesting < 0) {
        			complete= true;
        			break loop;
        		}
        		break;
        		
        	case IToken.tCOMMA:
        		if (nesting == 0) {
        			if (idx < argCount-1) { // next argument
        				isFirstOfArg= true;
        				space= null;
        				idx++;
            			continue loop;
        			}
        			else if (!hasVarargs) {
        				break loop;
        			}
        		}
        		break;
        		
        	case CPreprocessor.tSCOPE_MARKER:
        		if (argCount == 0) {
        			((ExpansionBoundary) t).execute(forbidden);
        		}
        		else {
        			result[idx].append(t);
        		}
        		continue loop;
        		
        	case CPreprocessor.tSPACE:
        		if (!isFirstOfArg) {
        			space= t;
        		}
        		continue loop;
        	}
    		if (argCount == 0) {
    			break loop;
    		}
    		if (space != null) {
    			result[idx].append(space);
    			space= null;
    		}
    		result[idx].append(t);
    		isFirstOfArg= false;
        }

		if (!complete || idx+1 < requiredArgs) {
			handleProblem(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macro.getNameCharArray());
		}
        return lastToken;
	}
	
	private void handleProblem(int problemID, char[] arg) {
		fCpp.handleProblem(problemID, arg, fStartOffset, fEndOffset);
	}

	private void replaceArgs(PreprocessorMacro macro, TokenList[] args, TokenList[] expandedArgs, TokenList result) {
		TokenList input= macro.getTokens(fDefinitionParser, fLexOptions);
		
		Token l= null;
		Token n;       
		Token pasteArg1= null;  
		for (Token t= input.first(); t != null; l=t, t=n) {
			n= (Token) t.getNext();
			boolean pasteNext= n != null && n.getType() == IToken.tPOUNDPOUND;

			switch(t.getType()) {
			case CPreprocessor.tMACRO_PARAMETER:
				if (l != null && l.hasGap(t)) {
					result.append(space());
				}
				int idx= ((PlaceHolderToken) t).getIndex();
				if (idx < args.length) { // be defensive
					TokenList arg= pasteNext ? args[idx] : expandedArgs[idx];
					pasteArg1= cloneAndAppend(arg.first(), result, pasteNext);
				}
				if (n != null && t.hasGap(n)) {
					result.append(space());
				}
				break;
				
			case IToken.tPOUND:
				if (l != null && l.hasGap(t)) {
					result.append(space());
				}
				StringBuffer buf= new StringBuffer();
				buf.append('"');
				if (n != null && n.getType() == CPreprocessor.tMACRO_PARAMETER) {
					idx= ((PlaceHolderToken) n).getIndex();
					if (idx < args.length) { // be defensive
						stringify(args[idx], buf);
					}
					t= n;
					n= (Token) n.getNext();
					pasteNext= n != null && n.getType() == IToken.tPOUNDPOUND;
				}
				buf.append('"');
				final int length= buf.length(); 
				final char[] image= new char[length];
				buf.getChars(0, length, image, 0);
				
				pasteArg1= appendToResult(new ImageToken(IToken.tSTRING, null, 0, 0, image), result, pasteNext);
				if (!pasteNext && n != null && t.hasGap(n)) {
					result.append(space());
				}
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
								
								// gcc-extension
								if (idx == args.length-1 && macro.hasVarArgs() != FunctionStyleMacro.NO_VAARGS) {
									if (pasteArg1.getType() == IToken.tCOMMA) {
										if (pasteArg2 == null) {
											pasteArg1= null;
										}
										else {
											pasteArg2.setNext(rest);
											rest= pasteArg2;
											pasteArg2= null;
										}
									}
								}
							}
						}
						else {
							pasteArg2= n;
						}
						t= n;
						n= (Token) n.getNext();
						pasteNext= n != null && n.getType() == IToken.tPOUNDPOUND;
					}
					Token tp= tokenpaste(pasteArg1, pasteArg2, macro);
					if (tp != null) {
						pasteArg1= appendToResult((Token) tp.clone(), result, pasteNext && rest == null);
					}
					if (rest != null) {
						pasteArg1= cloneAndAppend(rest, result, pasteNext);
					}
					if (!pasteNext && n != null && t.hasGap(n)) {
						result.append(space());
					}
				}
				break;
				
			default:
				pasteArg1= appendToResult((Token) t.clone(), result, pasteNext);
				break;
			}
		}
	}

	private SimpleToken space() {
		return new SimpleToken(CPreprocessor.tSPACE, null, 0, 0);
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
		if (t == null) {
			return null;
		}
		Token n= (Token) t.getNext();
		Token p= null;
		while (n != null) {
			result.append((Token) t.clone());
			p= t;
			t= n;
			n= (Token) n.getNext();
		}
		if (t != null && !pasteNext) {
			result.append((Token) t.clone());
			return null;
		}
		if (p != null && p.hasGap(t)) {
			result.append(space());
		}
		return t;
	}

	private Token tokenpaste(Token arg1, Token arg2, PreprocessorMacro macro) {
		if (arg2 == null) {
			return arg1;
		}
		final char[] image1= arg1.getCharImage();
		final char[] image2= arg2.getCharImage();
		final int l1 = image1.length;
		final int l2 = image2.length;
		final char[] image= new char[l1+l2];
		System.arraycopy(image1, 0, image, 0, l1);
		System.arraycopy(image2, 0, image, l1, l2);
		Lexer lex= new Lexer(image, fLexOptions, ILexerLog.NULL, null);
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
		Token l= null;
		Token n;
		boolean space= false;
		for (; t != null; l=t, t= n) {
			n= (Token) t.getNext();
			if (!space && l != null && l.hasGap(t)) {
				buf.append(' ');
				space= true;
			}
			switch(t.getType()) {
			case IToken.tSTRING:
			case IToken.tLSTRING:
			case IToken.tCHAR:
			case IToken.tLCHAR:
				final char[] image= t.getCharImage();
				for (int i = 0; i < image.length; i++) {
					final char c = image[i];
					if (c == '"' || c == '\\') {
						buf.append('\\');
					}
					buf.append(c);
				}
				space= false;
				break;
			
			case CPreprocessor.tSPACE:
				if (!space && l != null && n != null) {
					buf.append(' ');
					space= true;
				}
				break;

			default:
				buf.append(t.getCharImage());
				space= false;
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
			case CPreprocessor.tSPACE:
				replacement.removeBehind(l);
				continue;
			}
			t.setOffset(offset, ++offset);
			l= t;
		}
		return offset;
	}
}
