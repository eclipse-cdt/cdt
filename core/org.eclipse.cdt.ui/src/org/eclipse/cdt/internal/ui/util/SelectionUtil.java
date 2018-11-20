/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

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
		IStructuredSelection selection = (IStructuredSelection) s;
		if (selection.size() != 1)
			return null;

		return selection.getFirstElement();
	}

	/**
	 * Returns the selection in the currently active workbench window part.
	 * If the no selection exists or no selection exists in the active part <code>null</code> is returned.
	 *
	 * @return the current selection in the active workbench window part or null
	 */
	public static ISelection getActiveSelection() {
		ISelection selection = null;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage activePage = window.getActivePage();
			if (activePage != null) {
				IWorkbenchPart activePart = activePage.getActivePart();
				if (activePart != null) {
					selection = window.getSelectionService().getSelection(activePart.getSite().getId());
				}
			}
		}
		return selection;
	}
}
