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
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterObject;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.*;
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
public class RegisterManager extends SessionObject implements ICDIRegisterManager {

	private List regList;
	private boolean autoupdate;

	public RegisterManager(Session session) {
		super(session);
		regList = Collections.synchronizedList(new ArrayList());
		autoupdate = true;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#getRegisterObjects()
	 */
	public ICDIRegisterObject[] getRegisterObjects() throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
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
				regs[i] = new RegisterObject(session.getCurrentTarget(), names[i], i);
			}
			return regs;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createRegister()
	 */
	public ICDIRegister createRegister(ICDIRegisterObject regObject) throws CDIException {
		Register reg = getRegister(regObject);
		if (reg == null) {
			Session session = (Session)getSession();
			reg = new Register(session.getCurrentTarget(), regObject);
			regList.add(reg);
			MISession mi = session.getMISession();
			mi.fireEvent(new MIRegisterCreatedEvent(reg.getName(), reg.getID()));
		}
		return reg;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager#destroyRegister(ICDIRegister)
	 */
	public void destroyRegister(ICDIRegister reg) {
		regList.remove(reg);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager#setAutoUpdate(boolean)
	 */
	public void setAutoUpdate(boolean update) {
		autoupdate = update;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager#isAutoUpdate()
	 */
	public boolean isAutoUpdate() {
		return autoupdate;
	}

	/**
	 * Use by the eventManager to find the Register;
	 */
	public Register getRegister(int regno) throws CDIException {
		Register[] regs = getRegisters();
		for (int i = 0; i < regs.length; i++) {
			if (regs[i].getID() == regno) {
				return regs[i];
			}
		}
		return null;
	}

	/**
	 * Call the by the EventManager when the target is suspended.
	 */
	public void update() throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
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

	private Register[] getRegisters() {
		return (Register[])regList.toArray(new Register[0]);
	}


	private Register getRegister(ICDIRegisterObject regObject) throws CDIException {
		Register[] regs = getRegisters();
		for (int i = 0; i < regs.length; i++) {
			if (regObject.getName().equals(regs[i].getName())) {
				return regs[i];
			}
		}
		return null;
	}

}
