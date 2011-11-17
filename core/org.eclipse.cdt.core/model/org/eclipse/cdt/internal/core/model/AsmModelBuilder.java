/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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
import java.util.Map;

import org.eclipse.cdt.core.model.AssemblyLanguage;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.internal.core.parser.scanner.ILexerLog;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.cdt.internal.core.parser.scanner.Token;

/**
 * A simple model builder for assembly translation units.
 * Recognizes preprocessor directives (#include and #define)
 * and labels.
 *
 * @see AssemblyLanguage
 * @since 5.0
 */
public class AsmModelBuilder implements IContributedModelBuilder {

	private static final class AsmDirective {
		private static final AsmDirective GLOBAL= new AsmDirective();
		private static final AsmDirective DATA= new AsmDirective();
	}

	private static final class LexerLog implements ILexerLog {
		@Override
		public void handleComment(boolean isBlockComment, int offset, int endOffset) {
		}
		@Override
		public void handleProblem(int problemID, char[] info, int offset, int endOffset) {
		}
	}

	private static final class Counter {
		int fCount;
	}

	private static final Map<String, AsmDirective> fgDirectives;

	static {
		fgDirectives= new HashMap<String, AsmDirective>();
		fgDirectives.put("globl", AsmDirective.GLOBAL); //$NON-NLS-1$
		fgDirectives.put("global", AsmDirective.GLOBAL); //$NON-NLS-1$
		fgDirectives.put("ascii", AsmDirective.DATA); //$NON-NLS-1$
		fgDirectives.put("asciz", AsmDirective.DATA); //$NON-NLS-1$
		fgDirectives.put("byte", AsmDirective.DATA); //$NON-NLS-1$
		fgDirectives.put("long", AsmDirective.DATA); //$NON-NLS-1$
		fgDirectives.put("word", AsmDirective.DATA); //$NON-NLS-1$
	}

	private TranslationUnit fTranslationUnit;
	private char fLineSeparatorChar;
	private Map<String, Counter> fGlobals;
	private Map<String, Counter> fLabels;

	private int fLastLabelEndOffset;
	private AsmLabel fLastGlobalLabel;
	private SourceManipulation fLastLabel;
	private Lexer fLexer;

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
	@Override
	public void parse(boolean quickParseMode) throws Exception {
		char[] source = fTranslationUnit.getContents();
		if (source == null) {
			return;
		}
		buildModel(source);
		// not sure whether this is necessary or not
		fTranslationUnit.setIsStructureKnown(true);
		// cleanup
		fGlobals= null;
		fLabels= null;
		fLexer= null;
	}

	/**
	 * Build the model.
	 *
	 * @param source
	 * @throws CModelException
	 */
	private void buildModel(final char[] source) throws CModelException {
		fGlobals= new HashMap<String, Counter>();
		fLabels= new HashMap<String, Counter>();
		fLastLabel= null;
		fLastGlobalLabel= null;
		fLastLabelEndOffset= 0;

		final LexerOptions lexerOptions= new LexerOptions();
		fLexer= new Lexer(source, lexerOptions, new LexerLog(), null);

		// if true the next token is the first on a (logical) line
		boolean firstTokenOnLine= true;
		// next token can be an instruction or a label
		boolean expectInstruction=true;
		// inside instruction
		boolean inInstruction= false;

		IToken token= nextToken();
		while (token != null) {
			switch (token.getType()) {
			case IToken.tPOUND:
				if (fLexer.currentTokenIsFirstOnLine()) {
					parsePPDirective(fTranslationUnit);
				}
				break;
			case IToken.tDOT:
				// assembly directive?
				firstTokenOnLine= false;
				if (expectInstruction) {
					expectInstruction= false;
					token= nextToken();
					if (token != null && token.getType() == IToken.tIDENTIFIER) {
						String text= token.getImage();
						if (isGlobalDirective(text)) {
							firstTokenOnLine= parseGlobalDirective();
						} else if (isDataDirective(text)) {
							inInstruction= true;
						}
					}
				}
				break;
			case IToken.tIDENTIFIER:
				// identifier may be a label or part of an instruction
				if (firstTokenOnLine) {
					// peek next char
					char nextChar= source[token.getEndOffset()];
					if (nextChar == ':') {
						createLabel(fTranslationUnit, token);
						expectInstruction= true;
					} else {
						expectInstruction= false;
						inInstruction= true;
					}
					firstTokenOnLine= false;
				} else if (expectInstruction){
					expectInstruction= false;
					inInstruction= true;
				}
				break;
			case Lexer.tNEWLINE:
				if (!firstTokenOnLine) {
					firstTokenOnLine= true;
					if (inInstruction) {
						fLastLabelEndOffset= fLexer.currentToken().getEndOffset();
					}
				}
				break;
			default:
				expectInstruction= false;
				firstTokenOnLine= false;
				if (fLineSeparatorChar != 0) {
					if (token.getLength() == 1 && token.getCharImage()[0] == fLineSeparatorChar) {
						firstTokenOnLine= true;
					}
				}
			}
			if (firstTokenOnLine) {
				expectInstruction= true;
				inInstruction= false;
			}
			token= nextToken();
		}
		if (!firstTokenOnLine && inInstruction) {
			fLastLabelEndOffset= fLexer.currentToken().getEndOffset();
		}
		if (fLastLabel != null) {
			fixupLastLabel();
		}
		if (fLastGlobalLabel != null) {
			fixupLastGlobalLabel();
		}
	}

	/**
	 * @return the next token from the scanner or <code>null</code> on end-of-input.
	 */
	protected IToken nextToken() {
		Token token;
		try {
			token= fLexer.nextToken();
			if (token.getType() == IToken.tEND_OF_INPUT) {
				token = null;
			}
		} catch (OffsetLimitReachedException exc) {
			token= null;
		}
		return token;
	}

	private boolean parseGlobalDirective() {
		boolean eol= false;
		do {
			IToken t= nextToken();
			if (t == null) {
				break;
			}
			switch (t.getType()) {
			case IToken.tIDENTIFIER:
				registerGlobalLabel(t.getImage());
				break;
			case Lexer.tNEWLINE:
				eol= true;
				break;
			default:
				if (fLineSeparatorChar != 0) {
					if (t.getLength() == 1 && t.getCharImage()[0] == fLineSeparatorChar) {
						eol= true;
					}
				}
			}
		} while (!eol);
		return eol;
	}

	private static final boolean isDataDirective(String directive) {
		return fgDirectives.get(directive) == AsmDirective.DATA;
	}

	private static final boolean isGlobalDirective(String directive) {
		return fgDirectives.get(directive) == AsmDirective.GLOBAL;
	}

	protected void registerGlobalLabel(String labelName) {
		Counter counter= fGlobals.get(labelName);
		if (counter == null) {
			fGlobals.put(labelName, counter= new Counter());
		}
		counter.fCount++;
	}

	private int getGlobalLabelIndex(String labelName) {
		Counter counter= fGlobals.get(labelName);
		if (counter == null) {
			return 0;
		}
		return counter.fCount;
	}

	protected int registerLabel(String labelName) {
		Counter counter= fLabels.get(labelName);
		if (counter == null) {
			fLabels.put(labelName, counter= new Counter());
		}
		return counter.fCount++;
	}

	protected void createLabel(CElement parent, IToken token) throws CModelException {
		String labelName= token.getImage();
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

	private void parsePPDirective(CElement parent) throws CModelException {
		IToken token= nextToken();
		if (token != null && token.getType() == IToken.tIDENTIFIER) {
			final String image= token.getImage();
			if (image.equals("define")) { //$NON-NLS-1$
				try {
					parsePPDefine(parent);
				} catch (OffsetLimitReachedException exc) {
				}
				return;
			} else if (image.equals("include")) { //$NON-NLS-1$
				try {
					parsePPInclude(parent);
				} catch (OffsetLimitReachedException exc) {
				}
				return;
			}
		}
		try {
			skipToNewLine();
		} catch (OffsetLimitReachedException exc) {
		}
	}

	protected int skipToNewLine() throws OffsetLimitReachedException {
		return fLexer.consumeLine(fLexer.currentToken().getEndOffset());
	}

	private void parsePPDefine(CElement parent) throws CModelException, OffsetLimitReachedException {
		int startOffset= fLexer.currentToken().getOffset();
		int nameStart= 0;
		int nameEnd= 0;
		String name= null;
		IToken t= nextToken();
		if (t.getType() == IToken.tIDENTIFIER) {
			nameStart= fLexer.currentToken().getOffset();
			nameEnd= fLexer.currentToken().getEndOffset();
			name= t.getImage();
		}
		if (name == null) {
			return;
		}
		int endOffset= skipToNewLine();
		Macro macro= new Macro(parent, name);
		SourceManipulationInfo macroInfo= macro.getSourceManipulationInfo();
		macroInfo.setIdPos(nameStart, nameEnd - nameStart);
		macroInfo.setPos(startOffset, endOffset - startOffset);
		parent.addChild(macro);
	}

	private void parsePPInclude(CElement parent) throws CModelException, OffsetLimitReachedException {
		int startOffset= fLexer.currentToken().getOffset();
		int nameStart= 0;
		int nameEnd= 0;
		String name= null;
		boolean isStandard= false;
		IToken t= nextToken();
		switch (t.getType()) {
		case IToken.tLT:
			nameStart= fLexer.currentToken().getOffset();
			do {
				t= nextToken();
			} while (t.getType() != IToken.tGT);
			nameEnd= fLexer.currentToken().getEndOffset();
			name= new String(fLexer.getInputChars(nameStart + 1, nameEnd - 1));
			isStandard= true;
			break;
		case IToken.tSTRING:
			nameStart= fLexer.currentToken().getOffset();
			nameEnd= fLexer.currentToken().getEndOffset();
			name= t.getImage().substring(1, t.getLength() - 1);
			break;
		case IToken.tIDENTIFIER:
			nameStart= fLexer.currentToken().getOffset();
			nameEnd= fLexer.currentToken().getEndOffset();
			name= t.getImage();
			break;
		default:
		}
		if (name == null) {
			return;
		}
		int endOffset= skipToNewLine();
		Include include= new Include(parent, name, isStandard);
		SourceManipulationInfo includeInfo= include.getSourceManipulationInfo();
		includeInfo.setIdPos(nameStart, nameEnd - nameStart);
		includeInfo.setPos(startOffset, endOffset - startOffset);
		parent.addChild(include);
	}

}
