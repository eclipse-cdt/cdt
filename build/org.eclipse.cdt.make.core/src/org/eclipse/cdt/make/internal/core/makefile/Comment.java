/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

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

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(POUND_STRING).append(comment).append('\n');
		return buffer.toString();
	}

	public boolean equals(Comment cmt) {
		return cmt.toString().equals(toString());
	}
}
