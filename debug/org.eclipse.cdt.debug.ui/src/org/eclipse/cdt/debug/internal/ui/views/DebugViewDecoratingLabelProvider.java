/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views; 

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * A label provider which receives notification of labels computed in
 * the background by a LaunchViewLabelDecorator.
 */
public class DebugViewDecoratingLabelProvider extends DecoratingLabelProvider {
	
	/**
	 * A map of text computed for elements. Items are added to this map
	 * when notification is received that text has been computed for an element
	 * and they are removed the first time the text is returned for an
	 * element.
	 * key: Object the element
	 * value: String the label text
	 */
	private Map computedText= new HashMap();
	private StructuredViewer viewer= null;
	private boolean disposed= false;
	
	/**
	 * @see DecoratingLabelProvider#DecoratingLabelProvider(org.eclipse.jface.viewers.ILabelProvider, org.eclipse.jface.viewers.ILabelDecorator)
	 */
	public DebugViewDecoratingLabelProvider(StructuredViewer viewer, ILabelProvider provider, DebugViewLabelDecorator decorator) {
		super(provider, decorator);
		decorator.setLabelProvider(this);
		this.viewer= viewer;
	}
	
	/**
	 * Notifies this label provider that the given text was computed
	 * for the given element. The given text will be returned the next
	 * time its text is requested.
	 * 
	 * @param element the element whose label was computed
	 * @param text the label
	 */
	public void textComputed(Object element, String text) {
		computedText.put(element, text);
	}
	
	/**
	 * Labels have been computed for the given block of elements.
	 * This method tells the label provider to update the
	 * given elements in the view.
	 * 
	 * @param elements the elements which have had their text computed
	 */
	public void labelsComputed(Object[] elements) {
		if (!disposed) {
			viewer.update(elements, null);
		}
		for (int i = 0; i < elements.length; i++) {
			computedText.remove(elements[i]);
		}
	}
	
	/**
	 * Returns the stored text computed by the background decorator
	 * or delegates to the decorating label provider to compute text.
	 * The stored value is not cleared - the value is cleared when
	 * #lablesComputed(...) has completed the update of its elements.  
	 * 
	 * @see DecoratingLabelProvider#getText(java.lang.Object) 
	 */
	public String getText(Object element) {
		String text= (String) computedText.get(element);
		if (text != null) {
			return text;
		}
		return super.getText(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		disposed= true;
		super.dispose();
	}
}
