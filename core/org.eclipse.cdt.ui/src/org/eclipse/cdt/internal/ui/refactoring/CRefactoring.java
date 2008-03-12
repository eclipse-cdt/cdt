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
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.IASTDeclarationAmbiguity;

import org.eclipse.cdt.internal.ui.refactoring.utils.EclipseObjects;

public abstract class CRefactoring extends Refactoring {
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final int AST_STYLE = ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT | ITranslationUnit.AST_SKIP_INDEXED_HEADERS;

	protected String name = Messages.HSRRefactoring_name; 
	protected IFile file;
	protected ISelection selection;
	protected RefactoringStatus initStatus;
	protected IASTTranslationUnit unit;
	private IIndex fIndex;
	public static final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$

	public CRefactoring(IFile file, ISelection selection, boolean runHeadless) {
		this.file = file;
		this.selection = selection;
		this.initStatus=new RefactoringStatus();

		if(!runHeadless) {
			IWorkbenchPage activePage = EclipseObjects.getActivePage();

			if(!activePage.saveAllEditors(true)){
				initStatus.addError("EDITOR_NOT_SAVE");  //$NON-NLS-1$
			}
		}
		if(selection == null){
			initStatus.addError(Messages.HSRRefactoring_SelectionNotValid);  
		}

	}
	
	public CRefactoring(IFile file, ISelection selection) {
		this(file, selection, false);
	}
	
	private class ProblemFinder extends ASTVisitor{
		
		private boolean problemFound = false;
		private final RefactoringStatus status;
		
		public ProblemFinder(RefactoringStatus status){
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
			if(!problemFound){
				status.addWarning(Messages.HSRRefactoring_CompileErrorInTU); 
				problemFound = true;
			}
		}
		
	}
	
	private class AmbiguityFinder extends ASTVisitor{
		
		private boolean ambiguityFound = false;
		
		{
			shouldVisitDeclarations = true;
			shouldVisitExpressions = true;
			shouldVisitStatements= true;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			if (declaration instanceof IASTAmbiguousDeclaration || declaration instanceof IASTDeclarationAmbiguity) {
				ambiguityFound = true;
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (expression instanceof IASTAmbiguousExpression) {
				ambiguityFound = true;
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTAmbiguousStatement) {
				ambiguityFound = true;
			}
			return ASTVisitor.PROCESS_CONTINUE;
		}
		
		public boolean ambiguityFound() {
			return ambiguityFound;
		}
		
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		return status;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor sm = SubMonitor.convert(pm, 10);
		sm.subTask(Messages.HSRRefactoring_PM_LoadTU); 
		if(isProgressMonitorCanceld(sm, initStatus)) {
			return initStatus;
		}
		if(!loadTranslationUnit(initStatus, sm.newChild(8))){
			initStatus.addError(Messages.HSRRefactoring_CantLoadTU);  
		}
		if(isProgressMonitorCanceld(sm, initStatus)) {
			return initStatus;
		}
		sm.subTask(Messages.HSRRefactoring_PM_CheckTU); 
		translationUnitHasProblem();
		if(translationUnitIsAmbiguous()) {
			initStatus.addError(Messages.HSRRefactoring_Ambiguity); 
		}
		sm.worked(2);
		sm.subTask(Messages.HSRRefactoring_PM_InitRef); 
		sm.done();
		return initStatus;
	}

	protected boolean isProgressMonitorCanceld(IProgressMonitor sm,
			RefactoringStatus initStatus2) {
		if(sm.isCanceled()) {
			initStatus2.addFatalError(Messages.HSRRefactoring_CanceledByUser); 
			return true;
		}
		return false;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		ModificationCollector collector = new ModificationCollector();
		collectModifications(pm, collector);
		return collector.createFinalChange();
	}
	
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
		throws CoreException, OperationCanceledException {
	}

	@Override
	public String getName() {
		return name;
	}
	
	protected boolean loadTranslationUnit(RefactoringStatus status, IProgressMonitor mon) {
		SubMonitor subMonitor = SubMonitor.convert(mon, 10);
		if (file != null) {
			try {
				subMonitor.subTask(Messages.HSRRefactoring_PM_ParseTU); 
				ITranslationUnit tu = (ITranslationUnit) CCorePlugin
						.getDefault().getCoreModel().create(file);
				unit = tu.getAST(fIndex, AST_STYLE);
				subMonitor.worked(2);
				if(isProgressMonitorCanceld(subMonitor, initStatus)) {
					return true;
				}
				subMonitor.subTask(Messages.HSRRefactoring_PM_MergeComments); 

				subMonitor.worked(8);
			} catch (CoreException e) {
				status.addFatalError(e.getMessage()); 
				subMonitor.done();
				return false;
			}

		} else {
			status.addFatalError(Messages.NO_FILE); 
			subMonitor.done();
			return false;
		}
		subMonitor.done();
		return true;
	}

	private static class ExpressionPosition {
		public int start;
		public int end;
		              
		@Override
		public String toString() {
			return String.format("Position ranges from %d to %d and has a length of %d", Integer.valueOf(start),   //$NON-NLS-1$
					Integer.valueOf(end), Integer.valueOf(end - start));
		}
	}
	
	protected static ExpressionPosition createExpressionPosition(IASTNode expression) {
		ExpressionPosition selection = new ExpressionPosition();

		int nodeLength = 0;
		IASTNodeLocation[] nodeLocations = expression.getNodeLocations();
		if (nodeLocations.length != 1) {
			for (IASTNodeLocation location : nodeLocations) {
				if (location instanceof IASTMacroExpansionLocation) {
					IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) location;
					selection.start = macroLoc.asFileLocation().getNodeOffset();
					nodeLength = macroLoc.asFileLocation().getNodeLength();
				}
			}
		} else {
			if (nodeLocations[0] instanceof IASTMacroExpansionLocation) {
				IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) nodeLocations[0];
				selection.start = macroLoc.asFileLocation().getNodeOffset();
				nodeLength = macroLoc.asFileLocation().getNodeLength();
			} else {
				IASTFileLocation loc = expression.getFileLocation();
				selection.start = loc.getNodeOffset();
				nodeLength = loc.getNodeLength();
			}
		}
		selection.end = selection.start + nodeLength;
		return selection;
	}

	protected boolean isExpressionWhollyInSelection(ITextSelection textSelection, IASTNode expression) {
		ExpressionPosition exprPos = createExpressionPosition(expression);

		int selStart = textSelection.getOffset();
		int selEnd = textSelection.getLength() + selStart;

		return exprPos.start >= selStart && exprPos.end <= selEnd;
	}

	public static boolean isSelectionOnExpression(ITextSelection textSelection, IASTNode expression) {
		ExpressionPosition exprPos = createExpressionPosition(expression);
		int selStart = textSelection.getOffset();
		int selEnd = textSelection.getLength() + selStart;
		return exprPos.end > selStart && exprPos.start < selEnd;
	}
	
	protected boolean isInSameFile(IASTNode node) {
		IPath path = new Path(node.getContainingFilename());
		IFile locFile = ResourcesPlugin.getWorkspace().getRoot().getFile(file.getLocation());
		IFile tmpFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		return locFile.equals(tmpFile);
	}
	
	protected boolean isInSameFileSelection(ITextSelection textSelection, IASTNode node) {
		if( isInSameFile(node) ) {
			return isSelectionOnExpression(textSelection, node);
		}
		return false;
	}

	protected MethodContext findContext(IASTNode node) {
		boolean found = false;
		MethodContext context = new MethodContext();
		context.setType(MethodContext.ContextType.NONE);
		 IASTName name = null;
		 while(node != null && !found){
			 node = node.getParent();
			 if(node instanceof IASTFunctionDeclarator){
				 name=((IASTFunctionDeclarator)node).getName();
				 found = true;
				 context.setType(MethodContext.ContextType.FUNCTION);
			 } else if (node instanceof IASTFunctionDefinition){
				 name=((IASTFunctionDefinition)node).getDeclarator().getName();
				 found = true;
				 context.setType(MethodContext.ContextType.FUNCTION);
			 } 
		 }
		 if(name instanceof ICPPASTQualifiedName){
			 ICPPASTQualifiedName qname =( ICPPASTQualifiedName )name;
			 context.setMethodQName(qname);
			 IBinding bind = qname.resolveBinding();
			 IASTName[] decl = unit.getDeclarationsInAST(bind);//TODO HSR funktioniert nur fuer namen aus der aktuellen Translationunit
			 for (IASTName tmpname : decl) {
				 IASTNode methoddefinition = tmpname.getParent().getParent();
				 if (methoddefinition instanceof IASTSimpleDeclaration) {
					 context.setMethodDeclarationName(tmpname);
					 context.setType(MethodContext.ContextType.METHOD);
				 }
			 }

		 }
		 return context;
	}
	
	protected boolean translationUnitHasProblem() {
		ProblemFinder pf = new ProblemFinder(initStatus);
		unit.accept(pf);		
		return pf.hasProblem();
	}
	
	protected boolean translationUnitIsAmbiguous() {
		AmbiguityFinder af = new AmbiguityFinder();
		unit.accept(af);
		return af.ambiguityFound();
	}
	
	public void lockIndex() throws CoreException, InterruptedException {
		if (fIndex == null) {
			ICProject[] projects= CoreModel.getDefault().getCModel().getCProjects();
			fIndex= CCorePlugin.getIndexManager().getIndex(projects);
		}
		fIndex.acquireReadLock();
	}
	
	public void unlockIndex() {
		if (fIndex != null) {
			fIndex.releaseReadLock();
		}
		fIndex= null;
	}

	public IIndex getIndex() {
		return fIndex;
	}
}
