/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.Format;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataListRegisterValues;
import org.eclipse.cdt.debug.mi.core.command.MIDataWriteRegisterValues;
import org.eclipse.cdt.debug.mi.core.command.MIWhatis;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterChangedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIDataListRegisterValuesInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIRegisterValue;
import org.eclipse.cdt.debug.mi.core.output.MIWhatisInfo;

/**
 */
public class Register extends CObject implements ICDIRegister, ICDIValue {

	RegisterObject regObject;
	int format = MIFormat.HEXADECIMAL;
	Register parent;
	String lastname;
	String typename = null;
	int childrenNumber = -1;
	ICDIVariable[] variables = null;
	
	/**
	 * A container class to hold the values of the registers and sub values
	 * for example on x86, xmm0 register is consider to be a struct
	 * gdb/mi -data-list-register-values returns the value like this
	 * value="{f = {0x0, 0x0, 0x0, 0x0}}"
 	 * we'll parse() it and change it to:
	 * Argument[0] = { "xmm0", "{f = {0x0, 0x1, 0x2, 0x3}}"}
	 * Argument[1] = { "xmm0.f", "{0x0, 0x1, 0x2, 0x3}"}
	 * Argument[2] = { "xmm0.f.0", "0x0"}
	 * Argument[3] = { "xmm0.f.1", "0x1"}
	 * Argument[4] = { "xmm0.f.2", "0x2"}
	 * Argument[5] = { "xmm0.f.3", "0x3"}
	 * see @parse()
	 */
	class Argument {
		String name;
		String val;
		Argument(String n, String v) {
			name = n;
			val = v;
		}
		String getKey() {
			return name;
		}
		String getValue() {
			return val;
		}
	}

	public Register(ICDITarget target, ICDIRegisterObject r) {
		super(target);
		parent = null;
		lastname = r.getName();
		regObject = (RegisterObject)r;
	}

	public Register(Register p, String n) {
		super(p.getTarget());
		parent = p;
		lastname = n;
	}

	/**
	 * return the MI regno.
	 */
	public int getID() {
		return regObject.getPosition();
	}

	/**
	 * Returns a Unique name separated by '.' to describe the path
	 * for example xmm0.f.0
	 */
	public String getUniqName() {
		String n = "";
		if (parent != null) {
			n = parent.getUniqName() + "." + getLastName();
		} else {
			n = getLastName();
		}
		return n;
	}
	
	/**
	 * Returns the name of the register, for example xmm0.
	 */ 
	public String getBaseName() {
		String base = "";
		if (parent != null) {
			base = parent.getBaseName();
		} else {
			base = getLastName();
		}
		return base;
	}

	/**
	 * Returns the current name for xmm0.f the last name is "f".
	 */
	public String getLastName() {
		return lastname;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getName()
	 */
	public String getName() throws CDIException {
		return getLastName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		if (typename == null) {
			Session session = (Session)getTarget().getSession();
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			String var = "$" + getUniqName();
			MIWhatis whatis = factory.createMIWhatis(var);
			try {
				mi.postCommand(whatis);
				MIWhatisInfo info = whatis.getMIWhatisInfo();
				if (info == null) {
					throw new CDIException("No answer");
				}
				typename = info.getType();
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		}
		return typename;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getValue()
	 */
	public ICDIValue getValue() throws CDIException {
		return this;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getValueString()
	 */
	public String getValueString() throws CDIException {
		if (parent == null) {
			Session session = (Session)getTarget().getSession();
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			int[] regno = new int[]{regObject.getPosition()};
			MIDataListRegisterValues registers =
				factory.createMIDataListRegisterValues(format, regno);
			try {
				mi.postCommand(registers);
				MIDataListRegisterValuesInfo info =
					registers.getMIDataListRegisterValuesInfo();
				if (info == null) {
					throw new CDIException("No answer");
				}
				MIRegisterValue[] regValues = info.getMIRegisterValues();
				// We only ask for one.  But do the right thing
				for (int i = 0; i < regValues.length; i++) {
					if (regValues[i].getNumber() == regno[i]) {
						return regValues[i].getValue();
					}
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		} else {
			String u = getUniqName();
			String v = parent.getValueString();
			Argument[] args = parse(parent.getUniqName(), v);
			for (int i = 0; i < args.length; i++) {
				if (u.equals(args[i].getKey())) {
					return args[i].getValue();
				}
			}
		}
		return "";
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#isEditable()
	 */
	public boolean isEditable() throws CDIException {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(ICDIValue)
	 */
	public void setValue(ICDIValue val) throws CDIException {
		setValue(val.getValueString());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(String)
	 */
	public void setValue(String expression) throws CDIException {
		Session session = (Session)getTarget().getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		int[] regnos = new int[]{regObject.getPosition()};
		String[] values = new String[]{expression};
		MIDataWriteRegisterValues registers =
				factory.createMIDataWriteRegisterValues(format, regnos, values);
		try {
			mi.postCommand(registers);
			MIInfo info = registers.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		// If the assign was succesfull fire a MIRegisterChangedEvent()
		MIRegisterChangedEvent change = new MIRegisterChangedEvent(registers.getToken(),
			regObject.getName(), regObject.getPosition());
		mi.fireEvent(change);

	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setFormat()
	 */
	public void setFormat(int fmt) throws CDIException {
		format = Format.toMIFormat(fmt);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getChildrenNumber()
	 */
	public int getChildrenNumber() throws CDIException {
		if (childrenNumber == -1) {
			childrenNumber = getVariables().length;
		}
		return childrenNumber;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getVariables()
	 */
	public ICDIVariable[] getVariables() throws CDIException {
		if (variables == null) {
			List aList = new ArrayList(1);
			String v = getValueString();
			Argument[] args = parse(getUniqName(), v);
			/* The idea here is to get the first level
			 * 
	 		 * Argument[0] = { "xmm0", "{f = {0x0, 0x1, 0x2, 0x3}}"
		 	 * Argument[1] = { "xmm0.f", "{0x0, 0x1, 0x2, 0x3}"}
		 	 * Argument[2] = { "xmm0.f.0", "0x0"}
	 		 * Argument[3] = { "xmm0.f.1", "0x1"}
		 	 * Argument[4] = { "xmm0.f.2", "0x2"}
		 	 * Argument[5] = { "xmm0.f.3", "0x3"}
			 *
			 * For example the children or xmm0.f are xmm0.f.{0,1,2,3,}
			 *  
			 */
			for (int i = 0; i < args.length; i++) {
				String n = args[i].getKey();
				String u = getUniqName();
				if (n.startsWith(u) && n.length() > u.length()) {
					String p = n.substring(u.length());
					StringTokenizer st = new StringTokenizer(p, ".");
					if (st.countTokens() == 1) {
						aList.add(new Register(this, (String)st.nextElement()));
					}
				}
			}
	 		variables = (ICDIVariable[])aList.toArray(new ICDIVariable[0]);
	 	}	
		return variables;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#hasChildren()
	 */
	public boolean hasChildren() throws CDIException {
		return getChildrenNumber() > 0;
	}

	/**
	 * We are parsing this:
	 * "{f = {0x0, 0x0, 0x0, 0x0}}"
	 * into:
	 * Argument[0] = { "xmm0", "{f = {0x0, 0x1, 0x2, 0x3}}"
	 * Argument[1] = { "xmm0.f", "{0x0, 0x1, 0x2, 0x3}"}
	 * Argument[2] = { "xmm0.f.0", "0x0"}
	 * Argument[3] = { "xmm0.f.1", "0x1"}
	 * Argument[4] = { "xmm0.f.2", "0x2"}
	 * Argument[5] = { "xmm0.f.3", "0x3"}
	 */
	Argument[] parse(String base, String v) throws CDIException {
		List aList = new ArrayList(1);
		StringBuffer sb = new StringBuffer(base);
		aList.add(new Argument(sb.toString(), v.trim()));
		while (v.startsWith("{")) {
			int idx;
			v = v.substring(1);
			if (v.endsWith("}")) {
				idx = v.lastIndexOf('}');
				v = v.substring(0, idx);
			}
			idx = v.indexOf('=');
			if (idx != -1) {
				String n = v.substring(0, idx).trim();
				sb.append('.').append(n);
				v = v.substring(idx + 1).trim();
				aList.add(new Argument(sb.toString(), v));
			} else {
				StringTokenizer st = new StringTokenizer(v, ",");
				for (int i = 0; st.hasMoreElements(); i++) {
					aList.add(new Argument(sb.toString() + "." + Integer.toString(i), ((String)st.nextElement()).trim()));
				}
			}
		}
		return (Argument[])aList.toArray(new Argument[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getStackFrame()
	 */
	public ICDIStackFrame getStackFrame() throws CDIException {
		return null;
	}

}
