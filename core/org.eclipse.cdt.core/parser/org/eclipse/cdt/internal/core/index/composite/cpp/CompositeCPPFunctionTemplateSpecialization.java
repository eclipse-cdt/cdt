/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Systems and others.
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

import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPFunctionTemplateSpecialization	extends CompositeCPPFunctionSpecialization
		implements ICPPFunctionTemplate, ICPPInstanceCache {

	public CompositeCPPFunctionTemplateSpecialization(ICompositesFactory cf, ICPPFunction ft) {
		super(cf, ft);
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return TemplateInstanceUtil.convert(cf, ((ICPPFunctionTemplate) rbinding).getTemplateParameters());
	}

	@Override
	public ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		return CompositeInstanceCache.getCache(cf, rbinding).getInstance(arguments);	
	}

	@Override
	public void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		CompositeInstanceCache.getCache(cf, rbinding).addInstance(arguments, instance);	
	}

	@Override
	public ICPPTemplateInstance[] getAllInstances() {
		return CompositeInstanceCache.getCache(cf, rbinding).getAllInstances();
	}
}
