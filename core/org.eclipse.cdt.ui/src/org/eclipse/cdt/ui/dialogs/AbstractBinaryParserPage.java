/***********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.ui.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractBinaryParserPage extends AbstractCOptionPage {

	protected ICOptionPage fCurrentBinaryParserPage;
	protected Map fParserPageMap = null;

	// Composite parent provided by the block.
	protected Composite fCompositeParent;

	public AbstractBinaryParserPage() {
		super();
	}

	public AbstractBinaryParserPage(String title) {
		super(title);
	}

	public AbstractBinaryParserPage(String title, ImageDescriptor image) {
		super(title, image);
	}

	protected Composite getCompositeParent() {
		return fCompositeParent;
	}

	protected void setCompositeParent(Composite parent) {
		fCompositeParent = parent;
	}

	/**
	 * Save the current Binary parser page.
	 */
	protected void setCurrentBinaryParserPage(ICOptionPage current) {
		fCurrentBinaryParserPage = current;
	}
                                                                                                                             
	/**
	 * Get the current Binary parser page.
	 */
	protected ICOptionPage getCurrentBinaryParserPage() {
		return fCurrentBinaryParserPage;
	}

	/**
	 * Notification that the user changed the selection of the Binary Parser.
	 */
	protected void handleBinaryParserChanged() {
		loadDynamicBinaryParserArea();
	}

	/**
	 * Show the contributed piece of UI that was registered for the Binary parser id.
	 */
	protected void loadDynamicBinaryParserArea() {
		// Dispose of any current child widgets in the tab holder area
		Control[] children = getCompositeParent().getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}

		// Retrieve the dynamic UI for the current parser
		String parserID = getCurrentBinaryParserID();
		ICOptionPage page = getBinaryParserPage(parserID);
		if (page != null) {
			Composite parent = getCompositeParent();
			page.setContainer(getContainer());
			page.createControl(parent);
			page.getControl().setVisible(true);
			parent.layout(true);
			setCurrentBinaryParserPage(page);
		}
	}

	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);
		initializeParserPageMap();
	}

	public ICOptionPage getBinaryParserPage(String parserID) {
		if (fParserPageMap == null) {
			initializeParserPageMap();
		}
		IConfigurationElement configElement = (IConfigurationElement) fParserPageMap.get(parserID);
		ICOptionPage page = null;
		if (configElement != null) {
			try {
				page = (ICOptionPage) configElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException ce) {
				//ce.printStackTrace();
			}
		}
		return page;
	}

	protected void initializeParserPageMap() {
		fParserPageMap = new HashMap(5);

		IPluginDescriptor descriptor = CUIPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint = descriptor.getExtensionPoint("BinaryParserPage"); //$NON-NLS-1$
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		for (int i = 0; i < infos.length; i++) {
			String id = infos[i].getAttribute("parserID"); //$NON-NLS-1$
			fParserPageMap.put(id, infos[i]);
		}
	}

	abstract protected String getCurrentBinaryParserID();

	abstract public void createControl(Composite parent);

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		ICOptionPage page = getCurrentBinaryParserPage();
		if (page != null) {
			page.performApply(monitor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		ICOptionPage page = getCurrentBinaryParserPage();
		if (page != null) {
			page.performDefaults();
		}
	}

}
