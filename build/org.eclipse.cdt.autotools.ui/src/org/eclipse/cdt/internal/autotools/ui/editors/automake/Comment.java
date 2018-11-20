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
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.make.core.makefile.IComment;

public class Comment extends Directive implements IComment {

	String comment;

	public Comment(Directive parent, String cmt) {
		super(parent);
		if (cmt.startsWith(POUND_STRING)) {
			comment = cmt.substring(1);
		} else {
			comment = cmt;
		}
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(POUND_STRING).append(comment).append('\n');
		return buffer.toString();
	}

	@Override
	public boolean equals(Object cmt) {
		if (cmt instanceof Comment)
			return cmt.toString().equals(toString());
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
