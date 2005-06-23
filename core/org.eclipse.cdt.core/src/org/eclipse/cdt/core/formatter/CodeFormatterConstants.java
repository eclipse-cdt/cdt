/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.formatter;

import org.eclipse.cdt.core.CCorePlugin;

/**
 */
public class CodeFormatterConstants {
	

	/**
	 * <pre>
	 * FORMATTER / Option for alignment of arguments in allocation expression
	 *     - option id:         "org.eclipse.jdt.core.formatter.language"
	 *     - possible values:   values proposed in class <code>ParserLanguage</code> 
	 *     - default:           ParserLanguage.CPP
	 * </pre>
	 */
	public static final String FORMATTER_LANGUAGE = CCorePlugin.PLUGIN_ID + ".formatter.language";	 //$NON-NLS-1$
	
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of arguments in allocation expression
	 *     - option id:         "org.eclipse.jdt.core.formatter.current_file"
	 *     - possible values:   object of class <code>IFile</code> or <code>null</code> 
	 *     - default:           null
	 * </pre>
	 */
	public static final String FORMATTER_CURRENT_FILE = CCorePlugin.PLUGIN_ID + ".formatter.current_file";	 //$NON-NLS-1$
	
	

}
