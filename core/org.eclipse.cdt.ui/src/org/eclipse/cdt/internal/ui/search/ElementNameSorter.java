/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ElementNameSorter extends ViewerSorter {

	public int compare(Viewer viewer, Object e1, Object e2) {
		String property1= getProperty(e1);
		String property2= getProperty(e2);
		return collator.compare(property1, property2);
	}

	protected String getProperty(Object element) {
		if (element instanceof ICElement)
			return ((ICElement)element).getElementName();
		if (element instanceof IResource)
			return ((IResource)element).getName();
		if (element instanceof BasicSearchMatch)
			return ((BasicSearchMatch) element).getName(); 
		return ""; //$NON-NLS-1$
	}

	public boolean isSorterProperty(Object element, String property) {
		return true;
	}
	
}
