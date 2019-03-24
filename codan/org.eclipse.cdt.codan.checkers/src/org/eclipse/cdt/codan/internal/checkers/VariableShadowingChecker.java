/*******************************************************************************
 * Copyright (c) 2010 Gil Barash
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Gil Barash  - Initial implementation
 *    Marco Stornelli - Improvements
 *******************************************************************************/

package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPGlobalScope;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

@SuppressWarnings("restriction")
public class VariableShadowingChecker extends AbstractIndexAstChecker {

	public static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.VariableShadowingProblem"; //$NON-NLS-1$
	public static final String PARAM_MARK_SHADOWED = "param_mark_shadowed"; //$NON-NLS-1$

	private IASTTranslationUnit ast;
	private IIndex index;
	/**
	 * Mark also the variables shadowed by ones in the lower scopes
	 */
	private Boolean markShadowed;

	@Override
	public void processAst(IASTTranslationUnit ast) {
		markShadowed = (Boolean) getPreference(getProblemById(ERR_ID, getFile()), PARAM_MARK_SHADOWED);
		this.ast = ast;
		index = ast.getIndex();
		ast.accept(new VariableDeclarationVisitor());
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_MARK_SHADOWED,
				CheckersMessages.VariableShadowingChecker_ParamMarkShadowedDescription, Boolean.TRUE);
	}

	/**
	 * This visitor looks for variable declarations.
	 */
	class VariableDeclarationVisitor extends ASTVisitor {

		VariableDeclarationVisitor() {
			shouldVisitDeclarators = true;
		}

		@Override
		public int visit(IASTDeclarator declarator) {
			try {
				processDeclarator(declarator);
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return PROCESS_CONTINUE;
		}

		private void processDeclarator(IASTDeclarator declarator)
				throws DOMException, CoreException, InterruptedException {
			IBinding binding = declarator.getName().resolveBinding();

			IScope scope = binding.getScope();
			scope = scope.getParent();
			while (scope != null && !(scope instanceof PDOMCPPGlobalScope)) {

				IBinding[] scopeBindings = scope.find(declarator.getName().toString(), declarator.getTranslationUnit());

				for (IBinding scopeBinding : scopeBindings) {
					if (scopeBinding != null && (scopeBinding instanceof IVariable)) {
						IASTName[] declNames = ast.getDeclarationsInAST(scopeBinding);
						if (declNames != null && declNames.length != 0) {
							processDeclerationShadowing(declarator, declNames[0]);
						} else { // not found in AST, look in index
							IIndexName[] indexNames = index.findDeclarations(scopeBinding);
							if (indexNames != null && indexNames.length != 0)
								processDeclerationShadowing(declarator, indexNames[0]);
						}
						break; // We found the variable we were looking for...
					}
				}
				scope = scope.getParent();
			}
		}

		private void processDeclerationShadowing(IASTDeclarator shadowing, IIndexName shadowed)
				throws CoreException, InterruptedException {
			IASTName shadowedAstName = indexNameToASTName(shadowed);
			if (shadowedAstName != null) {
				processDeclerationShadowing(shadowing, shadowedAstName);
			} else {
				reportProblem(ERR_ID, shadowing, shadowing.getName(),
						CheckersMessages.VariableShadowingChecker_shadowing, nodeFullName(shadowed),
						nodeLocation(shadowed));
				if (markShadowed) {
					IFile ifile = ResourceLookup
							.selectFileForLocation(new Path(shadowed.getFileLocation().getFileName()), getProject());
					if (ifile != null) {
						IProblemLocation probLoc = createProblemLocation(ifile, shadowed.getNodeOffset(),
								shadowed.getNodeOffset() + shadowed.getNodeLength());
						reportProblem(ERR_ID, probLoc, shadowed.toString(),
								CheckersMessages.VariableShadowingChecker_shadowed_by,
								nodeFullName(shadowing.getName()), nodeLocation(shadowing.getName()));
					}
				}
			}
		}

		private IASTName indexNameToASTName(IIndexName iName) throws CoreException, InterruptedException {
			IASTName name;
			name = ast.getNodeSelector(iName.getFileLocation().getFileName()).findEnclosingName(iName.getNodeOffset(),
					iName.getNodeLength());
			if (name != null)
				return name;

			/* try a different AST... */

			IASTTranslationUnit ast = getNameAst(iName);
			if (ast == null)
				return null;

			name = ast.getNodeSelector(iName.getFileLocation().getFileName()).findEnclosingName(iName.getNodeOffset(),
					iName.getNodeLength());

			return name;
		}

		private synchronized IASTTranslationUnit getNameAst(IIndexName iName)
				throws CoreException, InterruptedException {
			IFile ifile = ResourceLookup.selectFileForLocation(new Path(iName.getFileLocation().getFileName()),
					getProject());
			if (ifile == null)
				return null;

			ICElement celement = CoreModel.getDefault().create(ifile);
			if (!(celement instanceof ITranslationUnit))
				return null; // not a C/C++ file

			ITranslationUnit tu = (ITranslationUnit) celement;
			IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
			// lock the index for read access
			index.acquireReadLock();
			try {
				// create index based ast
				IASTTranslationUnit ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
				if (ast == null)
					return null;
				return ast;
			} finally {
				index.releaseReadLock();
			}
		}

		private void processDeclerationShadowing(IASTDeclarator shadowing, IASTName shadowed) {
			// assert that the file location is not the same for both
			Assert.isTrue(!shadowing.getFileLocation().equals(shadowed.getFileLocation()));

			reportProblem(ERR_ID, shadowing, shadowing.getName(), CheckersMessages.VariableShadowingChecker_shadowing,
					nodeFullName(shadowed), nodeLocation(shadowed));
			if (markShadowed)
				reportProblem(ERR_ID, shadowed, shadowed, CheckersMessages.VariableShadowingChecker_shadowed_by,
						nodeFullName(shadowing.getName()), nodeLocation(shadowing.getName()));

		}

		private String getNodeFileName(IASTFileLocation fileLoc) {
			String fileName = fileLoc.getFileName();
			IFile ifile = ResourceLookup.selectFileForLocation(new Path(fileName), getProject());
			if (ifile != null)
				fileName = ifile.getFullPath().toOSString();
			return fileName;
		}

		private String nodeFullName(IASTName name) {
			String ret = ""; //$NON-NLS-1$

			try {
				IBinding owner;
				owner = name.resolveBinding().getOwner();
				ret = owner.getName() + "::"; //$NON-NLS-1$
			} catch (NullPointerException e) {
			}

			ret += name;
			return ret;
		}

		private String nodeFullName(IIndexName name) {
			return name.toString();
		}

		private String nodeLocation(IASTName name) {
			IASTFileLocation fileLoc = name.getFileLocation();
			return getNodeFileName(fileLoc) + CheckersMessages.VariableShadowingChecker_at_line
					+ fileLoc.getStartingLineNumber();
		}

		private String nodeLocation(IIndexName name) {
			IASTFileLocation fileLoc = name.getFileLocation();
			return getNodeFileName(fileLoc) + CheckersMessages.VariableShadowingChecker_at_byte + name.getNodeOffset();
		}
	}
}