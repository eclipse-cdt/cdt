package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICLocation;
import org.eclipse.cdt.debug.core.cdi.model.ICArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICObject;
import org.eclipse.cdt.debug.core.cdi.model.ICStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICTarget;
import org.eclipse.cdt.debug.core.cdi.model.ICVariable;
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
public class StackFrame implements ICStackFrame {

	CSession session;
	MIFrame frame;

	public StackFrame(CSession s, MIFrame f) {
		session = s;
		frame = f;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICStackFrame#getArguments()
	 */
	public ICArgument[] getArguments() throws CDIException {
		MIArg[] args = frame.getArgs();
		ICArgument[] cargs = new ICArgument[args.length];
		for (int i = 0; i < cargs.length; i++) {
			cargs[i] = new Argument(args[i]);
		}
		return cargs;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICStackFrame#getLocalVariables()
	 */
	public ICVariable[] getLocalVariables() throws CDIException {
		MIArg[] args = null;
		ICVariable[] variables = null;
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
			variables = new ICVariable[args.length];
			for (int i = 0; i < variables.length; i++) {
				variables[i] = new Variable(args[i]);
			}
		} else {
			variables = new ICVariable[0];
		}
		return variables;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICStackFrame#getLocation()
	 */
	public ICLocation getLocation() {
		return new Location(frame.getFile(), frame.getFunction(),
				frame.getLine(), frame.getAddress());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICObject#getCDITarget()
	 */
	public ICTarget getCDITarget() {
		return session.getCTarget();
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
