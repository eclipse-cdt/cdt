/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.Arrays;

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

import org.eclipse.cdt.internal.ui.text.CWordFinder;

public class CElementHyperlinkDetector extends AbstractHyperlinkDetector {

	public CElementHyperlinkDetector() {
	}
	
	public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor textEditor= (ITextEditor)getAdapter(ITextEditor.class);
		if (region == null || !(textEditor instanceof CEditor))
			return null;

		final IAction openAction= textEditor.getAction("OpenDeclarations"); //$NON-NLS-1$
		if (openAction == null)
			return null;

		// check partition type
		try {
			String partitionType= TextUtilities.getContentType(textViewer.getDocument(), ICPartitions.C_PARTITIONING, region.getOffset(), false);
			if (!IDocument.DEFAULT_CONTENT_TYPE.equals(partitionType) && !ICPartitions.C_PREPROCESSOR.equals(partitionType)) {
				return null;
			}
		} catch (BadLocationException exc) {
			return null;
		}
		
		final IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(textEditor.getEditorInput());
		if (workingCopy == null) {
			return null;
		}

		final IHyperlink[] result= {null};
		// Do not wait for AST if it's not available yet. Waiting for AST would block the UI thread
		// for the duration of the parsing.
		IStatus status= ASTProvider.getASTProvider().runOnAST(workingCopy, ASTProvider.WAIT_NO, null, new ASTRunnable() {
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
				if (ast != null) {
					final int offset= region.getOffset();
					final int length= Math.max(1, region.getLength());
					final IASTNodeSelector nodeSelector= ast.getNodeSelector(null);
					IASTName selectedName= nodeSelector.findEnclosingName(offset, length);
					IASTFileLocation linkLocation= null;
					if (selectedName != null) { // found a name
						// prefer include statement over the include name
						if (selectedName.getParent() instanceof IASTPreprocessorIncludeStatement) {
							linkLocation= selectedName.getParent().getFileLocation();
						}
						else {
							linkLocation= selectedName.getFileLocation();
						}
					}
					else { 
						final IASTNode implicit = nodeSelector.findEnclosingImplicitName(offset, length);
						if(implicit != null) {
							linkLocation = implicit.getFileLocation();
						}
						else {
							// search for include statement
							final IASTNode cand= nodeSelector.findEnclosingNode(offset, length);
							if (cand instanceof IASTPreprocessorIncludeStatement) {
								linkLocation= cand.getFileLocation();
							}
						}
					}
					if (linkLocation != null) {
						result[0]= 	new CElementHyperlink(
								new Region(linkLocation.getNodeOffset(), linkLocation.getNodeLength()), openAction);
					} else {
						// consider fallback navigation
						final IDocument document= textViewer.getDocument();
						IRegion wordRegion= CWordFinder.findWord(document, offset);
						if (wordRegion != null) {
							try {
								String word = document.get(wordRegion.getOffset(), wordRegion.getLength());
								if(word.length() > 0 && !Character.isDigit(word.charAt(0)) && !isLanguageKeyword(lang, word)) {
									result[0]= 	new CElementHyperlink(
											new Region(wordRegion.getOffset(), wordRegion.getLength()), openAction);
								}
							} catch (BadLocationException exc) {
								// ignore
							}
						}
					}
				}
				return Status.OK_STATUS;
			}
		});
		if (!status.isOK()) {
			CUIPlugin.log(status);
		}
		
		return result[0] == null ? null : result;
	}

	private static boolean isLanguageKeyword(ILanguage lang, String word) {
		ICLanguageKeywords keywords= (ICLanguageKeywords) lang.getAdapter(ICLanguageKeywords.class);
		if (keywords != null) {
			if (Arrays.asList(keywords.getKeywords()).contains(word)) {
				return true;
			}
			if (Arrays.asList(keywords.getBuiltinTypes()).contains(word)) {
				return true;
			}
			if (Arrays.asList(keywords.getPreprocessorKeywords()).contains('#'+word)) {
				return true;
			}
		}
		return false;
	}
}
