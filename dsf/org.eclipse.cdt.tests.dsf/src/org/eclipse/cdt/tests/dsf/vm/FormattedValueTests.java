/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format test cases (Bug 202556)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.IElementFormatProvider;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesUpdateStatus;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ManualUpdatePolicy;
import org.eclipse.cdt.tests.dsf.IViewerUpdatesListenerConstants;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElementValidator;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestEvent;
import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;

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
    int vmListenerLevel = -1;
    
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

    /**
     * helper to create view model and viewer
     * @param vmOnly true to create view model only and do not create viewer
     */
    void createViewer(boolean vmOnly) {
    	if (vmOnly == false) {
            fDisplay = PlatformUI.getWorkbench().getDisplay();
            fShell = new Shell(fDisplay/*, SWT.ON_TOP | SWT.SHELL_TRIM*/);
            fShell.setMaximized(true);
            fShell.setLayout(new FillLayout());
            fViewer = createViewer(fDisplay, fShell);
            fViewerListener = new TestModelUpdatesListener(fViewer, true, false);
    	}    	
    	fVMProvider = new TestElementFormatVMProvider(fVMAdapter, fViewer.getPresentationContext(), fDsfSession);
        fVMListener = new TestModelUpdatesListener();
        fVMProvider.getNode().setVMUpdateListener(fVMListener);
        fVMProvider.getNode().getLabelProvider().addPropertiesUpdateListener(fViewerListener);
        fVMProvider.getNode().setFormattedValuesListener(fFormattedValuesListener);
    	if (vmOnly == false) {
    		fShell.open();
    	}
    }

    /**
     * helper to destory view model and viewer
     * @param vmOnly true to destory view model only and do not destroy viewer
     */
    void destroyViewer(boolean vmOnly) {
        fVMProvider.getNode().setFormattedValuesListener(null);
        fVMProvider.getNode().getLabelProvider().removePropertiesUpdateListener(fViewerListener);
        fVMProvider.getNode().setVMUpdateListener(null);
        fVMListener.dispose();
        if (vmOnly == false) {
        	fViewerListener.dispose();
        	fViewer.getPresentationContext().dispose();
        	// Close the shell
        	fShell.close();
            while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        }
    }

    /**
     * helper to recreate view model only
     */
    void recreateViewModel() {
    	destroyViewer(true);
    	createViewer(true);
    }

    /**
     * helper to recreate viewer (and view model)
     */
    void recreateViewer() {
    	destroyViewer(false);
    	createViewer(false);
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
    
    abstract protected IInternalTreeModelViewer createViewer(Display display, Shell shell);
    
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

        validateModel(IFormattedValues.HEX_FORMAT, 
                      " (" + FormattedValueVMUtil.getFormatLabel(IFormattedValues.HEX_FORMAT) + ")", 
                      DummyFormattedValueService.DUMMY_FORMAT, 
                      " (" + DummyFormattedValueService.DUMMY_FORMAT + ")");        
    }

    /**
     * Test that each element can have its own format
     */
    public void testValidateElement() {
        recreateViewModel();
        String preferenceFormat = IFormattedValues.NATURAL_FORMAT;
        setInput(preferenceFormat);
        // set each element to the same element format different than the preference format, and verify
        HashMap<String, ElementFormatSetting> map = new HashMap<String, ElementFormatSetting>();
        String[] format = { IFormattedValues.HEX_FORMAT };
        makeElementFormatSetting(fViewer, TreePath.EMPTY, format, -1, 0, map);
        ArrayList<ElementFormatSetting> elementFormats = new ArrayList<ElementFormatSetting>(map.values());
        setFormatAndValidate(preferenceFormat, elementFormats, elementFormats, true, false, false);
        // element of same level use the same format and different levels have different formats, and verify
        map.clear();
        format = new String[] { IFormattedValues.HEX_FORMAT, IFormattedValues.DECIMAL_FORMAT,
        		IFormattedValues.OCTAL_FORMAT, IFormattedValues.BINARY_FORMAT,
        		IFormattedValues.NATURAL_FORMAT };
        makeElementFormatSetting(fViewer, TreePath.EMPTY, format, -1, 0, map);
        elementFormats = new ArrayList<ElementFormatSetting>(map.values());
        setFormatAndValidate(preferenceFormat, elementFormats, elementFormats, false, false, false);
    }

    /**
     * Test that each element can change to a format and then restore to preference format
     */
    public void testChangeElementFormat() {
    	recreateViewModel();
        String preferenceFormat = IFormattedValues.HEX_FORMAT;
        setInput(IFormattedValues.NATURAL_FORMAT);
        setFormatAndValidate(preferenceFormat, false, false, false);
        // set each element to a format, and verify
        HashMap<String, ElementFormatSetting> map = new HashMap<String, ElementFormatSetting>();
        String[] format = { IFormattedValues.HEX_FORMAT, IFormattedValues.DECIMAL_FORMAT,
        		IFormattedValues.OCTAL_FORMAT, IFormattedValues.BINARY_FORMAT,
        		IFormattedValues.NATURAL_FORMAT };
        makeElementFormatSetting(fViewer, TreePath.EMPTY, format, -1, 0, map);
        ArrayList<ElementFormatSetting> elementFormats = new ArrayList<ElementFormatSetting>(map.values());
        setFormatAndValidate(preferenceFormat, elementFormats, elementFormats, false, false, false);
        // Restore each element to preference format, and verify
        for (ElementFormatSetting e : elementFormats) {
        	e.formatId = null;
        }
        setFormatAndValidate(preferenceFormat, elementFormats, elementFormats, false, false, false);
    }

    /**
     * Test changing element to a format and then restore to preference format,
     * using a view model provider that applies a format to child elements
     * of a certain level of depth. 
     */
    public void testChangeElementFormatApplyDepth() {
    	recreateViewModel();
        if (fVMProvider instanceof TestElementFormatVMProvider == false) {
        	return;
        }
        TestElementFormatVMProvider myVM = (TestElementFormatVMProvider) fVMProvider;
        String preferenceFormat = IFormattedValues.HEX_FORMAT;
        setInput(IFormattedValues.NATURAL_FORMAT);
        setFormatAndValidate(preferenceFormat, false, false, false);
        int[] myDepths = new int[] { -1, 2 };
        for (int depth : myDepths) {
            myVM.elementFormatApplyDepth = depth;
            // set top level element to a format, and verify top and child elements
            // at certain levels have the correct format.
            String[] format = { IFormattedValues.DECIMAL_FORMAT };
            HashMap<String, ElementFormatSetting> map = new HashMap<String, ElementFormatSetting>();
            makeElementFormatSetting(fViewer, TreePath.EMPTY, format, 1, 0, map);
            ArrayList<ElementFormatSetting> setElementFormats = new ArrayList<ElementFormatSetting>(map.values());
            HashMap<String, ElementFormatSetting> expMap = new HashMap<String, ElementFormatSetting>();
            makeElementFormatSetting(fViewer, TreePath.EMPTY, format, depth, 0, expMap);
            ArrayList<ElementFormatSetting> expectElementFormats = new ArrayList<ElementFormatSetting>(expMap.values());
            setFormatAndValidate(preferenceFormat, setElementFormats, expectElementFormats, false, false, false);
            // Restore top level element to preference format, and verify.
            for (ElementFormatSetting e : setElementFormats) {
            	e.formatId = null;
            }
            for (ElementFormatSetting e : expectElementFormats) {
            	e.formatId = null;
            }
            setFormatAndValidate(preferenceFormat, setElementFormats, expectElementFormats, false, false, false);
        }
    }

    /**
     * Test changing format of each element under manual update policy.
     * Formatted values should be retrieved from cache if available.
     * Changing to a format whose formatted value is not in cache should get a cache miss error. 
     */
    public void testChangeElementFormatManualUpdateMode() {
    	recreateViewModel();
    	String preferenceFormat = IFormattedValues.NATURAL_FORMAT;
        setInput(IFormattedValues.NATURAL_FORMAT);
        setUpdatePolicy(ManualUpdatePolicy.MANUAL_UPDATE_POLICY_ID);
        
        // Change to a new format, this does not cause the cache entries to be 
        // set to dirty.  Retrieving new format values should happen from the service.
        HashMap<String, ElementFormatSetting> map1 = new HashMap<String, ElementFormatSetting>();
        String[] format1 = { IFormattedValues.HEX_FORMAT };
        makeElementFormatSetting(fViewer, TreePath.EMPTY, format1, -1, 0, map1);
        ArrayList<ElementFormatSetting> elementFormats1 = new ArrayList<ElementFormatSetting>(map1.values());
        setFormatAndValidate(preferenceFormat, elementFormats1, elementFormats1, true, false, false);
        
        // Remove element format and so restore back to preference - natural format.  Values should be retrieved from cache.
        HashMap<String, ElementFormatSetting> map2 = new HashMap<String, ElementFormatSetting>();
        String[] format2 = { null };
        makeElementFormatSetting(fViewer, TreePath.EMPTY, format2, -1, 0, map2);
        ArrayList<ElementFormatSetting> elementFormats2 = new ArrayList<ElementFormatSetting>(map2.values());
        setFormatAndValidate(preferenceFormat, elementFormats2, elementFormats2, true, true, false);
        
        // Generate an event which will cause all cache entries to be marked dirty.
        postEventInManualUpdateMode();
        
        // Change back again to hex format.  Values should be retrieved from cache.
        setFormatAndValidate(preferenceFormat, elementFormats1, elementFormats1, true, true, false);
        
        // Change to a decimal, which is not cached, values should come with an error.
        HashMap<String, ElementFormatSetting> map3 = new HashMap<String, ElementFormatSetting>();
        String[] format3 = { IFormattedValues.DECIMAL_FORMAT };
        makeElementFormatSetting(fViewer, TreePath.EMPTY, format3, -1, 0, map3);
        ArrayList<ElementFormatSetting> elementFormats3 = new ArrayList<ElementFormatSetting>(map3.values());
        setFormatAndValidate(preferenceFormat, elementFormats3, elementFormats3, true, true, true);
    }

    /**
     * Test changing element format under manual update policy,
     * using a view model provider that applies a format to child elements
     * of a certain level of depth. 
     */
    public void testChangeElementFormatApplyDepthManualUpdateMode() {
        int[] myDepths = new int[] { -1, 2 };
        for (int depth : myDepths) {
        	recreateViewer();
            if (fVMProvider instanceof TestElementFormatVMProvider == false) {
            	return;
            }
            TestElementFormatVMProvider myVM = (TestElementFormatVMProvider) fVMProvider;
        	String preferenceFormat = IFormattedValues.NATURAL_FORMAT;
            setInput(IFormattedValues.NATURAL_FORMAT);
            setUpdatePolicy(ManualUpdatePolicy.MANUAL_UPDATE_POLICY_ID);
            myVM.elementFormatApplyDepth = depth;
            // Change top level to a new format, this does not cause the cache entries to be 
            // set to dirty.  Retrieving new format values should happen from the service.
            String[] format1 = { IFormattedValues.HEX_FORMAT };
            HashMap<String, ElementFormatSetting> map1 = new HashMap<String, ElementFormatSetting>();
            makeElementFormatSetting(fViewer, TreePath.EMPTY, format1, 1, 0, map1);
            ArrayList<ElementFormatSetting> elementFormats1 = new ArrayList<ElementFormatSetting>(map1.values());
            HashMap<String, ElementFormatSetting> expMap1 = new HashMap<String, ElementFormatSetting>();
            makeElementFormatSetting(fViewer, TreePath.EMPTY, format1, depth, 0, expMap1);
            ArrayList<ElementFormatSetting> expectElementFormats1 = new ArrayList<ElementFormatSetting>(expMap1.values());
            vmListenerLevel = depth;
            setFormatAndValidate(preferenceFormat, elementFormats1, expectElementFormats1, true, false, false);

            // Remove element format and so restore back to preference format - natural.  Values should be retrieved from cache.
            String[] format2 = { null };
            HashMap<String, ElementFormatSetting> map2 = new HashMap<String, ElementFormatSetting>();
            makeElementFormatSetting(fViewer, TreePath.EMPTY, format2, 1, 0, map2);
            ArrayList<ElementFormatSetting> elementFormats2 = new ArrayList<ElementFormatSetting>(map2.values());
            HashMap<String, ElementFormatSetting> expMap2 = new HashMap<String, ElementFormatSetting>();
            makeElementFormatSetting(fViewer, TreePath.EMPTY, format2, depth, 0, expMap2);
            ArrayList<ElementFormatSetting> expectElementFormats2 = new ArrayList<ElementFormatSetting>(expMap2.values());
            setFormatAndValidate(preferenceFormat, elementFormats2, expectElementFormats2, true, true, false);

            // Generate an event which will cause all cache entries to be marked dirty.
            postEventInManualUpdateMode();

            // Change back again to hex format.  Values should be retrieved from cache.
            setFormatAndValidate(preferenceFormat, elementFormats1, expectElementFormats1, true, true, false);

            // Change to a decimal, which is not cached, values should come with an error.
            String[] format3 = { IFormattedValues.DECIMAL_FORMAT };
            HashMap<String, ElementFormatSetting> map3 = new HashMap<String, ElementFormatSetting>();
            makeElementFormatSetting(fViewer, TreePath.EMPTY, format3, 1, 0, map3);
            ArrayList<ElementFormatSetting> elementFormats3 = new ArrayList<ElementFormatSetting>(map3.values());
            HashMap<String, ElementFormatSetting> expMap3 = new HashMap<String, ElementFormatSetting>();
            makeElementFormatSetting(fViewer, TreePath.EMPTY, format3, depth, 0, expMap3);
            ArrayList<ElementFormatSetting> expectElementFormats3 = new ArrayList<ElementFormatSetting>(expMap3.values());
            setFormatAndValidate(preferenceFormat, elementFormats3, expectElementFormats3, true, true, true);
        }        
    }

    /**
     * Test that when the preference format is invalid, each element can still change to a format.
     * Also, each element can restore to the invalid preference format such that 
     * the element uses first available format from service.
     */
    public void testChangeElementFormatWithInvalidPreference() {
    	recreateViewModel();
        String preferenceFormat = IFormattedValues.NATURAL_FORMAT;
        setInput(preferenceFormat);
        // set preference format to an invalid format and verify
        setInvalidPreferenceAndVerify();
        // set each element to a format, and verify
        HashMap<String, ElementFormatSetting> map = new HashMap<String, ElementFormatSetting>();
        String[] format = { IFormattedValues.HEX_FORMAT, IFormattedValues.DECIMAL_FORMAT,
        		IFormattedValues.OCTAL_FORMAT, IFormattedValues.BINARY_FORMAT,
        		IFormattedValues.NATURAL_FORMAT };
        makeElementFormatSetting(fViewer, TreePath.EMPTY, format, -1, 0, map);
        ArrayList<ElementFormatSetting> elementFormats = new ArrayList<ElementFormatSetting>(map.values());
        setFormatAndValidate("invalid format", elementFormats, elementFormats, false, false, false);
        // Restore each element to preference format which is an invalid format
        for (ElementFormatSetting e : elementFormats) {
        	e.formatId = null;
        }
        fViewerListener.reset();
        fViewerListener.addUpdates(TreePath.EMPTY, ((TestElementVMContext)fViewer.getInput()).getElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        fVMListener.reset();
        fVMListener.addUpdates(TreePath.EMPTY, fModel.getRootElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        if (fVMProvider instanceof IElementFormatProvider) {
        	IElementFormatProvider ep = ((IElementFormatProvider) fVMProvider);
        	for (ElementFormatSetting es : elementFormats) {
            	ep.setActiveFormat(fViewer.getPresentationContext(),
            			es.nodes.toArray(new IVMNode[es.nodes.size()]), fViewer.getInput(),
            			es.elementPaths.toArray(new TreePath[es.elementPaths.size()]), es.formatId);
        	}
        }
        while (!fViewerListener.isFinished(ALL_UPDATES_COMPLETE | PROPERTY_UPDATES) || !fVMListener.isFinished(CONTENT_UPDATES | PROPERTY_UPDATES)) {
            if (!fDisplay.readAndDispatch ()) {
            	fDisplay.sleep ();
            }
        }
        // verify service's first available format is used
        validateModel(IFormattedValues.HEX_FORMAT, " (" + FormattedValueVMUtil.getFormatLabel(IFormattedValues.HEX_FORMAT) + ")",
        		DummyFormattedValueService.DUMMY_FORMAT, " (" + DummyFormattedValueService.DUMMY_FORMAT + ")");        
    }

    /**
     * Test that when an element is set to to an invalid format, the element uses preference format.
     */
    public void testInvalidElementFormat() {
    	recreateViewModel();
        String preferenceFormat = IFormattedValues.NATURAL_FORMAT;
        setInput(preferenceFormat);
        // set each element to an invalid format
        setElementInvalidFormat();
        // verify preference format is used when element format is invalid
        validateModel(preferenceFormat, ""); 
    }
    
    /**
     * Test that when an element is set to to an invalid format and the preference format is invalid,
     * the element uses first available format from service.
     */
    public void testInvalidElementFormatWithInvalidPreference() {
    	recreateViewModel();
        String preferenceFormat = IFormattedValues.NATURAL_FORMAT;
        setInput(preferenceFormat);
        // set preference format to an invalid format and verify
        setInvalidPreferenceAndVerify();
        // set each element to an invalid format
        setElementInvalidFormat();
        // verify service's first available format is used when element format and preference format are invalid
        validateModel(IFormattedValues.HEX_FORMAT, " (" + FormattedValueVMUtil.getFormatLabel(IFormattedValues.HEX_FORMAT) + ")",
        		DummyFormattedValueService.DUMMY_FORMAT, " (" + DummyFormattedValueService.DUMMY_FORMAT + ")");        
    }

    /**
     * Test that element format can be persisted in memento and viewer
     * can restore to the persisted settings.
     */
    public void testPersistElementFormat() {
    	recreateViewModel();
        String preferenceFormat = IFormattedValues.HEX_FORMAT;
        setInput(IFormattedValues.NATURAL_FORMAT);
        setFormatAndValidate(preferenceFormat, false, false, false);
        // set each element to a format, and verify
        HashMap<String, ElementFormatSetting> map = new HashMap<String, ElementFormatSetting>();
        String[] format = { IFormattedValues.HEX_FORMAT, IFormattedValues.DECIMAL_FORMAT,
        		IFormattedValues.OCTAL_FORMAT, IFormattedValues.BINARY_FORMAT,
        		IFormattedValues.NATURAL_FORMAT };
        makeElementFormatSetting(fViewer, TreePath.EMPTY, format, -1, 0, map);
        ArrayList<ElementFormatSetting> elementFormats = new ArrayList<ElementFormatSetting>(map.values());
        setFormatAndValidate(preferenceFormat, elementFormats, elementFormats, false, false, false);
        // save settings
		XMLMemento memento = XMLMemento.createWriteRoot("TEST");
		if (fViewer instanceof TreeModelViewer == false)
			return;
		((TreeModelViewer) fViewer).saveState(memento);
		// throw away any settings inside the viewer and create a new viewer
		// with memento settings, this is the same effect resulted from closing
		// and opening workspace again.
		recreateViewer();
		if (fViewer instanceof TreeModelViewer == false)
			return;
		((TreeModelViewer) fViewer).initState(memento);
		setInput(IFormattedValues.NATURAL_FORMAT);
		preferenceFormat = (String) fViewer.getPresentationContext().getProperty(PROP_FORMATTED_VALUE_FORMAT_PREFERENCE);
		validateModel(elementFormats, preferenceFormat, "", preferenceFormat, "");
    }

    /**
     * helper class that stores some element paths and nodes using a certain format 
     */
    class ElementFormatSetting {
    	ArrayList<IVMNode> nodes;
    	ArrayList<TreePath> elementPaths;
    	String formatId;
    }

    /**
     * helper to create element format settings for all children paths of a given element path.
     * Tree paths at the same level will use the same format. Tree paths at different
     * levels will use different formats.
     * @param _viewer tree viewer
     * @param path given element path
     * @param formats formats to rotate for different levels of children tree paths
     * @param levelStop depth to stop recursively walk down the children.  
     * @param levelIndex index to a format for a level of children
     * @param result store the created element format settings
     */
    void makeElementFormatSetting(ITreeModelViewer _viewer, TreePath path, String[] formats,
    		int levelStop, int levelIndex, HashMap<String, ElementFormatSetting> result) {
    	if (levelStop >= 0 && levelIndex >= levelStop)
    		return;
    	IInternalTreeModelViewer viewer = (IInternalTreeModelViewer)_viewer;
    	int childCount = viewer.getChildCount(path);
    	if (childCount == 0)
    		return;
    	String fmt = formats[levelIndex % formats.length];
    	ElementFormatSetting setting = result.get(fmt);
    	if (setting == null) {
    		setting = new ElementFormatSetting();
        	setting.nodes = new ArrayList<IVMNode>(childCount);
        	setting.elementPaths = new ArrayList<TreePath>(childCount);
        	setting.formatId = fmt;
        	result.put(fmt, setting);
    	}
        for (int i = 0; i < childCount; i++) {
            Object viewerObject = viewer.getChildElement(path, i);
            if (viewerObject instanceof TestElementVMContext) {
                TreePath childPath = path.createChildPath(viewerObject);
                setting.nodes.add(((TestElementVMContext)viewerObject).getVMNode());
                setting.elementPaths.add(childPath);
               	makeElementFormatSetting(viewer, childPath, formats, levelStop, levelIndex + 1, result);
            }
        }
    }

    /**
     * helper to set element to an invalid format
     */
    void setElementInvalidFormat() {
        fViewerListener.reset();
        fViewerListener.addUpdates(TreePath.EMPTY, ((TestElementVMContext)fViewer.getInput()).getElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        fVMListener.reset();
        fVMListener.addUpdates(TreePath.EMPTY, fModel.getRootElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        HashMap<String, ElementFormatSetting> map = new HashMap<String, ElementFormatSetting>();
        String[] format = { "invalid element format" };
        makeElementFormatSetting(fViewer, TreePath.EMPTY, format, -1, 0, map);
        ArrayList<ElementFormatSetting> elementFormats = new ArrayList<ElementFormatSetting>(map.values());
        if (fVMProvider instanceof IElementFormatProvider) {
        	IElementFormatProvider ep = ((IElementFormatProvider) fVMProvider);
        	for (ElementFormatSetting es : elementFormats) {
            	ep.setActiveFormat(fViewer.getPresentationContext(),
            			es.nodes.toArray(new IVMNode[es.nodes.size()]), fViewer.getInput(),
            			es.elementPaths.toArray(new TreePath[es.elementPaths.size()]), es.formatId);
        	}
        }
        while (!fViewerListener.isFinished(ALL_UPDATES_COMPLETE | PROPERTY_UPDATES) || !fVMListener.isFinished(CONTENT_UPDATES | PROPERTY_UPDATES)) {
            if (!fDisplay.readAndDispatch ()) {
            	fDisplay.sleep ();
            }
        }
    }

    /**
     * helper to set preference to an invalid format and verify.
     */
    void setInvalidPreferenceAndVerify() {
        fViewerListener.reset();
        fViewerListener.addUpdates(TreePath.EMPTY, ((TestElementVMContext)fViewer.getInput()).getElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        fVMListener.reset();
        fVMListener.addUpdates(TreePath.EMPTY, fModel.getRootElement(), -1, ALL_UPDATES_COMPLETE | PROPERTY_UPDATES);
        fViewer.getPresentationContext().setProperty(PROP_FORMATTED_VALUE_FORMAT_PREFERENCE, "invalid format");
        while (!fViewerListener.isFinished(ALL_UPDATES_COMPLETE | PROPERTY_UPDATES) || !fVMListener.isFinished(CONTENT_UPDATES | PROPERTY_UPDATES)) {
            if (!fDisplay.readAndDispatch ()) {
            	fDisplay.sleep ();
            }
        }
        validateModel(IFormattedValues.HEX_FORMAT, " (" + FormattedValueVMUtil.getFormatLabel(IFormattedValues.HEX_FORMAT) + ")",
        		DummyFormattedValueService.DUMMY_FORMAT, " (" + DummyFormattedValueService.DUMMY_FORMAT + ")");        
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
            boolean expectCacheMissError) {
    	setFormatAndValidate(formatId, null, null, expectContentCached, expectFormattedValuesCached, expectCacheMissError);
    }

    private void setFormatAndValidate(
        String formatId,
        ArrayList<ElementFormatSetting> setElementFormats,
        ArrayList<ElementFormatSetting> expectElementFormats,
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
        fVMListener.addUpdates(TreePath.EMPTY, fModel.getRootElement(), vmListenerLevel, vmUpdateFlags);
        
        fFormattedValuesListener.reset();
        if (expectFormattedValuesCached && !expectCacheMissError) {
            fFormattedValuesListener.setCachedFormats(new String[] {formatId} );
        }

        if (fVMProvider instanceof IElementFormatProvider && setElementFormats != null) {
        	IElementFormatProvider ep = ((IElementFormatProvider) fVMProvider);
        	for (ElementFormatSetting es : setElementFormats) {
            	ep.setActiveFormat(fViewer.getPresentationContext(),
            			es.nodes.toArray(new IVMNode[es.nodes.size()]), fViewer.getInput(),
            			es.elementPaths.toArray(new TreePath[es.elementPaths.size()]), es.formatId);
        	}
        } else {
            // Set the new number format to the viewer.
            fViewer.getPresentationContext().setProperty(PROP_FORMATTED_VALUE_FORMAT_PREFERENCE, formatId);
        }
        

        while (!fViewerListener.isFinished(ALL_UPDATES_COMPLETE | PROPERTY_UPDATES) || !fVMListener.isFinished(CONTENT_UPDATES | PROPERTY_UPDATES)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        
        if (expectCacheMissError) {
            try {
                validateModel(expectElementFormats, formatId, "", formatId, "");
                throw new RuntimeException("Expected validateModel to fail");
            }
            catch(AssertionFailedError e) {
                // expected
            }
        } else {
            validateModel(expectElementFormats, formatId, "", formatId, "");
        }
        
        if (expectCacheMissError) {
            String formatProperty = FormattedValueVMUtil.getPropertyForFormatId(formatId);
            
            Assert.assertTrue(fFormattedValuesListener.getFormattedValuesCompleted().isEmpty());
            Assert.assertFalse(fFormattedValuesListener.getPropertiesUpdates().isEmpty());
            for (IPropertiesUpdate update : fFormattedValuesListener.getPropertiesUpdates()) {
                PropertiesUpdateStatus status = (PropertiesUpdateStatus)update.getStatus();
                assertEquals(IDsfStatusConstants.INVALID_STATE, status.getCode());                
                ElementFormatSetting elementFormat = null;
                if (expectElementFormats != null) {
                	TreePath viewerPath = update.getElementPath();
                	for (ElementFormatSetting es : expectElementFormats) {
                		if (es.elementPaths.indexOf(viewerPath) >= 0) {
                			elementFormat = es;
                			break;
                		}
                	}
                }
                if (elementFormat != null) {
                    assertEquals("Cache contains stale data. Refresh view.", status.getStatus(
                    		FormattedValueVMUtil.getPropertyForFormatId(elementFormat.formatId)).getMessage());
                } else {
                   assertEquals("Cache contains stale data. Refresh view.", status.getStatus(formatProperty).getMessage());
                }
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
        validateModel(formatId, suffix, formatId, suffix);
    }
    
    private void validateModel(final String formatId, final String suffix, final String dummyFormatId, final String dummySuffix) {
        validateModel(null, formatId, suffix, dummyFormatId, dummySuffix);
    }
    
    private void validateModel(final ArrayList<ElementFormatSetting> elementFormats,
    		final String formatId, final String suffix, final String dummyFormatId, final String dummySuffix) {
        fModel.validateData(
            fViewer, TreePath.EMPTY, 
            new TestElementValidator() {
                public void validate(TestElement modelElement, TestElement viewerElement, TreePath viewerPath) {
                    ViewerLabel label = fViewer.getElementLabel(viewerPath, TestModelCachingVMProvider.COLUMN_ID);
                    assertEquals(modelElement.getID(), label.getText());
                    ElementFormatSetting elementFormat = null;
                    if (elementFormats != null) {
                    	for (ElementFormatSetting es : elementFormats) {
                    		if (es.elementPaths.indexOf(viewerPath) >= 0) {
                    			elementFormat = es;
                    			break;
                    		}
                    	}
                    }
                    label = fViewer.getElementLabel(viewerPath, TestModelCachingVMProvider.COLUMN_FORMATTED_VALUE);
                    if (elementFormat == null || elementFormat.formatId == null) {
                        assertEquals(fModel.getFormattedValueText(modelElement, formatId) + suffix, label.getText());
                    } else {
                    	String suffix = elementFormat.formatId.equals(formatId) ? "" : 
                    		" (" + FormattedValueVMUtil.getFormatLabel(elementFormat.formatId) + ")";
                        assertEquals(fModel.getFormattedValueText(modelElement, elementFormat.formatId) + suffix , label.getText());
                    }

                    label = fViewer.getElementLabel(viewerPath, TestModelCachingVMProvider.COLUMN_DUMMY_VALUE);
                    if (elementFormat == null || elementFormat.formatId == null) {
                        assertEquals(dummyFormatId + dummySuffix, label.getText());
                    } else {
                    	String suffix = elementFormat.formatId.equals(formatId) ? "" : 
                    		" (" + FormattedValueVMUtil.getFormatLabel(elementFormat.formatId) + ")";
                        assertEquals(elementFormat.formatId + suffix, label.getText());
                    }
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
