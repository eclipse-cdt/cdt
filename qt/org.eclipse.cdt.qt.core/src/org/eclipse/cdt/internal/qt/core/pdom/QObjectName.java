/*
 * Copyright (c) 2013, 2014 QNX Software Systems and others.
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.qt.core.ASTUtil;
import org.eclipse.core.runtime.CoreException;

/**
 * QObjects are C++ classes that have been annotated with Qt marker macros.  This class is
 * used to introduce the QObject to the Qt linkage.
 */
public class QObjectName extends AbstractQClassName {

	private final char[] fullyQualifiedName;
	private final List<QtPropertyName> properties = new ArrayList<>();
	private final Map<String, String> classInfos = new LinkedHashMap<>();

	public QObjectName(ICPPASTCompositeTypeSpecifier spec) {
		super(spec);

		String fqn = ASTUtil.getFullyQualifiedName(spec.getName());
		fullyQualifiedName = fqn == null ? new char[0] : fqn.toCharArray();
	}

	@Override
	public char[] getSimpleID() {
		// The Qt linkage uses the full qualified name when storing QObjects into the index.
		return fullyQualifiedName;
	}

	public List<QtPropertyName> getProperties() {
		return properties;
	}

	public void addProperty(QtPropertyName property) {
		properties.add(property);
	}

	public Map<String, String> getClassInfos() {
		return classInfos;
	}

	public String addClassInfo(String key, String value) {
		return classInfos.put(key, value);
	}

	@Override
	protected QtPDOMBinding createPDOMBinding(QtPDOMLinkage linkage, IASTName name) throws CoreException {
		return new QtPDOMQObject(linkage, this, name);
	}

	@Override
	protected IASTName copy(CopyStyle style, ICPPASTCompositeTypeSpecifier spec) {
		return new QObjectName(spec);
	}
}
