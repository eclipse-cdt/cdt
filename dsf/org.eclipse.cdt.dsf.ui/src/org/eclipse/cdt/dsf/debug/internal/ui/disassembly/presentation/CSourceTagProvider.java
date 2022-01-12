/*******************************************************************************
 * Copyright (c) 2007, 2017 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation;

import java.util.Collection;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.ListenerList;

/**
 * A source tag provider based on the C Model.
 */
public class CSourceTagProvider implements ISourceTagProvider {

	private ListenerList<ISourceTagListener> fListenerList = new ListenerList<>(ListenerList.IDENTITY);
	private ITranslationUnit fUnit;

	/**
	 * Create a new source tag provider for the given translation unit.
	 *
	 * @param unit
	 */
	public CSourceTagProvider(ITranslationUnit unit) {
		fUnit = unit;
	}

	@Override
	public void addSourceTagListener(ISourceTagListener listener) {
		fListenerList.add(listener);
	}

	@Override
	public int[] getActiveCodePositions() {
		// unsupported
		return null;
	}

	@Override
	public long getSnapshotTime() {
		return 0;
	}

	@Override
	public void getSourceTags(Collection<ISourceTag> target) {
		try {
			convertToSourceTags(fUnit.getChildren(), target);
		} catch (CModelException e) {
		}
	}

	/**
	 * @param element
	 * @return
	 */
	private ISourceTag convertToSourceTag(ICElement element) {
		if (element instanceof ISourceReference) {
			return new CSourceTag((ISourceReference) element, element.getElementType());
		}
		return null;
	}

	/**
	 * @param children
	 * @param target
	 * @throws CModelException
	 */
	private void convertToSourceTags(ICElement[] children, Collection<ISourceTag> target) throws CModelException {
		for (int i = 0; i < children.length; i++) {
			ICElement element = children[i];
			ISourceTag tag = convertToSourceTag(element);
			if (tag != null) {
				target.add(tag);
			}
			if (element instanceof IParent) {
				convertToSourceTags(((IParent) element).getChildren(), target);
			}
		}
	}

	@Override
	public void removeSourceTagListener(ISourceTagListener listener) {
		fListenerList.remove(listener);
	}

}
