/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.ui.cview;

import java.util.Iterator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.dnd.BasicSelectionTransferDragAdapter;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SelectionTransferDragAdapter extends BasicSelectionTransferDragAdapter {
	
	public SelectionTransferDragAdapter(ISelectionProvider provider) {
		super(provider);
	}
	
	protected boolean isDragable(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			for (Iterator iter= ((IStructuredSelection)selection).iterator(); iter.hasNext();) {
				Object element= iter.next();
				if (element instanceof ICElement) {
					ICElement celement = (ICElement)element;
					IResource res = celement.getResource();
					if (res != null) {
						return false;
					}
					if (!(element instanceof ISourceReference)) {
						return  false;
					}
				}
			}
			return true;
		}
		return false;
	}
}
