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
package org.eclipse.cdt.make.internal.core.makefile.gnu;



public class OverrideDefine extends DefineVariable {


	public OverrideDefine(String name, StringBuffer value) {
		super(name, value);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("override define ");
		sb.append(getName()).append('\n');
		sb.append(getValue());
		sb.append("endef");
		return sb.toString();
	}

	public boolean isOverride() {
		return true;
	}

}
