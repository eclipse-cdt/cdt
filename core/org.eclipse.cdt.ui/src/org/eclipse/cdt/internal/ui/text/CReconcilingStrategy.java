/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;

import org.eclipse.cdt.internal.core.model.CModelManager;


public class CReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private ITextEditor fEditor;	
	private IWorkingCopyManager fManager;
	private IProgressMonitor fProgressMonitor;
	private String txt = null;
	// used by tests
	protected boolean fInitialProcessDone;
	
	public CReconcilingStrategy(ITextEditor editor) {
		fEditor= editor;
		fManager= CUIPlugin.getDefault().getWorkingCopyManager();
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public void setDocument(IDocument document) {
	}	

	/*
	 * @see IReconcilingStrategyExtension#setProgressMonitor(IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(IRegion region) {
		reconcile(false);
	}


	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion, org.eclipse.jface.text.IRegion)
	 * Called for incremental reconciler only - currently not used (no shift deltas)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion region) {
		// consistent data needs not further checks !  
		ITranslationUnit tu = fManager.getWorkingCopy(fEditor.getEditorInput());		
		if (tu != null && tu.isWorkingCopy()) {
			try {
				if (tu.isConsistent()) return;
			} catch (CModelException e) {}	
		}
		
		// bug 113518
		// local data needs not to be re-parsed
		boolean needReconcile = true;
		int dOff = dirtyRegion.getOffset();
		int dLen = dirtyRegion.getLength();		
		IDocument doc = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
		
		if ((doc != null) && (!CWordFinder.isGlobal(doc, dOff))) {
			String s = ""; //$NON-NLS-1$
			if (dirtyRegion.getType().charAt(2) == 'i') { // insert operation
				s = dirtyRegion.getText();
				if (!CWordFinder.hasCBraces(s)) {
					CModelManager.getDefault().fireShift(tu, dOff, dLen, CWordFinder.countLFs(s));
					needReconcile = false;
				}					
			} else { // remove operation
				// check whether old document copy is relevant
				if (txt != null && (txt.length() == doc.getLength() + dLen)) {
					s = txt.substring(dOff, dOff + dLen);
					if (!CWordFinder.hasCBraces(s)) {
						CModelManager.getDefault().fireShift(tu, dOff, -dLen, -CWordFinder.countLFs(s));
						needReconcile = false;						
					}
				}
			}
		} 
		if (needReconcile) reconcile(false);
		txt = doc.get(); // save doc copy for further use
	}
	
	private void reconcile(final boolean initialReconcile) {
		boolean computeAST= fEditor instanceof ICReconcilingListener;
		IASTTranslationUnit ast= null;
		IWorkingCopy workingCopy= fManager.getWorkingCopy(fEditor.getEditorInput());
		if (workingCopy == null) {
			return;
		}
		boolean forced= false;
		try {
			// reconcile
			synchronized (workingCopy) {
				forced= workingCopy.isConsistent();
				ast= workingCopy.reconcile(computeAST, true, fProgressMonitor);
			}
		} catch(OperationCanceledException oce) {
			// document was modified while parsing
		} catch(CModelException e) {
			IStatus status= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.OK, "Error in CDT UI during reconcile", e);  //$NON-NLS-1$
			CUIPlugin.getDefault().log(status);
		} finally {
			if (computeAST) {
				IIndex index= null;
				if (ast != null) {
					index= ast.getIndex();
				}
				try {
					((ICReconcilingListener)fEditor).reconciled(ast, forced, fProgressMonitor);
				} finally {
					if (index != null) {
						index.releaseReadLock();
					}
				}
			}
		}
 	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		reconcile(true);
		fInitialProcessDone= true;
	}

	void aboutToBeReconciled() {
		if (fEditor instanceof ICReconcilingListener) {
			((ICReconcilingListener)fEditor).aboutToBeReconciled();
		}
	}	
}
