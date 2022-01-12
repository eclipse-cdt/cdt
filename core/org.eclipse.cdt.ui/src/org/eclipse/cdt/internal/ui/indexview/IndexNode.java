/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.swt.graphics.Image;

class IndexNode {
	Object fParent;
	IPDOMNode fObject;
	String fText;
	Image fImage;
	boolean fHasDeclarationInProject;
	int fBindingKind = 0;

	public ICProject getProject() {
		if (fParent instanceof IndexNode) {
			return ((IndexNode) fParent).getProject();
		}
		if (fParent instanceof ICProject) {
			return (ICProject) fParent;
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fParent == null) ? 0 : fParent.hashCode());
		result = prime * result + ((fText == null) ? 0 : fText.hashCode());
		result = prime * result + fBindingKind;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final IndexNode other = (IndexNode) obj;
		if (fBindingKind != other.fBindingKind) {
			return false;
		}
		if (fParent == null) {
			if (other.fParent != null)
				return false;
		} else if (!fParent.equals(other.fParent))
			return false;
		if (fText == null) {
			if (other.fText != null)
				return false;
		} else if (!fText.equals(other.fText))
			return false;
		return true;
	}

}