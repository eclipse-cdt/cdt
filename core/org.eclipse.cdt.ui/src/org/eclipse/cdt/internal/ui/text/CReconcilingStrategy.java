package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;


public class CReconcilingStrategy implements IReconcilingStrategy {


	private CContentOutlinePage fOutliner;
	private int fLastRegionOffset;


	public CReconcilingStrategy(CEditor editor) {
		fOutliner= editor.getOutlinePage();
		fLastRegionOffset = Integer.MAX_VALUE;
	}
	
	/**
	 * @see IReconcilingStrategy#reconcile(document)
	 */
	public void setDocument(IDocument document) {
	}	


	/**
	 * @see IReconcilingStrategy#reconcile(region)
	 */
	public void reconcile(IRegion region) {
		// We use a trick to avoid running the reconciler multiple times
		// on a file when it gets changed. This is because this gets called
		// multiple times with different regions of the file, we do a 
		// complete parse on the first region.
		if(region.getOffset() <= fLastRegionOffset) {
			reconcile();
		}
		fLastRegionOffset = region.getOffset();
	}


	/**
	 * @see IReconcilingStrategy#reconcile(dirtyRegion, region)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion region) {
		// FIXME: This seems to generate to much flashing in
		// the contentouline viewer.
		//reconcile();
	}
	
	private void reconcile() {
		fOutliner.contentUpdated();
	}	
}
