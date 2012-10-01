package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceStatusDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBTraceControl.ITraceTargetDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

/**
 * Command framework to select some trace record
 * subclasses must add implementation of doExecute(Object[] targets, IProgressMonitor monitor, IRequest request)
 * 
 * @since 2.1
 */
public abstract class GdbSelectTraceRecordCommand extends AbstractDebugCommand {
	
	protected final DsfExecutor fExecutor;
	protected final DsfServicesTracker fTracker;
	protected final DsfSession fSession;
	
	public GdbSelectTraceRecordCommand(DsfSession session) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
		fSession = session;
	}    

	public void dispose() {
		fTracker.dispose();
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
	throws CoreException 
	{
		if (targets.length != 1) {
			return false;
		}

		final ITraceTargetDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext)targets[0]).getDMContext(), ITraceTargetDMContext.class);
		if (dmc == null) {
			return false;
		}

        Query<Boolean> canSelectRecordQuery = new Query<Boolean>() {
        	@Override
        	public void execute(final DataRequestMonitor<Boolean> rm) {
        		IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);

        		if (traceControl != null) {
        			traceControl.getTraceStatus(dmc, new DataRequestMonitor<ITraceStatusDMData>(fExecutor, rm) {
						@Override
   						protected void handleSuccess() {
	        				if (getData().getNumberOfCollectedFrame() > 0) {
	        					IGDBTraceControl traceControl = fTracker.getService(IGDBTraceControl.class);
	        					if (traceControl != null) {
	        						traceControl.isTracing(dmc, new DataRequestMonitor<Boolean>(fExecutor, rm) {
	        							@Override
	        							protected void handleSuccess() {
	        								rm.setData(!getData());
	        								rm.done();
	        							};
	        						});
	        					} else {
	        						rm.setData(false);
	        						rm.done();
	        					}
	        				} else {
	        					rm.setData(false);
	        					rm.done();
	        				}
	        			};
        			});
        		} else {
        			rm.setData(false);
        			rm.done();
        		}
        	}
        };
		try {
			fExecutor.execute(canSelectRecordQuery);
			return canSelectRecordQuery.get();
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

	@Override
	protected boolean isRemainEnabled(IDebugCommandRequest request) {
		return true;
	}

}
