/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	private ListenerList fListenerList= new ListenerList(ListenerList.IDENTITY);
	private ITranslationUnit fUnit;
	
	/**
	 * Create a new source tag provider for the given translation unit.
	 * 
	 * @param unit
	 */
	public CSourceTagProvider(ITranslationUnit unit) {
		fUnit= unit;
	}

	public void addSourceTagListener(ISourceTagListener listener) {
		fListenerList.add(listener);
	}

	public int[] getActiveCodePositions() {
		// unsupported
		return null;
	}

	public long getSnapshotTime() {
		return 0;
	}

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
			return new CSourceTag((ISourceReference)element, element.getElementType());
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
			ICElement element= children[i];
			ISourceTag tag= convertToSourceTag(element);
			if (tag != null) {
				target.add(tag);
			}
			if (element instanceof IParent) {
				convertToSourceTags(((IParent)element).getChildren(), target);
			}
		}
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation.ISourceTagProvider#removeSourceTagListener(org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation.ISourceTagListener)
	 */
	public void removeSourceTagListener(ISourceTagListener listener) {
		fListenerList.remove(listener);
	}

}
