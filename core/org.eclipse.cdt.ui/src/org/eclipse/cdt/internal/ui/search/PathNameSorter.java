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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PathNameSorter extends ElementNameSorter {

	public int compare(Viewer viewer, Object e1, Object e2) {
		IPath path1= getPath(e1);
		IPath path2=getPath(e2);
		return compare(path1, path2);
	}
	
	/**
	 * @param e1
	 * @return
	 */
	private IPath getPath(Object element) {
		if (element instanceof ICElement)
			return ((ICElement)element).getPath();
		if (element instanceof IResource)
			return ((IResource)element).getFullPath();
		if (element instanceof BasicSearchMatch)
			return ((BasicSearchMatch) element).getLocation();
		return new Path(""); //$NON-NLS-1$
	}

	protected int compare(IPath path1, IPath path2) {
		int segmentCount= Math.min(path1.segmentCount(), path2.segmentCount());
		for (int i= 0; i < segmentCount; i++) {
			int value= collator.compare(path1.segment(i), path2.segment(i));
			if (value != 0)
				return value;
		}
		return path1.segmentCount() - path2.segmentCount();
	}
}
