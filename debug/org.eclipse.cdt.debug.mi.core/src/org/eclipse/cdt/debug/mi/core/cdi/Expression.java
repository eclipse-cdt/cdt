package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICObject;
import org.eclipse.cdt.debug.core.cdi.model.ICTarget;
import org.eclipse.cdt.debug.core.cdi.model.ICValue;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Expression implements ICExpression {

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICExpression#getValue()
	 */
	public ICValue getValue() throws CDIException {
		return null;
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
