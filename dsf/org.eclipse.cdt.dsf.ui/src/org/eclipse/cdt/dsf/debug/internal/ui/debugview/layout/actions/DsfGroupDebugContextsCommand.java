/*******************************************************************************
 * Copyright (c) 2011, 2015 Texas Instruments, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.debug.core.commands.IDebugCommandHandler;

/**
 * @since 2.2
 */
public class DsfGroupDebugContextsCommand extends DsfDebugViewLayoutCommand implements IDebugCommandHandler {
	
	public DsfGroupDebugContextsCommand(DsfSession session) {
		super(session);
	}    

	@Override
	void executeOnDsfThread(IExecutionContextTranslator translator, IExecutionDMContext[] contexts, RequestMonitor rm) {
		translator.group(contexts, rm);
	}
	
	@Override
	void canExecuteOnDsfThread(IExecutionContextTranslator translator, IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm) {
		translator.canGroup(contexts, rm);
	}
}
