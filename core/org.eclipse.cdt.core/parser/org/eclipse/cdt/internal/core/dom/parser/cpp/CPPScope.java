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
 *     Andrew Ferguson (Symbian)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Base class for c++-scopes of the AST.
 */
abstract public class CPPScope implements ICPPASTInternalScope {
	protected static final char[] CONSTRUCTOR_KEY = "!!!CTOR!!!".toCharArray(); //$NON-NLS-1$
	private static final IProgressMonitor NPM = new NullProgressMonitor();
	private static final ICPPNamespace UNINITIALIZED = new CPPNamespace.CPPNamespaceProblem(null, 0, null);

	private final IASTNode physicalNode;
	private boolean isCached = false;
	protected CharArrayObjectMap<Object> bindings;
	private ICPPNamespace fIndexNamespace = UNINITIALIZED;

	public static class CPPScopeProblem extends ProblemBinding implements ICPPScope {
		public CPPScopeProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}

		public CPPScopeProblem(IASTName name, int id) {
			super(name, id);
		}
	}

	public CPPScope(IASTNode physicalNode) {
		this.physicalNode = physicalNode;
	}

	@Override
	public IScope getParent() {
		return CPPVisitor.getContainingNonTemplateScope(physicalNode);
	}

	@Override
	public IASTNode getPhysicalNode() {
		return physicalNode;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public void addName(IASTName name, boolean adlOnly) {
		// Don't add inactive names to the scope.
		if (!name.isActive())
			return;

		if (name instanceof ICPPASTQualifiedName && !(physicalNode instanceof ICPPASTCompositeTypeSpecifier)
				&& !(physicalNode instanceof ICPPASTNamespaceDefinition)) {
			return;
		}

		if (bindings == null)
			bindings = new CharArrayObjectMap<>(1);

		final char[] c = name.getLookupKey();
		if (c.length == 0)
			return;
		Object o = bindings.get(c);
		if (o != null) {
			if (o instanceof ObjectSet) {
				((ObjectSet<Object>) o).put(name);
			} else {
				ObjectSet<Object> temp = new ObjectSet<>(2);
				temp.put(o);
				temp.put(name);
				bindings.put(c, temp);
			}
		} else {
			bindings.put(c, name);
		}
	}

	@Override
	public IBinding getBinding(IASTName name, boolean forceResolve, IIndexFileSet fileSet) {
		final ScopeLookupData lookup = new ScopeLookupData(name, forceResolve, false);
		lookup.setIgnorePointOfDeclaration(true);
		IBinding[] bs = getBindingsInAST(lookup);
		IBinding binding = CPPSemantics.resolveAmbiguities(name, bs);
		if (binding == null && forceResolve) {
			final IASTTranslationUnit tu = name.getTranslationUnit();
			IIndex index = tu == null ? null : tu.getIndex();
			if (index != null) {
				final char[] nchars = name.getLookupKey();
				// Try looking this up in the index.
				if (physicalNode instanceof IASTTranslationUnit) {
					try {
						IBinding[] bindings = index.findBindings(nchars,
								IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE, NPM);
						if (fileSet != null) {
							bindings = fileSet.filterFileLocalBindings(bindings);
						}
						binding = CPPSemantics.resolveAmbiguities(name, bindings);
						if (binding instanceof ICPPUsingDeclaration) {
							binding = CPPSemantics.resolveAmbiguities(name,
									((ICPPUsingDeclaration) binding).getDelegates());
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				} else {
					ICPPNamespace nsbinding = getNamespaceIndexBinding(index);
					if (nsbinding != null) {
						return nsbinding.getNamespaceScope().getBinding(name, forceResolve, fileSet);
					}
				}
			}
		}
		return binding;
	}

	protected ICPPNamespace getNamespaceIndexBinding(IIndex index) {
		if (fIndexNamespace == UNINITIALIZED) {
			fIndexNamespace = null;
			IASTNode node = getPhysicalNode();
			if (node instanceof ICPPASTNamespaceDefinition) {
				IASTName nsname = ((ICPPASTNamespaceDefinition) node).getName();
				IBinding nsbinding = nsname.resolveBinding();
				if (nsbinding != null) {
					fIndexNamespace = (ICPPNamespace) index.adaptBinding(nsbinding);
				}
			}
		}
		return fIndexNamespace;
	}

	/**
	 * @deprecated Use {@link #getBindings(ScopeLookupData)} instead
	 */
	@Deprecated
	@Override
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		IBinding[] result = getBindingsInAST(lookup);
		final IASTTranslationUnit tu = lookup.getTranslationUnit();
		if (tu != null) {
			IIndex index = tu.getIndex();
			if (index != null) {
				IIndexFileSet fileSet = lookup.getIncludedFiles();
				if (physicalNode instanceof IASTTranslationUnit) {
					try {
						IndexFilter filter = IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE;
						final char[] nchars = lookup.getLookupKey();
						IBinding[] bindings = lookup.isPrefixLookup()
								? index.findBindingsForContentAssist(nchars, true, filter, null)
								: index.findBindings(nchars, filter, null);
						if (fileSet != null) {
							bindings = fileSet.filterFileLocalBindings(bindings);
						}
						result = ArrayUtil.addAll(IBinding.class, result, bindings);
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				} else if (physicalNode instanceof ICPPASTNamespaceDefinition) {
					ICPPASTNamespaceDefinition ns = (ICPPASTNamespaceDefinition) physicalNode;
					try {
						IIndexBinding binding = index.findBinding(ns.getName());
						if (binding instanceof ICPPNamespace) {
							ICPPNamespaceScope indexNs = ((ICPPNamespace) binding).getNamespaceScope();
							IBinding[] bindings = indexNs.getBindings(lookup);
							for (IBinding candidate : bindings) {
								if (lookup.isPrefixLookup()
										|| CPPSemantics.declaredBefore(candidate, lookup.getLookupPoint(), true)) {
									result = ArrayUtil.append(result, candidate);
								}
							}
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			}
		}

		return ArrayUtil.trim(IBinding.class, result);
	}

	protected boolean nameIsVisibleToLookup(ScopeLookupData lookup) {
		return true;
	}

	public IBinding[] getBindingsInAST(ScopeLookupData lookup) {
		populateCache();
		final char[] c = lookup.getLookupKey();
		IBinding[] result = IBinding.EMPTY_BINDING_ARRAY;
		if (!nameIsVisibleToLookup(lookup)) {
			return result;
		}

		Object obj = null;
		if (lookup.isPrefixLookup()) {
			char[][] keys = bindings != null ? bindings.keys() : CharArrayUtils.EMPTY_ARRAY_OF_CHAR_ARRAYS;
			ObjectSet<Object> all = new ObjectSet<>(16);
			IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher(c);
			for (char[] key : keys) {
				if (key != CONSTRUCTOR_KEY && matcher.match(key)) {
					obj = bindings.get(key);
					if (obj instanceof ObjectSet<?>) {
						all.addAll((ObjectSet<?>) obj);
					} else if (obj != null) {
						all.put(obj);
					}
				}
			}
			obj = all;
		} else {
			obj = bindings != null ? bindings.get(c) : null;
		}

		if (obj != null) {
			if (obj instanceof ObjectSet<?>) {
				ObjectSet<?> os = (ObjectSet<?>) obj;
				for (int j = 0; j < os.size(); j++) {
					result = addCandidate(os.keyAt(j), lookup, result);
				}
			} else {
				result = addCandidate(obj, lookup, result);
			}
		}
		return ArrayUtil.trim(result);
	}

	private boolean isInsideClassScope(IScope scope) {
		try {
			return scope instanceof ICPPClassScope
					|| (scope instanceof ICPPEnumScope && scope.getParent() instanceof ICPPClassScope);
		} catch (DOMException e) {
			return false;
		}
	}

	private IBinding[] addCandidate(Object candidate, ScopeLookupData lookup, IBinding[] result) {
		final IASTNode point = lookup.getLookupPoint();
		if (!lookup.isIgnorePointOfDeclaration()) {
			IASTTranslationUnit tu = point.getTranslationUnit();
			if (!CPPSemantics.declaredBefore(candidate, point, tu != null && tu.getIndex() != null)) {
				if (!isInsideClassScope(this) || !LookupData.checkWholeClassScope(lookup.getLookupName()))
					return result;
			}
		}

		IBinding binding;
		if (candidate instanceof IASTName) {
			final IASTName candName = (IASTName) candidate;
			IASTName simpleName = candName.getLastName();
			if (simpleName instanceof ICPPASTTemplateId) {
				simpleName = ((ICPPASTTemplateId) simpleName).getTemplateName();
			}
			if (lookup.isResolve() && candName != point && simpleName != point) {
				candName.resolvePreBinding(); // Make sure to resolve the template-id
				binding = simpleName.resolvePreBinding();
			} else {
				binding = simpleName.getPreBinding();
			}
		} else {
			binding = (IBinding) candidate;
		}

		if (binding != null)
			result = ArrayUtil.append(result, binding);
		return result;
	}

	@Override
	public final void populateCache() {
		if (!isCached) {
			isCached = true; // set to true before doing the work, to avoid recursion
			CPPSemantics.populateCache(this);
		}
	}

	@Override
	public void removeNestedFromCache(IASTNode container) {
		if (bindings != null) {
			removeFromMap(bindings, container);
		}
	}

	private void removeFromMap(CharArrayObjectMap<Object> map, IASTNode container) {
		for (int i = 0; i < map.size(); i++) {
			Object o = map.getAt(i);
			if (o instanceof IASTName) {
				if (container.contains((IASTNode) o)) {
					final char[] key = map.keyAt(i);
					map.remove(key, 0, key.length);
					i--;
				}
			} else if (o instanceof ObjectSet) {
				@SuppressWarnings("unchecked")
				final ObjectSet<Object> set = (ObjectSet<Object>) o;
				removeFromSet(set, container);
			}
		}
	}

	private void removeFromSet(ObjectSet<Object> set, IASTNode container) {
		for (int i = 0; i < set.size(); i++) {
			Object o = set.keyAt(i);
			if (o instanceof IASTName) {
				if (container.contains((IASTNode) o)) {
					set.remove(o);
					i--;
				}
			}
		}
	}

	@Override
	public IBinding[] find(String name, IASTTranslationUnit tu) {
		return find(name);
	}

	@Override
	public IBinding[] find(String name) {
		return CPPSemantics.findBindings(this, name, false);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public void addBinding(IBinding binding) {
		if (bindings == null)
			bindings = new CharArrayObjectMap<>(1);
		char[] c = binding.getNameCharArray();
		if (c.length == 0) {
			return;
		}
		Object o = bindings.get(c);
		if (o != null) {
			if (o instanceof ObjectSet) {
				((ObjectSet<Object>) o).put(binding);
			} else {
				ObjectSet<Object> set = new ObjectSet<>(2);
				set.put(o);
				set.put(binding);
				bindings.put(c, set);
			}
		} else {
			bindings.put(c, binding);
		}
	}

	@Override
	public final IBinding getBinding(IASTName name, boolean resolve) {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

	@Override
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
		return getBindings(new ScopeLookupData(name, resolve, prefix));
	}

	@Override
	public IName getScopeName() {
		return null;
	}

	@Override
	public String toString() {
		IName name = getScopeName();
		final String n = name != null ? name.toString() : "<unnamed scope>"; //$NON-NLS-1$
		return getKind().toString() + ' ' + n + ' ' + '(' + super.toString() + ')';
	}
}
