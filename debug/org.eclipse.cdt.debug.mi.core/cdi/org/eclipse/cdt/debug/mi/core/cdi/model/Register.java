/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarListChildren;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarListChildrenInfo;

/**
 */
public class Register extends Variable implements ICDIRegister {

	public Register(RegisterObject obj, MIVar var) {
		super(obj, var);
	}

	public ICDIVariable[] getChildren() throws CDIException {
			Session session = (Session)(getTarget().getSession());
			MISession mi = session.getMISession();
			RegisterManager mgr = (RegisterManager)session.getRegisterManager();
			CommandFactory factory = mi.getCommandFactory();
			MIVarListChildren var = 
			factory.createMIVarListChildren(getMIVar().getVarName());
			try {
				mi.postCommand(var);
				MIVarListChildrenInfo info = var.getMIVarListChildrenInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				MIVar[] vars = info.getMIVars();
				children = new Register[vars.length];
				for (int i = 0; i < vars.length; i++) {
					RegisterObject regObj = new RegisterObject(getTarget(),
					 vars[i].getExp(), getPosition());
					children[i] = mgr.createRegister(regObj, vars[i]);
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
			return children;
	}

}
