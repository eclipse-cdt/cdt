/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson and others.
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
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType == IRunToLineTarget.class) {
			return (T)new DisassemblyRunToLineAdapter();
		} 
		if (adapterType == IMoveToLineTarget.class) {
			return (T)new DisassemblyMoveToLineAdapter();
		} 
		if (adapterType == IResumeAtLineTarget.class) {
			return (T)new DisassemblyResumeAtLineAdapter();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class<?>[] getAdapterList() {
		return new Class[]{ IRunToLineTarget.class, IResumeAtLineTarget.class, IMoveToLineTarget.class };
	}
}
