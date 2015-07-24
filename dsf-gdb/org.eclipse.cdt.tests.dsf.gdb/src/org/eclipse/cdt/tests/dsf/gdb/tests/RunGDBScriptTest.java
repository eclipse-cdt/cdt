/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBRunControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class RunGDBScriptTest extends BaseTestCase {

	/**
	 * Path to the script directory
	 */
	protected static final String SCRIPTS_PATH = "data/scripts/";

	private static final String SCRIPT_FILE = "gdb_script.txt";

	private DsfSession fSession;

	private DsfServicesTracker fServicesTracker;    

	private IGDBRunControl fRunControl;

	private IContainerDMContext fContainerDmc;

	public RunGDBScriptTest() {
	}

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();

		fSession = getGDBLaunch().getSession();
		
        fSession.getExecutor().submit(new DsfRunnable() {			
			@Override
			public void run() {
	           	fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
	            fRunControl = fServicesTracker.getService(IGDBRunControl.class);
			}
		}).get();

        fContainerDmc = SyncUtil.getContainerContext();
	}

	@Override
	public void doAfterTest() throws Exception {
		if (fServicesTracker != null) {
			fServicesTracker.dispose();
		}
		super.doAfterTest();
	}

	/**
	 * Runs the following GDB script and waits for {@link ISuspendedDMEvent}.
	 * 
	 * 	break GDBMIGenericTestApp.cc:10
	 *	continue	 
	 */
	@Test
	public void runGDBScript() throws Throwable {
		final File file = new File(SCRIPTS_PATH + SCRIPT_FILE);
		assertTrue(file.exists() && !file.isDirectory());

        ServiceEventWaitor<ISuspendedDMEvent> suspendedEventWaitor = 
            	new ServiceEventWaitor<>(getGDBLaunch().getSession(), ISuspendedDMEvent.class);

		Query<Void> query = new Query<Void>() {
			@Override
			protected void execute(final DataRequestMonitor<Void> rm) {
				fRunControl.canRunGDBScript(fContainerDmc, new ImmediateDataRequestMonitor<Boolean>(rm) {
					@Override
					protected void handleSuccess() {
						if (getData()) {
							fRunControl.runGDBScript(
								fContainerDmc, 
								file.getAbsolutePath(), rm);
						}
						else {
							rm.done(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, "Can not run GDB script"));
						}
					}
				});
			}
		};
		fSession.getExecutor().execute(query);
		query.get(TestsPlugin.massageTimeout(5000), TimeUnit.MILLISECONDS);
        
        suspendedEventWaitor.waitForEvent(TestsPlugin.massageTimeout(10000));
	}
}
