/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command;

import java.io.OutputStream;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.utils.pty.PTY;

/**
 * This class extends the behavior of its base to use MIInferiorProcess_7_3
 * which is needed to handle the inferior exit code.
 * 
 * @since 5.1
 */
public class MIInferiorEventProcessor_7_3 extends MIInferiorEventProcessor {
    
    public MIInferiorEventProcessor_7_3(ICommandControlService controlService) {
    	super(controlService);
    }
    
	@Override
	protected MIInferiorProcess createInferiorProcess(IContainerDMContext container, OutputStream outputStream) {
		return new MIInferiorProcess_7_3(container, outputStream);
	}

	@Override
	protected MIInferiorProcess createInferiorProcess(IContainerDMContext container, PTY pty) {
		return new MIInferiorProcess_7_3(container, pty);
	}
}
