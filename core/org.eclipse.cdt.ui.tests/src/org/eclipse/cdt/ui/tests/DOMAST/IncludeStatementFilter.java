/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * @author dsteffle
 */
public class IncludeStatementFilter extends ViewerFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof TreeObject) {
			int flag = ((TreeObject)element).getFiltersFlag() & TreeObject.FLAG_INCLUDE_STATEMENTS;
			if (flag > 0) {
				IASTNode node = ((TreeObject)element).getNode();
				if (node instanceof IASTPreprocessorIncludeStatement)
					return false;
				
				return true;
			}
		}

		return true;
	}

}
