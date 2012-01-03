/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlInitializedDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IOConsole;

/**
 * A console that is used to print traces.  The console is attached to a launch.
 *
 * Any input to this console is read and discarded, since this console should be
 * read-only.  We don't actually make the console read-only because it is nice
 * for the user to be able to add delimiters such as empty lines within the traces.
 * 
 * @since 2.1
 * This class was moved from package org.eclipse.cdt.dsf.gdb.internal.ui.tracing
 */
public class TracingConsole extends IOConsole {
	private ILaunch fLaunch;
	private OutputStream fTracingStream = null;
	private DsfSession fSession = null;
	private String fLabel = ""; //$NON-NLS-1$

	public TracingConsole(ILaunch launch, String label) {
		super("", null, null, true); //$NON-NLS-1$
		fLaunch = launch;
        fTracingStream = newOutputStream();
        fSession = ((GdbLaunch)launch).getSession();
        fLabel = label;

		resetName();

        // Start a job to swallow all the input from the user
		new InputReadJob().schedule();

        // This is needed if the service has already been created.
        // For example, if we turn on tracing after a launch is started.
        setStreamInService();
	}
	
    @Override
	protected void init() {
        super.init();
        fSession.getExecutor().submit(new DsfRunnable() {
            @Override
        	public void run() {
        		fSession.addServiceEventListener(TracingConsole.this, null);
        	}
        });
    }
    
	@Override
	protected void dispose() {
        try {
			fTracingStream.close();
		} catch (IOException e) {
		}
        try {
        	fSession.getExecutor().submit(new DsfRunnable() {
                @Override
        		public void run() {
        			fSession.removeServiceEventListener(TracingConsole.this);
        		}
        	});
		} catch (RejectedExecutionException e) {
			// Session already disposed
		}
		super.dispose();
	}
	
	public ILaunch getLaunch() { return fLaunch; }
    
    private void setStreamInService() {
    	try {
    		fSession.getExecutor().submit(new DsfRunnable() {
                @Override
    			public void run() {
    				DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
    				IGDBControl control = tracker.getService(IGDBControl.class);
    				tracker.dispose();
    				if (control != null) {
    					// Special method that need not be called on the executor
    					control.setTracingStream(fTracingStream);
    				}
    			}
    		});
	    } catch (RejectedExecutionException e) {
	    }
	}
	
    protected String computeName() {
        String label = fLabel;

        ILaunchConfiguration config = fLaunch.getLaunchConfiguration();
        if (config != null && !DebugUITools.isPrivate(config)) {
        	String type = null;
        	try {
        		type = config.getType().getName();
        	} catch (CoreException e) {
        	}
        	StringBuffer buffer = new StringBuffer();
        	buffer.append(config.getName());
        	if (type != null) {
        		buffer.append(" ["); //$NON-NLS-1$
        		buffer.append(type);
        		buffer.append("] "); //$NON-NLS-1$
        	}
        	buffer.append(label);
        	label = buffer.toString();
        }

        if (fLaunch.isTerminated()) {
        	return ConsoleMessages.ConsoleMessages_trace_console_terminated + label; 
        }
        
        return label;
    }
    
    public void resetName() {
    	final String newName = computeName();
    	String name = getName();
    	if (!name.equals(newName)) {
    		Runnable r = new Runnable() {
                @Override
    			public void run() {
    				setName(newName);
    			}
    		};
    		PlatformUI.getWorkbench().getDisplay().asyncExec(r);
    	}
    }
    
    @DsfServiceEventHandler
    public final void eventDispatched(ICommandControlInitializedDMEvent event) {
    	// Now that the service is started, we can set the stream.
    	// We won't receive this event if we enable tracing after a launch
    	// has been started.
    	setStreamInService();
    }

	/**
	 * A reading Job which will prevent the input stream
	 * from filling up.  We don't actually do anything with
	 * the data we read, since the Trace console should not
	 * accept input.
	 * 
	 * But instead of making the console read-only, we allow
	 * the user to type things to allow for comments to be
	 * inserted within the traces.
	 */
    private class InputReadJob extends Job {
    	{
    		setSystem(true); 
    	}
    	
        InputReadJob() {
            super("Traces Input Job"); //$NON-NLS-1$
        }

        @Override
		protected IStatus run(IProgressMonitor monitor) {
            try {
                byte[] b = new byte[1024];
                int read = 0;
                while (getInputStream() != null && read >= 0) {
                	// Read the input and swallow it.
                	read = getInputStream().read(b);
                }
            } catch (IOException e) {
            }
            return Status.OK_STATUS;
        }
    }
}
