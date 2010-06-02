/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
			IStringVariable variable = (IStringVariable)element;
			return variable.getName();
		}
		return super.getText(element);
	}

}