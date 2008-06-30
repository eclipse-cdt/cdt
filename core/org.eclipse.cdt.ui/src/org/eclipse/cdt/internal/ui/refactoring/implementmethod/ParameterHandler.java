/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.ui.refactoring.CTextFileChange;

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
	private ImplementMethodRefactoring refactoring;
	
	public ParameterHandler(ImplementMethodRefactoring refactoring) {
		this.refactoring = refactoring;
	}
	
	public boolean needsAdditionalArgumentNames() {
		return needsAditionalArgumentNames;
	}
	
	public void initArgumentNames() {
		if(parameterInfos != null) {
			return;
		}
		needsAditionalArgumentNames = false; 
		parameterInfos = new ArrayList<ParameterInfo>();
		for(IASTParameterDeclaration actParam : getParametersFromMethodNode()) {
			String actName = actParam.getDeclarator().getName().toString();
			boolean isChangable = false;
			if(actParam.getDeclSpecifier()instanceof IASTSimpleDeclSpecifier && ((IASTSimpleDeclSpecifier)actParam.getDeclSpecifier()).getType() == IASTSimpleDeclSpecifier.t_void) {
				actName = "";
				isChangable = false;
			}else if(actName.length() == 0) {
				needsAditionalArgumentNames = true;
				isChangable = true;
				actName = findNameForParameter(NameHelper.getTypeName(actParam));
			}
			parameterInfos.add(new ParameterInfo(actParam, actName, isChangable));
		}
	}

	private String findNameForParameter(String typeName) {
		if(pseudoNameGenerator == null) {
			pseudoNameGenerator = new PseudoNameGenerator();

			for(IASTParameterDeclaration parameter : getParametersFromMethodNode()) {
				if(parameter.getDeclarator().getName().toString().length() != 0) {
					pseudoNameGenerator.addExistingName(parameter.getDeclarator().getName().toString());
				}
			}
		}
		return pseudoNameGenerator.generateNewName(typeName);
	}
	
	private IASTParameterDeclaration[] getParametersFromMethodNode() {
		if(refactoring.getMethodDeclaration().getDeclarators().length < 1) {
			return null;
		}
		return ((ICPPASTFunctionDeclarator) refactoring.getMethodDeclaration().getDeclarators()[0]).getParameters();
	}
	
	public String createFunctionDefinitionSignature() {
		try {
			CompositeChange compositeChange = (CompositeChange) refactoring.createChange(new NullProgressMonitor());
			InsertEdit insertEdit = getInsertEdit(compositeChange);
			return insertEdit.getText().trim();
		} catch (OperationCanceledException e) {
			return Messages.PreviewGenerationNotPossible;
		} catch (CoreException e) {
			return Messages.PreviewGenerationNotPossible;
		}
	}

	private InsertEdit getInsertEdit(CompositeChange compositeChange) {
		for(Change actChange : compositeChange.getChildren()) {
			if(actChange instanceof CompositeChange) {
				return getInsertEdit((CompositeChange) actChange);
			} else if (actChange instanceof CTextFileChange) {
				CTextFileChange textFileChange = (CTextFileChange) actChange;
				MultiTextEdit multiEdit = (MultiTextEdit) textFileChange.getEdit();
				if(multiEdit.getChildrenSize() == 0) {
					continue;
				}
				return (InsertEdit) multiEdit.getChildren()[0];
			}
		}
		return null;
	}

	public Collection<ParameterInfo> getParameterInfos() {
		return parameterInfos;
	}
}
