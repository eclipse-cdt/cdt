/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.CSession;
import org.eclipse.cdt.debug.mi.core.cdi.MemoryManager;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.CObject;
import org.eclipse.cdt.debug.mi.core.cdi.model.CTarget;
import org.eclipse.cdt.debug.mi.core.cdi.model.Register;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarCreatedEvent;

/**
 */
public class CreatedEvent implements ICDICreatedEvent {

	CSession session;
	ICDIObject source;

	public CreatedEvent(CSession s, MIBreakpointCreatedEvent bpoint) {
		session = s;
		BreakpointManager mgr = (BreakpointManager)session.getBreakpointManager();
		int number = bpoint.getNumber();
		ICDIBreakpoint breakpoint = mgr.getBreakpoint(number);
		if (breakpoint != null) {
			source = breakpoint;
		} else {
			source = new CObject(session.getCTarget());
		}
	}

	public CreatedEvent(CSession s, MIVarCreatedEvent var) {
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

	public CreatedEvent(CSession s, MIRegisterCreatedEvent var) {
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

	public CreatedEvent(CSession s, MIThreadCreatedEvent ethread) {
		session = s;
		CTarget target = (CTarget)session.getCurrentTarget();
		ICDIThread thread = target.getThread(ethread.getId());
		if (thread != null) {
			source = thread;
		} else {
			source = new CObject(session.getCTarget());
		}
	}

	public CreatedEvent(CSession s, MIMemoryCreatedEvent mblock) {
		session = s;
		MemoryManager mgr = (MemoryManager)session.getMemoryManager();
		ICDIMemoryBlock[] blocks = mgr.listMemoryBlocks();
		for (int i = 0; i < blocks.length; i++) {
			if (blocks[i].getStartAddress() == mblock.getAddress() &&
			    blocks[i].getLength() == mblock.getLength()) {
				source = blocks[i];
				break;
			}
		}
		if (source == null) {
			source = new CObject(session.getCTarget());
		}
	}

	public CreatedEvent(CSession s, ICDIObject src) {
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
