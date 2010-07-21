/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.managedbuilder.internal.macros.MbsMacroSupplier;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableSubstitutor;

public class BuildSystemSpecificVariableSubstitutor extends SupplierBasedCdtVariableSubstitutor{
	private static final Set<String> fFileVarsSet = new HashSet<String>(Arrays.asList(MbsMacroSupplier.getInstance().getMacroNames(IBuildMacroProvider.CONTEXT_FILE)));
	private static final Set<String> fOptionVarsSet = new HashSet<String>(Arrays.asList(MbsMacroSupplier.getInstance().getMacroNames(IBuildMacroProvider.CONTEXT_OPTION)));
	private static final Set<String> fToolVarsSet =  new HashSet<String>(Arrays.asList(MbsMacroSupplier.getInstance().getMacroNames(IBuildMacroProvider.CONTEXT_TOOL)));
	
	public BuildSystemSpecificVariableSubstitutor(
			IVariableContextInfo contextInfo, String inexistentMacroValue,
			String listDelimiter, Map<String, String> delimiterMap,
			String incorrectlyReferencedMacroValue) {
		super(contextInfo, inexistentMacroValue, listDelimiter, delimiterMap,
				incorrectlyReferencedMacroValue);
	}

	public BuildSystemSpecificVariableSubstitutor(IVariableContextInfo contextInfo){
		this(contextInfo, "", " "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public BuildSystemSpecificVariableSubstitutor(
			IVariableContextInfo contextInfo, String inexistentMacroValue,
			String listDelimiter) {
		super(contextInfo, inexistentMacroValue, listDelimiter);
	}

	@Override
	protected ResolvedMacro resolveMacro(String macroName)
			throws CdtVariableException {
		if(fFileVarsSet.contains(macroName)
				|| fOptionVarsSet.contains(macroName)
				|| fToolVarsSet.contains(macroName))
			return super.resolveMacro(macroName);
		return new ResolvedMacro(macroName, CdtVariableResolver.createVariableReference(macroName));
	}
}
