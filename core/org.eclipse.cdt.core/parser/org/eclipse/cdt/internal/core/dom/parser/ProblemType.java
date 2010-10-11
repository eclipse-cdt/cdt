/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.parser.ParserMessages;


/**
 * Implementation of problem types.
 */
public class ProblemType implements IProblemType {
	private final int fID;

	public ProblemType(int id) {
		fID= id;
	}
	
	public int getID() {
		return fID;
	}

	public String getMessage() {
		return ParserMessages.getProblemPattern(this);
	}

	public boolean isSameType(IType type) {
		return type == this;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
