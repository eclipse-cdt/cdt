package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;

import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.PseudoNameGenerator;

public class ParameterHandler {
	private boolean needsAditionalArgumentNames;
	private PseudoNameGenerator pseudoNameGenerator;
	private ArrayList<Parameter> parameters;
	private ImplementMethodRefactoring refactoring;
	
	public ParameterHandler(ImplementMethodRefactoring refactoring) {
		this.refactoring = refactoring;
	}
	
	public boolean needsAdditionalArgumentNames() {
		return needsAditionalArgumentNames;
	}
	
	public void initAditionalArgumentNames() {
 		if(parameters != null) {
			return;
		}
		needsAditionalArgumentNames = false; 
		parameters = new ArrayList<Parameter>();
		for(IASTParameterDeclaration actParam : getParametersFromMethodNode()) {
			String actName = actParam.getDeclarator().getName().toString();
			boolean isChangable = false;
			String typeName = NameHelper.getTypeName(actParam);
			if(actName.length() == 0) {
				needsAditionalArgumentNames = true;
				isChangable = true;
				actName = findNameForParameter(typeName);
			}
			parameters.add(new Parameter(typeName, actName, isChangable));
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
		ASTWriter writer = new ASTWriter();
		IASTNode def = refactoring.createFunctionDefinition();
		return writer.write(def);
	}

	public Collection<Parameter> getParameters() {
		return parameters;
	}
}
