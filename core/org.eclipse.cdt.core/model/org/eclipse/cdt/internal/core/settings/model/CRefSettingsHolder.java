/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettingsManager.CContainerRef;

/**
 * This class, derived from CExternalSettingsHolder, is used to cache the
 * external settings exported by some container.
 * 
 * <p> External settings have two sides. The external settings exporter (represented
 * by a pure CExternalSettingsHolder) and the settings referencer referenced by this class.
 * The CRefSettingsHolder holds a cache of the settings exports by the settings holder
 * 
 * <p>Concretely, in the .cproject you might have:
 * 
 * <p> In the exporting config:
 * <code>
 * <br/>&lt;cconfiguration ... 
 * <br/>&nbsp;&lt;storageModule buildSystemId="org.eclipse.cdt.managedbuilder.core.configurationDataProvider" id="..." moduleId="org.eclipse.cdt.core.settings" name="Debug"&gt;
 * <br/>&nbsp;&lt;externalSettings&gt;
 * 		<br/>&nbsp;&nbsp;&lt;externalSetting&gt;
 *			<br/>&nbsp;&nbsp;&nbsp;&lt;entry flags="" kind="includePath" name="libProj"/&gt;
 *		<br/>&nbsp;&nbsp;&lt;/externalSetting&gt;
 * 	<br/>&nbsp;&lt;/externalSettings&gt;
 * 
 * </code>
 * 
 * <p>In the referencing project:
 * 
 * <code>
 * <br/>&lt;configuration ... &gt;
 * <br/>&lt;storageModule moduleId="org.eclipse.cdt.core.externalSettings"&gt;
 * 	<br/>&nbsp;&lt;externalSettings containerId="libProj;" factoryId="org.eclipse.cdt.core.cfg.export.settings.sipplier"&gt;
 * 		<br/>&nbsp;&nbsp;&lt;externalSetting&gt;
 *			<br/>&nbsp;&nbsp;&nbsp;&lt;entry flags="" kind="includePath" name="libProj"/&gt;
 *		<br/>&nbsp;&nbsp;&lt;/externalSetting&gt;
 * 	<br/>&nbsp;&lt;/externalSettings&gt;
 * <br/>&lt;/storageModule&gt;
 * </code>
 */
public class CRefSettingsHolder extends CExternalSettingsHolder {

	/**
	 * The factory responsible for the setting.
	 * One of 
	 * <ul>
	 * <li> {@link CfgExportSettingContainerFactory#FACTORY_ID} </li>
	 * <li> {@link ExtensionContainerFactory#FACTORY_ID} </lid
	 * </ul>
	 */
	private static final String ATTR_FACTORY_ID = "factoryId"; //$NON-NLS-1$
	/** Factory specific containerId used to resolve the settings container */
	private static final String ATTR_CONTAINER_ID = "containerId"; //$NON-NLS-1$

	/** The container we get settings from */
	private final CContainerRef fContainerRef;
	private boolean fIsReconsiled;

	public CRefSettingsHolder(CContainerRef ref) {
		super();
		fContainerRef = ref;
	}

	public CRefSettingsHolder(CRefSettingsHolder base) {
		super(base);

		fContainerRef = base.fContainerRef;
		fIsReconsiled = base.fIsReconsiled;
	}

	public CRefSettingsHolder(ICStorageElement element) {
		super(element);

		String factoryId = element.getAttribute(ATTR_FACTORY_ID); 
		String containerId = element.getAttribute(ATTR_CONTAINER_ID);
		
		fContainerRef = new CContainerRef(factoryId, containerId);
	}

	public CContainerRef getContainerInfo(){
		return fContainerRef;
	}
	
	public boolean isReconsiled(){
		return fIsReconsiled;
	}
	
	public void setReconsiled(boolean s){
		fIsReconsiled = s;
	}

	@Override
	public void serialize(ICStorageElement el) {
		super.serialize(el);
		el.setAttribute(ATTR_FACTORY_ID, fContainerRef.getFactoryId());
		el.setAttribute(ATTR_CONTAINER_ID, fContainerRef.getContainerId());
	}
}
