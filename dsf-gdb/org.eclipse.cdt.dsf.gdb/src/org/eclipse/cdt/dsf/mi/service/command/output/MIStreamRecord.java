/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
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
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Mathias Kunter       - Modified to use the new MIStringHandler class (Bug 307311)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI stream record response.
 */
public abstract class MIStreamRecord extends MIOOBRecord {

	String cstring = ""; //$NON-NLS-1$

	public String getCString() {
		return cstring;
	}

	public void setCString(String str) {
		cstring = str;
	}

	public String getString() {
		// Return the translated C string without escaping any special characters.
		return MIStringHandler.translateCString(getCString(), false);
	}

	@Override
	public String toString() {
		if (this instanceof MIConsoleStreamOutput) {
			return "~\"" + cstring + "\"\n"; //$NON-NLS-1$//$NON-NLS-2$
		} else if (this instanceof MITargetStreamOutput) {
			return "@\"" + cstring + "\"\n"; //$NON-NLS-1$//$NON-NLS-2$
		} else if (this instanceof MILogStreamOutput) {
			return "&\"" + cstring + "\"\n"; //$NON-NLS-1$//$NON-NLS-2$
		} else {
			return "\"" + cstring + "\"\n"; //$NON-NLS-1$//$NON-NLS-2$
		}
	}
}
