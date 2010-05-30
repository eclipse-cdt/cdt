/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.ui.text.ICPartitions;

/**
 * Helper class to match pairs of characters.
 */
public class CPairMatcher extends DefaultCharacterPairMatcher {

	private static final int ANGLE_BRACKETS_SEARCH_BOUND = 200;
	
	private boolean fMatchAngularBrackets= true;
	private int fAnchor= -1;

	public CPairMatcher(char[] pairs) {
		super(pairs, ICPartitions.C_PARTITIONING);
	}

	/* @see ICharacterPairMatcher#match(IDocument, int) */
	@Override
	public IRegion match(IDocument document, int offset) {
		try {
			return performMatch(document, offset);
		} catch (BadLocationException ble) {
			return null;
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.source.DefaultCharacterPairMatcher#getAnchor()
	 */
	@Override
	public int getAnchor() {
		if (fAnchor < 0) {
			return super.getAnchor();
		}
		return fAnchor;
	}

	/*
	 * Performs the actual work of matching for #match(IDocument, int).
	 */ 
	private IRegion performMatch(IDocument document, int offset) throws BadLocationException {
		if (offset < 0 || document == null) return null;
		final char prevChar= document.getChar(Math.max(offset - 1, 0));
		if ((prevChar == '<' || prevChar == '>') && !fMatchAngularBrackets)
			return null;
		final IRegion region;
		if (prevChar == '<') {
			region= findClosingAngleBracket(document, offset - 1);
			fAnchor= ICharacterPairMatcher.LEFT;
		} else if (prevChar == '>') {
			region= findOpeningAngleBracket(document, offset - 1);
			fAnchor= ICharacterPairMatcher.RIGHT;
		} else {
			region= super.match(document, offset);
			fAnchor= -1;
		}
		if (region != null) {
			if (prevChar == '>') {
				final int peer= region.getOffset();
				if (isLessThanOperator(document, peer))
					return null;
			} else if (prevChar == '<') {
				final int peer= region.getOffset() + region.getLength() - 1;
				if (isGreaterThanOperator(document, peer))
					return null;
			}
		}
		return region;
	}

	/**
	 * Returns the region enclosing the matching angle brackets.
	 * 
	 * @param document a document
	 * @param offset an offset within the document pointing after the closing angle bracket
	 * @return the matching region or {@link NullPointerException} if no match could be found
	 * @throws BadLocationException 
	 */
	private IRegion findOpeningAngleBracket(IDocument document, int offset) throws BadLocationException {
		if (offset < 0) return null;
		final String contentType= TextUtilities.getContentType(document, ICPartitions.C_PARTITIONING, offset, false);
		CHeuristicScanner scanner= new CHeuristicScanner(document, ICPartitions.C_PARTITIONING, contentType);
		if (isTemplateParameterCloseBracket(offset, document, scanner)) {
			int pos= scanner.findOpeningPeer(offset - 1, Math.max(0, offset - ANGLE_BRACKETS_SEARCH_BOUND), '<', '>');
			if (pos != CHeuristicScanner.NOT_FOUND) {
				return new Region(pos, offset - pos + 1);
			}
		}
		return null;
	}

	/**
	 * Returns the region enclosing the matching angle brackets.
	 * 
	 * @param document a document
	 * @param offset an offset within the document pointing after the opening angle bracket
	 * @return the matching region or {@link NullPointerException} if no match could be found
	 * @throws BadLocationException 
	 */
	private IRegion findClosingAngleBracket(IDocument document, int offset) throws BadLocationException {
		if (offset < 0) return null;
		final String contentType= TextUtilities.getContentType(document, ICPartitions.C_PARTITIONING, offset, false);
		CHeuristicScanner scanner= new CHeuristicScanner(document, ICPartitions.C_PARTITIONING, contentType);
		if (isTemplateParameterOpenBracket(offset, document, scanner)) {
			int pos= scanner.findClosingPeer(offset + 1, Math.min(document.getLength(), offset + ANGLE_BRACKETS_SEARCH_BOUND), '<', '>');
			if (pos != CHeuristicScanner.NOT_FOUND) {
				return new Region(offset, pos - offset + 1);
			}
		}
		return null;
	}

	/**
	 * Returns true if the character at the specified offset is a
	 * less-than sign, rather than an template parameter list open
	 * angle bracket.
	 * 
	 * @param document a document
	 * @param offset an offset within the document
	 * @return true if the character at the specified offset is not
	 *   a template parameter start bracket
	 * @throws BadLocationException
	 */
	private boolean isLessThanOperator(IDocument document, int offset) throws BadLocationException {
		if (offset < 0) return false;
		final String contentType= TextUtilities.getContentType(document, ICPartitions.C_PARTITIONING, offset, false);
		CHeuristicScanner scanner= new CHeuristicScanner(document, ICPartitions.C_PARTITIONING, contentType);
		return !isTemplateParameterOpenBracket(offset, document, scanner);
	}

	/**
	 * Returns true if the character at the specified offset is a
	 * greater-than sign, rather than an template parameter list close
	 * angle bracket.
	 * 
	 * @param document a document
	 * @param offset an offset within the document
	 * @return true if the character at the specified offset is not
	 *   a template parameter end bracket
	 * @throws BadLocationException
	 */
	private boolean isGreaterThanOperator(IDocument document, int offset) throws BadLocationException {
		if (offset < 0) return false;
		final String contentType= TextUtilities.getContentType(document, ICPartitions.C_PARTITIONING, offset, false);
		CHeuristicScanner scanner= new CHeuristicScanner(document, ICPartitions.C_PARTITIONING, contentType);
		return !isTemplateParameterCloseBracket(offset, document, scanner);
	}

	/**
	 * Checks if the angular bracket at <code>offset</code> is a template
	 * parameter bracket.
	 *
	 * @param offset the offset of the opening bracket
	 * @param document the document
	 * @param scanner a heuristic scanner on <code>document</code>
	 * @return <code>true</code> if the bracket is part of a template parameter,
	 *         <code>false</code> otherwise
	 */
	private boolean isTemplateParameterOpenBracket(int offset, IDocument document, CHeuristicScanner scanner) {
		int prevToken= scanner.previousToken(offset - 1, Math.max(0, offset - ANGLE_BRACKETS_SEARCH_BOUND));
		if (prevToken == Symbols.TokenIDENT || prevToken == Symbols.TokenTEMPLATE) {
			return true;
		}

		return false;
	}

	/**
	 * Checks if the angular bracket at <code>offset</code> is a template
	 * parameter bracket.
	 *
	 * @param offset the offset of the closing bracket
	 * @param document the document
	 * @param scanner a heuristic scanner on <code>document</code>
	 * @return <code>true</code> if the bracket is part of a template parameter,
	 *         <code>false</code> otherwise
	 */
	private boolean isTemplateParameterCloseBracket(int offset, IDocument document, CHeuristicScanner scanner) {
		if (offset >= document.getLength() - 1)
			return true;
		int thisToken= scanner.previousToken(offset, Math.max(0, offset - ANGLE_BRACKETS_SEARCH_BOUND));
		if (thisToken != Symbols.TokenGREATERTHAN)
			return false;
		int prevToken= scanner.previousToken(scanner.getPosition(), Math.max(0, offset - ANGLE_BRACKETS_SEARCH_BOUND));
		if (prevToken == Symbols.TokenGREATERTHAN)
			return true;
		int nextToken= scanner.nextToken(offset + 1, Math.min(document.getLength(), offset + ANGLE_BRACKETS_SEARCH_BOUND));

		switch (nextToken) {
		case Symbols.TokenGREATERTHAN:
		case Symbols.TokenCOMMA:
		case Symbols.TokenSEMICOLON:
		case Symbols.TokenCLASS:
		case Symbols.TokenSTRUCT:
		case Symbols.TokenUNION:
			return true;
		}
		return false;
	}

	/**
	 * Configure this bracket matcher for the given language.
	 * @param language
	 */
	public void configure(ILanguage language) {
		fMatchAngularBrackets= language != null && language.getLinkageID() == ILinkage.CPP_LINKAGE_ID;
	}

}
