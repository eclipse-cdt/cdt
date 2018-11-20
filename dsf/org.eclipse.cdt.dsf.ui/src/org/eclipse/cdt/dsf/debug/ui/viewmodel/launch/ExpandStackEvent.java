/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

/**
 * Event to increase the stack frame limit for an execution context.
 *
 * @since 1.1
 */
public class ExpandStackEvent extends AbstractDMEvent<IExecutionDMContext> {

	public ExpandStackEvent(IExecutionDMContext execCtx) {
		super(execCtx);
	}

}
