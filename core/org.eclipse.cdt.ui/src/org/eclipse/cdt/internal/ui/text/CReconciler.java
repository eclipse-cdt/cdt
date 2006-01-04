package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.Reconciler;

public class CReconciler extends Reconciler {
	
	protected void process(DirtyRegion dirtyRegion) {
		if(dirtyRegion != null) {
			getReconcilingStrategy(IDocument.DEFAULT_CONTENT_TYPE).reconcile(dirtyRegion, null);
		}
	}
	
}
