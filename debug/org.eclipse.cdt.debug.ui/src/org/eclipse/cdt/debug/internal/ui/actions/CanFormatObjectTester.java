/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.core.model.IWatchExpression;

/**
 * Property test for object in selection.
 * property: canFormatObject - if object is C/C++ formattable
 * @author elaskavaia
 *
 */
public class CanFormatObjectTester extends PropertyTester {

	public CanFormatObjectTester() {

	}

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (property.equals("canFormatObject")) { //$NON-NLS-1$
			if (receiver instanceof ICVariable) { return expectedValue == Boolean.TRUE; }
			if (receiver instanceof IWatchExpression) {
				IWatchExpression w = (IWatchExpression) receiver;
				if (w.getValue() instanceof ICValue) 
					return expectedValue == Boolean.TRUE;
			}
			return expectedValue == Boolean.FALSE;
		}
		return false;
	}

}
