/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

/**
 * Value of the problem preference. If more than one it can be composite, i.e.
 * map
 */
public interface IProblemPreferenceValue extends Cloneable {
	/**
	 * Get value of parameter if it is basic type.
	 * 
	 * @param key
	 * @return
	 */
	Object getValue();

	void setValue(Object value);

	String exportValue();

	void importValue(String str);
}
