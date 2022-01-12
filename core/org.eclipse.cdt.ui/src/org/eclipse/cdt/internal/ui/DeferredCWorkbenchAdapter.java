/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

public class DeferredCWorkbenchAdapter extends CWorkbenchAdapter implements IDeferredWorkbenchAdapter {

	private ICElement fCElement;

	public DeferredCWorkbenchAdapter(ICElement element) {
		super();
		fCElement = element;
	}

	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		Object[] children = getChildren(object);
		if (monitor.isCanceled()) {
			return;
		}
		collector.add(children, monitor);
		collector.done();
	}

	@Override
	public boolean isContainer() {
		return fCElement instanceof IParent;
	}

	@Override
	public ISchedulingRule getRule(final Object object) {
		return fCElement.getResource();
	}
}
