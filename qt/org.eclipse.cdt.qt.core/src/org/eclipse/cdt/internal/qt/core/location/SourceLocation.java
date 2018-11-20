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
package org.eclipse.cdt.internal.qt.core.location;

import org.eclipse.cdt.qt.core.location.IPosition;
import org.eclipse.cdt.qt.core.location.ISourceLocation;

public class SourceLocation implements ISourceLocation {
	private String source;
	private IPosition start;
	private IPosition end;

	public SourceLocation() {
		this(null, null, null);
	}

	public SourceLocation(String source, IPosition start, IPosition end) {
		this.source = source;
		this.start = start;
		this.end = end;
	}

	public void setSource(String value) {
		this.source = value;
	}

	@Override
	public String getSource() {
		return source;
	}

	public void setStart(IPosition value) {
		this.start = value;
	}

	@Override
	public IPosition getStart() {
		return start;
	}

	public void setEnd(IPosition value) {
		this.end = value;
	}

	@Override
	public IPosition getEnd() {
		return end;
	}

}
