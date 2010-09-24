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

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;

/**
 * @since 2.2
 */
public class TestElementVMContext extends AbstractVMContext implements IDMVMContext {

    final private TestElement fElement;
    
    public TestElementVMContext(IVMNode node, TestElement element) {
        super(node);
        fElement = element;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof TestElementVMContext && ((TestElementVMContext)obj).fElement.equals(fElement);
    }

    @Override
    public int hashCode() {
        return fElement.hashCode();
    }

    public TestElement getElement() {
        return fElement;
    }

    public IDMContext getDMContext() {
        return getElement();
    }
    
    @Override
    public String toString() {
        return getDMContext().toString();
    }
}
