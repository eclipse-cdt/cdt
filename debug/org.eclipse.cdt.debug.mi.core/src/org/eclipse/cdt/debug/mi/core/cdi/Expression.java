package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Expression implements ICDIExpression {

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#getValue()
	 */
	public ICDIValue getValue() throws CDIException {
		return null;
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
