/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
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
package org.eclipse.cdt.internal.qt.ui.resources;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;

public class QtWorkspaceSaveParticipant implements ISaveParticipant {

	@Override
	public void doneSaving(ISaveContext context) {
		// Nothing to do
	}

	@Override
	public void prepareToSave(ISaveContext context) throws CoreException {
		// Nothing to do
	}

	@Override
	public void rollback(ISaveContext context) {
		// Nothing to do
	}

	@Override
	public void saving(ISaveContext context) throws CoreException {
		context.needDelta();
	}

}
