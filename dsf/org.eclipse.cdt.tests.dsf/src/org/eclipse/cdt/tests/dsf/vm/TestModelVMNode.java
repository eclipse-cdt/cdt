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

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueLabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelAttribute;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesBasedLabelProvider;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;

/**
 * @since 2.2
 */
public class TestModelVMNode extends AbstractVMNode implements IRootVMNode, IElementLabelProvider, IElementPropertiesProvider {

    final private static String PROP_TEST_ELEMENT_LABEL = "PROP_TEST_ELEMENT_LABEL";
    
    final private static PropertiesBasedLabelProvider fLabelProvider = new PropertiesBasedLabelProvider();
    {
        LabelColumnInfo idLabelInfo = new LabelColumnInfo(new LabelAttribute[] { 
            new LabelText("{0}", new String[] { PROP_TEST_ELEMENT_LABEL })
        });  
        
        fLabelProvider.setColumnInfo(PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS, idLabelInfo); 
        fLabelProvider.setColumnInfo(TestModelCachingVMProvider.COLUMN_ID, idLabelInfo);
        fLabelProvider.setColumnInfo(
            TestModelCachingVMProvider.COLUMN_FORMATTED_VALUE, 
            new LabelColumnInfo(new LabelAttribute[] {
                new FormattedValueLabelText()
            }));
    }
    
    public void update(final ILabelUpdate[] updates) {
        fLabelProvider.update(updates);
    }
    
    public TestModelVMNode(AbstractVMProvider provider) {
        super(provider);
    }

    public void update(IHasChildrenUpdate[] updates) {
        for (IHasChildrenUpdate update : updates) {
            if (update.getElement() instanceof TestElementVMContext) {
                TestElement element = ((TestElementVMContext)update.getElement()).getElement();
                update.setHasChilren(element.getChildren().length != 0);
            }
            update.done();
        }
    }
    
    public void update(IChildrenCountUpdate[] updates) {
        for (IChildrenCountUpdate update : updates) {
            if (update.getElement() instanceof TestElementVMContext) {
                TestElement element = ((TestElementVMContext)update.getElement()).getElement();
                update.setChildCount(element.getChildren().length);
            }
            update.done();
        }
    }

    public void update(IChildrenUpdate[] updates) {
        for (IChildrenUpdate update : updates) {
            if (update.getElement() instanceof TestElementVMContext) {
                TestElement element = ((TestElementVMContext)update.getElement()).getElement();
                fillUpdateWithTestElements(update, element.getChildren());
            }
            update.done();
        }        
    }
    
    public void update(IPropertiesUpdate[] updates) {
        for (IPropertiesUpdate update : updates) {
            if (update.getElement() instanceof TestElementVMContext) {
                TestElement element = ((TestElementVMContext)update.getElement()).getElement();
                update.setProperty(PROP_TEST_ELEMENT_LABEL, element.getLabel());
            }
            update.done();
        }
    }

    private void fillUpdateWithTestElements(IChildrenUpdate update, TestElement[] elements) {
        int updateIdx = update.getOffset() != -1 ? update.getOffset() : 0;
        int endIdx = updateIdx + (update.getLength() != -1 ? update.getLength() : elements.length);
        while (updateIdx < endIdx && updateIdx < elements.length) {
            update.setChild(createVMContext(elements[updateIdx]), updateIdx);
            updateIdx++;
        }
    }
    
    public TestElementVMContext createVMContext(TestElement element) {
        return new TestElementVMContext(this, element);
    }

    public int getDeltaFlags(Object event) {
        return 0;
    }

    public void buildDelta(Object event, VMDelta parent, int nodeOffset, RequestMonitor rm) {
        rm.done();
    }


    public boolean isDeltaEvent(Object rootObject, Object event) {
        return false;
    }

    public void createRootDelta(Object rootObject, Object event, DataRequestMonitor<VMDelta> rm) {
        rm.setStatus(new Status(IStatus.ERROR, DsfTestPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED,  "Not implemented", null));
        rm.done();
    }


}
