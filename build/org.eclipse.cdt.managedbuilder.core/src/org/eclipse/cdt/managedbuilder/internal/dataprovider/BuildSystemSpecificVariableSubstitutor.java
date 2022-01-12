/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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

public class BuildSystemSpecificVariableSubstitutor extends SupplierBasedCdtVariableSubstitutor {
	private static final Set<String> fFileVarsSet = new HashSet<>(
			Arrays.asList(MbsMacroSupplier.getInstance().getMacroNames(IBuildMacroProvider.CONTEXT_FILE)));
	private static final Set<String> fOptionVarsSet = new HashSet<>(
			Arrays.asList(MbsMacroSupplier.getInstance().getMacroNames(IBuildMacroProvider.CONTEXT_OPTION)));
	private static final Set<String> fToolVarsSet = new HashSet<>(
			Arrays.asList(MbsMacroSupplier.getInstance().getMacroNames(IBuildMacroProvider.CONTEXT_TOOL)));

	public BuildSystemSpecificVariableSubstitutor(IVariableContextInfo contextInfo, String inexistentMacroValue,
			String listDelimiter, Map<String, String> delimiterMap, String incorrectlyReferencedMacroValue) {
		super(contextInfo, inexistentMacroValue, listDelimiter, delimiterMap, incorrectlyReferencedMacroValue);
	}

	public BuildSystemSpecificVariableSubstitutor(IVariableContextInfo contextInfo) {
		this(contextInfo, "", " "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public BuildSystemSpecificVariableSubstitutor(IVariableContextInfo contextInfo, String inexistentMacroValue,
			String listDelimiter) {
		super(contextInfo, inexistentMacroValue, listDelimiter);
	}

	@Override
	protected ResolvedMacro resolveMacro(String macroName) throws CdtVariableException {
		if (fFileVarsSet.contains(macroName) || fOptionVarsSet.contains(macroName) || fToolVarsSet.contains(macroName))
			return super.resolveMacro(macroName);
		return new ResolvedMacro(macroName, CdtVariableResolver.createVariableReference(macroName));
	}
}
