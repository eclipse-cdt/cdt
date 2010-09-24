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

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueRetriever;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelAttribute;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesBasedLabelProvider;
import org.eclipse.cdt.tests.dsf.ViewerUpdatesListener;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestEvent;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @since 2.2
 */
public class TestModelDMVMNode extends AbstractDMVMNode implements IRootVMNode, IElementLabelProvider, IElementPropertiesProvider {

    final public static String PROP_PREFIX_DUMMY = "dummy.";
    
    final private static String PROP_TEST_ELEMENT_LABEL = "PROP_TEST_ELEMENT_LABEL";
    
    private ViewerUpdatesListener fViewerUpdateListener = NULL_VIEWER_UPDATE_LISTENER; 
    private FormattedValuesListener fFormattedValuesListener;
    
    private static final ViewerUpdatesListener NULL_VIEWER_UPDATE_LISTENER = new ViewerUpdatesListener();
        
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
        fLabelProvider.setColumnInfo(
            TestModelCachingVMProvider.COLUMN_DUMMY_VALUE, 
            new LabelColumnInfo(new LabelAttribute[] {
                new FormattedValueLabelText(PROP_PREFIX_DUMMY)
            }));
    }

    private final FormattedValueRetriever fFormattedValueRetriever;
    private final FormattedValueRetriever fDummyFormattedValueRetriever;

    public TestModelDMVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, TestElement.class);
        fFormattedValueRetriever = new FormattedValueRetriever(this, getSession(), TestModel.class, TestElement.class);
        fDummyFormattedValueRetriever = new FormattedValueRetriever(this, getSession(), DummyFormattedValueService.class, TestElement.class, PROP_PREFIX_DUMMY);
    }

    @Override
    public void dispose() {
        super.dispose();
        fFormattedValueRetriever.dispose();
        fDummyFormattedValueRetriever.dispose();
    }
    
    public void setVMUpdateListener(ViewerUpdatesListener viewerUpdateListener) {
        if (viewerUpdateListener != null) {
            fViewerUpdateListener = viewerUpdateListener;
        } else {
            fViewerUpdateListener = NULL_VIEWER_UPDATE_LISTENER;
        }
    }

    public void setFormattedValuesListener(FormattedValuesListener formattedValuesListener) {
        fFormattedValuesListener = formattedValuesListener;
    }
    
    public PropertiesBasedLabelProvider getLabelProvider() {
        return fLabelProvider;
    }
    
    public void update(final ILabelUpdate[] updates) {
        fLabelProvider.update(updates);
    }
        
    @Override
    public void update(IHasChildrenUpdate[] updates) {
        fViewerUpdateListener.viewerUpdatesBegin();
        for (IHasChildrenUpdate update : updates) {
            fViewerUpdateListener.updateStarted(update);
            if (update.getElement() instanceof TestElementVMContext) {
                TestElement element = ((TestElementVMContext)update.getElement()).getElement();
                update.setHasChilren(element.getChildren().length != 0);
            }
            update.done();
            fViewerUpdateListener.updateComplete(update);
        }
        fViewerUpdateListener.viewerUpdatesComplete();
    }

    @Override
    public void update(IChildrenCountUpdate[] updates) {
        fViewerUpdateListener.viewerUpdatesBegin();
        for (IChildrenCountUpdate update : updates) {
            fViewerUpdateListener.updateStarted(update);
            if (update.getElement() instanceof TestElementVMContext) {
                TestElement element = ((TestElementVMContext)update.getElement()).getElement();
                update.setChildCount(element.getChildren().length);
            }
            update.done();
            fViewerUpdateListener.updateComplete(update);
        }
        fViewerUpdateListener.viewerUpdatesComplete();
    }

    @Override
    public void update(IChildrenUpdate[] updates) {
        fViewerUpdateListener.viewerUpdatesBegin();
        for (IChildrenUpdate update : updates) {
            fViewerUpdateListener.updateStarted(update);
            if (update.getElement() instanceof TestElementVMContext) {
                TestElement element = ((TestElementVMContext)update.getElement()).getElement();
                fillUpdateWithTestElements(update, element.getChildren());
            }
            update.done();
            fViewerUpdateListener.updateComplete(update);
        }        
        fViewerUpdateListener.viewerUpdatesComplete();
    }
    
    
    @Override
    protected void updateElementsInSessionThread(IChildrenUpdate update) {
        // TODO Auto-generated method stub
        
    }
    
    public void update(final IPropertiesUpdate[] updates) {
        fViewerUpdateListener.propertiesUpdatesStarted(updates);
        if (fFormattedValuesListener != null) fFormattedValuesListener.propertiesUpdatesStarted(updates);
        
        CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), null) {
            @Override
            protected void handleSuccess() {
                for (IPropertiesUpdate update : updates) {
                    if (update.getElement() instanceof TestElementVMContext) {
                        TestElement element = ((TestElementVMContext)update.getElement()).getElement();
                        update.setProperty(PROP_TEST_ELEMENT_LABEL, element.getLabel());
                    }
                    update.done();
                    fViewerUpdateListener.propertiesUpdateCompleted(update);
                    if (fFormattedValuesListener != null) fFormattedValuesListener.propertiesUpdateCompleted(update);
                }
            }
        };
        int count = 0;
        
        fFormattedValueRetriever.update(updates, crm);
        count++;
        fDummyFormattedValueRetriever.update(updates, crm);
        count++;
        crm.setDoneCount(count);
    }
    
    private void fillUpdateWithTestElements(IChildrenUpdate update, TestElement[] modelElements) {
        int updateIdx = update.getOffset() != -1 ? update.getOffset() : 0;
        int endIdx = updateIdx + (update.getLength() != -1 ? update.getLength() : modelElements.length);
        while (updateIdx < endIdx && updateIdx < modelElements.length) {
            update.setChild(createVMContext(modelElements[updateIdx]), updateIdx);
            updateIdx++;
        }
    }
    
    public TestElementVMContext createVMContext(TestElement element) {
        return new TestElementVMContext(this, element);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.IVMNode#getDeltaFlags(java.lang.Object)
     */
    public int getDeltaFlags(Object e) {
        if ( e instanceof PropertyChangeEvent &&
             ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE) 
        {
            return IModelDelta.CONTENT;
        } 
        if (e instanceof TestEvent) {
            return ((TestEvent)e).getType();
        }
        
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, VMDelta parent, int nodeOffset, RequestMonitor rm) {
        if ( e instanceof PropertyChangeEvent &&
            ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE) 
        {
            parent.setFlags(parent.getFlags() | IModelDelta.CONTENT);
        } 
        rm.done();
    }


    public boolean isDeltaEvent(Object rootObject, Object event) {
        return getDeltaFlags(event) != IModelDelta.NO_CHANGE;
    }

    public void createRootDelta(Object rootObject, Object event, DataRequestMonitor<VMDelta> rm) {
        int flags = IModelDelta.NO_CHANGE;
        if ( event instanceof PropertyChangeEvent &&
            ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE) 
        {
            flags |= IModelDelta.CONTENT;
        } 
        
        // TODO: make more sophisticated to update specific elements.
        if (event instanceof TestEvent) {
            flags|= ((TestEvent)event).getType();
        }
        
        rm.setData( new VMDelta(rootObject, 0, flags) );
        rm.done();
    }


}
