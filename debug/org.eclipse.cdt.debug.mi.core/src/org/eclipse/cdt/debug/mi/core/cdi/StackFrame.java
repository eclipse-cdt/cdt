package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIStackListLocals;
import org.eclipse.cdt.debug.mi.core.output.MIArg;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIStackListLocalsInfo;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class StackFrame implements ICDIStackFrame {

	CSession session;
	MIFrame frame;

	public StackFrame(CSession s, MIFrame f) {
		session = s;
		frame = f;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getArguments()
	 */
	public ICDIArgument[] getArguments() throws CDIException {
		MIArg[] args = frame.getArgs();
		ICDIArgument[] cargs = new ICDIArgument[args.length];
		for (int i = 0; i < cargs.length; i++) {
			cargs[i] = new Argument(args[i]);
		}
		return cargs;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocalVariables()
	 */
	public ICDIVariable[] getLocalVariables() throws CDIException {
		MIArg[] args = null;
		ICDIVariable[] variables = null;
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIStackListLocals locals = factory.createMIStackListLocals(true);
		try {
			mi.postCommand(locals);
			MIStackListLocalsInfo info = locals.getMIStackListLocalsInfo();
			if (info == null) {
				// throw new CDIException();
			}
			args = info.getLocals();
			
		} catch (MIException e) {
			//throw new CDIException(e);
		}
		if (args != null) {
			variables = new ICDIVariable[args.length];
			for (int i = 0; i < variables.length; i++) {
				variables[i] = new Variable(args[i]);
			}
		} else {
			variables = new ICDIVariable[0];
		}
		return variables;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocation()
	 */
	public ICDILocation getLocation() {
		return new Location(frame.getFile(), frame.getFunction(),
				frame.getLine(), frame.getAddress());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getCDITarget()
	 */
	public ICDITarget getCDITarget() {
		return session.getCTarget();
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
