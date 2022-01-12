/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.wizards.indexwizards;

import org.eclipse.core.variables.IStringVariable;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Copied from org.eclipse.debug.ui
 * @since 4.0
 */
public class StringVariableLabelProvider extends LabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IStringVariable) {
			IStringVariable variable = (IStringVariable) element;
			return variable.getName();
		}
		return super.getText(element);
	}

}