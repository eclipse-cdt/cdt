package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.command.MIVarListChildren;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarEvaluateExpressionInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarListChildrenInfo;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Value extends CObject implements ICDIValue {

	Variable variable;

	public Value(Variable v) {
		super(v.getCTarget());
		variable = v;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		return variable.getTypeName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getValueString()
	 */
	public String getValueString() throws CDIException {
		String result = "";
		StackFrame stack = variable.getStackFrame();
		stack.getCThread().setCurrentStackFrame(stack);
		MISession mi = getCTarget().getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarEvaluateExpression var =
			factory.createMIVarEvaluateExpression(variable.getMIVar().getVarName());
		try {
			mi.postCommand(var);
			MIVarEvaluateExpressionInfo info = var.getMIVarEvaluateExpressionInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			result = info.getValue();
		} catch (MIException e) {
			//throw new CDIException(e.toString());
		}
		return result;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIValue#getVariables()
	 */
	public ICDIVariable[] getVariables() throws CDIException {
		StackFrame stack = variable.getStackFrame();
		stack.getCThread().setCurrentStackFrame(stack);
		Variable[] variables = null;
		MISession mi = getCTarget().getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarListChildren var = 
			factory.createMIVarListChildren(variable.getMIVar().getVarName());
		try {
			mi.postCommand(var);
			MIVarListChildrenInfo info = var.getMIVarListChildrenInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MIVar[] vars = info.getMIVars();
			variables = new Variable[vars.length];
			for (int i = 0; i < vars.length; i++) {
				variables[i] =
					new Variable(variable.getStackFrame(), vars[i].getExp(), vars[i]);
			}
		} catch (MIException e) {
			//throw new CDIException(e.toString());
		}
		return variables;
	}

}
