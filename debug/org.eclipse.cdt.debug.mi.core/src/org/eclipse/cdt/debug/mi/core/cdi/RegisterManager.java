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
import org.eclipse.cdt.debug.mi.core.cdi.model.Register;
import org.eclipse.cdt.debug.mi.core.cdi.model.RegisterObject;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataListChangedRegisters;
import org.eclipse.cdt.debug.mi.core.command.MIDataListRegisterNames;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.command.MIVarUpdate;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIDataListChangedRegistersInfo;
import org.eclipse.cdt.debug.mi.core.output.MIDataListRegisterNamesInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarUpdateInfo;

/**
 */
public class RegisterManager extends SessionObject implements ICDIRegisterManager {

	private List regList;
	private boolean autoupdate;
	MIVarChange[] noChanges = new MIVarChange[0];

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
		RegisterObject regObj = null;
		if (regObject instanceof RegisterObject) {
			regObj = (RegisterObject)regObject;
		}
		if (regObj != null) {
			Register reg = getRegister(regObject);
			if (reg == null) {
				try {
					String name = "$" + regObj.getName();
					Session session = (Session)getSession();
					MISession mi = session.getMISession();
					CommandFactory factory = mi.getCommandFactory();
					MIVarCreate var = factory.createMIVarCreate(name);
					mi.postCommand(var);
					MIVarCreateInfo info = var.getMIVarCreateInfo();
					if (info == null) {
						throw new CDIException("No answer");
					}
					reg = new Register(regObj, info.getMIVar());
					regList.add(reg);
				} catch (MIException e) {
					throw new MI2CDIException(e);
				}
			}
			return reg;
		}
		throw new CDIException("Wrong register type");
	}

	public Register createRegister(RegisterObject v, MIVar mivar) throws CDIException {
		Register reg = new Register(v, mivar);
		regList.add(reg);
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
	public Register getRegister(String varName) {
		Register[] regs = getRegisters();
		for (int i = 0; i < regs.length; i++) {
			if (regs[i].getMIVar().getVarName().equals(varName)) {
				return regs[i];
			}
			try {
				Register r = (Register)regs[i].getChild(varName);
				if (r != null) {
					return r;
				}
			} catch (ClassCastException e) {
			}
		}
		return null;
	}

	/**
	 * Use by the eventManager to find the Register;
	 */
	public Register getRegister(int regno) {
		Register[] regs = getRegisters();
		for (int i = 0; i < regs.length; i++) {
			if (regs[i].getPosition() == regno) {
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
			// Now that we know the registers changed
			// call -var-update to update the value in gdb.
			// And send the notification.
			for (int i = 0 ; i < regnos.length; i++) {
				Register reg = getRegister(regnos[i]);
				if (reg != null) {
					String varName = reg.getMIVar().getVarName();
					MIVarChange[] changes = noChanges;
					MIVarUpdate update = factory.createMIVarUpdate(varName);
					try {
						mi.postCommand(update);
						MIVarUpdateInfo updateInfo = update.getMIVarUpdateInfo();
						if (updateInfo == null) {
							throw new CDIException("No answer");
						}
						changes = updateInfo.getMIVarChanges();
					} catch (MIException e) {
						//throw new MI2CDIException(e);
						//eventList.add(new MIVarDeletedEvent(varName));
					}
					if (changes.length != 0) {
						for (int j = 0 ; j < changes.length; j++) {
							String n = changes[j].getVarName();
							if (changes[j].isInScope()) {
								eventList.add(new MIVarChangedEvent(n));
							}
						}
					} else {
						// Fall back to the register number.
						eventList.add(new MIRegisterChangedEvent(update.getToken(), reg.getName(), regnos[i]));
					}
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
