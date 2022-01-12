/*******************************************************************************
 * Copyright (c) 2010, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents the enum scope for an enum stored in the index.
 * For safe use all fields need to be final.
 */
class PDOMCPPEnumScope implements ICPPEnumScope, IIndexScope {
	private final IPDOMCPPEnumType fBinding;

	public PDOMCPPEnumScope(IPDOMCPPEnumType binding) {
		fBinding = binding;
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eEnumeration;
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
			CharArrayObjectMap<IPDOMCPPEnumerator> map = getBindingMap(fBinding);
			return map.get(name.toCharArray());
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	@Deprecated
	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		try {
			CharArrayObjectMap<IPDOMCPPEnumerator> map = getBindingMap(fBinding);
			if (lookup.isPrefixLookup()) {
				final List<IBinding> result = new ArrayList<>();
				final char[] nc = lookup.getLookupKey();
				IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher(nc);
				for (char[] key : map.keys()) {
					if (matcher.match(key)) {
						result.add(map.get(key));
					}
				}
				return result.toArray(new IBinding[result.size()]);
			}
			IBinding b = map.get(lookup.getLookupKey());
			if (b != null) {
				return new IBinding[] { b };
			}
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
		return IBinding.EMPTY_BINDING_ARRAY;
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
	public IIndexScope getParent() {
		return fBinding.getScope();
	}

	@Override
	public IIndexName getScopeName() {
		return fBinding.getScopeName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PDOMCPPEnumScope)
			return fBinding.equals(((PDOMCPPEnumScope) obj).fBinding);
		return false;
	}

	@Override
	public int hashCode() {
		return fBinding.hashCode();
	}

	private static CharArrayObjectMap<IPDOMCPPEnumerator> getBindingMap(IPDOMCPPEnumType enumeration)
			throws CoreException {
		final Long key = enumeration.getRecord() + PDOMCPPLinkage.CACHE_MEMBERS;
		final PDOM pdom = enumeration.getPDOM();
		@SuppressWarnings("unchecked")
		Reference<CharArrayObjectMap<IPDOMCPPEnumerator>> cached = (Reference<CharArrayObjectMap<IPDOMCPPEnumerator>>) pdom
				.getCachedResult(key);
		CharArrayObjectMap<IPDOMCPPEnumerator> map = cached == null ? null : cached.get();

		if (map == null) {
			// there is no cache, build it:
			List<IPDOMCPPEnumerator> enumerators = new ArrayList<>();
			enumeration.loadEnumerators(enumerators);
			map = new CharArrayObjectMap<>(enumerators.size());
			for (IPDOMCPPEnumerator enumerator : enumerators) {
				map.put(enumerator.getNameCharArray(), enumerator);
			}
			pdom.putCachedResult(key, new SoftReference<>(map));
		}
		return map;
	}

	public static void updateCache(IPDOMCPPEnumType enumType, IPDOMCPPEnumerator enumItem) {
		final Long key = enumType.getRecord() + PDOMCPPLinkage.CACHE_MEMBERS;
		final PDOM pdom = enumType.getPDOM();
		@SuppressWarnings("unchecked")
		Reference<CharArrayObjectMap<IPDOMCPPEnumerator>> cached = (Reference<CharArrayObjectMap<IPDOMCPPEnumerator>>) pdom
				.getCachedResult(key);
		CharArrayObjectMap<IPDOMCPPEnumerator> map = cached == null ? null : cached.get();
		if (map != null) {
			map.put(enumItem.getNameCharArray(), enumItem);
		}
	}

	public static IEnumerator[] getEnumerators(IPDOMCPPEnumType enumType) {
		try {
			// We want to return the enumerators in order of declaration, so we don't
			// use the cache (getBindingsMap()) which stores them in a hash map and thus
			// loses the order.
			List<IPDOMCPPEnumerator> enumerators = new ArrayList<>();
			enumType.loadEnumerators(enumerators);
			List<IEnumerator> result = new ArrayList<>();
			for (IEnumerator value : enumerators) {
				if (IndexFilter.ALL_DECLARED.acceptBinding(value)) {
					result.add(value);
				}
			}
			return result.toArray(new IEnumerator[result.size()]);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return new IEnumerator[0];
	}

	public static void acceptViaCache(IPDOMCPPEnumType enumType, IPDOMVisitor visitor) {
		try {
			CharArrayObjectMap<IPDOMCPPEnumerator> map = getBindingMap(enumType);
			for (IPDOMCPPEnumerator enumItem : map.values()) {
				visitor.visit(enumItem);
				visitor.leave(enumItem);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public ICPPEnumeration getEnumerationType() {
		return (ICPPEnumeration) getScopeBinding();
	}
}
