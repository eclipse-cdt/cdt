/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;

public class RefactoringStatusContentProvider implements IStructuredContentProvider{

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object obj) {
			return ((RefactoringStatus)obj).getEntries().toArray();
		}
}

