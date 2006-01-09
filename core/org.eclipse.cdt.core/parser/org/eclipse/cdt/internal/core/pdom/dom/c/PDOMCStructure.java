/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCStructure extends PDOMMemberOwner implements ICompositeType {

	public PDOMCStructure(PDOMDatabase pdom, PDOMNode parent, IASTName name) throws CoreException {
		super(pdom, parent, name, PDOMCLinkage.CSTRUCTURE);
	}

	public PDOMCStructure(PDOMDatabase pdom, int record) {
		super(pdom, record);
	}

	public Object clone() {
		throw new PDOMNotImplementedError();
	}
	
	public int getKey() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IField[] getFields() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IField findField(String name) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IScope getCompositeScope() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isSameType(IType type) {
		throw new PDOMNotImplementedError();
	}

}
