/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import java.text.Collator;
import java.util.Comparator;

import org.eclipse.ui.IWorkingSet;

class WorkingSetComparator implements Comparator {

	private Collator fCollator= Collator.getInstance();
	
	/*
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2) {
		String name1= null;
		String name2= null;
		
		if (o1 instanceof IWorkingSet)
			name1= ((IWorkingSet)o1).getName();

		if (o2 instanceof IWorkingSet)
			name2= ((IWorkingSet)o2).getName();

		return fCollator.compare(name1, name2);
	}
}
