/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.debug.tools;


public class DebugSym implements Comparable {

	public long addr;
	public long size;
	public int startLine;
	public int endLine;
	public String name;
	public String type;
	public String filename;

	public DebugSym() {
	}

	public int compareTo(Object obj) {
		long thisVal = 0;
		long anotherVal = 0;
		if (obj instanceof DebugSym) {
			DebugSym entry = (DebugSym) obj;
			thisVal = this.addr;
			anotherVal = entry.addr;
		} else if (obj instanceof Long) {
			Long val = (Long) obj;
			anotherVal = val.longValue();
			thisVal = this.addr;
		}
		return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Type:").append(type).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("Name: ").append(name).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\taddress:").append("0x").append(Long.toHexString(addr)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\tstartLine:").append(startLine).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\tendLine:").append(endLine).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\tSize:").append(size).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}
