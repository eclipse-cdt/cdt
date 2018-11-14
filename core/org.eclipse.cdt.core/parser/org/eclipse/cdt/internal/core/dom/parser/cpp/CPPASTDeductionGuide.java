/*******************************************************************************
 * Copyright (c) 2018, Institute for Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Felix Morgner - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeductionGuide;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public class CPPASTDeductionGuide extends ASTNode implements ICPPASTDeductionGuide, IASTAmbiguityParent {

	private IASTParameterDeclaration[] parameters;
	private IASTName templateName;
	private ICPPASTTemplateId templateId;
	private boolean takesVarArgs;
	private boolean isExplicit;

	@Override
	public boolean accept(ASTVisitor visitor) {
		if (visitor.shouldVisitDeclarations) {
			switch (visitor.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (!templateName.accept(visitor)) {
			return false;
		}

		if (parameters != null) {
			for (IASTParameterDeclaration parameter : parameters) {
				if (parameter != null && !parameter.accept(visitor)) {
					return false;
				}
			}
		}

		if (!templateId.accept(visitor)) {
			return false;
		}

		return true;
	}

	@Override
	public void addParameterDeclaration(IASTParameterDeclaration parameter) {
		assertNotFrozen();
		assert (parameter != null);
		parameter.setParent(this);
		parameter.setPropertyInParent(PARAMETER);
		parameters = ArrayUtil.append(IASTParameterDeclaration.class, parameters, parameter);
	}

	@Override
	public IASTParameterDeclaration[] getParameters() {
		if (parameters == null) {
			return ICPPASTParameterDeclaration.EMPTY_CPPPARAMETERDECLARATION_ARRAY;
		}
		return ArrayUtil.trim(parameters);
	}

	@Override
	public void setVarArgs(boolean value) {
		assertNotFrozen();
		takesVarArgs = value;
	}

	@Override
	public boolean takesVarArgs() {
		return takesVarArgs;
	}

	@Override
	public boolean isExplicit() {
		return isExplicit;
	}

	@Override
	public void setExplicit(boolean value) {
		assertNotFrozen();
		isExplicit = value;
	}

	@Override
	public IASTName getTemplateName() {
		return templateName;
	}

	@Override
	public void setTemplateName(IASTName name) {
		assertNotFrozen();
		assert (name != null);
		name.setParent(this);
		name.setPropertyInParent(TEMPLATE_NAME);
		templateName = name;
	}

	@Override
	public ICPPASTTemplateId getSimpleTemplateId() {
		return templateId;
	}

	@Override
	public void setSimpleTemplateId(ICPPASTTemplateId id) {
		assertNotFrozen();
		assert (id != null);
		id.setParent(this);
		id.setPropertyInParent(TEMPLATE_ID);
		templateId = id;
	}

	@Override
	public ICPPASTDeductionGuide copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public ICPPASTDeductionGuide copy(CopyStyle style) {
		CPPASTDeductionGuide copy = new CPPASTDeductionGuide();
		copy.isExplicit = isExplicit;
		copy.takesVarArgs = takesVarArgs;
		copy.setTemplateName(templateName.copy(style));
		copy.setSimpleTemplateId(templateId.copy(style));
		if (parameters != null) {
			for (IASTParameterDeclaration parameter : parameters) {
				if (parameter != null) {
					copy.addParameterDeclaration(parameter.copy(style));
				}
			}
		}
		return super.copy(copy, style);
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		int indexOfChild = ArrayUtil.indexOfEqual(parameters, child);
		if (indexOfChild > -1) {
			other.setParent(this);
			other.setPropertyInParent(PARAMETER);
			child.setParent(null);
			parameters[indexOfChild] = (IASTParameterDeclaration) other;
		}
	}
}
