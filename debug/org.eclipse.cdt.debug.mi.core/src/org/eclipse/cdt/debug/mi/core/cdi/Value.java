package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICObject;
import org.eclipse.cdt.debug.core.cdi.model.ICTarget;
import org.eclipse.cdt.debug.core.cdi.model.ICValue;
import org.eclipse.cdt.debug.core.cdi.model.ICVariable;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Value implements ICValue {

	String val = "";

	public Value(String s) {
		val = s;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICValue#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		return "";
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICValue#getValueString()
	 */
	public String getValueString() throws CDIException {
		return val;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICValue#getVariables()
	 */
	public ICVariable[] getVariables() throws CDIException {
		return new ICVariable[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICObject#getCDITarget()
	 */
	public ICTarget getCDITarget() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICObject#getId()
	 */
	public String getId() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICObject#getParent()
	 */
	public ICObject getParent() {
		return null;
	}
}
