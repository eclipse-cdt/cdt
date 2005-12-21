/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * @author Doug Schaefer
 * This factory adapts IBinding object to PDOMBinding object
 */
public class PDOMBindingAdapterFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof PDOMBinding)
			return adaptableObject;
		
		try {
			IBinding binding = (IBinding)adaptableObject;
			IScope scope = binding.getScope();
			IPDOM ipdom = scope.getPhysicalNode().getTranslationUnit().getIndex();
			if (ipdom == null)
				return null;
			PDOMDatabase pdom = (PDOMDatabase)ipdom;
			
			for (PDOMLinkage linkage = pdom.getFirstLinkage(); linkage != null; linkage = linkage.getNextLinkage()) {
				PDOMBinding pdomBinding = linkage.adaptBinding(binding);
				if (binding != null)
					return pdomBinding;
			}
			return null;
		} catch (DOMException e) {
			CCorePlugin.log(e);
			return null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	private static Class[] adapterList = {
		PDOMBinding.class
	};
	
	public Class[] getAdapterList() {
		return adapterList;
	}

}
