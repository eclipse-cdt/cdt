/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMModelProxy;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * @since 2.2
 */
public class TestModelVMProvider extends AbstractVMProvider {

	private DsfExecutor fDsfExecutor;

	public TestModelVMProvider(AbstractVMAdapter adapter, IPresentationContext context) {
		super(adapter, context);

		fDsfExecutor = new DefaultDsfExecutor("TestModelVMProvider");

		setRootNode(new TestModelVMNode(this));
		addChildNodes(getRootVMNode(), new IVMNode[] { getRootVMNode() });
	}

	@Override
	public void dispose() {
		super.dispose();
		fDsfExecutor.shutdown();
	}

	public DsfExecutor getDsfExecutor() {
		return fDsfExecutor;
	}

	public TestElementVMContext getElementVMContext(IPresentationContext context, TestElement element) {
		return ((TestModelVMNode) getRootVMNode()).createVMContext(element);
	}

	public void postDelta(IModelDelta delta) {
		for (IVMModelProxy proxy : getActiveModelProxies()) {
			proxy.fireModelChanged(delta);
		}
	}
}
