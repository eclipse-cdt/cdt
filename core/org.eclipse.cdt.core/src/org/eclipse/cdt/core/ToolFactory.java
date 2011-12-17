/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.util.Map;

import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.internal.formatter.CCodeFormatter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ToolFactory {

	/**
	 * Create an instance of a code formatter. A code formatter implementation can be contributed via the
	 * extension point "org.eclipse.cdt.core.CodeFormatter". If unable to find a registered extension, the factory
	 * will default to using the default code formatter.
	 * @param options - the options map to use for formatting with the code formatter. Recognized options
	 * 	are documented on <code>DefaultCodeFormatterConstants</code>. If set to <code>null</code>, then use
	 * 	the current settings from <code>CCorePlugin.getOptions()</code>.
	 * @return an instance of either a contributed the built-in code formatter
	 * @see CodeFormatter
	 * @see DefaultCodeFormatterConstants
	 * @see CCorePlugin#getOptions()
	 */
	public static CodeFormatter createCodeFormatter(Map<String, ?> options){
		if (options == null)
			options = CCorePlugin.getOptions();
		String formatterID = (String)options.get(CCorePreferenceConstants.CODE_FORMATTER);
		String extID = CCorePlugin.FORMATTER_EXTPOINT_ID;
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, extID);
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			for (int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
				for (int j = 0; j < configElements.length; j++){
					String initializerID = configElements[j].getAttribute("id"); //$NON-NLS-1$
					if (initializerID != null && initializerID.equals(formatterID)){
						try {
							Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							if (execExt instanceof CodeFormatter){
								CodeFormatter formatter = (CodeFormatter) execExt;
								formatter.setOptions(options);
								return formatter;
							}
						} catch (CoreException e) {
							CCorePlugin.log(e.getStatus());
							break;
						}
					}
				}
			}
		}
		return createDefaultCodeFormatter(options);
	}

	/**
	 * Create an instance of the built-in code formatter.
	 * 
	 * @param options - the options map to use for formatting with the default code formatter. Recognized options
	 * 	are documented on <code>DefaultCodeFormatterConstants</code>. If set to <code>null</code>, then use
	 * 	the current settings from <code>CCorePlugin.getOptions()</code>.
	 * @return an instance of the built-in code formatter
	 * @see CodeFormatter
	 * @see DefaultCodeFormatterConstants
	 * @see CCorePlugin#getOptions()
	 */
	public static CodeFormatter createDefaultCodeFormatter(Map<String, ?> options){
		if (options == null)
			options = CCorePlugin.getOptions();
		return new CCodeFormatter(options);
	}
}
