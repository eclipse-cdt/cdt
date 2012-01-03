/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems, Inc. - adapted for for disassembly parts
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.disassembly.rulers;

import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.texteditor.rulers.ExtensionPointHelper;
import org.eclipse.ui.internal.texteditor.rulers.RulerColumnMessages;
import org.eclipse.ui.internal.texteditor.rulers.RulerColumnPlacement;
import org.eclipse.ui.internal.texteditor.rulers.RulerColumnTarget;


/**
 * The description of an extension to the
 * <code>org.eclipse.ui.workbench.texteditor.rulerColumns</code> extension point. Instances are
 * immutable. Instances can be obtained from a {@link RulerColumnRegistry}.
 *
 * @since 7.2
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class RulerColumnDescriptor {
	/** The extension schema name of the class attribute. */
	private static final String CLASS= "class"; //$NON-NLS-1$
	/** The extension schema name of the id attribute. */
	private static final String ID= "id"; //$NON-NLS-1$
	/** The extension schema name of the optional name attribute. */
	private static final String NAME= "name"; //$NON-NLS-1$
	/** The extension schema name of the optional enabled attribute. */
	private static final String ENABLED= "enabled"; //$NON-NLS-1$
	/** The extension schema name of the optional icon attribute. */
	private static final String ICON= "icon"; //$NON-NLS-1$
	/** The extension schema name of the optional global attribute. */
	private static final String GLOBAL= "global"; //$NON-NLS-1$
	/** The extension schema name of the optional menu inclusion attribute. */
	private static final String INCLUDE_IN_MENU= "includeInMenu"; //$NON-NLS-1$
	/** The extension schema name of the targetEditor element. */
	private static final String TARGET_ID= "targetId"; //$NON-NLS-1$
	/** The extension schema name of the targetClass element. */
	private static final String TARGET_CLASS= "targetClass"; //$NON-NLS-1$
	/** The extension schema name of the placement element. */
	private static final String PLACEMENT= "placement"; //$NON-NLS-1$

	/** The identifier of the extension. */
	private final String fId;
	/** The name of the extension, equal to the id if no name is given. */
	private final String fName;
	/** The icon descriptor. */
	private final ImageDescriptor fIcon;
	/** The configuration element of this extension. */
	private final IConfigurationElement fElement;
	/** The target specification of the ruler column contribution. */
	private final RulerColumnTarget fTarget;
	/** The placement specification of the ruler column contribution. */
	private final RulerColumnPlacement fRulerColumnPlacement;
	/** The default enablement setting of the ruler column contribution. */
	private final boolean fDefaultEnablement;
	/** The global setting of the ruler column contribution. */
	private final boolean fIsGlobal;
	/** The menu inclusion setting of the ruler column contribution. */
	private final boolean fIncludeInMenu;

	/**
	 * Creates a new descriptor.
	 *
	 * @param element the configuration element to read
	 * @param registry the computer registry creating this descriptor
	 * @throws InvalidRegistryObjectException if the configuration element is no longer valid
	 * @throws CoreException if the configuration element does not conform to the extension point spec
	 */
	RulerColumnDescriptor(IConfigurationElement element, RulerColumnRegistry registry) throws InvalidRegistryObjectException, CoreException {
		Assert.isLegal(registry != null);
		Assert.isLegal(element != null);
		fElement= element;

		ExtensionPointHelper helper= new ExtensionPointHelper(element);

		fId= helper.getNonNullAttribute(ID);
		fName= helper.getDefaultAttribute(NAME, fId);
		helper.getNonNullAttribute(CLASS); // just check validity
		URL iconURL= helper.getDefaultResourceURL(ICON, null);
		fIcon= iconURL == null ? null : ImageDescriptor.createFromURL(iconURL);
		fDefaultEnablement= helper.getDefaultAttribute(ENABLED, true);
		fIsGlobal= helper.getDefaultAttribute(GLOBAL, true);
		fIncludeInMenu= helper.getDefaultAttribute(INCLUDE_IN_MENU, true);

		@SuppressWarnings("null")
		IConfigurationElement[] targetEditors= element.getChildren(TARGET_ID);
		IConfigurationElement[] targetClasses= element.getChildren(TARGET_CLASS);

		RulerColumnTarget combined= null;
		for (int i= 0; i < targetEditors.length; i++) {
			IConfigurationElement targetEditor= targetEditors[i];
			RulerColumnTarget target= RulerColumnTarget.createEditorIdTarget(new ExtensionPointHelper(targetEditor).getNonNullAttribute(ID));
			combined= RulerColumnTarget.createOrTarget(combined, target);
		}
		for (int i= 0; i < targetClasses.length; i++) {
			IConfigurationElement targetClass= targetClasses[i];
			RulerColumnTarget target= RulerColumnTarget.createClassTarget(new ExtensionPointHelper(targetClass).getNonNullAttribute(CLASS));
			combined= RulerColumnTarget.createOrTarget(combined, target);
		}
		fTarget= combined;

		IConfigurationElement[] placements= element.getChildren(PLACEMENT);
		switch (placements.length) {
			case 0:
				fRulerColumnPlacement= new RulerColumnPlacement();
				break;
			case 1:
				fRulerColumnPlacement= new RulerColumnPlacement(placements[0]);
				break;
			default:
				helper.fail(RulerColumnMessages.RulerColumnDescriptor_invalid_placement_msg);
				fRulerColumnPlacement= null; // dummy
				break;
		}

		Assert.isTrue(fTarget != null);
		Assert.isTrue(fRulerColumnPlacement != null);
	}

	/**
	 * Returns the identifier of the described extension.
	 *
	 * @return the identifier of the described extension
	 */
	public String getId() {
		return fId;
	}

	/**
	 * Returns the name of the described extension.
	 *
	 * @return the name of the described extension
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Returns the image descriptor of the described extension, <code>null</code> if it does not
	 * have an image.
	 *
	 * @return the image descriptor of the described extension or <code>null</code> for no image
	 */
	public ImageDescriptor getIcon() {
		return fIcon;
	}

	RulerColumnTarget getTarget() {
		return fTarget;
	}

	RulerColumnPlacement getPlacement() {
		return fRulerColumnPlacement;
	}

	/**
	 * Returns the default enablement of the described extension. Editors that support this
	 * contribution should typically enable the column by default.
	 *
	 * @return the default enablement of the described extension
	 */
	public boolean getDefaultEnablement() {
		return fDefaultEnablement;
	}

	/**
	 * Returns the global property of the described extension. Changing the visibility of a column
	 * with the global property set to <code>true</code> should typically affect all matching
	 * editors. Changing the visibility of a column with the global property set to
	 * <code>false</code> should only affect the current disassembly part.
	 *
	 * @return the global property of the described extension
	 */
	public boolean isGlobal() {
		return fIsGlobal;
	}

	/**
	 * Returns the menu inclusion property of the described extension. A toggle menu entry should be
	 * inluded in the ruler context menu for columns with this property set to <code>true</code>.
	 *
	 * @return the menu inclusion property of the described extension
	 */
	public boolean isIncludedInMenu() {
		return fIncludeInMenu;
	}

	/**
	 * Returns <code>true</code> if this contribution matches the passed disassembly part , <code>false</code> if not.
	 *
	 * @param disassembly the disassembly part to check
	 * @return <code>true</code> if this contribution targets the passed disassembly part 
	 */
	public boolean matchesPart(IWorkbenchPart disassembly) {
		Assert.isLegal(disassembly != null);
		RulerColumnTarget target= getTarget();

		@SuppressWarnings("null")
		IWorkbenchPartSite site= disassembly.getSite();
		if (site != null && target.matchesEditorId(site.getId()))
			return true;

		if (target.matchesClass(disassembly.getClass()))
			return true;

		IContentType contentType= getContentType(disassembly);
		return contentType != null && target.matchesContentType(contentType);

	}

	/**
	 * Creates a {@link IContributedRulerColumn} instance as described by the receiver. This may load the contributing plug-in.
	 *
	 * @param disassembly the disassembly part that loads the contributed column
	 * @return the instantiated column
	 * @throws CoreException as thrown by {@link IConfigurationElement#createExecutableExtension(String)}
	 * @throws InvalidRegistryObjectException as thrown by {@link IConfigurationElement#createExecutableExtension(String)}
	 */
	public IContributedRulerColumn createColumn(IWorkbenchPart disassembly) throws CoreException, InvalidRegistryObjectException {
		Assert.isLegal(disassembly != null);
		IContributedRulerColumn column= (IContributedRulerColumn)fElement.createExecutableExtension(CLASS);
		column.setDescriptor(this);
		column.setDisassemblyPart(disassembly);
		column.columnCreated();
		return column;
	}

	/*
	 * @see java.lang.Object#toString()
	 * @since 3.3
	 */
	@Override
	public String toString() {
		return "RulerColumnDescriptor[name=" + getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	IConfigurationElement getConfigurationElement() {
		return fElement;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime= 31;
		int result= 1;
		result= prime * result + ((fId == null) ? 0 : fId.hashCode());
		return result;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RulerColumnDescriptor other= (RulerColumnDescriptor) obj;
		if (fId == null) {
			if (other.fId != null)
				return false;
		} else if (!fId.equals(other.fId))
			return false;
		return true;
	}

	/**
	 * Returns the content type of the disassembly part's input, <code>null</code> if the disassembly part input or
	 * the document provider is <code>null</code> or the content type cannot be determined.
	 *
	 * @param disassembly part the disassembly part to get the content type from
	 * @return the content type of the disassembly part's input, <code>null</code> if it cannot be
	 *         determined
	 */
	private IContentType getContentType(IWorkbenchPart disassembly) {
		return null;
	}

	String getContributor() {
		try {
			return fElement.getContributor().getName();
		} catch (InvalidRegistryObjectException e) {
			return "unknown"; //$NON-NLS-1$
		}
	}
}
