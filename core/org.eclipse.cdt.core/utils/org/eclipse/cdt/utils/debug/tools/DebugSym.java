/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.debug.tools;

public class DebugSym implements Comparable<Object> {

	public long addr;
	public long size;
	public int startLine;
	public int endLine;
	public String name;
	public String type;
	public String filename;

	public DebugSym() {
	}

	@Override
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

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Type:").append(type).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("Name: ").append(name).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\taddress:").append("0x").append(Long.toHexString(addr)).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\tstartLine:").append(startLine).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\tendLine:").append(endLine).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\tSize:").append(size).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}
