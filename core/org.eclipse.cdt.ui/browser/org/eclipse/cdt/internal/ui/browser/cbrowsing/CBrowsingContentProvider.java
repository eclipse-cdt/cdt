/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import java.util.Collection;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public abstract class CBrowsingContentProvider implements ITreeContentProvider, IElementChangedListener {
	
	protected StructuredViewer fViewer;
	protected Object fInput;
	protected CBrowsingPart fBrowsingPart;
	protected int fReadsInDisplayThread;
	protected static final Object[] NO_CHILDREN = new Object[0];
	
	public CBrowsingContentProvider(CBrowsingPart browsingPart) {
		fBrowsingPart= browsingPart;
		fViewer= fBrowsingPart.getViewer();
		CoreModel.getDefault().addElementChangedListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof Collection) {
			// Get a template object from the collection
			Collection col = (Collection) newInput;
			if (!col.isEmpty())
				newInput = col.iterator().next();
			else
				newInput = null;
		}
		fInput = newInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		CoreModel.getDefault().removeElementChangedListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent event) {
//		TODO listen for cache updates
//		try {
//			processDelta(event.getDelta());
//		} catch(CModelException e) {
//			CUIPlugin.getDefault().log(e.getStatus());
//		}
	}
	
	
	protected void startReadInDisplayThread() {
		if (isDisplayThread())
			fReadsInDisplayThread++;
	}

	protected void finishedReadInDisplayThread() {
		if (isDisplayThread())
			fReadsInDisplayThread--;
	}
	
	private boolean isDisplayThread() {
		Control ctrl= fViewer.getControl();
		if (ctrl == null)
			return false;
		
		Display currentDisplay= Display.getCurrent();
		return currentDisplay != null && currentDisplay.equals(ctrl.getDisplay());
	}
	
	/**
	 * Note: This method is for internal use only. Clients should not call this method.
	 */
	protected static Object[] concatenate(Object[] a1, Object[] a2) {
		int a1Len = a1.length;
		int a2Len = a2.length;
		Object[] res = new Object[a1Len + a2Len];
		System.arraycopy(a1, 0, res, 0, a1Len);
		System.arraycopy(a2, 0, res, a1Len, a2Len); 
		return res;
	}
}
