/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
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
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

public class CElementHyperlinkDetector extends AbstractHyperlinkDetector {

	public CElementHyperlinkDetector() {
	}
	
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, final IRegion region, boolean canShowMultipleHyperlinks) {
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
		IStatus status= ASTProvider.getASTProvider().runOnAST(workingCopy, ASTProvider.WAIT_ACTIVE_ONLY, null, new ASTRunnable() {
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
						// search for include statement
						final IASTNode cand= nodeSelector.findEnclosingNode(offset, length);
						if (cand instanceof IASTPreprocessorIncludeStatement) {
							linkLocation= cand.getFileLocation();
						}
					}
					if (linkLocation != null) {
						result[0]= 	new CElementHyperlink(
								new Region(linkLocation.getNodeOffset(), linkLocation.getNodeLength()), openAction);
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
}
