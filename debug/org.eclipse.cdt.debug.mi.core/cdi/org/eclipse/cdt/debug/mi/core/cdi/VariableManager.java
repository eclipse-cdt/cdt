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
import org.eclipse.cdt.debug.core.cdi.ICDIVariableManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Argument;
import org.eclipse.cdt.debug.mi.core.cdi.model.ArgumentObject;
import org.eclipse.cdt.debug.mi.core.cdi.model.StackFrame;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
import org.eclipse.cdt.debug.mi.core.cdi.model.VariableObject;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIPType;
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
import org.eclipse.cdt.debug.mi.core.output.MIPTypeInfo;
import org.eclipse.cdt.debug.mi.core.output.MIStackListArgumentsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIStackListLocalsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarUpdateInfo;

/**
 */
public class VariableManager extends Manager implements ICDIVariableManager {

	static final ICDIVariable[] EMPTY_VARIABLES = {};
	// We put a restriction on how deep we want to
	// go when doing update of the variables.
	// If the number is to high, gdb will just hang.
	int MAX_STACK_DEPTH = 200;
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
	 * Return the Element with this stackframe, and with this name.
	 * null is return if the element is not in the cache.
	 */
	Variable findVariable(VariableObject v) throws CDIException {
		Target target = (Target)v.getTarget();
		ICDIStackFrame stack = v.getStackFrame();
		String name = v.getName();
		int position = v.getPosition();
		int depth = v.getStackDepth();
		Variable[] vars = getVariables(target);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getName().equals(name)
				&& vars[i].getCastingArrayStart() == v.getCastingArrayStart()
				&& vars[i].getCastingArrayEnd() == v.getCastingArrayEnd()
				&& ((vars[i].getCastingType() == null && v.getCastingType() == null)
					|| (vars[i].getCastingType() != null
						&& v.getCastingType() != null
						&& vars[i].getCastingType().equals(v.getCastingType())))) {
				ICDIStackFrame frame = vars[i].getStackFrame();
				if (stack == null && frame == null) {
					return vars[i];
				} else if (frame != null && stack != null && frame.equals(stack)) {
					if (vars[i].getPosition() == position) {
						if (vars[i].getStackDepth() == depth) {
							return vars[i];
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
			Session session = (Session)getSession();
			Target target = (Target)frame.getTarget();
			Target currentTarget = session.getCurrentTarget();
			ICDIThread currentThread = target.getCurrentThread();
			ICDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
			session.setCurrentTarget(target);
			target.setCurrentThread(frame.getThread(), false);
			frame.getThread().setCurrentStackFrame(frame, false);
			try {
				MISession miSession = target.getMISession();
				CommandFactory factory = miSession.getCommandFactory();
				MIPType ptype = factory.createMIPType(type);
				miSession.postCommand(ptype);
				MIPTypeInfo info = ptype.getMIPtypeInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			} finally {
				session.setCurrentTarget(currentTarget);
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
	 * When element are remove from the cache, they are put on the OutOfScope list, oos,
	 * because they are still needed for the destroy events.  The destroy event will
	 * call removeOutOfScope.
	 */
	public void removeVariable(MISession miSession, String varName) throws CDIException {
		Target target = ((Session)getSession()).getTarget(miSession);
		removeVariable(target, varName);
	}
	public void removeVariable(Target target, String varName) throws CDIException {
		Variable[] vars = getVariables(target);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getMIVar().getVarName().equals(varName)) {
				List variableList = (List)variablesMap.get(target);
				if (variableList != null) {
					variableList.remove(vars[i]);
				}
				removeMIVar(target.getMISession(), vars[i].getMIVar());
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#createArgument(ICDIArgumentObject)
	 */
	public ICDIArgument createArgument(ICDIArgumentObject a) throws CDIException {
		ArgumentObject argObj = null;
		if (a instanceof ArgumentObject) {
			argObj = (ArgumentObject) a;
		}
		if (argObj != null) {
			Variable variable = findVariable(argObj);
			Argument argument = null;
			if (variable != null && variable instanceof Argument) {
				argument = (Argument) variable;
			}
			if (argument == null) {
				String name = argObj.getQualifiedName();
				ICDIStackFrame stack = argObj.getStackFrame();
				Session session = (Session) getSession();
				ICDIThread currentThread = null;
				ICDIStackFrame currentFrame = null;
				Target target = (Target)argObj.getTarget();
				Target currentTarget = session.getCurrentTarget();
				if (stack != null) {
					currentThread = target.getCurrentThread();
					currentFrame = currentThread.getCurrentStackFrame();
					target.setCurrentThread(stack.getThread(), false);
					stack.getThread().setCurrentStackFrame(stack, false);
				}
				try {
					MISession mi = target.getMISession();
					CommandFactory factory = mi.getCommandFactory();
					MIVarCreate var = factory.createMIVarCreate(name);
					mi.postCommand(var);
					MIVarCreateInfo info = var.getMIVarCreateInfo();
					if (info == null) {
						throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
					}
					argument = new Argument(argObj, info.getMIVar());
					List variablesList = getVariablesList(target);
					variablesList.add(argument);
				} catch (MIException e) {
					throw new MI2CDIException(e);
				} finally {
					session.setCurrentTarget(currentTarget);
					if (currentThread != null) {
						target.setCurrentThread(currentThread, false);
						currentThread.setCurrentStackFrame(currentFrame, false);
					}
				}
			}
			return argument;
		}
		throw new CDIException(CdiResources.getString("cdi.VariableManager.Wrong_variable_type")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getArgumentObjects(ICDIStackFrame)
	 */
	public ICDIArgumentObject[] getArgumentObjects(ICDIStackFrame frame) throws CDIException {
		List argObjects = new ArrayList();
		Session session = (Session) getSession();
		Target target = (Target)frame.getTarget();
		Target currentTarget = session.getCurrentTarget();
		ICDIThread currentThread = target.getCurrentThread();
		ICDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		frame.getThread().setCurrentStackFrame(frame, false);
		try {
			MISession mi = target.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			int depth = frame.getThread().getStackFrameCount();
			int level = frame.getLevel();
			// Need the GDB/MI view of leve which the reverse i.e. Highest frame is 0
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
					ArgumentObject arg = new ArgumentObject(target, args[i].getName(), frame, args.length - i, level);
					argObjects.add(arg);
				}
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			session.setCurrentTarget(currentTarget);
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
		return (ICDIArgumentObject[]) argObjects.toArray(new ICDIArgumentObject[0]);
	}

	/**
	 * @deprecated
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getGlobalVariableObject(String, String, String)
	 */
	public ICDIVariableObject getGlobalVariableObject(String filename, String function, String name) throws CDIException {
		Target target = ((Session)getSession()).getCurrentTarget();
		return getGlobalVariableObject(target, filename, function, name);
	}
	public ICDIVariableObject getGlobalVariableObject(Target target, String filename, String function, String name) throws CDIException {
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
		return new VariableObject(target, buffer.toString(), null, 0, 0);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getVariableObjectAsArray(ICDIVariableObject, int, int)
	 */
	public ICDIVariableObject getVariableObjectAsArray(ICDIVariableObject object, int start, int length)
		throws CDIException {
		VariableObject obj = null;
		if (object instanceof VariableObject) {
			obj = (VariableObject) object;
		}
		if (obj != null) {
			VariableObject vo =
				new VariableObject(
					(Target)obj.getTarget(),
					obj.getName(),
					obj.getFullName(),
					obj.getStackFrame(),
					obj.getPosition(),
					obj.getStackDepth());
			vo.setCastingArrayStart(obj.getCastingArrayStart() + start);
			vo.setCastingArrayEnd(length);
			return vo;
		}
		throw new CDIException(CdiResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getVariableObjectAsArray(ICDIVariableObject, String, int, int)
	 */
	public ICDIVariableObject getVariableObjectAsType(ICDIVariableObject object, String type) throws CDIException {
		VariableObject obj = null;
		if (object instanceof VariableObject) {
			obj = (VariableObject) object;
		}
		if (obj != null) {
			// throw an exception if not a good type.
			Target target = (Target)obj.getTarget();
			checkType((StackFrame)obj.getStackFrame(), type);
			VariableObject vo =
				new VariableObject(
					target,
					obj.getName(),
					obj.getFullName(),
					obj.getStackFrame(),
					obj.getPosition(),
					obj.getStackDepth());
			String casting = obj.getCastingType();
			if (casting != null && casting.length() > 0) {
				type = "(" + type + ")" + "(" + casting + " )"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			vo.setCastingType(type);
			return vo;
		}
		throw new CDIException(CdiResources.getString("cdi.VariableManager.Unknown_variable_object")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getVariableObjects(ICDIStackFrame)
	 */
	public ICDIVariableObject[] getLocalVariableObjects(ICDIStackFrame frame) throws CDIException {
		List varObjects = new ArrayList();
		Session session = (Session) getSession();
		Target target = (Target)frame.getTarget();
		Target currentTarget = session.getCurrentTarget();
		ICDIThread currentThread = target.getCurrentThread();
		ICDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		frame.getThread().setCurrentStackFrame(frame, false);
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
					VariableObject varObj = new VariableObject(target, args[i].getName(), frame, args.length - i, level);
					varObjects.add(varObj);
				}
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			session.setCurrentTarget(currentTarget);
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
		return (ICDIVariableObject[]) varObjects.toArray(new ICDIVariableObject[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getVariableObjects(ICDIStackFrame)
	 */
	public ICDIVariableObject[] getVariableObjects(ICDIStackFrame frame) throws CDIException {
		ICDIVariableObject[] locals = getLocalVariableObjects(frame);
		ICDIVariableObject[] args = getArgumentObjects(frame);
		ICDIVariableObject[] vars = new ICDIVariableObject[locals.length + args.length];
		System.arraycopy(locals, 0, vars, 0, locals.length);
		System.arraycopy(args, 0, vars, locals.length, args.length);
		return vars;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#createVariable(ICDIVariableObject)
	 */
	public ICDIVariable createVariable(ICDIVariableObject v) throws CDIException {
		VariableObject varObj = null;
		if (v instanceof VariableObject) {
			varObj = (VariableObject) v;
		}
		if (varObj != null) {
			Variable variable = findVariable(varObj);
			if (variable == null) {
				String name = varObj.getQualifiedName();
				Session session = (Session) getSession();
				ICDIStackFrame stack = varObj.getStackFrame();
				ICDIThread currentThread = null;
				ICDIStackFrame currentFrame = null;
				Target target = (Target)varObj.getTarget();
				Target currentTarget = session.getCurrentTarget();
				if (stack != null) {
					currentThread = target.getCurrentThread();
					currentFrame = currentThread.getCurrentStackFrame();
					target.setCurrentThread(stack.getThread(), false);
					stack.getThread().setCurrentStackFrame(stack, false);
				}
				try {
					MISession mi = target.getMISession();
					CommandFactory factory = mi.getCommandFactory();
					MIVarCreate var = factory.createMIVarCreate(name);
					mi.postCommand(var);
					MIVarCreateInfo info = var.getMIVarCreateInfo();
					if (info == null) {
						throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
					}
					variable = new Variable(varObj, info.getMIVar());
					List variablesList = getVariablesList(target);
					variablesList.add(variable);
				} catch (MIException e) {
					throw new MI2CDIException(e);
				} finally {
					session.setCurrentTarget(currentTarget);
					if (currentThread != null) {
						target.setCurrentThread(currentThread, false);
						currentThread.setCurrentStackFrame(currentFrame, false);
					}
				}
			}
			return variable;
		}
		throw new CDIException(CdiResources.getString("cdi.VariableManager.Wrong_variable_type")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#destroyVariable(ICDIVariable)
	 */
	public void destroyVariable(ICDIVariable var) throws CDIException {
		if (var instanceof Variable) {
			// Fire  a destroyEvent ?
			Variable variable = (Variable) var;
			Target target = (Target)variable.getTarget();
			MISession mi = target.getMISession();
			MIVarDeletedEvent del = new MIVarDeletedEvent(mi, variable.getMIVar().getVarName());
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
		int high = 0;
		int low = 0;
		List eventList = new ArrayList();
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		Variable[] vars = getVariables(target);
		ICDIStackFrame[] frames = null;
		ICDIStackFrame currentStack = null;
		ICDIThread currentThread = target.getCurrentThread();
		if (currentThread != null) {
			currentStack = currentThread.getCurrentStackFrame();
			if (currentStack != null) {
				high = currentStack.getLevel();
			}
			if (high > 0) {
				high--;
			}
			low = high - MAX_STACK_DEPTH;
			if (low < 0) {
				low = 0;
			}
			frames = currentThread.getStackFrames(low, high);
		}
		for (int i = 0; i < vars.length; i++) {
			Variable variable = vars[i];
			if (isVariableNeedsToBeUpdate(variable, currentStack, frames, low)) {
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
				for (int j = 0; j < changes.length; j++) {
					String n = changes[j].getVarName();
					if (changes[j].isInScope()) {
						eventList.add(new MIVarChangedEvent(mi, n));
					} else {
						eventList.add(new MIVarDeletedEvent(mi, n));
					}
				}
			}
		}
		MIEvent[] events = (MIEvent[]) eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
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
	boolean isVariableNeedsToBeUpdate(Variable variable, ICDIStackFrame current, ICDIStackFrame[] frames, int low)
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
			if (varStack.getLevel() >= low) {
				// Check if the Variable is still in Scope 
				// if it is no longer in scope so update() call call "-var-delete".
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
