/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.DsfCastToTypeSupport;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions2;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.ui.DsfDebugUITools;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterBitFieldVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterGroupVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.SyncRegisterDataAccess;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.DefaultVMContentProviderStrategy;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMModelProxy;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.update.AutomaticUpdatePolicy;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.core.IExpressionsListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.preference.IPreferenceStore;
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
    implements IExpressionsListener2
{
    private IExpressionVMNode[] fExpressionNodes;

    private IPropertyChangeListener fPreferencesListener = new IPropertyChangeListener() {
        @Override
		public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
			if (property.equals(IDsfDebugUIConstants.PREF_WAIT_FOR_VIEW_UPDATE_AFTER_STEP_ENABLE)) {
                IPreferenceStore store = DsfDebugUITools.getPreferenceStore();
                setDelayEventHandleForViewUpdate(store.getBoolean(property));
            }
        }
    };

    private IPropertyChangeListener fPresentationContextListener = new IPropertyChangeListener() {
        @Override
		public void propertyChange(PropertyChangeEvent event) {
            handleEvent(event);
        }        
    };

    public ExpressionVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
        super(adapter, context, session);
        
        context.addPropertyChangeListener(fPresentationContextListener);

        IPreferenceStore store = DsfDebugUITools.getPreferenceStore();
        store.addPropertyChangeListener(fPreferencesListener);
        setDelayEventHandleForViewUpdate(store.getBoolean(IDsfDebugUIConstants.PREF_WAIT_FOR_VIEW_UPDATE_AFTER_STEP_ENABLE));

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
        final List<IVMModelProxy> activeModelProxies= getActiveModelProxies();
        int count = activeModelProxies.size();
        if (count > 0) {
            return ((ExpressionVMProviderModelProxyStrategy)activeModelProxies.get(count - 1)).getDeltaFlagsForExpression(expression, event);
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
        if (IDsfDebugUIConstants.ID_EXPRESSION_HOVER.equals(getPresentationContext().getId())) {
        	SingleExpressionVMNode expressionManagerNode = new SingleExpressionVMNode(this);
        	addChildNodes(rootNode, new IVMNode[] { expressionManagerNode });
        } else {
            ExpressionManagerVMNode expressionManagerNode = new ExpressionManagerVMNode(this);
            addChildNodes(rootNode, new IVMNode[] {expressionManagerNode});
        }
        
        // Disabled expression node intercepts disabled expressions and prevents them from being
        // evaluated by other nodes.
        IExpressionVMNode disabledExpressionNode = new DisabledExpressionVMNode(this);
        
        /*
         *  The expression view wants to support fully all of the components of the register view.
         */
        IExpressionVMNode registerGroupNode = new RegisterGroupVMNode(this, getSession(), syncRegDataAccess);
        
        IExpressionVMNode registerNode = new RegisterVMNode(this, getSession(), syncRegDataAccess);
        addChildNodes(registerGroupNode, new IExpressionVMNode[] {registerNode});
        
        /*
         * Create the next level which is the bit-field level.
         */
        IVMNode bitFieldNode = new RegisterBitFieldVMNode(this, getSession(), syncRegDataAccess);
        addChildNodes(registerNode, new IVMNode[] { bitFieldNode });
        
        /*
         *  Create the support for the SubExpressions. Anything which is brought into the expressions
         *  view comes in as a fully qualified expression so we go directly to the SubExpression layout
         *  node.
         */
        VariableVMNode variableNode =  new VariableVMNode(this, getSession(), syncvarDataAccess);
        addChildNodes(variableNode, new IExpressionVMNode[] {variableNode});
        
        /*
         * Hook up IExpressions2 if it exists.
         */
        hookUpCastingSupport(syncvarDataAccess, variableNode);

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
        setExpressionNodes(new IExpressionVMNode[] {disabledExpressionNode, registerGroupNode, variableNode});
        
        /*
         *  Let the work know which is the top level node.
         */
        setRootNode(rootNode);
    }

	private void hookUpCastingSupport(final SyncVariableDataAccess syncvarDataAccess,
			final VariableVMNode variableNode) {
		 try {
            getSession().getExecutor().execute(new DsfRunnable() {
                @Override
				public void run() {
                    DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), getSession().getId());
                    IExpressions2 expressions2 = tracker.getService(IExpressions2.class);
                    if (expressions2 != null) {
                    	variableNode.setCastToTypeSupport(new DsfCastToTypeSupport(
                    			getSession(), ExpressionVMProvider.this, syncvarDataAccess));
                    }
                    tracker.dispose();
                }
            });
        } catch (RejectedExecutionException e) {
            // Session disposed, ignore.
        }
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
        DsfDebugUITools.getPreferenceStore().removePropertyChangeListener(fPreferencesListener);
        getPresentationContext().removePropertyChangeListener(fPresentationContextListener);
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
        return new IVMUpdatePolicy[] { new AutomaticUpdatePolicy(), new ExpressionsManualUpdatePolicy(), 
            new ExpressionsBreakpointHitUpdatePolicy() };
    }

    @Override
	public void expressionsAdded(IExpression[] expressions) {
        expressionsListChanged(ExpressionsChangedEvent.Type.ADDED, expressions, -1);
    }
    
    @Override
	public void expressionsRemoved(IExpression[] expressions) {
        expressionsListChanged(ExpressionsChangedEvent.Type.REMOVED, expressions, -1);
    }
    
    @Override
	public void expressionsInserted(IExpression[] expressions, int index) {
        expressionsListChanged(ExpressionsChangedEvent.Type.INSERTED, expressions, index);
    }

    @Override
	public void expressionsMoved(IExpression[] expressions, int index) {
        expressionsListChanged(ExpressionsChangedEvent.Type.MOVED, expressions, index);
    }
    
    @Override
	public void expressionsChanged(IExpression[] expressions) {
        expressionsListChanged(ExpressionsChangedEvent.Type.CHANGED, expressions, -1);
    }
    
    private void expressionsListChanged(ExpressionsChangedEvent.Type type, IExpression[] expressions, int index) {
        Set<Object> rootElements = new HashSet<Object>();
        for (IVMModelProxy proxy : getActiveModelProxies()) {
            rootElements.add(proxy.getRootElement());
        }
        handleEvent(new ExpressionsChangedEvent(type, rootElements, expressions, index));
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
                @Override
				public void run() {
                    DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), getSession().getId());
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

    @Override
    public void update(IViewerInputUpdate update) {
        if (IDsfDebugUIConstants.ID_EXPRESSION_HOVER.equals(getPresentationContext().getId())) {
        	Object input = update.getElement();
        	if (input instanceof IExpressionDMContext) {
        		IExpressionDMContext dmc = (IExpressionDMContext) input;
        		SingleExpressionVMNode vmNode = (SingleExpressionVMNode) getChildVMNodes(getRootVMNode())[0];
        		vmNode.setExpression(dmc);
				final IDMVMContext viewerInput= vmNode.createVMContext(dmc);

				// provide access to viewer (needed by details pane)
	            getPresentationContext().setProperty("__viewerInput", viewerInput); //$NON-NLS-1$
	            
                update.setInputElement(viewerInput);
        		update.done();
        		return;
        	}
        }
        super.update(update);
    }
}
