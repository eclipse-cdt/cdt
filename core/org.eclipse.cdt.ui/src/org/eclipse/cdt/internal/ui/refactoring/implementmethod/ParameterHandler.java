/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.PseudoNameGenerator;

/**
 * Manages and creates Method Parameter Infos.
 *
 * @author Lukas Felber
 *
 */
public class ParameterHandler {
	private boolean needsAditionalArgumentNames;
	private PseudoNameGenerator pseudoNameGenerator;
	private ArrayList<ParameterInfo> parameterInfos;
	private IASTSimpleDeclaration method;

	public ParameterHandler(IASTSimpleDeclaration method) {
		this.method = method;
		initArgumentNames();
	}

	public boolean needsAdditionalArgumentNames() {
		return needsAditionalArgumentNames;
	}

	public void initArgumentNames() {
		if (parameterInfos != null) {
			return;
		}
		needsAditionalArgumentNames = false;
		parameterInfos = new ArrayList<>();
		for (IASTParameterDeclaration actParam : getParametersFromMethodNode()) {
			String actName = actParam.getDeclarator().getName().toString();
			boolean isChangable = false;
			if (actParam.getDeclSpecifier() instanceof IASTSimpleDeclSpecifier
					&& ((IASTSimpleDeclSpecifier) actParam.getDeclSpecifier())
							.getType() == IASTSimpleDeclSpecifier.t_void) {
				actName = ""; //$NON-NLS-1$
				isChangable = false;
			} else if (actName.length() == 0) {
				needsAditionalArgumentNames = true;
				isChangable = true;
				actName = findNameForParameter(NameHelper.getTypeName(actParam));
			}
			parameterInfos.add(new ParameterInfo(actParam, actName, isChangable));
		}
	}

	private String findNameForParameter(String typeName) {
		if (pseudoNameGenerator == null) {
			pseudoNameGenerator = new PseudoNameGenerator();

			for (IASTParameterDeclaration parameter : getParametersFromMethodNode()) {
				if (parameter.getDeclarator().getName().toString().length() != 0) {
					pseudoNameGenerator.addExistingName(parameter.getDeclarator().getName().toString());
				}
			}
		}
		return pseudoNameGenerator.generateNewName(typeName);
	}

	private IASTParameterDeclaration[] getParametersFromMethodNode() {
		if (method.getDeclarators().length < 1) {
			return null;
		}
		return ((ICPPASTFunctionDeclarator) method.getDeclarators()[0]).getParameters();
	}

	public Collection<ParameterInfo> getParameterInfos() {
		return parameterInfos;
	}
}
