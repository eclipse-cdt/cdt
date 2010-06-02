/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Marc-Andre Laperle - Bug 233390 (adapted from IncludesGrouping)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CElementGrouping;

/**
 * Grouping for macro definitions.
 * 
 * @since 5.2
 */
public class MacrosGrouping extends CElementGrouping {
	ITranslationUnit tu;

	public MacrosGrouping(ITranslationUnit unit) {
		super(CElementGrouping.MACROS_GROUPING);
		tu = unit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object object) {
		try {
			return tu.getChildrenOfType(ICElement.C_MACRO).toArray();
		} catch (CModelException e) {
		}
		return super.getChildren(object);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object object) {
		return tu;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof MacrosGrouping) {
			return tu.equals(((MacrosGrouping)obj).tu) ;
		}
		return false;
	}
}
