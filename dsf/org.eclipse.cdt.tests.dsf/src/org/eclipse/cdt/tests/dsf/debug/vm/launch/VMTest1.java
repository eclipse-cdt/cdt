/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.debug.vm.launch;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.VMPropertiesUpdate;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.tests.dsf.IViewerUpdatesListenerConstants;
import org.eclipse.cdt.tests.dsf.vm.TestModelUpdatesListener;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Display;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 */
public class VMTest1 extends VMTestBase implements IViewerUpdatesListenerConstants {

    @Override
    protected String getProgramPath() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("samples/example.pda"));
        return programFile.getPath();
    }

    @Test
    public void testRun() throws Throwable {
        
        Display display = Display.getDefault();
        
        final VirtualTreeModelViewer dv = new VirtualTreeModelViewer(
            display, 0, new PresentationContext(IDebugUIConstants.ID_DEBUG_VIEW));
        
        TestModelUpdatesListener listener = new TestModelUpdatesListener(dv, false, false);

        // Wait for container expand delta, sent by the model upon DV install event.
        final boolean[] containerExpandReceived = new boolean[1];
        containerExpandReceived[0] = false;
        dv.addModelChangedListener(new IModelChangedListener() {
            public void modelChanged(IModelDelta delta, IModelProxy proxy) {
                delta.accept(new IModelDeltaVisitor() {
                    public boolean visit(IModelDelta delta, int depth) {
                        if (delta.getElement() instanceof IDMVMContext &&
                            ((IDMVMContext)delta.getElement()).getDMContext() instanceof IContainerDMContext &&
                            (delta.getFlags() & IModelDelta.EXPAND) != 0) 
                        {
                            containerExpandReceived[0] = true;
                            return false;
                        }
                        return true;
                    }
                });
            }
        });
        
        dv.setInput(DebugPlugin.getDefault().getLaunchManager());
        
        while(!containerExpandReceived[0]) {
            if (!display.readAndDispatch()) display.sleep();
        }
        
        listener.reset();
        
        // TODO: need to wait for the install delta for the launch to be processed
        while (!listener.isFinished(CONTENT_SEQUENCE_COMPLETE)) {
            if (!display.readAndDispatch()) display.sleep();
        }
        
        // Find our launch
        int launchIdx = dv.findElementIndex(TreePath.EMPTY, getLaunch());
        Assert.assertTrue(-1 != launchIdx);
        
        // Find the debug container
        TreePath launchPath = TreePath.EMPTY.createChildPath(getLaunch());
        int launchChildCount = dv.getChildCount(launchPath);
        IDMVMContext _containerVMC = null;
        for (int i = 0; i < launchChildCount; i++) {
            Object launchChild = dv.getChildElement(launchPath, i);
            if (launchChild instanceof IDMVMContext && 
                ((IDMVMContext)launchChild).getDMContext() instanceof IContainerDMContext) 
            {
                _containerVMC = (IDMVMContext)launchChild;
            }
        }
        Assert.assertNotNull(_containerVMC);
        final IDMVMContext containerVMC = _containerVMC;
        final TreePath containerPath = launchPath.createChildPath(containerVMC);
        final IElementPropertiesProvider containerPropProvider = 
            (IElementPropertiesProvider)containerVMC.getAdapter(IElementPropertiesProvider.class);
        Assert.assertNotNull(containerPropProvider);
        
        // Check if container is suspended.
        Query<Map<String,Object>> suspendedQuery = new Query<Map<String,Object>>() {
            @Override
            protected void execute(DataRequestMonitor<Map<String, Object>> rm) {
                Set<String> properties = new HashSet<String>();
                properties.add(ILaunchVMConstants.PROP_IS_SUSPENDED);
                
                containerPropProvider.update( new VMPropertiesUpdate[] {
                    new VMPropertiesUpdate(properties, containerPath, dv.getInput(), dv.getPresentationContext(), rm) });
            }
        };
        suspendedQuery.run();

        // Wait for the properties update to complete
        while (!suspendedQuery.isDone()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        
        Map<String,Object> properties = suspendedQuery.get();
        Assert.assertEquals(Boolean.TRUE, properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED));
        
    }
}
