/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.jface.viewers.IElementComparer;

public class CViewElementComparer implements IElementComparer {

	@Override
	public boolean equals(Object o1, Object o2) {
		if (o1 == o2) // this handles also the case that both are null
			return true;
		if (o1 == null)
			return false; // o2 != null if we reach this point
		if (o1.equals(o2))
			return true;

		// Assume they are CElements
		ICElement c1 = (o1 instanceof ICElement) ? (ICElement) o1 : null;
		ICElement c2 = (o2 instanceof ICElement) ? (ICElement) o2 : null;
		if (c1 == null || c2 == null)
			return false;

		if (c1.getElementType() != c2.getElementType())
			return false;

		// Below is for children of TranslationUnits but we have to make sure
		// we handle the case that the child comes from the a workingCopy in that
		// case it should be equal as the original element.
		ITranslationUnit u1 = (ITranslationUnit) c1.getAncestor(ICElement.C_UNIT);
		ITranslationUnit u2 = (ITranslationUnit) c2.getAncestor(ICElement.C_UNIT);
		if (u1 == null || u2 == null) {
			return false;
		}

		if (u1.isWorkingCopy() && u2.isWorkingCopy() || !u1.isWorkingCopy() && !u2.isWorkingCopy()) {
			return false;
		}
		// From here on either c1 or c2 is a working copy.
		if (u1.isWorkingCopy()) {
			c1 = ((IWorkingCopy) u1).getOriginal(c1);
		} else if (u2.isWorkingCopy()) {
			c2 = ((IWorkingCopy) u2).getOriginal(c2);
		}
		if (c1 == null || c2 == null)
			return false;
		return c1.equals(c2);
	}

	@Override
	public int hashCode(Object o1) {
		return o1.hashCode();
	}
}
