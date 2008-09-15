/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.variable;

import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.Query;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

@ThreadSafeAndProhibitedFromDsfExecutor("fSession#getExecutor")
public class SyncVariableDataAccess {

    /**
     * The session that this data access operates in.
     */
    private final DsfSession fSession;
    
    /**
     * Need to use the OSGi service tracker here (instead of DsfServiceTracker),
     * because we're accessing it in non-dispatch thread. DsfServiceTracker is
     * not thread-safe.
     */
    @ThreadSafe
    private ServiceTracker fServiceTracker;

    
    public SyncVariableDataAccess(DsfSession session) {
        fSession = session;
    }
    
    @ThreadSafe
    private synchronized IExpressions getService() {

        if (fServiceTracker == null) {
            try {
                fServiceTracker = new ServiceTracker(
                    DsfDebugUIPlugin.getBundleContext(), 
                    DsfDebugUIPlugin.getBundleContext().createFilter(getServiceFilter()), null);
                fServiceTracker.open();
            } catch (InvalidSyntaxException e) {
                return null;
            }
        } 
        return (IExpressions) fServiceTracker.getService();
    }

    private String getServiceFilter() {
        StringBuffer filter = new StringBuffer();
        filter.append("(&"); //$NON-NLS-1$
        filter.append("(OBJECTCLASS="); //$NON-NLS-1$
        filter.append(IExpressions.class.getName());
        filter.append(')');
        filter.append('(');
        filter.append(IDsfService.PROP_SESSION_ID);
        filter.append('=');
        filter.append(fSession.getId());
        filter.append(')');
        filter.append(')');
        return filter.toString();
    }

    @ThreadSafe
    public synchronized void dispose() {
        if (fServiceTracker != null) {
            fServiceTracker.close();
        }
    }
    
    public IExpressionDMContext getVariableDMC(Object element) {
        if (element instanceof IAdaptable) {
            return (IExpressionDMContext) ((IAdaptable) element).getAdapter(IExpressionDMContext.class);
        }
        return null;
    }


    public class GetVariableValueQuery extends Query<IExpressionDMData> {

        private IExpressionDMContext fDmc;

        public GetVariableValueQuery(IExpressionDMContext dmc) {
            super();
            fDmc = dmc;
        }

        @Override
        protected void execute(final DataRequestMonitor<IExpressionDMData> rm) {
            /*
             * Guard against the session being disposed. If session is disposed
             * it could mean that the executor is shut-down, which in turn could
             * mean that we can't complete the RequestMonitor argument. in that
             * case, cancel to notify waiting thread.
             */
            final DsfSession session = DsfSession.getSession(fDmc.getSessionId());
            if (session == null) {
                cancel(false);
                rm.done();
                return;
            }

            IExpressions service = getService();
            if (service == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Service not available", null)); //$NON-NLS-1$
                rm.done();
                return;
            }

            service.getExpressionData(fDmc, new DataRequestMonitor<IExpressionDMData>(session.getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    /*
                     * All good set return value.
                     */
                    rm.setData(getData());
                    rm.done();
                }
            });
        }
    }

    public IExpressionDMContext getExpressionDMC(Object element) {
        if (element instanceof IAdaptable) {
            return (IExpressionDMContext) ((IAdaptable) element).getAdapter(IExpressionDMContext.class);
        }
        return null;
    }

    public IExpressionDMData readVariable(Object element) {
        /*
         * Get the DMC and the session. If element is not an expression DMC, or
         * session is stale, then bail out.
         */
        IExpressionDMContext dmc = getExpressionDMC(element);
        if (dmc == null) return null;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null) return null;

        /*
         * Create the query to request the value from service. Note: no need to
         * guard against RejectedExecutionException, because
         * DsfSession.getSession() above would only return an active session.
         */
        GetVariableValueQuery query = new GetVariableValueQuery(dmc);
        session.getExecutor().execute(query);

        /*
         * Now we have the data, go and get it. Since the call is completed now
         * the ".get()" will not suspend it will immediately return with the
         * data.
         */
        try {
            return query.get();
        } catch (InterruptedException e) {
            assert false;
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }

    public class SetVariableValueQuery extends Query<Object> {

        private IExpressionDMContext fDmc;
        private String fValue;
        private String fFormatId;

        public SetVariableValueQuery(IExpressionDMContext dmc, String value, String formatId) {
            super();
            fDmc = dmc;
            fValue = value;
            fFormatId = formatId;
        }

        @Override
        protected void execute(final DataRequestMonitor<Object> rm) {
            /*
             * We're in another dispatch, so we must guard against executor
             * shutdown again.
             */
            final DsfSession session = DsfSession.getSession(fDmc.getSessionId());
            if (session == null) {
                cancel(false);
                rm.done();
                return;
            }

            /*
             * Guard against a disposed service
             */
            IExpressions service = getService();
            if (service == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Service unavailable", null)); //$NON-NLS-1$
                rm.done();
                return;
            }

            /*
             * Write the expression value using a string/format style.
             */
            service.writeExpression(
               fDmc, 
               fValue, 
               fFormatId,
               new DataRequestMonitor<IExpressionDMData>(session.getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        /*
                         * All good set return value.
                         */
                        rm.setData(new Object());
                        rm.done();
                    }
                }
           );
        }
    }

    public void writeVariable(Object element, String value, String formatId) {

        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IExpressionDMContext dmc = getExpressionDMC(element);
        if (dmc == null) return;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null) return;

        /*
         * Create the query to write the value to the service. Note: no need to
         * guard against RejectedExecutionException, because
         * DsfSession.getSession() above would only return an active session.
         */
        SetVariableValueQuery query = new SetVariableValueQuery(dmc, value, formatId);
        session.getExecutor().execute(query);

        /*
         * Now we have the data, go and get it. Since the call is completed now
         * the ".get()" will not suspend it will immediately return with the
         * data.
         */
        try {
            /*
             * Return value is irrelevant, any error would come through with an
             * exception.
             */
            query.get();
        } catch (InterruptedException e) {
            assert false;
        } catch (ExecutionException e) {
            /*
             * View must be shutting down, no need to show error dialog.
             */
        }
    }

    public IFormattedDataDMContext getFormattedDMC(Object element) {
        if (element instanceof IAdaptable) {
            return (IFormattedDataDMContext) ((IAdaptable) element).getAdapter(IFormattedDataDMContext.class);
        }
        return null;
    }
    
    public class GetSupportFormatsValueQuery extends Query<Object> {

        IFormattedDataDMContext fDmc;

        public GetSupportFormatsValueQuery(IFormattedDataDMContext dmc) {
            super();
            fDmc = dmc;
        }

        @Override
        protected void execute(final DataRequestMonitor<Object> rm) {
            /*
             * We're in another dispatch, so we must guard against executor
             * shutdown again.
             */
            final DsfSession session = DsfSession.getSession(fDmc.getSessionId());
            if (session == null) {
                cancel(false);
                rm.done();
                return;
            }

            /*
             * Guard against a disposed service
             */
            IExpressions service = getService();
            if (service == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Service unavailable", null)); //$NON-NLS-1$
                rm.done();
                return;
            }

            /*
             * Get the available formats from the service.
             */
            service.getAvailableFormats(
                fDmc,
                new DataRequestMonitor<String[]>(session.getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        /*
                         * All good set return value.
                         */
                        rm.setData(new Object());
                        rm.done();
                    }
                }
            );
        }
    }

    public String[] getSupportedFormats(Object element) {

        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IFormattedDataDMContext dmc = getFormattedDMC(element);
        if (dmc == null) return null;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null) return null;
        
        /*
         * Create the query to write the value to the service. Note: no need to
         * guard against RejectedExecutionException, because
         * DsfSession.getSession() above would only return an active session.
         */
        GetSupportFormatsValueQuery query = new GetSupportFormatsValueQuery(dmc);
        session.getExecutor().execute(query);

        /*
         * Now we have the data, go and get it. Since the call is completed now
         * the ".get()" will not suspend it will immediately return with the
         * data.
         */
        try {
            return (String[]) query.get();
        } catch (InterruptedException e) {
            assert false;
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }

    public class GetFormattedValueValueQuery extends Query<Object> {

        private IFormattedDataDMContext fDmc;
        private String fFormatId;

        public GetFormattedValueValueQuery(IFormattedDataDMContext dmc, String formatId) {
            super();
            fDmc = dmc;
            fFormatId = formatId;
        }

        @Override
        protected void execute(final DataRequestMonitor<Object> rm) {
            /*
             * We're in another dispatch, so we must guard against executor
             * shutdown again.
             */
            final DsfSession session = DsfSession.getSession(fDmc.getSessionId());
            if (session == null) {
                cancel(false);
                rm.done();
                return;
            }

            /*
             * Guard against a disposed service
             */
            IExpressions service = getService();
            if (service == null) {
                rm .setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Service unavailable", null)); //$NON-NLS-1$
                rm.done();
                return;
            }

            /*
             * Convert to the proper formatting DMC then go get the formatted value.
             */
            
            FormattedValueDMContext formDmc = service.getFormattedValueContext(fDmc, fFormatId);
            
            service.getFormattedExpressionValue(formDmc, new DataRequestMonitor<FormattedValueDMData>(session.getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    /*
                     * All good set return value.
                     */
                    rm.setData(getData().getFormattedValue());
                    rm.done();
                }
            });
        }
    }

    public String getFormattedValue(Object element, String formatId) {

        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IFormattedDataDMContext dmc = getFormattedDMC(element);
        if (dmc == null) return null;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null) return null;
        
        /*
         * Create the query to write the value to the service. Note: no need to
         * guard against RejectedExecutionException, because
         * DsfSession.getSession() above would only return an active session.
         */
        GetFormattedValueValueQuery query = new GetFormattedValueValueQuery(dmc, formatId);
        session.getExecutor().execute(query);

        /*
         * Now we have the data, go and get it. Since the call is completed now
         * the ".get()" will not suspend it will immediately return with the
         * data.
         */
        try {
            return (String) query.get();
        } catch (InterruptedException e) {
            assert false;
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }
 
    /**
     * @since 1.1
     */
    public class CanWriteExpressionQuery extends Query<Boolean> {

        private IExpressionDMContext fDmc;

        public CanWriteExpressionQuery(IExpressionDMContext dmc) {
        	super();
            fDmc = dmc;
        }

        @Override
        protected void execute(final DataRequestMonitor<Boolean> rm) {
            /*
             * We're in another dispatch, so we must guard against executor
             * shutdown again.
             */
            final DsfSession session = DsfSession.getSession(fDmc.getSessionId());
            if (session == null) {
                cancel(false);
                rm.done();
                return;
            }

            /*
             * Guard against a disposed service
             */
            IExpressions service = getService();
            if (service == null) {
                rm .setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Service unavailable", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            
            service.canWriteExpression(fDmc, new DataRequestMonitor<Boolean>(session.getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    /*
                     * All good set return value.
                     */
                    rm.setData(getData());
                    rm.done();
                }
            });    
        }
    }

    public boolean canWriteExpression(Object element) {
        /*
         * Get the DMC and the session. If element is not an expression DMC, or
         * session is stale, then bail out.
         */
        IExpressionDMContext dmc = getExpressionDMC(element);
        if (dmc == null) return false;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null) return false;
        
        /*
         * Create the query to make the request to the service. Note: no need to
         * guard against RejectedExecutionException, because
         * DsfSession.getSession() above would only return an active session.
         */
        CanWriteExpressionQuery query = new CanWriteExpressionQuery(dmc);
        session.getExecutor().execute(query);

        /*
         * Now we have the data, go and get it. Since the call is completed now
         * the ".get()" will not suspend it will immediately return with the
         * data.
         */
        try {
            return query.get();
        } catch (InterruptedException e) {
            assert false;
            return false;
        } catch (ExecutionException e) {
            return false;
        }
    }
    
 
}
