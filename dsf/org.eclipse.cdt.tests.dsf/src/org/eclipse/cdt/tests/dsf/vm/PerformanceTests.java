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

import junit.framework.TestCase;

import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.IViewerUpdatesListenerConstants;
import org.eclipse.cdt.tests.dsf.vm.TestModel.TestElement;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.ui.PlatformUI;

/**
 * Tests to measure the performance of the viewer updates.  
 */
abstract public class PerformanceTests extends TestCase implements IViewerUpdatesListenerConstants {
    Display fDisplay;
    Shell fShell;
    DsfExecutor fDsfExecutor;
    DsfSession fDsfSession;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    TestModel fModel;
    TestModelVMAdapter fVMAdapter;
    TestModelVMProvider fVMProvider;
    
    public PerformanceTests(String name) {
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
        
        fListener = new TestModelUpdatesListener(fViewer, false, false);
       
        fModel = new TestModel(fDsfSession);
        fModel.setRoot( new TestElement(fModel, "root", new TestElement[0] ) ); 
        fModel.setElementChildren(TreePath.EMPTY, makeModelElements(fModel, getTestModelDepth(), "model"));
        fVMAdapter = new TestModelVMAdapter();
        fVMProvider = fVMAdapter.getTestModelProvider(fViewer.getPresentationContext());
        
        fShell.open ();
    }

    abstract protected ITreeModelContentProviderTarget createViewer(Display display, Shell shell);
    
    /**
     * @throws java.lang.Exception
     */
    @Override
    protected void tearDown() throws Exception {
        fVMAdapter.dispose();
        
        fListener.dispose();
        fViewer.getPresentationContext().dispose();
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        DsfSession.endSession(fDsfSession);
        fDsfExecutor.shutdown();
    }

    /**
     * Depth (size) of the test model to be used in the tests.  This number allows
     * the jface based tests to use a small enough model to fit on the screen, and 
     * for the virtual viewer to exercise the content provider to a greater extent.
     */
    abstract protected int getTestModelDepth();
    
    public void testRefreshStruct() {
        fViewer.setAutoExpandLevel(-1);

        TestElementVMContext rootVMC = fVMProvider.getElementVMContext(fViewer.getPresentationContext(), fModel.getRootElement());
        
        // Create the listener
        fListener.reset(TreePath.EMPTY, rootVMC.getElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(rootVMC);
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        //fModel.validateData(fViewer, TreePath.EMPTY);

        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            for (int i = 0; i < 100; i++) {
                // Update the model
                fModel.setAllAppendix(" - pass " + i);
                
                fListener.reset(TreePath.EMPTY, rootVMC.getElement(), -1, false, false);
                
                System.gc();
                meter.start();
                fVMProvider.postDelta(new ModelDelta(rootVMC.getElement(), IModelDelta.CONTENT));
                while (!fListener.isFinished(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE)) 
                    if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
                meter.stop();
                System.gc();
            }
            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }
    }

    public void _x_testRefreshStructOnePass() {
        fViewer.setAutoExpandLevel(-1);

        TestElementVMContext rootVMC = fVMProvider.getElementVMContext(fViewer.getPresentationContext(), fModel.getRootElement());
        
        // Create the listener
        fListener.reset(TreePath.EMPTY, rootVMC.getElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(rootVMC);
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) 
            if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        //fModel.validateData(fViewer, TreePath.EMPTY);

        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            System.gc();
            meter.start();
            for (int i = 0; i < 1000; i++) {
                // Update the model
                fModel.setAllAppendix(" - pass " + i);
                
                fListener.reset(TreePath.EMPTY, rootVMC.getElement(), -1, false, false);
                
                fVMProvider.postDelta(new ModelDelta(rootVMC.getElement(), IModelDelta.CONTENT));
                while (!fListener.isFinished(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE)) 
                    if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
            }
            System.gc();
            meter.stop();
            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }
    }

    public void _x_testRefreshStructReplaceElements() {
        fModel.setRoot( new TestElement(fModel, "root", new TestElement[0] ) ); 
        fModel.setElementChildren(TreePath.EMPTY, makeModelElements(fModel, getTestModelDepth(), "model"));
        
        fViewer.setAutoExpandLevel(-1);

        // Create the listener
        fListener.reset(TreePath.EMPTY, fModel.getRootElement(), -1, true, false); 

        // Set the input into the view and update the view.
        fViewer.setInput(fModel.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
        fModel.validateData(fViewer, TreePath.EMPTY);

        Performance perf = Performance.getDefault();
        PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
        try {
            for (int i = 0; i < 2000; i++) {
                // Update the model
                fModel.setElementChildren(TreePath.EMPTY, makeModelElements(fModel, getTestModelDepth(), "pass " + i));
                
                TestElement element = fModel.getRootElement();
                fListener.reset(TreePath.EMPTY, element, -1, false, false);
                
                meter.start();
                //fModel.postDelta(new ModelDelta(element, IModelDelta.CONTENT));
                while (!fListener.isFinished(ALL_UPDATES_COMPLETE | MODEL_CHANGED_COMPLETE)) 
                    if (!fDisplay.readAndDispatch ()) fDisplay.sleep ();
                //model.validateData(fViewer, TreePath.EMPTY);
                meter.stop();
                System.gc();
            }
            
            meter.commit();
            perf.assertPerformance(meter);
        } finally {
            meter.dispose();
        }
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
