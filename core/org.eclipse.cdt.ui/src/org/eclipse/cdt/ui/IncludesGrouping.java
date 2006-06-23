/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;


/**
 * IncludesGrouping
 */
public class IncludesGrouping extends CElementGrouping {
	ITranslationUnit tu;

	public IncludesGrouping(ITranslationUnit unit) {
		super(CElementGrouping.INCLUDES_GROUPING);
		tu = unit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object object) {
		try {
			return tu.getChildrenOfType(ICElement.C_INCLUDE).toArray();
		} catch (CModelException e) {
		}
		return super.getChildren(object);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object object) {
		return tu;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IncludesGrouping) {
			return tu.equals(((IncludesGrouping)obj).getParent(obj)) ;
		}
		return super.equals(obj);
	}
}
