/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.output;


/**
 * GDB/MI shared information
 */
public class MISigHandle {

	String signal = ""; //$NON-NLS-1$
	boolean stop;
	boolean print;
	boolean pass;
	String description = ""; //$NON-NLS-1$

	public MISigHandle (String name, boolean stp, boolean prnt, boolean ps, String desc) {
		signal = name;
		stop = stp;
		print = prnt;
		pass = ps;
		description = desc;
	}

	public String getName() {
		return signal;
	}

	public boolean isStop() {
		return stop;
	}

	public boolean isPrint() {
		return print;
	}

	public boolean isPass() {
		return pass;
	}

	public void handle(boolean isIgnore, boolean isStop) {
		pass = !isIgnore;
		stop = isStop;
	}

	public String getDescription() {
		return description;
	}

}
