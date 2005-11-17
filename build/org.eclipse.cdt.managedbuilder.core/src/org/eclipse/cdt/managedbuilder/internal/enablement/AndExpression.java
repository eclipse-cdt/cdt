/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.enablement;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;

public class AndExpression extends CompositeExpression {
	public static final String NAME = "and"; 	//$NON-NLS-1$
	
	public AndExpression(IManagedConfigElement element) {
		super(element);
	}

	public boolean evaluate(IBuildObject configuration, 
            IHoldsOptions holder, 
            IOption option) {
		IBooleanExpression children[] = getChildren();
		for(int i = 0; i < children.length; i++){
			if(!children[i].evaluate(configuration, holder, option))
				return false;
		}
		return true;
	}
}
