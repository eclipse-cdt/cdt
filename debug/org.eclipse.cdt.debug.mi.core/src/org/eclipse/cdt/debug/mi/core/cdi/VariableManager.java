/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cdt.debug.mi.core.command.MIVarDelete;
import org.eclipse.cdt.debug.mi.core.command.MIVarUpdate;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarChange;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarUpdateInfo;

/**
 */
public class VariableManager extends SessionObject implements ICDIExpressionManager {

	List elementList;
	List oosList;  // Out of Scope variable lists;

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
		oosList = new ArrayList();
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
			if (elements[i].name.equals(name)) {
				if (elements[i].stackframe.equals(stack)) {
					return elements[i];
				}
			}
		}
		return null;
	}

	/**
	 * Make sure an element is not added twice.
	 */
	void addElement(Element element) {
		Element[] elements = getElements();
		for (int i = 0; i < elements.length; i++) {
			String name = elements[i].miVar.getVarName();
			if (name.equals(element.miVar.getVarName())) {
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
			MIVarChange[] changes = info.getMIVarChanges();
			List eventList = new ArrayList(changes.length);
			for (int i = 0 ; i < changes.length; i++) {
				String varName = changes[i].getVarName();
				Element element = getElement(varName);
				if (element != null) {
					eventList.add( new MIVarChangedEvent(varName, changes[i].isInScope()));
				}
				if (! changes[i].isInScope()) {
					// Only remove ICDIVariables.
					if (! (element.variable instanceof Expression)) {
						removeElement(changes[i]);
					}
				}
			}
			MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
			mi.fireEvents(events);
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
	}

	Element createElement(StackFrame stack, String name) throws CDIException {
		Element element = getElement(stack, name);
		if (element == null) {
			//stack.getCThread().setCurrentStackFrame(stack);
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
				throw new CDIException(e.getMessage());
			}
		}
		return element;
	}

	Element removeOutOfScope(String varName) {
		Element[] oos = (Element[])oosList.toArray(new Element[0]);
		for (int i = 0; i < oos.length; i++) {
			if (oos[i].miVar.getVarName().equals(varName)) {
				return oos[i];
			}
		}
		return null;
	}

	void removeMIVar(MIVar miVar) throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarDelete var = factory.createMIVarDelete(miVar.getVarName());
		try {
			mi.postCommand(var);
			MIInfo info = var.getMIInfo();
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
	}

	void removeElement(String varName) throws CDIException {
		Element[] elements = getElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].miVar.getVarName().equals(varName)) {
				elementList.remove(elements[i]);
				oosList.add(elements[i]); // Put on the Out Of Scope list
				removeMIVar(elements[i].miVar);
			}
		}
	}

	void removeElement(MIVarChange changed) throws CDIException {
		String varName = changed.getVarName();
		removeElement(varName);
	}

	void removeElement(Variable variable) throws CDIException {
		String varName = ((Variable)variable).getMIVar().getVarName();
		removeElement(varName);
	}

	void removeElements(Variable[] variables) throws CDIException {
		for (int i = 0; i < variables.length; i++) {
			removeElement(variables[i]);
		}
	}

	ICDIVariable createVariable(StackFrame stack, String name) throws CDIException {
		Element element = createElement(stack, name);
		Variable var;
		if (element.variable != null) {
			var = element.variable;
		} else {
			var = new Variable(stack, name, element.miVar);
			element.variable = var;
			addElement(element);
		}
		return var;
	}

	Variable createVariable(StackFrame stack, String name, MIVar miVar )
		throws CDIException {
		Element element = new Element();
		element.miVar = miVar;
		element.name = name;
		element.stackframe = stack;
		Variable var = new Variable(stack, name, miVar);
		element.variable = var;
		addElement(element);
		return var;
	}


	ICDIArgument createArgument(StackFrame stack, String name) throws CDIException {
		Element element = createElement(stack, name);
		Argument carg;
		if (element.variable != null && element.variable instanceof Argument) { 
			carg = (Argument)element.variable;
		} else {
			carg = new Argument(stack, name,element.miVar);
			element.variable = carg;
			addElement(element);
		}
		return carg;
	}

	ICDIExpression createExpression(StackFrame stack, String name) throws CDIException {
		Element element = createElement(stack, name);
		Expression cexp;
		if (element.variable != null && element.variable instanceof Expression) {
			cexp = (Expression)element.variable;
		} else {
			cexp = new Expression(stack, name, element.miVar);
			element.variable = cexp;
			addElement(element);
		}
		return cexp;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#createExpression(String)
	 */
	public ICDIExpression createExpression(String name) throws CDIException {
		CTarget target = getCSession().getCTarget();
		StackFrame frame = ((CThread)target.getCurrentThread()).getCurrentStackFrame();
		return createExpression(frame, name);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#getExpressions()
	 */
	public ICDIExpression[] getExpressions() throws CDIException {
		Element[] elements = getElements();
		List aList = new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].variable instanceof ICDIExpression) {
				aList.add(elements[i].variable);
			}
		}
		return (ICDIExpression[])aList.toArray(new ICDIExpression[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#removeExpression(ICDIExpression)
	 */
	public void removeExpression(ICDIExpression expression)
		throws CDIException {
		if (expression instanceof Variable) {
			removeElement((Variable)expression);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#removeExpressions(ICDIExpression[])
	 */
	public void removeExpressions(ICDIExpression[] expressions)
		throws CDIException {
		for (int i = 0; i < expressions.length; i++) {
			removeExpression(expressions[i]);
		}
	}

}
