/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

/**
 * Map of parameters for the specific content assist command.
 *
 * @since 4.0
 */
public final class ContentAssistComputerParameter implements IParameterValues {
	/*
	 * @see org.eclipse.core.commands.IParameterValues#getParameterValues()
	 */
	@Override
	public Map<String, String> getParameterValues() {
		Collection<CompletionProposalCategory> descriptors = CompletionProposalComputerRegistry.getDefault()
				.getProposalCategories();
		Map<String, String> map = new HashMap<>(descriptors.size());
		for (CompletionProposalCategory category : descriptors) {
			map.put(category.getDisplayName(), category.getId());
		}
		return map;
	}
}
