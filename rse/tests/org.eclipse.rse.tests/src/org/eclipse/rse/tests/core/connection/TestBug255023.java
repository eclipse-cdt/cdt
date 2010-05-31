/********************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [247908] extract from RSEConnectionTestCase
 ********************************************************************************/
package org.eclipse.rse.tests.core.connection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;

/**
 * Basic connection tests.
 */
public class TestBug255023 extends RSEBaseConnectionTestCase {

	public TestBug255023(String name) {
		super(name);
	}

	/**
	 * Creating/disposing elements in the systemView can lead
	 * to "Widget is disposed" exception when Refresh is called
	 * rarely so there is much to refresh. This might be due to
	 * the elementComparer only comparing by absolute name.
	 */
	public void testBug255023() throws Exception {
		// -test-author-:MartinOberhuber
		if (isTestDisabled())
			return;
		Job j = new Job("testBug255023") {

			protected IStatus run(IProgressMonitor monitor) {
				try {
					ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
					ISystemProfile prof = sr.createSystemProfile("Foo", true);
					IRSESystemType st = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_SSH_ONLY_ID);
					IHost h1 = sr.createHost("Foo", st, "vxsim0", "localhost", "vxsim0");
					IHost h2 = sr.createHost("Foo", st, "vxsim1", "localhost", "vxsim1");
					IHost h3 = sr.createHost("Foo", st, "vxsim2", "localhost", "vxsim2");
					sr.fireEvent(new SystemResourceChangeEvent(sr, ISystemResourceChangeEvents.EVENT_REFRESH, null));
					// flushEventQueue();
					Thread.sleep(10000);
					sr.deleteHost(h1);
					sr.deleteHost(h2);
					sr.deleteHost(h3);
					// // Firing a refresh event here, after deleting the hosts
					// // but before adding the new one, makes the bug
					// disappear.
					// // Perhaps a correct fix would be that our content
					// provider
					// // refreshes the view right away by means of a listener,
					// // instead of relying on forced manual refresh only.
					// sr.fireEvent(new SystemResourceChangeEvent(sr,
					// ISystemResourceChangeEvents.EVENT_REFRESH, null));
					IHost h4 = sr.createHost("Foo", st, "vxsim1", "localhost", "vxsim1");
					sr.fireEvent(new SystemResourceChangeEvent(sr, ISystemResourceChangeEvents.EVENT_REFRESH, null));
					// flushEventQueue(); // will throw exception in main Thread!
					Thread.sleep(10000);
					sr.deleteSystemProfile(prof);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				return Status.OK_STATUS;
			}
		};
		j.schedule();
		while (j.getState() != Job.NONE) {
			flushEventQueue();
		}
	}

}
