/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.cdt.ui.newui.CDTPrefUtil;

import org.eclipse.cdt.internal.ui.CPluginImages;

/**
 * This class is basic implementation for ICWizardHandler interface.
 * It is independent of managed build system, and, so, almost useless
 * It creates "empty" project with no specific.
 * 
 * Its descendants should overwrite some methods,
 * including createProject() and handleSelection()
 * 
 * This object is created per each Project type
 * on the left pane of New Project Wizard page 
 *  
 * It is responsible for:
 * - corresponding line in left pane of 1st wizard page
 * - whole view of right pane
 * - processing preferred items, if any.
 * - providing data for ConfigPage
 * - processing data received from config page 
 *
 */
public class CWizardHandler implements Cloneable {
	protected static final Image IMG0 = CPluginImages.get(CPluginImages.IMG_EMPTY);
	protected static final Image IMG1 = CPluginImages.get(CPluginImages.IMG_PREFERRED);
	
	protected String head;
	protected String name;
	protected Composite parent;
	protected Table table;
	protected boolean supportedOnly = true;
	
	public CWizardHandler(Composite _parent, String _head, String _name) {
		parent = _parent;
		head = _head;
		name = _name;
	}

	public void handleSelection() {
		List preferred = CDTPrefUtil.getPreferredTCs();
		if (table == null) {
			table = new Table(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText("---"); //$NON-NLS-1$
			ti.setImage(IMG0);
			table.select(0);
		}
		updatePreferred(preferred);
		table.setVisible(true);
		parent.layout();
	}

	public void handleUnSelection() {
		if (table != null) {
			table.setVisible(false);
		}
	}
	
	public String getHeader() { return head; }
	public String getName() { return name; }
	public String getErrorMessage() { return null; }

	public void setSupportedOnly(boolean supp) { supportedOnly = supp;}
	public boolean supportedOnly() { return supportedOnly; }
	public boolean supportsPreferred() { return false; }
	public IWizardPage getSpecificPage() { return null; }
	public void updatePreferred(List prefs) {}

	public void createProject(IProject proj, boolean defaults)
			throws CoreException {}
	public boolean isChanged() { return true; } 
	public void saveState() {}
	public void postProcess(IProject proj) {}
	public boolean isApplicable(EntryDescriptor data) { return true; }
	public void initialize(EntryDescriptor data) throws CoreException {}
	public boolean canFinich() {return true;}

	public Object clone() {
		try {
			CWizardHandler clone = (CWizardHandler)super.clone();
			clone.parent = parent;
			clone.head = head;
			clone.name = name;
			return clone;
		} catch (CloneNotSupportedException e) { return null; }
	}
}
