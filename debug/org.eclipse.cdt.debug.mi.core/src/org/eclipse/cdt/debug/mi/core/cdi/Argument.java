package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICObject;
import org.eclipse.cdt.debug.core.cdi.model.ICTarget;
import org.eclipse.cdt.debug.core.cdi.model.ICValue;
import org.eclipse.cdt.debug.mi.core.output.MIArg;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Argument implements ICArgument {

	MIArg arg;

	public Argument(MIArg a) {
		arg = a;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICVariable#getName()
	 */
	public String getName() throws CDIException {
		return arg.getName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICVariable#getValue()
	 */
	public ICValue getValue() throws CDIException {
		return new Value(arg.getValue());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws CDIException {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICVariable#setValue(ICValue)
	 */
	public void setValue(ICValue value) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICVariable#setValue(String)
	 */
	public void setValue(String expression) throws CDIException {
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

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICVariable#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		return "";
	}
}
