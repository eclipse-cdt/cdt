/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *     Axel Mueller            - Bug 306555 - Add support for cast to type / view as array (IExpressions2)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.DsfCastToTypeSupport;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.DisabledExpressionVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.ExpressionManagerVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.ExpressionVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.IExpressionVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.SingleExpressionVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterBitFieldVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterGroupVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.SyncRegisterDataAccess;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * A specialization of ExpressionVMProvider that uses a GDB-specific variable VM
 * node. To understand why this is necessary, see GdbVariableVMNode.
 */
@SuppressWarnings("restriction")
public class GdbExpressionVMProvider extends ExpressionVMProvider {

	/**
	 * Constructor (passthru)
	 */
	public GdbExpressionVMProvider(AbstractVMAdapter adapter,
			IPresentationContext context, DsfSession session) {
		super(adapter, context, session);
	}

	/**
	 * The only difference between this and our super implementation is that we
	 * create a GdbVariableVMNode instead of a VariableVMNode. 
	 * 
	 * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.ExpressionVMProvider#configureLayout()
	 */
    @Override
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
        IExpressionVMNode variableNode =  new GdbVariableVMNode(this, getSession(), syncvarDataAccess);
        addChildNodes(variableNode, new IExpressionVMNode[] {variableNode});
        
        /* Wire up the casting support. IExpressions2 service is always available
		 * for gdb. No need to call hookUpCastingSupport */
		((VariableVMNode) variableNode).setCastToTypeSupport(
				new DsfCastToTypeSupport(getSession(), GdbExpressionVMProvider.this, syncvarDataAccess));
		
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
}
