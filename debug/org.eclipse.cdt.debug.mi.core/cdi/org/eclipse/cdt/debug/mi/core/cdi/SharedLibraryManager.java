/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.mi.core.IMIConstants;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Breakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.SharedLibrary;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetAutoSolib;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetSolibSearchPath;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetStopOnSolibEvents;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShow;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowSolibSearchPath;
import org.eclipse.cdt.debug.mi.core.command.MIInfoSharedLibrary;
import org.eclipse.cdt.debug.mi.core.command.MISharedLibrary;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibUnloadedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowInfo;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowSolibSearchPathInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoSharedLibraryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIShared;

/**
 * Manager of the CDI shared libraries.
 */
public class SharedLibraryManager extends Manager {

	ICDISharedLibrary[] EMPTY_SHAREDLIB = {};
	Map sharedMap;
	boolean isDeferred;

	public SharedLibraryManager (Session session) {
		super(session, true);
		sharedMap = new Hashtable();
		setAutoUpdate( MIPlugin.getDefault().getPluginPreferences().getBoolean( IMIConstants.PREF_SHARED_LIBRARIES_AUTO_REFRESH ) );
	}

	synchronized List getSharedList(Target target) {
		List sharedList = (List)sharedMap.get(target);
		if (sharedList == null) {
			sharedList = Collections.synchronizedList(new ArrayList());
			sharedMap.put(target, sharedList);
		}
		return sharedList;
	}

	MIShared[] getMIShareds(MISession miSession) throws CDIException {
		MIShared[] miLibs = new MIShared[0];
		CommandFactory factory = miSession.getCommandFactory();
		MIInfoSharedLibrary infoShared = factory.createMIInfoSharedLibrary();
		try {
			miSession.postCommand(infoShared);
			MIInfoSharedLibraryInfo info = infoShared.getMIInfoSharedLibraryInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			miLibs = info.getMIShared();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return miLibs;
	}

	public void update(Target target) throws CDIException {
		MISession miSession = target.getMISession();
		Session session = (Session)target.getSession();
		List eventList = updateState(target);
		// A new Libraries loaded or something change, try to set the breakpoints.
		if (eventList.size() > 0) {
			BreakpointManager bpMgr = session.getBreakpointManager();
			ICDIBreakpoint bpoints[] = null;
			try {
				bpoints = bpMgr.getDeferredBreakpoints(target);
			} catch (CDIException e) {
				bpoints = new ICDIBreakpoint[0];
			}
			for (int i = 0; i < bpoints.length; i++) {
				if (bpoints[i] instanceof Breakpoint) {
					Breakpoint bkpt = (Breakpoint)bpoints[i];
					try {
						boolean enable = bkpt.isEnabled();
						bpMgr.setLocationBreakpoint(bkpt);
						bpMgr.deleteFromDeferredList(bkpt);
						bpMgr.addToBreakpointList(bkpt);
						// If the breakpoint was disable in the IDE
						// install it but keep it disable
						if (!enable) {
							bpMgr.disableBreakpoint(bkpt);
						}
						MIBreakpoint[] miBreakpoints = bkpt.getMIBreakpoints();
						if (miBreakpoints != null && miBreakpoints.length > 0) {
							eventList.add(new MIBreakpointCreatedEvent(miSession, miBreakpoints[0].getNumber()));
						}
					} catch (CDIException e) {
						// ignore
					}
				}
			}
		}
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		miSession.fireEvents(events);
	}

	private List updateState(Target target) throws CDIException {
		MISession miSession = target.getMISession();
		ICDITargetConfiguration conf = target.getConfiguration();
		if (!conf.supportsSharedLibrary()) {
			return Collections.EMPTY_LIST; // Bail out early;
		}

		MIShared[] miLibs = getMIShareds(miSession);
		ArrayList eventList = new ArrayList(miLibs.length);
		for (int i = 0; i < miLibs.length; i++) {
			SharedLibrary sharedlib = getSharedLibrary(target, miLibs[i].getName());
			if (sharedlib != null) {
				if (hasSharedLibChanged(sharedlib, miLibs[i])) {
					// Fire ChangedEvent
					sharedlib.setMIShared(miLibs[i]);
					eventList.add(new MISharedLibChangedEvent(miSession, miLibs[i].getName())); 
				}
			} else {
				// add the new breakpoint and fire CreatedEvent
				List sharedList = getSharedList(target);
				sharedList.add(new SharedLibrary(target, miLibs[i]));
				eventList.add(new MISharedLibCreatedEvent(miSession, miLibs[i].getName())); 
			}
		}
		// Check if any libraries was unloaded.
		List sharedList = (List)sharedMap.get(target);
		if (sharedList != null) {
			SharedLibrary[] oldlibs = (SharedLibrary[]) sharedList.toArray(new SharedLibrary[sharedList.size()]);
			for (int i = 0; i < oldlibs.length; i++) {
				boolean found = false;
				for (int j = 0; j < miLibs.length; j++) {
					if (miLibs[j].getName().equals(oldlibs[i].getFileName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					// Fire destroyed Events.
					eventList.add(new MISharedLibUnloadedEvent(miSession, oldlibs[i].getFileName())); 
				}
			}
		}
		return eventList;
	}

	public boolean hasSharedLibChanged(SharedLibrary lib, MIShared miLib) {
		return !miLib.getName().equals(lib.getFileName()) ||
			!MIFormat.getBigInteger(miLib.getFrom()).equals(lib.getStartAddress())   ||
		    !MIFormat.getBigInteger(miLib.getTo()).equals(lib.getEndAddress()) ||
			miLib.isRead() != lib.areSymbolsLoaded();
	}

	/*
	 * this for the events
	 */
	public void deleteSharedLibrary(MISession miSession, SharedLibrary lib) {
		Target target = ((Session)getSession()).getTarget(miSession);
		List sharedList = (List)sharedMap.get(target);
		if (sharedList != null) {
			sharedList.remove(lib);
		}
	}

	public SharedLibrary getSharedLibrary(MISession miSession, String name) {
		Target target = ((Session)getSession()).getTarget(miSession);
		return getSharedLibrary(target, name);
	}
	public SharedLibrary getSharedLibrary(Target target, String name) {
		List sharedList = (List)sharedMap.get(target);
		if (sharedList != null) {
			SharedLibrary[] libs = (SharedLibrary[]) sharedList.toArray(new SharedLibrary[sharedList.size()]);
			for (int i = 0; i < libs.length; i++) {
				if (name.equals(libs[i].getFileName())) {
					return libs[i];
				}
			}
		}
		return null;
	}

	public void setDeferredBreakpoint (boolean set) {
		isDeferred = set;
	}

	public boolean isDeferredBreakpoint() {
		return isDeferred;
	}

	public void setAutoLoadSymbols(Target target, boolean set) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBSetAutoSolib solib = factory.createMIGDBSetAutoSolib(set);
		try {
			mi.postCommand(solib);
			solib.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public boolean isAutoLoadSymbols(Target target) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBShow show = factory.createMIGDBShow(new String[]{"auto-solib-add"}); //$NON-NLS-1$
		try {
			mi.postCommand(show);
			MIGDBShowInfo info = show.getMIGDBShowInfo();
			String value = info.getValue();
			if (value != null) {
				return value.equalsIgnoreCase("on"); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return false;
	}

	public void setStopOnSolibEvents(Target target, boolean set) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBSetStopOnSolibEvents stop = factory.createMIGDBSetStopOnSolibEvents(set);
		try {
			mi.postCommand(stop);
			stop.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public boolean isStopOnSolibEvents(Target target) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBShow show = factory.createMIGDBShow(new String[]{"stop-on-solib-events"}); //$NON-NLS-1$
		try {
			mi.postCommand(show);
			MIGDBShowInfo info = show.getMIGDBShowInfo();
			String value = info.getValue();
			if (value != null) {
				return value.equalsIgnoreCase("1"); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return false;
	}

	public void setSharedLibraryPaths(Target target, String[] libPaths) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBSetSolibSearchPath solib = factory.createMIGDBSetSolibSearchPath(libPaths);
		try {
			mi.postCommand(solib);
			solib.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public String[] getSharedLibraryPaths(Target target) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBShowSolibSearchPath dir = factory.createMIGDBShowSolibSearchPath();
		try {
			mi.postCommand(dir);
			MIGDBShowSolibSearchPathInfo info = dir.getMIGDBShowSolibSearchPathInfo();
			return info.getDirectories();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public ICDISharedLibrary[] getSharedLibraries(Target target) throws CDIException {
		List sharedList = (List)sharedMap.get(target);
		if (sharedList != null) {
			return (ICDISharedLibrary[]) sharedList.toArray(new ICDISharedLibrary[sharedList.size()]);
		}
		return EMPTY_SHAREDLIB;
	}

	public void loadSymbols(Target target) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MISharedLibrary sharedlibrary = factory.createMISharedLibrary();
		try {
			mi.postCommand(sharedlibrary);
			MIInfo info = sharedlibrary.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		update(target);
	}

	public void loadSymbols(Target target, ICDISharedLibrary[] libs) throws CDIException {
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		for (int i = 0; i < libs.length; i++) {
			if (libs[i].areSymbolsLoaded()) {
				continue;
			}
			MISharedLibrary sharedlibrary = factory.createMISharedLibrary(libs[i].getFileName());
			try {
				miSession.postCommand(sharedlibrary);
				MIInfo info = sharedlibrary.getMIInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
			// Do not do this, error are not propagate by the CLI "shared command
			// So we have to manually recheck all the shared with "info shared"
			//((SharedLibrary)libs[i]).getMIShared().setSymbolsRead(true);
			//mi.fireEvent(new MISharedLibChangedEvent(libs[i].getFileName()));
			update(target);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#supportsAutoLoadSymbols()
	 */
	public boolean supportsAutoLoadSymbols() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#supportsStopOnSolibEvents()
	 */
	public boolean supportsStopOnSolibEvents() {
		return true;
	}
}
