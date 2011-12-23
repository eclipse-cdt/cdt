/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.model.ISaveTraceDataHandler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Command to save the trace data to file
 * 
 * @since 2.1
 */
public class GdbSaveTraceDataCommand extends AbstractDebugCommand implements ISaveTraceDataHandler {
	
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public GdbSaveTraceDataCommand(DsfSession session) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
	}    

	public void dispose() {
		fTracker.dispose();
	}

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) 
	throws CoreException {
		if (targets.length != 1) {
			return;
		}

		final ITraceTargetDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext)targets[0]).getDMContext(), ITraceTargetDMContext.class);
		if (dmc == null) {
			return;
		}

		final String[] fileName = new String[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            @Override
			public void run() {
				fileName[0] = promptForFileName();
			};
		});
		
		if (fileName[0] != null) {
			Query<Object> saveTraceDataQuery = new Query<Object>() {
				@Override
				public void execute(final DataRequestMonitor<Object> rm) {
					IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);

					if (traceControl != null) {
						traceControl.saveTraceData(dmc, fileName[0], false, rm);
					} else {
						rm.done();
					}
				}
			};
			try {
				fExecutor.execute(saveTraceDataQuery);
				saveTraceDataQuery.get();
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			} catch (RejectedExecutionException e) {
				// Can be thrown if the session is shutdown
			}
		}
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
	throws CoreException {
		if (targets.length != 1) {
			return false;
		}

		final ITraceTargetDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext)targets[0]).getDMContext(), ITraceTargetDMContext.class);
		if (dmc == null) {
			return false;
		}

        Query<Boolean> canSaveQuery = new Query<Boolean>() {
        	@Override
        	public void execute(DataRequestMonitor<Boolean> rm) {
        		IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);

        		if (traceControl != null) {
        			traceControl.canSaveTraceData(dmc, rm);
        		} else {
        			rm.setData(false);
        			rm.done();
        		}
        	}
        };
		try {
			fExecutor.execute(canSaveQuery);
			return canSaveQuery.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (RejectedExecutionException e) {
			// Can be thrown if the session is shutdown
		}

		return false;
	}

	@Override
	protected Object getTarget(Object element) {
		if (element instanceof IDMVMContext) {
			return element;
		}
		return null;
	}

	/*
	 * Keep the command enabled since there is no automatic re-selection of the debug
	 * context for this command.  If not, it will remain disabled until something causes
	 * the debug context to change.
	 * 
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.commands.AbstractDebugCommand#isRemainEnabled(org.eclipse.debug.core.commands.IDebugCommandRequest)
	 */
	@Override
	protected boolean isRemainEnabled(IDebugCommandRequest request) {
		return true;
	}
	
	private String promptForFileName() {
		Shell shell = Display.getDefault().getActiveShell();		
		if (shell == null) {
			return null;
		}

		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		return fd.open();
	}
}
