/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation (Bug 365471)
 *     Marc Khouzam (Ericsson) - Support for different charsets (bug 370462)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Subclass for GDB >= 7.0.
 * This class currently sets non-stop mode, or makes
 * sure target-async is off for all-stop.
 * 
 * @since 4.1
 */
public class FinalLaunchSequence_7_0 extends FinalLaunchSequence {

	private IGDBControl fCommandControl;
	private CommandFactory fCommandFactory;
	private DsfServicesTracker fTracker;
	private DsfSession fSession;

	public FinalLaunchSequence_7_0(DsfSession session, Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		super(session, attributes, rm);
		fSession = session;
	}

	@Override
	protected String[] getExecutionOrder(String group) {
		if (GROUP_TOP_LEVEL.equals(group)) {
			// Initialize the list with the base class' steps
			// We need to create a list that we can modify, which is why we create our own ArrayList.
			List<String> orderList = new ArrayList<String>(Arrays.asList(super.getExecutionOrder(GROUP_TOP_LEVEL)));

			// Now insert our steps right after the initialization of the base class.
			orderList.add(orderList.indexOf("stepInitializeFinalLaunchSequence") + 1, "stepInitializeFinalLaunchSequence_7_0"); //$NON-NLS-1$ //$NON-NLS-2$
			
			// Note that stepSetNonStop is defined in the base class for backwards-compatibility
			orderList.add(orderList.indexOf("stepSourceGDBInitFile") + 1, "stepSetNonStop"); //$NON-NLS-1$ //$NON-NLS-2$
			
			return orderList.toArray(new String[orderList.size()]);
		}

		return null;
	}
	
	/** 
	 * Initialize the members of the FinalLaunchSequence_7_0 class.
	 * This step is mandatory for the rest of the sequence to complete.
	 */
	@Execute
	public void stepInitializeFinalLaunchSequence_7_0(RequestMonitor rm) {
		fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSession.getId());

		fCommandControl = fTracker.getService(IGDBControl.class);
		if (fCommandControl == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain control service", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		fCommandFactory = fCommandControl.getCommandFactory();

		rm.done();
	}
	
	/** 
	 * Rollback method for {@link #stepInitializeFinalLaunchSequence_7_0()}
	 * @since 4.0 
	 */
	@RollBack("stepInitializeFinalLaunchSequence_7_0")
	public void rollBackInitializeFinalLaunchSequence_7_0(RequestMonitor rm) {
		if (fTracker != null) fTracker.dispose();
		fTracker = null;
		rm.done();
	}
	
	@Override
	@Execute
	public void stepSetCharset(final RequestMonitor rm) {
		// Enable printing of sevenbit-strings. This is required to avoid charset issues.
		// See bug 307311 for details.
		fCommandControl.queueCommand(
			fCommandFactory.createMIGDBSetPrintSevenbitStrings(fCommandControl.getContext(), true),
			new ImmediateDataRequestMonitor<MIInfo>(rm) {
				@Override
				protected void handleCompleted() {
					// Set the host charset to UTF-8. This ensures that we can correctly handle different
					// charsets used by the inferior program.
					fCommandControl.queueCommand(
						fCommandFactory.createMIGDBSetHostCharset(fCommandControl.getContext(), "UTF-8"), //$NON-NLS-1$
						new ImmediateDataRequestMonitor<MIInfo>(rm) {
							@Override
							protected void handleCompleted() {
								// Set the target charset. The target charset is the charset used by the char type of
								// the inferior program. Note that GDB only accepts upper case charset names.
								String charset = 
										Platform.getPreferencesService().getString(CDebugCorePlugin.PLUGIN_ID, 
												                                   ICDebugConstants.PREF_DEBUG_CHARSET, 
												                                   Charset.defaultCharset().name(),
												                                   null);
								fCommandControl.queueCommand(
									fCommandFactory.createMIGDBSetTargetCharset(fCommandControl.getContext(), charset.toUpperCase()),
									new ImmediateDataRequestMonitor<MIInfo>(rm) {
										@Override
										protected void handleCompleted() {
											// Set the target wide charset. The target wide charset is the charset used by the wchar_t
											// type of the inferior program. Note that GDB only accepts upper case charset names.
											String defaultWideCharset = "UTF-32"; //$NON-NLS-1$
											if (Platform.getOS().equals(Platform.OS_WIN32)) {
												defaultWideCharset = "UTF-16"; //$NON-NLS-1$
											}

											String wideCharset = 
													Platform.getPreferencesService().getString(CDebugCorePlugin.PLUGIN_ID, 
															ICDebugConstants.PREF_DEBUG_WIDE_CHARSET,
															defaultWideCharset,
															null);
											
											fCommandControl.queueCommand(
												fCommandFactory.createMIGDBSetTargetWideCharset(fCommandControl.getContext(), wideCharset.toUpperCase()),
												new ImmediateDataRequestMonitor<MIInfo>(rm) {
													@Override
													protected void handleCompleted() {
														// Not an essential command, so accept errors
														rm.done();
													}
												});							
										}
									});							
							}
						});
				}
			});
	}
}
