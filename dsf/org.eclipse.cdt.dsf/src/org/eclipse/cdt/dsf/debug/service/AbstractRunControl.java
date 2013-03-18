/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * @author Alvaro Sanchez-Leon
 * @since 2.4
 */
public abstract class AbstractRunControl extends AbstractDsfService implements
		IRunControl3 {

	public AbstractRunControl(DsfSession session) {
		super(session);
	}

	@Override
	public void canStepIntoSelection(IExecutionDMContext context, DataRequestMonitor<Boolean> rm) {
		canStep(context, StepType.STEP_INTO, rm);
	}

	@Override
	public void stepIntoSelection(IExecutionDMContext context, String sourceFile, int lineNumber, boolean skipBreakpoints, RequestMonitor rm, IFunctionDeclaration selectedFunction) {
		return;
	}
}
