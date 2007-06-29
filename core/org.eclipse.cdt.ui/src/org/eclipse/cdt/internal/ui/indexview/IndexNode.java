/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.model.ICProject;

class IndexNode {
	Object fParent;
	IPDOMNode fObject;
	String fText;
	Image fImage;
	boolean fHasDeclarationInProject;
	
	public ICProject getProject() {
		if (fParent instanceof IndexNode) {
			return ((IndexNode) fParent).getProject();
		}
		if (fParent instanceof ICProject) {
			return (ICProject) fParent;
		}
		return null;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fParent == null) ? 0 : fParent.hashCode());
		result = prime * result + ((fText == null) ? 0 : fText.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final IndexNode other = (IndexNode) obj;
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