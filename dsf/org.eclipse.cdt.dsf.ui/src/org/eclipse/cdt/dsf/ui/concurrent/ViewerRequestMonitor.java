/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.ui.concurrent;

import java.util.concurrent.Executor;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/**
 * Data Request monitor that takes <code>IViewerUpdate</code> as a parent.
 * If the IViewerUpdate is canceled, this request monitor becomes canceled as well.
 * @see IViewerUpdate
 *
 * @since 2.0
 */
public class ViewerRequestMonitor extends RequestMonitor {

	private final IViewerUpdate fUpdate;

	public ViewerRequestMonitor(Executor executor, IViewerUpdate update) {
		super(executor, null);
		fUpdate = update;
	}

	@Override
	public synchronized boolean isCanceled() {
		return fUpdate.isCanceled() || super.isCanceled();
	}

	@Override
	protected void handleSuccess() {
		fUpdate.done();
	}

	@Override
	protected void handleErrorOrWarning() {
		fUpdate.setStatus(getStatus());
		fUpdate.done();
	}

	@Override
	protected void handleCancel() {
		fUpdate.setStatus(getStatus());
		fUpdate.done();
	}
}
