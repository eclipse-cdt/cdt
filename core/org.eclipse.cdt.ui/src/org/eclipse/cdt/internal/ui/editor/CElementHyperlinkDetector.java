/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsAction;

public class CElementHyperlinkDetector implements IHyperlinkDetector {

	private ITextEditor fTextEditor;

	public CElementHyperlinkDetector(ITextEditor editor) {
		fTextEditor= editor;
	}
	
	
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || canShowMultipleHyperlinks || !(fTextEditor instanceof CEditor))
			return null;
		
		CEditor editor = (CEditor) fTextEditor;
		int offset = region.getOffset();
		
		final IAction openAction= editor.getAction("OpenDeclarations"); //$NON-NLS-1$
		if (openAction == null)
			return null;

		// reuse the logic from Open Decl that recognizes a word in the editor
		final ITextSelection selection = OpenDeclarationsAction.selectWord(offset, editor);
		if(selection == null)
			return null;
		
		final IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
		if (workingCopy == null) {
			return null;
		}

		IIndex index;
		try {
			index = CCorePlugin.getIndexManager().getIndex(workingCopy.getCProject(),
				       IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
		} catch(CoreException e) {
			return null;
		} 
		
		try {
			index.acquireReadLock();
		} catch (InterruptedException e) {
			return null;
		}
		
		final IHyperlink[] result= {null};
		try {
			IStatus status= ASTProvider.getASTProvider().runOnAST(workingCopy, ASTProvider.WAIT_YES, null, new ASTRunnable() {
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
					if (ast != null) {
						IASTName[] selectedNames= 
							lang.getSelectedNames(ast, selection.getOffset(), selection.getLength());

						IRegion linkRegion;
						if(selectedNames.length > 0 && selectedNames[0] != null) { // found a name
							linkRegion = new Region(selection.getOffset(), selection.getLength());
						}
						else { // check if we are in an include statement
							linkRegion = matchIncludeStatement(ast, selection);
						}

						if(linkRegion != null)
							result[0]= new CElementHyperlink(linkRegion, openAction);
					}
					return Status.OK_STATUS;
				}
			});
			if (!status.isOK()) {
				CUIPlugin.getDefault().log(status);
			}
		} finally {
			index.releaseReadLock();
		}
		
		return result[0] == null ? null : result;
	}
	
	
	/**
	 * Returns the region that represents an include directive if one is found
	 * that matches the selection, null otherwise.
	 */
	private IRegion matchIncludeStatement(IASTTranslationUnit ast, ITextSelection selection) {
		IASTPreprocessorStatement[] preprocs = ast.getAllPreprocessorStatements();
		for (int i = 0; i < preprocs.length; ++i) {
			
			if (!(preprocs[i] instanceof IASTPreprocessorIncludeStatement))
				continue;
			
			IASTFileLocation loc = preprocs[i].getFileLocation();
			if (loc != null
					&& loc.getFileName().equals(ast.getFilePath())
					&& loc.getNodeOffset() < selection.getOffset()
					&& loc.getNodeOffset() + loc.getNodeLength() > selection.getOffset()) {
				
				return new Region(loc.getNodeOffset(), loc.getNodeLength());
			}
		}
		return null;
	}

}
