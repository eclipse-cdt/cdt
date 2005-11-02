/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.RxThread;
import org.eclipse.cdt.debug.mi.core.cdi.model.Argument;
import org.eclipse.cdt.debug.mi.core.cdi.model.ArgumentDescriptor;
import org.eclipse.cdt.debug.mi.core.cdi.model.GlobalVariable;
import org.eclipse.cdt.debug.mi.core.cdi.model.GlobalVariableDescriptor;
import org.eclipse.cdt.debug.mi.core.cdi.model.LocalVariable;
import org.eclipse.cdt.debug.mi.core.cdi.model.LocalVariableDescriptor;
import org.eclipse.cdt.debug.mi.core.cdi.model.Register;
import org.eclipse.cdt.debug.mi.core.cdi.model.RegisterDescriptor;
import org.eclipse.cdt.debug.mi.core.cdi.model.StackFrame;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Thread;
import org.eclipse.cdt.debug.mi.core.cdi.model.ThreadStorage;
import org.eclipse.cdt.debug.mi.core.cdi.model.ThreadStorageDescriptor;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
import org.eclipse.cdt.debug.mi.core.cdi.model.VariableDescriptor;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.CLIPType;
import org.eclipse.cdt.debug.mi.core.command.MIStackListArguments;
import org.eclipse.cdt.debug.mi.core.command.MIStackListLocals;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.command.MIVarDelete;
import org.eclipse.cdt.debug.mi.core.command.MIVarUpdate;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarDeletedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIArg;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.CLIPTypeInfo;
import org.eclipse.cdt.debug.mi.core.output.MIStackListArgumentsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIStackListLocalsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarUpdateInfo;

/**
 */
public class VariableManager extends Manager {

	static final ICDIVariable[] EMPTY_VARIABLES = {};
	// We put a restriction on how deep we want to
	// go when doing update of the variables.
	// If the number is to high, gdb will just hang.
	int MAX_STACK_DEPTH = Thread.STACKFRAME_DEFAULT_DEPTH;
	Map variablesMap;
	MIVarChange[] noChanges = new MIVarChange[0];

	public VariableManager(Session session) {
		super(session, true);
		variablesMap = new Hashtable();
	}

	synchronized List getVariablesList(Target target) {
		List variablesList = (List) variablesMap.get(target);
		if (variablesList == null) {
			variablesList = Collections.synchronizedList(new ArrayList());
			variablesMap.put(target, variablesList);
		}
		return variablesList;
	}

	/**
	 * Return the element that have the uniq varName.
	 * null is return if the element is not in the cache.
	 */
	public Variable getVariable(MISession miSession, String varName) {
		Target target = ((Session)getSession()).getTarget(miSession);
		return getVariable(target, varName);
	}
	/**
	 * Return the element that have the uniq varName.
	 * null is return if the element is not in the cache.
	 */
	public Variable getVariable(Target target, String varName) {
		Variable[] vars = getVariables(target);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getMIVar().getVarName().equals(varName)) {
				return vars[i];
			}
			Variable v = vars[i].getChild(varName);
			if (v != null) {
				return v;
			}
		}
		return null;
	}

	/**
	 * Return the Element with this thread/stackframe, and with this name.
	 * null is return if the element is not in the cache.
	 */
	Variable findVariable(VariableDescriptor v) throws CDIException {
		Target target = (Target)v.getTarget();
		ICDIStackFrame vstack = v.getStackFrame();
		ICDIThread vthread = v.getThread();
		int position = v.getPosition();
		int depth = v.getStackDepth();
		Variable[] vars = getVariables(target);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getFullName().equals(v.getFullName())
				&& vars[i].getName().equals(v.getName()) // see bug #113364
				&& vars[i].getCastingArrayStart() == v.getCastingArrayStart()
				&& vars[i].getCastingArrayEnd() == v.getCastingArrayEnd()
				&& VariableDescriptor.equalsCasting(vars[i], v)) {
				// check threads
				ICDIThread thread = vars[i].getThread();
				if ((vthread == null && thread == null) ||
						(vthread != null && thread != null && thread.equals(vthread))) {
					// check stackframes
					ICDIStackFrame frame = vars[i].getStackFrame();
					if (vstack == null && frame == null) {
						return vars[i];
					} else if (frame != null && vstack != null && frame.equals(vstack)) {
						if (vars[i].getPosition() == position) {
							if (vars[i].getStackDepth() == depth) {
								return vars[i];
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns all the elements that are in the cache.
	 */
	Variable[] getVariables(Target target) {
		List variableList = (List)variablesMap.get(target);
		if (variableList != null) {
			return (Variable[]) variableList.toArray(new Variable[variableList.size()]);
		}
		return new Variable[0];
	}

	/**
	 * Check the type
	 */
	public void checkType(StackFrame frame, String type) throws CDIException {
		if (type != null && type.length() > 0) {
			Target target = (Target)frame.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.setCurrentThread(frame.getThread(), false);
			((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
			try {
				MISession miSession = target.getMISession();
				RxThread rxThread = miSession.getRxThread();
				rxThread.setEnableConsole(false);
				CommandFactory factory = miSession.getCommandFactory();
				CLIPType ptype = factory.createCLIPType(type);
				miSession.postCommand(ptype);
				CLIPTypeInfo info = ptype.getMIPtypeInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			} finally {
				MISession miSession = target.getMISession();
				RxThread rxThread = miSession.getRxThread();
				rxThread.setEnableConsole(true);
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
			}
		} else {
			throw new CDIException(CdiResources.getString("cdi.VariableManager.Unknown_type")); //$NON-NLS-1$
		}
	}

	/**
	 * Tell gdb to remove the underlying var-object also.
	 */
	void removeMIVar(MISession miSession, MIVar miVar) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIVarDelete var = factory.createMIVarDelete(miVar.getVarName());
		try {
			miSession.postCommand(var);
			var.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * Remove variable form the maintained cache list.
	 * @param miSession
	 * @param varName
	 * @return
	 */
	public Variable removeVariableFromList(MISession miSession, String varName) {
		Target target = ((Session)getSession()).getTarget(miSession);
		List varList = getVariablesList(target);
		synchronized (varList) {
			for (Iterator iterator = varList.iterator(); iterator.hasNext();) {
				Variable variable = (Variable)iterator.next();
				if (variable.getMIVar().getVarName().equals(varName)) {
					iterator.remove();
					return variable;
				}
			}
		}
		return null;
	}

	/**
	 * Encode the variableDescriptor as an array
	 * @param varDesc
	 * @param start
	 * @param length
	 * @return
	 * @throws CDIException
	 */
	public VariableDescriptor getVariableDescriptorAsArray(VariableDescriptor varDesc, int start, int length)
		throws CDIException {
		Target target = (Target)varDesc.getTarget();
		Thread thread = (Thread)varDesc.getThread();
		StackFrame frame = (StackFrame)varDesc.getStackFrame();
		String name = varDesc.getName();
		String fullName = varDesc.getFullName();
		int pos = varDesc.getPosition();
		int depth = varDesc.getStackDepth();
		VariableDescriptor vo = null;

		if (varDesc instanceof ArgumentDescriptor || varDesc instanceof Argument) {
			vo = new ArgumentDescriptor(target, thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof LocalVariableDescriptor || varDesc instanceof LocalVariable) {
			vo = new LocalVariableDescriptor(target, thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof GlobalVariableDescriptor || varDesc instanceof GlobalVariable) {
			vo = new GlobalVariableDescriptor(target, thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof RegisterDescriptor || varDesc instanceof Register) {
			vo = new RegisterDescriptor(target, thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof ThreadStorageDescriptor || varDesc instanceof ThreadStorage) {
			vo = new ThreadStorageDescriptor(target, thread, frame, name, fullName, pos, depth);
		} else {
			throw new CDIException(CdiResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$			
		}

		vo.setCastingArrayStart(varDesc.getCastingArrayStart() + start);
		vo.setCastingArrayEnd(length);
		return vo;
	}

	/**
	 * Encode the variableDescriptor in a typecasting.
	 * @param varDesc
	 * @param type
	 * @return
	 * @throws CDIException
	 */
	public VariableDescriptor getVariableDescriptorAsType(VariableDescriptor varDesc, String type) throws CDIException {
		// throw an exception if not a good type.
		Target target = (Target)varDesc.getTarget();
		Thread thread = (Thread)varDesc.getThread();
		StackFrame frame = (StackFrame)varDesc.getStackFrame();
		String name = varDesc.getName();
		String fullName = varDesc.getFullName();
		int pos = varDesc.getPosition();
		int depth = varDesc.getStackDepth();

		// Check the type validity.
		{
			StackFrame f = frame;
			if (f == null) {
				if (thread != null) {
					f = thread.getCurrentStackFrame();
				} else {
					Thread t = (Thread)target.getCurrentThread();
					f = t.getCurrentStackFrame();
				}
			}
			checkType(f, type);
		}

		VariableDescriptor vo = null;

		if (varDesc instanceof ArgumentDescriptor || varDesc instanceof Argument) {
			vo = new ArgumentDescriptor(target, thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof LocalVariableDescriptor || varDesc instanceof LocalVariable) {
			vo = new LocalVariableDescriptor(target, thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof GlobalVariableDescriptor || varDesc instanceof GlobalVariable) {
			vo = new GlobalVariableDescriptor(target, thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof ThreadStorageDescriptor || varDesc instanceof ThreadStorage) {
			vo = new ThreadStorageDescriptor(target, thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof RegisterDescriptor || varDesc instanceof Register) {
			vo = new RegisterDescriptor(target, thread, frame, name, fullName, pos, depth);
		} else {
			throw new CDIException(CdiResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$			
		}

		String[] castings = varDesc.getCastingTypes();
		if (castings == null) {
			castings = new String[] { type };
		} else {
			String[] temp = new String[castings.length + 1];
			System.arraycopy(castings, 0, temp, 0, castings.length);
			temp[castings.length] = type;
			castings = temp;
		}
		vo.setCastingTypes(castings);
		return vo;
	}

	public Variable createVariable(VariableDescriptor varDesc) throws CDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			return createArgument((ArgumentDescriptor)varDesc);
		} else if (varDesc instanceof LocalVariableDescriptor) {
			return createLocalVariable((LocalVariableDescriptor)varDesc);
		} else if (varDesc instanceof GlobalVariableDescriptor) {
			return createGlobalVariable((GlobalVariableDescriptor)varDesc);
		} else if (varDesc instanceof RegisterDescriptor) {
			RegisterManager regMgr = ((Session)getSession()).getRegisterManager();
			return regMgr.createRegister((RegisterDescriptor)varDesc);
		} else if (varDesc instanceof ThreadStorageDescriptor) {
			return createThreadStorage((ThreadStorageDescriptor)varDesc);
		}
		throw new CDIException(CdiResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$			
	}

	public Argument createArgument(ArgumentDescriptor argDesc) throws CDIException {
		Variable variable = findVariable(argDesc);
		Argument argument = null;
		if (variable != null && variable instanceof Argument) {
			argument = (Argument) variable;
		}
		if (argument == null) {
			String name = argDesc.getQualifiedName();
			StackFrame stack = (StackFrame)argDesc.getStackFrame();
			Target target = (Target)argDesc.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.setCurrentThread(stack.getThread(), false);
			((Thread)stack.getThread()).setCurrentStackFrame(stack, false);
			try {
				MISession mi = target.getMISession();
				CommandFactory factory = mi.getCommandFactory();
				MIVarCreate var = factory.createMIVarCreate(name);
				mi.postCommand(var);
				MIVarCreateInfo info = var.getMIVarCreateInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				argument = new Argument(argDesc, info.getMIVar());
				List variablesList = getVariablesList(target);
				variablesList.add(argument);
			} catch (MIException e) {
				throw new MI2CDIException(e);
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
			}
		}
		return argument;
	}

	public ICDIArgumentDescriptor[] getArgumentDescriptors(StackFrame frame) throws CDIException {
		List argObjects = new ArrayList();
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		try {
			MISession mi = target.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			int depth = frame.getThread().getStackFrameCount();
			int level = frame.getLevel();
			// Need the GDB/MI view of level which the reverse i.e. Highest frame is 0
			int miLevel = depth - level;
			MIStackListArguments listArgs = factory.createMIStackListArguments(false, miLevel, miLevel);
			MIArg[] args = null;
			mi.postCommand(listArgs);
			MIStackListArgumentsInfo info = listArgs.getMIStackListArgumentsInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$ 
			}
			MIFrame[] miFrames = info.getMIFrames();
			if (miFrames != null && miFrames.length == 1) {
				args = miFrames[0].getArgs();
			}
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					ArgumentDescriptor arg = new ArgumentDescriptor(target, null, frame, args[i].getName(), null, args.length - i, level);
					argObjects.add(arg);
				}
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
		return (ICDIArgumentDescriptor[]) argObjects.toArray(new ICDIArgumentDescriptor[0]);
	}

	public GlobalVariableDescriptor getGlobalVariableDescriptor(Target target, String filename, String function, String name) throws CDIException {
		if (filename == null) {
			filename = new String();
		}
		if (function == null) {
			function = new String();
		}
		if (name == null) {
			name = new String();
		}
		StringBuffer buffer = new StringBuffer();
		if (filename.length() > 0) {
			buffer.append('\'').append(filename).append('\'').append("::"); //$NON-NLS-1$
		}
		if (function.length() > 0) {
			buffer.append(function).append("::"); //$NON-NLS-1$
		}
		buffer.append(name);
		return new GlobalVariableDescriptor(target, null, null, buffer.toString(), null, 0, 0);
	}

	public GlobalVariable createGlobalVariable(GlobalVariableDescriptor varDesc) throws CDIException {
		Variable variable = findVariable(varDesc);
		GlobalVariable global = null;
		if (variable instanceof GlobalVariable) {
			global = (GlobalVariable)variable;
		}
		if (global == null) {
			String name = varDesc.getQualifiedName();
			Target target = (Target)varDesc.getTarget();
			try {
				MISession mi = target.getMISession();
				CommandFactory factory = mi.getCommandFactory();
				MIVarCreate var = factory.createMIVarCreate(name);
				mi.postCommand(var);
				MIVarCreateInfo info = var.getMIVarCreateInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				global = new GlobalVariable(varDesc, info.getMIVar());
				List variablesList = getVariablesList(target);
				variablesList.add(global);
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		}
		return global;
	}

	public ICDILocalVariableDescriptor[] getLocalVariableDescriptors(StackFrame frame) throws CDIException {
		List varObjects = new ArrayList();
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		try {
			MISession mi = target.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			int level = frame.getLevel();
			MIArg[] args = null;
			MIStackListLocals locals = factory.createMIStackListLocals(false);
			mi.postCommand(locals);
			MIStackListLocalsInfo info = locals.getMIStackListLocalsInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			args = info.getLocals();
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					LocalVariableDescriptor varObj = new LocalVariableDescriptor(target, null, frame, args[i].getName(), null, args.length - i, level);
					varObjects.add(varObj);
				}
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
		return (ICDILocalVariableDescriptor[]) varObjects.toArray(new ICDILocalVariableDescriptor[0]);
	}

	public LocalVariable createLocalVariable(LocalVariableDescriptor varDesc) throws CDIException {
		Variable variable = findVariable(varDesc);
		LocalVariable local = null;
		if (variable instanceof LocalVariable) {
			local = (LocalVariable)variable;
		}
		if (local == null) {
			String name = varDesc.getQualifiedName();
			StackFrame stack = (StackFrame)varDesc.getStackFrame();
			Target target = (Target)varDesc.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.setCurrentThread(stack.getThread(), false);
			((Thread)stack.getThread()).setCurrentStackFrame(stack, false);
			try {
				MISession mi = target.getMISession();
				CommandFactory factory = mi.getCommandFactory();
				MIVarCreate var = factory.createMIVarCreate(name);
				mi.postCommand(var);
				MIVarCreateInfo info = var.getMIVarCreateInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				local = new LocalVariable(varDesc, info.getMIVar());
				List variablesList = getVariablesList(target);
				variablesList.add(local);
			} catch (MIException e) {
				throw new MI2CDIException(e);
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
			}
		}
		return local;
	}

	public ICDIThreadStorageDescriptor[] getThreadStorageDescriptors(Thread thread) throws CDIException {
		return new ICDIThreadStorageDescriptor[0];
	}

	public ThreadStorage createThreadStorage(ThreadStorageDescriptor desc) throws CDIException {
		throw new CDIException(CdiResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$
	}

	public void destroyVariable(Variable variable) throws CDIException {
		// Fire  a destroyEvent ?
		Target target = (Target)variable.getTarget();
		MISession mi = target.getMISession();
		// no need to call -var-delete for variable that are not in
		// the list most probaby they are children of other variables and in this case
		// we should not delete them
		List varList = getVariablesList(target);
		if (varList.contains(variable)) {
			removeMIVar(mi, variable.getMIVar());
		}
		MIVarDeletedEvent del = new MIVarDeletedEvent(mi, variable.getMIVar().getVarName());
		mi.fireEvent(del);
	}

	public void destroyAllVariables(Target target) throws CDIException {
		Variable[] variables = getVariables(target);
		MISession mi = target.getMISession();
		for (int i = 0; i < variables.length; ++i) {
			removeMIVar(mi, variables[i].getMIVar());
			MIVarDeletedEvent del = new MIVarDeletedEvent(mi, variables[i].getMIVar().getVarName());
			mi.fireEvent(del);			
		}
	}

	/**
	 * Update the elements in the cache, from the response of the "-var-update"
	 * mi/command.  Althought tempting we do not use the "-var-update *" command, since
	 * for some reason on gdb-5.2.1 it starts to misbehave until it hangs ... sigh
	 * We take the approach of updating the variables ourselfs.  But we do it a smart
	 * way by only updating the variables visible in the current stackframe but not
	 * the other locals in different frames.  The downside if any side effects we loose,
	 * This ok, since the IDE only a frame at a time.
	 *
	 */
	public void update(Target target) throws CDIException {
		int highLevel = 0;
		int lowLevel = 0;
		List eventList = new ArrayList();
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		Variable[] vars = getVariables(target);
		ICDIStackFrame[] frames = null;
		StackFrame currentStack = null;
		Thread currentThread = (Thread)target.getCurrentThread();
		if (currentThread != null) {
			currentStack = currentThread.getCurrentStackFrame();
			if (currentStack != null) {
				highLevel = currentStack.getLevel();
			}
			if (highLevel > MAX_STACK_DEPTH) {
				highLevel = MAX_STACK_DEPTH;
			}
			lowLevel = highLevel - MAX_STACK_DEPTH;
			if (lowLevel < 0) {
				lowLevel = 0;
			}
			frames = currentThread.getStackFrames(0, highLevel);
		}
		for (int i = 0; i < vars.length; i++) {
			Variable variable = vars[i];
			if (isVariableNeedsToBeUpdate(variable, currentStack, frames, lowLevel)) {
				String varName = variable.getMIVar().getVarName();
				MIVarChange[] changes = noChanges;
				MIVarUpdate update = factory.createMIVarUpdate(varName);
				try {
					mi.postCommand(update);
					MIVarUpdateInfo info = update.getMIVarUpdateInfo();
					if (info == null) {
						throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
					}
					changes = info.getMIVarChanges();
				} catch (MIException e) {
					//throw new MI2CDIException(e);
					eventList.add(new MIVarDeletedEvent(mi, varName));
				}
				variable.setUpdated(true);
				for (int j = 0; j < changes.length; j++) {
					String n = changes[j].getVarName();
					if (changes[j].isInScope()) {
						eventList.add(new MIVarChangedEvent(mi, n));
					} else {
						destroyVariable(variable);
						eventList.add(new MIVarDeletedEvent(mi, n));
					}
				}
			} else {
				variable.setUpdated(false);
			}
		}
		MIEvent[] events = (MIEvent[]) eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
	}

	public void update(Variable variable) throws CDIException {
		Target target = (Target)variable.getTarget();
		MISession mi = target.getMISession();
		List eventList = new ArrayList();
		update(target, variable, eventList);
		MIEvent[] events = (MIEvent[]) eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);		
	}

	public void update(Target target, Variable variable, List eventList) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String varName = variable.getMIVar().getVarName();
		MIVarChange[] changes = noChanges;
		MIVarUpdate update = factory.createMIVarUpdate(varName);
		try {
			mi.postCommand(update);
			MIVarUpdateInfo info = update.getMIVarUpdateInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			changes = info.getMIVarChanges();
		} catch (MIException e) {
			//throw new MI2CDIException(e);
			eventList.add(new MIVarDeletedEvent(mi, varName));
		}
		variable.setUpdated(true);
		for (int j = 0; j < changes.length; j++) {
			String n = changes[j].getVarName();
			if (changes[j].isInScope()) {
				eventList.add(new MIVarChangedEvent(mi, n));
			} else {
				destroyVariable(variable);
				eventList.add(new MIVarDeletedEvent(mi, n));
			}
		}
	}

	/**
	 * We are trying to minimize the impact of the updates, this can be very long and unncessary if we
	 * have a very deep stack and lots of local variables.  We can assume here that the local variables
	 * in the other non-selected stackframe will not change and only update the selected frame variables.
	 * 
	 * @param variable
	 * @param current
	 * @param frames
	 * @return
	 */
	boolean isVariableNeedsToBeUpdate(Variable variable, ICDIStackFrame current, ICDIStackFrame[] frames, int lowLevel)
		throws CDIException {
		ICDIStackFrame varStack = variable.getStackFrame();
		boolean inScope = false;

		// Something wrong and the program terminated bail out here.
		if (current == null || frames == null) {
			return false;
		}

		// If the variable Stack is null, it means this is a global variable we should update
		if (varStack == null) {
			return true;
		} else if (varStack.equals(current)) {
			// The variable is in the current selected frame it should be updated
			return true;
		} else {
			if (varStack.getLevel() >= lowLevel) {
				// Check if the Variable is still in Scope 
				// if it is no longer in scope so update() can call "-var-delete".
				for (int i = 0; i < frames.length; i++) {
					if (varStack.equals(frames[i])) {
						inScope = true;
					}
				}
			} else {
				inScope = true;
			}
		}
		// return true if the variable is no longer in scope we
		// need to call -var-delete.
		return !inScope;
	}

}
