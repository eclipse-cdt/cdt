/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public Map<String,String> getParameterValues() {
		Collection<CompletionProposalCategory> descriptors= CompletionProposalComputerRegistry.getDefault().getProposalCategories();
		Map<String, String> map= new HashMap<String, String>(descriptors.size());
		for (CompletionProposalCategory category : descriptors) {
			map.put(category.getDisplayName(), category.getId());
		}
		return map;
	}
}
