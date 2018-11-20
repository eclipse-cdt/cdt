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
import org.eclipse.cdt.ui.extensions.ICallToResult;

public class CallsToResult implements ICallToResult {
	private Map<CElementSet, List<IIndexName>> fElementSetsToReferences = new HashMap<>();

	public CElementSet[] getElementSets() {
		Set<CElementSet> elementSets = fElementSetsToReferences.keySet();
		return elementSets.toArray(new CElementSet[elementSets.size()]);
	}

	public IIndexName[] getReferences(CElementSet elementSet) {
		List<IIndexName> references = fElementSetsToReferences.get(elementSet);
		return references.toArray(new IIndexName[references.size()]);
	}

	@Override
	public void add(ICElement[] elems, IIndexName ref) {
		CElementSet key = new CElementSet(elems);
		List<IIndexName> list = fElementSetsToReferences.get(key);
		if (list == null) {
			list = new ArrayList<>();
			fElementSetsToReferences.put(key, list);
		}
		list.add(ref);
	}
}
