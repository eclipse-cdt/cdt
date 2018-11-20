/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.qt.core.index.IQProperty;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class QtPropertyName extends AbstractQObjectMemberName implements IQtASTName {

	private String type;
	// TODO The PDOM attrs should only be created in #createPDOMBinding
	private List<QtPDOMProperty.Attribute> attributes = new ArrayList<>();

	public QtPropertyName(QObjectName qobjName, IASTName ast, String name, QtASTImageLocation location) {
		super(qobjName, ast, name, location);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * This permits storage of duplicate attributes, a Codan checker should flag this as an error, but
	 * while the invalid code exists, the references should continue to be properly resolved.
	 */
	public void addAttribute(IQProperty.Attribute attr, String value) {
		attributes.add(new QtPDOMProperty.Attribute(attr, value));
	}

	/**
	 * This permits storage of duplicate attributes, a Codan checker should flag this as an error, but
	 * while the invalid code exists, the references should continue to be properly resolved.
	 */
	public void addAttribute(IQProperty.Attribute attr, String value, IBinding cppBinding) {
		PDOMBinding pdomBinding = cppBinding == null ? null : (PDOMBinding) cppBinding.getAdapter(PDOMBinding.class);
		attributes.add(new QtPDOMProperty.Attribute(attr, value, pdomBinding));
	}

	@Override
	public QtPDOMBinding createPDOMBinding(QtPDOMLinkage linkage) throws CoreException {
		QtPDOMProperty pdom = new QtPDOMProperty(linkage, getOwner(linkage), this);
		pdom.setAttributes(attributes.toArray(new QtPDOMProperty.Attribute[attributes.size()]));
		return pdom;
	}
}
