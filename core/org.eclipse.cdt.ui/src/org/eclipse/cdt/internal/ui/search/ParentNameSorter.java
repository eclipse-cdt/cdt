/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jun 24, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ParentNameSorter extends ElementNameSorter {
		
	public int compare(Viewer viewer, Object e1, Object e2) {
		String leftParent= getParentName(e1);
		String rightParent= getParentName(e2);
		return collator.compare(leftParent, rightParent);
	}
	
	private String getParentName(Object element) {
		if (element instanceof ICElement) {
			ICElement parent= ((ICElement)element).getParent();
		
			if (parent != null)
				return parent.getElementName();
		}
		if (element instanceof IResource) {
			IResource parent= ((IResource)element).getParent();
			if (parent != null)
				return parent.getName();
		}
		if (element instanceof BasicSearchMatch){
			return ((BasicSearchMatch) element).getParentName();
		}
		return ""; //$NON-NLS-1$
	}
}
