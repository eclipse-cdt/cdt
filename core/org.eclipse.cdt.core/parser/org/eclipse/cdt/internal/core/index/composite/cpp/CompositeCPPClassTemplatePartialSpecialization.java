/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.core.runtime.CoreException;

public class CompositeCPPClassTemplatePartialSpecialization extends CompositeCPPClassTemplate
		implements ICPPClassTemplatePartialSpecialization, IPDOMOverloader {
	public CompositeCPPClassTemplatePartialSpecialization(ICompositesFactory cf,
			ICPPClassTemplatePartialSpecialization delegate) {
		super(cf, delegate);
	}

	@Override
	public ICPPClassTemplate getPrimaryClassTemplate() {
		ICPPClassTemplate preresult = ((ICPPClassTemplatePartialSpecialization) rbinding).getPrimaryClassTemplate();
		return (ICPPClassTemplate) cf.getCompositeBinding((IIndexFragmentBinding) preresult);
	}

	@Override
	public int getSignatureHash() throws CoreException {
		return ((IPDOMOverloader) rbinding).getSignatureHash();
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return TemplateInstanceUtil.getTemplateArguments(cf, (ICPPClassTemplatePartialSpecialization) rbinding);
	}

	@Override
	public ICPPTemplateDefinition getPrimaryTemplate() {
		return getPrimaryClassTemplate();
	}
}
