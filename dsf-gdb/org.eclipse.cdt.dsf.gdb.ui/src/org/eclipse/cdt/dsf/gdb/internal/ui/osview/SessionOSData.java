/*******************************************************************************
 * Copyright (c) 2011, 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.osview;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.GDBRunControl_7_0_NS;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardwareAndOS;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

/* Responsible for fetching and storing OS awareness data for a 
 * specific DSF session.
 * 
 */

public class SessionOSData {
	
	private DsfSession fSession;
	private DsfServicesTracker fTracker;
	private IGDBHardwareAndOS fHardwareOs;
	private IExecutionDMContext fContext;
		
	private IGDBHardwareAndOS.IResourceClass[] fResourceClasses = new IGDBHardwareAndOS.IResourceClass[0];
	private Map<String, OSData> fExistingData = new HashMap<String, OSData>();
	private Map<String, Date> fTimestamp = new HashMap<String, Date>();
	
	private Listener fUIListener;
	private Control fUIControl;
	
	private boolean fSupported = true;
	private boolean fAcceptingCommands = false;
	private boolean fFetchingClasses = false;
	private boolean fFetchingContent = false;
	
	public SessionOSData(DsfSession session, final IExecutionDMContext executionContext)
	{
		fSession = session;
		BundleContext c = GdbUIPlugin.getDefault().getBundle().getBundleContext();
		fTracker = new DsfServicesTracker(c, fSession.getId());
		fHardwareOs = fTracker.getService(IGDBHardwareAndOS.class);
		fContext = executionContext;

		final DsfExecutor executor = fSession.getExecutor();
		executor.submit(new DsfRunnable() {
			
			@Override
			public void run() {
				
				fFetchingClasses = true;
				fHardwareOs.getResourceClasses(executionContext, new DataRequestMonitor<IGDBHardwareAndOS.IResourceClass[]>(fSession.getExecutor(), null) {
					@Override
					@ConfinedToDsfExecutor("fExecutor")
					protected void handleCompleted() {
						
						if (isSuccess())
						{
							fResourceClasses = getData();
							if (fResourceClasses.length == 0)
								fSupported = false;
						}
						else
						{
							fSupported = false;
						}
						fFetchingClasses = false;
						notifyUI();
						super.handleCompleted();
					}
				}); 
				
			}
		});
		
		installEventHandler(executionContext);
	}
	
	public boolean osResourcesSupported()
	{
		return fSupported;
	}
		
	public void dispose()
	{
		fSession.removeServiceEventListener(SessionOSData.this);
		fTracker.dispose();
	}
	
	public IGDBHardwareAndOS.IResourceClass[] getResourceClasses()
	{
		return fResourceClasses;
	}
		
	/** Returns OS awareness data for given resource class that
	 * was previously fetched, or null if none was ever fetched.
	 */
	public OSData existingData(String resourceClass)
	{
		return fExistingData.get(resourceClass);
	}
	
	/** Returns the timestamp at which data for 'resourceClass' have
	 * been obtained.
	 * @pre existingData(resourceClass) != null
	 */
	public Date timestamp(String resourceClass)
	{
		return fTimestamp.get(resourceClass);
	}
	
	/** Returns true if fresh data can be fetched at this time.
	 * Generally, it's possible if we're not fetching data already,
	 * and if GDB is accepting commands right now.
	 *   
	 */
	public boolean canFetchData()
	{
		return fAcceptingCommands && !fFetchingContent;
	}
	
	public boolean fetchingClasses()
	{
		return fFetchingClasses;
	}
	
	/** Returns true if we're presently fetching data. This can
	 * be used to provide some feedback to the user.
	 */
	public boolean fetchingContent()
	{
		return fFetchingContent;
	}
		
	/** Fetches up-to-date data for resourceClass.  Listeners will be
	 * informed when the new data is available.  */
	public void fetchData(final String resourceClass)
	{		
		fFetchingContent = true;
		notifyUI();
		
		final DsfExecutor executor = fSession.getExecutor();
		executor.submit(new DsfRunnable() {

			@Override
			public void run() {
				fHardwareOs.getResourcesInformation(fContext, resourceClass, new DataRequestMonitor<IGDBHardwareAndOS.IResourcesInformation>(executor, null) {
					
					@Override
					@ConfinedToDsfExecutor("fExecutor")
					protected void handleCompleted() {
						
						fFetchingContent = false;

						if (isSuccess())
						{
							OSData data = new OSData(resourceClass, getData());
							fExistingData.put(resourceClass, data);
							fTimestamp.put(resourceClass, new Date());
						}
						else
						{
							StatusManager.getManager().handle(getStatus(), StatusManager.BLOCK);
						}
						notifyUI();
					}
				});
			}
		});
		
		
	}
	
	public interface Listener
	{
		void update();
	}
	
	/** Setup the listener that will be notified whenever externally
	 * visible state changes. The listener will always be invoked
	 * in the UI thread. 'control' is the control associated with
	 * the listener. The listener will not be called if the control
	 * is disposed.
	 */
	public void setUIListener(Listener listener, Control control)
	{
		fUIListener = listener;
		fUIControl = control;
	}
		
	private void notifyUI()
	{
		if (!fUIControl.isDisposed())
			fUIControl.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (!fUIControl.isDisposed())
						fUIListener.update();					
				}
			});
	}
	
	private void installEventHandler(final IExecutionDMContext executionContext)
	{		
		fSession.getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {

				// In non-async mode, GDB naturally does not accept -info-os when any
				// command is running, and therefore we want to disable the "fetch" button
				// in UI in this case. Generally, async mode can be enabled independently
				// from non-stop, however DSF does not actually support all-stop async,
				// which leaves us with two cases:
				// - all-stop, no async
				// - non-stop, async
				// In the first case, we listen for resume/suspend events on some IExecutionDMContext
				// and change fAcceptingCommands accordingly. 
				// IExecutionContext we use, as long as it belongs to right GDB.
				boolean nonstop = fTracker.getService(GDBRunControl_7_0_NS.class) != null;
				if (!nonstop)
				{
					IRunControl runControl = fTracker.getService(IRunControl.class);					
					fAcceptingCommands = runControl.isSuspended(executionContext);
				}
				else
				{
					fAcceptingCommands = true;
				}
										
				fSession.addServiceEventListener(SessionOSData.this, null);
			}
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
}
