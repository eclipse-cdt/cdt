/*
 * Created on Aug 17, 2004
 *
 * Copyright 2004, QNX Software Systems Ltd. All Rights Reserved.
 *
 * This source code may contain confidential information of QNX Software
 * Systems Ltd.  (QSSL) and its licensors. Any use, reproduction,
 * modification, disclosure, distribution or transfer of this software,
 * or any software which includes or is based upon any of this code, is
 * prohibited unless expressly authorized by QSSL by written agreement. For
 * more information (including whether this source code file has been
 * published) please email licensing@qnx.com.
 */
package org.eclipse.cdt.core;

import java.util.Map;

import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * @author AChapiro
 */
public class ToolFactory {

	/**
	 * Create an instance of the built-in code formatter. 
	 * @param options - the options map to use for formatting with the default code formatter. Recognized options
	 * 	are documented on <code>CCorePlugin#getDefaultOptions()</code>. If set to <code>null</code>, then use 
	 * 	the current settings from <code>CCorePlugin#getOptions</code>.
	 * @return an instance of the built-in code formatter
	 * @see CodeFormatter
	 * @see CCorePlugin#getOptions()
	 */
	public static CodeFormatter createCodeFormatter(Map options){
		if (options == null) 
			options = CCorePlugin.getOptions();
		String formatterID = (String)options.get(CCorePreferenceConstants.CODE_FORMATTER);
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extension = Platform.getExtensionRegistry().
			getExtensionPoint(CCorePlugin.PLUGIN_ID, "CodeFormatter");
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			for(int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
				for(int j = 0; j < configElements.length; j++){
					String initializerID = configElements[j].getAttribute("id"); //$NON-NLS-1$
					if (initializerID != null && initializerID.equals(formatterID)){
						try {
							Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							if (execExt instanceof CodeFormatter){
								CodeFormatter formatter = (CodeFormatter)execExt;
								formatter.setOptions(options);
								return formatter;
							}
						} catch(CoreException e) {
							//TODO: add more reasonable error processing
							e.printStackTrace();
							break;
						}
					}
				}
			}	
		}		
		// TODO: open this code later 
		// return new DefaultCodeFormatter(options);
		return null;
	}

	
}
