package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


public class CReconcilingStrategy implements IReconcilingStrategy {


	private CContentOutlinePage fOutliner;
	private ITextEditor fEditor;	
	private IWorkingCopyManager fManager;
	private IDocumentProvider fDocumentProvider;
	private IProgressMonitor fProgressMonitor;


	public CReconcilingStrategy(CEditor editor) {
		fOutliner= editor.getOutlinePage();
		fEditor= editor;
		fManager= CUIPlugin.getDefault().getWorkingCopyManager();
		fDocumentProvider= CUIPlugin.getDefault().getDocumentProvider();
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
	 * @see IReconcilingStrategy#reconcile(dirtyRegion, region)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion region) {
		reconcile();
	}
	
	private void reconcile() {
		boolean doUpdate = false;
		try {
			ITranslationUnit tu = fManager.getWorkingCopy(fEditor.getEditorInput());		
			if (tu != null && tu.isWorkingCopy()) {
				IWorkingCopy workingCopy = (IWorkingCopy)tu;
				// reconcile
				synchronized (workingCopy) {
					doUpdate = workingCopy.reconcile(true, fProgressMonitor);
				}
			}
			if(doUpdate){				
				fOutliner.contentUpdated();
			}
		} catch(CModelException e) {
				
		}
 	}	
}
