/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.StructuredViewer;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;


public class FilterUpdater implements IResourceChangeListener {

	private StructuredViewer fViewer;
	
	public FilterUpdater(StructuredViewer viewer) {
		Assert.isNotNull(viewer);
		fViewer= viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta == null)
			return;
		
		IResourceDelta[] projDeltas = delta.getAffectedChildren(IResourceDelta.CHANGED);
		for (int i= 0; i < projDeltas.length; i++) {
			IResourceDelta pDelta= projDeltas[i];
			if ((pDelta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
				final Control ctrl= fViewer.getControl();
				if (ctrl != null && !ctrl.isDisposed()) {
					// async is needed due to bug 33783
					ctrl.getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (!ctrl.isDisposed())
								fViewer.refresh(false);
						}
					});
				}
			}
		}
	}
}
