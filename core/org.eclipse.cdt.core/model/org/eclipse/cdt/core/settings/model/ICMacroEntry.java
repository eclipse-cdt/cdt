/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

/**
 * Representation in the project model of language settings entries having
 * name-value attributes such as preprocessor defines (-D).
 * See {@link ICSettingEntry#MACRO}.
 */
public interface ICMacroEntry extends ICLanguageSettingEntry {
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingEntry#getValue()
	 */
	@Override
	String getValue();
}
