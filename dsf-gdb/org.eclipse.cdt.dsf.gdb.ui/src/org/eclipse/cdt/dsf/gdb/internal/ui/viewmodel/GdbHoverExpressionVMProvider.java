/*******************************************************************************
 * Copyright (c) 2014 Freescale Semiconductor. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *     Axel Mueller            - Bug 306555 - Add support for cast to type / view as array (IExpressions2)
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.DsfCastToTypeSupport;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.DisabledExpressionVMNode;
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
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;

/**
 * A specialization of GdbExpressionVMProvider that doesn't update variables
 * upon debugging and inferior event, such as "single-step". This avoid useless
 * updates of formerly shown variables. In oder words:
 */
public class GdbHoverExpressionVMProvider extends GdbExpressionVMProvider {

	public GdbHoverExpressionVMProvider(AbstractVMAdapter adapter,
			IPresentationContext context, DsfSession session) {
		super(adapter, context, session);
	}

	/**
	 * The only difference between this and our super implementation is that we
	 * create a SingleExpressionVMNode instead of a ExpressionManagerVMNode.
	 * 
	 * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.GdbExpressionVMProvider#configureLayout()
	 */
	@Override
	protected void configureLayout() {

		/*
		 * Allocate the synchronous data providers.
		 */
		SyncRegisterDataAccess syncRegDataAccess = new SyncRegisterDataAccess(
				getSession());
		SyncVariableDataAccess syncvarDataAccess = new SyncVariableDataAccess(
				getSession());

		/*
		 * Create the top level node which provides the anchor starting point.
		 */
		IRootVMNode rootNode = new RootDMVMNode(this);

		/*
		 * Now the Over-arching management node.
		 */
		SingleExpressionVMNode expressionManagerNode = new SingleExpressionVMNode(
				this);
		addChildNodes(rootNode, new IVMNode[] { expressionManagerNode });

		// Disabled expression node intercepts disabled expressions and prevents
		// them from being
		// evaluated by other nodes.
		IExpressionVMNode disabledExpressionNode = new DisabledExpressionVMNode(
				this);

		/*
		 * The expression view wants to support fully all of the components of
		 * the register view.
		 */
		IExpressionVMNode registerGroupNode = new RegisterGroupVMNode(this,
				getSession(), syncRegDataAccess);

		IExpressionVMNode registerNode = new RegisterVMNode(this, getSession(),
				syncRegDataAccess);
		addChildNodes(registerGroupNode,
				new IExpressionVMNode[] { registerNode });

		/*
		 * Create the next level which is the bit-field level.
		 */
		IVMNode bitFieldNode = new RegisterBitFieldVMNode(this, getSession(),
				syncRegDataAccess);
		addChildNodes(registerNode, new IVMNode[] { bitFieldNode });

		/*
		 * Create the support for the SubExpressions. Anything which is brought
		 * into the expressions view comes in as a fully qualified expression so
		 * we go directly to the SubExpression layout node.
		 */
		IExpressionVMNode variableNode = new GdbVariableVMNode(this,
				getSession(), syncvarDataAccess);
		addChildNodes(variableNode, new IExpressionVMNode[] { variableNode });

		/*
		 * Wire up the casting support. IExpressions2 service is always
		 * available for gdb. No need to call hookUpCastingSupport
		 */
		((VariableVMNode) variableNode)
				.setCastToTypeSupport(new DsfCastToTypeSupport(getSession(),
						GdbHoverExpressionVMProvider.this, syncvarDataAccess));

		/*
		 * Tell the expression node which sub-nodes it will directly support. It
		 * is very important that the variables node be the last in this chain.
		 * The model assumes that there is some form of metalanguage expression
		 * syntax which each of the nodes evaluates and decides if they are
		 * dealing with it or not. The variables node assumes that the
		 * expression is fully qualified and there is no analysis or subdivision
		 * of the expression it will parse. So it it currently the case that the
		 * location of the nodes within the array being passed in is the order
		 * of search/evaluation. Thus variables wants to be last. Otherwise it
		 * would just assume what it was passed was for it and the real node
		 * which wants to handle it would be left out in the cold.
		 */
		setExpressionNodes(new IExpressionVMNode[] { disabledExpressionNode,
				registerGroupNode, variableNode });

		/*
		 * Let the work know which is the top level node.
		 */
		setRootNode(rootNode);
	}

	@Override
	public void handleEvent(Object event, final RequestMonitor rm) {
		// Avoid executing the base implementation for all "data model events".
		// Reason: If we'd call the base, the formerly shown expression in the
		// hover would be evaluated upon every debug-event, such as after every
		// single-step action. This is useless, as the hover has already been
		// disposed at the time of performing a single-step. While updating 1
		// expression does usually not cost much CPU time, it may cost much CPU
		// time e.g. in case of large ELF files and with a symbol that is
		// out-of-scope.
		//
		// note: To ensure a 'refresh' of the value whenever the hover is shown
		// again, we have an overridden version of update.
		if (!(event instanceof IDMEvent)) 
		{
			super.handleEvent(event, rm);
		}
	}

	private String prevExpression = ""; //$NON-NLS-1$

	/**
	 * overridden in order to refresh the data whenever the hover is shown
	 * "again". This is required in the following use case:
	 * <ul>
	 * <li>You're in a function</li>
	 * <li>You watch a variable value by hover</li>
	 * <li>You step over code that modifies that variable</li>
	 * <li>You again watch the variable value by hover</li>
	 * </ul>
	 * Because we omitted to refresh the variable during stepping (see
	 * handleEvent), we need to refresh the value now, otherwise the original
	 * value would be shown again.
	 */
	@Override
	public void update(IViewerInputUpdate update) {
		if (IDsfDebugUIConstants.ID_EXPRESSION_HOVER
				.equals(getPresentationContext().getId())) {
			Object input = update.getElement();
			if (input instanceof IExpressionDMContext) {
				IExpressionDMContext dmc = (IExpressionDMContext) input;
				if (prevExpression.equals(dmc.getExpression())) {
					// Always refresh the value
					refresh();
				}
				prevExpression = dmc.getExpression();
				SingleExpressionVMNode vmNode = (SingleExpressionVMNode) getChildVMNodes(getRootVMNode())[0];
				final IDMVMContext viewerInput = vmNode.createVMContext(dmc);

				// provide access to viewer (needed by details pane)
				getPresentationContext().setProperty(
						"__viewerInput", viewerInput); //$NON-NLS-1$

				update.setInputElement(viewerInput);
				update.done();
				return;
			}
		}
		super.update(update);
	}
}
