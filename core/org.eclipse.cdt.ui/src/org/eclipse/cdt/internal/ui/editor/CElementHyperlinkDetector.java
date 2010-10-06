/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.formatter.scanner.Scanner;
import org.eclipse.cdt.internal.formatter.scanner.Token;

import org.eclipse.cdt.internal.ui.text.CWordFinder;

public class CElementHyperlinkDetector extends AbstractHyperlinkDetector {

	public CElementHyperlinkDetector() {
	}

	public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor textEditor= (ITextEditor) getAdapter(ITextEditor.class);
		if (region == null || !(textEditor instanceof CEditor))
			return null;

		final IAction openAction= textEditor.getAction("OpenDeclarations"); //$NON-NLS-1$
		if (openAction == null)
			return null;

		IDocument document = textViewer.getDocument();
		final IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(textEditor.getEditorInput());
		if (workingCopy == null) {
			return null;
		}

		final IRegion[] hyperlinkRegion = { null };
		// Do not wait for AST if it's not available yet. Waiting for AST would block the UI thread
		// for the duration of the parsing.
		IStatus status= ASTProvider.getASTProvider().runOnAST(workingCopy, ASTProvider.WAIT_NO, null, new ASTRunnable() {
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
				if (ast == null)
					return Status.CANCEL_STATUS;
				final int offset= region.getOffset();
				final int length= Math.max(1, region.getLength());
				final IASTNodeSelector nodeSelector= ast.getNodeSelector(null);
				IASTName selectedName= nodeSelector.findEnclosingName(offset, length);
				IASTFileLocation linkLocation= null;
				if (selectedName != null) { // found a name
					// Prefer include statement over the include name
					if (selectedName.getParent() instanceof IASTPreprocessorIncludeStatement) {
						linkLocation= selectedName.getParent().getFileLocation();
					} else {
						linkLocation= selectedName.getFileLocation();
					}
				} else { 
					final IASTNode implicit = nodeSelector.findEnclosingImplicitName(offset, length);
					if (implicit != null) {
						linkLocation = implicit.getFileLocation();
					} else {
						// Search for include statement
						final IASTNode cand= nodeSelector.findEnclosingNode(offset, length);
						if (cand instanceof IASTPreprocessorIncludeStatement) {
							linkLocation= cand.getFileLocation();
						}
					}
				}
				if (linkLocation != null) {
					hyperlinkRegion[0] = new Region(linkLocation.getNodeOffset(), linkLocation.getNodeLength());
				}
				return Status.OK_STATUS;
			}
		});

		if (status == Status.CANCEL_STATUS) {
			// AST is not available yet, try to compute the hyperlink without it.  
			try {
				// Check partition type.
				String partitionType= TextUtilities.getContentType(document, ICPartitions.C_PARTITIONING, region.getOffset(), false);
				if (IDocument.DEFAULT_CONTENT_TYPE.equals(partitionType)) {
					// Regular code.
					hyperlinkRegion[0] = getIdentifier(document, region.getOffset(), workingCopy.getLanguage());
				} else if (ICPartitions.C_PREPROCESSOR.equals(partitionType)) {
					// Preprocessor directive.
					Scanner scanner= new Scanner();
					scanner.setSplitPreprocessor(true);
		            scanner.setSource(document.get().toCharArray());
		            scanner.setCurrentPosition(findPreprocessorDirectiveStart(document, region.getOffset()));
		            Token token = scanner.nextToken();
		            if (token.getType() == Token.tPREPROCESSOR_INCLUDE) {
		            	int endPos = token.getOffset() + token.getLength();
		            	// Trim trailing whitespace.
		            	while (Character.isWhitespace(document.getChar(--endPos))) {
		            	}
		            	endPos++;
			            if (region.getOffset() <= endPos) {
			            	hyperlinkRegion[0] = new Region(token.getOffset(), endPos - token.getOffset());
			            }
		            } else {
						hyperlinkRegion[0] = getIdentifier(document, region.getOffset(), workingCopy.getLanguage());
		            }
				}
			} catch (BadLocationException e) {
				// Ignore to return null.
			} catch (CoreException e) {
				// Ignore to return null.
			}
		}
		if (hyperlinkRegion[0] == null)
			return null;
		return new IHyperlink[] { new CElementHyperlink(hyperlinkRegion[0], openAction) };
	}

	/**
	 * Returns the identifier at the given offset, or {@code null} if the there is no identifier
	 * at the offset.
	 */
	private static IRegion getIdentifier(IDocument document, int offset, ILanguage language) throws BadLocationException {
		IRegion wordRegion= CWordFinder.findWord(document, offset);
		if (wordRegion != null && wordRegion.getLength() > 0) {
			String word = document.get(wordRegion.getOffset(), wordRegion.getLength());
			if (!Character.isDigit(word.charAt(0)) && !isLanguageKeyword(language, word)) {
				return wordRegion;
			}
		}
		return null;
	}

	private static boolean isLanguageKeyword(ILanguage lang, String word) {
		ICLanguageKeywords keywords= (ICLanguageKeywords) lang.getAdapter(ICLanguageKeywords.class);
		if (keywords != null) {
			for (String keyword : keywords.getKeywords()) {
				if (keyword.equals(word))
					return true;
			}
			for (String type : keywords.getBuiltinTypes()) {
				if (type.equals(word))
					return true;
			}
			for (String keyword : keywords.getPreprocessorKeywords()) {
				if (keyword.charAt(0) == '#' && keyword.length() == word.length() + 1 &&
						keyword.regionMatches(1, word, 0, word.length())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Finds beginning of a preprocessor directive.
	 */
	private static int findPreprocessorDirectiveStart(IDocument document, int offset) throws BadLocationException {
		while (true) {
			IRegion lineRegion = document.getLineInformationOfOffset(offset);
			int lineOffset = lineRegion.getOffset();
			if (lineOffset == 0)
				return lineOffset;
			int lineEnd = lineOffset + lineRegion.getLength();
			for (offset = lineOffset; offset < lineEnd && Character.isWhitespace(document.getChar(offset)); offset++) {
			}
			if (offset < document.getLength() && document.getChar(offset) == '#') {
				return lineOffset;
			}
			// The line doesn't start with #, try previous line.
			offset = lineOffset - 1;
		}
	}
}
