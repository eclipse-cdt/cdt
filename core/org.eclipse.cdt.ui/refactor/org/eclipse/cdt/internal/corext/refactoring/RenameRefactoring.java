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


import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.Refactoring;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.cdt.internal.corext.refactoring.rename.RenameElementProcessor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

public class RenameRefactoring extends Refactoring implements IProcessorBasedRefactoring, IRenameRefactoring {

	private Object fElement;
	private IRenameProcessor fProcessor;
	
	public RenameRefactoring(Object element) throws CoreException {
		Assert.isNotNull(element);
		
		fElement= element;
		fProcessor = new RenameElementProcessor();
		fProcessor.initialize(new Object[] {fElement});
	}
	
	public boolean isAvailable() {
		return fProcessor != null;
	}
		
	public Object getAdapter(Class clazz) {
		if (clazz.isInstance(fProcessor))
			return fProcessor;
		return super.getAdapter(clazz);
	}
	
	public IRefactoringProcessor getProcessor() {
		return fProcessor;
	}
	
	public int getStyle() {
		return fProcessor.getStyle();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.tagging.IRenameRefactoring#getNewName()
	 */
	public String getNewName() {
		return fProcessor.getNewElementName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.tagging.IRenameRefactoring#setNewName(java.lang.String)
	 */
	public void setNewName(String newName) {
		fProcessor.setNewElementName(newName);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.tagging.IRenameRefactoring#getCurrentName()
	 */
	public String getCurrentName() {
		return fProcessor.getCurrentElementName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.tagging.IRenameRefactoring#checkNewName(java.lang.String)
	 */
	public RefactoringStatus checkNewName(String newName) throws CModelException {
		RefactoringStatus result= new RefactoringStatus();
		try {
			result.merge(fProcessor.checkNewElementName(newName));
		} catch (CoreException e) {
			throw new CModelException(e);
		}
		return result;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IRefactoring#getName()
	 */
	public String getName() {
		return fProcessor.getProcessorName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Refactoring#checkActivation(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus checkActivation(IProgressMonitor pm) throws CModelException {
		RefactoringStatus result= new RefactoringStatus();
		try {
			result.merge(fProcessor.checkActivation());
		} catch (CoreException e) {
			throw new CModelException(e);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.Refactoring#checkInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public RefactoringStatus checkInput(IProgressMonitor pm) throws CModelException {
		RefactoringStatus result= new RefactoringStatus();
		try {
			//initParticipants();
			pm.beginTask("", 2); //$NON-NLS-1$
			
			result.merge(fProcessor.checkInput(new SubProgressMonitor(pm, 1)));
			if (result.hasFatalError())
				return result;
							
		} catch (CModelException e) {
			throw e;
		} catch (CoreException e) {
			throw new CModelException(e);
		}
		return result;		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.base.IRefactoring#createChange(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IChange createChange(IProgressMonitor pm) throws CModelException {
		pm.beginTask("", 1); //$NON-NLS-1$
		CompositeChange result= new CompositeChange();
		try {
			result.add(fProcessor.createChange(new SubProgressMonitor(pm, 1)));			
		} catch (CModelException e) {
			throw e;
		} catch (CoreException e) {
			throw new CModelException(e);
		}
		return result;
	}
	
	/* non java-doc
	 * for debugging only
	 */
	public String toString() {
		if (isAvailable())
			return getName();
		else
			return "No refactoring available to process: " + fElement; //$NON-NLS-1$
	}
}
