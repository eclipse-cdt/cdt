/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.command.MIVarDelete;
import org.eclipse.cdt.debug.mi.core.command.MIVarUpdate;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarUpdateInfo;

/**
 */
public class VariableManager extends SessionObject {

	List elementList;

	/**
	 * Class container to regroup all info concerning a variable.
	 */
	class Element {
		MIVar miVar;
		String name;
		StackFrame stackframe;
		Variable variable;
	}

	public VariableManager(CSession session) {
		super(session);
		elementList = new ArrayList();
	}

	Element getElement(String varName) {
		Element[] elements = getElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].miVar.getVarName().equals(varName)) {
				return elements[i];
			}
		}
		return null;
	}

	Element getElement(StackFrame stack, String name) {
		Element[] elements = getElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].stackframe.equals(stack) &&
				elements[i].name.equals(name)) {
				return elements[i];
			}
		}
		return null;
	}

	void addElement(Element element) {
		Element[] elements = getElements();
		for (int i = 0; i < elements.length; i++) {
			String name = elements[i].miVar.getVarName();
			if (name.equals(element.miVar.getVarName())) {
			//	Thread.currentThread().dumpStack();
				return;
			}
		}
		elementList.add(element);
	}

	Element[] getElements() {
		return (Element[]) elementList.toArray(new Element[0]);
	}

	void update() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarUpdate update = factory.createMIVarUpdate();
		try {
			mi.postCommand(update);
			MIVarUpdateInfo info = update.getMIVarUpdateInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MIVarChange[]changes = info.getMIVarChanges();
			for (int i = 0 ; i < changes.length; i++) {
				ICDIEvent cdiEvent = null;
				Element element = getElement(changes[i].getVarName());
				if (!changes[i].isInScope()) {
					if (element != null) {
						cdiEvent = new DestroyedEvent(getCSession(), element.variable);
					}
					removeVariable(changes[i]);
				} else {
					if (element != null) {
						cdiEvent = new ChangedEvent(getCSession(), element.variable);
					}
				}
				EventManager mgr = (EventManager)getCSession().getEventManager();
				mgr.fireEvent(cdiEvent);
			}
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
	}

	Element createElement(StackFrame stack, String name) throws CDIException {
		Element element = getElement(stack, name);
		if (element == null) {
			stack.getCThread().setCurrentStackFrame(stack);
			MISession mi = getCSession().getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIVarCreate var = factory.createMIVarCreate(name);
			try {
				mi.postCommand(var);
				MIVarCreateInfo info = var.getMIVarCreateInfo();
				if (info == null) {
					throw new CDIException("No answer");
				}
				element = new Element();
				element.miVar = info.getMIVar();
				element.name = name;
				element.stackframe = stack;
			} catch (MIException e) {
				throw new CDIException(e.toString());
			}
		}
		return element;
	}

	void removeMIVar(MIVar miVar) throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarDelete var = factory.createMIVarDelete(miVar.getVarName());
		try {
			mi.postCommand(var);
			MIInfo info = var.getMIInfo();
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
	}

	void removeVariable(MIVarChange changed) throws CDIException {
		String varName = changed.getVarName();
		removeVariable(varName);
	}

	void removeVariable(String varName) throws CDIException {
		Element[] elements = getElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].miVar.getVarName().equals(varName)) {
				elementList.remove(elements[i]);
				removeMIVar(elements[i].miVar);
			}
		}
	}

	void removeVariable(Variable variable) throws CDIException {
		String varName = ((Variable)variable).getMIVar().getVarName();
		removeVariable(varName);
	}

	void removeVariables(Variable[] variables) throws CDIException {
		for (int i = 0; i < variables.length; i++) {
			removeVariable(variables[i]);
		}
	}

	ICDIVariable createVariable(StackFrame stack, String name) throws CDIException {
		Element element = createElement(stack, name);
		Variable var = new Variable(stack, name, element.miVar);
		element.variable = var;
		addElement(element);
		return (ICDIVariable)var;
	}


	ICDIArgument createArgument(StackFrame stack, String name) throws CDIException {
		Element element = createElement(stack, name);
		Variable carg = new Argument(stack, name,element.miVar);
		element.variable = carg;
		addElement(element);
		return (ICDIArgument)carg;
	}

	ICDIExpression createExpression(StackFrame stack, String name) throws CDIException {
		Element element = createElement(stack, name);
		Variable cexp = new Expression(stack, name, element.miVar);
		element.variable = cexp;
		addElement(element);
		return (ICDIExpression)cexp;
	}

	ICDIRegister createRegister(StackFrame stack, String name) throws CDIException {
		Element element = createElement(stack, "$" + name);
		Variable reg = new Register(stack, name, element.miVar);
		element.variable = reg;
		addElement(element);
		return (ICDIRegister)reg;
	}

}
