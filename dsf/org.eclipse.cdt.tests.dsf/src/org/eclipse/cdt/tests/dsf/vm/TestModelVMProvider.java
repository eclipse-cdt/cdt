/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMModelProxy;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * 
 */
public class TestModelVMProvider extends AbstractVMProvider {
    public TestModelVMProvider(AbstractVMAdapter adapter, IPresentationContext context) {
        super(adapter, context);
        
        setRootNode(new TestModelVMNode(this));
        addChildNodes(getRootVMNode(), new IVMNode[] { getRootVMNode() });
    }


    public TestElementVMContext getElementVMContext(IPresentationContext context, TestElement element) {
        return ((TestModelVMNode)getRootVMNode()).createVMContext(element);
    }

    public void postDelta(IModelDelta delta) {
        for (IVMModelProxy proxy : getActiveModelProxies()) {
            proxy.fireModelChanged(delta);
        }
    }
}
