/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.extensions.ICalledByResult;

public class CalledByResult implements ICalledByResult {
	private Map<ICElement, List<IIndexName>> fElementToReferences = new HashMap<>();

	public ICElement[] getElements() {
		Set<ICElement> elements = fElementToReferences.keySet();
		return elements.toArray(new ICElement[elements.size()]);
	}

	public IIndexName[] getReferences(ICElement calledElement) {
		List<IIndexName> references = fElementToReferences.get(calledElement);
		return references.toArray(new IIndexName[references.size()]);
	}

	@Override
	public void add(ICElement elem, IIndexName ref) {
		List<IIndexName> list = fElementToReferences.get(elem);
		if (list == null) {
			list = new ArrayList<>();
			fElementToReferences.put(elem, list);
		}
		list.add(ref);
	}
}
