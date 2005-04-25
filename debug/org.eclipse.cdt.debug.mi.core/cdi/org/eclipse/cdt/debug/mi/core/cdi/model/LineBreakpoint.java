/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;

public class LineBreakpoint extends LocationBreakpoint implements ICDILineBreakpoint {

	public LineBreakpoint(Target target, int kind, ICDILineLocation loc, ICDICondition cond) {
		super(target, kind, loc, cond);
	}

}
