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
import org.eclipse.cdt.debug.core.cdi.ICDIVariableManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.*;
import org.eclipse.cdt.debug.mi.core.cdi.model.Argument;
import org.eclipse.cdt.debug.mi.core.cdi.model.StackFrame;
import org.eclipse.cdt.debug.mi.core.cdi.model.Thread;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIStackListArguments;
import org.eclipse.cdt.debug.mi.core.command.MIStackListLocals;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.command.MIVarDelete;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIArg;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIStackListArgumentsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIStackListLocalsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;

/**
 */
public class VariableManager extends SessionObject implements ICDIVariableManager, IUpdateListener {

	List variableList;
	boolean autoupdate;

	public VariableManager(Session session) {
		super(session);
		variableList = Collections.synchronizedList(new ArrayList());
		autoupdate = true;
	}

	/**
	 * Return the element that have the uniq varName.
	 * null is return if the element is not in the cache.
	 */
	public Variable getVariable(String varName) {
		Variable[] vars = getVariables();
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getMIVar().getVarName().equals(varName)) {
				return vars[i];
			}
		}
		return null;
	}

	/**
	 * Return the Element with this stackframe, and with this name.
	 * null is return if the element is not in the cache.
	 */
	Variable findVariable(VariableObject v) throws CDIException {
		ICDIStackFrame stack = v.getStackFrame();
		String name = v.getName();
		int position = v.getPosition();
		int depth = v.getStackDepth();
		Variable[] vars = getVariables();
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getName().equals(name)) {
				if (stack != null && vars[i].getStackFrame().equals(stack)) {
					if (vars[i].getVariableObject().getPosition() == position) {
						if (vars[i].getVariableObject().getStackDepth() == depth) {
							return vars[i];
						}
					}
				} else {
					return vars[i];
				}
			}
		}
		return null;
	}

	/**
	 * Make sure an element is not added twice.
	 */
	void addVariable(Variable var) {
		Variable[] vars = getVariables();
		for (int i = 0; i < vars.length; i++) {
			String name = vars[i].getMIVar().getVarName();
			if (name.equals(var.getMIVar().getVarName())) {
				return;
			}
		}
		variableList.add(var);
	}

	/**
	 * Returns all the elements that are in the cache.
	 */
	Variable[] getVariables() {
		return (Variable[]) variableList.toArray(new Variable[0]);
	}

	public Variable createVariable(VariableObject v, MIVar mivar) throws CDIException {
		Variable variable = new Variable(v, mivar);
		addVariable(variable);
		return variable;
	}

	/**
	 * Tell gdb to remove the underlying var-object also.
	 */
	void removeMIVar(MIVar miVar) throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarDelete var = factory.createMIVarDelete(miVar.getVarName());
		try {
			mi.postCommand(var);
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
	public void removeVariable(String varName) throws CDIException {
		Variable[] vars = getVariables();
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getMIVar().getVarName().equals(varName)) {
				variableList.remove(vars[i]);
				removeMIVar(vars[i].getMIVar());
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#createArgument(ICDIArgumentObject)
	 */
	public ICDIArgument createArgument(ICDIArgumentObject a) throws CDIException {
		if (a instanceof ArgumentObject) {
			ArgumentObject argObj = (ArgumentObject)a;
			Variable variable = findVariable(argObj);
			Argument argument = null;
			if (variable != null && variable instanceof Argument) {
				argument = (Argument)variable;
			}
			if (argument == null) {
				String name = argObj.getName();
				StackFrame stack = argObj.getStackFrame();
				Session session = (Session)getSession();
				Thread currentThread = null;
				StackFrame currentFrame = null;
				if (stack != null) {
					ICDITarget currentTarget = session.getCurrentTarget();
					currentThread = (Thread)currentTarget.getCurrentThread();
					currentFrame = (StackFrame)currentThread.getCurrentStackFrame();
					((Thread)stack.getThread()).setCurrentStackFrame(stack, false);
				}
				try {
					MISession mi = session.getMISession();
					CommandFactory factory = mi.getCommandFactory();
					MIVarCreate var = factory.createMIVarCreate(name);
					mi.postCommand(var);
					MIVarCreateInfo info = var.getMIVarCreateInfo();
					if (info == null) {
						throw new CDIException("No answer");
					}
					argument = new Argument(argObj, info.getMIVar());
					addVariable(argument);
				} catch (MIException e) {
					throw new MI2CDIException(e);
				} finally {
					if (currentThread != null) {
						currentThread.setCurrentStackFrame(currentFrame, false);
					}
				}
			}
			return argument;
		}
		throw  new CDIException("Wrong variable type");
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getArgumentObject(ICDIStackFrame, String)
	 */
	public ICDIArgumentObject getArgumentObject(ICDIStackFrame stack, String name)
		throws CDIException {
		ICDIArgumentObject[] argsObjects = getArgumentObjects(stack);
		for (int i = 0; i < argsObjects.length; i++) {
			if (argsObjects[i].getName().equals(name)) {
				return argsObjects[i];
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getArgumentObjects(ICDIStackFrame)
	 */
	public ICDIArgumentObject[] getArgumentObjects(ICDIStackFrame frame) throws CDIException {
		if (!(frame instanceof StackFrame)) {
			return new ICDIArgumentObject[0];
		}
		List argObjects = new ArrayList();
		Session session = (Session)getSession();
		ICDITarget currentTarget = session.getCurrentTarget();
		Thread currentThread = (Thread)currentTarget.getCurrentThread();
		StackFrame currentFrame = (StackFrame)currentThread.getCurrentStackFrame();
		((Thread)(frame.getThread())).setCurrentStackFrame((StackFrame)frame, false);
		try {
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			int depth = frame.getThread().getStackFrameCount();
			int level = frame.getLevel();
			MIStackListArguments listArgs =
			factory.createMIStackListArguments(false, level, level);
			MIArg[] args = null;
			mi.postCommand(listArgs);
			MIStackListArgumentsInfo info = listArgs.getMIStackListArgumentsInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MIFrame[] miFrames = info.getMIFrames();
			if (miFrames != null && miFrames.length == 1) {
				args = miFrames[0].getArgs();
			}
			if (args != null) {
				ICDITarget target = frame.getThread().getTarget();
				for (int i = 0; i < args.length; i++) {
					ArgumentObject arg = new ArgumentObject(target, args[i].getName(),
					 (StackFrame)frame, args.length - i, depth);
					argObjects.add(arg);
				}
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			currentThread.setCurrentStackFrame(currentFrame);
		}
		return (ICDIArgumentObject[])argObjects.toArray(new ICDIArgumentObject[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getVariableObject(ICDIStackFrame, String)
	 */
	public ICDIVariableObject getVariableObject(ICDIStackFrame stack, String name) throws CDIException {
		ICDIVariableObject[] varObjects = getVariableObjects(stack);
		for (int i = 0; i < varObjects.length; i++) {
			if (varObjects[i].getName().equals(name)) {
				return varObjects[i];
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getVariableObject(String, String, String)
	 */
	public ICDIVariableObject getVariableObject(String filename, String function, String name) throws CDIException {
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
			buffer.append('\'').append(filename).append('\'').append("::");
		}
		if (function.length() > 0) {
			buffer.append(function).append("::");
		}
		buffer.append(name);
		ICDITarget target = getSession().getCurrentTarget();
		return new VariableObject(target, buffer.toString(), null, 0, 0);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getVariableObjects(ICDIStackFrame)
	 */
	public ICDIVariableObject[] getVariableObjects(ICDIStackFrame frame) throws CDIException {
		if (!(frame instanceof StackFrame)) {
			return new ICDIVariableObject[0];
		}
		List varObjects = new ArrayList();
		Session session = (Session)getSession();
		ICDITarget currentTarget = session.getCurrentTarget();
		Thread currentThread = (Thread)currentTarget.getCurrentThread();
		StackFrame currentFrame = (StackFrame)currentThread.getCurrentStackFrame();
		((Thread)(frame.getThread())).setCurrentStackFrame((StackFrame)frame, false);
		try {
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			int depth = frame.getThread().getStackFrameCount();
			MIArg[] args = null;
			MIStackListLocals locals = factory.createMIStackListLocals(false);
			mi.postCommand(locals);
			MIStackListLocalsInfo info = locals.getMIStackListLocalsInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			args = info.getLocals();
			if (args != null) {
				ICDITarget target = frame.getThread().getTarget();
				for (int i = 0; i < args.length; i++) {
					VariableObject varObj = new VariableObject(target, args[i].getName(),
						 (StackFrame)frame, args.length - i, depth);
					varObjects.add(varObj);
				}
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
			currentThread.setCurrentStackFrame((StackFrame)currentFrame, false);
		}
		return (ICDIVariableObject[])varObjects.toArray(new ICDIVariableObject[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#createVariable(ICDIVariableObject)
	 */
	public ICDIVariable createVariable(ICDIVariableObject v) throws CDIException {
		if (v instanceof VariableObject) {
			VariableObject varObj = (VariableObject)v;
			Variable variable = findVariable(varObj);
			if (variable == null) {
				String name = varObj.getName();
				Session session = (Session)getSession();
				StackFrame stack = varObj.getStackFrame();
				Thread currentThread = null;
				StackFrame currentFrame = null;
				if (stack != null) {
					ICDITarget currentTarget = session.getCurrentTarget();
					currentThread = (Thread)currentTarget.getCurrentThread();
					currentFrame = (StackFrame)currentThread.getCurrentStackFrame();
					((Thread)stack.getThread()).setCurrentStackFrame(stack, false);
				}
				try {
					MISession mi = session.getMISession();
					CommandFactory factory = mi.getCommandFactory();
					MIVarCreate var = factory.createMIVarCreate(name);
					mi.postCommand(var);
					MIVarCreateInfo info = var.getMIVarCreateInfo();
					if (info == null) {
						throw new CDIException("No answer");
					}
					variable = new Variable(varObj, info.getMIVar());
					addVariable(variable);
				} catch (MIException e) {
					throw new MI2CDIException(e);
				} finally {
					if (currentThread != null) {
						currentThread.setCurrentStackFrame(currentFrame, false);
					}
				}
			}
			return variable;
		}
		throw new CDIException("Wrong variable type");
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#destroyVariable(ICDIVariable)
	 */
	public void destroyVariable(ICDIVariable var) throws CDIException {
		if (var instanceof Variable) {
			// Fire  a destroyEvent ?
			Variable variable = (Variable)var;
			MIVarChangedEvent change = new MIVarChangedEvent(0, variable.getMIVar().getVarName(), false);
			Session session = (Session)getSession();
			MISession mi = session.getMISession();
			mi.fireEvent(change);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.mi.core.cdi.IVarUpdateListener#changeList(MIVarChange[])
	 */
	public void changeList(MIVarChange[] changes) {
		List eventList = new ArrayList(changes.length);
		for (int i = 0 ; i < changes.length; i++) {
			String varName = changes[i].getVarName();
			Variable variable = getVariable(varName);
			if (variable != null) {
				eventList.add(new MIVarChangedEvent(0, varName, changes[i].isInScope()));
			}
		}
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#isAutoUpdate()
	 */
	public boolean isAutoUpdate() {
		return autoupdate;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#setAutoUpdate(boolean)
	 */
	public void setAutoUpdate(boolean update) {
		autoupdate = update;
	}

	/**
	 * Update the elements in the cache, from the response of the "-var-update *"
	 * mi/command.
	 *
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#createArgument(ICDIArgumentObject)
	 */
	public void update() throws CDIException {
		Session session = (Session)getSession();
		UpdateManager mgr = session.getUpdateManager();
		mgr.update();
	}

}
