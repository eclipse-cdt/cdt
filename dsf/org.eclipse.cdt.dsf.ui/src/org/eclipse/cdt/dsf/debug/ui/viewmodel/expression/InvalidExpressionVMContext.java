/*******************************************************************************
 *  Copyright (c) 2009, 2010 Wind River Systems and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;

/**
 * VMC of an expression object that failed to get parsed by any of the 
 * configured expression layout nodes.  It is only used to display an
 * error message in the view, and to allow the user to edit the 
 * expression.
 * <p>
 * Note: VM Nodes using this invalid expression VM context should 
 * provide a cell modifier to edit the expressions.  The cell modifier 
 * should subclass {@link WatchExpressionCellModifier}.
 * </p> 
 * 
 * @since 1.1
 */
public class InvalidExpressionVMContext extends AbstractVMContext implements IElementLabelProvider {
    
    final private IExpression fExpression;
    
    public InvalidExpressionVMContext(IVMNode node, IExpression expression) {
        super(node);
        fExpression = expression;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" }) 
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(fExpression.getClass())) {
            return fExpression;
        } else {
            return super.getAdapter(adapter);
        }
    }
    
    public IExpression getExpression() {
        return fExpression;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof InvalidExpressionVMContext && ((InvalidExpressionVMContext)obj).fExpression.equals(fExpression);
    }
    
    @Override
    public int hashCode() {
        return fExpression.hashCode();
    }
    
    /**
     * Updates the label for the InvalidExpressionVMC.
     */
    @Override
	public void update(ILabelUpdate[] updates) {
        for (ILabelUpdate update : updates) {
            String[] columnIds = update.getColumnIds() != null ? 
                update.getColumnIds() : new String[] { IDebugVMConstants.COLUMN_ID__NAME };
                
            for (int i = 0; i < columnIds.length; i++) {
                if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnIds[i])) {
                    update.setLabel(getExpression().getExpressionText(), i);
                    update.setImageDescriptor(DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_EXPRESSION ), i);
                } else if (IDebugVMConstants.COLUMN_ID__NAME.equals(columnIds[i])) {
                    update.setLabel(getExpression().getExpressionText(), i);
                    update.setImageDescriptor(DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_EXPRESSION ), i);
                } else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(columnIds[i])) {
                    update.setLabel(MessagesForExpressionVM.ExpressionManagerLayoutNode__invalidExpression_valueColumn_label, i);
                } else {
                    update.setLabel("", i); //$NON-NLS-1$
                }
                update.setFontData(JFaceResources.getFontDescriptor(IDebugUIConstants.PREF_VARIABLE_TEXT_FONT).getFontData()[0], i);            
            }

            update.done();
        }
    }

}