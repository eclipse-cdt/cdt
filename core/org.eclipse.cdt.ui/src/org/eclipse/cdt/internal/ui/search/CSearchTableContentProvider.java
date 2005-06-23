/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;

public class CSearchTableContentProvider extends CSearchContentProvider implements IStructuredContentProvider {

	private TableViewer _tableViewer;

	public CSearchTableContentProvider(TableViewer viewer) {
		_tableViewer= viewer;
	}
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof CSearchResult)
			return ((CSearchResult)inputElement).getElements();
		return EMPTY_ARR;
	}

	public void elementsChanged(Object[] updatedElements) {
		if (_result == null)
			return;
		
		int addCount= 0;
		int removeCount= 0;
		for (int i= 0; i < updatedElements.length; i++) {
			if (_result.getMatchCount(updatedElements[i]) > 0) {
				if (_tableViewer.testFindItem(updatedElements[i]) != null)
					_tableViewer.refresh(updatedElements[i]);
				else
					_tableViewer.add(updatedElements[i]);
				addCount++;
			} else {
				_tableViewer.remove(updatedElements[i]);
				removeCount++;
			}
		}
	}

	public void clear() {
		_tableViewer.refresh();
	}

}
