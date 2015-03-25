/*****************************************************************
 * Copyright (c) 2011, 2014 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format example (Bug 202556)
 *****************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui.viewmodel;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SimpleMapPersistable;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.ExpressionVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.IElementFormatProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * PDA View Model provider for the expression view.
 */
@SuppressWarnings("restriction")
public class PDAExpressionVMProvider extends ExpressionVMProvider implements IElementFormatProvider {

	static String myPersistId = "org.eclipse.cdt.examples.dsf.pda.ui.variablePersistable";
	
	public PDAExpressionVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
		super(adapter, context, session);
	}

	@SuppressWarnings("unchecked")
	public void getActiveFormat(IPresentationContext context, IVMNode node, Object viewerInput, TreePath elementPath,
			DataRequestMonitor<String> rm) {
		Object p = context.getProperty(myPersistId);
		if (p instanceof SimpleMapPersistable == false) {
			rm.setData(null);
			rm.done();
			return;
		}
	    SimpleMapPersistable<String> persistable = (SimpleMapPersistable<String>) p;
		Object x = elementPath.getLastSegment();
		if (x instanceof VariableVMNode.VariableExpressionVMC) {
			IExpressionDMContext ctx = DMContexts.getAncestorOfType(((VariableVMNode.VariableExpressionVMC) x).getDMContext(), IExpressionDMContext.class);
			if (ctx == null) {
				rm.setData(null);
			} else {
				rm.setData(persistable.getValue(ctx.getExpression()));
			}
			rm.done();
			return;
		} else if (x instanceof IDMVMContext) {
			// register and bit field context are covered here.
			// When these show up in expression view, the register/bit field vm node's associateExpression has called
			// RegisterVMC/BitFieldVMC's setExpression
			IExpression y = ((IVMContext) x).getAdapter(IExpression.class);
			if (y == null) {
				rm.setData(null);
			} else {
				rm.setData(persistable.getValue(y.getExpressionText()));
			}
			rm.done();
			return;
		}
		rm.setData(null);
		rm.done();
		return;
	}

	@SuppressWarnings("unchecked")
	public void setActiveFormat(IPresentationContext context, IVMNode[] node, Object viewerInput, TreePath[] elementPath, String format) {
		Object p = context.getProperty(myPersistId);
		SimpleMapPersistable<String> persistable = null;
		if (p instanceof SimpleMapPersistable) {
		    persistable = (SimpleMapPersistable<String>) p;
		} else {
			persistable = new SimpleMapPersistable<String>(String.class);
			context.setProperty(myPersistId, persistable);
		}
		ArrayList<IDMVMContext> changed = new ArrayList<IDMVMContext>(elementPath.length);
		for (int i = 0; i < elementPath.length; i++) {
			Object x = elementPath[i].getLastSegment();
			if (x instanceof VariableVMNode.VariableExpressionVMC) {
				IExpressionDMContext ctx = DMContexts.getAncestorOfType(((VariableVMNode.VariableExpressionVMC) x).getDMContext(), IExpressionDMContext.class);
				if (ctx == null)
					continue;
				persistable.setValue(ctx.getExpression(), format);
				changed.add((IDMVMContext) x);
			} else if (x instanceof IDMVMContext) {
				IExpression y = ((IVMContext) x).getAdapter(IExpression.class);
				if (y == null)
					continue;
				persistable.setValue(y.getExpressionText(), format);
			}
		}
		if (changed.size() > 0) {
			refresh();
		}
	}

	public boolean supportFormat(IVMContext context) {
		if (context instanceof VariableVMNode.VariableExpressionVMC) {
			return true;
		}
		return false;
	}
}
