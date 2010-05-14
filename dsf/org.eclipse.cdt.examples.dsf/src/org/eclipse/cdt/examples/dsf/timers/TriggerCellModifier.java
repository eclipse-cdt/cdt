/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.timers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.service.DsfServices;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.DsfExamplesPlugin;
import org.eclipse.cdt.examples.dsf.timers.AlarmService.TriggerDMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Cell modifier used to edit the trigger value.
 */
@ThreadSafeAndProhibitedFromDsfExecutor("fSession.getExecutor()")
public class TriggerCellModifier implements ICellModifier {

    private final DsfSession fSession;
    
    // Need to use the OSGi service tracker (instead of DsfServiceTracker), 
    // because it's being accessed on multiple threads.
    @ThreadSafe
    private ServiceTracker fServiceTracker;

    /**
     * Constructor for the modifier requires a valid session in order to 
     * initialize the service tracker.  
     * @param session DSF session this modifier will use.
     */
    public TriggerCellModifier(DsfSession session) {
        fSession = session;
    }

    public boolean canModify(Object element, String property) {
        return TimersViewColumnPresentation.COL_VALUE.equals(property) && 
            getAlarmDMC(element) != null; 
    }

    public Object getValue(Object element, String property) {
        if (!TimersViewColumnPresentation.COL_VALUE.equals(property)) return ""; 
        
        // Get the context and the session.  If element is not an trigger 
        // context or if the session is stale then bail out. 
        TriggerDMContext triggerCtx = getAlarmDMC(element);
        if (triggerCtx == null) return ""; 
        DsfSession session = DsfSession.getSession(triggerCtx.getSessionId());
        if (session == null) return ""; 
        
        // Create the query to request the value from service.  
        GetValueQuery query = new GetValueQuery(triggerCtx);
        try {
            session.getExecutor().execute(query);
        } catch (RejectedExecutionException e) {
            return "";
        }
        try {
            return query.get().toString();
        } catch (InterruptedException e) {
            assert false;
            return ""; 
        } catch (ExecutionException e) {
            return ""; 
        }
    }

    
    public void modify(Object element, String property, Object value) {
        if (!TimersViewColumnPresentation.COL_VALUE.equals(property)) return;

        TriggerDMContext dmc = getAlarmDMC(element);
        if (dmc == null) return;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null) return;

        // Shell is used in displaying error dialogs.
        Shell shell = getShell();
        if (shell == null) return;
        
        Integer intValue = null;
        if (value instanceof String) {
            try {
                intValue = new Integer(((String)value).trim());
            } catch (NumberFormatException e) {
                MessageDialog.openError(shell, "Invalid Value", 
                    "Please enter a positive integer");  
                return;
            }
            if (intValue.intValue() <= 0) {
                MessageDialog.openError(shell, "Invalid Value", 
                    "Please enter a positive integer");  
                return;
            }
        }

        // Create the query to write the value to the service.
        SetValueQuery query = new SetValueQuery(dmc, intValue);

        try {
            session.getExecutor().execute(query);
        } catch (RejectedExecutionException e) {
            // View must be shutting down, no need to show error dialog.
        }
        try {
            // Return value is irrelevant, any error would come through with an exception.
            query.get().toString();
        } catch (InterruptedException e) {
            assert false;
        } catch (ExecutionException e) {
            // View must be shutting down, no need to show error dialog.
        }
    }

    /**
     * Need to dispose the cell modifier property because of the service 
     * tracker.
     */
    @ThreadSafe
    public synchronized void dispose() {
        if (fServiceTracker != null) {
            fServiceTracker.close();
        }
    }
    
    private Shell getShell() {
        if (DsfExamplesPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() != null) {
            return DsfExamplesPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        }
        return null;
    }

    private TriggerDMContext getAlarmDMC(Object element) {
        if (element instanceof IAdaptable) {
            return (TriggerDMContext)((IAdaptable)element).getAdapter(TriggerDMContext.class);
        }
        return null;
    }

    @ThreadSafe
    private synchronized AlarmService getService(TriggerDMContext dmc) {
        // Create and initialize the service tracker if needed.
    	String serviceId = DsfServices.createServiceFilter( AlarmService.class, fSession.getId() ); 
        if (fServiceTracker == null) {
            try {
                fServiceTracker = new ServiceTracker(
                    DsfExamplesPlugin.getBundleContext(), 
                    DsfExamplesPlugin.getBundleContext().createFilter(serviceId), 
                    null);
                fServiceTracker.open();
            } catch (InvalidSyntaxException e) {
                return null;
            }
        }
        // Get the service.
        return (AlarmService)fServiceTracker.getService();
    }

    
    private class GetValueQuery extends Query<Integer> {
        final TriggerDMContext fDmc;
        
        private GetValueQuery(TriggerDMContext dmc) {
            super();
            fDmc = dmc; 
        }
        
        @Override
        protected void execute(final DataRequestMonitor<Integer> rm) {
            // Guard against the session being disposed.  If session is disposed
            // it could mean that the executor is shut-down, which in turn 
            // could mean that we can't execute the "done" argument.
            // In that case, cancel to notify waiting thread.
            final DsfSession session = DsfSession.getSession(fDmc.getSessionId());
            if (session == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Debug session already shut down.", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
                
            AlarmService service = getService(fDmc);
            if (service == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, 
                                          "Service not available", null)); 
                rm.done();
                return;
            }
            
            int value = service.getTriggerValue(fDmc);
            if (value == -1) {
                rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, 
                    "Invalid context", null)); 
                rm.done();                
                return;
            }
            
            rm.setData(value);
            rm.done();
        }
    }

    private class SetValueQuery extends Query<Object> {
        
        TriggerDMContext fDmc;
        int fValue;
        
        SetValueQuery(TriggerDMContext dmc, int value) {
            super();
            fDmc = dmc;
            fValue = value;
        }
        
        @Override
        protected void execute(final DataRequestMonitor<Object> rm) {
            // Guard against terminated session
            final DsfSession session = DsfSession.getSession(fDmc.getSessionId());
            if (session == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Debug session already shut down.", null)); //$NON-NLS-1$
                rm.done();
                return;
            }

            // Guard against a disposed service
            AlarmService service = getService(fDmc);
            if (service == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, 
                                          "Service not available", null)); 
                rm.done();
                return;
            }

            // Finally set the value and return.
            service.setTriggerValue(fDmc, fValue);
            
            // Return value is irrelevant.
            rm.setData(new Object());
            rm.done();
        }
    }
}
