package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRunningEvent;

/**
 */
public class ResumedEvent implements ICDIResumedEvent {

	CSession session;
	MIRunningEvent event;

	public ResumedEvent(CSession s, MIRunningEvent e) {
		session = s;
		event = e;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return session.getCurrentTarget();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent#getType()
	 */
	public int getType() {
		MIRunningEvent running = (MIRunningEvent)event;
		int type = running.getType();
		int cdiType = 0;
		switch (type) {
			case MIRunningEvent.CONTINUE:
				cdiType = ICDIResumedEvent.CONTINUE;
			break;

			case MIRunningEvent.UNTIL:
			case MIRunningEvent.NEXT:
				cdiType = ICDIResumedEvent.STEP_OVER;
			break;

			case MIRunningEvent.NEXTI:
				cdiType = ICDIResumedEvent.STEP_OVER_INSTRUCTION;
			break;

			case MIRunningEvent.STEP:
				cdiType = ICDIResumedEvent.STEP_INTO;
			break;

			case MIRunningEvent.STEPI:
				cdiType = ICDIResumedEvent.STEP_INTO_INSTRUCTION;
			break;

			case MIRunningEvent.FINISH:
				cdiType = ICDIResumedEvent.STEP_RETURN;
			break;

			//MIRunningEvent.UNTIL:
			//cdiType = ICDIResumedEvent.STEP_UNTIL;
			//break;
		}
		return cdiType;
	}

}
