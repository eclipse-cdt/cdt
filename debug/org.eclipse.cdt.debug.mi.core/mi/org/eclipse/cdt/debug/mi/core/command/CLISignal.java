/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;



/**
 * 
 *    signal SIGUSR1
 *
 */
public class CLISignal extends CLICommand {

	MIOutput out;

	public CLISignal(String arg) {
		super("signal " + arg); //$NON-NLS-1$
	}

	/**
	 *  This is a CLI command contraly to
	 *  the -exec-continue or -exec-run
	 *  it does not return so we have to fake
	 *  a return value. We return "^running"
	 */
	@Override
	public MIOutput getMIOutput() {
		if (out == null) {
			out =  new MIOutput();
			MIResultRecord rr = new MIResultRecord();
			rr.setToken(getToken());
			rr.setResultClass(MIResultRecord.RUNNING);
			out.setMIResultRecord(rr);
		}
		return out;
	}

}
