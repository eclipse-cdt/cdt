/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CFormattedMemoryBlock;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IProcess;

/**
 * 
 * Provides utility methods for creating debug sessions, targets and breakpoints specific to the CDI debug model.
 */
public class CDebugModel {

	/**
	 * Constructor for CDebugModel.
	 */
	public CDebugModel() {
		super();
	}

	/**
	 * Returns the identifier for the CDI debug model plug-in
	 * 
	 * @return plugin identifier
	 */
	public static String getPluginIdentifier() {
		return CDebugCorePlugin.getUniqueIdentifier();
	}

	/**
	 * @throws CoreException
	 * @deprecated
	 * Use {@link CDIDebugModel#newDebugTarget(ILaunch, ICDITarget, String, IProcess, IProcess, IFile, boolean, boolean, boolean)}. 
	 */
	public static IDebugTarget newDebugTarget( final ILaunch launch, final ICDITarget cdiTarget, final String name, final IProcess debuggeeProcess, final IProcess debuggerProcess, final IFile file, final boolean allowTerminate, final boolean allowDisconnect, final boolean stopInMain ) throws CoreException {
		return CDIDebugModel.newDebugTarget( launch, cdiTarget, name, debuggeeProcess, debuggerProcess, file, allowTerminate, allowDisconnect, stopInMain );
	}

	/**
	 * @throws CoreException
	 * @deprecated
	 * Use {@link CDIDebugModel#newAttachDebugTarget(ILaunch, ICDITarget, String, IProcess, IFile)}. 
	 */
	public static IDebugTarget newAttachDebugTarget( final ILaunch launch, final ICDITarget cdiTarget, final String name, final IProcess debuggerProcess, final IFile file ) throws CoreException {
		return CDIDebugModel.newAttachDebugTarget( launch, cdiTarget, name, debuggerProcess, file );
	}

	/**
	 * @throws CoreException
	 * @deprecated
	 * Use {@link CDIDebugModel#newCoreFileDebugTarget(ILaunch, ICDITarget, String, IProcess, IFile)}. 
	 */
	public static IDebugTarget newCoreFileDebugTarget( final ILaunch launch, final ICDITarget cdiTarget, final String name, final IProcess debuggerProcess, final IFile file ) throws CoreException {
		return CDIDebugModel.newCoreFileDebugTarget( launch, cdiTarget, name, debuggerProcess, file );
	}

	public static IFormattedMemoryBlock createFormattedMemoryBlock( IDebugTarget target, String addressExpression, int format, int wordSize, int numberOfRows, int numberOfColumns, char paddingChar ) throws DebugException {
		if ( target != null && target instanceof CDebugTarget ) {
			try {
				ICDIExpression expression = ((CDebugTarget)target).getCDITarget().createExpression( addressExpression );
				ICDIMemoryBlock cdiMemoryBlock = ((CDebugTarget)target).getCDITarget().createMemoryBlock( expression.getExpressionText(), wordSize * numberOfRows * numberOfColumns );
				return new CFormattedMemoryBlock( (CDebugTarget)target, cdiMemoryBlock, expression, format, wordSize, numberOfRows, numberOfColumns, paddingChar );
			}
			catch( CDIException e ) {
				throw new DebugException( new Status( IStatus.ERROR, getPluginIdentifier(), DebugException.TARGET_REQUEST_FAILED, e.getDetailMessage(), null ) );
			}
		}
		return null;
	}

	public static IFormattedMemoryBlock createFormattedMemoryBlock( IDebugTarget target, String addressExpression, int format, int wordSize, int numberOfRows, int numberOfColumns ) throws DebugException {
		if ( target != null && target instanceof CDebugTarget ) {
			try {
				ICDIExpression expression = ((CDebugTarget)target).getCDITarget().createExpression( addressExpression );
				ICDIMemoryBlock cdiMemoryBlock = ((CDebugTarget)target).getCDITarget().createMemoryBlock( expression.getExpressionText(), wordSize * numberOfRows * numberOfColumns );
				return new CFormattedMemoryBlock( (CDebugTarget)target, cdiMemoryBlock, expression, format, wordSize, numberOfRows, numberOfColumns );
			}
			catch( CDIException e ) {
				throw new DebugException( new Status( IStatus.ERROR, getPluginIdentifier(), DebugException.TARGET_REQUEST_FAILED, e.getDetailMessage(), null ) );
			}
		}
		return null;
	}
}