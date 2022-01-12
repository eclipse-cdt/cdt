/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Miwako Tokugawa (Intel Corporation) - bug 222817 (OptionCategoryApplicability)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.enablement;

import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;

public class OrExpression extends CompositeExpression {
	public static final String NAME = "or"; //$NON-NLS-1$

	public OrExpression(IManagedConfigElement element) {
		super(element);
	}

	@Override
	public boolean evaluate(IResourceInfo rcInfo, IHoldsOptions holder, IOption option) {
		IBooleanExpression children[] = getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i].evaluate(rcInfo, holder, option))
				return true;
		}
		return false;
	}

	@Override
	public boolean evaluate(IResourceInfo rcInfo, IHoldsOptions holder, IOptionCategory category) {
		IBooleanExpression children[] = getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i].evaluate(rcInfo, holder, category))
				return true;
		}
		return false;
	}
}
