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
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat.FormattedValuePreferenceStore;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat.IFormattedValuePreferenceStore;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.register.RegisterBitFieldVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.register.RegisterGroupVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.register.RegisterVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.register.SyncRegisterDataAccess;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.update.BreakpointHitUpdatePolicy;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.variable.VariableVMNode;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.ICachingService;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.DefaultVMContentProviderStrategy;
import org.eclipse.dd.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMModelProxy;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.update.AutomaticUpdatePolicy;
import org.eclipse.dd.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.dd.dsf.ui.viewmodel.update.ManualUpdatePolicy;
import org.eclipse.dd.dsf.ui.viewmodel.update.UserEditEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.core.IExpressionsListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreePath;

/**
 * The expression provider is used to populate the contents of the expressions 
 * view.  The node hierarchy in this view is a little different than in a typical 
 * provider: the expression manager node should be registered as the single child
 * of the root node and no nodes should be registered as children of expression node.
 * Instead the top level expression nodes should be registered with a call to 
 * {@link #setExpressionNodes(IExpressionVMNode[])}.  And each expression node can
 * have its own sub-hierarchy of elements as needed.  However all nodes configured
 * with this provider (with the exception of the root and the expression manager) 
 * should implement {@link IExpressionVMNode}.
 */ 
@SuppressWarnings("restriction")
public class ExpressionVMProvider extends AbstractDMVMProvider 
    implements IPropertyChangeListener, IExpressionsListener2
{
    /**
     * Object representing a change in configured expressions.  This event is 
     * object is used when generating a model delta.
     */
    public static class ExpressionsChangedEvent extends UserEditEvent {
        enum Type {ADDED, CHANGED, REMOVED, MOVED, INSERTED}
        public final Type fType;
        public ExpressionsChangedEvent(Type type, Set<Object> elements) {
            super(elements);
            fType = type;
        }
    }
 
    private IExpressionVMNode[] fExpressionNodes;
    
    public ExpressionVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
        super(adapter, context, session);
        
        context.addPropertyChangeListener(this);
        
        // The VM provider has to handle all events that result in model deltas.  
        // Add the provider as listener to expression changes events.
        DebugPlugin.getDefault().getExpressionManager().addExpressionListener(this);
        
        configureLayout();
    }

    @Override
    protected DefaultVMContentProviderStrategy createContentStrategy() {
        return new ExpressionVMProviderContentStragegy(this);
    }
    
    @Override
    protected IVMModelProxy createModelProxyStrategy(Object rootElement) {
        return new ExpressionVMProviderModelProxyStrategy(this, rootElement);
    }

    /**
     * Updates the given expression element.  This method is used by the 
     * expression manager node to obtain a view model element based on the
     * {@link IExpression} retrieved from the expression manager.  The 
     * implementation of this method (which is in the content strategy), 
     * checks the configured expression nodes to see which one can 
     * process the given expression, when it finds it it delegates
     * to that expression node's {@link IExpressionVMNode#update(IExpressionUpdate)}
     * method. 
     * @param update Expression update to process.
     */
    public void update(IExpressionUpdate update) {
        ((ExpressionVMProviderContentStragegy)getContentStrategy()).update(update);
    }
    
    /**
     * Retrieves the delta flags that can be generated for the given expression
     * and the given event.  This method is used by the 
     * expression manager node to obtain the delta flags based on the
     * {@link IExpression} retrieved from the expression manager.  The 
     * implementation of this method (which is in the model proxy strategy), 
     * checks the configured expression nodes to see which one can 
     * process the given expression, when it finds it it delegates
     * to that expression node's {@link IExpressionVMNode#getDeltaFlagsForExpression(IExpression, Object)}
     * method.  
     */
    public int getDeltaFlagsForExpression(IExpression expression, Object event) {
        // Workaround: find the first active proxy and use it.
        if (!getActiveModelProxies().isEmpty()) {
            return ((ExpressionVMProviderModelProxyStrategy)getActiveModelProxies().get(0)).getDeltaFlagsForExpression(expression, event);
        }
        return 0;
    }
    
    /**
     * Builds the model delta based on the given expression
     * and the given event.  This method is used by the 
     * expression manager to build the delta based on the
     * {@link IExpression} retrieved from the expression manager.  The 
     * implementation of this method (which is in the model proxy strategy), 
     * checks the configured expression nodes to see which one can 
     * process the given expression, when it finds it it delegates
     * to that expression node's {@link IExpressionVMNode#buildDeltaForExpression(IExpression, int, Object, ModelDelta, TreePath, RequestMonitor)}
     * and {@link IExpressionVMNode#buildDeltaForExpressionElement(Object, int, Object, ModelDelta, RequestMonitor)
     * methods.  
     */
    public void buildDeltaForExpression(final IExpression expression, final int expressionElementIdx, final Object event, 
        final VMDelta parentDelta, final TreePath path, final RequestMonitor rm) 
    {
        // Workaround: find the first active proxy and use it.
        if (!getActiveModelProxies().isEmpty()) {
            ((ExpressionVMProviderModelProxyStrategy)getActiveModelProxies().get(0)).buildDeltaForExpression(
                expression, expressionElementIdx, event, parentDelta, path, rm);
        } else {
            rm.done();
        }
    }
    
    /**
     * Configures the given nodes as the top-level expression nodes.
     */
    protected void setExpressionNodes(IExpressionVMNode[] nodes) {
        fExpressionNodes = nodes;
        
        // Call the base class to make sure that the nodes are also 
        // returned by the getAllNodes method.
        for (IExpressionVMNode node : nodes) {
            addNode(node);
        }
    }

    /**
     * Returns the list of configured top-level expression nodes.
     * @return
     */
    public IExpressionVMNode[] getExpressionNodes() {
        return fExpressionNodes;
    }
    
    /**
     * Configures the nodes of this provider.  This method may be over-ridden by
     * sub classes to create an alternate configuration in this provider.
     */
    protected void configureLayout() {
    	
    	IFormattedValuePreferenceStore prefStore = FormattedValuePreferenceStore.getDefault();
        
        /*
         *  Allocate the synchronous data providers.
         */
        SyncRegisterDataAccess syncRegDataAccess = new SyncRegisterDataAccess(getSession());
        SyncVariableDataAccess syncvarDataAccess = new SyncVariableDataAccess(getSession()) ;
        
        /*
         *  Create the top level node which provides the anchor starting point.
         */
        IRootVMNode rootNode = new RootDMVMNode(this); 
        
        /*
         * Now the Over-arching management node.
         */
        ExpressionManagerVMNode expressionManagerNode = new ExpressionManagerVMNode(this);
        addChildNodes(rootNode, new IVMNode[] {expressionManagerNode});
        
        /*
         *  The expression view wants to support fully all of the components of the register view.
         */
        IExpressionVMNode registerGroupNode = new RegisterGroupVMNode(this, getSession(), syncRegDataAccess);
        
        IExpressionVMNode registerNode = new RegisterVMNode(prefStore, this, getSession(), syncRegDataAccess);
        addChildNodes(registerGroupNode, new IExpressionVMNode[] {registerNode});
        
        /*
         * Create the next level which is the bit-field level.
         */
        IVMNode bitFieldNode = new RegisterBitFieldVMNode(prefStore, this, getSession(), syncRegDataAccess);
        addChildNodes(registerNode, new IVMNode[] { bitFieldNode });
        
        /*
         *  Create the support for the SubExpressions. Anything which is brought into the expressions
         *  view comes in as a fully qualified expression so we go directly to the SubExpression layout
         *  node.
         */
        IExpressionVMNode variableNode =  new VariableVMNode(prefStore, this, getSession(), syncvarDataAccess);
        addChildNodes(variableNode, new IExpressionVMNode[] {variableNode});
        
        /*
         *  Tell the expression node which sub-nodes it will directly support.  It is very important
         *  that the variables node be the last in this chain.  The model assumes that there is some
         *  form of metalanguage expression syntax which each  of the nodes evaluates and decides if
         *  they are dealing with it or not. The variables node assumes that the expression is fully
         *  qualified and there is no analysis or subdivision of the expression it will parse. So it
         *  it currently the case that the location of the nodes within the array being passed in is
         *  the order of search/evaluation. Thus variables wants to be last. Otherwise it would just
         *  assume what it was passed was for it and the real node which wants to handle it would be
         *  left out in the cold.
         */
        setExpressionNodes(new IExpressionVMNode[] {registerGroupNode, variableNode});
        
        /*
         *  Let the work know which is the top level node.
         */
        setRootNode(rootNode);
    }
    
    /**
     * Finds the expression node which can parse the given expression.  This 
     * method is used by the expression content and model proxy strategies.
     * 
     * @param parentNode The parent of the nodes to search.  If <code>null</code>,
     * then the top level expressions will be searched.
     * @param expression The expression object.
     * @return The matching expression node.
     */
    public IExpressionVMNode findNodeToParseExpression(IExpressionVMNode parentNode, IExpression expression) {
        IVMNode[] childNOdes; 
        if (parentNode == null) {
            childNOdes = getExpressionNodes();
        } else {
            childNOdes = getChildVMNodes(parentNode);
        }
        for (IVMNode childNode : childNOdes) {
            if (childNode instanceof IExpressionVMNode) {
                IExpressionVMNode childExpressionNode = (IExpressionVMNode)childNode;
                if (childExpressionNode.canParseExpression(expression)) {
                    return childExpressionNode;
                } else if (!childExpressionNode.equals(parentNode)) {
                    // The above check is to make sure that child isn't the same as 
                    // parent to avoid recursive loops.
                    IExpressionVMNode matchingNode = 
                        findNodeToParseExpression(childExpressionNode, expression);
                    if (matchingNode != null) {
                        return matchingNode;
                    }
                }
            }
        }
        return null;
    }


    @Override
    public void dispose() {
        DebugPlugin.getDefault().getExpressionManager().removeExpressionListener(this);
        getPresentationContext().removePropertyChangeListener(this);
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

    @Override
    protected IVMUpdatePolicy[] createUpdateModes() {
        return new IVMUpdatePolicy[] { new AutomaticUpdatePolicy(), new ManualUpdatePolicy(), new BreakpointHitUpdatePolicy() };
    }

    public void propertyChange(PropertyChangeEvent event) {
        handleEvent(event);
    }
    
    public void expressionsAdded(IExpression[] expressions) {
        expressionsListChanged(ExpressionsChangedEvent.Type.ADDED);
    }
    
    public void expressionsRemoved(IExpression[] expressions) {
        expressionsListChanged(ExpressionsChangedEvent.Type.REMOVED);
    }
    
    public void expressionsInserted(IExpression[] expressions, int index) {
        expressionsListChanged(ExpressionsChangedEvent.Type.INSERTED);
    }

    public void expressionsMoved(IExpression[] expressions, int index) {
        expressionsListChanged(ExpressionsChangedEvent.Type.MOVED);
    }
    
    public void expressionsChanged(IExpression[] expressions) {
        Set<Object> expressionsSet = new HashSet<Object>();
        expressionsSet.addAll(Arrays.asList(expressions));
        handleEvent(new ExpressionsChangedEvent(ExpressionsChangedEvent.Type.CHANGED, expressionsSet));
    }
    
    private void expressionsListChanged(ExpressionsChangedEvent.Type type) {
        Set<Object> rootElements = new HashSet<Object>();
        for (IVMModelProxy proxy : getActiveModelProxies()) {
            rootElements.add(proxy.getRootElement());
        }
        handleEvent(new ExpressionsChangedEvent(type, rootElements));
    }
    
    @Override
    protected boolean canSkipHandlingEvent(Object newEvent, Object eventToSkip) {
        // To optimize the performance of the view when stepping rapidly, skip all 
        // other events when a suspended event is received, including older suspended
        // events.
        return newEvent instanceof ISuspendedDMEvent;
    }
    
    @Override
    public void refresh() {
        super.refresh();
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    DsfServicesTracker tracker = new DsfServicesTracker(DsfDebugUIPlugin.getBundleContext(), getSession().getId());
                    IExpressions expressionsService = tracker.getService(IExpressions.class);
                    if (expressionsService instanceof ICachingService) {
                        ((ICachingService)expressionsService).flushCache(null);
                    }
                    IRegisters registerService = tracker.getService(IRegisters.class);
                    if (registerService instanceof ICachingService) {
                        ((ICachingService)registerService).flushCache(null);
                    }
                    tracker.dispose();
                }
            });
        } catch (RejectedExecutionException e) {
            // Session disposed, ignore.
        }
    }
}
