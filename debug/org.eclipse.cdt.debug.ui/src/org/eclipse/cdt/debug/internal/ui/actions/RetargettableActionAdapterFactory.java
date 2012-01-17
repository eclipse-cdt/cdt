/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IResumeWithoutSignalHandler;
import org.eclipse.cdt.debug.internal.ui.commands.ResumeWithoutSignalCommand;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IRunToLineTarget;

/**
 * Creates adapters for retargettable actions in debug platform.
 * Contributed via <code>org.eclipse.core.runtime.adapters</code> 
 * extension point. 
 */
public class RetargettableActionAdapterFactory implements IAdapterFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter( Object adaptableObject, Class adapterType ) {
		if ( adapterType == IRunToLineTarget.class ) {
			return new RunToLineAdapter();
		} 
		if ( adapterType == IResumeAtLineTarget.class ) {
			return new ResumeAtLineAdapter();
		}
		if ( adapterType == IMoveToLineTarget.class ) {
			return new MoveToLineAdapter();
		} 		
		if ( adapterType == IResumeWithoutSignalHandler.class ) {
			return new ResumeWithoutSignalCommand();
		} 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[]{ IRunToLineTarget.class, 
				            IResumeAtLineTarget.class, 
				            IMoveToLineTarget.class,
				            IResumeWithoutSignalHandler.class };
	}
}
