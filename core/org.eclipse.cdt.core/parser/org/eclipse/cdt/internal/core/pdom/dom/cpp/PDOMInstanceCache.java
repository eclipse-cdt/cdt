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
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.NamedNodeCollector;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

public class PDOMInstanceCache {
	
	public static PDOMInstanceCache getCache(PDOMBinding binding) {
		final PDOM pdom= binding.getPDOM();
		final int record= binding.getRecord();
		final Integer key = record+PDOMCPPLinkage.CACHE_INSTANCES;
		Object cache= pdom.getCachedResult(key);
		if (cache instanceof PDOMInstanceCache) {
			return (PDOMInstanceCache) cache;
		}
		
		PDOMInstanceCache newCache= new PDOMInstanceCache();
		try {
			newCache.populate(binding);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		
		cache= pdom.getCachedResult(key);
		if (cache instanceof PDOMInstanceCache) {
			return (PDOMInstanceCache) cache;
		}
		pdom.putCachedResult(key, newCache);
		return newCache;
	}
	
	private final ArrayList<Object> fList;

	public PDOMInstanceCache() {
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
	
	private void populate(PDOMBinding binding) throws CoreException {
		PDOMNode parent= binding.getParentNode();
		if (parent == null) {
			parent= binding.getLinkage();
		}
		NamedNodeCollector nn= new NamedNodeCollector(binding.getLinkage(), binding.getNameCharArray());
		parent.accept(nn);
		PDOMNamedNode[] nodes= nn.getNodes();
		for (PDOMNamedNode node : nodes) {
			if (node instanceof ICPPTemplateInstance) {
				ICPPTemplateInstance inst= (ICPPTemplateInstance) node;
				if (binding.equals(inst.getTemplateDefinition())) {
					IType[] args= inst.getArguments();
					fList.add(args);
					fList.add(inst);
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
