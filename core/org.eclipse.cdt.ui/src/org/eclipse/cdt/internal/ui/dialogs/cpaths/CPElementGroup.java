/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;

public class CPElementGroup {

	CPElement element;
	int kind;

	public CPElementGroup(CPElement element, int kind) {
		this.element = element;
		this.kind = kind;
	}

	public CPElement getElement() {
		return element;
	}
	
	public int getEntryType() {
		return kind;
	}

	public Object[] getChildren() {
		Object[] children = element.getChildren();
		List rv = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			if ((children[i] instanceof CPElement) && ((CPElement)children[i]).getEntryKind() == kind) {
				rv.add(children[i]);
			}
		}
		return rv.toArray();
	}
}
