/*******************************************************************************
 * Copyright (c) 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPFieldTemplate extends PDOMCPPVariableTemplate implements ICPPFieldTemplate {

	public PDOMCPPFieldTemplate(PDOMCPPLinkage linkage, PDOMNode parent, ICPPFieldTemplate template)
			throws CoreException, DOMException {
		super(linkage, parent, template);
	}

	public PDOMCPPFieldTemplate(PDOMLinkage pdomLinkage, long record) {
		super(pdomLinkage, record);
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FIELD_TEMPLATE;
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}

	@Override
	public int getVisibility() {
		return PDOMCPPAnnotation.getVisibility(getByte(record + PDOMCPPVariableTemplate.ANNOTATIONS));
	}

	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}
}
