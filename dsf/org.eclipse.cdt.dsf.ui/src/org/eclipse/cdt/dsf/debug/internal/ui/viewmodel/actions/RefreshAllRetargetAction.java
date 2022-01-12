/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.actions;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.IRefreshAllTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;

/**
 *
 */
public class RefreshAllRetargetAction extends RetargetDebugContextAction {

	@Override
	protected boolean canPerformAction(Object target, ISelection selection) {
		return true;
	}

	@Override
	protected Class<?> getAdapterClass() {
		return IRefreshAllTarget.class;
	}

	@Override
	protected void performAction(Object target, ISelection debugContext) throws CoreException {
		((IRefreshAllTarget) target).refresh(debugContext);
	}

}
