/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
import java.util.Arrays;
import java.util.BitSet;
import java.util.IdentityHashMap;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.util.CharArrayMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.scanner.ImageLocationInfo.MacroImageLocationInfo;
import org.eclipse.cdt.internal.core.parser.scanner.ImageLocationInfo.ParameterImageLocationInfo;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.cdt.internal.core.parser.scanner.MacroDefinitionParser.TokenParameterReference;

/**
 * Utility class to perform macro expansion.
 * @since 5.0
 */
public class MacroExpander {
	private static final class AbortMacroExpansionException extends Exception {}

	private static final int ORIGIN = OffsetLimitReachedException.ORIGIN_MACRO_EXPANSION;
	private static final TokenList EMPTY_TOKEN_LIST = new TokenList();	

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

		@Override
		public char[] getCharImage() {
			return CharArrayUtils.EMPTY;
		}
		
		@Override
		public String toString() {
			return "{" + (fIsStart ? '+' : '-') + fMacro.getName() + '}';  //$NON-NLS-1$
		}

		public void execute(IdentityHashMap<PreprocessorMacro, PreprocessorMacro> forbidden) {
			if (fIsStart) {
				forbidden.put(fMacro, fMacro);
			} else {
				forbidden.remove(fMacro);
			}
		}
	}
		
	/** 
	 * Combines a list of tokens with the preprocessor to form the input for macro expansion.
	 */
	private class TokenSource extends TokenList {
		private final ITokenSequence fLexer;

		public TokenSource(ITokenSequence lexer) {
			fLexer= lexer;
		}

		public Token fetchFirst() throws OffsetLimitReachedException {
			Token t= removeFirst();
			if (t == null && fLexer != null) {
				t= fLexer.currentToken();
				if (t.getType() != IToken.tEND_OF_INPUT) {
					fEndOffset= t.getEndOffset();
					fLexer.nextToken();
				}
			}
			return t;
		}

		public boolean findLParenthesis() throws OffsetLimitReachedException {
			Token t= first();
			while (t != null) {
				switch (t.getType()) {
				case CPreprocessor.tSPACE:
				case CPreprocessor.tNOSPACE:
				case Lexer.tNEWLINE:
				case CPreprocessor.tSCOPE_MARKER:
					break;
				case IToken.tLPAREN:
					return true;
				default:
					return false;
				}
				t= (Token) t.getNext();
			}

			if (fLexer != null) {
				t= fLexer.currentToken();
				while(t.getType() == Lexer.tNEWLINE) {
					t= fLexer.nextToken();
				}
				return t.getType() == IToken.tLPAREN;
    		}
			return false;
		}
	}

	private final ILexerLog fLog;
	private final MacroDefinitionParser fDefinitionParser;
	private final CharArrayMap<PreprocessorMacro> fDictionary;
	private final LocationMap fLocationMap;
	private final LexerOptions fLexOptions;
	private ArrayList<IASTName> fImplicitMacroExpansions= new ArrayList<IASTName>();
	private ArrayList<ImageLocationInfo> fImageLocationInfos= new ArrayList<ImageLocationInfo>();
	private boolean fCompletionMode;
	private int fStartOffset;
	private int fEndOffset;
	
	// for using the expander to track expansions
	private String fFixedCurrentFilename;
	private int fFixedLineNumber;
	private char[] fFixedInput;
	private ScannerContext fReportMacros;
	private boolean fReportUndefined;
	
	public MacroExpander(ILexerLog log, CharArrayMap<PreprocessorMacro> macroDictionary, LocationMap locationMap, LexerOptions lexOptions) {
		fDictionary= macroDictionary;
		fLocationMap= locationMap;
		fDefinitionParser= new MacroDefinitionParser();
		fLexOptions= lexOptions;
		fLog= log;
	}
	
	/** 
	 * Expects that the identifier has been consumed, stores the result in the list provided.
	 * @param scannerContext 
	 */
	public TokenList expand(ITokenSequence lexer, final int ppOptions,
			PreprocessorMacro macro, Token identifier, boolean completionMode,
			ScannerContext scannerContext) throws OffsetLimitReachedException {
		final boolean protectDefined= (ppOptions & CPreprocessor.PROTECT_DEFINED) != 0;
		if ((ppOptions & CPreprocessor.REPORT_SIGNIFICANT_MACROS) != 0) {
			fReportMacros= scannerContext;
			fReportUndefined= (ppOptions & CPreprocessor.IGNORE_UNDEFINED_SIGNIFICANT_MACROS) == 0;
		} else {
			fReportMacros= null;
		}
		
		fImplicitMacroExpansions.clear();
		fImageLocationInfos.clear();
		
		fStartOffset= identifier.getOffset();
		fEndOffset= identifier.getEndOffset();
		fCompletionMode= completionMode;
		
		IdentityHashMap<PreprocessorMacro, PreprocessorMacro> forbidden= new IdentityHashMap<PreprocessorMacro, PreprocessorMacro>();
		
		// setup input sequence
		TokenSource input= new TokenSource(lexer);
		TokenList firstExpansion= new TokenList();

		TokenList result;
		try {
			firstExpansion.append(new ExpansionBoundary(macro, true));
			expandOne(identifier, macro, forbidden, input, firstExpansion, null);
			firstExpansion.append(new ExpansionBoundary(macro, false));

			input.prepend(firstExpansion);

			result= expandAll(input, forbidden, protectDefined, null);
		} catch (CompletionInMacroExpansionException e) {
			// for content assist in macro expansions, we return the list of tokens of the 
			// parameter at the current cursor position and hope that they make sense if 
			// they are inserted at the position of the expansion.
			// For a better solution one would have to perform the expansion with artificial
			// parameters and then check where the completion token ends up in the expansion.
			result= e.getParameterTokens().cloneTokens();
		}
		postProcessTokens(result);
		fReportMacros= null;
		return result;
	}

	/**
	 * Method for tracking macro expansions.
	 * @since 5.0
	 */
	public void expand(String beforeExpansion, MacroExpansionTracker tracker, String filePath, int lineNumber, boolean protectDefinedConstructs) {
		fImplicitMacroExpansions.clear();
		fImageLocationInfos.clear();
		fFixedInput= beforeExpansion.toCharArray();
		fFixedCurrentFilename= filePath;
		fFixedLineNumber= lineNumber;
		fReportMacros= null;
		Lexer lexer= new Lexer(fFixedInput, fLexOptions, fLog, this);
		
		try {
			tracker.start(fFixedInput);
			Token identifier= lexer.nextToken();
			if (identifier.getType() != IToken.tIDENTIFIER) {
				tracker.fail();
				return;
			}
			PreprocessorMacro macro= fDictionary.get(identifier.getCharImage());
			if (macro == null) {
				tracker.fail();
				return;
			}
			lexer.nextToken();

			fStartOffset= identifier.getOffset();
			fEndOffset= identifier.getEndOffset();
			fCompletionMode= false;
			IdentityHashMap<PreprocessorMacro, PreprocessorMacro> forbidden= new IdentityHashMap<PreprocessorMacro, PreprocessorMacro>();

			// setup input sequence
			TokenSource input= new TokenSource(lexer);
			TokenList firstExpansion= new TokenList();

			firstExpansion.append(new ExpansionBoundary(macro, true));
			expandOne(identifier, macro, forbidden, input, firstExpansion, tracker);
			firstExpansion.append(new ExpansionBoundary(macro, false));
			input.prepend(firstExpansion);

			TokenList result= expandAll(input, forbidden, protectDefinedConstructs, tracker);
			tracker.finish(result, fEndOffset);
		} catch (OffsetLimitReachedException e) {
		}
	}
	
	/**
	 * Expects that the identifier of the macro expansion has been consumed. Expands the macro consuming
	 * tokens from the input (to read the parameters) and stores the resulting tokens together
	 * with boundary markers in the result token list.
	 * Returns the last token of the expansion.
	 * @throws AbortMacroExpansionException 
	 */
	private Token expandOne(Token lastConsumed, PreprocessorMacro macro, 
			IdentityHashMap<PreprocessorMacro, PreprocessorMacro> forbidden, TokenSource input, TokenList result,
			MacroExpansionTracker tracker) 
			throws OffsetLimitReachedException {
		if (fReportMacros != null)
			fReportMacros.significantMacro(macro);
		
		if (macro.isFunctionStyle()) {
			final int paramCount = macro.getParameterPlaceholderList().length;
			final TokenSource[] argInputs= new TokenSource[paramCount];
			final BitSet paramUsage= getParamUsage(macro);
			if (tracker != null) {
				tracker.startFunctionStyleMacro((Token) lastConsumed.clone());
			}
			try {
				lastConsumed= parseArguments(input, (FunctionStyleMacro) macro, forbidden, argInputs, tracker);
			} catch (AbortMacroExpansionException e) {
				// ignore this macro expansion
				for (TokenSource argInput : argInputs) {
					executeScopeMarkers(argInput, forbidden);
					if (tracker != null) {
						tracker.setExpandedMacroArgument(null);
					}
				}			
				if (tracker != null) {
					if (tracker.isRequestedStep()) {
						tracker.storeFunctionStyleMacroReplacement(macro, new TokenList(), result);
					} else if (tracker.isDone()) {
						tracker.appendFunctionStyleMacro(result);
					}
					tracker.endFunctionStyleMacro();
				}
				return null;
			}
			
			TokenList[] clonedArgs= new TokenList[paramCount];
			TokenList[] expandedArgs= new TokenList[paramCount];
			for (int i = 0; i < paramCount; i++) {
				final TokenSource argInput = argInputs[i];
				final boolean needCopy= paramUsage.get(2*i);
				final boolean needExpansion = paramUsage.get(2*i+1);
				clonedArgs[i]= needCopy ? argInput.cloneTokens() : EMPTY_TOKEN_LIST;
				expandedArgs[i]= needExpansion ? expandAll(argInput, forbidden, false, tracker) : EMPTY_TOKEN_LIST;
				if (!needExpansion) {
					executeScopeMarkers(argInput, forbidden);
				}

				if (tracker != null) {
					tracker.setExpandedMacroArgument(needExpansion ? expandedArgs[i] : null);
					// make sure that the trailing arguments do not get expanded.
					if (tracker.isDone()) {
						paramUsage.clear();
					}
				}
			}
			if (tracker == null) {
				replaceArgs(macro, clonedArgs, expandedArgs, result);
			} else {
				if (tracker.isRequestedStep()) {
					TokenList replacement= new TokenList();
					replaceArgs(macro, clonedArgs, expandedArgs, replacement);
					tracker.storeFunctionStyleMacroReplacement(macro, replacement, result);
				} else if (tracker.isDone()) {
					tracker.appendFunctionStyleMacro(result);
				} else {
					replaceArgs(macro, clonedArgs, expandedArgs, result);
				}
				tracker.endFunctionStyleMacro();
			}
		} else {
			if (tracker == null) {
				objStyleTokenPaste(macro, result);
			} else {
				if (tracker.isRequestedStep()) {
					TokenList replacement= new TokenList();
					objStyleTokenPaste(macro, replacement);
					tracker.storeObjectStyleMacroReplacement(macro, lastConsumed, replacement, result);
				} else {
					objStyleTokenPaste(macro, result);
				}
				tracker.endObjectStyleMacro();
			}
		}
		return lastConsumed;
	}

	private void executeScopeMarkers(TokenSource input, IdentityHashMap<PreprocessorMacro, PreprocessorMacro> forbidden) {
		Token t= input.removeFirst();
		while(t != null) {
			if (t.getType() == CPreprocessor.tSCOPE_MARKER) {
				((ExpansionBoundary) t).execute(forbidden);
			}
			t= input.removeFirst(); 
		}
	}

	private TokenList expandAll(TokenSource input, IdentityHashMap<PreprocessorMacro, PreprocessorMacro> forbidden,
			boolean protectDefinedConstructs, MacroExpansionTracker tracker) throws OffsetLimitReachedException {
		final TokenList result= new TokenList();
		boolean protect= false;
		Token l= null;
		Token t= input.removeFirst();
		while(t != null) {
			switch(t.getType()) {
			case CPreprocessor.tSCOPE_MARKER:
				((ExpansionBoundary) t).execute(forbidden);
				break;
			case IToken.tIDENTIFIER:
				final char[] image = t.getCharImage();
				PreprocessorMacro macro= fDictionary.get(image);
				if (protect || (tracker != null && tracker.isDone())) {
					result.append(t);
				} else if (protectDefinedConstructs && Arrays.equals(image, Keywords.cDEFINED)) {
					t.setType(CPreprocessor.tDEFINED);
					result.append(t);
					protect= true;
				} else if (macro == null || (macro.isFunctionStyle() && !input.findLParenthesis())) {
					// Tricky: Don't mark function-style macros if you don't find the left parenthesis
					if (fReportMacros != null) {
						if (macro != null) {
							fReportMacros.significantMacro(macro);
						} else if (fReportUndefined){
							fReportMacros.significantMacroUndefined(image);
						}
					}
					result.append(t);
				} else if (forbidden.containsKey(macro)) {
					t.setType(CPreprocessor.tEXPANDED_IDENTIFIER); // prevent any further expansion
					result.append(t);
				} else {
					if (fLocationMap != null) {
						ImageLocationInfo info= null;
						if (fLexOptions.fCreateImageLocations) {
							info = createImageLocationInfo(t);
						}
						fImplicitMacroExpansions.add(fLocationMap.encounterImplicitMacroExpansion(macro, info));
					}
					TokenList replacement= new TokenList();

					addSpacemarker(l, t, replacement); // start expansion
					replacement.append(new ExpansionBoundary(macro, true));
					Token last= expandOne(t, macro, forbidden, input, replacement, tracker); 
					replacement.append(new ExpansionBoundary(macro, false));
					addSpacemarker(last, input.first(), replacement); // end expansion

					input.prepend(replacement);
				}
				break;
			case IToken.tLPAREN:
			case CPreprocessor.tNOSPACE:
			case CPreprocessor.tSPACE:				
				result.append(t);
				break;
			default:
				protect= false;
				result.append(t); 
				break;
			}
			l= t;
			t= input.removeFirst();
		}
		return result;
	}

	private ImageLocationInfo createImageLocationInfo(Token t) {
		if (fLocationMap != null) {
			final Object s= t.fSource;
			if (s instanceof ObjectStyleMacro) {
				return new MacroImageLocationInfo((ObjectStyleMacro) s, t.getOffset(), t.getEndOffset());
			} else if (s instanceof CPreprocessor) {
				int sequenceNumber= fLocationMap.getSequenceNumberForOffset(t.getOffset());
				int sequenceEndNumber= fLocationMap.getSequenceNumberForOffset(t.getEndOffset());
				return new ParameterImageLocationInfo(sequenceNumber, sequenceEndNumber);
			}
		}
		return null;
	}

	private static boolean isNeighborInSource(Token l, Token t) {
		return l != null && t != null && l.fSource != null && l.fSource == t.fSource;
	}

	static boolean hasImplicitSpace(Token l, Token t) {
		return isNeighborInSource(l, t) && l.getEndOffset() != t.getOffset();
	}

	static void addSpacemarker(Token l, Token t, TokenList target) {
		if (isNeighborInSource(l, t)) {
			final int from= l.getEndOffset();
			final int to= t.getOffset();
			if (from != to) {
				target.append(new Token(CPreprocessor.tSPACE, l.fSource, from, to));
			}
		}
		target.append(new Token(CPreprocessor.tNOSPACE, null, 0, 0));
	}
	
	/**
	 * Expects that the identifier has been consumed.
	 */
	private Token parseArguments(TokenSource input, FunctionStyleMacro macro, IdentityHashMap<PreprocessorMacro, PreprocessorMacro> forbidden, 
			TokenSource[] result, MacroExpansionTracker tracker) throws OffsetLimitReachedException, AbortMacroExpansionException {
		final int argCount= macro.getParameterPlaceholderList().length;
		final boolean hasVarargs= macro.hasVarArgs() != FunctionStyleMacro.NO_VAARGS;
		final int requiredArgs= hasVarargs ? argCount-1 : argCount;
		int idx= 0;
		int nesting= -1;
		for (int i = 0; i < result.length; i++) {
			result[i]= new TokenSource(null);
		}
		
		boolean missingRParenthesis= false;
		boolean tooManyArgs= false;
		
		boolean isFirstOfArg= true;
		Token lastToken= null;
		TokenList spaceMarkers= new TokenList();
        loop: while (true) {
    		Token t= input.fetchFirst();
    		if (t == null) {
    			missingRParenthesis= true;
    			break loop;
    		}
    		if (tracker != null) {
    			switch(t.getType()) {
    			case IToken.tEND_OF_INPUT:        	
    			case IToken.tCOMPLETION:
    			case CPreprocessor.tSCOPE_MARKER:
    			case Lexer.tNEWLINE:
    				break;
    			default:
    				tracker.addFunctionStyleMacroExpansionToken((Token) t.clone());
    				break;
    			}
    		}
			lastToken= t;
        	switch(t.getType()) {
        	case IToken.tEND_OF_INPUT:
        		assert nesting >= 0;
        		if (fCompletionMode) {
            		if (idx < result.length) {
            			throw new CompletionInMacroExpansionException(ORIGIN, t, result[idx]);
            		}
        			throw new OffsetLimitReachedException(ORIGIN, null);
        		}
        		missingRParenthesis= true;
        		break loop;
        	case IToken.tCOMPLETION:
        		if (idx < result.length) {
        			result[idx].append(t);
        			throw new CompletionInMacroExpansionException(ORIGIN, t, result[idx]);
        		}
        		throw new OffsetLimitReachedException(ORIGIN, t);
        		
        	case Lexer.tNEWLINE:
        		continue loop;

        	case IToken.tLPAREN:
        		// the first one sets nesting to zero.
        		if (++nesting == 0) {
        			continue;
        		}
        		break;
        		
        	case IToken.tRPAREN:
        		assert nesting >= 0;
        		if (--nesting < 0) {
        			break loop;
        		}
        		break;
        		
        	case IToken.tCOMMA:
        		assert nesting >= 0;
        		if (nesting == 0) {
        			if (idx < argCount-1) { // next argument
        				isFirstOfArg= true;
        				spaceMarkers.clear();
        				idx++;
            			continue loop;
        			} else if (!hasVarargs) {
        				tooManyArgs= true;
        				break loop;
        			}
        		}
        		break;
        		
        	case CPreprocessor.tSCOPE_MARKER:
        		if (argCount == 0) {
        			((ExpansionBoundary) t).execute(forbidden);
        		} else {
        			result[idx].append(t);
        		}
        		continue loop;
        		
        	case CPreprocessor.tSPACE:
        	case CPreprocessor.tNOSPACE:
        		if (!isFirstOfArg) {
        			spaceMarkers.append(t);
        		}
        		continue loop;
        		
        	default:
        		assert nesting >= 0;
        	}
    		if (argCount == 0) {
    			tooManyArgs= true;
    			break loop;
    		}
    		result[idx].appendAll(spaceMarkers);
    		result[idx].append(t);
    		isFirstOfArg= false;
        }


		if (missingRParenthesis) {
			handleProblem(IProblem.PREPROCESSOR_MISSING_RPAREN_PARMLIST, macro.getNameCharArray());
			throw new AbortMacroExpansionException();
		}
		
		if (tooManyArgs) {
			handleProblem(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macro.getNameCharArray());
		} else if (idx+1 < requiredArgs) {
			handleProblem(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macro.getNameCharArray());
		}
        return lastToken;
	}
	
	private void handleProblem(int problemID, char[] arg) {
		fLog.handleProblem(problemID, arg, fStartOffset, fEndOffset);
	}

	private void replaceArgs(PreprocessorMacro macro, TokenList[] args, TokenList[] expandedArgs, TokenList result) {
		TokenList replacement= clone(macro.getTokens(fDefinitionParser, fLexOptions, this));
		
		Token l= null;
		Token n;       
		Token pasteArg1= null;  
		for (Token t= replacement.first(); t != null; l=t, t=n) {
			n= (Token) t.getNext();

			switch(t.getType()) {
			case CPreprocessor.tMACRO_PARAMETER:
				int idx= ((TokenParameterReference) t).getIndex();
				if (idx < args.length) { // be defensive
					addSpacemarker(l, t, result); // start argument replacement
					if (isKind(n, IToken.tPOUNDPOUND)) {
						TokenList arg= clone(args[idx]);
						pasteArg1= arg.last();
						if (pasteArg1 != null) {
							result.appendAllButLast(arg);
							addSpacemarker(result.last(), pasteArg1, result); // start token paste
						}
					} else {
						TokenList arg= clone(expandedArgs[idx]);
						result.appendAll(arg);
						addSpacemarker(t, n, result); // end argument replacement
					}
				}
				break;
				
			case IToken.tPOUND:
				addSpacemarker(l, t, result);	// start stringify
				StringBuilder buf= new StringBuilder();
				buf.append('"');
				if (isKind(n, CPreprocessor.tMACRO_PARAMETER)) {
					idx= ((TokenParameterReference) n).getIndex();
					if (idx < args.length) { // be defensive
						stringify(args[idx], buf);
					}
					t= n;
					n= (Token) n.getNext();
				}
				buf.append('"');
				final int length= buf.length(); 
				final char[] image= new char[length];
				buf.getChars(0, length, image, 0);
				
				Token generated= new TokenWithImage(IToken.tSTRING, null, 0, 0, image);
				if (isKind(n, IToken.tPOUNDPOUND)) {  // start token paste, same as start stringify
					pasteArg1= generated;	
				} else {
					result.append(generated);
					addSpacemarker(t, n, result);  // end stringify
				}
				break;
				
			case IToken.tPOUNDPOUND:
				Token pasteArg2= null;
				TokenList rest= null;
				if (n != null) {
					Token spaceDef0= n;
					Token spaceDef1= (Token) n.getNext();
					if (n.getType() == CPreprocessor.tMACRO_PARAMETER) {
						TokenList arg;
						idx= ((TokenParameterReference) n).getIndex();
						if (idx < args.length) { // be defensive
							arg= clone(args[idx]);
							pasteArg2= arg.first();
							if (pasteArg2 != null && arg.first() != arg.last()) {
								spaceDef0= pasteArg2;
								rest= arg;
								rest.removeFirst();
								spaceDef1= rest.first();
							}
						}
					} else {
						idx= -1;
						pasteArg2= n;
					}
					
					t= n;
					n= (Token) n.getNext();
					final boolean pasteNext= isKind(n, IToken.tPOUNDPOUND);

					generated= tokenpaste(pasteArg1, pasteArg2, macro);
					if (generated == null) {
						// Cannot perform token paste.
						// Use the two tokens instead, see bug 354553.
						generated= pasteArg1;
						if (rest == null)
							rest= new TokenList();
						
						rest.prepend(pasteArg2);
						spaceDef0= generated;
						spaceDef1= pasteArg2;
					}
					pasteArg1= null;

					if (generated != null) {
						if (pasteNext && rest == null) {
							pasteArg1= generated;	// no need to mark spaces, done ahead
						} else {
							result.append(generated);
							addSpacemarker(spaceDef0, spaceDef1, result); // end token paste
						}
					}
					if (rest != null) {
						if (pasteNext) {
							pasteArg1= rest.last();
							if (pasteArg1 != null) {
								result.appendAllButLast(rest);
								addSpacemarker(result.last(), pasteArg1, result); // start token paste
							}
						} else {
							result.appendAll(rest);
							if (idx >= 0) {
								addSpacemarker(t, n, result);	// end argument replacement
							}
						}
					}
				}
				break;
				
			case IToken.tCOMMA:
				if (isKind(n, IToken.tPOUNDPOUND)) {
					final Token nn= (Token) n.getNext();
					if (isKind(nn, CPreprocessor.tMACRO_PARAMETER)) {
						idx= ((TokenParameterReference) nn).getIndex();
						
						// check for gcc-extension preventing the paste operation
						if (idx == args.length-1 && macro.hasVarArgs() != FunctionStyleMacro.NO_VAARGS && 
								!isKind(nn.getNext(), IToken.tPOUNDPOUND)) {
							final Token nnn= (Token) nn.getNext();
							TokenList arg= clone(expandedArgs[idx]);
							if (arg.isEmpty()) {
								addSpacemarker(l, t, result);
								addSpacemarker(nn, nnn, result);
							} else {
								result.append(t);
								addSpacemarker(t, n, result);
								result.appendAll(arg);
								addSpacemarker(nn, nnn, result);
							}
							t= nn;
							n= nnn;
							break;
						}
					}
					
					addSpacemarker(l, t, result);
					pasteArg1= t;
				} else {
					result.append(t);
				}
				break;
				
			default:
				if (isKind(n, IToken.tPOUNDPOUND)) {
					addSpacemarker(l, t, result);	// start token paste
					pasteArg1= t;
				} else {
					result.append(t);
				}
				break;
			}
		}
	}
	
	private boolean isKind(final IToken t, final int kind) {
		return t!=null && t.getType() == kind;
	}

	private BitSet getParamUsage(PreprocessorMacro macro) {
		final BitSet result= new BitSet();
		final TokenList replacement= macro.getTokens(fDefinitionParser, fLexOptions, this);
		
		Token l= null;
		Token n;       
		for (Token t= replacement.first(); t != null; l=t, t=n) {
			n= (Token) t.getNext();
			switch(t.getType()) {
			case CPreprocessor.tMACRO_PARAMETER:
				int idx= 2*((TokenParameterReference) t).getIndex();
				if (!isKind(n, IToken.tPOUNDPOUND)) {
					idx++;
				}
				result.set(idx);
				break;
				
			case IToken.tPOUND:
				if (isKind(n, CPreprocessor.tMACRO_PARAMETER)) {
					idx= ((TokenParameterReference) n).getIndex();
					result.set(2*idx);
					t= n; n= (Token) n.getNext();
				}
				break;
				
			case IToken.tPOUNDPOUND:
				if (isKind(n, CPreprocessor.tMACRO_PARAMETER)) {
					idx= ((TokenParameterReference) n).getIndex();
					// gcc-extension
					if (isKind(l, IToken.tCOMMA) && macro.hasVarArgs() != FunctionStyleMacro.NO_VAARGS &&
							idx == macro.getParameterPlaceholderList().length-1 && !isKind(n.getNext(), IToken.tPOUNDPOUND)) {
						result.set(2*idx+1);
					} else {
						result.set(2*idx);
					}
					t= n; n= (Token) n.getNext();
				}
				break;
			}				
		}
		return result;
	}

	private void objStyleTokenPaste(PreprocessorMacro macro, TokenList result) {
		TokenList replacement= clone(macro.getTokens(fDefinitionParser, fLexOptions, this));
		
		Token l= null;
		Token n;       
		Token pasteArg1= null;  
		for (Token t= replacement.first(); t != null; l=t, t=n) {
			n= (Token) t.getNext();

			switch(t.getType()) {
			case IToken.tPOUNDPOUND:
				if (pasteArg1 != null) {
					Token pasteArg2= null;
					if (n != null) {
						pasteArg2= n;
						n= (Token) n.getNext();
					}
					
					t= tokenpaste(pasteArg1, pasteArg2, macro);
					if (t != null) {
						if (isKind(n, IToken.tPOUNDPOUND)) {
							pasteArg1= t;
						} else {
							result.append(t);
							addSpacemarker(pasteArg2, n, result); // end token paste
						}
					}
				}
				break;
				
			default:
				if (isKind(n, IToken.tPOUNDPOUND)) {
					addSpacemarker(l, t, result); // start token paste
					pasteArg1= t;
				} else {
					result.append(t);
				}
				break;
			}
		}
	}

	private TokenList clone(TokenList tl) {
		TokenList result= new TokenList();
		for (Token t= tl.first(); t != null; t= (Token) t.getNext()) {
			result.append((Token) t.clone());
		}
		return result;
	}

	private Token tokenpaste(Token arg1, Token arg2, PreprocessorMacro macro) {
		if (arg1 == null) {
			return arg2;
		}
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
			if (t1.getType() != IToken.tEND_OF_INPUT && t2.getType() == IToken.tEND_OF_INPUT) {
				t1.setOffset(arg1.getOffset(), arg2.getEndOffset());
				return t1;
			}
		} catch (OffsetLimitReachedException e) {
		}
		handleProblem(IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, macro.getNameCharArray());
		return null;
	}

	private void stringify(TokenList tokenList, StringBuilder buf) {
		Token t= tokenList.first();
		if (t == null) {
			return;
		}
		Token l= null;
		Token n;
		boolean space= false;
		for (; t != null; l=t, t=n) {
			n= (Token) t.getNext();
			if (!space && hasImplicitSpace(l, t)) {
				buf.append(' ');
				space= true;
			}
			switch(t.getType()) {
			case IToken.tSTRING:
			case IToken.tLSTRING:
	        case IToken.tUTF16STRING:
	        case IToken.tUTF32STRING:
			case IToken.tCHAR:
			case IToken.tLCHAR:
	    	case IToken.tUTF16CHAR:
	    	case IToken.tUTF32CHAR:
				final char[] image= t.getCharImage();
				for (final char c : image) {
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

			case CPreprocessor.tNOSPACE:
				break;
				
			default:
				buf.append(t.getCharImage());
				space= false;
				break;
			}
		}
	}
	
	public IASTName[] clearImplicitExpansions() {
		IASTName[] result= fImplicitMacroExpansions.toArray(new IASTName[fImplicitMacroExpansions.size()]);
		fImplicitMacroExpansions.clear();
		return result;
	}

	public ImageLocationInfo[] clearImageLocationInfos() {
		ImageLocationInfo[] result= fImageLocationInfos.toArray(new ImageLocationInfo[fImageLocationInfos.size()]);
		fImageLocationInfos.clear();
		return result;
	}

	private void postProcessTokens(TokenList replacement) {
		final boolean createImageLocations= fLexOptions.fCreateImageLocations;
		int offset= 0;
		Token l= null;
		for (Token t= replacement.first(); t!=null; t= (Token) t.getNext()) {
			switch(t.getType()) {
			case CPreprocessor.tEXPANDED_IDENTIFIER:
				t.setType(IToken.tIDENTIFIER);
				if (createImageLocations) {
					ImageLocationInfo info= createImageLocationInfo(t);
					if (info != null) {
						info.fTokenOffsetInExpansion= offset;
						fImageLocationInfos.add(info);
					}
				}
				break;
			case IToken.tIDENTIFIER:
				if (createImageLocations) {
					ImageLocationInfo info= createImageLocationInfo(t);
					if (info != null) {
						info.fTokenOffsetInExpansion= offset;
						fImageLocationInfos.add(info);
					}
				}
				break;

			case CPreprocessor.tSCOPE_MARKER:
			case CPreprocessor.tSPACE:
			case CPreprocessor.tNOSPACE:
				replacement.removeBehind(l);
				continue;

			case IToken.tCOMPLETION:
				// we need to preserve the length of the completion token.
				t.setOffset(offset, offset+t.getLength());
				t.setNext(null);
				return;
			}
			t.setOffset(offset, ++offset);
			l= t;
		}
	}
	
	int getCurrentLineNumber() {
		if (fFixedInput != null) {
			return fFixedLineNumber + countNewlines(fFixedInput);
		}
		if (fLocationMap != null) {
			return fLocationMap.getCurrentLineNumber(fEndOffset);
		}
		return 0;
	}

	private int countNewlines(char[] input) {
		int nl= 0;
		for (int i = 0; i < input.length && i<fEndOffset; i++) {
			if (input[i] == '\n') {
				nl++;
			}
		}
		return nl;
	}

	String getCurrentFilename() {
		if (fFixedCurrentFilename != null) {
			return fFixedCurrentFilename;
		}
		if (fLocationMap != null) {
			return fLocationMap.getCurrentFilePath();
		}
		return ""; //$NON-NLS-1$
	}
}
