/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterObject;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Register;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataListChangedRegisters;
import org.eclipse.cdt.debug.mi.core.command.MIDataListRegisterNames;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterCreatedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIDataListChangedRegistersInfo;
import org.eclipse.cdt.debug.mi.core.output.MIDataListRegisterNamesInfo;

/**
 */
public class RegisterManager extends SessionObject {

	List regList;

	public RegisterManager(CSession session) {
		super(session);
		regList = new ArrayList();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getRegisterObjects()
	 */
	public ICDIRegisterObject[] getRegisterObjects() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataListRegisterNames registers = factory.createMIDataListRegisterNames();
		try {
			mi.postCommand(registers);
			MIDataListRegisterNamesInfo info =
				registers.getMIDataListRegisterNamesInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			String[] names = info.getRegisterNames();
			RegisterObject[] regs = new RegisterObject[names.length];
			for (int i = 0; i < names.length; i++) {
				regs[i] = new RegisterObject(names[i], i);
			}
			return regs;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	Register[] getRegisters() {
		return (Register[])regList.toArray(new Register[0]);
	}

	public Register getRegister(int regno) throws CDIException {
		Register[] regs = getRegisters();
		for (int i = 0; i < regs.length; i++) {
			if (regs[i].getID() == regno) {
				return regs[i];
			}
		}
		return null;
	}

	Register getRegister(ICDIRegisterObject regObject) throws CDIException {
		Register[] regs = getRegisters();
		for (int i = 0; i < regs.length; i++) {
			if (regObject.getName().equals(regs[i].getName())) {
				return regs[i];
			}
		}
		return null;
	}
	
	public Register createRegister(ICDIRegisterObject regObject) throws CDIException {
		Register reg = getRegister(regObject);
		if (reg == null) {
			reg = new Register(getCSession().getCTarget(), regObject);
			regList.add(reg);
			MISession mi = getCSession().getMISession();
			mi.fireEvent(new MIRegisterCreatedEvent(reg.getName(), reg.getID()));
		}
		return reg;
	}

	Register[] createRegisters(ICDIRegisterObject[] regObjects) throws CDIException {
		Register[] regs = new Register[regObjects.length];
		for (int i = 0; i < regs.length; i++) {
			regs[i] = createRegister(regObjects[i]);
		} 
		return regs;
	}

	public void update() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataListChangedRegisters changed = factory.createMIDataListChangedRegisters();
		try {
			mi.postCommand(changed);
			MIDataListChangedRegistersInfo info =
				changed.getMIDataListChangedRegistersInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			int[] regnos = info.getRegisterNumbers();
			List eventList = new ArrayList(regnos.length);
			for (int i = 0 ; i < regnos.length; i++) {
				Register reg = getRegister(regnos[i]);
				if (reg != null) {
					eventList.add(new MIRegisterChangedEvent(changed.getToken(), reg.getName(), regnos[i]));
				}
			}
			MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
			mi.fireEvents(events);
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

}
