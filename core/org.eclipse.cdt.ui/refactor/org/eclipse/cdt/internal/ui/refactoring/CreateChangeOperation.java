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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;

import org.eclipse.cdt.core.model.CModelException;

import org.eclipse.cdt.internal.corext.refactoring.base.IChange;
import org.eclipse.cdt.internal.corext.refactoring.base.Refactoring;
import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;

/**
 * Operation that, when performed, creates an <code>IChange</code> object for the refactoring
 * passed as a constructor parameter.
 */
public class CreateChangeOperation implements IRunnableWithProgress {

	private Refactoring fRefactoring;
	private int fStyle;
	private int fCheckPassedSeverity;
	private IChange fChange;
	private RefactoringStatus fStatus;
	
	public static final int CHECK_NONE=         CheckConditionsOperation.NONE;
	public static final int CHECK_ACTIVATION=   CheckConditionsOperation.ACTIVATION;
	public static final int CHECK_INPUT=        CheckConditionsOperation.INPUT;
	public static final int CHECK_PRECONDITION= CheckConditionsOperation.PRECONDITIONS;
	private static final int LAST=              CheckConditionsOperation.LAST; 
	
	/**
	 * Creates a new instance with the given refactoring.
	 *
	 * @param refactoring the refactoring. Parameter must not be <code>null</code>
	 * @param style style to define which conditions to check
	 */
	public CreateChangeOperation(Refactoring refactoring, int style) {
		Assert.isNotNull(refactoring);
		fRefactoring= refactoring;
		fStyle= style;
		Assert.isTrue(checkStyle(fStyle));
		fCheckPassedSeverity= RefactoringStatus.ERROR;
	}
	
	/**
	 * Creates a new instance with the given refactoring.
	 * 
	 * @param refactoring the refactoring. Parameter must not be <code>null</code>
	 * @param style style to define which conditions to check
	 * @param checkPassedSeverity the severity below which the check is considered
	 *  to be passed
	 * @see #setCheckPassedSeverity(int)
	 */
	public CreateChangeOperation(Refactoring refactoring, int style, int checkPassedSeverity) {
		Assert.isNotNull(refactoring);
		fRefactoring= refactoring;
		fStyle= style;
		Assert.isTrue(checkStyle(fStyle));
		setCheckPassedSeverity(checkPassedSeverity);
	}
	
	/**
	 * Sets the check passed severity value. This value is used to deceide whether the 
	 * condition check is interpreted as passed or not. The condition check is considered 
	 * to be passed if the refactoring status's severity is less or equal the given severity.
	 * The given value must be smaller than <code>RefactoringStatus.FATAL</code>.
	 */
	public void setCheckPassedSeverity(int severity) {
		fCheckPassedSeverity= severity;
		Assert.isTrue (fCheckPassedSeverity < RefactoringStatus.FATAL);
	}
	
	/* (Non=Javadoc)
	 * Method declared in IRunnableWithProgress
	 */
	public void run(IProgressMonitor pm) throws InvocationTargetException {
		fChange= null;
		fStatus= null;
		try {
			fChange= null;
			if (fStyle != CHECK_NONE) {
				pm.beginTask("", 5); //$NON-NLS-1$
				pm.subTask(""); //$NON-NLS-1$
				CheckConditionsOperation op= new CheckConditionsOperation(fRefactoring, fStyle);
				op.run(new SubProgressMonitor(pm, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
				fStatus= op.getStatus();
				if (fStatus != null && fStatus.getSeverity() <= fCheckPassedSeverity) {
					fChange= fRefactoring.createChange(
						new SubProgressMonitor(pm, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
				} else {
					pm.worked(1);
				}
			} else {
				fChange= fRefactoring.createChange(pm);
			}
		} catch (CModelException e) {
			throw new InvocationTargetException(e);
		} finally {
			pm.done();
		}
	}

	/**
	 * Returns the outcome of the operation or <code>null</code> if an exception 
	 * occured when performing the operation.
	 * 
	 * @return the outcome of the operation
	 */
	public IChange getChange() {
		return fChange;
	}
	
	/**
	 * Returns the status of the condition cheking. Returns <code>null</code> if an
	 * exception occured when performing the operation.
	 * 
	 * @return the condition checking's status
	 */
	public RefactoringStatus getStatus() {
		return fStatus;
	} 
	
	/**
	 * Returns the checking style.
	 * 
	 * @return the style used for precondition checking. Is one of <code>NONE</code>,
	 *  <code>ACTIVATION</code>, <code>INPUT</code>, or <code>PRECONDITION</code>.
	 */
	public int getConditionCheckingStyle() {
		return fStyle;
	}
	
	public void setConditionCheckingStyle(int style) {
		Assert.isTrue(checkStyle(style));
		fStyle= style;
	}
	
	private boolean checkStyle(int style) {
		return style < LAST;
	}
}
