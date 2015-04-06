/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command;

import java.io.OutputStream;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupExitedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

/**
 * Specialization for GDB >= 7.3.
 * 
 * @since 4.7
 */
public class MIInferiorProcess_7_3 extends MIInferiorProcess
{

	private DsfSession fSession;
	
    @ConfinedToDsfExecutor("fSession#getExecutor")
    public MIInferiorProcess_7_3(IContainerDMContext container, OutputStream gdbOutputStream) {
        this(container, gdbOutputStream, null);
    }
    
    @ConfinedToDsfExecutor("fSession#getExecutor")
    public MIInferiorProcess_7_3(IContainerDMContext container, PTY p) {
        this(container, null, p);
    }
    
    @ConfinedToDsfExecutor("fSession#getExecutor")
    protected MIInferiorProcess_7_3(IContainerDMContext container, final OutputStream gdbOutputStream, PTY pty) {
        super(container, gdbOutputStream, pty);
        fSession = DsfSession.getSession(container.getSessionId());
    }

    @ThreadSafeAndProhibitedFromDsfExecutor("fSession#getExecutor")
    @Override
    public int exitValue() {
    	assert !fSession.getExecutor().isInExecutorThread();

    	synchronized(this) {
    		if (fExitCode != null) {
    			return fExitCode;
    		}
    	}

    	if (!isTerminated()) {
    		// Throw an exception because the process is still running.
   			throw new IllegalThreadStateException();
    	}
    	
    	return 0;
    }

    /** @since 4.2 */
    @Override
	@DsfServiceEventHandler
    public void eventDispatched(MIThreadGroupExitedEvent e) {
		if (getContainer() instanceof IMIContainerDMContext) {
			if (((IMIContainerDMContext)getContainer()).getGroupId().equals(e.getGroupId())) {
    			if (isStarted()) {
    				// Only handle this event if this process was already
    				// started.  This is to protect ourselves in the case of
    				// a restart, where the new inferior is already created
    				// and gets the exited event for the old inferior.
    				String exitCode = e.getExitCode();
    				if (exitCode != null) {
    					setExitCodeAttribute();
    					try {
    						// Must use 'decode' since GDB returns an octal value
    						Integer decodedExitCode = Integer.decode(exitCode);
			        		synchronized(this) {
    			            	fExitCode = decodedExitCode;
    			            }
    					} catch (NumberFormatException exception) {
    					}    					
    				}
    			}
    		}
    	}
    }
    
	/**
	 * Set an attribute in the inferior process of the launch to indicate
	 * that the inferior has properly exited and its exit value can be used.
	 */
    @ConfinedToDsfExecutor("fSession#getExecutor")
	private void setExitCodeAttribute() {
		// Update the console label to contain the exit code
		ILaunch launch = (ILaunch)fSession.getModelAdapter(ILaunch.class);
		IProcess[] launchProcesses = launch.getProcesses();
		for (IProcess proc : launchProcesses) {
			if (proc instanceof InferiorRuntimeProcess) {
				String groupAttribute = proc.getAttribute(IGdbDebugConstants.INFERIOR_GROUPID_ATTR);

				if (getContainer() instanceof IMIContainerDMContext) {
					if (groupAttribute != null && groupAttribute.equals(((IMIContainerDMContext)getContainer()).getGroupId())) {
						// Simply set the attribute that indicates the inferior has properly exited and its
						// exit code can be used.
						proc.setAttribute(IGdbDebugConstants.INFERIOR_EXITED_ATTR, ""); //$NON-NLS-1$
						return;
					}
				}
			}
		}
	}
}
