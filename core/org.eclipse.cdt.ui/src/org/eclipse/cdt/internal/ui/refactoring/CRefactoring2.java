/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * 	   Institute for Software - initial API and implementation
 *	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.osgi.util.NLS;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;

/**
 * The base class for all AST based refactorings, provides some common implementations for
 * AST creation, condition checking, change generating, and selection handling.
 * This class is intended as a replacement for CRefactoring.
 */
public abstract class CRefactoring2 extends Refactoring {
	protected String name = Messages.Refactoring_name; 
	protected final ICProject project;
	protected final ITranslationUnit tu;
	protected final RefactoringStatus initStatus;
	protected final RefactoringASTCache astCache;
	protected Region selectedRegion;

	public CRefactoring2(ICElement element, ISelection selection, ICProject project, 
			RefactoringASTCache astCache) {
		this.project = project;
		this.astCache = astCache;
		this.initStatus= new RefactoringStatus();
		if (!(element instanceof ISourceReference)) {
			this.tu = null;
			initStatus.addFatalError(Messages.Refactoring_SelectionNotValid);
			return;
		}

		ISourceReference sourceRef= (ISourceReference) element;
		tu = CModelUtil.toWorkingCopy(sourceRef.getTranslationUnit());

		if (selection instanceof ITextSelection) {
			this.selectedRegion = SelectionHelper.getRegion(selection);
		} else {
			try {
				ISourceRange sourceRange = sourceRef.getSourceRange();
				this.selectedRegion = new Region(sourceRange.getIdStartPos(), sourceRange.getIdLength());
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
	}

	private class ProblemFinder extends ASTVisitor {
		private boolean problemFound = false;
		private final RefactoringStatus status;
		
		public ProblemFinder(RefactoringStatus status) {
			this.status = status;
		}

		{
			shouldVisitProblems = true;
			shouldVisitDeclarations = true;
			shouldVisitExpressions = true;
			shouldVisitStatements = true;
			shouldVisitTypeIds = true;
		}

		@Override
		public int visit(IASTProblem problem) {
			addWarningToState();
			return ASTVisitor.PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			if (declaration instanceof IASTProblemDeclaration) {
				addWarningToState();
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (expression instanceof IASTProblemExpression) {
				addWarningToState();
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTProblemStatement) {
				addWarningToState();
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTTypeId typeId) {
			if (typeId instanceof IASTProblemTypeId) {
				addWarningToState();
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}

		public boolean hasProblem() {
			return problemFound;
		}

		private void addWarningToState() {
			if (!problemFound) {
				status.addWarning(Messages.Refactoring_CompileErrorInTU); 
				problemFound = true;
			}
		}
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		if (pm == null)
			pm = new NullProgressMonitor();
		pm.beginTask(Messages.CRefactoring_checking_final_conditions, 6);

		CheckConditionsContext context = createCheckConditionsContext();
		RefactoringStatus result = checkFinalConditions(new SubProgressMonitor(pm, 5), context);
		if (result.hasFatalError()) {
			pm.done();
			return result;
		}
		if (pm.isCanceled())
			throw new OperationCanceledException();

		result.merge(context.check(new SubProgressMonitor(pm, 1)));
		pm.done();
		return result;
	}

	protected abstract RefactoringStatus checkFinalConditions(IProgressMonitor subProgressMonitor,
			CheckConditionsContext checkContext) throws CoreException, OperationCanceledException;

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		sm.subTask(Messages.Refactoring_PM_LoadTU); 
		if (isProgressMonitorCanceld(sm, initStatus)) {
			return initStatus;
		}
		IASTTranslationUnit ast = getAST(tu, sm);
		if (ast == null) {
			initStatus.addError(NLS.bind(Messages.Refactoring_ParsingError, tu.getPath()));
			return initStatus;
		}
		if (isProgressMonitorCanceld(sm, initStatus)) {
			return initStatus;
		}
		sm.subTask(Messages.Refactoring_PM_CheckTU); 
		checkAST(ast);
		sm.worked(2);
		sm.subTask(Messages.Refactoring_PM_InitRef); 
		sm.done();
		return initStatus;
	}

	protected static boolean isProgressMonitorCanceld(IProgressMonitor sm, RefactoringStatus status) {
		if (sm.isCanceled()) {
			status.addFatalError(Messages.Refactoring_CanceledByUser); 
			return true;
		}
		return false;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		ModificationCollector collector = new ModificationCollector();
		collectModifications(pm, collector);
		CCompositeChange finalChange = collector.createFinalChange();
		finalChange.setDescription(new RefactoringChangeDescriptor(getRefactoringDescriptor()));
		return finalChange;
	}

	abstract protected RefactoringDescriptor getRefactoringDescriptor();

	abstract protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException;

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns the translation unit where the refactoring started.
	 */
	public ITranslationUnit getTranslationUnit() {
		return tu;
	}

	protected IASTTranslationUnit getAST(ITranslationUnit tu, IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return astCache.getAST(tu, pm);
	}

	protected boolean checkAST(IASTTranslationUnit ast) {
		ProblemFinder problemFinder = new ProblemFinder(initStatus);
		ast.accept(problemFinder);		
		return problemFinder.hasProblem();
	}

	protected List<IASTName> findAllMarkedNames(IASTTranslationUnit ast) {
		final List<IASTName> names = new ArrayList<IASTName>();

		ast.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (name.isPartOfTranslationUnitFile() &&
						SelectionHelper.isSelectionOnExpression(selectedRegion, name) &&
						!(name instanceof ICPPASTQualifiedName)) {
					names.add(name);
				}
				return super.visit(name);
			}
		});
		return names;
	}

	private CheckConditionsContext createCheckConditionsContext() throws CoreException {
		CheckConditionsContext result= new CheckConditionsContext();
		result.add(new ValidateEditChecker(getValidationContext()));
		result.add(new ResourceChangeChecker());
		return result;
	}
}
