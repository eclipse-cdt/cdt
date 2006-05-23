/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchListContentProvider implements
		IStructuredContentProvider, IPDOMSearchContentProvider {

	private TableViewer viewer;
	private PDOMSearchResult result;
	
	public Object[] getElements(Object inputElement) {
		return result.getElements();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer)viewer;
		result = (PDOMSearchResult)newInput;
	}

	public void elementsChanged(Object[] elements) {
		if (result == null)
			return;
		
		for (int i= 0; i < elements.length; i++) {
			if (result.getMatchCount(elements[i]) > 0) {
				if (viewer.testFindItem(elements[i]) != null)
					viewer.refresh(elements[i]);
				else
					viewer.add(elements[i]);
			} else {
				viewer.remove(elements[i]);
			}
		}
	}
	
	public void clear() {
		viewer.refresh();
	}

}
