/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarUpdate;
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarUpdateInfo;

/**
 */
public class UpdateManager {

	Session session;
	List updateList = Collections.synchronizedList(new ArrayList(5));
	MIVarChange[] noChanges = new MIVarChange[0];

	public UpdateManager(Session s) {
		session = s;
	}

	public void addUpdateListener(IUpdateListener listener) {
		updateList.add(listener);
	}

	public void removeUpdateListener(IUpdateListener listener) {
		updateList.remove(listener);
	}

	/**
	 * Update the variables, from the response of the "-var-update *"
	 * mi/command.
	 */
	public void update() throws CDIException {
		MIVarChange[] changes = noChanges;
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarUpdate update = factory.createMIVarUpdate();
		try {
			mi.postCommand(update);
			MIVarUpdateInfo info = update.getMIVarUpdateInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			changes = info.getMIVarChanges();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		IUpdateListener[] listeners = (IUpdateListener[])updateList.toArray(new IUpdateListener[0]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].changeList(changes);
		}
	}
}
