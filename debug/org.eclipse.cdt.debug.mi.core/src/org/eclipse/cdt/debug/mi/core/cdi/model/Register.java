/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarAssign;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterChangedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVar;

/**
 */
public class Register extends Variable implements ICDIRegister {

	public Register(RegisterObject obj, MIVar var) {
		super(obj, var);
	}

	public RegisterObject getRegisterObject() {
		return (RegisterObject)super.getVariableObject();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getValue()
	 */
	public ICDIValue getValue() throws CDIException {
		if (value == null) {
			value = new RegisterValue(this);
		}
		return value;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(String)
	 */
	public void setValue(String expression) throws CDIException {
		Session session = (Session)(getTarget().getSession());
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarAssign var = factory.createMIVarAssign(getMIVar().getVarName(), expression);
		try {
			mi.postCommand(var);
			MIInfo info = var.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		// If the assign was succesfull fire a MIVarChangedEvent()
		// Note gdb may not fire an event for the change register, do it manually.
		MIRegisterChangedEvent change =	new MIRegisterChangedEvent(0, getName(), getVariableObject().getPosition());
		mi.fireEvent(change);
		// If register was on autoupdate, update all the other registers
		// assigning may have side effects i.e. affecting other registers.
		ICDIRegisterManager mgr = session.getRegisterManager();
		if (mgr.isAutoUpdate()) {
			mgr.update();
		}
	}
}
