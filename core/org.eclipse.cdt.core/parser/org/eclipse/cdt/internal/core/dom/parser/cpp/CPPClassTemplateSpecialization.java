/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * Represents a specialization of a class-template
 */
public class CPPClassTemplateSpecialization extends CPPClassSpecialization
		implements ICPPClassTemplate, ICPPInternalClassTemplate {

	private ObjectMap instances = null;

	public CPPClassTemplateSpecialization(ICPPClassTemplate orig, ICPPClassType owner, ICPPTemplateParameterMap argumentMap) {
		super(orig, owner, argumentMap);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate#getPartialSpecializations()
	 */
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() throws DOMException {
		return ((ICPPClassTemplate) getSpecializedBinding()).getPartialSpecializations();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition#getTemplateParameters()
	 */
	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		ICPPClassTemplate template = (ICPPClassTemplate) getSpecializedBinding();
		return template.getTemplateParameters();
	}

	public synchronized final void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		if (instances == null)
			instances = new ObjectMap(2);
		instances.put(arguments, instance);
	}

	public synchronized final ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		if (instances != null) {
			loop: for (int i=0; i < instances.size(); i++) {
				ICPPTemplateArgument[] args = (ICPPTemplateArgument[]) instances.keyAt(i);
				if (args.length == arguments.length) {
					for (int j=0; j < args.length; j++) {
						if (!args[j].isSameValue(arguments[j])) {
							continue loop;
						}
					}
					return (ICPPTemplateInstance) instances.getAt(i);
				}
			}
		}
		return null;
	}

	public synchronized ICPPTemplateInstance[] getAllInstances() {
		if (instances != null) {
			ICPPTemplateInstance[] result= new ICPPTemplateInstance[instances.size()];
			for (int i=0; i < instances.size(); i++) {
				result[i]= (ICPPTemplateInstance) instances.getAt(i);
			}
			return result;
		}
		return ICPPTemplateInstance.EMPTY_TEMPLATE_INSTANCE_ARRAY;
	}

	public void addPartialSpecialization(ICPPClassTemplatePartialSpecialization spec) {
		//should not occur mstodo- why not??
	}

	@Override
	public String toString() {
		return getName();
	}
}
