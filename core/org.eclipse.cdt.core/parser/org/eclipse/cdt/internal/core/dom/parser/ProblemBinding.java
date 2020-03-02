/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.parser.ParserMessages;
import org.eclipse.core.runtime.PlatformObject;

import com.ibm.icu.text.MessageFormat;

/**
 * Implementation of problem bindings
 */
public class ProblemBinding extends PlatformObject implements IProblemBinding, IASTInternalScope {
	public static ProblemBinding NOT_INITIALIZED = new ProblemBinding(null, 0);

	protected final int id;
	protected char[] arg;
	protected IASTNode node;
	private IBinding[] candidateBindings;

	public ProblemBinding(IASTName name, int id) {
		this(name, id, null, null);
	}

	public ProblemBinding(IASTName name, int id, IBinding[] candidateBindings) {
		this(name, id, null, candidateBindings);
	}

	/**
	 * @param name the name that could not be resolved, may be {@code null}
	 * @param point the point in code where the problem was encountered
	 * @param id the ID of the problem, see {@link IProblemBinding}
	 */
	public ProblemBinding(IASTName name, IASTNode point, int id) {
		this(name, point, id, null);
	}

	/**
	 * @param name the name that could not be resolved, may be {@code null}
	 * @param point the point in code where the problem was encountered
	 * @param id the ID of the problem, see {@link IProblemBinding}
	 * @param candidateBindings candidate bindings that were rejected due to ambiguity or for other
	 *     reasons, may be {@code null}
	 */
	public ProblemBinding(IASTName name, IASTNode point, int id, IBinding[] candidateBindings) {
		this.id = id;
		if (name != null && name.getTranslationUnit() != null) {
			this.node = name;
		} else {
			this.node = point;
			if (name != null) {
				this.arg = name.getSimpleID();
			} else if (candidateBindings != null && candidateBindings.length != 0) {
				this.arg = candidateBindings[0].getNameCharArray();
			}
		}
		this.candidateBindings = candidateBindings;
	}

	public ProblemBinding(IASTNode node, int id, char[] arg) {
		this(node, id, arg, null);
	}

	public ProblemBinding(IASTNode node, int id, char[] arg, IBinding[] candidateBindings) {
		this.id = id;
		this.arg = arg;
		this.node = node;
		this.candidateBindings = candidateBindings;
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eLocal;
	}

	@Override
	public IASTNode getASTNode() {
		return node;
	}

	@Override
	public IBinding[] getCandidateBindings() {
		return candidateBindings != null ? candidateBindings : IBinding.EMPTY_BINDING_ARRAY;
	}

	public void setCandidateBindings(IBinding[] foundBindings) {
		candidateBindings = foundBindings;
	}

	@Override
	public int getID() {
		return id;
	}

	@Override
	public String getMessage() {
		String msg = ParserMessages.getProblemPattern(this);
		if (msg == null)
			return ""; //$NON-NLS-1$

		if (arg == null) {
			if (node instanceof IASTName) {
				arg = ((IASTName) node).toCharArray();
			} else if (candidateBindings != null && candidateBindings.length != 0) {
				arg = candidateBindings[0].getNameCharArray();
			}
		}

		if (arg != null) {
			msg = MessageFormat.format(msg, new Object[] { new String(arg) });
		}

		return msg;
	}

	@Override
	public String getName() {
		if (node instanceof IASTName)
			return new String(((IASTName) node).getSimpleID());
		else
			return arg != null ? new String(arg) : CPPSemantics.EMPTY_NAME;
	}

	@Override
	public char[] getNameCharArray() {
		if (node instanceof IASTName)
			return ((IASTName) node).getSimpleID();
		else
			return arg != null ? arg : CharArrayUtils.EMPTY;
	}

	@Override
	public IScope getScope() throws DOMException {
		throw new DOMException(this);
	}

	@Override
	public IASTNode getPhysicalNode() {
		return getASTNode();
	}

	@Override
	public Object clone() {
		// Don't clone problems.
		return this;
	}

	@Override
	public IScope getParent() throws DOMException {
		throw new DOMException(this);
	}

	@Override
	public IBinding[] find(String name, IASTTranslationUnit tu) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public IBinding[] find(String name) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public IName getScopeName() {
		return null;
	}

	@Override
	public void addName(IASTName name, boolean adlOnly) {
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve) {
		return null;
	}

	@Override
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		return null;
	}

	@Deprecated
	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public boolean isSameType(IType type) {
		return type == this;
	}

	@Override
	public String getFileName() {
		if (node != null)
			return node.getContainingFilename();

		return ""; //$NON-NLS-1$
	}

	@Override
	public int getLineNumber() {
		if (node != null) {
			IASTFileLocation fileLoc = node.getFileLocation();
			if (fileLoc != null)
				return fileLoc.getStartingLineNumber();
		}
		return -1;
	}

	@Override
	public void addBinding(IBinding binding) {
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.NO_LINKAGE;
	}

	@Override
	public String toString() {
		return getMessage();
	}

	@Override
	public IBinding getOwner() {
		if (node instanceof IASTName) {
			IASTTranslationUnit tu = node.getTranslationUnit();
			if (tu instanceof ICPPASTTranslationUnit) {
				return CPPVisitor.findNameOwner((IASTName) node, true);
			}
		}
		return null;
	}

	public void setASTNode(IASTName name) {
		if (name != null) {
			this.node = name;
			this.arg = null;
		}
	}

	@Override
	public void populateCache() {
	}

	@Override
	public void removeNestedFromCache(IASTNode container) {
	}

	// Dummy methods for derived classes.
	public IType getType() {
		return new ProblemType(getID());
	}

	public boolean isStatic() {
		return false;
	}

	public String[] getQualifiedName() throws DOMException {
		throw new DOMException(this);
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		throw new DOMException(this);
	}

	public boolean isGloballyQualified() throws DOMException {
		throw new DOMException(this);
	}

	public boolean isMutable() {
		return false;
	}

	public boolean isConstexpr() {
		return false;
	}

	public boolean isExtern() {
		return false;
	}

	public boolean isExternC() {
		return false;
	}

	public boolean isAuto() {
		return false;
	}

	public boolean isRegister() {
		return false;
	}

	public IValue getInitialValue() {
		return null;
	}

	public ICPPEvaluation getInitializerEvaluation() {
		return EvalFixed.INCOMPLETE;
	}

	public boolean isAnonymous() {
		return false;
	}

	public boolean isDeleted() {
		return false;
	}

	public boolean isInline() {
		return false;
	}

	public boolean takesVarArgs() {
		return false;
	}

	public IType[] getExceptionSpecification() {
		return null;
	}

	public boolean hasParameterPack() {
		return false;
	}

	public boolean isVirtual() {
		return false;
	}

	public boolean isPureVirtual() {
		return false;
	}

	public boolean isImplicit() {
		return false;
	}

	public boolean isExplicit() {
		return false;
	}

	public boolean hasDefaultValue() {
		return false;
	}

	public IValue getDefaultValue() {
		return null;
	}

	public boolean isParameterPack() {
		return false;
	}
}
