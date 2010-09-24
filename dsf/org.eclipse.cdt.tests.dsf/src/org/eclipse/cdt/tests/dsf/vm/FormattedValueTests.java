/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import java.util.concurrent.ExecutionException;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesUpdateStatus;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ManualUpdatePolicy;
import org.eclipse.cdt.tests.dsf.IViewerUpdatesListenerConstants;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElementValidator;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestEvent;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests to verify the operation of FormattedValuesVMUtil
 * @since 2.2
 */
abstract public class FormattedValueTests extends TestCase implements IViewerUpdatesListenerConstants, IDebugVMConstants {
    
    Display fDisplay;
    Shell fShell;
    DsfExecutor fDsfExecutor;
    DsfSession fDsfSession;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fViewerListener;
    TestModelUpdatesListener fVMListener;
    FormattedValuesListener fFormattedValuesListener;
    TestModel fModel;
    DummyFormattedValueService fDummyValuesService;
    AbstractVMAdapter fVMAdapter;
    TestModelCachingVMProvider fVMProvider;
    
    public FormattedValueTests(String name) {
        super(name);
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    protected void setUp() throws Exception {
        fDsfExecutor = new DefaultDsfExecutor();
        fDsfSession = DsfSession.startSession(fDsfExecutor, getClass().getName());
        
        fDisplay = PlatformUI.getWorkbench().getDisplay();
        fShell = new Shell(fDisplay/*, SWT.ON_TOP | SWT.SHELL_TRIM*/);
        fShell.setMaximized(true);
        fShell.setLayout(new FillLayout());

        fViewer = createViewer(fDisplay, fShell);
        
        fModel = new TestModel(fDsfSession);
        initializeService(fModel);
        fDummyValuesService = new DummyFormattedValueService(fDsfSession);
        initializeService(fDummyValuesService);

        fViewerListener = new TestModelUpdatesListener(fViewer, true, false);

        fModel.setRoot( new TestElement(fModel, "root", new TestElement[0] ) ); 
        fModel.setElementChildren(TreePath.EMPTY, makeModelElements(fModel, getTestModelDepth(), "model"));
        
        fVMAdapter = new AbstractVMAdapter() {
            @Override
            protected IVMProvider createViewModelProvider(IPresentationContext context) {
                return fVMProvider;
            }
        };
        fVMProvider = new TestModelCachingVMProvider(fVMAdapter, fViewer.getPresentationContext(), fDsfSession);

        fVMListener = new TestModelUpdatesListener();
        fVMProvider.getNode().setVMUpdateListener(fVMListener);
        fVMProvider.getNode().getLabelProvider().addPropertiesUpdateListener(fViewerListener);

        fFormattedValuesListener = new FormattedValuesListener(fModel);
        fVMProvider.getNode().setFormattedValuesListener(fFormattedValuesListener);
        fModel.setTestModelListener(fFormattedValuesListener);
        
        fShell.open ();
    }

    private void initializeService(final IDsfService service) throws InterruptedException, ExecutionException {
        Query<Object> initQuery = new Query<Object>() {
            @Override
            protected void execute(DataRequestMonitor<Object> rm) {
                rm.setData(new Object());
                service.initialize(rm);
            }
        };
        fDsfExecutor.execute(initQuery);        
        initQuery.get();
    }
    
    abstract protected ITreeModelContentProviderTarget createViewer(Display display, Shell shell);
    
    /**
     * @throws java.lang.Exception
     */
    @Override
    protected void tearDown() throws Exception {
        fVMProvider.getNode().setFormattedValuesListener(null);
        fModel.setTestModelListener(null);
        
        fVMProvider.getNode().getLabelProvider().removePropertiesUpdateListener(fViewerListener);
        fVMProvider.getNode().setVMUpdateListener(null);
        
        fVMAdapter.dispose();
        
        fVMListener.dispose();
        fViewerListener.dispose();
        
        shutdownService(fDummyValuesService);
        shutdownService(fModel);
        fViewer.getPresentationContext().dispose();
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        DsfSession.endSession(fDsfSession);
        fDsfExecutor.shutdown();
    }

    private void shutdownService(final IDsfService service) throws InterruptedException, ExecutionException {
        Query<Object> shutdownQuery = new Query<Object>() {
            @Override
            protected void execute(DataRequestMonitor<Object> rm) {
                rm.setData(new Object());
                service.shutdown(rm);
            }
        };
        fDsfExecutor.execute(shutdownQuery);        
        shutdownQuery.get();
    }

    /**
     * Depth (size) of the test model to be used in the tests.  This number allows
     * the jface based tests to use a small enough model to fit on the screen, and 
     * for the virtual viewer to exercise the content provider to a greater extent.
     */
    abstract protected int getTestModelDepth();
    
    public void testValidate() {
        setInput(IFormattedValues.NATURAL_FORMAT);
        setFormatAndValidate(IFormattedValues.HEX_FORMAT, false, false, false);
    }

    public void testChangeFormat() {
        setInput(IFormattedValues.NATURAL_FORMAT);
        setFormatAndValidate(IFormattedValues.HEX_FORMAT, false, false, false);
        setFormatAndValidate(IFormattedValues.NATURAL_FORMAT, false, false, false);
    }

    public void testChangeFormatManualUpdateMode() {
        setInput(IFormattedValues.NATURAL_FORMAT);
        setUpdatePolicy(ManualUpdatePolicy.MANUAL_UPDATE_POLICY_ID);
        
        // Chenge to a new format, this does not cause the cache entries to be 
        // set to dirty.  Retrieving new format values should happen from the service.
        setFormatAndValidate(IFormattedValues.HEX_FORMAT, true, false, false);
        
        // Change _back_ to natural format.  Values should be retrieved from cache.
        setFormatAndValidate(IFormattedValues.NATURAL_FORMAT, true, true, false);
        
        // Generate an event which will cause all cache entries to be marked dirty.
        postEventInManualUpdateMode();
        
        // Change back again to hex format.  Values should be retrieved from cache.
        setFormatAndValidate(IFormattedValues.HEX_FORMAT, true, true, false);
        
        // Change to a decimal, which is not cached, values should come with an error.
        setFormatAndValidate(IFormattedValues.DECIMAL_FORMAT, true, true, true);
        
    }

    private void postEventInManualUpdateMode() {
        // Generate an event which will cause all cache entries to be marked dirty.
        fViewerListener.reset();
        fViewerListener.addUpdates(TreePath.EMPTY, fModel.getRootElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        fVMListener.reset();
        fFormattedValuesListener.reset();
        fVMProvider.postEvent(new TestEvent(fModel.getRootElement(), IModelDelta.CONTENT));
        while (!fViewerListener.isFinished(ALL_UPDATES_COMPLETE | PROPERTY_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        Assert.assertTrue(fFormattedValuesListener.getFormattedValuesCompleted().isEmpty());
    }
    
    public void testInvalidFormat() {
        setInput(IFormattedValues.NATURAL_FORMAT);

        fViewerListener.reset();
        fViewerListener.addUpdates(TreePath.EMPTY, ((TestElementVMContext)fViewer.getInput()).getElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        
        fVMListener.reset();
        fVMListener.addUpdates(TreePath.EMPTY, fModel.getRootElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        
        // Set the new number format to the viewer.
        fViewer.getPresentationContext().setProperty(PROP_FORMATTED_VALUE_FORMAT_PREFERENCE, "invalid format");

        while (!fViewerListener.isFinished(ALL_UPDATES_COMPLETE | PROPERTY_UPDATES) || !fVMListener.isFinished(CONTENT_UPDATES | PROPERTY_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();

        validateModel(IFormattedValues.HEX_FORMAT, " (" + FormattedValueVMUtil.getFormatLabel(IFormattedValues.HEX_FORMAT) + ")");        
    }

    /**
     * Initial format is NATURAL.
     */
    private void setInput(String formatId) {
        // Set the new number format to the viewer.
        fViewer.getPresentationContext().setProperty(PROP_FORMATTED_VALUE_FORMAT_PREFERENCE, formatId);

        fViewer.setAutoExpandLevel(-1);
        TestElementVMContext rootVMC = fVMProvider.getElementVMContext(fViewer.getPresentationContext(), fModel.getRootElement());
        
        // Create the listener
        fViewerListener.reset();
        fViewerListener.addUpdates(TreePath.EMPTY, rootVMC.getElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        fVMListener.reset();
        fVMListener.addUpdates(TreePath.EMPTY, rootVMC.getElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        fFormattedValuesListener.reset();
        
        fViewer.setInput(rootVMC);
        while (!fViewerListener.isFinished(ALL_UPDATES_COMPLETE | PROPERTY_UPDATES) || !fVMListener.isFinished(CONTENT_COMPLETE | PROPERTY_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        
        Assert.assertTrue(fFormattedValuesListener.isFinished());
    }

    private void setUpdatePolicy(String policyId) {
        IVMUpdatePolicy[] policies = fVMProvider.getAvailableUpdatePolicies();
        IVMUpdatePolicy newPolicy = null; 
        for (IVMUpdatePolicy policy : policies) {
            if (policyId.equals(policy.getID())) {
                newPolicy = policy;
                break;
            }
        }
        if (newPolicy != null) {
            fVMProvider.setActiveUpdatePolicy(newPolicy);
        } else {
            throw new RuntimeException("Update policy " + policyId + " not available");
        }
        fViewerListener.reset();
        fViewerListener.addUpdates(TreePath.EMPTY, fModel.getRootElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        fVMListener.setFailOnRedundantUpdates(false);
        while (!fViewerListener.isFinished(ALL_UPDATES_COMPLETE | PROPERTY_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        
        fVMListener.setFailOnRedundantUpdates(true);
    }

    private void setFormatAndValidate(
        String formatId, 
        boolean expectContentCached, 
        boolean expectFormattedValuesCached,
        boolean expectCacheMissError) 
    {
        fViewerListener.reset();
        fViewerListener.addUpdates(TreePath.EMPTY, ((TestElementVMContext)fViewer.getInput()).getElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        
        fVMListener.reset();
        int vmUpdateFlags = PROPERTY_UPDATES;
        if (!expectContentCached) {
            vmUpdateFlags |= ALL_UPDATES_COMPLETE;
        }
        fVMListener.addUpdates(TreePath.EMPTY, fModel.getRootElement(), -1, vmUpdateFlags);
        
        fFormattedValuesListener.reset();
        if (expectFormattedValuesCached && !expectCacheMissError) {
            fFormattedValuesListener.setCachedFormats(new String[] {formatId} );
        }
        
        // Set the new number format to the viewer.
        fViewer.getPresentationContext().setProperty(PROP_FORMATTED_VALUE_FORMAT_PREFERENCE, formatId);

        while (!fViewerListener.isFinished(ALL_UPDATES_COMPLETE | PROPERTY_UPDATES) || !fVMListener.isFinished(CONTENT_UPDATES | PROPERTY_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        
        if (expectCacheMissError) {
            try {
                validateModel(formatId, "");
                throw new RuntimeException("Expected validateModel to fail");
            }
            catch(AssertionFailedError e) {
                // expected
            }
        } else {
            validateModel(formatId, "");
        }
        
        if (expectCacheMissError) {
            String formatProperty = FormattedValueVMUtil.getPropertyForFormatId(formatId);
            
            Assert.assertTrue(fFormattedValuesListener.getFormattedValuesCompleted().isEmpty());
            Assert.assertFalse(fFormattedValuesListener.getPropertiesUpdates().isEmpty());
            for (IPropertiesUpdate update : fFormattedValuesListener.getPropertiesUpdates()) {
                PropertiesUpdateStatus status = (PropertiesUpdateStatus)update.getStatus();
                assertEquals(IDsfStatusConstants.INVALID_STATE, status.getCode());
                assertEquals("Cache contains stale data. Refresh view.", status.getStatus(formatProperty).getMessage());
                assertEquals(
                    "Cache contains stale data. Refresh view.", 
                    status.getStatus(PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE).getMessage());
                assertEquals(1, status.getChildren().length);
                
            }
            
        } else {
            Assert.assertTrue(fFormattedValuesListener.isFinished());
        }
        
    }

    private void validateModel(final String formatId, final String suffix) {
        fModel.validateData(
            fViewer, TreePath.EMPTY, 
            new TestElementValidator() {
                public void validate(TestElement modelElement, TestElement viewerElement, TreePath viewerPath) {
                    ViewerLabel label = fViewer.getElementLabel(viewerPath, TestModelCachingVMProvider.COLUMN_ID);
                    assertEquals(modelElement.getID(), label.getText());
                    
                    label = fViewer.getElementLabel(viewerPath, TestModelCachingVMProvider.COLUMN_FORMATTED_VALUE);
                    assertEquals(fModel.getFormattedValueText(modelElement, formatId) + suffix, label.getText());

                    label = fViewer.getElementLabel(viewerPath, TestModelCachingVMProvider.COLUMN_DUMMY_VALUE);
                    assertEquals(formatId, label.getText());
                }
            });
    }
    
    private TestElement[] makeModelElements(TestModel model, int depth, String prefix) {
        TestElement[] elements = new TestElement[depth];
        for (int i = 0; i < depth; i++) {
            String name = prefix + "." + i;
            elements[i] = new TestElement(model, name, makeModelElements(model, i, name));
        }
        return elements;
    }
}
