/*******************************************************************************
 * Copyright (c) 2005, 2006 Freescale, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.cdt.debug.internal.core.model.CMemoryBlockExtension;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * Modifies the label shown in a rendering tab of the memory view.
 * 
 * <p> CDT adapter logic will link us to a CMemoryBlockExtension if and 
 * only if that block was created by specifying a memory space. In that
 * case, a literal address and memory space identifier are the basis for 
 * the memory monitor, and the default label provided by the platform:
 * <PRE>   expression : address &lt;rendering-name&gt; </PRE>
 * isn't well suited. Our job is to reduce this to
 * <pre>   expression &lt;rendering-name&gt; </PRE>
 * The expression ends up being the back-end provided string encoding of 
 * a memory space + address pair.
 * <p>
 * @since 3.2
 */
public class MemoryBlockLabelDecorator implements ILabelDecorator {

	/**
	 * The memory block we decorate the label for
	 */
	private CMemoryBlockExtension fMemoryBlock;

	/**
	 * Constructor 
	 * @param memoryBlock the memory block we decorate the label for 
	 */
	public MemoryBlockLabelDecorator(CMemoryBlockExtension memoryBlock) {
		super();
		fMemoryBlock = memoryBlock;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
	 */
	public Image decorateImage(Image image, Object element) {
		// we only decorate the text
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
	 */
	public String decorateText(String text, Object element) {
		// The rendering name is enclosed in <>. We replace everything before
		// that with the memory block's expression.
		int i = text.indexOf('<');
		if (i >= 0)
			return fMemoryBlock.getExpression() + " " + text.substring(i);

		return text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		// how we decorate labels is not affected by any state
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		// nothing to clean up 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		// how we decorate a label is not affected by any properties
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		// how we decorate labels is not affected by any state
	}

}
