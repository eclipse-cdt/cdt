/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class CommandTimeoutTest extends BaseTestCase {

	private static boolean fgTimeoutEnabled = false;
	private static int fgTimeout = IGdbDebugPreferenceConstants.COMMAND_TIMEOUT_VALUE_DEFAULT;
	
    @BeforeClass
	public static void doBeforeClass() throws Exception {
		fgTimeoutEnabled = Platform.getPreferencesService().getBoolean( 
				GdbPlugin.PLUGIN_ID, 
				IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT, 
				false, 
				null );		
		fgTimeout = Platform.getPreferencesService().getInt( 
				GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE, 
				IGdbDebugPreferenceConstants.COMMAND_TIMEOUT_VALUE_DEFAULT,
				null );		
    }
    
    @Override
	public void doBeforeTest() throws Exception {
		setLaunchAttributes();
		// Can't run the launch right away because each test needs to first set some 
		// parameters.  The individual tests will be responsible for starting the launch. 
		// Save the original values of the timeout-related preferences
    }

	@Override
	public void doAfterTest() throws Exception {
		// Don't call super here, as the launch is already terminated

		// Restore the timeout preferences
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode( GdbPlugin.PLUGIN_ID );
		node.putBoolean( IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT, fgTimeoutEnabled );
		node.putInt( IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE, fgTimeout );
	}

	protected void performLaunchAndTerminate() throws Exception {
	   	// perform the launch
        doLaunch();
        // terminate the launch right away
		super.doAfterTest();
	}

	@Override
	protected boolean reallyLaunchGDBServer() {
		return false;
	}

	/**
	 * Enables the timeout support and sets the timeout value to minimal - 1.
	 * Launch is expected to timeout on the first gdb command.
	 */
	@Test
	public void firstCommandTimedOut() {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode( GdbPlugin.PLUGIN_ID );
		node.putBoolean( IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT, true );
		node.putInt( IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE, 1 );

		try {
			performLaunchAndTerminate();
			Assert.fail( "Launch is expected to fail" );
		}
		catch( Exception e ) {
			processException( e );
		}
	}

	/**
	 * Tries to connect to gdbserver without starting it.
	 * Launch is expected to timeout on "target-remote" command.
	 */
	@Test
	public void remoteConnectionTimedOut() {
		if ( !isRemoteSession() )
			return;

		IEclipsePreferences node = InstanceScope.INSTANCE.getNode( GdbPlugin.PLUGIN_ID );
		node.putBoolean( IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT, true );
		node.putInt( IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE, 1000 );

		try {
			performLaunchAndTerminate();
			Assert.fail( "Launch is expected to fail" );
		}
		catch( Exception e ) {
			processException( e );
		}
	}

	/**
	 * Checks whether the given exception is an instance of {@link CoreException} 
	 * with the status code 20100 which indicates that a gdb command has been timed out.
	 */
	private void processException( Exception e ) {
		if ( e instanceof DebugException ) {
			Throwable t = getExceptionCause( e );
			Assert.assertTrue(
				"Unexpected exception",
				t instanceof CoreException && ((CoreException)t).getStatus().getCode() == 20100 );
		}
		else {
			Assert.fail( "Unexpected exception type" );
		}
	}

	private Throwable getExceptionCause(Throwable e) {
		Throwable current = e;
		while ( current instanceof CoreException ) {
			Throwable t = ((CoreException)current).getCause();
			if ( t == null )
				break;
			current = t;
		}
		return current;
	}
}
