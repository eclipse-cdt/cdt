/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarListChildren;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarListChildrenInfo;

public class  RegisterValue extends Value {

	Register reg;

	public RegisterValue(Register r) {
		super(r);
		reg = r;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getVariables()
	 */
	public ICDIVariable[] getVariables() throws CDIException {
		Register[] registers = null;
		Session session = (Session)(getTarget().getSession());
		MISession mi = session.getMISession();
		RegisterManager mgr = (RegisterManager)session.getRegisterManager();
		CommandFactory factory = mi.getCommandFactory();
		MIVarListChildren var = 
		factory.createMIVarListChildren(reg.getMIVar().getVarName());
		try {
			mi.postCommand(var);
			MIVarListChildrenInfo info = var.getMIVarListChildrenInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MIVar[] vars = info.getMIVars();
			registers = new Register[vars.length];
			for (int i = 0; i < vars.length; i++) {
				RegisterObject regObj = new RegisterObject(getTarget(),
				 vars[i].getExp(), reg.getVariableObject().getPosition());
				registers[i] = mgr.createRegister(regObj, vars[i]);
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return registers;
	}

}
