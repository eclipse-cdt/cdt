/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import javax.naming.OperationNotSupportedException;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * @since 2.4
 */
public abstract class AbstractRunControl extends AbstractDsfService implements
		IRunControl3 {

	public AbstractRunControl(DsfSession session) {
		super(session);
	}

	@Override
	public void canStepIntoSelection(IExecutionDMContext context, String sourceFile, Integer lineNumber, IFunctionDeclaration selectedFunction, DataRequestMonitor<Boolean> rm) {
		rm.done(false);
	}

	@Override
	public void stepIntoSelection(IExecutionDMContext context, String sourceFile, int lineNumber, boolean skipBreakpoints, IFunctionDeclaration selectedFunction, RequestMonitor rm) throws OperationNotSupportedException {
		throw new OperationNotSupportedException("Step Into Selection is not supported"); //$NON-NLS-1$
	}
}
