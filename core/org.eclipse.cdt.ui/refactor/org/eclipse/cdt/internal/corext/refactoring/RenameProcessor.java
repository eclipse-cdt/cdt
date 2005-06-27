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
package org.eclipse.cdt.internal.corext.refactoring;

import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.core.runtime.CoreException;

public abstract class RenameProcessor implements IRenameProcessor {

	private int fStyle;
	protected String fNewElementName;
	
	public int getStyle() {
		return fStyle;
	}
	
	protected RenameProcessor() {
		fStyle= RefactoringStyles.NEEDS_PREVIEW;	
	}
	
	protected RenameProcessor(int style) {
		fStyle= style;	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRenameProcessor#setNewElementName(java.lang.String)
	 */
	public void setNewElementName(String newName) {
		Assert.isNotNull(newName);
		fNewElementName= newName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRenameProcessor#getNewElementName()
	 */
	public String getNewElementName() {
		return fNewElementName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.IRefactoringProcessor#getDerivedElements()
	 */
	public Object[] getDerivedElements() throws CoreException {
		return new Object[0];
	}
	
//	public void propagateDataTo(IRenameParticipant participant) throws CoreException {
//		participant.setNewElementName(fNewElementName);
//		if (this instanceof IReferenceUpdating) {
//			participant.setUpdateReferences(((IReferenceUpdating)this).getUpdateReferences());
//		}
//	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class clazz) {
		return null;
	}
}
