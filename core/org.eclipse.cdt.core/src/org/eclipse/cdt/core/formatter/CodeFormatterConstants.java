/*
 * Created on Sep 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.core.formatter;

import org.eclipse.cdt.core.CCorePlugin;

/**
 * @author Alex Chapiro
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
