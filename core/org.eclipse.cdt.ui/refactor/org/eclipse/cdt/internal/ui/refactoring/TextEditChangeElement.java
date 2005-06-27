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

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.Assert;

import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.changes.TextChange;
import org.eclipse.cdt.internal.corext.refactoring.changes.TextChange.EditChange;

/* package */ class TextEditChangeElement extends ChangeElement {
	
	private static final ChangeElement[] fgChildren= new ChangeElement[0];
	
	private EditChange fChange;
	
	public TextEditChangeElement(ChangeElement parent, EditChange change) {
		super(parent);
		fChange= change;
		Assert.isNotNull(fChange);
	}
	
	/**
	 * Returns the <code>TextEditChange</code> managed by this node.
	 * 
	 * @return the <code>TextEditChange</code>
	 */
	public EditChange getTextEditChange() {
		return fChange;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.ChangeElement#getChangePreviewViewer()
	 */
//	public ChangePreviewViewerDescriptor getChangePreviewViewer() throws CoreException {
//		DefaultChangeElement element= getStandardChangeElement();
//		if (element == null)
//			return null;
//		return element.getChangePreviewViewer();
//	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.refactoring.ChangeElement#feedInput(org.eclipse.jdt.internal.ui.refactoring.IChangePreviewViewer)
	 */
	public void feedInput(IChangePreviewViewer viewer) throws CoreException {
		DefaultChangeElement element= getStandardChangeElement();
		if (element != null) {
			IChange change= element.getChange();
			if (change instanceof TextChange) {
				IRegion range= getTextRange(this);
				Object input= null;
				if (range != null) {
					input= TextChangePreviewViewer.createInput(new EditChange[] {fChange}, range);
				} else {
					input= TextChangePreviewViewer.createInput(fChange, 2);
				}
				viewer.setInput(input);
			}
		} else {
			viewer.setInput(null);
		}
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
		return fChange.isActive() ? ACTIVE : INACTIVE;
	}
	
	/* non Java-doc
	 * @see ChangeElement.getChildren
	 */
	public ChangeElement[] getChildren() {
		return fgChildren;
	}
	
	private DefaultChangeElement getStandardChangeElement() {
		ChangeElement element= getParent();
		while(!(element instanceof DefaultChangeElement) && element != null) {
			element= element.getParent();
		}
		return (DefaultChangeElement)element;
	}
	
	private static IRegion getTextRange(ChangeElement element) throws CoreException {
		if (element == null)
			return null;
		if (element instanceof PseudoCChangeElement) {
			return ((PseudoCChangeElement)element).getTextRange();
		} else 
			if (element instanceof DefaultChangeElement) {
			return null;
		}
		return getTextRange(element.getParent());
	}
}

