package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIStackListFrames;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIStackListFramesInfo;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CThread extends CObject implements ICDIThread {

	String id = "";
	
	public CThread(CTarget target, String threadId) {
		super(target);
		id = threadId;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#finish()
	 */
	public void finish() throws CDIException {
		getTarget().finish();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getStackFrames()
	 */
	public ICDIStackFrame[] getStackFrames() throws CDIException {
		MISession mi = getCTarget().getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIStackListFrames frames = factory.createMIStackListFrames();
		try {
			mi.postCommand(frames);
			MIStackListFramesInfo info = frames.getMIStackListFramesInfo();
			if (info == null) {
				throw new CDIException("Timedout");
			}
			MIFrame[] miFrames = info.getMIFrames();
			ICDIStackFrame[] stack = new ICDIStackFrame[miFrames.length];
			for (int i = 0; i < stack.length; i++) {
				stack[i] = new StackFrame(getCTarget(), miFrames[i]);
			}
			return stack;
		} catch (MIException e) {
			throw new CDIException(e.toString());
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#isStepping()
	 */
	public boolean isStepping() {
		return getTarget().isStepping();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#isSuspended()
	 */
	public boolean isSuspended() {
		return getTarget().isSuspended();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#resume()
	 */
	public void resume() throws CDIException {
		getTarget().resume();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepInto()
	 */
	public void stepInto() throws CDIException {
		getTarget().stepInto();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepIntoInstruction()
	 */
	public void stepIntoInstruction() throws CDIException {
		getTarget().stepIntoInstruction();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepOver()
	 */
	public void stepOver() throws CDIException {
		getTarget().stepOver();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepOverInstruction()
	 */
	public void stepOverInstruction() throws CDIException {
		getTarget().stepOverInstruction();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#suspend()
	 */
	public void suspend() throws CDIException {
		getTarget().suspend();
	}

}
