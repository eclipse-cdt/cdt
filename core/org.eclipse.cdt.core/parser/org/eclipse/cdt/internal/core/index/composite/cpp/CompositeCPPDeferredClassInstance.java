/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPDeferredClassInstance extends CompositeCPPClassType implements ICPPDeferredClassInstance {

	private ICPPScope unknownScope;

	public CompositeCPPDeferredClassInstance(ICompositesFactory cf,	ICPPClassType rbinding) {
		super(cf, rbinding);
	}

	public ICPPTemplateDefinition getTemplateDefinition() {
		ICPPTemplateDefinition preresult= ((ICPPTemplateInstance)rbinding).getTemplateDefinition();
		return (ICPPTemplateDefinition) cf.getCompositeBinding((IIndexFragmentBinding)preresult);
	}
	
	@Override
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}
		
	public IType[] getArguments() { return TemplateInstanceUtil.getArguments(cf, (ICPPTemplateInstance) rbinding);	}
	public ObjectMap getArgumentMap() {	return TemplateInstanceUtil.getArgumentMap(cf, rbinding); }
	public IBinding getSpecializedBinding() { return TemplateInstanceUtil.getSpecializedBinding(cf, rbinding); }

	public IASTName getUnknownName() {
		return ((ICPPUnknownClassType) rbinding).getUnknownName();
	}

	public ICPPBinding getContainerBinding() {
		ICPPBinding scopeBinding= ((ICPPUnknownClassType) rbinding).getContainerBinding();
		return (ICPPBinding) cf.getCompositeBinding((IIndexFragmentBinding)scopeBinding);
	}

	public ICPPScope getUnknownScope() throws DOMException {
		if (unknownScope == null) {
			final ICPPClassTemplate classTemplate= (ICPPClassTemplate) getTemplateDefinition();
			if (classTemplate.getPartialSpecializations().length == 0) {
				unknownScope= new CompositeCPPClassSpecializationScope(cf, rbinding);
			}
			else {
				unknownScope= new CompositeCPPUnknownScope(this, getUnknownName());
			}
		}
		return unknownScope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPUnknownBinding#resolvePartially(org.eclipse.cdt.core.dom.ast.cpp.ICPPUnknownClassBinding, org.eclipse.cdt.core.parser.util.ObjectMap)
	 */
	public IBinding resolvePartially(ICPPUnknownBinding parentBinding, ObjectMap argMap) {
		IType[] arguments = getArguments();
		
		IType [] newArgs = new IType[ arguments.length ];
		int size = arguments.length;
		for( int i = 0; i < size; i++ ){
			newArgs[i] = CPPTemplates.instantiateType( arguments[i], argMap );
		}
		
		return ((ICPPInternalTemplateInstantiator)getTemplateDefinition()).instantiate( newArgs );
	}
}
