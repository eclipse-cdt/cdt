/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

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
			thisVal = (long) this.addr;
		}
		return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Type:").append(type).append("\n");
		buf.append("Name: ").append(name).append("\n");
		buf.append("\taddress:").append("0x").append(Long.toHexString(addr)).append("\n");
		buf.append("\tstartLine:").append(startLine).append("\n");
		buf.append("\tendLine:").append(endLine).append("\n");
		buf.append("\tSize:").append(size).append("\n");
		return buf.toString();
	}
}
