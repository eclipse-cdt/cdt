/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.ICompositeChange;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.Assert;
//import org.eclipse.jdt.internal.corext.refactoring.changes.CompilationUnitChange;
//import org.eclipse.jdt.internal.corext.refactoring.changes.TextChange;

class DefaultChangeElement extends ChangeElement {
	
	private IChange fChange;
	private ChangeElement[] fChildren;

	/**
	 * Creates a new <code>ChangeElement</code> for the given
	 * change.
	 * 
	 * @param parent the change element's parent or <code>null
	 * 	</code> if the change element doesn't have a parent
	 * @param change the actual change. Argument must not be
	 * 	<code>null</code>
	 */
	public DefaultChangeElement(ChangeElement parent, IChange change) {
		super(parent);
		fChange= change;
		Assert.isNotNull(fChange);
	}

	/**
	 * Returns the underlying <code>IChange</code> object.
	 * 
	 * @return the underlying change
	 */
	public IChange getChange() {
		return fChange;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.ChangeElement#getChangePreviewViewer()
	 */
//	public ChangePreviewViewerDescriptor getChangePreviewViewer() throws CoreException {
//		return ChangePreviewViewerDescriptor.get(fChange);
//	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.ChangeElement#feedInput(org.eclipse.jdt.internal.ui.refactoring.IChangePreviewViewer)
	 */
	public void feedInput(IChangePreviewViewer viewer) throws CoreException {
		viewer.setInput(fChange);
	}
	
	/* non Java-doc
	 * @see ChangeElement#setActive
	 */
	public void setActive(boolean active) {
		fChange.setActive(active);
	}
	
	/* non Java-doc
	 * @see ChangeElement.getActive
	 */
	public int getActive() {
		if (fChange instanceof ICompositeChange /*|| fChange instanceof CompilationUnitChange || fChange instanceof TextChange*/)
			return getCompositeChangeActive();
		else
			return getDefaultChangeActive();
	}
	
	/* non Java-doc
	 * @see ChangeElement.getChildren
	 */	
	public ChangeElement[] getChildren() {
		return fChildren;
	}
	
	/**
	 * Sets the children.
	 * 
	 * @param the children of this node. Must not be <code>null</code>
	 */
	public void setChildren(ChangeElement[] children) {
		Assert.isNotNull(children);
		fChildren= children;
	}

	private int getDefaultChangeActive() {
		int result= fChange.isActive() ? ACTIVE : INACTIVE;
		if (fChildren != null) {
			for (int i= 0; i < fChildren.length; i++) {
				result= ACTIVATION_TABLE[fChildren[i].getActive()][result];
				if (result == PARTLY_ACTIVE)
					break;
			}
		}
		return result;
	}
	
	private int getCompositeChangeActive() {		
		if (fChildren != null && fChildren.length > 0) {
			int result= fChildren[0].getActive();
			for (int i= 1; i < fChildren.length; i++) {
				result= ACTIVATION_TABLE[fChildren[i].getActive()][result];
				if (result == PARTLY_ACTIVE)
					break;
			}
			return result;
		} else {
			return ACTIVE;
		}
	}
}

