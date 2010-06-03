/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.service;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAChildrenCommand;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDACommandResult;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAListResult;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDASetVarCommand;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAVarCommand;
import org.osgi.framework.BundleContext;

/**
 * 
 */
public class PDAExpressions extends AbstractDsfService implements ICachingService, IExpressions {

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
    private PDAStack fStack;

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
        fStack = getServicesTracker().getService(PDAStack.class);
        fCommandCache = new CommandCache(getSession(), fCommandControl);
        fCommandCache.setContextAvailable(fCommandControl.getContext(), true);

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
        PDAThreadDMContext threadCtx = DMContexts.getAncestorOfType(ctx, PDAThreadDMContext.class);
        if (threadCtx != null) {
            // The PDA debugger can only evaluate variables as expressions and only
            // in context of a frame, so if a frame is not given, create a top-level frame.
            IFrameDMContext frameCtx = DMContexts.getAncestorOfType(ctx, IFrameDMContext.class);
            if (frameCtx == null) {
                frameCtx = fStack.getFrameDMContext(threadCtx, 0);
            }
            
            return new ExpressionDMContext(getSession().getId(), frameCtx, expression);
        } 
            
        // If the thread cannot be found in context, return an "invalid" 
        // expression context, because a null return value is not allowed.
        // Evaluating an invalid expression context will always yield an 
        // error.
        return new InvalidExpressionDMContext(getSession().getId(), ctx, expression);
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

    public void getSubExpressionCount(final IExpressionDMContext exprCtx, final DataRequestMonitor<Integer> rm) {
        if (exprCtx instanceof ExpressionDMContext) {
            final PDAThreadDMContext threadCtx = DMContexts.getAncestorOfType(exprCtx, PDAThreadDMContext.class);
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
                            new PDAChildrenCommand(threadCtx, frameId, exprCtx.getExpression()), 
                            new DataRequestMonitor<PDAListResult>(getExecutor(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    rm.setData(getData().fValues.length);
                                    rm.done();
                                }
                            });        
                    }
                });
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid context");
        }
    }

    public void getSubExpressions(IExpressionDMContext exprCtx, DataRequestMonitor<IExpressionDMContext[]> rm) {
        getSubExpressions(exprCtx, -1, -1, rm);
    }

    public void getSubExpressions(final IExpressionDMContext exprCtx, final int startIndexArg, final int lengthArg,
        final DataRequestMonitor<IExpressionDMContext[]> rm) 
    {
        if (exprCtx instanceof ExpressionDMContext) {
            final PDAThreadDMContext threadCtx = DMContexts.getAncestorOfType(exprCtx, PDAThreadDMContext.class);
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
                            new PDAChildrenCommand(threadCtx, frameId, exprCtx.getExpression()), 
                            new DataRequestMonitor<PDAListResult>(getExecutor(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    int start = startIndexArg > 0 ? startIndexArg : 0;
                                    int end = lengthArg > 0 ? (start + lengthArg) : getData().fValues.length;
                                    IExpressionDMContext[] contexts = new IExpressionDMContext[end - start];                                     
                                    for (int i = start; i < end && i < getData().fValues.length; i++) {
                                        contexts[i] = new ExpressionDMContext(
                                            getSession().getId(), frameCtx, getData().fValues[i]);
                                    }
                                    rm.setData(contexts);
                                    rm.done();
                                }
                            });        
                    }
                });
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid context");
        }
    }

    public void getAvailableFormats(IFormattedDataDMContext dmc, final DataRequestMonitor<String[]> rm) {
        getFormattedExpressionValue(
            new FormattedValueDMContext(this, dmc, NATURAL_FORMAT),
            new DataRequestMonitor<FormattedValueDMData>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    try {
                        Integer.parseInt(getData().getFormattedValue());
                        rm.setData(new String[] { DECIMAL_FORMAT, HEX_FORMAT, DECIMAL_FORMAT, OCTAL_FORMAT, BINARY_FORMAT });                        
                        rm.done();
                    } catch (NumberFormatException e) {
                        rm.setData(new String[] { STRING_FORMAT });
                        rm.done();
                    }
                }
                
                @Override
                protected void handleErrorOrWarning() {
                    rm.setData(new String[] { STRING_FORMAT });
                    rm.done();
                }
            });
    }

    public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext exprCtx, String formatId) {
        // Creates a context that can be used to retrieve a formatted value.
        return new FormattedValueDMContext(this, exprCtx, formatId);
    }

    public void getFormattedExpressionValue(FormattedValueDMContext formattedCtx, 
        final DataRequestMonitor<FormattedValueDMData> rm) 
    {
        final String formatId = formattedCtx.getFormatID();
        final ExpressionDMContext exprCtx = DMContexts.getAncestorOfType(formattedCtx, ExpressionDMContext.class);
        if (exprCtx != null) {
            final PDAThreadDMContext threadCtx = DMContexts.getAncestorOfType(exprCtx, PDAThreadDMContext.class);
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
                            new PDAVarCommand(threadCtx, frameId, exprCtx.getExpression()), 
                            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    if (NATURAL_FORMAT.equals(formatId) || STRING_FORMAT.equals(formatId)) {
                                        rm.setData(new FormattedValueDMData(getData().fResponseText));
                                        rm.done();
                                    } else {                                        
                                        int result;
                                        try {
                                            int intResult = Integer.parseInt(getData().fResponseText);
                                            String formattedResult = "";
                                            if (HEX_FORMAT.equals(formatId)) {
                                                formattedResult = Integer.toHexString(intResult);
                                                StringBuffer prefix = new StringBuffer("0x");
                                                for (int i = 0; i < 8 - formattedResult.length(); i++) {
                                                    prefix.append('0');
                                                }
                                                prefix.append(formattedResult);
                                                formattedResult = prefix.toString();
                                            } else if (OCTAL_FORMAT.equals(formatId)) {
                                                formattedResult = Integer.toOctalString(intResult);
                                                StringBuffer prefix = new StringBuffer("0c");
                                                for (int i = 0; i < 16 - formattedResult.length(); i++) {
                                                    prefix.append('0');
                                                }
                                                prefix.append(formattedResult);
                                                formattedResult = prefix.toString();
                                            } else if (BINARY_FORMAT.equals(formatId)) {
                                                formattedResult = Integer.toBinaryString(intResult);
                                                StringBuffer prefix = new StringBuffer("0b");
                                                for (int i = 0; i < 32 - formattedResult.length(); i++) {
                                                    prefix.append('0');
                                                }
                                                prefix.append(formattedResult);
                                                formattedResult = prefix.toString();
                                            } else if (DECIMAL_FORMAT.equals(formatId)) {
                                                formattedResult = Integer.toString(intResult);                                                
                                            } else {
                                                PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid format");
                                            }
                                            rm.setData(new FormattedValueDMData(formattedResult));
                                            rm.done();
                                        } catch (NumberFormatException e) {
                                            PDAPlugin.failRequest(rm, REQUEST_FAILED, "Cannot format value");
                                        }
                                    }
                                }
                            }); 
                    }
                });
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid expression context " + formattedCtx);
        }
    }

    
    public void writeExpression(final IExpressionDMContext exprCtx, final String exprValue, String formatId, 
        final RequestMonitor rm) 
    {
        writeExpression(exprCtx, exprValue, formatId, true, rm);
    }
    
    /**
     * Method to write an expression, with an additional parameter to suppress
     * issuing of the expression changed event. 
     * @see IExpressions#writeExpression(org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext, String, String, RequestMonitor)
     */
    public void writeExpression(final IExpressionDMContext exprCtx, String formattedExprValue, String formatId, 
        final boolean sendEvent, final RequestMonitor rm) 
    {
        String value = null;
        try {
            int intValue = 0;
            if (HEX_FORMAT.equals(formatId)) {
                if (formattedExprValue.startsWith("0x")) formattedExprValue = formattedExprValue.substring(2); 
                value = Integer.toString( Integer.parseInt(formattedExprValue, 16) );
            } else if (DECIMAL_FORMAT.equals(formatId)) {
                value = Integer.toString( Integer.parseInt(formattedExprValue, 10) );
            } else if (OCTAL_FORMAT.equals(formatId)) {
                if (formattedExprValue.startsWith("0c")) formattedExprValue = formattedExprValue.substring(2); 
                value = Integer.toString( Integer.parseInt(formattedExprValue, 8) );
            } else if (BINARY_FORMAT.equals(formatId)) {
                if (formattedExprValue.startsWith("0b")) formattedExprValue = formattedExprValue.substring(2); 
                value = Integer.toString( Integer.parseInt(formattedExprValue, 2) ); 
            }
        } catch (NumberFormatException e) {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Value not formatted properly");
            return;
        }
        
        final String exprValue = value != null ? value : formattedExprValue;
        
        
        if (exprCtx instanceof ExpressionDMContext) {
            final PDAThreadDMContext threadCtx = DMContexts.getAncestorOfType(exprCtx, PDAThreadDMContext.class);
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
                            new PDASetVarCommand( threadCtx, frameId, exprCtx.getExpression(), exprValue), 
                            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    if (sendEvent) {
                                        getSession().dispatchEvent(new ExpressionChangedDMEvent(exprCtx), getProperties());
                                    }
                                    // An expression changed, clear the cache corresponding to 
                                    // this event.  Since the var evaluate commands, use the thread
                                    // context, we have to clear all the cache entries for that thread.
                                    fCommandCache.reset(DMContexts.getAncestorOfType(exprCtx, PDAThreadDMContext.class));
                                    rm.done();
                                }
                            });
                    }
                });
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid expression context " + exprCtx);
        }
    }

    @DsfServiceEventHandler 
    public void eventDispatched(IResumedDMEvent e) {
        // Mark the cache as not available, so that data retrieval commands 
        // will fail.  Also reset the cache unless it was a step command.
        fCommandCache.setContextAvailable(e.getDMContext(), false);
        if (!e.getReason().equals(StateChangeReason.STEP)) {
            fCommandCache.reset(e.getDMContext());
        }
    }    


    @DsfServiceEventHandler 
    public void eventDispatched(ISuspendedDMEvent e) {
        // Enable sending commands to target and clear the cache.
        fCommandCache.setContextAvailable(e.getDMContext(), true);
        fCommandCache.reset(e.getDMContext());
    }
    
    @DsfServiceEventHandler 
    public void eventDispatched(ExpressionChangedDMEvent e) {
        // An expression changed, clear the cache corresponding to 
        // this event.  Since the var evaluate commands, use the thread
        // context, we have to clear all the cache entries for that thread.
        fCommandCache.reset(DMContexts.getAncestorOfType(e.getDMContext(), PDAThreadDMContext.class));
    }    

    public void flushCache(IDMContext context) {
        fCommandCache.reset(context);
    }
}
