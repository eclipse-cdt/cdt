/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.folding;

import org.eclipse.cdt.ui.text.folding.ICFoldingPreferenceBlock;
import org.eclipse.cdt.ui.text.folding.ICFoldingStructureProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.text.Assert;

/**
 * Describes a contribution to the folding provider extension point.
 * 
 */
public final class CFoldingStructureProviderDescriptor {

	/* extension point attribute names */
	
	private static final String PREFERENCES_CLASS= "preferencesClass"; //$NON-NLS-1$
	private static final String CLASS= "class"; //$NON-NLS-1$
	private static final String NAME= "name"; //$NON-NLS-1$
	private static final String ID= "id"; //$NON-NLS-1$
	
	/** The identifier of the extension. */
	private String fId;
	/** The name of the extension. */
	private String fName;
	/** The class name of the provided <code>ICFoldingStructureProvider</code>. */
	private String fClass;
	/**
	 * <code>true</code> if the extension specifies a custom
	 * <code>ICFoldingPreferenceBlock</code>.
	 */
	private boolean fHasPreferences;
	/** The configuration element of this extension. */
	private IConfigurationElement fElement;
	
	/**
	 * Creates a new descriptor.
	 * 
	 * @param element the configuration element to read
	 */
	CFoldingStructureProviderDescriptor(IConfigurationElement element) {
		fElement= element;
		fId= element.getAttributeAsIs(ID);
		Assert.isLegal(fId != null);
		
		fName= element.getAttribute(NAME);
		if (fName == null)
			fName= fId;
		
		fClass= element.getAttributeAsIs(CLASS);
		Assert.isLegal(fClass != null);
		
		if (element.getAttributeAsIs(PREFERENCES_CLASS) == null)
			fHasPreferences= false;
		else
			fHasPreferences= true;
	}
	
	/**
	 * Creates a folding provider as described in the extension's xml.
	 * 
	 * @return a new instance of the folding provider described by this
	 *         descriptor
	 * @throws CoreException if creation fails
	 */
	public ICFoldingStructureProvider createProvider() throws CoreException {
		ICFoldingStructureProvider prov= (ICFoldingStructureProvider) fElement.createExecutableExtension(CLASS);
		return prov;
	}

	/**
	 * Creates a preferences object as described in the extension's xml.
	 * 
	 * @return a new instance of the reference provider described by this
	 *         descriptor
	 * @throws CoreException if creation fails
	 */
	public ICFoldingPreferenceBlock createPreferences() throws CoreException {
		if (fHasPreferences) {
			ICFoldingPreferenceBlock prefs= (ICFoldingPreferenceBlock) fElement.createExecutableExtension(PREFERENCES_CLASS);
			return prefs;
		}
		return new EmptyCFoldingPreferenceBlock();
	}
	
	/**
	 * Returns the identifier of the described extension.
	 * 
	 * @return Returns the id
	 */
	public String getId() {
		return fId;
	}
	
	/**
	 * Returns the name of the described extension.
	 * 
	 * @return Returns the name
	 */
	public String getName() {
		return fName;
	}

}
