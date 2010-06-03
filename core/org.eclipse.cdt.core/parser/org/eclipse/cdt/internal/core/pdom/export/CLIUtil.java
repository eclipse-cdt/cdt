/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.export;

import com.ibm.icu.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.index.export.Messages;
import org.eclipse.core.runtime.CoreException;

/**
 * Helper methods for command-line options
 * <br>
 * <b>Non-API</b> Should a more suitable way for manipulating command-line arguments become usable
 * in the future we will switch to that.
 */
public class CLIUtil {
	public static final String UNQUALIFIED_PARAMETERS= "UNQUALIFIED_PARAMETERS"; //$NON-NLS-1$
	
	/**
	 * Returns the list of options associated with the specified option
	 * @param arguments the arguments map
	 * @param opt the option name to check
	 * @param number the number of parameters
	 * @throws CoreException if the number of parameters is not the specified expected number
	 */
	public static List<String> getArg(Map<String, List<String>> arguments, String opt, int number) throws CoreException {
		List<String> list = arguments.get(opt);
		if(list==null || list.size()!=number) {
			String msg= MessageFormat.format(Messages.CLIUtil_OptionParametersMismatch, new Object[] {opt, ""+number}); //$NON-NLS-1$
			GeneratePDOMApplication.fail(msg);
		}
		return list;
	}

	/**
	 * Returns a map of String option to List of String parameters.
	 */
	public static Map<String,List<String>> parseToMap(String[] args) {
		Map<String,List<String>> result = new HashMap<String,List<String>>();
		String current = null;
		for (String arg : args) {
			if(arg.startsWith("-")) { //$NON-NLS-1$
				current = arg;
				result.put(current, new ArrayList<String>());
			} else {
				if(current==null) {
					current= UNQUALIFIED_PARAMETERS;
					result.put(current, new ArrayList<String>());
				}
				(result.get(current)).add(arg);
			}
		}
		return result;
	}
}
