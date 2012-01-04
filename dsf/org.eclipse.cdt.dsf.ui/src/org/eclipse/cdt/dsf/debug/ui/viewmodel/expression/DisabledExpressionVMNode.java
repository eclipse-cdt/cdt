/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import java.text.MessageFormat;

import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Composite;

/**
 * Expression VM Node which handles displaying disabled expressions.
 * 
 * @since 2.1
 */
public class DisabledExpressionVMNode extends AbstractVMNode 
    implements IExpressionVMNode, IElementEditor
{

    /** Cached reference to a cell modifier for editing expression strings of disabled expressions */
    private WatchExpressionCellModifier fWatchExpressionCellModifier = new WatchExpressionCellModifier();
    

    private static class DisabledExpressionVMContext extends InvalidExpressionVMContext {

        private static final MessageFormat NO_COLUMN_FORMAT = new MessageFormat(MessagesForExpressionVM.DisabledExpressionVMNode_disabled_no_columns); 

        DisabledExpressionVMContext(IVMNode node, IExpression expression) {
            super(node, expression);
        }
        
        /**
         * Updates the label for the DisabledExpressionVMNode.
         */
        @Override
        public void update(ILabelUpdate[] updates) {
            for (ILabelUpdate update : updates) {
                if (update.getColumnIds() == null) {
                    update.setLabel(NO_COLUMN_FORMAT.format( new Object[] { getExpression().getExpressionText() }), 0); 
                    update.setImageDescriptor(DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_EXPRESSION ), 0);
                } else {
                    String[] columnIds = update.getColumnIds();
                        
                    for (int i = 0; i < update.getColumnIds().length; i++) {
                        if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnIds[i]) ||
                            IDebugVMConstants.COLUMN_ID__NAME.equals(columnIds[i])) 
                        {
                            update.setLabel(getExpression().getExpressionText(), i);
                            update.setImageDescriptor(DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_EXPRESSION ), i);
                        } else {
                            update.setLabel(MessagesForExpressionVM.DisabledExpressionVMNode_disabled_value, i);
                        }
                        update.setFontData(JFaceResources.getFontDescriptor(IDebugUIConstants.PREF_VARIABLE_TEXT_FONT).getFontData()[0], i);            
                    }
                }
                update.done();
            }
        }

    }
    
    public DisabledExpressionVMNode(IVMProvider provider) {
        super(provider);
    }
    
    @Override
    public boolean canParseExpression(IExpression expression) {
        return expression instanceof IWatchExpression && !((IWatchExpression)expression).isEnabled();
    }

    @Override
    public int getDeltaFlagsForExpression(IExpression expression, Object event) {
        return IModelDelta.NO_CHANGE;
    }

    @Override
    public int getDeltaFlags(Object event) {
        return IModelDelta.NO_CHANGE;
    }

    @Override
    public void buildDelta(Object event, VMDelta parent, int nodeOffset, RequestMonitor requestMonitor) {
        requestMonitor.done();
    }

    @Override
    public void update(IChildrenCountUpdate[] updates) {
        for (IViewerUpdate update : updates) {
            update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
            update.done();
        }
    }

    @Override
    public void update(IChildrenUpdate[] updates) {
        for (IViewerUpdate update : updates) {
            update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
            update.done();
        }
    }

    @Override
    public void update(IHasChildrenUpdate[] updates) {
        for (IViewerUpdate update : updates) {
            update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
            update.done();
        }
    }

    @Override
    public void update(IExpressionUpdate update) {
        update.setExpressionElement(new DisabledExpressionVMContext(this, update.getExpression()));
        update.done();
    }

    @Override
    public void buildDeltaForExpression(IExpression expression, int elementIdx, Object event, VMDelta parentDelta, 
        TreePath path, RequestMonitor rm) 
    {
        rm.done();
    }

    @Override
    public void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta,
        RequestMonitor rm) {
        rm.done();
    }
    
    @Override
    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnId)) {
            return new TextCellEditor(parent);
        } 
        return null;
    }
    
    @Override
    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        return fWatchExpressionCellModifier;
    }

}
