package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIErrorInfo;
import org.eclipse.cdt.debug.mi.core.event.MIErrorEvent;

/**
 */
public class ErrorInfo extends SessionObject implements ICDIErrorInfo {

	MIErrorEvent event;

	public ErrorInfo(CSession session, MIErrorEvent e) {
		super(session);
		event = e;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIErrorInfo#getMessage()
	 */
	public String getMessage() {
		return event.getMessage();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIErrorInfo#getDetailMessage()
	 */
	public String getDetailMessage() {
		return event.getLogMessage();
	}

}
