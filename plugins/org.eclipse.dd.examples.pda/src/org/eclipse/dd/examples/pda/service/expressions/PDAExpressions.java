/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.expressions;

import java.util.Hashtable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.debug.service.command.CommandCache;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.dd.examples.pda.service.command.PDACommandControl;
import org.eclipse.dd.examples.pda.service.command.PDACommandResult;
import org.eclipse.dd.examples.pda.service.command.commands.PDASetVarCommand;
import org.eclipse.dd.examples.pda.service.command.commands.PDAVarCommand;
import org.osgi.framework.BundleContext;

/**
 * 
 */
public class PDAExpressions extends AbstractDsfService implements IExpressions {

    private PDACommandControl fCommandControl;
    private IStack fStack;

    private CommandCache fCommandCache;

    public PDAExpressions(DsfSession session) {
        super(session);
    }

    @Override
    protected BundleContext getBundleContext() {
        return PDAPlugin.getBundleContext();
    }

    @Override
    public void initialize(final RequestMonitor rm) {
        super.initialize(
            new RequestMonitor(getExecutor(), rm) { 
                @Override
                protected void handleOK() {
                    doInitialize(rm);
                }});
    }

    private void doInitialize(final RequestMonitor rm) {
        fCommandControl = getServicesTracker().getService(PDACommandControl.class);
        fStack = getServicesTracker().getService(IStack.class);
        fCommandCache = new CommandCache(fCommandControl);

        getSession().addServiceEventListener(this, null);
        
        register(new String[]{IExpressions.class.getName(), PDAExpressions.class.getName()}, new Hashtable<String,String>());
        
        rm.done();
    }

    @Override
    public void shutdown(final RequestMonitor rm) {
        getSession().removeServiceEventListener(this);
        fCommandCache.reset();
        super.shutdown(rm);
    }

    public void canWriteExpression(IExpressionDMContext expressionContext, DataRequestMonitor<Boolean> rm) {
        rm.setData(true);
        rm.done();
    }

    public IExpressionDMContext createExpression(IDMContext ctx, String expression) {
        IFrameDMContext frameCtx = DMContexts.getAncestorOfType(ctx, IFrameDMContext.class);
        if (frameCtx != null) {
            return new ExpressionDMContext(getSession().getId(), frameCtx, expression);
        } else {
            return new InvalidExpressionDMContext(getSession().getId(), ctx, expression);
        }
    }

    public void getBaseExpressions(IExpressionDMContext exprContext, DataRequestMonitor<IExpressionDMContext[]> rm) {
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
        rm.done();
    }

    public void getExpressionAddressData(IExpressionDMContext dmc, DataRequestMonitor<IExpressionDMAddress> rm) {
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
        rm.done();
    }

    public void getExpressionData(final IExpressionDMContext exprCtx, final DataRequestMonitor<IExpressionDMData> rm) {
        if (exprCtx instanceof ExpressionDMContext) {
            rm.setData(new ExpressionDMData(exprCtx.getExpression()));
            rm.done();
        } else {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid expression context " + exprCtx, null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void getSubExpressionCount(IExpressionDMContext exprCtx, DataRequestMonitor<Integer> rm) {
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
        rm.done();
    }

    public void getSubExpressions(IExpressionDMContext exprCtx, DataRequestMonitor<IExpressionDMContext[]> rm) {
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
        rm.done();
    }

    public void getSubExpressions(IExpressionDMContext exprCtx, int startIndex, int length,
        DataRequestMonitor<IExpressionDMContext[]> rm) 
    {
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
        rm.done();
    }

    
    public void writeExpression(IExpressionDMContext exprCtx, String exprValue, String formatId, RequestMonitor rm) 
    {
        if (exprCtx instanceof ExpressionDMContext) {
            fCommandCache.execute(
                new PDASetVarCommand(fCommandControl.getDMContext(), exprCtx.getExpression(), exprValue), 
                new DataRequestMonitor<PDACommandResult>(getExecutor(), rm));
        } else {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid expression context " + exprCtx, null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
        rm.setData(new String[] { NATURAL_FORMAT });
        rm.done();
    }

    public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId) {
        return new FormattedValueDMContext(this, dmc, formatId);
    }

    public void getFormattedExpressionValue(FormattedValueDMContext formattedCtx, final DataRequestMonitor<FormattedValueDMData> rm) {
        final ExpressionDMContext exprCtx = DMContexts.getAncestorOfType(formattedCtx, ExpressionDMContext.class);
        if (exprCtx != null) {
            getExpressionValue(
                exprCtx, 
                new DataRequestMonitor<String>(getExecutor(), rm) {
                    @Override
                    protected void handleOK() {
                        rm.setData(new FormattedValueDMData(getData()));
                        rm.done();
                    }
                });
        } else {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid expression context " + formattedCtx, null)); //$NON-NLS-1$
            rm.done();
        }
    }

    private void getExpressionValue(final ExpressionDMContext exprCtx, final DataRequestMonitor<String> rm) {
        final IFrameDMContext frameCtx = DMContexts.getAncestorOfType(exprCtx, IFrameDMContext.class);
        fStack.getStackDepth(
            frameCtx, 0,
            new DataRequestMonitor<Integer>(getExecutor(), rm) {
                @Override
                protected void handleOK() {
                    int frameId = getData() - frameCtx.getLevel() - 1;
                    fCommandCache.execute(
                        new PDAVarCommand(fCommandControl.getDMContext(), frameId, exprCtx.getExpression()), 
                        new DataRequestMonitor<PDACommandResult>(getExecutor(), rm) {
                            @Override
                            protected void handleOK() {
                                rm.setData(getData().fResponseText);
                                rm.done();
                            }
                        });        
                }
            });
    }
    
    @Deprecated
    public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
        if (dmc instanceof IExpressionDMContext) {
            getExpressionData((IExpressionDMContext) dmc, (DataRequestMonitor<IExpressionDMData>) rm);
        } else if (dmc instanceof FormattedValueDMContext) {
            getFormattedExpressionValue((FormattedValueDMContext) dmc, (DataRequestMonitor<FormattedValueDMData>) rm);
        } else {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    @DsfServiceEventHandler 
    public void eventDispatched(IResumedDMEvent e) {
        fCommandCache.setTargetAvailable(false);
        if (!e.getReason().equals(StateChangeReason.STEP)) {
            fCommandCache.reset();
        }
    }    


    @DsfServiceEventHandler 
    public void eventDispatched(ISuspendedDMEvent e) {
        fCommandCache.setTargetAvailable(true);
        fCommandCache.reset();
    }
}
