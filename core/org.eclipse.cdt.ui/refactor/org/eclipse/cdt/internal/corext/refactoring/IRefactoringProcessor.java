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

import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;


public interface IRefactoringProcessor extends IAdaptable {
	
	public void initialize(Object[] elements) throws CoreException;
	
	public boolean isAvailable() throws CoreException;
	
	public String getProcessorName();
	
	public int getStyle();
	
//	public IProject[] getAffectedProjects() throws CoreException;
	
	public Object[] getElements();
	
	public Object[] getDerivedElements() throws CoreException;
	
//	public IResourceModifications getResourceModifications() throws CoreException;
	
	public RefactoringStatus checkActivation() throws CoreException;
	
	public RefactoringStatus checkInput(IProgressMonitor pm) throws CoreException;
	
	public IChange createChange(IProgressMonitor pm) throws CoreException;
}
