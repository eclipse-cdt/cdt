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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.changes.TextChange;
import org.eclipse.cdt.internal.corext.refactoring.changes.TextChange.EditChange;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.Assert;

/* package */ class PseudoCChangeElement extends ChangeElement {

	private ICElement fCElement;
	private List fChildren;

	public PseudoCChangeElement(ChangeElement parent, ICElement element) {
		super(parent);
		fCElement= element;
		Assert.isNotNull(fCElement);
	}
	
	/**
	 * Returns the C element.
	 * 
	 * @return the C element managed by this node
	 */
	public ICElement getCElement() {
		return fCElement;
	}

	/* (non-Cdoc)
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
				List edits= collectTextEditChanges();
				viewer.setInput(TextChangePreviewViewer.createInput(
					(EditChange[])edits.toArray(new EditChange[edits.size()]),
					getTextRange()));
			}
		} else {
			viewer.setInput(null);
		}
	}
	
	/* non Java-doc
	 * @see ChangeElement#setActive
	 */
	public void setActive(boolean active) {
		for (Iterator iter= fChildren.iterator(); iter.hasNext();) {
			ChangeElement element= (ChangeElement)iter.next();
			element.setActive(active);
		}
	}
	
	/* non Java-doc
	 * @see ChangeElement.getActive
	 */
	public int getActive() {
		Assert.isTrue(fChildren.size() > 0);
		int result= ((ChangeElement)fChildren.get(0)).getActive();
		for (int i= 1; i < fChildren.size(); i++) {
			ChangeElement element= (ChangeElement)fChildren.get(i);
			result= ACTIVATION_TABLE[element.getActive()][result];
			if (result == PARTLY_ACTIVE)
				break;
		}
		return result;
	}
	
	/* non Java-doc
	 * @see ChangeElement.getChildren
	 */
	public ChangeElement[] getChildren() {
		if (fChildren == null)
			return EMPTY_CHILDREN;
		return (ChangeElement[]) fChildren.toArray(new ChangeElement[fChildren.size()]);
	}
	
	/**
	 * Adds the given <code>TextEditChangeElement<code> as a child to this 
	 * <code>PseudoCChangeElement</code>
	 * 
	 * @param child the child to be added
	 */
	public void addChild(TextEditChangeElement child) {
		doAddChild(child);
	}
	
	/**
	 * Adds the given <code>PseudoCChangeElement<code> as a child to this 
	 * <code>PseudoCChangeElement</code>
	 * 
	 * @param child the child to be added
	 */
	public void addChild(PseudoCChangeElement child) {
		doAddChild(child);
	}
	
	private void doAddChild(ChangeElement child) {
		if (fChildren == null)
			fChildren= new ArrayList(2);
		fChildren.add(child);
	}
	
	private DefaultChangeElement getStandardChangeElement() {
		ChangeElement element= getParent();
		while(!(element instanceof DefaultChangeElement) && element != null) {
			element= element.getParent();
		}
		return (DefaultChangeElement)element;
	}
	
	private List collectTextEditChanges() {
		List result= new ArrayList(10);
		ChangeElement[] children= getChildren();
		for (int i= 0; i < children.length; i++) {
			ChangeElement child= children[i];
			if (child instanceof TextEditChangeElement) {
				result.add(((TextEditChangeElement)child).getTextEditChange());
			} else if (child instanceof PseudoCChangeElement) {
				result.addAll(((PseudoCChangeElement)child).collectTextEditChanges());
			}
		}
		return result;
	}
	
	public IRegion getTextRange() throws CoreException {
		ISourceRange range= ((ISourceReference)fCElement).getSourceRange();
		return new Region(range.getStartPos(), range.getLength());
	}	
}

