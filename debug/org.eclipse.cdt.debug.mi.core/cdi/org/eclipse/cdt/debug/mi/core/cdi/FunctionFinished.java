/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionFinished;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.cdi.model.LocalVariableDescriptor;
import org.eclipse.cdt.debug.mi.core.cdi.model.StackFrame;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Thread;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
import org.eclipse.cdt.debug.mi.core.event.MIFunctionFinishedEvent;

/*
 * FunctionFinished 
 */
public class FunctionFinished extends EndSteppingRange implements ICDIFunctionFinished {

	MIFunctionFinishedEvent fMIEvent;

	/**
	 * @param session
	 */
	public FunctionFinished(Session session, MIFunctionFinishedEvent event) {
		super(session);
		fMIEvent = event;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIFunctionFinished#getReturnType()
	 */
	public ICDIType getReturnType() throws CDIException {
		Session session = (Session)getSession();
		Target target = session.getTarget(fMIEvent.getMISession());
		Thread thread = (Thread)target.getCurrentThread();
		StackFrame frame = thread.getCurrentStackFrame();
		String rType = fMIEvent.getReturnType();
		if (rType == null || rType.length() == 0) {
			throw new CDIException(CdiResources.getString("cdi.VariableManager.Unknown_type")); //$NON-NLS-1$
		}
		SourceManager srcMgr = session.getSourceManager();
		return srcMgr.getType(frame, rType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIFunctionFinished#getReturnValue()
	 */
	public ICDIValue getReturnValue() throws CDIException {
		Session session = (Session)getSession();
		Target target = session.getTarget(fMIEvent.getMISession());
		Thread thread = (Thread)target.getCurrentThread();
		StackFrame frame = thread.getCurrentStackFrame();
		String gdbVariable = fMIEvent.getGDBResultVar();
		if (gdbVariable == null || gdbVariable.length() == 0) {
			throw new CDIException(CdiResources.getString("cdi.VariableManager.Unknown_type")); //$NON-NLS-1$
		}
		LocalVariableDescriptor varDesc = new LocalVariableDescriptor(target, thread, frame, gdbVariable, null, 0, 0);
		VariableManager varMgr = session.getVariableManager();
		Variable var = varMgr.createVariable(varDesc);
		return var.getValue();
	}

}
