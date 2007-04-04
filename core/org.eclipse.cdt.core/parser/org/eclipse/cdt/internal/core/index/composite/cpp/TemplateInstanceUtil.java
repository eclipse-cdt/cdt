/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

/**
 * For implementation re-use in the absence of multiple inheritance
 */
public class TemplateInstanceUtil {
	public static ObjectMap getArgumentMap(ICompositesFactory cf, IIndexBinding rbinding) {
		ObjectMap preresult= ((ICPPSpecialization)rbinding).getArgumentMap();
		ObjectMap result= new ObjectMap(preresult.size());
		Object[] keys= preresult.keyArray();
		for(int i=0; i<keys.length; i++) {
			IType type= (IType) preresult.get(keys[i]);
			result.put(
					cf.getCompositeBinding((IIndexFragmentBinding)keys[i]),
					cf.getCompositeBinding((IIndexFragmentBinding)type));
		}
		return result;
	}

	public static  IBinding getSpecializedBinding(ICompositesFactory cf, IIndexBinding rbinding) {
		IBinding preresult= ((ICPPSpecialization)rbinding).getSpecializedBinding();
		return cf.getCompositeBinding((IIndexFragmentBinding)preresult);
	}
	

	public static  IType[] getArguments(ICompositesFactory cf, IIndexBinding rbinding) {
		IType[] result = ((ICPPTemplateInstance)rbinding).getArguments();
		try {
			for(int i=0; i<result.length; i++) {
				result[i] = cf.getCompositeType((IIndexType)result[i]);
			}
		} catch(DOMException de) {
			CCorePlugin.log(de);
		}
		return result;
	}
	
	public static ICPPTemplateDefinition getTemplateDefinition(ICompositesFactory cf, IIndexBinding rbinding) {
		ICPPTemplateDefinition preresult= ((ICPPTemplateInstance)rbinding).getTemplateDefinition();
		return (ICPPTemplateDefinition) cf.getCompositeBinding((IIndexFragmentBinding)preresult);
	}
}
