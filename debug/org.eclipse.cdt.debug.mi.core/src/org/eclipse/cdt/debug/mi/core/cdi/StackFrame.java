package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIStackListArguments;
import org.eclipse.cdt.debug.mi.core.command.MIStackListLocals;
import org.eclipse.cdt.debug.mi.core.output.MIArg;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIStackListArgumentsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIStackListLocalsInfo;

/**
 */
public class StackFrame extends CObject implements ICDIStackFrame {

	MIFrame frame;

	public StackFrame(CTarget target, MIFrame f) {
		super(target);
		frame = f;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getArguments()
	 */
	public ICDIArgument[] getArguments() throws CDIException {
		MIArg[] args = null;
		ICDIArgument[] cdiArgs = null;
		if (frame != null) {
			MISession mi = getCTarget().getCSession().getMISession();
			CommandFactory factory = mi.getCommandFactory();
			int level = frame.getLevel();
			MIStackListArguments listArgs =
				factory.createMIStackListArguments(true, level, level);
			try {
				mi.postCommand(listArgs);
				MIStackListArgumentsInfo info = listArgs.getMIStackListArgumentsInfo();
				MIFrame[] miFrames = info.getMIFrames();
				if (miFrames != null && miFrames.length == 1) {
					args = miFrames[0].getArgs();
				}
			} catch (MIException e) {
				//throw new CDIException(e);
			}
		}
		if (args != null) {
			cdiArgs = new ICDIArgument[args.length];
			for (int i = 0; i < cdiArgs.length; i++) {
				cdiArgs[i] = new Argument(getCTarget(), args[i]);
			}
		} else {
			cdiArgs = new ICDIArgument[0];
		}
		return cdiArgs;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocalVariables()
	 */
	public ICDIVariable[] getLocalVariables() throws CDIException {
		MIArg[] args = null;
		ICDIVariable[] variables = null;
		MISession mi = getCTarget().getCSession().getMISession();
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
				variables[i] = new Variable(getCTarget(), args[i]);
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
		if (frame != null) {
			return new Location(frame.getFile(), frame.getFunction(),
					frame.getLine(), frame.getAddress());
		}
		return new Location("", "", 0, 0);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLevel()
	 */
	public int getLevel() {
		if (frame != null) {
			return frame.getLevel();
		}
		return 0;
	}
}
