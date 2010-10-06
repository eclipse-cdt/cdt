/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

/**
 * Represents the possible cv-qualification of a type.
 */
public enum CVQualifier {
	cv(true, true), c(true, false), v(false, true), _(false, false);

	final private boolean fConst;
	final private boolean fVolatile;
	
	private CVQualifier(boolean c, boolean v) {
		fConst= c;
		fVolatile= v;
	}
	
	public boolean isConst() {
		return fConst;
	}
	public boolean isVolatile() {
		return fVolatile;
	}

	public boolean isAtLeastAsQualifiedAs(CVQualifier other) {
		return other == _ || this == other || this == cv;
	}

	public boolean isMoreQualifiedThan(CVQualifier other) {
		return this != other && (other == _ || this == cv);
	}

	public CVQualifier remove(CVQualifier arg) {
		if (this == arg)
			return _;
		
		switch (arg) {
		case _:
			return this;
		case c:
			return isVolatile() ? v : _;
		case v:
			return isConst() ? c : _;
		case cv:
			return _;
		}
		return _;
	}
}
