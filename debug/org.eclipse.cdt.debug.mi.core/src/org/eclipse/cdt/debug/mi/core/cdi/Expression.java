package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarAssign;
import org.eclipse.cdt.debug.mi.core.command.MIVarEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.output.MIChild;
import org.eclipse.cdt.debug.mi.core.output.MIVarEvaluateExpressionInfo;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Expression extends CObject implements ICDIExpression {

	MIChild child;
	String exp;

	public Expression(CTarget target, String e, MIChild c) {
		super(target);
		exp = e;
		child = c;
	}

	String getVarName() {
		return child.getName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getName()
	 */
	public String getName() throws CDIException {
		return exp;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		return child.getType();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getValue()
	 */
	public ICDIValue getValue() throws CDIException {
		Value cvalue;
		MISession mi = getCTarget().getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarEvaluateExpression var = factory.createMIVarEvaluateExpression(getVarName());
		try {
			mi.postCommand(var);
			MIVarEvaluateExpressionInfo info = var.getMIVarEvaluateExpressionInfo();
			String value = info.getValue();
			cvalue = new Value(getCTarget(), value);
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
                return cvalue;
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
		MISession mi = getCTarget().getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarAssign var = factory.createMIVarAssign(getVarName(), expression);
		try {
			mi.postCommand(var);
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
	}

}
