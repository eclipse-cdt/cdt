/*******************************************************************************
 * Copyright (c) 2013 - Xdin AB
 * This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Erik Johansson
 ******************************************************************************/


package org.eclipse.cdt.internal.ui.refactoring.createmethod;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

@SuppressWarnings("restriction")
public class CreateMethodRefactoring extends CRefactoring {

	//private ICodanProblemMarker marker;
	private IMarker marker;
	private String[] targetClassQualifiedName;
	private IBinding targetClassBinding;
	private ICPPASTFunctionCallExpression methodCall;
	private IASTTranslationUnit classDefAST;
	private IASTTranslationUnit markerFileAST;
	private ICPPASTCompositeTypeSpecifier targetClass;
	private IASTNode insertionPoint;
	private IASTFunctionDefinition functionDefinition;
	private boolean shouldOpenLocation;
	
	public boolean shouldOpenLocation() {
		return shouldOpenLocation;
	}

	public void shouldOpenLocation(boolean shouldOpenLocation) {
		this.shouldOpenLocation = shouldOpenLocation;
	}

	/**
	 * 
	 * @param element ITranslationUnit for the file containing the marker.
	 * @param selection
	 * @param project
	 * @param codanMarker
	 */
	public CreateMethodRefactoring(ICElement element, ISelection selection,
			ICProject project, IMarker marker) {
		super(element, selection, project);

		this.marker = marker;
		this.shouldOpenLocation = false;
	}
	
	public boolean initialConditionsOK() {
		return initStatus.isOK();
	}
	
	public int getNumberOfParameters() {
		if (functionDefinition == null)
			return -1;
		else {
			IASTStandardFunctionDeclarator decl = (IASTStandardFunctionDeclarator) functionDefinition.getDeclarator();
			return decl.getParameters().length;
		}
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		super.checkInitialConditions(sm.newChild(6));

		if (!initStatus.hasFatalError()) {

			markerFileAST = getAST(getTranslationUnit(), pm);
			extractMethodCallAndTargetClassInfo(markerFileAST);
			if (targetClassQualifiedName == null || targetClassBinding == null)
				initStatus.addFatalError(Messages.CreateMethodRefactoring_ClassNotFound);
			else {
				classDefAST = findASTForBinding(targetClassBinding);
				if (classDefAST == null)
					initStatus.addFatalError(Messages.CreateMethodRefactoring_ClassNotFound);
				else {
					targetClass = Helpers.getClass(classDefAST, targetClassQualifiedName);
					if (targetClass == null)
						initStatus.addFatalError(Messages.CreateMethodRefactoring_ClassNotFound);
					else {
						insertionPoint = getInsertionPoint(targetClass);
						functionDefinition = createNewNode(markerFileAST, classDefAST);
						if (functionDefinition == null) {
							initStatus.addFatalError(Messages.CreateMethodRefactoring_MethodCannotBeCreated);
						}
					}
				}
			}
		}

		sm.done();
		return initStatus;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext checkContext)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		
		return status;
	}
	
	public IASTFunctionDefinition getFunctionDefinition() {
		return functionDefinition;
	}
	
	/**
	 * Don't call before checkInitialConditions OK.
	 * 
	 * @return
	 * @throws NullPointerException
	 */
	public String getInsertionPointFileName() throws NullPointerException {
		return classDefAST.getFilePath();
	}
	
	/**
	 * Don't call before checkInitialConditions OK.
	 * 
	 * @return
	 * @throws NullPointerException
	 */
	public int getInsertionPointOffset() throws NullPointerException {
		return insertionPoint == null ? targetClass.getFileLocation().getNodeOffset() 
				+ targetClass.getFileLocation().getNodeLength()
				: insertionPoint.getFileLocation().getNodeOffset();
	}
	
	public void updateParameterName(IASTParameterDeclaration parameter, String newName) {
		if (classDefAST == null)
			return;
		parameter.getDeclarator().setName(classDefAST.getASTNodeFactory().newName(newName.toCharArray()));
	}
	
	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		
		return null;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm,
			ModificationCollector collector) throws CoreException,
			OperationCanceledException {

		ASTRewrite r = collector.rewriterForTranslationUnit(classDefAST);
		r.insertBefore(targetClass, insertionPoint, getFunctionDefinition(), null);
		
	}
	
	private IASTTranslationUnit findASTForBinding(IBinding binding) {
		try {
			IIndex index = getIndex();
			IIndexName[] definitions = index.findDefinitions(binding);
			if (definitions.length > 0) {
				IIndexName def = definitions[0];
				ITranslationUnit tUnit = CoreModelUtil.findTranslationUnitForLocation(
						def.getFile().getLocation(), getTranslationUnit().getCProject());
				if (tUnit != null)
					return getAST(tUnit, null);
			}
		} catch (OperationCanceledException e) {

			e.printStackTrace();
		} catch (CoreException e) {

			e.printStackTrace();
		}
		return null;
	}
	
	private void extractMethodCallAndTargetClassInfo(IASTTranslationUnit ast) {
		
	String methodName = Helpers.getProblemArgument(marker, 0);
		
		final List<ICPPASTFunctionCallExpression> list = new ArrayList<ICPPASTFunctionCallExpression>();
		markerFileAST.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}
			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof ICPPASTFunctionCallExpression) {
					list.add((ICPPASTFunctionCallExpression) expression);
					return ASTVisitor.PROCESS_SKIP;
				}
				else
					return ASTVisitor.PROCESS_CONTINUE;
			}
		});
		
		for (ICPPASTFunctionCallExpression functionCall : list) {
			IASTExpression fNameExpr = functionCall.getFunctionNameExpression();
			if (fNameExpr instanceof ICPPASTFieldReference) {
				ICPPASTFieldReference fieldRef = (ICPPASTFieldReference) fNameExpr;
				IASTName fName = fieldRef.getFieldName();
				if (fName.toString().equals(methodName)) {
					IType type = fieldRef.getFieldOwner().getExpressionType();
					if (type instanceof IPointerType)
						type = ((IPointerType) type).getType();
					if (type instanceof ICPPClassType) {
						ICPPClassType ct = (ICPPClassType) type;
						try {
							targetClassBinding = (IBinding) ct;
							targetClassQualifiedName = ct.getQualifiedName();
							methodCall = functionCall;
							return;
						} catch (DOMException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return;
	}
	
	/**
	* Get insertion point, which will be at the end of the first "public block" 
	* or as the last declaration if none exist.
	*/
	private IASTNode getInsertionPoint(ICPPASTCompositeTypeSpecifier c) {
		boolean belowPublic = false;
		for (IASTDeclaration declaration : c.getDeclarations(true)) {
			if (declaration instanceof ICPPASTVisibilityLabel) {
				if (belowPublic)
					return declaration;
				else if (((ICPPASTVisibilityLabel) declaration).getVisibility() == ICPPASTVisibilityLabel.v_public)
					belowPublic = true;	
				else
					belowPublic = false;
			}
		}
		return null;
	}
	
	private IASTFunctionDefinition createNewNode(IASTTranslationUnit markerFileAST, IASTTranslationUnit classDefAST) {
		
		return Helpers.createFunctionDefinitionFromFunctionCall(classDefAST.getASTNodeFactory(), methodCall, markerFileAST);
	}
}