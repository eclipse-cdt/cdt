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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPFieldTemplatePartialSpecialization extends PDOMCPPVariableTemplatePartialSpecialization
		implements ICPPField {

	public PDOMCPPFieldTemplatePartialSpecialization(PDOMCPPLinkage linkage, PDOMNode parent,
			ICPPVariableTemplatePartialSpecialization parSpec, PDOMCPPFieldTemplate pdomPrimary)
			throws CoreException, DOMException {
		super(linkage, parent, parSpec, pdomPrimary);
	}

	public PDOMCPPFieldTemplatePartialSpecialization(PDOMLinkage pdomLinkage, long record) {
		super(pdomLinkage, record);
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FIELD_TEMPLATE_PARTIAL_SPECIALIZATION;
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

	@Override
	public ICPPTemplateDefinition getPrimaryTemplate() {
		try {
			return new PDOMCPPFieldTemplate(getLinkage(), getPrimaryTemplateRec());
		} catch (CoreException e) {
			CCorePlugin.log("Failed to load primary template for " + getName(), e); //$NON-NLS-1$
			return null;
		}
	}

	@Override
	public PDOMCPPVariableTemplatePartialSpecialization getNextPartial() throws CoreException {
		long rec = getNextPartialRec();
		if (rec == 0)
			return null;
		return new PDOMCPPFieldTemplatePartialSpecialization(getLinkage(), rec);
	}
}
