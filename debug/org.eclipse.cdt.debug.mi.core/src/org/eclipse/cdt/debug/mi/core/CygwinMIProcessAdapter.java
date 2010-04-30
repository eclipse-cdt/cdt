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

import org.eclipse.cdt.utils.spawner.Spawner;
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
		if (fGDBProcess instanceof Spawner) {
			if (inferior.isRunning()) {
				boolean interruptedInferior = false;
				Spawner gdbSpawner = (Spawner) fGDBProcess;
				
				// Cygwin gdb 6.8 is capricious when it comes to interrupting
				// the target. MinGW and later versions of Cygwin aren't. A
				// simple CTRL-C to gdb seems to do the trick in every case.
				// Once we drop support for gdb 6.8, we should be able to ditch
				// this method and rely on the base implementation
				// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=304096#c56
				if (inferior.isRemoteInferior()) {
					// Interrupt gdb with a 'kill -SIGINT'. The reason we
					// need to send a simulated Cygwin/POSIX SIGINT to
					// Cygwin gdb is that it has special handling in the case
					// of remote debugging, as explained in the bugzilla 
					// comment above. That special handling will forward the
					// interrupt request through gdbserver to the remote
					// inferior, but the interrupt to gdb *must* be a
					// simulated Cygwin/POSIX SIGINT; a CTRL-C won't do.
					gdbSpawner.interrupt();		
				}
				else if (inferior.isAttachedInferior()) {
					// Cygwin gdb 6.8 has no support for forwarding an
					// interrupt request to the local process it has
					// attached to. That support has since been added and
					// will be available in 7.x. So, the only way to suspend the
					// attached-to inferior is to interrupt it directly.
					// The following call will take a special path in the
					// JNI code. See
					// Java_org_eclipse_cdt_utils_spawner_Spawner_raise()
					// We don't use the Cygwin 'kill' command since (a) we don't
					// know if the process associated with PID (the inferior) is
					// a cygwin one (kill only works on cygwin programs), and
					// (b) a CTRL-C will work just fine whether it's a cygwin
					// program or not
					interruptInferior(inferior);
					interruptedInferior = true;
				}
				else {
					// The typical case--gdb launches the inferior.
					// Interrupt gdb but with a CTRL-C. gdb (6.8) itself
					// doesn't have a handler for CTRL-C, but all processes
					// in the console
					// process group will receive the CTRL-C, and gdb
					// registers itself to catch any such events that
					// happen in the inferior. Thus it is able to determine
					// and report that the inferior has been interrupted.
					// But it's important we don't interrupt Cygwin gdb with
					// a 'kill' since that will only reach gdb, and gdb
					// won't forward the request on to the inferior. See
					// bugzilla comment referenced above for details.
					gdbSpawner.interruptCTRLC(); 
				}
				
				waitForInterrupt(inferior);

				// If we are still running try to interrupt the inferior (unless we
				// already tried that above)
				if (inferior.isRunning() && inferior.getInferiorPID() > 0 && !interruptedInferior) {
					// lets try something else.
					interruptInferior(inferior);
				}
			}
		}
			
	}
	
}
