/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import java.util.Iterator;

/**
 * Default implementation for single parameter checker of type string.
 * 
 */
public abstract class AbstractProblemParameterInfo implements
		IProblemParameterInfo {
	public static final String PARAM = "param"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemParameterInfo#getKey()
	 */
	public String getKey() {
		return PARAM;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemParameterInfo#getType()
	 */
	public ParameterType getType() {
		return ParameterType.TYPE_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemParameterInfo#getUiInfo()
	 */
	public String getUiInfo() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemParameterInfo#getLabel()
	 */
	public String getLabel() {
		return getKey();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.IProblemParameterInfo#getElement(java
	 * .lang.String)
	 */
	public IProblemParameterInfo getElement(String key) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemParameterInfo#getToolTip()
	 */
	public String getToolTip() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemParameterInfo#getIterator()
	 */
	public Iterator<IProblemParameterInfo> getIterator() {
		return null;
	}
}
