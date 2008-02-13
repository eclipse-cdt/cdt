/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.dsf.timers;

import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.Query;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.dd.dsf.service.DsfServices;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.examples.dsf.DsfExamplesPlugin;
import org.eclipse.dd.examples.dsf.timers.AlarmService.AlarmDMC;
import org.eclipse.dd.examples.dsf.timers.AlarmService.AlarmData;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * 
 */
@ThreadSafeAndProhibitedFromDsfExecutor("")
public class AlarmCellModifier implements ICellModifier {

    private final DsfSession fSession;
    
    /** 
     * Need to use the OSGi service tracker here (instead of DsfServiceTracker), 
     * because we're accessing it in non-dispatch thread.  DsfServiceTracker is not
     * thread-safe.
     */
    @ThreadSafe
    private ServiceTracker fServiceTracker;

    /**
     * Constructor for the modifier requires a valid DSF session in order to 
     * initialize the service tracker.  
     * @param session DSF session this modifier will use.
     */
    public AlarmCellModifier(DsfSession session) {
        fSession = session;
    }

    public boolean canModify(Object element, String property) {
        return TimersViewColumnPresentation.COL_VALUE.equals(property) && getAlarmDMC(element) != null; 
    }

    public Object getValue(Object element, String property) {
        if (!TimersViewColumnPresentation.COL_VALUE.equals(property)) return ""; //$NON-NLS-1$
        
        // Get the DMC and the session.  If element is not an alarm DMC, or 
        // session is stale, then bail out. 
        AlarmDMC dmc = getAlarmDMC(element);
        if (dmc == null) return ""; //$NON-NLS-1$
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null) return ""; //$NON-NLS-1$
        
        /*
         * Create the query to request the value from service.  
         * Note: no need to guard against RejectedExecutionException, because 
         * DsfSession.getSession() above would only return an active session. 
         */ 
        GetValueQuery query = new GetValueQuery(dmc);
        session.getExecutor().execute(query);
        try {
            return query.get().toString();
        } catch (InterruptedException e) {
            assert false;
            return ""; //$NON-NLS-1$
        } catch (ExecutionException e) {
            return ""; //$NON-NLS-1$
        }
    }

    
    public void modify(Object element, String property, Object value) {
        if (!TimersViewColumnPresentation.COL_VALUE.equals(property)) return;

        AlarmDMC dmc = getAlarmDMC(element);
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
                MessageDialog.openError(shell, "Invalid Value", "Please enter a positive integer"); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            if (intValue.intValue() <= 0) {
                MessageDialog.openError(shell, "Invalid Value", "Please enter a positive integer"); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
        }

        /*
         * Create the query to write the value to the service.
         * Note: no need to guard against RejectedExecutionException, because 
         * DsfSession.getSession() above would only return an active session. 
         */ 
        SetValueQuery query = new SetValueQuery(dmc, intValue);

        session.getExecutor().execute(query);

        try {
            // Return value is irrelevant, any error would come through with an exception.
            query.get().toString();
        } catch (InterruptedException e) {
            assert false;
        } catch (ExecutionException e) {
            // View must be shutting down, no need to show erro dialog.
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

    private AlarmDMC getAlarmDMC(Object element) {
        if (element instanceof IAdaptable) {
            return (AlarmDMC)((IAdaptable)element).getAdapter(AlarmDMC.class);
        }
        return null;
    }

    @ThreadSafe
    private synchronized AlarmService getService(AlarmDMC dmc) {
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
        return (AlarmService)fServiceTracker.getService();
    }
    
    private class GetValueQuery extends Query<Integer> {
        
        final AlarmDMC fDmc;
        
        private GetValueQuery(AlarmDMC dmc) {
            super();
            fDmc = dmc; 
        }
        
        @Override
        protected void execute(final DataRequestMonitor<Integer> rm) {
            /*
             * Guard against the session being disposed.  If session is disposed
             * it could mean that the executor is shut-down, which in turn 
             * could mean that we can't execute the "done" argument.
             * In that case, cancel to notify waiting thread.
             */ 
            final DsfSession session = DsfSession.getSession(fDmc.getSessionId());
            if (session == null) {
                cancel(false);
                return;
            }
                
            AlarmService service = getService(fDmc);
            if (service == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, IDsfService.INVALID_STATE, 
                                          "Service not available", null)); //$NON-NLS-1$
                return;
            }
            
            service.getAlarmData(fDmc, new DataRequestMonitor<AlarmData>(session.getExecutor(), rm) {
                @Override
                protected void handleCompleted() {
                    // We're in another dispatch, so we must guard against executor shutdown again. 
                    if (DsfSession.isSessionActive(session.getId())) {
                        super.handleCompleted();
                    }
                }
                
                @Override
                protected void handleOK() {
                    rm.setData(getData().getTriggeringValue());
                    rm.done();
                }
            });
        }
    }

    private class SetValueQuery extends Query<Object> {
        
        AlarmDMC fDmc;
        int fValue;
        
        SetValueQuery(AlarmDMC dmc, int value) {
            super();
            fDmc = dmc;
            fValue = value;
        }
        
        @Override
        protected void execute(final DataRequestMonitor<Object> rm) {
            // Guard against terminated session
            final DsfSession session = DsfSession.getSession(fDmc.getSessionId());
            if (session == null) {
                cancel(false);
                return;
            }

            // Guard against a disposed service
            AlarmService service = getService(fDmc);
            if (service == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfExamplesPlugin.PLUGIN_ID, IDsfService.INVALID_STATE, 
                                          "Service not available", null)); //$NON-NLS-1$
                rm.done();
                return;
            }

            // Finally set the value and return.
            service.setAlarmValue(fDmc, fValue);
            
            // Return value is irrelevant.
            rm.setData(new Object());
            rm.done();
        }
    }
}
