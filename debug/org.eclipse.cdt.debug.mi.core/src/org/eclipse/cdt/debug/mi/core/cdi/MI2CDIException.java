package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.mi.core.MIException;

/**
 */
public class MI2CDIException extends CDIException {
	
	public MI2CDIException(MIException e) {
		super(e.getMessage(), e.getLogMessage());
	}

}
