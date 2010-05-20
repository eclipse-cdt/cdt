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
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerCountingRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;

/**
 * This is the top-level view model node in the expressions view.  Its job is to:
 * <li>
 *   <ol> retrieve the {@link IExpression} objects from the global {@link IExpressionManager},</ol>
 *   <ol> retrieve the expression string from the <code>IExpression</code> object,</ol>
 *   <ol> then to call the configured expression nodes to parse the expression string.</ol>
 * </li>
 * <p>
 * This node is not intended to have any standard child nodes, therefore 
 * the implementation of {@link #setChildNodes(IVMNode[])} throws an exception.  
 * Instead users should call {@link #setExpressionNodes(IExpressionVMNode[])}
 * to configure the nodes that this node will delegate to when processing expressions.
 * </p> 
 */
public class ExpressionManagerVMNode extends AbstractVMNode
    implements IElementLabelProvider, IElementEditor
{
    /**
     * VMC for a new expression object to be added.  When user clicks on this node to 
     * edit it, he will create a new expression.
     */
    public class NewExpressionVMC extends AbstractVMContext {
        public NewExpressionVMC() {
            super(ExpressionManagerVMNode.this);
        }
        
        @Override
        @SuppressWarnings("rawtypes") 
        public Object getAdapter(Class adapter) {
            return super.getAdapter(adapter);
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof NewExpressionVMC;
        }
        
        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }

    /** Local reference to the global expression manager */ 
    private IExpressionManager fManager = DebugPlugin.getDefault().getExpressionManager();
    
    /** Cached reference to a cell modifier for editing expression strings of invalid expressions */
    private ICellModifier fWatchExpressionCellModifier = null;
    
    /**
     * @since 2.1
     * 
     * @return The cell modifier to be used when editing. If you need to provide
     *         a custom cell editor you would override this method.
     */
    protected ICellModifier createCellModifier() {
    	return new WatchExpressionCellModifier();
    }
    
    public ExpressionManagerVMNode(ExpressionVMProvider provider) {
        super(provider);
    }

    @Override
    public String toString() {
        return "ExpressionManagerVMNode";  //$NON-NLS-1$ 
    }
    
    private ExpressionVMProvider getExpressionVMProvider() {
        return (ExpressionVMProvider)getVMProvider();
    }

    public void update(IHasChildrenUpdate[] updates) {
        // Test availability of children based on whether there are any expressions 
        // in the manager.  We assume that the getExpressions() will just read 
        // local state data, so we don't bother using a job to perform this 
        // operation.
        for (int i = 0; i < updates.length; i++) {
            updates[i].setHasChilren(fManager.getExpressions().length != 0);
            updates[i].done();
        }
    }
    
    public void update(IChildrenCountUpdate[] updates) {
        for (IChildrenCountUpdate update : updates) {
            if (!checkUpdate(update)) continue;

            // We assume that the getExpressions() will just read local state data,
            // so we don't bother using a job to perform this operation.
            update.setChildCount(fManager.getExpressions().length + 1);
            update.done();
        }
    }
    
    public void update(final IChildrenUpdate[] updates) {
        for (IChildrenUpdate update : updates) {
            doUpdateChildren(update);
        }        
    }
    
    public void doUpdateChildren(final IChildrenUpdate update) {
        final IExpression[] expressions = fManager.getExpressions();
        
        // For each (expression) element in update, find the layout node that can 
        // parse it.  And for each expression that has a corresponding layout node, 
        // call IExpressionLayoutNode#getElementForExpression to generate a VMC.
        // Since the last is an async call, we need to create a multi-RM to wait
        // for all the calls to complete.
        final CountingRequestMonitor multiRm = new ViewerCountingRequestMonitor(getVMProvider().getExecutor(), update);
        int multiRmCount = 0;
        
        int lowOffset= update.getOffset();
        if (lowOffset < 0) {
        	lowOffset = 0;
        }
		int length= update.getLength();
		if (length <= 0) {
			length = expressions.length;
		}
		final int highOffset= lowOffset + length;
		for (int i = lowOffset; i < highOffset && i < expressions.length + 1; i++) {
            if (i < expressions.length) {
                multiRmCount++;
                final int childIndex = i;
                final IExpression expression = expressions[i];
                // getElementForExpression() accepts a IElementsUpdate as an argument.
                // Construct an instance of VMElementsUpdate which will call a 
                // the request monitor when it is finished.  The request monitor
                // will in turn set the element in the update argument in this method. 
                ((ExpressionVMProvider)getVMProvider()).update(
                    new VMExpressionUpdate(
                        update, expression,
                        new DataRequestMonitor<Object>(getVMProvider().getExecutor(), multiRm) {
                            @Override
                            protected void handleSuccess() {
                                update.setChild(getData(), childIndex);
                                multiRm.done();
                            } 
                            
                            @Override
                            protected void handleError() {
                                update.setChild(new InvalidExpressionVMContext(ExpressionManagerVMNode.this, expression), childIndex);                                
                                multiRm.done();
                            }
                        })
                    );
            } else {
                // Last element in the list of expressions is the "add new expression"
                // dummy entry.
                update.setChild(new NewExpressionVMC(), i);
            }
        }

        // If no expressions were parsed, we're finished.
        // Set the count to the counting RM.
        multiRm.setDoneCount(multiRmCount);
    }

    public void update(ILabelUpdate[] updates) {
        // The label update handler only handles labels for the invalid expression VMCs.
        // The expression layout nodes are responsible for supplying label providers 
        // for their VMCs.
        for (ILabelUpdate update : updates) {
            if (update.getElement() instanceof NewExpressionVMC) {
                updateNewExpressionVMCLabel(update, (NewExpressionVMC) update.getElement());
            } else {
                update.done();
            }
        }
    }

    /**
     * Updates the label for the NewExpressionVMC.
     */
    private void updateNewExpressionVMCLabel(ILabelUpdate update, NewExpressionVMC vmc) {
        String[] columnIds = update.getColumnIds() != null ? 
            update.getColumnIds() : new String[] { IDebugVMConstants.COLUMN_ID__NAME };
            
        for (int i = 0; i < columnIds.length; i++) {
            if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnIds[i])) {
                update.setLabel(MessagesForExpressionVM.ExpressionManagerLayoutNode__newExpression_label, i);
                // TODO: replace with an API image consant after bug 313828 is addressed.
                update.setImageDescriptor(DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_MONITOR_EXPRESSION), i);
                FontData fontData = JFaceResources.getFontDescriptor(IDebugUIConstants.PREF_VARIABLE_TEXT_FONT).getFontData()[0];
                fontData.setStyle(SWT.ITALIC);  // Bugzilla 287598: Distinguish 'Add new expression' entry from actual expressions                
                update.setFontData(fontData, i);
            } else {
                update.setLabel("", i); //$NON-NLS-1$
            }
        }
        
        
        update.done();
    }

    public int getDeltaFlags(Object event) {
        int retVal = 0;
        
        // Add a flag if the list of expressions in the global expression manager has changed.
        if (event instanceof ExpressionsChangedEvent) {
            retVal |= IModelDelta.ADDED | IModelDelta.REMOVED | IModelDelta.INSERTED | IModelDelta.CONTENT ;
        }

        for (IExpression expression : fManager.getExpressions()) {
            retVal |= getExpressionVMProvider().getDeltaFlagsForExpression(expression, event);
        }
        
        return retVal;
    }

    public void buildDelta(final Object event, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
        if (event instanceof ExpressionsChangedEvent) {
            buildDeltaForExpressionsChangedEvent((ExpressionsChangedEvent)event, parentDelta, nodeOffset, requestMonitor);
        } else {
        
            // For each expression, find its corresponding node and ask that
            // layout node for its delta flags for given event.  If there are delta flags to be 
            // generated, call the asynchronous method to do so.
            CountingRequestMonitor multiRm = new CountingRequestMonitor(getExecutor(), requestMonitor);
            
            int buildDeltaForExpressionCallCount = 0;
            
            IExpression[] expressions = fManager.getExpressions();
            for (int i = 0; i < expressions.length; i++ ) {
                int flags = getExpressionVMProvider().getDeltaFlagsForExpression(expressions[i], event);
                // If the given expression has no delta flags, skip it.
                if (flags == IModelDelta.NO_CHANGE) continue;
    
                int elementOffset = nodeOffset >= 0 ? nodeOffset + i : -1;
                getExpressionVMProvider().buildDeltaForExpression(
                    expressions[i], elementOffset, event, parentDelta, getTreePathFromDelta(parentDelta), 
                    new RequestMonitor(getExecutor(), multiRm));
                buildDeltaForExpressionCallCount++;
            }
            
            multiRm.setDoneCount(buildDeltaForExpressionCallCount);
        }
    }
    
    private void buildDeltaForExpressionsChangedEvent(ExpressionsChangedEvent event, VMDelta parentDelta, 
        int nodeOffset, RequestMonitor requestMonitor) 
    {
        CountingRequestMonitor multiRm = new CountingRequestMonitor(getExecutor(), requestMonitor);
        for (int i = 0; i < event.getExpressions().length; i++) {
            int expIndex = event.getIndex() != -1 
                ? nodeOffset + event.getIndex() + i 
                : -1; 
            getExpressionVMProvider().buildDeltaForExpression(
                event.getExpressions()[i], expIndex, event, parentDelta, getTreePathFromDelta(parentDelta), 
                new RequestMonitor(getExecutor(), multiRm));                
        }
        multiRm.setDoneCount(event.getExpressions().length);
    }
        
    private TreePath getTreePathFromDelta(IModelDelta delta) {
        List<Object> elementList = new LinkedList<Object>();
        IModelDelta listDelta = delta;
        elementList.add(0, listDelta.getElement());
        while (listDelta.getParentDelta() != null) {
            elementList.add(0, listDelta.getElement());
            listDelta = listDelta.getParentDelta();
        }
        return new TreePath(elementList.toArray());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellEditor(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String, java.lang.Object, org.eclipse.swt.widgets.Composite)
     */
    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnId)) {
            return new TextCellEditor(parent);
        } 
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellModifier(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
     */
    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        if ( fWatchExpressionCellModifier == null ) {
        	fWatchExpressionCellModifier = createCellModifier();
        }
        return fWatchExpressionCellModifier;
    }
}
