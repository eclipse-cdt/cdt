package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Value implements ICDIValue {

	String val = "";

	public Value(String s) {
		val = s;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		return "";
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getValueString()
	 */
	public String getValueString() throws CDIException {
		return val;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getVariables()
	 */
	public ICDIVariable[] getVariables() throws CDIException {
		return new ICDIVariable[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getCDITarget()
	 */
	public ICDITarget getCDITarget() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getId()
	 */
	public String getId() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getParent()
	 */
	public ICDIObject getParent() {
		return null;
	}
}
