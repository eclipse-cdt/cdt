package org.eclipse.cdt.internal.ui.text.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.corext.template.TemplateSet;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


public class TemplateContentProvider implements IStructuredContentProvider {

	private TemplateSet fTemplateSet;	

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */	
	public Object[] getElements(Object input) {
		return fTemplateSet.getTemplates();
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fTemplateSet= (TemplateSet) newInput;
	}

	/**
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
		fTemplateSet= null;
	}
	
}

