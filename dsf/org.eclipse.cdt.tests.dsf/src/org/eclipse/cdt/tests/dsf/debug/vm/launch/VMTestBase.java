/*******************************************************************************
 * Copyright (c) 2007 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.debug.vm.launch;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionStartedListener;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.launch.PDALaunch;
import org.eclipse.cdt.tests.dsf.ServiceEventWaitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * This is the base class for the GDB/MI Unit tests.
 * It provides the @Before and @After methods which setup
 * and teardown the launch, for each test.
 * If these methods are overwridden by a subclass, the new method
 * must call super.baseSetup or super.baseTeardown itself, if this
 * code is to be run.
 */
public class VMTestBase {

    private PDALaunch fLaunch;
    public PDALaunch getPDALaunch() { return fLaunch; }

    @BeforeClass
    public static void baseBeforeClassMethod() {
        DebugUIPlugin.getDefault().getPreferenceStore().setValue(
            IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE, MessageDialogWithToggle.NEVER);
        DebugUIPlugin.getDefault().getPreferenceStore().setValue(
            IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND, MessageDialogWithToggle.NEVER);
    }
    
    @Before
    public void baseBeforeMethod() throws Exception {
        Map<String, Object> attrs = new HashMap<String, Object>();
        
        initLaunchAttributes(attrs);
        
        System.out.println("====================================================================");
		System.out.println("Launching test application: " + attrs.get(PDAPlugin.ATTR_PDA_PROGRAM));
		System.out.println("====================================================================");
		
 		ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();
 		ILaunchConfigurationType lcType = launchMgr.getLaunchConfigurationType("org.eclipse.cdt.examples.dsf.pda.launchType");
 		assert lcType != null;

 		ILaunchConfigurationWorkingCopy lcWorkingCopy = lcType.newInstance(
 				null, 
 				launchMgr.generateUniqueLaunchConfigurationNameFrom("Test Launch")); //$NON-NLS-1$
 		assert lcWorkingCopy != null;
 		lcWorkingCopy.setAttributes(attrs);

 		final ILaunchConfiguration lc = lcWorkingCopy.doSave();
 		assert lc != null;

        final ServiceEventWaitor<?> eventWaitor[] = new ServiceEventWaitor<?>[1];
        
        SessionStartedListener newSessionListener = new SessionStartedListener() {
            public void sessionStarted(DsfSession session) {
                eventWaitor[0] = new ServiceEventWaitor<IStartedDMEvent>(session, IStartedDMEvent.class);
            }
        };

        DsfSession.addSessionStartedListener(newSessionListener);
        try {
            fLaunch = (PDALaunch)lc.launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor());
            Assert.assertNotNull(fLaunch);
            Assert.assertNotNull(eventWaitor[0]);
            Assert.assertSame(fLaunch.getSession(), eventWaitor[0].getSession());
			eventWaitor[0].waitForEvent(60000);
		} finally {
		    DsfSession.removeSessionStartedListener(newSessionListener);
		    if (eventWaitor[0] != null) {
		        eventWaitor[0].dispose();
		    }
		}
	}

    protected void initLaunchAttributes(Map<String, Object> attrs) {
        attrs.put(PDAPlugin.ATTR_PDA_PROGRAM, getProgramPath());
    }
    
    protected String getProgramPath() {
        File programFile = PDAPlugin.getFileInPlugin(new Path("samples/example.pda"));
        return programFile.getPath();
    }
    
    protected ILaunch getLaunch() {
        return fLaunch;
    }
    
 	@After
	public void baseAfterMethod() throws Exception {
 		if (fLaunch != null) {
 			fLaunch.terminate();
            fLaunch = null;
 		}
 		
	}
 	
}
