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

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Utility to parse macro definitions and create the macro objects for the preprocessor.
 * @since 5.0
 */
class MacroDefinitionParser {
	static class InvalidMacroDefinitionException extends Exception {
		public char[] fName;
		public int fStartOffset;
		public int fEndOffset;
		public InvalidMacroDefinitionException(char[] name, int startOffset, int endOffset) {
			fName= name;
			fStartOffset= startOffset;
			fEndOffset= endOffset;
		}
	}

	private static final int ORIGIN_PREPROCESSOR_DIRECTIVE = OffsetLimitReachedException.ORIGIN_PREPROCESSOR_DIRECTIVE;

	private int fHasVarArgs;
	private int fExpansionOffset;
	private int fExpansionEndOffset;
	private Token fNameToken;
		
	/**
	 * In case the name was successfully parsed, the name token is returned.
	 * Otherwise the return value is undefined.
	 */
	public Token getNameToken() {
		return fNameToken;
	}

	/** 
	 * Parses an entire macro definition. Name must be the next token of the lexer.
	 */
	public ObjectStyleMacro parseMacroDefinition(final Lexer lexer, final ILexerLog log) 
			throws OffsetLimitReachedException, InvalidMacroDefinitionException {
    	final Token name = parseName(lexer);
    	final char[] source= lexer.getInput();
    	final char[] nameChars= name.getCharImage();
    	final char[][] paramList= parseParamList(lexer, name);
    	final TokenList replacement= new TokenList();
    	parseExpansion(lexer, log, nameChars, paramList, replacement);
    	if (paramList == null) {
    		return new ObjectStyleMacro(nameChars, fExpansionOffset, fExpansionEndOffset, replacement, source);
    	}
    	return new FunctionStyleMacro(nameChars, paramList, fHasVarArgs, fExpansionOffset, fExpansionEndOffset, replacement, source);
	}

	/** 
	 * Parses a macro definition without the replacement. Name must be the next token of the lexer.
	 */
	public PreprocessorMacro parseMacroDefinition(final Lexer lexer, final ILexerLog log, final char[] replacement) 
			throws InvalidMacroDefinitionException, OffsetLimitReachedException {
		final Token name = parseName(lexer);

		final char[] nameChars = name.getCharImage();
		final char[][] paramList= parseParamList(lexer, name);
		final Token replacementToken = lexer.currentToken();
		if (replacementToken.getType() != Lexer.tEND_OF_INPUT) {
			throw new InvalidMacroDefinitionException(nameChars, replacementToken.getOffset(), replacementToken.getEndOffset());
		}
		
		if (paramList == null) { 
			return new ObjectStyleMacro(nameChars, replacement);
		}
		return new FunctionStyleMacro(nameChars, paramList, fHasVarArgs, replacement);
	}

	/** 
	 * Parses a macro definition basically checking for var-args.
	 */
	public PreprocessorMacro parseMacroDefinition(final char[] name, char[][] paramList, final char[] replacement) {
		final int length = paramList.length;
		fHasVarArgs= 0;
		if (paramList != null && length > 0) {
			char[] lastParam= paramList[length-1];
			final int lpl = lastParam.length;
			switch(lpl) {
			case 0: case 1: case 2:
				break;
			case 3:
				if (CharArrayUtils.equals(lastParam, Keywords.cpELLIPSIS)) {
					fHasVarArgs= FunctionStyleMacro.VAARGS;
					char[][] copy= new char[length][];
					System.arraycopy(paramList, 0, copy, 0, length-1);
					copy[length-1]= Keywords.cVA_ARGS;
					paramList= copy;
				}
				break;
			default:
				if (CharArrayUtils.equals(lastParam, lpl-3, 3, Keywords.cpELLIPSIS)) {
					fHasVarArgs= FunctionStyleMacro.NAMED_VAARGS;
					char[][] copy= new char[length][];
					System.arraycopy(paramList, 0, copy, 0, length-1);
					copy[length-1]= CharArrayUtils.subarray(lastParam, 0, lpl-3);
					paramList= copy;
				}
				break;
			}
		}
		
		if (paramList == null) { 
			return new ObjectStyleMacro(name, replacement);
		}
		return new FunctionStyleMacro(name, paramList, fHasVarArgs, replacement);
	}

	private Token parseName(final Lexer lexer) throws OffsetLimitReachedException,	InvalidMacroDefinitionException {
		final Token name= lexer.nextToken();
    	final int tt= name.getType();
    	if (tt != IToken.tIDENTIFIER) {
    		if (tt == IToken.tCOMPLETION) {
    			throw new OffsetLimitReachedException(ORIGIN_PREPROCESSOR_DIRECTIVE, name);
    		}
    		throw new InvalidMacroDefinitionException(name.getCharImage(), name.getOffset(), name.getEndOffset());
    	}
    	fNameToken= name;
		return name;
	}
	
	private char[][] parseParamList(Lexer lex, final Token name) throws OffsetLimitReachedException, InvalidMacroDefinitionException {
	    final Token lparen= lex.nextToken();
		fHasVarArgs= FunctionStyleMacro.NO_VAARGS;
		if (lparen.getType() != IToken.tLPAREN || name.getEndOffset() != lparen.getOffset()) { 
			return null;
		}
		ArrayList paramList= new ArrayList();
		IToken next= null;
		do {
			final Token param= lex.nextToken();
			switch (param.getType()) {
			case IToken.tCOMPLETION:
				throw new OffsetLimitReachedException(ORIGIN_PREPROCESSOR_DIRECTIVE, param);

			case IToken.tIDENTIFIER:
				paramList.add(param.getCharImage());
				next= lex.nextToken();
				if (next.getType() == IToken.tELLIPSIS) {
					fHasVarArgs= FunctionStyleMacro.NAMED_VAARGS;
					next= lex.nextToken();
				}
				break;

			case IToken.tELLIPSIS:
				fHasVarArgs= FunctionStyleMacro.VAARGS;
				paramList.add(Keywords.cVA_ARGS);
				next= lex.nextToken();
				break;
				
			case IToken.tRPAREN:
				if (next == null) {
					next= param;
					break;
				}
				// no break;
			default:
				throw new InvalidMacroDefinitionException(name.getCharImage(), name.getOffset(), param.getEndOffset());
			}
		}
		while (fHasVarArgs==0 && next.getType() == IToken.tCOMMA);
		if (next.getType() != IToken.tRPAREN) {
			throw new InvalidMacroDefinitionException(name.getCharImage(), name.getOffset(), next.getEndOffset());
		}
		next= lex.nextToken(); // consume the closing parenthesis

		return (char[][]) paramList.toArray(new char[paramList.size()][]);
	}

	public void parseExpansion(final Lexer lexer, final ILexerLog log, final char[] name, final char[][] paramList,
			TokenList result) throws OffsetLimitReachedException {
		boolean needParam= false;
		boolean isFirst= true;
		Token needAnotherToken= null;

		Token candidate= lexer.currentToken();
		fExpansionOffset= fExpansionEndOffset= candidate.getOffset();		

		loop: while(true) {
			switch(candidate.getType()) {
			case IToken.tCOMPLETION:
				throw new OffsetLimitReachedException(ORIGIN_PREPROCESSOR_DIRECTIVE, candidate);
			case Lexer.tEND_OF_INPUT:
			case Lexer.tNEWLINE:
				break loop;
			case IToken.tIDENTIFIER:
				if (paramList != null) {
					// convert the parameters to special tokens
					final char[] image = candidate.getCharImage();
					int idx= CharArrayUtils.indexOf(image, paramList);
					if (idx >= 0) {
						candidate= new PlaceHolderToken(CPreprocessor.tMACRO_PARAMETER, idx, lexer.getSource(), candidate.getOffset(), candidate.getEndOffset(), paramList[idx]);
						needParam= false;
					}
					else {
						if (needParam) {
							log.handleProblem(IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, name, fExpansionOffset, candidate.getEndOffset());
						}
						else if (CharArrayUtils.equals(Keywords.cVA_ARGS, image)) {
							log.handleProblem(IProblem.PREPROCESSOR_INVALID_VA_ARGS, null, fExpansionOffset, candidate.getEndOffset());
						}
						needParam= false;
					}
				}
				needAnotherToken= null;
				break;
			case IToken.tPOUND:
				needParam= paramList != null;
				break;
			case IToken.tPOUNDPOUND:
				if (needParam || isFirst) {
					log.handleProblem(IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, name, fExpansionOffset, candidate.getEndOffset());
				}
				needAnotherToken= candidate;
				needParam= false;
				break;
			default:
				if (needParam) {
					log.handleProblem(IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, name, fExpansionOffset, candidate.getEndOffset());
					needParam= false;
				}
				needAnotherToken= null;
				break;
			}
			isFirst= false;
			fExpansionEndOffset= candidate.getEndOffset();
			result.append(candidate);
			candidate= lexer.nextToken();
		}
		if (needAnotherToken != null) {
			log.handleProblem(IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, name, needAnotherToken.getOffset(), needAnotherToken.getEndOffset());
		}
	}
}
