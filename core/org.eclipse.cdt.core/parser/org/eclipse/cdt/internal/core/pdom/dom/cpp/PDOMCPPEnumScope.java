/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.CharArrayMap;
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
class PDOMCPPEnumScope implements ICPPScope, IIndexScope {
	private final IPDOMCPPEnumType fBinding;

	public PDOMCPPEnumScope(IPDOMCPPEnumType binding) {
		fBinding= binding;
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
		return getBindings(name, resolve, prefixLookup, null);
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		try {
			CharArrayMap<PDOMCPPEnumerator> map= getBindingMap(fBinding);
			return map.get(name.toCharArray());
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		try {
			CharArrayMap<PDOMCPPEnumerator> map= getBindingMap(fBinding);
			if (prefixLookup) {
				final List<IBinding> result= new ArrayList<IBinding>();
				final char[] nc= name.toCharArray();
				IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher(nc);
				for (char[] key : map.keys()) {
					if (matcher.match(key)) {
						result.add(map.get(key));
					}
				}
				return result.toArray(new IBinding[result.size()]);
			} 
			IBinding b= map.get(name.toCharArray());
			if (b != null) {
				return new IBinding[] {b};
			}
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
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

	private static CharArrayMap<PDOMCPPEnumerator> getBindingMap(IPDOMCPPEnumType enumeration) throws CoreException {
		final Long key= enumeration.getRecord() + PDOMCPPLinkage.CACHE_MEMBERS;
		final PDOM pdom = enumeration.getPDOM();
		@SuppressWarnings("unchecked")
		Reference<CharArrayMap<PDOMCPPEnumerator>> cached= (Reference<CharArrayMap<PDOMCPPEnumerator>>) pdom.getCachedResult(key);
		CharArrayMap<PDOMCPPEnumerator> map= cached == null ? null : cached.get();

		if (map == null) {
			// there is no cache, build it:
			map= new CharArrayMap<PDOMCPPEnumerator>();
			enumeration.loadEnumerators(map);
			pdom.putCachedResult(key, new SoftReference<CharArrayMap<?>>(map));
		}
		return map;
	}

	public static void updateCache(PDOMCPPEnumeration enumType, PDOMCPPEnumerator enumItem) {
		final Long key= enumType.getRecord() + PDOMCPPLinkage.CACHE_MEMBERS;
		final PDOM pdom = enumType.getPDOM();
		@SuppressWarnings("unchecked")
		Reference<CharArrayMap<PDOMCPPEnumerator>> cached= (Reference<CharArrayMap<PDOMCPPEnumerator>>) pdom.getCachedResult(key);
		CharArrayMap<PDOMCPPEnumerator> map= cached == null ? null : cached.get();
		if (map != null) {
			map.put(enumType.getNameCharArray(), enumItem);
		}
	}

	public static IEnumerator[] getEnumerators(PDOMCPPEnumeration enumType) {
		try {
			CharArrayMap<PDOMCPPEnumerator> map = getBindingMap(enumType);
			List<IEnumerator> result= new ArrayList<IEnumerator>();
			for (IEnumerator value : map.values()) {
				if (IndexFilter.ALL_DECLARED.acceptBinding(value)) {
					result.add(value);
				}
			}
			Collections.reverse(result);
			return result.toArray(new IEnumerator[result.size()]);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return new IEnumerator[0];
	}

	public static void acceptViaCache(PDOMCPPEnumeration enumType, IPDOMVisitor visitor) {
		try {
			CharArrayMap<PDOMCPPEnumerator> map = getBindingMap(enumType);
			for (PDOMCPPEnumerator enumItem : map.values()) {
				visitor.visit(enumItem);
				visitor.leave(enumItem);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
}
