/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.util.CoreUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.IPathEntryContainerPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.Bundle;

public class CPathContainerDescriptor implements IContainerDescriptor {

	/**
	 * Adapter class to adapter deprecated ICPathContainerPage to new IPathEntryContainerPage
	 * @author Dave
	 * @deprecated
	 */
	@Deprecated
	public static class PathEntryContainerPageAdapter implements IPathEntryContainerPage {
		public static IPathEntryContainerPage createAdapter(Object elem) {
			if (elem instanceof org.eclipse.cdt.ui.wizards.ICPathContainerPage) {
				return new PathEntryContainerPageAdapter((org.eclipse.cdt.ui.wizards.ICPathContainerPage) elem);
			}
			return null;
		}

		private final org.eclipse.cdt.ui.wizards.ICPathContainerPage fPage;

		protected PathEntryContainerPageAdapter(org.eclipse.cdt.ui.wizards.ICPathContainerPage page) {
			fPage = page;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.wizards.IPathEntryContainerPage#initialize(org.eclipse.cdt.core.model.ICProject, org.eclipse.cdt.core.model.IPathEntry[])
		 */
		@Override
		public void initialize(ICProject project, IPathEntry[] currentEntries) {
			fPage.initialize(project, currentEntries);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.wizards.IPathEntryContainerPage#finish()
		 */
		@Override
		public boolean finish() {
			return fPage.finish();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.wizards.IPathEntryContainerPage#getNewContainers()
		 */
		@Override
		public IContainerEntry[] getNewContainers() {
			IPathEntry[] entries = fPage.getContainerEntries();
			IContainerEntry[] containers = new IContainerEntry[entries.length];
			System.arraycopy(entries, 0, containers, 0, entries.length);
			return containers;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.wizards.IPathEntryContainerPage#setSelection(org.eclipse.cdt.core.model.IContainerEntry)
		 */
		@Override
		public void setSelection(IContainerEntry containerEntry) {
			fPage.setSelection(containerEntry);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		public void createControl(Composite parent) {
			fPage.createControl(parent);

		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
		 */
		@Override
		public boolean canFlipToNextPage() {
			return fPage.canFlipToNextPage();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardPage#getName()
		 */
		@Override
		public String getName() {
			return fPage.getName();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
		 */
		@Override
		public IWizardPage getNextPage() {
			return fPage.getNextPage();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardPage#getPreviousPage()
		 */
		@Override
		public IWizardPage getPreviousPage() {
			return fPage.getPreviousPage();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardPage#getWizard()
		 */
		@Override
		public IWizard getWizard() {
			return fPage.getWizard();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardPage#isPageComplete()
		 */
		@Override
		public boolean isPageComplete() {
			return fPage.isPageComplete();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardPage#setPreviousPage(org.eclipse.jface.wizard.IWizardPage)
		 */
		@Override
		public void setPreviousPage(IWizardPage page) {
			fPage.setPreviousPage(page);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardPage#setWizard(org.eclipse.jface.wizard.IWizard)
		 */
		@Override
		public void setWizard(IWizard newWizard) {
			fPage.setWizard(newWizard);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
		 */
		@Override
		public void dispose() {
			fPage.dispose();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#getControl()
		 */
		@Override
		public Control getControl() {
			return fPage.getControl();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
		 */
		@Override
		public String getDescription() {
			return fPage.getDescription();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
		 */
		@Override
		public String getErrorMessage() {
			return fPage.getErrorMessage();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
		 */
		@Override
		public Image getImage() {
			return fPage.getImage();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
		 */
		@Override
		public String getMessage() {
			return fPage.getMessage();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
		 */
		@Override
		public String getTitle() {
			return fPage.getTitle();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
		 */
		@Override
		public void performHelp() {
			fPage.performHelp();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#setDescription(java.lang.String)
		 */
		@Override
		public void setDescription(String description) {
			fPage.setDescription(description);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
		 */
		@Override
		public void setImageDescriptor(ImageDescriptor image) {
			fPage.setImageDescriptor(image);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#setTitle(java.lang.String)
		 */
		@Override
		public void setTitle(String title) {
			fPage.setTitle(title);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
		 */
		@Override
		public void setVisible(boolean visible) {
			fPage.setVisible(visible);
		}

	}

	private IConfigurationElement fConfigElement;

	private static final String ATT_EXTENSION = "PathContainerPage"; //$NON-NLS-1$

	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$
	private static final String ATT_ICON = "icon"; //$NON-NLS-1$
	private static final String ATT_PAGE_CLASS = "class"; //$NON-NLS-1$

	private Image pageImage;

	public CPathContainerDescriptor(IConfigurationElement configElement) throws CoreException {
		super();
		fConfigElement = configElement;

		String id = fConfigElement.getAttribute(ATT_ID);
		String name = configElement.getAttribute(ATT_NAME);
		String pageClassName = configElement.getAttribute(ATT_PAGE_CLASS);

		if (name == null) {
			throw new CoreException(
					new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, "Invalid extension (missing name): " + id, //$NON-NLS-1$
							null));
		}
		if (pageClassName == null) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0,
					"Invalid extension (missing page class name): " + id, null)); //$NON-NLS-1$
		}
	}

	@Override
	public IPathEntryContainerPage createPage() throws CoreException {
		Object elem = CoreUtility.createExtension(fConfigElement, ATT_PAGE_CLASS);
		if (elem instanceof IPathEntryContainerPage) {
			return (IPathEntryContainerPage) elem;
		}
		IPathEntryContainerPage result = PathEntryContainerPageAdapter.createAdapter(elem);
		if (result != null) {
			return result;
		}
		String id = fConfigElement.getAttribute(ATT_ID);
		throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0,
				"Invalid extension (page not of type IClasspathContainerPage): " + id, null)); //$NON-NLS-1$
	}

	@Override
	public String getName() {
		return fConfigElement.getAttribute(ATT_NAME);
	}

	@Override
	public Image getImage() {
		if (pageImage == null) {
			String imageName = fConfigElement.getAttribute(ATT_ICON);
			if (imageName != null) {
				IExtension extension = fConfigElement.getDeclaringExtension();
				String plugin = extension.getContributor().getName();
				Image image = getImageFromPlugin(plugin, imageName);
				pageImage = image;
			}
		}
		return pageImage;
	}

	public Image getImageFromPlugin(String plugin, String subdirectoryAndFilename) {
		Bundle bundle = Platform.getBundle(plugin);
		URL iconURL = bundle.getEntry("/"); //$NON-NLS-1$
		return getImageFromURL(iconURL, subdirectoryAndFilename);
	}

	public Image getImageFromURL(URL installURL, String subdirectoryAndFilename) {
		Image image = null;
		try {
			URL newURL = new URL(installURL, subdirectoryAndFilename);
			ImageDescriptor desc = ImageDescriptor.createFromURL(newURL);
			image = desc.createImage();
		} catch (MalformedURLException e) {
		} catch (SWTException e) {
		}
		return image;
	}

	public String getPageClass() {
		return fConfigElement.getAttribute(ATT_PAGE_CLASS);
	}

	@Override
	public boolean canEdit(IPathEntry entry) {
		String id = fConfigElement.getAttribute(ATT_ID);
		if (entry.getEntryKind() == IPathEntry.CDT_CONTAINER) {
			String type = entry.getPath().segment(0);
			return id.equals(type);
		}
		return false;
	}

	public static IContainerDescriptor[] getDescriptors() {
		ArrayList<IContainerDescriptor> containers = new ArrayList<>();

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID,
				ATT_EXTENSION);
		if (extensionPoint != null) {
			IContainerDescriptor defaultPage = null;
			String defaultPageName = CPathContainerDefaultPage.class.getName();

			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				try {
					CPathContainerDescriptor curr = new CPathContainerDescriptor(element);
					if (defaultPageName.equals(curr.getPageClass())) {
						defaultPage = curr;
					} else {
						containers.add(curr);
					}
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
			if (defaultPageName != null && containers.isEmpty()) {
				// default page only added if no other extensions found
				containers.add(defaultPage);
			}
		}
		return containers.toArray(new CPathContainerDescriptor[containers.size()]);
	}

}
