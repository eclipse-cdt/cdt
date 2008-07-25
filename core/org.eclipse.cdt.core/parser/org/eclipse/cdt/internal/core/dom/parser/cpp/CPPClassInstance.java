/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   IBM - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPClassInstance extends CPPClassSpecialization implements ICPPTemplateInstance {
	private IType[] arguments;

	public CPPClassInstance(IBinding owner, ICPPClassType orig, ObjectMap argMap, IType[] args) {
		super(orig, owner, argMap);
		this.arguments= args;
	}

	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}

	public IType[] getArguments() {
		return arguments;
	}

	/* (non-Javadoc)
	 * For debug purposes only
	 */
	@Override
	public String toString() {
		return getName() + " <" + ASTTypeUtil.getTypeListString(arguments) + ">"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof ICPPClassType && isSameType((ICPPClassType) obj);
	}
}
