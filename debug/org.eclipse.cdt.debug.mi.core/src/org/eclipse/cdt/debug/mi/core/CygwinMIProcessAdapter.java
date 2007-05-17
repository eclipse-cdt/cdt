/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Doug Schaefer
 */
public class CygwinMIProcessAdapter extends MIProcessAdapter {

	/**
	 * @param args
	 * @param launchTimeout
	 * @param monitor
	 * @throws IOException
	 */
	public CygwinMIProcessAdapter(String[] args, int launchTimeout,
			IProgressMonitor monitor) throws IOException {
		super(args, launchTimeout, monitor);
	}

	public void interrupt(MIInferior inferior) {
		// With cygwin gdb, interrupting gdb itself never works.
		// You need to interrupt the inferior directly.
		interruptInferior(inferior);
	}
	
}
