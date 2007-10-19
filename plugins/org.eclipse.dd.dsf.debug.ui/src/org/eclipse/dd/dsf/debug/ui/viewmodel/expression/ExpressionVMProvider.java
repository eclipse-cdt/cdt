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
package org.eclipse.dd.dsf.debug.ui.viewmodel.expression;

import org.eclipse.dd.dsf.debug.ui.viewmodel.dm.AbstractDebugDMVMProviderWithCache;
import org.eclipse.dd.dsf.debug.ui.viewmodel.formatsupport.FormattedValuePreferenceStore;
import org.eclipse.dd.dsf.debug.ui.viewmodel.register.RegisterGroupLayoutNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.register.RegisterLayoutNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.register.SyncRegisterDataAccess;
import org.eclipse.dd.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.dd.dsf.debug.ui.viewmodel.variable.VariableLayoutNode;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.dm.DMVMRootLayoutNode;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * 
 */
@SuppressWarnings("restriction")
public class ExpressionVMProvider extends AbstractDebugDMVMProviderWithCache implements IExpressionsListener
{
    public static class ExpressionsChangedEvent {
        enum Type {ADDED, CHANGED, REMOVED}
        public final Type fType;
        public final IExpression[] fExpressions;
        public ExpressionsChangedEvent(Type type, IExpression[] expressions) {
            fType = type;
            fExpressions = expressions;
        }
    }
    
    public ExpressionVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
        super(adapter, context, session);
        
        // The VM provider has to handle all events that result in model deltas.  
        // Add the provider as listener to expression changes events.
        DebugPlugin.getDefault().getExpressionManager().addExpressionListener(this);

        configureLayout();
    }
    
    protected void configureLayout() {
        
        /*
         *  Allocate the synchronous data providers.
         */
        SyncRegisterDataAccess syncRegDataAccess = new SyncRegisterDataAccess(getSession());
        SyncVariableDataAccess syncvarDataAccess = new SyncVariableDataAccess(getSession()) ;
        
        /*
         *  Create the top level node which provides the anchor starting point.
         */
        IVMRootLayoutNode debugViewSelectionNode = new DMVMRootLayoutNode(this); 
        
        /*
         * Now the Overarching management node.
         */
        ExpressionManagerLayoutNode expressionManagerNode = new ExpressionManagerLayoutNode(this);
        debugViewSelectionNode.setChildNodes(new IVMLayoutNode[] {expressionManagerNode});
        
        /*
         *  The expression view wants to support fully all of the components of the register view.
         */
        IExpressionLayoutNode registerGroupNode = new RegisterGroupLayoutNode(this, getSession(), syncRegDataAccess);
        IVMLayoutNode registerNode = new RegisterLayoutNode(FormattedValuePreferenceStore.getDefault(), this, getSession(), syncRegDataAccess);
        registerGroupNode.setChildNodes(new IVMLayoutNode[] { registerNode });
        
        /*
         *  Create the support for the SubExpressions. Anything which is brought into the expressions
         *  view comes in as a fully qualified expression so we go directly to the SubExpression layout
         *  node.
         */
        IExpressionLayoutNode subExpressioNode = 
            
            new VariableLayoutNode(FormattedValuePreferenceStore.getDefault(), this, getSession(), syncvarDataAccess);
        
        /*
         *  Tell the expression node which subnodes  it will directly support.  It is very important
         *  that the variables node be the last in this chain.  The model assumes that there is some
         *  form of metalanguage expression syntax which each  of the nodes evaluates and decides if
         *  they are dealing with it or not. The variables node assumes that the expression is fully
         *  qualified and there is no analysis or subdivision of the expression it will parse. So it
         *  it currently the case that the location of the nodes within the array being passed in is
         *  the order of search/evaluation. Thus variables wants to be last. Otherwise it would just
         *  assume what it was passed was for it and the real node which wants to handle it would be
         *  left out in the cold.
         */
        expressionManagerNode.setExpressionLayoutNodes(new IExpressionLayoutNode[] { registerGroupNode, subExpressioNode });
        
        /*
         *  Let the work know which is the top level node.
         */
        setRootLayoutNode(debugViewSelectionNode);
    }

    @Override
    public void dispose() {
        DebugPlugin.getDefault().getExpressionManager().removeExpressionListener(this);
        super.dispose();
    }
    
    @Override
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        return new ExpressionColumnPresentation();
    }
    
    @Override
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return ExpressionColumnPresentation.ID;
    }

    /**
     * Override this operation to avoid the standard test of isOurLayoutNode(), 
     * which does not take into account {@link ExpressionManagerLayoutNode.setExpressionLayoutNodes}
     * nodes.
     */
    @Override
    protected IVMLayoutNode getLayoutNodeForElement(Object element) {
        /*
         * First check to see if the parent object is the root object of the 
         * hierarchy.  If that's the case, then retrieve the correcponding
         * root VMC from the root node, and pass this root vmc to the root's 
         * child layout nodes.
         */
        IVMRootLayoutNode rootLayoutNode = getRootLayoutNode();
        if (rootLayoutNode == null) {
            return null;
        } 
        else if (element.equals(getRootElement())) {
            return rootLayoutNode;
        } 
        else if (element instanceof IVMContext){
            return ((IVMContext)element).getLayoutNode();
        } 
        return null;
    }
    
    public void expressionsAdded(IExpression[] expressions) {
        handleEvent(new ExpressionsChangedEvent(ExpressionsChangedEvent.Type.ADDED, expressions));
    }
    
    public void expressionsChanged(IExpression[] expressions) {
        handleEvent(new ExpressionsChangedEvent(ExpressionsChangedEvent.Type.CHANGED, expressions));
    }
    
    public void expressionsRemoved(IExpression[] expressions) {
        handleEvent(new ExpressionsChangedEvent(ExpressionsChangedEvent.Type.REMOVED, expressions));
    }
}
