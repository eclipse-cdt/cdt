/*******************************************************************************
 * Copyright (c) 2012, 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;

public class CPPASTAliasDeclaration extends CPPASTAttributeOwner implements ICPPASTAliasDeclaration {
	private IASTName aliasName;
	private ICPPASTTypeId mappingTypeId;

	public CPPASTAliasDeclaration(IASTName aliasName, ICPPASTTypeId mappingTypeId) {
		setAlias(aliasName);
		setMappingTypeId(mappingTypeId);
	}

	@Override
	public int getRoleForName(IASTName name) {
		if (aliasName == name)
			return r_definition;
		if (mappingTypeId == name)
			return r_reference;
		return r_unclear;
	}

	@Override
	public IASTName getAlias() {
		return aliasName;
	}

	@Override
	public void setAlias(IASTName aliasName) {
		assertNotFrozen();
		this.aliasName = aliasName;
		if (aliasName != null) {
			aliasName.setParent(this);
			aliasName.setPropertyInParent(ALIAS_NAME);
		}
	}

	@Override
	public ICPPASTTypeId getMappingTypeId() {
		return mappingTypeId;
	}

	@Override
	public void setMappingTypeId(ICPPASTTypeId mappingTypeId) {
		assertNotFrozen();
		this.mappingTypeId = mappingTypeId;
		if (mappingTypeId != null) {
			mappingTypeId.setParent(this);
			mappingTypeId.setPropertyInParent(TARGET_TYPEID);
		}
	}

	@Override
	public ICPPASTAliasDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public ICPPASTAliasDeclaration copy(CopyStyle style) {
		CPPASTAliasDeclaration copy = new CPPASTAliasDeclaration(aliasName == null ? null : aliasName.copy(style),
				mappingTypeId == null ? null : mappingTypeId.copy(style));
		return copy(copy, style);
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (aliasName != null && !aliasName.accept(action))
			return false;
		if (!acceptByAttributeSpecifiers(action))
			return false;
		if (mappingTypeId != null && !mappingTypeId.accept(action))
			return false;

		if (action.shouldVisitDeclarations) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}
}
