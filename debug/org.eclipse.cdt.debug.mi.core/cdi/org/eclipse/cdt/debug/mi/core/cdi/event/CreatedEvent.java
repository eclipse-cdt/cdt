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
package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.MemoryManager;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.CObject;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarCreatedEvent;

/**
 */
public class CreatedEvent implements ICDICreatedEvent {

	Session session;
	ICDIObject source;

	public CreatedEvent(Session s, MIBreakpointCreatedEvent bpoint) {
		session = s;
		BreakpointManager mgr = session.getBreakpointManager();
		MISession miSession = bpoint.getMISession();
		int number = bpoint.getNumber();
		source = mgr.getBreakpoint(miSession, number);
		if (source == null) {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
		}
	}

	public CreatedEvent(Session s, MIVarCreatedEvent var) {
		session = s;
		VariableManager mgr = (VariableManager)session.getVariableManager();
		MISession miSession = var.getMISession();
		String varName = var.getVarName();
		source = mgr.getVariable(miSession, varName);
		if (source == null) {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
		}
	}

	public CreatedEvent(Session s, MIRegisterCreatedEvent reg) {
		session = s;
		RegisterManager mgr = (RegisterManager)session.getRegisterManager();
		MISession miSession = reg.getMISession();
		int regno = reg.getNumber();
		source = mgr.getRegister(miSession, regno);
		if (source == null) {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
		}
	}

	public CreatedEvent(Session s, MIThreadCreatedEvent cthread) {
		session = s;
		MISession miSession = cthread.getMISession();
		Target target = session.getTarget(miSession);
		source = target.getThread(cthread.getId());
		if (source == null) {
			source = new CObject(target);
		}
	}

	public CreatedEvent(Session s, MIInferiorCreatedEvent inferior) {
		session  = s;
		MISession miSession = inferior.getMISession();
		source = session.getTarget(miSession);
	}

	public CreatedEvent(Session s, MIMemoryCreatedEvent mblock) {
		session = s;
		MemoryManager mgr = (MemoryManager)session.getMemoryManager();
		MISession miSession = mblock.getMISession();
		ICDIMemoryBlock[] blocks = mgr.getMemoryBlocks(miSession);
		for (int i = 0; i < blocks.length; i++) {
			if (blocks[i].getStartAddress() == mblock.getAddress() &&
			    blocks[i].getLength() == mblock.getLength()) {
				source = blocks[i];
				break;
			}
		}
		if (source == null) {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
		}
	}

	public CreatedEvent(Session s, MISharedLibCreatedEvent slib) {
		session = s;
		SharedLibraryManager mgr = (SharedLibraryManager)session.getSharedLibraryManager();
		MISession miSession = slib.getMISession();
		String name = slib.getName();
		source = mgr.getSharedLibrary(miSession, name);
		if (source == null) {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
		}
	}

	public CreatedEvent(Session s, ICDIObject src) {
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
