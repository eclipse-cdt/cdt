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
	CThread cthread;

	public StackFrame(CThread thread, MIFrame f) {
		super(thread.getCTarget());
		cthread = thread;
		frame = f;
	}

	MIFrame getMIFrame() {
		return frame;
	}

	CThread getCThread() {
		return cthread;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getArguments()
	 */
	public ICDIArgument[] getArguments() throws CDIException {
		ICDIArgument[] cdiArgs = null;
		if (frame != null) {
			CSession session = getCTarget().getCSession();
			VariableManager mgr = (VariableManager)session.getVariableManager();
			mgr.update();
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			int level = frame.getLevel();
			MIStackListArguments listArgs =
				factory.createMIStackListArguments(false, level, level);
			try {
				MIArg[] args = null;
				mi.postCommand(listArgs);
				MIStackListArgumentsInfo info =
					listArgs.getMIStackListArgumentsInfo();
				if (info == null) {
					throw new CDIException("No answer");
				}
				MIFrame[] miFrames = info.getMIFrames();
				if (miFrames != null && miFrames.length == 1) {
					args = miFrames[0].getArgs();
				}
				if (args != null) {
					cdiArgs = new ICDIArgument[args.length];
					for (int i = 0; i < cdiArgs.length; i++) {
						cdiArgs[i] =
							mgr.createArgument(this, args[i].getName());
					}
				} else {
					cdiArgs = new ICDIArgument[0];
				}
			} catch (MIException e) {
				throw new CDIException(e.toString());
			}
		}
		return cdiArgs;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocalVariables()
	 */
	public ICDIVariable[] getLocalVariables() throws CDIException {
		CSession session = getCTarget().getCSession();
		VariableManager mgr = (VariableManager)session.getVariableManager();
		mgr.update();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIStackListLocals locals = factory.createMIStackListLocals(false);
		try {
			MIArg[] args = null;
			ICDIVariable[] variables = null;
			mi.postCommand(locals);
			MIStackListLocalsInfo info = locals.getMIStackListLocalsInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			args = info.getLocals();
			if (args != null) {
				variables = new ICDIVariable[args.length];
				for (int i = 0; i < variables.length; i++) {
					variables[i] = mgr.createVariable(this, args[i].getName());
				}
			} else {
				variables = new ICDIVariable[0];
			}
			return variables;
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
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
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#equals(ICDIStackFrame)
	 */
	public boolean equals(ICDIStackFrame stackframe) {
		if (stackframe instanceof StackFrame) {
			StackFrame stack = (StackFrame)stackframe;
			return  cthread != null &&
				cthread.equals(stack.getCThread()) &&
				frame != null &&
				frame.getLevel() == stack.getMIFrame().getLevel() &&
				frame.getFile().equals(stack.getMIFrame().getFile()) &&
				frame.getFunction().equals(stack.getMIFrame().getFunction());
		}
		return super.equals(stackframe);
	}

}
