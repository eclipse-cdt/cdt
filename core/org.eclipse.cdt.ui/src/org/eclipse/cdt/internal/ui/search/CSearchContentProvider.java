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
import org.eclipse.jface.viewers.Viewer;

public abstract class CSearchContentProvider implements IStructuredContentProvider {
	protected CSearchResult _result;
	protected final Object[] EMPTY_ARR= new Object[0];
	
	public Object[] getElements(Object inputElement) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void dispose() {
		// TODO Auto-generated method stub
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		initialize((CSearchResult) newInput);
	}
	
	protected void initialize(CSearchResult result) {
		_result= result;
	}
	
	public abstract void elementsChanged(Object[] updatedElements);
	public abstract void clear();
}
