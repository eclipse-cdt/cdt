/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.mi.core.output.MIVarChange;

public interface IUpdateListener {
	void changeList(MIVarChange[] changes);
}
