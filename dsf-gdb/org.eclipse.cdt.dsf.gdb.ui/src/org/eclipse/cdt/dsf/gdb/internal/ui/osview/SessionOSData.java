/*******************************************************************************
 * Copyright (c) 2011, 2016 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *     Teodor Madan (Freescale Semiconductor) - Bug 486521: attaching to selected process
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.osview;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.IResourceClass;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS2.IResourcesInformation;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

/** Responsible for fetching and storing OS awareness data for a
 * specific DSF session.
 *
 * @since 2.4
 */
public class SessionOSData {

	private DsfSession fSession;
	private DsfServicesTracker fTracker;
	private IGDBHardwareAndOS2 fHardwareOs;
	private ICommandControlDMContext fContext;

	private IResourceClass[] fResourceClasses = new IResourceClass[0];
	private Map<String, OSData> fExistingData = new HashMap<>();
	private Map<String, Date> fTimestamp = new HashMap<>();

	private Listener fUIListener;
	private Control fUIControl;

	private boolean fWaitingForSession = true;
	private boolean fSupported = true;
	private boolean fAcceptingCommands = false;
	private boolean fFetchingClasses = false;
	private boolean fFetchingContent = false;

	public SessionOSData(DsfSession session, final ICommandControlDMContext executionContext) {
		fSession = session;
		BundleContext c = GdbUIPlugin.getDefault().getBundle().getBundleContext();
		fTracker = new DsfServicesTracker(c, fSession.getId());
		fContext = executionContext;

		final DsfExecutor executor = fSession.getExecutor();
		executor.submit(new DsfRunnable() {

			@Override
			public void run() {

				IMIRunControl runControl = fTracker.getService(IMIRunControl.class);
				fAcceptingCommands = runControl.isTargetAcceptingCommands();

				fSession.addServiceEventListener(SessionOSData.this, null);

				fHardwareOs = fTracker.getService(IGDBHardwareAndOS2.class);

				if (fHardwareOs == null) {
					fSupported = false;
					notifyUI();
					return;
				}

				if (fHardwareOs.isAvailable()) {
					fetchClasses();
				}
			}
		});
	}

	@ConfinedToDsfExecutor("")
	private void fetchClasses() {
		fWaitingForSession = false;
		fFetchingClasses = true;
		fHardwareOs.getResourceClasses(fContext,
				new DataRequestMonitor<IResourceClass[]>(fSession.getExecutor(), null) {
					@Override
					@ConfinedToDsfExecutor("fExecutor")
					protected void handleCompleted() {

						if (isSuccess()) {
							fResourceClasses = getData();
							if (fResourceClasses.length == 0)
								fSupported = false;
						} else {
							fSupported = false;
						}
						fFetchingClasses = false;
						notifyUI();
					}
				});
	}

	@DsfServiceEventHandler
	public void eventDispatched(DataModelInitializedEvent e) {
		// If we see this event, it necessary means that by the time we've set event listener,
		// isAvailable() was returning false, so we need to fetch classes now.
		if (fHardwareOs != null)
			fetchClasses();
	}

	public boolean waitingForSessionInitialization() {
		return fWaitingForSession;
	}

	public boolean osResourcesSupported() {
		return fSupported;
	}

	public void dispose() {
		fSession.removeServiceEventListener(SessionOSData.this);
		fTracker.dispose();
	}

	public IResourceClass[] getResourceClasses() {
		return fResourceClasses;
	}

	/** Returns OS awareness data for given resource class that
	 * was previously fetched, or null if none was ever fetched.
	 */
	public OSData existingData(String resourceClass) {
		return fExistingData.get(resourceClass);
	}

	/** Returns the timestamp at which data for 'resourceClass' have
	 * been obtained.
	 * @pre existingData(resourceClass) != null
	 */
	public Date timestamp(String resourceClass) {
		return fTimestamp.get(resourceClass);
	}

	/** Returns true if fresh data can be fetched at this time.
	 * Generally, it's possible if we're not fetching data already,
	 * and if GDB is accepting commands right now.
	 *
	 */
	public boolean canFetchData() {
		return fAcceptingCommands && !fFetchingContent;
	}

	public boolean fetchingClasses() {
		return fFetchingClasses;
	}

	/** Returns true if we're presently fetching data. This can
	 * be used to provide some feedback to the user.
	 */
	public boolean fetchingContent() {
		return fFetchingContent;
	}

	/** Fetches up-to-date data for resourceClass.  Listeners will be
	 * informed when the new data is available.  */
	public void fetchData(final String resourceClass) {
		fFetchingContent = true;
		notifyUI();

		final DsfExecutor executor = fSession.getExecutor();
		executor.submit(new DsfRunnable() {

			@Override
			public void run() {
				fHardwareOs.getResourcesInformation(fContext, resourceClass,
						new DataRequestMonitor<IResourcesInformation>(executor, null) {

							@Override
							@ConfinedToDsfExecutor("fExecutor")
							protected void handleCompleted() {

								fFetchingContent = false;

								if (isSuccess()) {
									OSData data = new OSData(resourceClass, getData());
									fExistingData.put(resourceClass, data);
									fTimestamp.put(resourceClass, new Date());
								} else {
									StatusManager.getManager().handle(getStatus(), StatusManager.SHOW);
								}
								notifyUI();
							}
						});
			}
		});

	}

	public interface Listener {
		void update();
	}

	/** Setup the listener that will be notified whenever externally
	 * visible state changes. The listener will always be invoked
	 * in the UI thread. 'control' is the control associated with
	 * the listener. The listener will not be called if the control
	 * is disposed.
	 */
	public void setUIListener(Listener listener, Control control) {
		fUIListener = listener;
		fUIControl = control;
	}

	private void notifyUI() {

		final Control c = fUIControl;
		if (c != null && !c.isDisposed())
			// There be dragons: if you try to use c.getDisplay() below, then this Runnable will not
			// run until resource view is actually visible. And it will also block other interesting
			//  async/job runnables, like perspective switch runnable using during debug launch,
			//   causing launch to be stuck at random point.
			//
			Display.getDefault().asyncExec(() -> {

				if (!c.isDisposed())
					fUIListener.update();
			});

	}

	@DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
		if (e instanceof IContainerResumedDMEvent) {
			// This event is raised only in all-stop. It does not
			// seem to be possible to issue -info-os in all-stop,
			// regardless of whether target-async is in effect, and
			// according to DSF folks, all-stop+target-async will
			// not work anyway. So, we assume that no commands
			// can be issued right now.
			fAcceptingCommands = false;
			notifyUI();
		}
	}

	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		if (e instanceof IContainerSuspendedDMEvent) {
			fAcceptingCommands = true;
			notifyUI();
		}
	}

	/**
	 * @return the fContext
	 */
	public ICommandControlDMContext getContext() {
		return fContext;
	}
}
