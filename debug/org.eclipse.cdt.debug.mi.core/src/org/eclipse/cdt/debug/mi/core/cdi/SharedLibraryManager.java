/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.SharedLibrary;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIInfoSharedLibrary;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibUnloadedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIInfoSharedLibraryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIShared;

/**
 * Manager of the CDI shared libraries.
 */
public class SharedLibraryManager extends SessionObject implements ICDISharedLibraryManager {

	List sharedList;
	List delList;

	public SharedLibraryManager (CSession session) {
		super(session);
		sharedList = new ArrayList(1);
		delList = new ArrayList(1);
	}

	public void loadSymbols(ICDISharedLibrary slib) throws CDIException {
		throw new CDIException("not implemented");
	}

	public void update() throws CDIException {
		MIShared[] miLibs = new MIShared[0];
		CSession s = getCSession();
		CommandFactory factory = s.getMISession().getCommandFactory();
		MIInfoSharedLibrary infoShared = factory.createMIInfoSharedLibrary();
		try {
			s.getMISession().postCommand(infoShared);
			MIInfoSharedLibraryInfo info = infoShared.getMIInfoSharedLibraryInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			miLibs = info.getMIShared();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		List eventList = new ArrayList(miLibs.length);
		for (int i = 0; i < miLibs.length; i++) {
			if (containsSharedLib(miLibs[i])) {
				if (hasSharedLibChanged(miLibs[i])) {
					// Fire ChangedEvent
					SharedLibrary sharedlib = (SharedLibrary)getSharedLibrary(miLibs[i].getName());
					sharedlib.setMIShared(miLibs[i]);
					eventList.add(new MISharedLibChangedEvent(miLibs[i].getName())); 
				}
			} else {
				// add the new breakpoint and fire CreatedEvent
				sharedList.add(new SharedLibrary(this, miLibs[i]));
				eventList.add(new MISharedLibCreatedEvent(miLibs[i].getName())); 
			}
		}
		// Check if any libraries was unloaded.
		ICDISharedLibrary[] oldlibs = (ICDISharedLibrary[])sharedList.toArray(new ICDISharedLibrary[0]);
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
				sharedList.remove(oldlibs[i]);
				delList.add(oldlibs[i]);
				eventList.add(new MISharedLibUnloadedEvent(oldlibs[i].getFileName())); 
			}
		}
		MISession mi = getCSession().getMISession();
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
	}

	public boolean containsSharedLib(MIShared miLib) {
		ICDISharedLibrary[] libs = (ICDISharedLibrary[])sharedList.toArray(new ICDISharedLibrary[0]);
		for (int i = 0; i < libs.length; i++) {
			if (miLib.getName().equals(libs[i].getFileName())) {
					return true;
			}
		}
		return false;
	}

	public boolean hasSharedLibChanged(MIShared miLib) {
		ICDISharedLibrary[] libs = (ICDISharedLibrary[])sharedList.toArray(new ICDISharedLibrary[0]);
		for (int i = 0; i < libs.length; i++) {
			if (miLib.getName().equals(libs[i].getFileName())) {
				return miLib.getFrom() != libs[i].getStartAddress() ||
					miLib.getTo() != libs[i].getEndAddress() ||
					miLib.isRead() != libs[i].areSymbolsLoaded();
			}
		}
		return false;
	}

	public ICDISharedLibrary getSharedLibrary(String name) {
		ICDISharedLibrary[] libs = (ICDISharedLibrary[])sharedList.toArray(new ICDISharedLibrary[0]);
		for (int i = 0; i < libs.length; i++) {
			if (name.equals(libs[i].getFileName())) {
					return libs[i];
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#getSharedLibraries()
	 */
	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		update();
		return (ICDISharedLibrary[])sharedList.toArray(new ICDISharedLibrary[0]);
	}

}
