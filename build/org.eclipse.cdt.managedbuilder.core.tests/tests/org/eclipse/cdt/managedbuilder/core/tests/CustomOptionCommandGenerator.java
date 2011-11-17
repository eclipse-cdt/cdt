/*******************************************************************************
 * Copyright (c) 2011 Texas Instruments and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Baltasar Belyavsky (Texas Instruments) - [279633] Custom command-generator support
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCommandGenerator;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;


public class CustomOptionCommandGenerator implements IOptionCommandGenerator
{
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionCommandGenerator#generateCommand(org.eclipse.cdt.managedbuilder.core.IOption, org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor)
	 */
	@Override
	public String generateCommand(IOption option, IVariableSubstitutor macroSubstitutor) {
		Object value = option.getValue();

		if(value instanceof List) {
			try {
				String[] list = CdtVariableResolver.resolveStringListValues(option.getBasicStringListValue(), macroSubstitutor, true);
				if(list != null) {
					StringBuilder sb = new StringBuilder();

					for(String entry : list) {
						sb.append(entry + ';');
					}

					return option.getCommand() + '\"' + sb.toString() + '\"';
				}
			}
			catch(Exception x) {
			}
		}

		return null;
	}

}
