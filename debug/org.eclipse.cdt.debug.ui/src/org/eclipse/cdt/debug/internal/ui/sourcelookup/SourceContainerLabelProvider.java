/**********************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup; 

import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupUIUtils;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Label provider for source containers and source container types.
 */
public class SourceContainerLabelProvider extends LabelProvider {
	
	private ILabelProvider fLabelProvider = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		// first allow workbench adapter to provide image
		Image image = getWorkbenchLabelProvider().getImage(element);
		if (image == null) {
			ISourceContainerType type = null;
			if (element instanceof ISourceContainer) {
				type = ((ISourceContainer)element).getType();
			} else if (element instanceof ISourceContainerType) {
				type = (ISourceContainerType) element;
			}
			if (type != null) {
				// next consult contributed image
				image = SourceLookupUIUtils.getSourceContainerImage(type.getId());
			}
		}		
		if (image != null) {
			return image;
		}
		return super.getImage(element);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		// first, allo workbench adapter to provide label
		String label = getWorkbenchLabelProvider().getText(element);
		if (label == null || label.length() == 0) {
			if (element instanceof ISourceContainer) {
				return ((ISourceContainer) element).getName(); 
			} else if (element instanceof ISourceContainerType) {
				return ((ISourceContainerType)element).getName();
			}
		} else {
			return label;
		}
		return super.getText(element);
	}
	
	private ILabelProvider getWorkbenchLabelProvider() {
		if (fLabelProvider == null) {
			fLabelProvider = new WorkbenchLabelProvider();
		}
		return fLabelProvider;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fLabelProvider != null) {
			fLabelProvider.dispose();
		}
	}
}
