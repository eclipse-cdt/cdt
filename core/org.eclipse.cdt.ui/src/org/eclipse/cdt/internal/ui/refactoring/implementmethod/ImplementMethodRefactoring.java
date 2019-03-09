/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik
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
 *     Institute for Software - initial API and implementation
 *     Marc-Andre Laperle
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.Checks;
import org.eclipse.cdt.internal.ui.refactoring.utils.DefinitionFinder;
import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

/**
 * Main class of the ImplementMethodRefactoring (Source generator).
 * Checks conditions, finds insert location and generates the ImplementationNode.
 *
 * @author Mirko Stocker, Lukas Felber, Emanuel Graf
 */
public class ImplementMethodRefactoring extends CRefactoring {
	private ICPPASTFunctionDeclarator createdMethodDeclarator;
	private ImplementMethodData data;
	private MethodDefinitionInsertLocationFinder methodDefinitionInsertLocationFinder;
	private Map<IASTSimpleDeclaration, InsertLocation> insertLocations;
	private static ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

	public ImplementMethodRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
		data = new ImplementMethodData();
		methodDefinitionInsertLocationFinder = new MethodDefinitionInsertLocationFinder();
		insertLocations = new HashMap<>();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		super.checkInitialConditions(sm.newChild(6));

		if (!initStatus.hasFatalError()) {
			List<IASTSimpleDeclaration> unimplementedMethodDeclarations = findUnimplementedMethodDeclarations(pm);
			if (unimplementedMethodDeclarations.isEmpty()) {
				initStatus.addFatalError(Messages.ImplementMethodRefactoring_NoMethodToImplement);
			} else {
				data.setMethodDeclarations(unimplementedMethodDeclarations);

				if (selectedRegion.getLength() > 0) {
					IASTSimpleDeclaration methodDeclaration = SelectionHelper
							.findFirstSelectedDeclaration(selectedRegion, getAST(tu, pm));
					if (NodeHelper.isMethodDeclaration(methodDeclaration)) {
						for (MethodToImplementConfig config : data.getMethodDeclarations()) {
							if (config.getDeclaration() == methodDeclaration) {
								config.setChecked(true);
							}
						}
					}
				}
			}
		}
		sm.done();
		return initStatus;
	}

	private List<IASTSimpleDeclaration> findUnimplementedMethodDeclarations(IProgressMonitor pm)
			throws OperationCanceledException, CoreException {
		final SubMonitor sm = SubMonitor.convert(pm, 2);
		IASTTranslationUnit ast = getAST(tu, sm.newChild(1));
		final List<IASTSimpleDeclaration> list = new ArrayList<>();
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
					if (NodeHelper.isMethodDeclaration(simpleDeclaration)) {
						IASTDeclarator[] declarators = simpleDeclaration.getDeclarators();
						IBinding binding = declarators[0].getName().resolveBinding();
						if (isUnimplementedMethodBinding(binding, sm.newChild(0))) {
							list.add(simpleDeclaration);
							return ASTVisitor.PROCESS_SKIP;
						}
					}
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}
		});
		return list;
	}

	private boolean isUnimplementedMethodBinding(IBinding binding, IProgressMonitor pm) {
		if (binding instanceof ICPPFunction) {
			if (binding instanceof ICPPMethod) {
				ICPPMethod methodBinding = (ICPPMethod) binding;
				if (methodBinding.isPureVirtual()) {
					return false; // Pure virtual not handled for now, see bug 303870
				}
			}

			try {
				return !DefinitionFinder.hasDefinition(binding, refactoringContext, pm);
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}

		return false;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		List<MethodToImplementConfig> methodsToImplement = data.getMethodsToImplement();
		SubMonitor sm = SubMonitor.convert(pm, 4 * methodsToImplement.size());
		for (MethodToImplementConfig config : methodsToImplement) {
			createDefinition(collector, config, sm.newChild(4), -1);
		}
	}

	/**
	 * Utility method to collect modifications from another Refactoring
	 * @param pm The progress monitor
	 * @param collector The collector
	 * @param methods List of methods
	 * @param functionOffset A function offset to determine fully qualified names
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	public void collectModifications(IProgressMonitor pm, ModificationCollector collector,
			List<IASTSimpleDeclaration> methods, int functionOffset) throws CoreException, OperationCanceledException {
		data.setMethodDeclarations(methods);
		for (MethodToImplementConfig config : data.getMethodDeclarations()) {
			config.setChecked(true);
		}
		List<MethodToImplementConfig> methodsToImplement = data.getMethodsToImplement();
		SubMonitor sm = SubMonitor.convert(pm, 4 * methodsToImplement.size());
		for (MethodToImplementConfig config : methodsToImplement) {
			createDefinition(collector, config, sm.newChild(4), functionOffset);
		}
	}

	/**
	 * Create definition for a method
	 * @param collector A modification collector
	 * @param config The method to be inserted
	 * @param subMonitor A sub monitor for the progress
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	protected void createDefinition(ModificationCollector collector, MethodToImplementConfig config,
			IProgressMonitor subMonitor) throws CoreException, OperationCanceledException {
		createDefinition(collector, config, subMonitor, -1);
	}

	/**
	 * Create definition for a method
	 * @param collector A modification collector
	 * @param config The method to be inserted
	 * @param subMonitor A sub monitor for the progress
	 * @param functionOffset The node offset can be explicitly defined with this parameter,
	 * worth when the declarator does not have a valid offset yet. A negative number
	 * can be used to use the node offset of method instead, as returned by getFileLocation().getNodeOffset()
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	protected void createDefinition(ModificationCollector collector, MethodToImplementConfig config,
			IProgressMonitor subMonitor, int functionOffset) throws CoreException, OperationCanceledException {
		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		IASTSimpleDeclaration decl = config.getDeclaration();
		InsertLocation insertLocation = findInsertLocation(decl, subMonitor);
		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		subMonitor.worked(1);
		IASTNode parent = insertLocation.getParentOfNodeToInsertBefore();
		IASTTranslationUnit ast = parent.getTranslationUnit();
		ASTRewrite translationUnitRewrite = collector.rewriterForTranslationUnit(ast);
		subMonitor.worked(1);
		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		IASTNode nodeToInsertBefore = insertLocation.getNodeToInsertBefore();
		IASTNode createdMethodDefinition = createFunctionDefinition(ast, decl, insertLocation, functionOffset);
		subMonitor.worked(1);
		ASTRewrite methodRewrite = translationUnitRewrite.insertBefore(parent, nodeToInsertBefore,
				createdMethodDefinition, null);
		createParameterModifications(methodRewrite, config.getParaHandler());
		subMonitor.done();
	}

	private void createParameterModifications(ASTRewrite methodRewrite, ParameterHandler handler) {
		for (ParameterInfo actParameterInfo : handler.getParameterInfos()) {
			ASTRewrite parameterRewrite = methodRewrite.insertBefore(createdMethodDeclarator, null,
					actParameterInfo.getParameter(), null);
			createNewNameInsertModification(actParameterInfo, parameterRewrite);
			createRemoveDefaultValueModification(actParameterInfo, parameterRewrite);
		}
	}

	private void createRemoveDefaultValueModification(ParameterInfo parameterInfo, ASTRewrite parameterRewrite) {
		if (parameterInfo.hasDefaultValue()) {
			parameterRewrite.remove(parameterInfo.getDefaultValueNode(), null);
		}
	}

	private void createNewNameInsertModification(ParameterInfo parameterInfo, ASTRewrite parameterRewrite) {
		if (parameterInfo.hasNewName()) {
			IASTNode insertNode = parameterInfo.getNewNameNode();
			IASTName replaceNode = parameterInfo.getNameNode();
			parameterRewrite.replace(replaceNode, insertNode, null);
		}
	}

	private InsertLocation findInsertLocation(IASTSimpleDeclaration methodDeclaration, IProgressMonitor subMonitor)
			throws CoreException {
		if (insertLocations.containsKey(methodDeclaration)) {
			return insertLocations.get(methodDeclaration);
		}
		InsertLocation insertLocation = methodDefinitionInsertLocationFinder.find(tu,
				methodDeclaration.getFileLocation(), methodDeclaration.getParent(), refactoringContext, subMonitor);

		if (insertLocation.getTranslationUnit() == null
				|| NodeHelper.isContainedInTemplateDeclaration(methodDeclaration)) {
			insertLocation.setNodeToInsertAfter(NodeHelper.findTopLevelParent(methodDeclaration), tu);
		}
		insertLocations.put(methodDeclaration, insertLocation);
		return insertLocation;
	}

	/**
	 * Create the function definition
	 * @param unit The translation unit
	 * @param methodDeclaration The method to be inserted
	 * @param insertLocation The position for the insert operation
	 * @param functionOffset A function offset to determine fully qualified names. A negative number
	 * can be used to use the node offset of method as returned by getFileLocation().getNodeOffset()
	 * @return
	 * @throws CoreException
	 */
	private IASTDeclaration createFunctionDefinition(IASTTranslationUnit unit, IASTSimpleDeclaration methodDeclaration,
			InsertLocation insertLocation, int functionOffset) throws CoreException {
		IASTDeclSpecifier declSpecifier = methodDeclaration.getDeclSpecifier().copy(CopyStyle.withLocations);
		ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) methodDeclaration
				.getDeclarators()[0];
		IASTNode declarationParent = methodDeclaration.getParent();

		if (declSpecifier instanceof ICPPASTDeclSpecifier) {
			((ICPPASTDeclSpecifier) declSpecifier).setVirtual(false);
			((ICPPASTDeclSpecifier) declSpecifier).setExplicit(false);
		}

		String currentFileName = declarationParent.getNodeLocations()[0].asFileLocation().getFileName();
		if (Path.fromOSString(currentFileName).equals(insertLocation.getFile().getLocation())) {
			declSpecifier.setInline(true);
		}

		if (declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_static) {
			declSpecifier.setStorageClass(IASTDeclSpecifier.sc_unspecified);
		}

		ICPPASTQualifiedName qName = createQualifiedNameFor(functionDeclarator, declarationParent, insertLocation,
				functionOffset);

		createdMethodDeclarator = nodeFactory.newFunctionDeclarator(qName);
		createdMethodDeclarator.setConst(functionDeclarator.isConst());
		createdMethodDeclarator.setRefQualifier(functionDeclarator.getRefQualifier());
		for (IASTPointerOperator pop : functionDeclarator.getPointerOperators()) {
			createdMethodDeclarator.addPointerOperator(pop.copy(CopyStyle.withLocations));
		}
		IASTTypeId[] exceptionSpecification = functionDeclarator.getExceptionSpecification();
		if (exceptionSpecification != ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION) {
			createdMethodDeclarator.setEmptyExceptionSpecification();
			for (IASTTypeId typeId : exceptionSpecification) {
				createdMethodDeclarator
						.addExceptionSpecificationTypeId(typeId == null ? null : typeId.copy(CopyStyle.withLocations));
			}
		}
		IASTFunctionDefinition functionDefinition = nodeFactory.newFunctionDefinition(declSpecifier,
				createdMethodDeclarator, nodeFactory.newCompoundStatement());
		functionDefinition.setParent(unit);

		ICPPASTTemplateDeclaration templateDeclaration = ASTQueries.findAncestorWithType(declarationParent,
				ICPPASTTemplateDeclaration.class);
		if (templateDeclaration != null) {
			ICPPASTTemplateDeclaration newTemplateDeclaration = nodeFactory.newTemplateDeclaration(functionDefinition);
			newTemplateDeclaration.setParent(unit);

			for (ICPPASTTemplateParameter templateParameter : templateDeclaration.getTemplateParameters()) {
				newTemplateDeclaration.addTemplateParameter(templateParameter.copy(CopyStyle.withLocations));
			}

			return newTemplateDeclaration;
		}
		return functionDefinition;
	}

	/**
	 * Create the fully qualified name for the declaration
	 * @param functionDeclarator The function declaration
	 * @param declarationParent Parent of declaration
	 * @param insertLocation Insert position
	 * @param functionOffset A function offset to determine fully qualified names. A negative number
	 * can be used to use the node offset of method as returned by getFileLocation().getNodeOffset()
	 * @return The fully qualified name
	 * @throws CoreException
	 */
	private ICPPASTQualifiedName createQualifiedNameFor(IASTFunctionDeclarator functionDeclarator,
			IASTNode declarationParent, InsertLocation insertLocation, int functionOffset) throws CoreException {
		int insertOffset = insertLocation.getInsertPosition();
		return NameHelper.createQualifiedNameFor(functionDeclarator.getName(), tu,
				functionOffset >= 0 ? functionOffset : functionDeclarator.getFileLocation().getNodeOffset(),
				insertLocation.getTranslationUnit(), insertOffset, refactoringContext);
	}

	public ImplementMethodData getRefactoringData() {
		return data;
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		// TODO egraf add Descriptor
		return null;
	}

	private IFile[] getAllFilesToModify() {
		List<IFile> files = new ArrayList<>(2);
		IFile file = (IFile) tu.getResource();
		if (file != null) {
			files.add(file);
		}

		for (InsertLocation insertLocation : insertLocations.values()) {
			if (insertLocation != null) {
				file = insertLocation.getFile();
				if (file != null) {
					files.add(file);
				}
			}
		}

		return files.toArray(new IFile[files.size()]);
	}

	@Override
	protected RefactoringStatus checkFinalConditions(IProgressMonitor subProgressMonitor,
			CheckConditionsContext checkContext) throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		if (isOneOrMoreImplementationInHeader(subProgressMonitor)) {
			result.addInfo(Messages.ImplementMethodRefactoring_NoImplFile);
		}
		finalConditions(checkContext);
		return result;
	}

	public void finalConditions(CheckConditionsContext checkContext) {
		Checks.addModifiedFilesToChecker(getAllFilesToModify(), checkContext);
	}

	private boolean isOneOrMoreImplementationInHeader(IProgressMonitor subProgressMonitor) throws CoreException {
		for (MethodToImplementConfig config : data.getMethodsToImplement()) {
			IASTSimpleDeclaration decl = config.getDeclaration();
			findInsertLocation(decl, subProgressMonitor);
		}

		if (insertLocations.isEmpty()) {
			return true;
		}

		for (InsertLocation insertLocation : insertLocations.values()) {
			if (insertLocation != null && tu.equals(insertLocation.getTranslationUnit())) {
				return true;
			}
		}
		return false;
	}
}
