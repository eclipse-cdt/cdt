/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType == IRunToLineTarget.class) {
			return (T) new RunToLineAdapter();
		}
		if (adapterType == IResumeAtLineTarget.class) {
			return (T) new ResumeAtLineAdapter();
		}
		if (adapterType == IMoveToLineTarget.class) {
			return (T) new MoveToLineAdapter();
		}
		if (adapterType == IResumeWithoutSignalHandler.class) {
			return (T) new ResumeWithoutSignalCommand();
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IRunToLineTarget.class, IResumeAtLineTarget.class, IMoveToLineTarget.class,
				IResumeWithoutSignalHandler.class };
	}
}
