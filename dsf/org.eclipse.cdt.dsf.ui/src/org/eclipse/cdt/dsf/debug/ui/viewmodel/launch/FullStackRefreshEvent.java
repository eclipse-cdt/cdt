/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

/**
 * Indicates the end of a sequence of steps. Should be handled like a suspended
 * event to trigger a full refresh of stack frames.
 *
 * @since 1.1
 */
public class FullStackRefreshEvent extends AbstractDMEvent<IExecutionDMContext> {

	private final IDMEvent<? extends IDMContext> fTriggeringEvent;

	public FullStackRefreshEvent(IExecutionDMContext execCtx) {
		this(execCtx, null);
	}

	public FullStackRefreshEvent(IExecutionDMContext execCtx, IDMEvent<? extends IDMContext> triggeringEvent) {
		super(execCtx);
		fTriggeringEvent = triggeringEvent;
	}

	public IDMEvent<? extends IDMContext> getTriggeringEvent() {
		return fTriggeringEvent;
	}
}
