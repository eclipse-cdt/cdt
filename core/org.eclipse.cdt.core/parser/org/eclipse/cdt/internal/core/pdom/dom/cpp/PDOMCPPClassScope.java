/*******************************************************************************
 * Copyright (c) 2005, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.DeclaredBindingsFilter;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.BindingCollector;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents the class scope for a class stored in the index.
 * For safe use, all fields need to be final.
 */
class PDOMCPPClassScope implements ICPPClassScope, IIndexScope {
	private static final class PopulateMap implements IPDOMVisitor {
		private final CharArrayObjectMap<List<PDOMBinding>> fResult;

		private PopulateMap(CharArrayObjectMap<List<PDOMBinding>> result) {
			fResult = result;
		}

		@Override
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof PDOMBinding) {
				final PDOMBinding binding = (PDOMBinding) node;
				final char[] nchars = binding.getNameCharArray();
				List<PDOMBinding> list = fResult.get(nchars);
				if (list == null) {
					list = new ArrayList<>();
					fResult.put(nchars, list);
				}
				list.add(binding);
				if (binding instanceof ICompositeType && ((ICompositeType) binding).isAnonymous()) {
					return true; // visit children
				}
			}
			return false;
		}

		@Override
		public void leave(IPDOMNode node) {
		}
	}

	private static final IndexFilter CONVERSION_FILTER = new DeclaredBindingsFilter(ILinkage.CPP_LINKAGE_ID, true,
			false) {
		@Override
		public boolean acceptBinding(IBinding binding) throws CoreException {
			return binding instanceof ICPPMethod && SemanticUtil.isConversionOperator(((ICPPMethod) binding))
					&& super.acceptBinding(binding);
		}
	};

	private final IPDOMCPPClassType fBinding;

	public PDOMCPPClassScope(IPDOMCPPClassType binding) {
		fBinding = binding;
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

	@Override
	public ICPPClassType getClassType() {
		return fBinding;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve) {
		return getBinding(name, resolve, null);
	}

	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		try {
			final char[] nameChars = name.getSimpleID();
			if (CharArrayUtils.equals(fBinding.getNameCharArray(), nameChars)) {
				// 9.2 ... The class-name is also inserted into the scope of the class itself
				return getClassNameBinding();
			}

			final IBinding[] candidates = getBindingsViaCache(fBinding, nameChars,
					IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE);
			return CPPSemantics.resolveAmbiguities(name, candidates);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	private IBinding getClassNameBinding() {
		return fBinding;
	}

	@Deprecated
	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		try {
			if (lookup.getLookupName() instanceof ICPPASTConversionName) {
				BindingCollector visitor = new BindingCollector(fBinding.getLinkage(), Keywords.cOPERATOR,
						CONVERSION_FILTER, true, false, true);
				acceptViaCache(fBinding, visitor, true);
				return visitor.getBindings();
			}

			final char[] nameChars = lookup.getLookupKey();
			if (!lookup.isPrefixLookup()) {
				if (CharArrayUtils.equals(fBinding.getNameCharArray(), nameChars)) {
					if (CPPClassScope.shallReturnConstructors(lookup.getLookupName(), false)) {
						return fBinding.getConstructors();
					}
					return new IBinding[] { getClassNameBinding() };
				}
				return getBindingsViaCache(fBinding, nameChars, IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE);
			}

			// Prefix lookup.
			BindingCollector visitor = new BindingCollector(fBinding.getLinkage(), nameChars,
					IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE, true, true, false);
			if (ContentAssistMatcherFactory.getInstance().match(nameChars, fBinding.getNameCharArray())) {
				// Add the class itself, constructors will be found during the visit.
				visitor.visit((IPDOMNode) getClassNameBinding());
			}
			acceptViaCache(fBinding, visitor, true);
			return visitor.getBindings();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public static IBinding[] getBindingsViaCache(IPDOMCPPClassType ct, final char[] name, IndexFilter filter)
			throws CoreException {
		CharArrayObjectMap<List<PDOMBinding>> map = getBindingMap(ct);
		List<PDOMBinding> cached = map.get(name);
		if (cached == null)
			return IBinding.EMPTY_BINDING_ARRAY;

		int i = 0;
		IBinding[] result = new IBinding[cached.size()];
		for (IBinding binding : cached) {
			if (filter.acceptBinding(binding)) {
				result[i++] = binding;
			}
		}
		if (i == result.length)
			return result;

		final IBinding[] bresult = new IBinding[i];
		System.arraycopy(result, 0, bresult, 0, i);
		return bresult;
	}

	/**
	 * Visit bindings via the cache.
	 */
	public static void acceptViaCache(IPDOMCPPClassType ct, IPDOMVisitor visitor, boolean includeNestedInAnonymous)
			throws CoreException {
		final long record = ct.getRecord();
		CharArrayObjectMap<List<PDOMBinding>> map = getBindingMap(ct);
		for (List<PDOMBinding> list : map.values()) {
			for (PDOMBinding node : list) {
				if (includeNestedInAnonymous || node.getParentNodeRec() == record) {
					if (visitor.visit(node)) {
						node.accept(visitor);
					}
					visitor.leave(node);
				}
			}
		}
	}

	public static void updateCache(IPDOMCPPClassType ct, PDOMNode member) throws CoreException {
		if (member instanceof PDOMBinding) {
			final Long key = ct.getRecord() + PDOMCPPLinkage.CACHE_MEMBERS;
			final PDOM pdom = ct.getPDOM();
			@SuppressWarnings("unchecked")
			Reference<CharArrayObjectMap<List<PDOMBinding>>> cached = (Reference<CharArrayObjectMap<List<PDOMBinding>>>) pdom
					.getCachedResult(key);
			CharArrayObjectMap<List<PDOMBinding>> map = cached == null ? null : cached.get();
			if (map != null) {
				new PopulateMap(map).visit(member);
			}
		}
	}

	public static CharArrayObjectMap<List<PDOMBinding>> getBindingMap(IPDOMCPPClassType ct) throws CoreException {
		final Long key = ct.getRecord() + PDOMCPPLinkage.CACHE_MEMBERS;
		final PDOM pdom = ct.getPDOM();
		@SuppressWarnings("unchecked")
		Reference<CharArrayObjectMap<List<PDOMBinding>>> cached = (Reference<CharArrayObjectMap<List<PDOMBinding>>>) pdom
				.getCachedResult(key);
		CharArrayObjectMap<List<PDOMBinding>> map = cached == null ? null : cached.get();

		if (map == null) {
			// There is no cache, build it:
			map = new CharArrayObjectMap<>(8);
			IPDOMVisitor visitor = new PopulateMap(map);
			visitor.visit(ct);
			ct.acceptUncached(visitor);
			pdom.putCachedResult(key, new SoftReference<>(map));
		}
		return map;
	}

	@Override
	public IBinding[] find(String name, IASTTranslationUnit tu) {
		return CPPSemantics.findBindingsInScope(this, name, tu);
	}

	@Override
	@Deprecated
	public IBinding[] find(String name) {
		return CPPSemantics.findBindings(this, name, false);
	}

	@Override
	public IIndexBinding getScopeBinding() {
		return fBinding;
	}

	@Override
	public ICPPMethod[] getImplicitMethods() {
		try {
			PDOMClassUtil.MethodCollector methods = new PDOMClassUtil.MethodCollector(true, false);
			acceptViaCache(fBinding, methods, false);
			return methods.getMethods();
		} catch (CoreException e) {
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		}
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		return fBinding.getConstructors();
	}

	@Override
	public IIndexScope getParent() {
		return fBinding.getScope();
	}

	@Override
	public IIndexName getScopeName() {
		return fBinding.getScopeName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PDOMCPPClassScope)
			return fBinding.equals(((PDOMCPPClassScope) obj).fBinding);
		return false;
	}

	@Override
	public int hashCode() {
		return fBinding.hashCode();
	}
}
