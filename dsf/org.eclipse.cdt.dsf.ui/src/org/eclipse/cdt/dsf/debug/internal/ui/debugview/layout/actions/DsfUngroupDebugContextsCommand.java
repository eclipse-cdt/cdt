/*******************************************************************************
 * Copyright (c) 2011, 2015 Texas Instruments, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dobrin Alexiev (Texas Instruments) - initial API and implementation (bug 336876)
********************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.debugview.layout.actions;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.internal.provisional.service.IExecutionContextTranslator;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * @since 2.2
 */
public class DsfUngroupDebugContextsCommand extends DsfDebugViewLayoutCommand {

	public DsfUngroupDebugContextsCommand(DsfSession session) {
		super(session);
	}

	@Override
	void executeOnDsfThread(IExecutionContextTranslator translator, IExecutionDMContext[] contexts, RequestMonitor rm) {
		translator.ungroup(contexts, rm);
	}

	@Override
	void canExecuteOnDsfThread(IExecutionContextTranslator translator, IExecutionDMContext[] contexts,
			DataRequestMonitor<Boolean> rm) {
		translator.canUngroup(contexts, rm);
	}
}
