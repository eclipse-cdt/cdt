/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.qmldir;

import org.eclipse.cdt.internal.qt.core.location.SourceLocation;
import org.eclipse.cdt.qt.core.qmldir.IQDirASTNode;

public class QDirASTNode implements IQDirASTNode {

	private SourceLocation location;
	private int start;
	private int end;

	public QDirASTNode() {
		this.location = new SourceLocation();
		this.start = -1;
		this.end = -1;
	}

	public void setLocation(SourceLocation value) {
		this.location = value;
	}

	@Override
	public SourceLocation getLocation() {
		return location;
	}

	public void setStart(int value) {
		this.start = value;
	}

	@Override
	public int getStart() {
		return start;
	}

	public void setEnd(int value) {
		this.end = value;
	}

	@Override
	public int getEnd() {
		return end;
	}

}
