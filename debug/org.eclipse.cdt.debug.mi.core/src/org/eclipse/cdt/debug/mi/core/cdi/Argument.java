package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.mi.core.output.MIArg;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Argument implements ICDIArgument {

	MIArg arg;

	public Argument(MIArg a) {
		arg = a;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getName()
	 */
	public String getName() throws CDIException {
		return arg.getName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getValue()
	 */
	public ICDIValue getValue() throws CDIException {
		return new Value(arg.getValue());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws CDIException {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(ICDIValue)
	 */
	public void setValue(ICDIValue value) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(String)
	 */
	public void setValue(String expression) throws CDIException {
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

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		return "";
	}
}
