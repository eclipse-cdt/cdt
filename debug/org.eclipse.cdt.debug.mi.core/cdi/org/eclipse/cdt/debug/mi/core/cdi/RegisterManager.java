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
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Register;
import org.eclipse.cdt.debug.mi.core.cdi.model.RegisterDescriptor;
import org.eclipse.cdt.debug.mi.core.cdi.model.RegisterGroup;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
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
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarUpdateInfo;

/**
 */
public class RegisterManager extends Manager {

	Map regsMap;
	MIVarChange[] noChanges = new MIVarChange[0];

	public RegisterManager(Session session) {
		super(session, true);
		regsMap = new Hashtable();
	}

	synchronized List getRegistersList(Target target) {
		List regsList = (List)regsMap.get(target);
		if (regsList == null) {
			regsList = Collections.synchronizedList(new ArrayList());
			regsMap.put(target, regsList);
		}
		return regsList;
	}

	public ICDIRegisterGroup[] getRegisterGroups(Target target) throws CDIException {
		RegisterGroup group = new RegisterGroup(target, "Main"); //$NON-NLS-1$
		return new ICDIRegisterGroup[] { group };
	}

	public ICDIRegisterDescriptor[] getRegisterDescriptors(RegisterGroup group) throws CDIException {
		Target target = (Target)group.getTarget();
		return getRegisterDescriptors(target);
	}
	public ICDIRegisterDescriptor[] getRegisterDescriptors(Target target) throws CDIException {
		Session session = (Session)getSession();
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataListRegisterNames registers = factory.createMIDataListRegisterNames();
		try {
			mi.postCommand(registers);
			MIDataListRegisterNamesInfo info =
				registers.getMIDataListRegisterNamesInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			String[] names = info.getRegisterNames();
			List regsList = new ArrayList(names.length);
			for (int i = 0; i < names.length; i++) {
				if (names[i].length() > 0) {
					regsList.add(new RegisterDescriptor(target, null, null, names[i], null, i, 0));
				}
			}
			return (ICDIRegisterDescriptor[])regsList.toArray(new ICDIRegisterDescriptor[0]);
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
		}
	}

	public Register createRegister(RegisterDescriptor regDesc) throws CDIException {
		Register reg = getRegister(regDesc);
		if (reg == null) {
			try {
				String name = "$" + regDesc.getName(); //$NON-NLS-1$
				Target target = (Target)regDesc.getTarget();
				MISession mi = target.getMISession();
				CommandFactory factory = mi.getCommandFactory();
				MIVarCreate var = factory.createMIVarCreate(name);
				mi.postCommand(var);
				MIVarCreateInfo info = var.getMIVarCreateInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				reg = new Register(regDesc, info.getMIVar());
				List regList = getRegistersList(target);
				regList.add(reg);
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		}
		return reg;
		//throw new CDIException(CdiResources.getString("cdi.RegisterManager.Wrong_register_type")); //$NON-NLS-1$
	}

	public void destroyRegister(Register reg) {
		Target target = (Target)reg.getTarget();
		List regList = (List)regsMap.get(target);
		if (regList != null) {
			regList.remove(reg);
		}
	}

	/**
	 * Use by the eventManager to find the Register;
	 */
	public Register getRegister(MISession miSession, String varName) {
		Target target = ((Session)getSession()).getTarget(miSession);
		Register[] regs = getRegisters(target);
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
	public Register getRegister(MISession miSession, int regno) {
		Target target = ((Session)getSession()).getTarget(miSession);
		return getRegister(target, regno);
	}
	public Register getRegister(Target target, int regno) {
		Register[] regs = getRegisters(target);
		for (int i = 0; i < regs.length; i++) {
			if (regs[i].getPosition() == regno) {
				return regs[i];
			}
		}
		return null;
	}

	public void update(Target target) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataListChangedRegisters changed = factory.createMIDataListChangedRegisters();
		try {
			mi.postCommand(changed);
			MIDataListChangedRegistersInfo info =
				changed.getMIDataListChangedRegistersInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			int[] regnos = info.getRegisterNumbers();
			List eventList = new ArrayList(regnos.length);
			// Now that we know the registers changed
			// call -var-update to update the value in gdb.
			// And send the notification.
			for (int i = 0 ; i < regnos.length; i++) {
				Register reg = getRegister(target, regnos[i]);
				if (reg != null) {
					String varName = reg.getMIVar().getVarName();
					MIVarChange[] changes = noChanges;
					MIVarUpdate update = factory.createMIVarUpdate(varName);
					try {
						mi.postCommand(update);
						MIVarUpdateInfo updateInfo = update.getMIVarUpdateInfo();
						if (updateInfo == null) {
							throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
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
								eventList.add(new MIVarChangedEvent(mi, n));
							}
						}
					} else {
						// Fall back to the register number.
						eventList.add(new MIRegisterChangedEvent(mi, update.getToken(), reg.getName(), regnos[i]));
					}
				}
			}
			MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
			mi.fireEvents(events);
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	private Register[] getRegisters(Target target) {
		List regsList = (List)regsMap.get(target);
		if (regsList != null) {
			return (Register[]) regsList.toArray(new Register[regsList.size()]);
		}
		return new Register[0];
	}

	private Register getRegister(RegisterDescriptor regDesc) throws CDIException {
		Register[] regs = getRegisters((Target)regDesc.getTarget());
		for (int i = 0; i < regs.length; i++) {
			if (regDesc.getName().equals(regs[i].getName())) {
				return regs[i];
			}
		}
		return null;
	}

}
