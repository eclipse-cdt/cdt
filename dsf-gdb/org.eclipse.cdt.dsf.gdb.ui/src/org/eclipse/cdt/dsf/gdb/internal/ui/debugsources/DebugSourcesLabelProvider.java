/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Baha El-Kassaby - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources;

import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.tree.DebugTree;
import org.eclipse.jface.viewers.ColumnLabelProvider;

public class DebugSourcesLabelProvider extends ColumnLabelProvider {

	private int index;

	/**
	 * 
	 * @param index of column
	 */
	public DebugSourcesLabelProvider(int index) {
		this.index = index;
	}

	@Override
	public String getText(Object element) {
		return getLabel(element, index);
	}

	private String getLabel(Object element, int columnIdx) {
		String emptyString = ""; //$NON-NLS-1$
		if (element instanceof String) {
			return (String) element;
		}
		if (element instanceof DebugTree) {
			DebugTree<?> node = (DebugTree<?>) element;
			if (columnIdx == 0) {
				return (String) node.getData();
			}
			if (columnIdx == 1) {
				return node.hasChildren() ? emptyString : (String) node.getLeafData();
			}
		}
		return emptyString;
	}
}
