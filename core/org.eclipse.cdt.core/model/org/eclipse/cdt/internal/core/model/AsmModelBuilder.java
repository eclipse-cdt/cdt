/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.HashMap;

import org.eclipse.cdt.core.model.AssemblyLanguage;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.internal.formatter.scanner.Scanner;
import org.eclipse.cdt.internal.formatter.scanner.Token;

/**
 * A simple model builder for assembly translation units.
 * Recognizes preprocessor directives (#include and #define)
 * and labels.
 * 
 * @see AssemblyLanguage
 * @since 5.0
 */
public class AsmModelBuilder implements IContributedModelBuilder {

	private static final class Counter {
		int fCount;
	}

	private final static class Tokenizer extends Scanner {
		public Tokenizer(char[] source) {
			setSource(source);
		}

		public Token nextToken() {
			Token t= super.nextToken();
			while (t != null && (t.isWhiteSpace())) {
				t= super.nextToken();
			}
			return t;
		}
	}

	private TranslationUnit fTranslationUnit;
	private char fLineSeparatorChar;
	private HashMap fGlobals;
	private HashMap fLabels;
	private int fLastLabelEndOffset;
	private AsmLabel fLastGlobalLabel;
	private SourceManipulation fLastLabel;

	/**
	 * Creates a model builder for the given assembly translation unit.
	 * 
	 * @param tu  the translation unit
	 */
	public AsmModelBuilder(ITranslationUnit tu) {
		fTranslationUnit= (TranslationUnit)tu;
	}

	/**
	 * Configure the line separator character (in addition to normal newlines).
	 * 
	 * @param lineSeparatorChar
	 */
	public void setLineSeparatorCharacter(char lineSeparatorChar) {
		fLineSeparatorChar= lineSeparatorChar;
	}

	/*
	 * @see org.eclipse.cdt.core.model.IContributedModelBuilder#parse(boolean)
	 */
	public void parse(boolean quickParseMode) throws Exception {
		CodeReader reader;
		reader = fTranslationUnit.getCodeReader();
		if (reader == null) {
			return;
		}
		buildModel(reader.buffer);
		// not sure whether this is necessary or not
		fTranslationUnit.setIsStructureKnown(true);
		fGlobals= null;
		fLabels= null;
	}

	/**
	 * Build the model.
	 * 
	 * @param source
	 * @throws CModelException
	 */
	private void buildModel(final char[] source) throws CModelException {
		fGlobals= new HashMap();
		fLabels= new HashMap();
		fLastLabel= null;
		fLastGlobalLabel= null;
		fLastLabelEndOffset= 0;

		// TLETODO use new Lexer?
		Scanner scanner= new Scanner();
		scanner.setSplitPreprocessor(true);
		scanner.setSource(source);
		
		// if true the next token is the first on a (logical) line
		boolean firstTokenOnLine= true;
		// next token can be an instruction or a label
		boolean expectInstruction=true;
		// inside instruction
		boolean inInstruction= false;

		Token token= scanner.nextToken();
		while (token != null) {
			int type= adaptTokenType(token);
			switch (type) {
			case Token.tPREPROCESSOR_INCLUDE:
				parseInclude(fTranslationUnit, token);
				break;
			case Token.tPREPROCESSOR_DEFINE:
				parseDefine(fTranslationUnit, token);
				break;
			case Token.tFLOATINGPT:
				if (token.getText().startsWith(".")) { //$NON-NLS-1$
					// move scanner behind '.'
					scanner.setCurrentPosition(scanner.getCurrentTokenStartPosition() + 1);
					// fallthrough
				} else {
					break;
				}
			case Token.tDOT:
				// assembly directive?
				firstTokenOnLine= false;
				if (expectInstruction) {
					expectInstruction= false;
					int pos= scanner.getCurrentPosition();
					token= scanner.nextToken();
					if (token != null && adaptTokenType(token) == Token.tIDENTIFIER) {
						String text= token.getText();
						if (isGlobalDirective(text)) {
							firstTokenOnLine= parseGlobalDirective(token, scanner);
						} else if (isDataDirective(text)) {
							inInstruction= true;
							fLastLabelEndOffset= scanner.getCurrentTokenEndPosition();
						}
					} else {
						scanner.setCurrentPosition(pos);
					}
				}
				break;
			case Token.tIDENTIFIER:
				// identifier may be a label or part of an instruction
				if (firstTokenOnLine) {
					int pos= scanner.getCurrentPosition();
					int ch= scanner.getNextChar();
					if (ch == ':') {
						parseLabel(fTranslationUnit, token);
						fLastLabelEndOffset= scanner.getCurrentPosition();
						expectInstruction= true;
					} else {
						fLastLabelEndOffset= scanner.getCurrentTokenEndPosition();
						scanner.setCurrentPosition(pos);
						expectInstruction= false;
						inInstruction= true;
					}
					firstTokenOnLine= false;
				} else if (expectInstruction){
					expectInstruction= false;
					inInstruction= true;
					fLastLabelEndOffset= scanner.getCurrentTokenEndPosition();
				} else if (inInstruction) {
					fLastLabelEndOffset= scanner.getCurrentTokenEndPosition();
				}
				break;
			case Token.tPREPROCESSOR:
			case Token.tWHITESPACE:
				if (!firstTokenOnLine) {
					int nlIndex= token.getText().indexOf('\n');
					firstTokenOnLine= nlIndex >= 0;
					if (firstTokenOnLine && inInstruction) {
						fLastLabelEndOffset= scanner.getCurrentTokenStartPosition() + nlIndex + 1;
					}
				}
				break;
			default:
				expectInstruction= false;
				firstTokenOnLine= false;
				if (fLineSeparatorChar != 0) {
					if (token.getLength() == 1 && token.getText().charAt(0) == fLineSeparatorChar) {
						firstTokenOnLine= true;
					}
				}
			}
			if (firstTokenOnLine) {
				expectInstruction= true;
				inInstruction= false;
			}
			token= scanner.nextToken();
		}
		if (!firstTokenOnLine && inInstruction) {
			fLastLabelEndOffset= scanner.getCurrentTokenEndPosition();
		}
		if (fLastLabel != null) {
			fixupLastLabel();
		}
		if (fLastGlobalLabel != null) {
			fixupLastGlobalLabel();
		}
	}
	
	/**
	 * Adapt non-identifier tokens to identifier type if they look like valid asm words.
	 * 
	 * @param token
	 * @return the adapted token type
	 */
	private int adaptTokenType(Token token) {
		int type= token.getType();
		if (type != Token.tIDENTIFIER) {
			if (Character.isUnicodeIdentifierStart(token.getText().charAt(0))) {
				type= Token.tIDENTIFIER;
			}
		}
		return type;
	}

	private boolean isDataDirective(String directive) {
		return "byte".equals(directive) || "long".equals(directive) || "word".equals(directive) || "ascii".equals(directive); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
	}

	private boolean parseGlobalDirective(Token token, Scanner scanner) {
		boolean eol= false;
		do {
			Token t= scanner.nextToken();
			if (t == null) {
				break;
			}
			switch (adaptTokenType(t)) {
			case Token.tIDENTIFIER:
				registerGlobalLabel(t.getText());
				break;
			case Token.tWHITESPACE:
				if (token.getText().indexOf('\n') >= 0) {
					eol= true;
				}
				break;
			default:
				if (fLineSeparatorChar != 0) {
					if (token.getLength() == 1 && token.getText().charAt(0) == fLineSeparatorChar) {
						eol= true;
					}
				}
			}
		} while (!eol);
		return eol;
	}

	private boolean isGlobalDirective(String name) {
		return "globl".equals(name) || "global".equals(name); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void registerGlobalLabel(String labelName) {
		Counter counter= (Counter) fGlobals.get(labelName);
		if (counter == null) {
			fGlobals.put(labelName, counter= new Counter());
		}
		counter.fCount++;
	}

	private int getGlobalLabelIndex(String labelName) {
		Counter counter= (Counter) fGlobals.get(labelName);
		if (counter == null) {
			return 0;
		}
		return counter.fCount;
	}

	private int registerLabel(String labelName) {
		Counter counter= (Counter) fLabels.get(labelName);
		if (counter == null) {
			fLabels.put(labelName, counter= new Counter());
		}
		return counter.fCount++;
	}

	private void parseLabel(CElement parent, Token token) throws CModelException {
		String labelName=  token.getText();
		int index= getGlobalLabelIndex(labelName);
		boolean global= index > 0;
		if (!global) {
			index= registerLabel(labelName);
		}
		AsmLabel label= new AsmLabel(parent, labelName, global, index);
		SourceManipulationInfo labelInfo= label.getSourceManipulationInfo();
		labelInfo.setIdPos(token.getOffset(), token.getLength());
		labelInfo.setPos(token.getOffset(), token.getLength());
		if (fLastLabel != null) {
			fixupLastLabel();
		}
		if (global) {
			// new global label
			if (fLastGlobalLabel != null) {
				fixupLastGlobalLabel();
			}
			fLastGlobalLabel= label;
		} else {
			// add under global label if available
			if (fLastGlobalLabel != null) {
				parent= fLastGlobalLabel;
			}
		}
		fLastLabel= label;
		parent.addChild(label);
	}

	/**
	 * Set the body position of the last global label.
	 * 
	 * @throws CModelException 
	 */
	private void fixupLastGlobalLabel() throws CModelException {
		if (fLastLabel != null && fLastLabel != fLastGlobalLabel) {
			SourceManipulationInfo globalLabelInfo= fLastGlobalLabel.getSourceManipulationInfo();
			SourceManipulationInfo labelInfo= fLastLabel.getSourceManipulationInfo();
			globalLabelInfo.setPos(globalLabelInfo.getStartPos(), labelInfo.getStartPos() + labelInfo.getLength() - globalLabelInfo.getStartPos());
			// TLETODO set line info
		}
	}

	/**
	 * Set the body position of the last label.
	 * 
	 * @throws CModelException 
	 */
	private void fixupLastLabel() throws CModelException {
		if (fLastLabelEndOffset > 0) {
			SourceManipulationInfo labelInfo= fLastLabel.getSourceManipulationInfo();
			labelInfo.setPos(labelInfo.getStartPos(), fLastLabelEndOffset - labelInfo.getStartPos());
			// TLETODO set line info
			fLastLabelEndOffset= 0;
		}
	}

	private void parseDefine(CElement parent, Token token) throws CModelException {
		final String tokenText= token.getText();
		Scanner defineScanner= new Tokenizer(tokenText.toCharArray());
		defineScanner.setCurrentPosition(tokenText.indexOf('#') + 1);
		int nameStart= 0;
		int nameEnd= 0;
		String name= null;
		Token t= defineScanner.nextToken();
		if (adaptTokenType(t) == Token.tIDENTIFIER && t.getText().equals("define")) { //$NON-NLS-1$
			t= defineScanner.nextToken();
			if (adaptTokenType(t) == Token.tIDENTIFIER) {
				nameStart= defineScanner.getCurrentTokenStartPosition();
				nameEnd= defineScanner.getCurrentTokenEndPosition() + 1;
				name= t.getText();
			}
		}
		if (name == null) {
			return;
		}
		Macro macro= new Macro(parent, name);
		SourceManipulationInfo macroInfo= macro.getSourceManipulationInfo();
		macroInfo.setIdPos(token.getOffset() + nameStart, nameEnd - nameStart);
		macroInfo.setPos(token.getOffset(), token.getLength());
		parent.addChild(macro);
	}

	private void parseInclude(CElement parent, Token token) throws CModelException {
		final String tokenText= token.getText();
		Scanner includeScanner= new Tokenizer(tokenText.toCharArray());
		includeScanner.setCurrentPosition(tokenText.indexOf('#') + 1);
		int nameStart= 0;
		int nameEnd= includeScanner.eofPosition + 1;
		String name= null;
		boolean isStandard= false;
		Token t= includeScanner.nextToken();
		if (adaptTokenType(t) == Token.tIDENTIFIER) {
			t= includeScanner.nextToken();
			if (t.type == Token.tLT) {
				nameStart= includeScanner.getCurrentTokenStartPosition();
				do {
					t= includeScanner.nextToken();
				} while (t != null && t.type != Token.tGT);
				nameEnd= includeScanner.getCurrentTokenEndPosition() + 1;
				name= tokenText.substring(nameStart + 1, nameEnd - 1);
			} else if (t.type == Token.tSTRING) {
				nameStart= includeScanner.getCurrentTokenStartPosition();
				nameEnd= includeScanner.getCurrentTokenEndPosition() + 1;
				name= t.getText().substring(1, t.getLength() - 1);
				isStandard= true;
			} else if (adaptTokenType(t) == Token.tIDENTIFIER) {
				nameStart= includeScanner.getCurrentTokenStartPosition();
				nameEnd= includeScanner.getCurrentTokenEndPosition() + 1;
				name= t.getText();
			}
		}
		if (name == null) {
			return;
		}
		Include include= new Include(parent, name, isStandard);
		SourceManipulationInfo includeInfo= include.getSourceManipulationInfo();
		includeInfo.setIdPos(token.getOffset() + nameStart, nameEnd - nameStart);
		includeInfo.setPos(token.getOffset(), token.getLength());
		parent.addChild(include);
	}

}
