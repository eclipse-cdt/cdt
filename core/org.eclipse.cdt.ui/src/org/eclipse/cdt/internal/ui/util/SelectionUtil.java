/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SelectionUtil {
	/**
	 * Returns the selected element if the selection consists of a single element only.
	 *
	 * @param s the selection
	 * @return the selected first element or null
	 */
	public static Object getSingleElement(ISelection s) {
		if (!(s instanceof IStructuredSelection))
			return null;
		IStructuredSelection selection= (IStructuredSelection) s;
		if (selection.size() != 1)
			return null;

		return selection.getFirstElement();
	}
}
