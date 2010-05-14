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

package org.eclipse.cdt.dsf.debug.ui.viewmodel.register;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IMnemonic;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServices;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

@ThreadSafeAndProhibitedFromDsfExecutor("fSession#getExecutor")
public class SyncRegisterDataAccess {

    abstract public class  RegistersServiceQuery<V, K extends IDMContext> extends Query<V> {

        final protected K fDmc;

        public RegistersServiceQuery(K dmc) {
            fDmc = dmc;
        }

        @Override
        protected void execute(final DataRequestMonitor<V> rm) {
            /*
             * We're in another dispatch, so we must guard against executor
             * shutdown again.
             */
            final DsfSession session = DsfSession.getSession(fDmc.getSessionId());
            if (session == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Debug session already shut down.", null)); //$NON-NLS-1$
                rm.done();
                return;
            }

            /*
             * Guard against a disposed service
             */
            IRegisters service = getService();
            if (service == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE,
                    "Service unavailable", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            
            doExecute(service, rm);
        }
        
        abstract protected void doExecute(IRegisters registersService, DataRequestMonitor<V> rm);
    }

    
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

    public SyncRegisterDataAccess(DsfSession session) {
        fSession = session;
    }

    @ThreadSafe
    private synchronized IRegisters getService() {

        String serviceId = DsfServices.createServiceFilter(IRegisters.class, fSession.getId());
        if (fServiceTracker == null) {
            try {
                fServiceTracker = new ServiceTracker(DsfUIPlugin.getBundleContext(), DsfUIPlugin
                    .getBundleContext().createFilter(serviceId), null);
                fServiceTracker.open();
            } catch (InvalidSyntaxException e) {
                return null;
            }
        }
        return (IRegisters) fServiceTracker.getService();
    }

    @ThreadSafe
    public synchronized void dispose() {
        if (fServiceTracker != null) {
            fServiceTracker.close();
        }
    }

    public class GetBitFieldValueQuery extends RegistersServiceQuery<IBitFieldDMData, IBitFieldDMContext> {

        public GetBitFieldValueQuery(IBitFieldDMContext dmc) {
            super(dmc);
        }

        @Override
        protected void doExecute(IRegisters service, final DataRequestMonitor<IBitFieldDMData> rm) {
            service.getBitFieldData(
                fDmc, 
                new DataRequestMonitor<IBitFieldDMData>(ImmediateExecutor.getInstance(), rm) {
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

    public IBitFieldDMContext getBitFieldDMC(Object element) {
        if (element instanceof IDMVMContext) {
            IDMContext dmc = ((IDMVMContext) element).getDMContext();
            return DMContexts.getAncestorOfType(dmc, IBitFieldDMContext.class);
        }
        return null;
    }

    public IBitFieldDMData readBitField(Object element) {
        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IBitFieldDMContext dmc = getBitFieldDMC(element);
        if (dmc == null)
            return null;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null)
            return null;

        /*
         * Create the query to request the value from service. Note: no need to
         * guard agains RejectedExecutionException, because
         * DsfSession.getSession() above would only return an active session.
         */
        GetBitFieldValueQuery query = new GetBitFieldValueQuery(dmc);
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

    public class SetBitFieldValueQuery extends RegistersServiceQuery<Object, IBitFieldDMContext> {

        private String fValue;
        private String fFormatId;

        public SetBitFieldValueQuery(IBitFieldDMContext dmc, String value, String formatId) {
            super(dmc);
            fValue = value;
            fFormatId = formatId;
        }

        @Override
        protected void doExecute(IRegisters service, final DataRequestMonitor<Object> rm) {
            // Write the bit field using a string/format style.
            service.writeBitField(
                fDmc, fValue, fFormatId, 
                new DataRequestMonitor<IBitFieldDMData>(ImmediateExecutor.getInstance(), rm) {
                    @Override
                    protected void handleSuccess() {
                        /*
                         * All good set return value.
                         */
                        rm.setData(new Object());
                        rm.done();
                    }
                });
        }
    }

    public void writeBitField(Object element, String value, String formatId) {

        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IBitFieldDMContext dmc = getBitFieldDMC(element);
        if (dmc == null)
            return;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null)
            return;

        /*
         * Create the query to write the value to the service. Note: no need to
         * guard agains RejectedExecutionException, because
         * DsfSession.getSession() above would only return an active session.
         */
        SetBitFieldValueQuery query = new SetBitFieldValueQuery(dmc, value, formatId);
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
            assert false;
            /*
             * View must be shutting down, no need to show erro dialog.
             */
        }
    }

    public class SetBitFieldValueMnemonicQuery extends RegistersServiceQuery<Object, IBitFieldDMContext> {
        IMnemonic fMnemonic;

        public SetBitFieldValueMnemonicQuery(IBitFieldDMContext dmc, IMnemonic mnemonic) {
            super(dmc);
            fMnemonic = mnemonic;
        }

        @Override
        protected void doExecute(IRegisters service, final DataRequestMonitor<Object> rm) {
            // Write the bit field using the mnemonic style.
            service.writeBitField(
                fDmc, fMnemonic, 
                new DataRequestMonitor<IBitFieldDMData>(ImmediateExecutor.getInstance(), rm) {
                    @Override
                    protected void handleSuccess() {
                        /*
                         * All good set return value.
                         */
                        rm.setData(new Object());
                        rm.done();
                    }
                });
        }
    }

    public void writeBitField(Object element, IMnemonic mnemonic) {

        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IBitFieldDMContext dmc = getBitFieldDMC(element);
        if (dmc == null)
            return;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null)
            return;

        /*
         * Create the query to write the value to the service. Note: no need to
         * guard agains RejectedExecutionException, because
         * DsfSession.getSession() above would only return an active session.
         */
        SetBitFieldValueMnemonicQuery query = new SetBitFieldValueMnemonicQuery(dmc, mnemonic);
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
             * View must be shutting down, no need to show erro dialog.
             */
        }
    }

    public IRegisterGroupDMContext getRegisterGroupDMC(Object element) {
        if (element instanceof IDMVMContext) {
            IDMContext dmc = ((IDMVMContext) element).getDMContext();
            return DMContexts.getAncestorOfType(dmc, IRegisterGroupDMContext.class);
        }
        return null;
    }

    public IRegisterDMContext getRegisterDMC(Object element) {
        if (element instanceof IDMVMContext) {
            IDMContext dmc = ((IDMVMContext) element).getDMContext();
            return DMContexts.getAncestorOfType(dmc, IRegisterDMContext.class);
        }
        return null;
    }

    public IFormattedDataDMContext getFormattedDMC(Object element) {
        if (element instanceof IDMVMContext) {
            IDMContext dmc = ((IDMVMContext) element).getDMContext();
            IRegisterDMContext regdmc = DMContexts.getAncestorOfType(dmc, IRegisterDMContext.class);
            return DMContexts.getAncestorOfType(regdmc, IFormattedDataDMContext.class);
        }
        return null;
    }

    public class GetRegisterGroupValueQuery extends RegistersServiceQuery<IRegisterGroupDMData, IRegisterGroupDMContext> {
        public GetRegisterGroupValueQuery(IRegisterGroupDMContext dmc) {
            super(dmc);
        }

        @Override
        protected void doExecute(IRegisters service, final DataRequestMonitor<IRegisterGroupDMData> rm) {
            service.getRegisterGroupData(
                fDmc, 
                new DataRequestMonitor<IRegisterGroupDMData>(ImmediateExecutor.getInstance(), rm) {
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

    public IRegisterGroupDMData readRegisterGroup(Object element) {
        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IRegisterGroupDMContext dmc = getRegisterGroupDMC(element);
        if (dmc == null)
            return null;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null)
            return null;

        /*
         * Create the query to request the value from service. Note: no need to
         * guard agains RejectedExecutionException, because
         * DsfSession.getSession() above would only return an active session.
         */
        GetRegisterGroupValueQuery query = new GetRegisterGroupValueQuery(dmc);
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

    public class GetRegisterValueQuery extends RegistersServiceQuery<IRegisterDMData, IRegisterDMContext> {
        public GetRegisterValueQuery(IRegisterDMContext dmc) {
            super(dmc);
        }

        @Override
        protected void doExecute(IRegisters service, final DataRequestMonitor<IRegisterDMData> rm) {
            service.getRegisterData(
                fDmc, 
                new DataRequestMonitor<IRegisterDMData>(ImmediateExecutor.getInstance(), rm) {
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

    public IRegisterDMData readRegister(Object element) {
        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IRegisterDMContext dmc = getRegisterDMC(element);
        if (dmc == null)
            return null;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null)
            return null;

        /*
         * Create the query to request the value from service. Note: no need to
         * guard agains RejectedExecutionException, because
         * DsfSession.getSession() above would only return an active session.
         */
        GetRegisterValueQuery query = new GetRegisterValueQuery(dmc);
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

    public class SetRegisterValueQuery extends RegistersServiceQuery<Object, IRegisterDMContext> {
        private String fValue;

        private String fFormatId;

        public SetRegisterValueQuery(IRegisterDMContext dmc, String value, String formatId) {
            super(dmc);
            fValue = value;
            fFormatId = formatId;
        }

        @Override
        protected void doExecute(IRegisters service, final DataRequestMonitor<Object> rm) {
            /*
             * Write the bit field using a string/format style.
             */
            service.writeRegister(
                fDmc, fValue, fFormatId,
                new DataRequestMonitor<IBitFieldDMData>(ImmediateExecutor.getInstance(), rm) {
                    @Override
                    protected void handleSuccess() {
                        /*
                         * All good set return value.
                         */
                        rm.setData(new Object());
                        rm.done();
                    }
                });
        }
    }

    public void writeRegister(Object element, String value, String formatId) {

        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IRegisterDMContext dmc = getRegisterDMC(element);
        if (dmc == null)
            return;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null)
            return;

        /*
         * Create the query to write the value to the service. Note: no need to
         * guard agains RejectedExecutionException, because
         * DsfSession.getSession() above would only return an active session.
         */
        SetRegisterValueQuery query = new SetRegisterValueQuery(dmc, value, formatId);
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
             * View must be shutting down, no need to show erro dialog.
             */
        }
    }

    public class GetSupportFormatsValueQuery extends RegistersServiceQuery<String[], IFormattedDataDMContext> {

        public GetSupportFormatsValueQuery(IFormattedDataDMContext dmc) {
            super(dmc);
        }

        @Override
        protected void doExecute(IRegisters service, final DataRequestMonitor<String[]> rm) {
            service.getAvailableFormats(fDmc, rm);
        }
    }

    public String[] getSupportedFormats(Object element) {

        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IFormattedDataDMContext dmc = null;
        if (element instanceof IDMVMContext) {
            IDMContext vmcdmc = ((IDMVMContext) element).getDMContext();
            IRegisterDMContext regdmc = DMContexts.getAncestorOfType(vmcdmc, IRegisterDMContext.class);
            dmc = DMContexts.getAncestorOfType(regdmc, IFormattedDataDMContext.class);
        }

        if (dmc == null)
            return null;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null)
            return null;

        /*
         * Create the query to write the value to the service. Note: no need to
         * guard agains RejectedExecutionException, because
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
            return query.get();
        } catch (InterruptedException e) {
            assert false;
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }

    public class GetFormattedValueValueQuery extends RegistersServiceQuery<String, IFormattedDataDMContext> {

        private String fFormatId;

        public GetFormattedValueValueQuery(IFormattedDataDMContext dmc, String formatId) {
            super(dmc);
            fFormatId = formatId;
        }

        @Override
        protected void doExecute(IRegisters service, final DataRequestMonitor<String> rm) {
            /*
             * Convert to the proper formatting DMC then go get the formatted
             * value.
             */

            FormattedValueDMContext formDmc = service.getFormattedValueContext(fDmc, fFormatId);

            service.getFormattedExpressionValue(
                formDmc, 
                new DataRequestMonitor<FormattedValueDMData>(ImmediateExecutor.getInstance(), rm) {
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

    public String getFormattedRegisterValue(Object element, String formatId) {

        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IFormattedDataDMContext dmc = null;
        if (element instanceof IDMVMContext) {
            IDMContext vmcdmc = ((IDMVMContext) element).getDMContext();
            IRegisterDMContext regdmc = DMContexts.getAncestorOfType(vmcdmc, IRegisterDMContext.class);
            dmc = DMContexts.getAncestorOfType(regdmc, IFormattedDataDMContext.class);
        }

        if (dmc == null)
            return null;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null)
            return null;

        /*
         * Create the query to write the value to the service. Note: no need to
         * guard agains RejectedExecutionException, because
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
            return query.get();
        } catch (InterruptedException e) {
            assert false;
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }

    public String getFormattedBitFieldValue(Object element, String formatId) {

        /*
         * Get the DMC and the session. If element is not an register DMC, or
         * session is stale, then bail out.
         */
        IFormattedDataDMContext dmc = null;
        if (element instanceof IDMVMContext) {
            IDMContext vmcdmc = ((IDMVMContext) element).getDMContext();
            IBitFieldDMContext bitfielddmc = DMContexts.getAncestorOfType(vmcdmc, IBitFieldDMContext.class);
            dmc = DMContexts.getAncestorOfType(bitfielddmc, IFormattedDataDMContext.class);
        }

        if (dmc == null)
            return null;
        DsfSession session = DsfSession.getSession(dmc.getSessionId());
        if (session == null)
            return null;

        /*
         * Create the query to write the value to the service. Note: no need to
         * guard agains RejectedExecutionException, because
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
            return query.get();
        } catch (InterruptedException e) {
            assert false;
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }
    
    public class GetRegisterGroupDataQuery extends RegistersServiceQuery<IRegisterGroupDMData, IRegisterGroupDMContext> {

        public GetRegisterGroupDataQuery(IRegisterGroupDMContext dmc) {
            super(dmc);
        }

        @Override
        protected void doExecute(IRegisters service, final DataRequestMonitor<IRegisterGroupDMData> rm) {
            service.getRegisterGroupData(fDmc, rm);
        }
    }

    public IRegisterGroupDMData getRegisterGroupDMData(Object element) {
        IRegisterGroupDMContext dmc = null;
        if (element instanceof IDMVMContext) {
            dmc = DMContexts.getAncestorOfType(
                ((IDMVMContext) element).getDMContext(), 
                IRegisterGroupDMContext.class);
        }

        DsfSession session = DsfSession.getSession(dmc.getSessionId());

        if (dmc != null && session != null) {
            GetRegisterGroupDataQuery query = new GetRegisterGroupDataQuery(dmc);
            session.getExecutor().execute(query);

            try {
                return query.get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }
        return null;
    }

    
    public class GetRegisterDataQuery extends RegistersServiceQuery<IRegisterDMData, IRegisterDMContext> {

        public GetRegisterDataQuery(IRegisterDMContext dmc) {
            super(dmc);
        }

        @Override
        protected void doExecute(IRegisters service, final DataRequestMonitor<IRegisterDMData> rm) {
            service.getRegisterData(fDmc, rm);
        }
    }

    public IRegisterDMData getRegisterDMData(Object element) {
        IRegisterDMContext dmc = null;
        if (element instanceof IDMVMContext) {
            dmc = DMContexts.getAncestorOfType( ((IDMVMContext) element).getDMContext(), IRegisterDMContext.class );
        }
        DsfSession session = DsfSession.getSession(dmc.getSessionId());

        if (dmc != null && session != null) {
            GetRegisterDataQuery query = new GetRegisterDataQuery(dmc);
            session.getExecutor().execute(query);

            try {
                return query.get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }
        return null;
    }

    public class GetBitFieldQuery extends RegistersServiceQuery<IBitFieldDMData, IBitFieldDMContext> {

        public GetBitFieldQuery(IBitFieldDMContext dmc) {
            super(dmc);
        }

        @Override
        protected void doExecute(IRegisters service, final DataRequestMonitor<IBitFieldDMData> rm) {
            service.getBitFieldData(fDmc, rm);
        }
    }

    public IBitFieldDMData getBitFieldDMData(Object element) {
        IBitFieldDMContext dmc = null;
        if (element instanceof IDMVMContext) {
            dmc = DMContexts.getAncestorOfType( ((IDMVMContext) element).getDMContext(), IBitFieldDMContext.class );
        }
        DsfSession session = DsfSession.getSession(dmc.getSessionId());

        if (dmc != null && session != null) {
            GetBitFieldQuery query = new GetBitFieldQuery(dmc);
            session.getExecutor().execute(query);

            try {
                return query.get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }
        return null;
    }


}
