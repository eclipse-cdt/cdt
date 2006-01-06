/*******************************************************************************
 * Copyright (c) 2000 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.IReconcilingParticipant;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.ui.texteditor.ITextEditor;


public class CReconcilingStrategy implements IReconcilingStrategy {

	private ITextEditor fEditor;	
	private IWorkingCopyManager fManager;
	private IProgressMonitor fProgressMonitor;
	private String txt = null;
	
	public CReconcilingStrategy(CEditor editor) {
		fEditor= editor;
		fManager= CUIPlugin.getDefault().getWorkingCopyManager();
	}
	
	/**
	 * @see IReconcilingStrategy#reconcile(document)
	 */
	public void setDocument(IDocument document) {
	}	


	/*
	 * @see IReconcilingStrategyExtension#setProgressMonitor(IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}

	/**
	 * @see IReconcilingStrategy#reconcile(region)
	 */
	public void reconcile(IRegion region) {
		reconcile();
	}


	/**
	 * @see IReconcilingStrategy#reconcile(dirtyRegion, reion)
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
			String s = "";
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
		if (needReconcile) reconcile();
		txt = doc.get(); // save doc copy for further use
	}
	
	private void reconcile() {
		try {
			ITranslationUnit tu = fManager.getWorkingCopy(fEditor.getEditorInput());		
			if (tu != null && tu.isWorkingCopy()) {
				IWorkingCopy workingCopy = (IWorkingCopy)tu;
				// reconcile
				synchronized (workingCopy) {
					workingCopy.reconcile(true, fProgressMonitor);
				}
			}
			
			// update participants
			if (fEditor instanceof IReconcilingParticipant /*&& !fProgressMonitor.isCanceled()*/) {
				IReconcilingParticipant p= (IReconcilingParticipant) fEditor;
				p.reconciled(true);
			}
			
		} catch(CModelException e) {
				
		}
 	}	
}
