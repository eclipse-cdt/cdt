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
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;

public class CheckHolderExpression implements IBooleanExpression {
	public static final String NAME = "checkHolder"; 	//$NON-NLS-1$

	public static final String HOLDER_ID = "holderId"; 	//$NON-NLS-1$
	
	private String fHolderId;

	public CheckHolderExpression(IManagedConfigElement element){
		fHolderId = element.getAttribute(HOLDER_ID);
	}

	
	public boolean evaluate(IBuildObject configuration, IHoldsOptions holder,
			IOption option) {
		if(fHolderId != null){
			for(; holder != null; holder = getHolderSuperClass(holder)){
				if(fHolderId.equals(holder.getId()))
					return true;
			}
			return false;
		}
		return true;
	}
	
	private IHoldsOptions getHolderSuperClass(IHoldsOptions holder){
		if(holder instanceof ITool)
			return ((ITool)holder).getSuperClass();
		else if(holder instanceof IToolChain)
			return ((IToolChain)holder).getSuperClass();
		return null;
	}

}
