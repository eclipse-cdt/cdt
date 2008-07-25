/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index.composite.cpp;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeInstanceCache {
	
	public static CompositeInstanceCache getCache(ICompositesFactory cf, IIndexFragmentBinding fb) {
		final IIndexFragment frag= fb.getFragment();
		final Object key = CPPCompositesFactory.createInstanceCacheKey(cf, fb);
		Object cache= frag.getCachedResult(key);
		if (cache instanceof CompositeInstanceCache) {
			return (CompositeInstanceCache) cache;
		}
		
		CompositeInstanceCache newCache= new CompositeInstanceCache();
		newCache.populate(cf, fb);
		
		cache= frag.getCachedResult(key);
		if (cache instanceof CompositeInstanceCache) {
			return (CompositeInstanceCache) cache;
		}
		frag.putCachedResult(key, newCache);
		return newCache;
	}
	
	private final ArrayList<Object> fList;

	public CompositeInstanceCache() {
		fList= new ArrayList<Object>();
	}
	
	synchronized public final void addInstance(IType[] arguments, ICPPTemplateInstance instance) {
		fList.add(arguments);
		fList.add(instance);
	}

	synchronized public final ICPPTemplateInstance getInstance(IType[] arguments) {
		loop: for (int i=0; i < fList.size(); i+=2) {
			final IType[] args = (IType[]) fList.get(i);
			if (args.length == arguments.length) {
				for (int j=0; j < args.length; j++) {
					if (!CPPTemplates.isSameTemplateArgument(args[j], arguments[j])) {
						continue loop;
					}
				}
				return (ICPPTemplateInstance) fList.get(i+1);
			}
		}
		return null;
	}
	
	private void populate(ICompositesFactory cf, IIndexFragmentBinding fb) {
		if (fb instanceof ICPPInstanceCache) {
			ICPPTemplateInstance[] insts= ((ICPPInstanceCache) fb).getAllInstances();
			for (ICPPTemplateInstance ti : insts) {
				if (ti instanceof IIndexFragmentBinding) {
					ICPPTemplateInstance comp= (ICPPTemplateInstance) cf.getCompositeBinding((IIndexFragmentBinding) ti);
					fList.add(comp.getArguments());
					fList.add(comp);
				}
			}
		}
	}

	synchronized public ICPPTemplateInstance[] getAllInstances() {
		ICPPTemplateInstance[] result= new ICPPTemplateInstance[fList.size()/2];
		for (int i=0; i < fList.size(); i+=2) {
			result[i/2]= (ICPPTemplateInstance) fList.get(i+1);
		}
		return result;
	}
}
