/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

public class IndexToASTNameHelper {
	public static List<IASTName> findNamesIn(IASTTranslationUnit tu, IBinding binding, IIndex index) {
		BindingToAstNameMatcher visitor = new BindingToAstNameMatcher(binding, index);
		tu.accept(visitor);
		return visitor.getMatches();
	}

	public static IASTName findMatchingASTName(IASTTranslationUnit tu, IName name, IIndex index) throws CoreException {
		if (name instanceof IASTName) {
			return (IASTName) name;
		} else if (!(name instanceof IIndexName)) {
			return null;
		}

		IndexNameToAstNameMatcher visitor = new IndexNameToAstNameMatcher(tu, (IIndexName) name, index);
		tu.accept(visitor);
		return visitor.getMatch();
	}

	static boolean shouldConsiderName(IASTName candidate) {
		return !isQualifiedName(candidate) && isLastNameInQualifiedName(candidate) && !isUnnamedName(candidate);
	}

	private static boolean isLastNameInQualifiedName(IASTName name) {
		if (name.getParent() instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qName = (ICPPASTQualifiedName) name.getParent();
			return name.equals(qName.getLastName());
		}
		return true;
	}

	private static boolean isUnnamedName(IASTName name) {
		return name.getFileLocation() == null && "".equals(name.toString()); //$NON-NLS-1$
	}

	private static boolean isQualifiedName(IASTName name) {
		return name instanceof ICPPASTQualifiedName;
	}
}

class IndexNameToAstNameMatcher extends ASTVisitor {
	private IASTName result;
	private IBinding bindingToFind;
	private char[] charNameToFind;
	private IIndex index;
	private IASTFileLocation locationToFind;

	public IndexNameToAstNameMatcher(IASTTranslationUnit tu, IIndexName indexName, IIndex index) throws CoreException {
		super(true);
		locationToFind = indexName.getFileLocation();
		bindingToFind = index.findBinding(indexName);
		this.index = index;
		charNameToFind = bindingToFind.getNameCharArray();
		shouldVisitImplicitNames = true;
		shouldVisitImplicitNameAlternates = true;
	}

	@Override
	public int visit(IASTName candidate) {
		if (!IndexToASTNameHelper.shouldConsiderName(candidate)) {
			return PROCESS_CONTINUE;
		}
		if (isEquivalent(candidate)) {
			result = candidate;
			return PROCESS_ABORT;
		}
		return PROCESS_CONTINUE;
	}

	private boolean isEquivalent(IASTName candidate) {
		return matchesIndexName(candidate) && bindingToFind.equals(index.adaptBinding(candidate.resolveBinding()));
	}

	private boolean matchesIndexName(IASTName candidate) {
		IASTFileLocation candidateLocation = candidate.getFileLocation();
		return locationToFind.getNodeOffset() == candidateLocation.getNodeOffset() &&
				locationToFind.getNodeLength() == candidateLocation.getNodeLength() &&
				locationToFind.getFileName().equals(candidateLocation.getFileName()) &&
				CharArrayUtils.equals(candidate.getLookupKey(), charNameToFind);
	}

	public IASTName getMatch() {
		return result;
	}
}

class BindingToAstNameMatcher extends ASTVisitor {
	private List<IASTName> results = new ArrayList<IASTName>();
	private IBinding bindingToFind;
	private char[] toFindName;
	private IIndex index;

	public BindingToAstNameMatcher(IBinding binding, IIndex index) {
		super(true);
		bindingToFind = index.adaptBinding(binding);
		this.index = index;
		toFindName = binding.getNameCharArray();
		shouldVisitImplicitNames = true;
		shouldVisitImplicitNameAlternates = true;
	}

	@Override
	public int visit(IASTName candidate) {
		if (!IndexToASTNameHelper.shouldConsiderName(candidate)) {
			return PROCESS_CONTINUE;
		}
		if (isEquivalent(candidate)) {
			results.add(candidate);
		}
		return PROCESS_CONTINUE;
	}

	private boolean isEquivalent(IASTName candidate) {
		return CharArrayUtils.equals(candidate.getSimpleID(), toFindName) && bindingToFind.equals(index.adaptBinding(candidate.resolveBinding()));
	}

	public List<IASTName> getMatches() {
		return results;
	}
}
