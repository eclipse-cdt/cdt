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
package org.eclipse.dd.examples.pda.service;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.debug.service.command.CommandCache;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.dd.examples.pda.service.commands.PDACommandResult;
import org.eclipse.dd.examples.pda.service.commands.PDASetVarCommand;
import org.eclipse.dd.examples.pda.service.commands.PDAVarCommand;
import org.osgi.framework.BundleContext;

/**
 * 
 */
public class PDAExpressions extends AbstractDsfService implements IExpressions {

    @Immutable
    private static class ExpressionDMContext extends AbstractDMContext implements IExpressionDMContext {

        final private String fExpression;
        
        ExpressionDMContext(String sessionId, IFrameDMContext frameDmc, String expressin) {
            super(sessionId, new IDMContext[] { frameDmc });
            fExpression = expressin;
        }

        public String getExpression() { 
            return fExpression;
        }
        
        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) && ((ExpressionDMContext)other).fExpression.equals(fExpression);
        }
        
        @Override
        public int hashCode() {
            return super.baseHashCode() + fExpression.hashCode();
        }
        
        @Override
        public String toString() { 
            return baseToString() + ".expression(" + fExpression + ")";  
        }
    }

    /**
     * PDA expressions are simply variables.  Only the variable name 
     * is relevant for its data.
     */
    @Immutable
    private static class ExpressionDMData implements IExpressionDMData {

        final private String fExpression;
        
        public ExpressionDMData(String expression) {
            fExpression = expression;
        }
        
        public BasicType getBasicType() {
            return BasicType.basic;
        }

        public String getEncoding() {
            return null;
        }

        public Map<String, Integer> getEnumerations() {
            return null;
        }

        public String getName() {
            return fExpression;
        }

        public IRegisterDMContext getRegister() {
            return null;
        }

        public String getStringValue() {
            return null;
        }

        public String getTypeId() {
            return null;
        }

        public String getTypeName() {
            return null;
        }

    }

    // @see #createExpression()
    @Immutable
    private static class InvalidExpressionDMContext extends AbstractDMContext implements IExpressionDMContext {
        final private String fExpression;
    
        public InvalidExpressionDMContext(String sessionId, IDMContext parent, String expr) {
            super(sessionId, new IDMContext[] { parent });
            fExpression = expr;
        }
    
        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) && 
                fExpression == null 
                    ? ((InvalidExpressionDMContext) other).getExpression() == null 
                    : fExpression.equals(((InvalidExpressionDMContext) other).getExpression());
        }
    
        @Override
        public int hashCode() {
            return fExpression == null ? super.baseHashCode() : super.baseHashCode() ^ fExpression.hashCode();
        }
    
        @Override
        public String toString() {
            return baseToString() + ".invalid_expr[" + fExpression + "]"; 
        }
    
        public String getExpression() {
            return fExpression;
        }
    }
    
    @Immutable
    private static class ExpressionChangedDMEvent extends AbstractDMEvent<IExpressionDMContext> 
        implements IExpressionChangedDMEvent 
    {
        ExpressionChangedDMEvent(IExpressionDMContext expression) {
            super(expression);
        }
    }

    
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
                protected void handleSuccess() {
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
        // Create an expression based on the given context and string expression.  
        // The PDA debugger can only evaluate variables as expressions and only
        // in context of a frame.  
        IFrameDMContext frameCtx = DMContexts.getAncestorOfType(ctx, IFrameDMContext.class);
        if (frameCtx != null) {
            return new ExpressionDMContext(getSession().getId(), frameCtx, expression);
        } else {
            // If a frame cannot be found in context, return an "invalid" 
            // expression context, because a null return value is not allowed.
            // Evaluating an invalid expression context will always yield an 
            // error.
            return new InvalidExpressionDMContext(getSession().getId(), ctx, expression);
        }
    }

    public void getBaseExpressions(IExpressionDMContext exprContext, DataRequestMonitor<IExpressionDMContext[]> rm) {
        PDAPlugin.failRequest(rm, NOT_SUPPORTED, "Not supported");
    }

    public void getExpressionAddressData(IExpressionDMContext dmc, DataRequestMonitor<IExpressionDMAddress> rm) {
        PDAPlugin.failRequest(rm, NOT_SUPPORTED, "Not supported");
    }

    public void getExpressionData(final IExpressionDMContext exprCtx, final DataRequestMonitor<IExpressionDMData> rm) {
        // Since expression data doesn't contain any more information than the 
        // context, it doesn't require any debugger commmands.
        if (exprCtx instanceof ExpressionDMContext) {
            rm.setData(new ExpressionDMData(exprCtx.getExpression()));
            rm.done();
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid expression context " + exprCtx);
        }
    }

    public void getSubExpressionCount(IExpressionDMContext exprCtx, DataRequestMonitor<Integer> rm) {
        PDAPlugin.failRequest(rm, NOT_SUPPORTED, "Not supported");
    }

    public void getSubExpressions(IExpressionDMContext exprCtx, DataRequestMonitor<IExpressionDMContext[]> rm) {
        PDAPlugin.failRequest(rm, NOT_SUPPORTED, "Not supported");
    }

    public void getSubExpressions(IExpressionDMContext exprCtx, int startIndex, int length,
        DataRequestMonitor<IExpressionDMContext[]> rm) 
    {
        PDAPlugin.failRequest(rm, NOT_SUPPORTED, "Not supported");
    }

    public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
        // PDA debugger doesn't support formatting the expression.  Natural 
        // formatting is the only available option.
        rm.setData(new String[] { NATURAL_FORMAT });
        rm.done();
    }

    public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext exprCtx, String formatId) {
        // Creates a context that can be used to retrieve a formatted value.
        return new FormattedValueDMContext(this, exprCtx, formatId);
    }

    public void getFormattedExpressionValue(FormattedValueDMContext formattedCtx, 
        final DataRequestMonitor<FormattedValueDMData> rm) 
    {
        final ExpressionDMContext exprCtx = DMContexts.getAncestorOfType(formattedCtx, ExpressionDMContext.class);
        if (exprCtx != null) {
            final IFrameDMContext frameCtx = DMContexts.getAncestorOfType(exprCtx, IFrameDMContext.class);
            
            // First retrieve the stack depth, needed to properly calculate
            // the frame index that is used by the PDAVarCommand. 
            fStack.getStackDepth(
                frameCtx, 0,
                new DataRequestMonitor<Integer>(getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        // Calculate the frame index.
                        int frameId = getData() - frameCtx.getLevel() - 1;
                        
                        // Send the command to evaluate the variable.
                        fCommandCache.execute(
                            new PDAVarCommand(fCommandControl.getProgramDMContext(), frameId, exprCtx.getExpression()), 
                            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    rm.setData(new FormattedValueDMData(getData().fResponseText));
                                    rm.done();
                                }
                            });        
                    }
                });
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid expression context " + formattedCtx);
            rm.done();
        }
    }

    public void writeExpression(final IExpressionDMContext exprCtx, final String exprValue, String formatId, 
        final RequestMonitor rm) 
    {
        if (exprCtx instanceof ExpressionDMContext) {
            final IFrameDMContext frameCtx = DMContexts.getAncestorOfType(exprCtx, IFrameDMContext.class);
            
            // Similarly to retrieving the variable, retrieve the 
            // stack depth first.
            fStack.getStackDepth(
                frameCtx, 0,
                new DataRequestMonitor<Integer>(getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        // Calculate the frame index.
                        int frameId = getData() - frameCtx.getLevel() - 1;
                        
                        // Send the "write" command to PDA debugger
                        fCommandCache.execute(
                            new PDASetVarCommand(fCommandControl.getProgramDMContext(), frameId, exprCtx.getExpression(), exprValue), 
                            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    getSession().dispatchEvent(new ExpressionChangedDMEvent(exprCtx), getProperties());
                                    rm.done();
                                }
                            });
                    }
                });
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid expression context " + exprCtx);
            rm.done();
        }
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
        if (dmc instanceof IExpressionDMContext) {
            getExpressionData((IExpressionDMContext) dmc, (DataRequestMonitor<IExpressionDMData>) rm);
        } else if (dmc instanceof FormattedValueDMContext) {
            getFormattedExpressionValue((FormattedValueDMContext) dmc, (DataRequestMonitor<FormattedValueDMData>) rm);
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Unknown DMC type");
            rm.done();
        }
    }

    @DsfServiceEventHandler 
    public void eventDispatched(IResumedDMEvent e) {
        // Mark the cache as not available, so that data retrieval commands 
        // will fail.  Also reset the cache unless it was a step command.
        fCommandCache.setContextAvailable(e.getDMContext(), false);
        if (!e.getReason().equals(StateChangeReason.STEP)) {
            fCommandCache.reset();
        }
    }    


    @DsfServiceEventHandler 
    public void eventDispatched(ISuspendedDMEvent e) {
        // Enable sending commands to target and clear the cache.
        fCommandCache.setContextAvailable(e.getDMContext(), true);
        fCommandCache.reset();
    }
}
