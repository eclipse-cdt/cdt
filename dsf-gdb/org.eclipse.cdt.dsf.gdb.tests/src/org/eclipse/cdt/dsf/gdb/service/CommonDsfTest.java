/*******************************************************************************
 * Copyright (c) 2017 Renesas Electronics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros (Renesas) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.model.DsfLaunch;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ISourceLocator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Helper base class for running DSF related tests
 */
public class CommonDsfTest extends CommonTest {
	
	protected static DsfSession session = null;
	
	/**
	 * Setup the test. 
	 * The session is typically configured once per class load, but we allow subclasses to override this,
	 * and sometimes re-setup during a test (or interactive debugging). 
	 * Therefore {@link Before} is used, not {@link BeforeClass}. 
	 */
	@Before
	public void setup() {
		if(session != null) {
			return; // Already set-up
		}
		
		doSetupSession();
	}
	
	protected void doSetupSession() {
		session = DsfSession.startSession(new DefaultDsfExecutor(GdbPlugin.PLUGIN_ID), GdbPlugin.PLUGIN_ID);
		
		registerLaunch();
	}

	@AfterClass
	public static void tearDown() {
		if(session != null) {
			DsfSession.endSession(session);
			session = null;
		}
	}
	
	protected ILaunchConfigurationType getCLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP);
	}
	
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	protected void registerLaunch() {
		ILaunchConfigurationWorkingCopy lc;
		try {
			lc = getCLaunchConfigType().newInstance(null, "TestLaunch");
		} catch(CoreException e) {
			throw assertFail();
		}
		ISourceLocator sourceLocator = null;
		DsfLaunch dsfLaunch = new DsfLaunch(lc, "debug", sourceLocator);
		session.registerModelAdapter(ILaunch.class, dsfLaunch);
	}
	
	protected RequestMonitor newRequestMonitor() {
		return new RequestMonitor(session.getExecutor(), null);
	}
	
	protected <T> DataRequestMonitor<T> newDataRequestMonitor() {
		return new DataRequestMonitor<>(session.getExecutor(), null);
	}

	
}

/**
 * Misc test utilities.
 */
class CommonTest {
	
	public static void assertTrue(boolean condition) {
		Assert.isTrue(condition);
	}
	
	public static RuntimeException assertFail() {
		Assert.isTrue(false);
		return null; // unreachable
	}
	
	public static RuntimeException assertFail(String message) {
		Assert.isTrue(false, message);
		return null; // unreachable
	}
	
	/* -----------------  ----------------- */
	
	@SafeVarargs
	public static <T> T[] array(T... elems) {
		return elems;
	}
	
}