/**********************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Doug Schaefer (QNX Software Systems) - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.rules.RuleBasedScanner;

/**
 * Adapter interface to {@link org.eclipse.cdt.core.model.ILanguage ILanguage} for language extensions to
 * provide a custom code scanner implementation.
 * <p>
 * Clients may implement this interface.
 * </p>
 */
public interface ILanguageUI extends IAdaptable {

	/**
	 * Get the code scanner that drives coloring in the editor.
	 * 
	 * @return code scanner for this language
	 */
	RuleBasedScanner getCodeScanner();
	
}
