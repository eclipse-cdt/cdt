/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import java.util.HashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.core.runtime.CoreException;

public class CompositeInstanceCache {

	public static CompositeInstanceCache getCache(ICompositesFactory cf, IIndexFragmentBinding fb) {
		final IIndexFragment frag = fb.getFragment();
		final Object key = CPPCompositesFactory.createInstanceCacheKey(cf, fb);
		Object cache = frag.getCachedResult(key);
		if (cache != null) {
			return (CompositeInstanceCache) cache;
		}

		CompositeInstanceCache newCache = new CompositeInstanceCache();
		newCache.populate(cf, fb);
		return (CompositeInstanceCache) frag.putCachedResult(key, newCache, false);
	}

	private final HashMap<String, ICPPTemplateInstance> fMap;
	private ICPPDeferredClassInstance fDeferredInstance;

	public CompositeInstanceCache() {
		fMap = new HashMap<>();
	}

	synchronized public final void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		try {
			String key = IndexCPPSignatureUtil.getTemplateArgString(arguments, true);
			fMap.put(key, instance);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (DOMException e) {
		}
	}

	synchronized public final ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		try {
			String key = IndexCPPSignatureUtil.getTemplateArgString(arguments, true);
			return fMap.get(key);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (DOMException e) {
		}
		return null;
	}

	private void addInstancesFrom(ICompositesFactory cf, ICPPInstanceCache cache) {
		ICPPTemplateInstance[] insts = cache.getAllInstances();
		for (ICPPTemplateInstance ti : insts) {
			if (ti instanceof IIndexFragmentBinding) {
				ICPPTemplateInstance comp = (ICPPTemplateInstance) cf.getCompositeBinding((IIndexFragmentBinding) ti);
				ICPPTemplateArgument[] args = comp.getTemplateArguments();
				addInstance(args, comp);
			}
		}
	}

	private void populate(ICompositesFactory cf, IIndexFragmentBinding fb) {
		if (fb instanceof ICPPInstanceCache) {
			addInstancesFrom(cf, (ICPPInstanceCache) fb);
		}

		// Also add instanced cached in other fragments.
		CIndex index = (CIndex) ((CPPCompositesFactory) cf).getContext();
		try {
			IIndexFragmentBinding[] fragmentBindings = index.findEquivalentBindings(fb);
			for (IIndexFragmentBinding fragmentBinding : fragmentBindings) {
				if (fragmentBinding instanceof ICPPInstanceCache) {
					addInstancesFrom(cf, (ICPPInstanceCache) fragmentBinding);
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	synchronized public ICPPTemplateInstance[] getAllInstances() {
		return fMap.values().toArray(new ICPPTemplateInstance[fMap.size()]);
	}

	public ICPPDeferredClassInstance getDeferredInstance() {
		return fDeferredInstance;
	}

	public void putDeferredInstance(ICPPDeferredClassInstance dci) {
		fDeferredInstance = dci;
	}
}
