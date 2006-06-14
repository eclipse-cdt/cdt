/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractBinaryParserPage extends AbstractCOptionPage {

	protected ICOptionPage fCurrentBinaryParserPage;
	protected Map fParserPageMap = null;

	// Composite parent provided by the block.
	protected Composite fCompositeParent;
	private ICOptionPage fCurrentPage;

	protected class BinaryParserPageConfiguration {

		ICOptionPage page;
		IConfigurationElement fElement;

		public BinaryParserPageConfiguration(IConfigurationElement element) {
			fElement = element;
		}

		public ICOptionPage getPage() throws CoreException {
			if (page == null) {
				page = (ICOptionPage) fElement.createExecutableExtension("class"); //$NON-NLS-1$
			}
			return page;
		}
	}

	protected AbstractBinaryParserPage(String title) {
		super(title);
		initializeParserPageMap();

	}

	protected AbstractBinaryParserPage(String title, ImageDescriptor image) {
		super(title, image);
		initializeParserPageMap();
	}

	private void initializeParserPageMap() {
		fParserPageMap = new HashMap(5);

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID, "BinaryParserPage"); //$NON-NLS-1$
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].getName().equals("parserPage")) { //$NON-NLS-1$
				String id = infos[i].getAttribute("parserID"); //$NON-NLS-1$
				fParserPageMap.put(id, new BinaryParserPageConfiguration(infos[i]));
			}
		}
	}

	protected Composite getCompositeParent() {
		return fCompositeParent;
	}

	protected void setCompositeParent(Composite parent) {
		fCompositeParent = parent;
		fCompositeParent.setLayout(new TabFolderLayout());
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		handleBinaryParserChanged();
	}
	/**
	 * Notification that the user changed the selection of the Binary Parser.
	 */
	protected void handleBinaryParserChanged() {
		if (getCompositeParent() == null) {
			return;
		}
		String[] enabled = getBinaryParserIDs();
		ICOptionPage page;
		for (int i = 0; i < enabled.length; i++) { // create all enabled pages
			page = getBinaryParserPage(enabled[i]);
			if (page != null) {
				if (page.getControl() == null) {
					Composite parent = getCompositeParent();
					page.setContainer(getContainer());
					page.createControl(parent);
					parent.layout(true);
				} else {
					page.setVisible(false);
				}
			}
		}
		// Retrieve the dynamic UI for the current parser
		String parserID = getCurrentBinaryParserID();
		page = getBinaryParserPage(parserID);
		if (page != null) {
			page.setVisible(true);
		}
		setCurrentPage(page);
	}

	protected ICOptionPage getCurrentPage() {
		return fCurrentPage;
	}

	protected void setCurrentPage(ICOptionPage page) {
		fCurrentPage = page;
	}

	protected ICOptionPage getBinaryParserPage(String parserID) {
		BinaryParserPageConfiguration configElement = (BinaryParserPageConfiguration) fParserPageMap.get(parserID);
		if (configElement != null) {
			try {
				return configElement.getPage();
			} catch (CoreException e) {
			}
		}
		return null;
	}

	abstract protected String getCurrentBinaryParserID();

	abstract protected String[] getBinaryParserIDs();

	abstract public void createControl(Composite parent);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	abstract public void performApply(IProgressMonitor monitor) throws CoreException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	abstract public void performDefaults();

}
