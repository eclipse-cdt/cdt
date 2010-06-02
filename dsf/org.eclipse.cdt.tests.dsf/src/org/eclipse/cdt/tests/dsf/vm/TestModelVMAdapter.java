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
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * 
 */
public class TestModelVMAdapter extends AbstractVMAdapter {

    @Override
    protected IVMProvider createViewModelProvider(IPresentationContext context) {
        return new TestModelVMProvider(this, context);
    }
    
    public TestModelVMProvider getTestModelProvider(IPresentationContext context) {
        return (TestModelVMProvider)getVMProvider(context);
    }
}
