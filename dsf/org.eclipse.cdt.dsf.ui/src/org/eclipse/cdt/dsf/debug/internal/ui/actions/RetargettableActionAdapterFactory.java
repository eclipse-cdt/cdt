/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.actions.IMoveToLineTarget;
import org.eclipse.cdt.debug.internal.ui.actions.IResumeAtLineTarget;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IRunToLineTarget;

/**
 * Retargettable Action Adapter Factory for the DSF Disassembly view
 * 
 * @since 2.1
 */
public class RetargettableActionAdapterFactory implements IAdapterFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IRunToLineTarget.class) {
			return new DisassemblyRunToLineAdapter();
		} 
		if (adapterType == IMoveToLineTarget.class) {
			return new DisassemblyMoveToLineAdapter();
		} 
		if (adapterType == IResumeAtLineTarget.class) {
			return new DisassemblyResumeAtLineAdapter();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[]{ IRunToLineTarget.class, IResumeAtLineTarget.class, IMoveToLineTarget.class };
	}
}
