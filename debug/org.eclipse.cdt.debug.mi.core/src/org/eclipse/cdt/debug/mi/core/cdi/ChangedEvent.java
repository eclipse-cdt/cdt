/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;

/**
 */
public class ChangedEvent implements ICDIChangedEvent {

	CSession session;
	ICDIObject source;

	public ChangedEvent(CSession s, MIVarChangedEvent var) {
		session = s;
		VariableManager mgr = session.getVariableManager();
		String varName = var.getVarName();
		VariableManager.Element element = mgr.getElement(varName);
		if (element != null && element.variable != null) {
			source = element.variable;
		} else {
			source = new CObject(session.getCTarget());
		}
	}

	public ChangedEvent(CSession s, MIRegisterChangedEvent var) {
		session = s;
		RegisterManager mgr = session.getRegisterManager();
		int regno = var.getNumber();
		Register reg = null;
		try {
			reg = mgr.getRegister(regno);
		} catch (CDIException e) {
		}
		if (reg != null) {
			source = reg;
		} else {
			source = new CObject(session.getCTarget());
		}
	}

	public ChangedEvent(CSession s, ICDIObject src) {
		session = s;
		source = src;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return source;
	}

}
