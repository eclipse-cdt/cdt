/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.Format;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarAssign;
import org.eclipse.cdt.debug.mi.core.command.MIVarSetFormat;
import org.eclipse.cdt.debug.mi.core.command.MIVarShowAttributes;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarShowAttributesInfo;

/**
 */
public class Variable extends CObject implements ICDIVariable {

	MIVar miVar;
	Value value;
	VariableObject varObj;

	public Variable(VariableObject obj, MIVar v) {
		super(obj.getTarget());
		miVar = v;
		varObj = obj;
	}

	public MIVar getMIVar() {
		return miVar;
	}

	public VariableObject getVariableObject() {
		return varObj;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getName()
	 */
	public String getName() throws CDIException {
		return varObj.getName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		return miVar.getType();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getValue()
	 */
	public ICDIValue getValue() throws CDIException {
		if (value == null) {
			value = new Value(this);
		}
		return value;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(ICDIValue)
	 */
	public void setValue(ICDIValue value) throws CDIException {
		setValue(value.getValueString());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(String)
	 */
	public void setValue(String expression) throws CDIException {
		MISession mi = ((Session)(getTarget().getSession())).getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarAssign var = factory.createMIVarAssign(miVar.getVarName(), expression);
		try {
			mi.postCommand(var);
			MIInfo info = var.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		
		// If the assign was succesfull fire a MIVarChangedEvent()
		MIVarChangedEvent change = new MIVarChangedEvent(var.getToken(), miVar.getVarName(), true);
		mi.fireEvent(change);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#isEditable()
	 */
	public boolean isEditable() throws CDIException {
		MISession mi = ((Session)(getTarget().getSession())).getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarShowAttributes var = factory.createMIVarShowAttributes(miVar.getVarName());
		try {
			mi.postCommand(var);
			MIVarShowAttributesInfo info = var.getMIVarShowAttributesInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			return info.isEditable();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setFormat()
	 */
	public void setFormat(int format) throws CDIException {
		int fmt = Format.toMIFormat(format);
		MISession mi = ((Session)(getTarget().getSession())).getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarSetFormat var = factory.createMIVarSetFormat(miVar.getVarName(), fmt);
		try {
			mi.postCommand(var);
			MIInfo info = var.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#equals()
	 */
	public boolean equals(ICDIVariable var) {
		if (var instanceof Variable) {
			Variable variable = (Variable)var;
			return miVar.getVarName().equals(variable.getMIVar().getVarName());
		}
		return super.equals(var);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getStackFrame()
	 */
	public ICDIStackFrame getStackFrame() throws CDIException {
		return varObj.getStackFrame();
	}

}
