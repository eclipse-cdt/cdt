/*******************************************************************************
 * Copyright (c) 2004, 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.opentype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.IndexTypeInfo;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.ui.browser.typeinfo.TypeSelectionDialog;

import org.eclipse.cdt.internal.core.browser.util.IndexModelUtil; 

/**
 * A dialog to select a type from a list of types. The selected type will be
 * opened in the editor.
 */
public class OpenTypeDialog extends TypeSelectionDialog {

	private static final String DIALOG_SETTINGS= OpenTypeDialog.class.getName();

	/**
	 * Constructs an instance of <code>OpenTypeDialog</code>.
	 * @param parent  the parent shell.
	 */
	public OpenTypeDialog(Shell parent) {
		super(parent);
		setTitle(OpenTypeMessages.getString("OpenTypeDialog.title")); //$NON-NLS-1$
		setMessage(OpenTypeMessages.getString("OpenTypeDialog.message")); //$NON-NLS-1$
		setDialogSettings(DIALOG_SETTINGS);
		
	}
	

	char[] toPrefix(String userFilter) {
		userFilter= userFilter.trim().replaceAll("^(\\*)*", "");  //$NON-NLS-1$//$NON-NLS-2$
		int asterix= userFilter.indexOf("*"); //$NON-NLS-1$
		return (asterix==-1 ? userFilter : userFilter.substring(0, asterix)).toCharArray();		
	}
	
	/**
	 * Update the list of elements in AbstractElementListSelectionDialog
	 * 
	 * Filtering on wildcards and types is done by the superclass - we just provide
	 * a good starting point
	 * @param userFilter
	 */
	public void update(String userFilter) {
		char[] prefix = toPrefix(userFilter);
		List types = new ArrayList();
		if(prefix.length>0)
		try {
			IIndex index = CCorePlugin.getIndexManager().getIndex(CoreModel.getDefault().getCModel().getCProjects());
			try {
				index.acquireReadLock();
				IBinding[] bindings= index.findBindingsForPrefix(prefix, false, IndexFilter.ALL);
				for(int i=0; i<bindings.length; i++) {
					IBinding binding = bindings[i];
					try {
						String[] fqn;

						if(binding instanceof ICPPBinding) {
							fqn= ((ICPPBinding)binding).getQualifiedName();
						} else {
							fqn = new String[] {binding.getName()};
						}
						types.add(new IndexTypeInfo(fqn, IndexModelUtil.getElementType(binding), index));
					} catch(DOMException de) {

					}
				}
			} finally {
				index.releaseReadLock();
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		} catch(InterruptedException ie) {
			CCorePlugin.log(ie);
		}
		setListElements(types.toArray(new ITypeInfo[types.size()]));
	}
	
	protected void setListElements(Object[] elements) {
		super.setListElements(elements);
	}
	
	/**
	 * @deprecated
	 */
	public void setElements(Object[] elements) {
	}
	
	protected void handleEmptyList() {
		// override super-class behaviour with no-op
	}
	
	protected Text createFilterText(Composite parent) {
		final Text result = super.createFilterText(parent);
		Listener listener = new Listener() {
            public void handleEvent(Event e) {
                update(result.getText());
            }
        };
        result.addListener(SWT.Modify, listener);
        return result;
	}
}
