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

import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.internal.core.CConfigBasedDescriptor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public abstract class AbstractCExtensionProxy implements ICProjectDescriptionListener{
	private IProject fProject;
	private String fExtId;
	private boolean fIsNewStyle;
	private boolean fInited;
	private String fExtPointId;
	private Object fProvider;

	public AbstractCExtensionProxy(IProject project, String extPointId) {
		fProject = project;
		fExtPointId = extPointId;
		CProjectDescriptionManager.getInstance().addCProjectDescriptionListener(this, CProjectDescriptionEvent.LOADED | CProjectDescriptionEvent.APPLIED);
	}

	protected final void providerRequested(){
		if(!fInited)
			checkUpdateProvider(CProjectDescriptionManager.getInstance().getProjectDescription(fProject, false), false, false);
	}

	public void updateProject(IProject project){
		IProject oldProj = fProject;
		fProject = project;
		if(oldProj == null || !oldProj.equals(fProject))
			fInited = false;
	}

	private ICExtensionReference getRef(ICConfigurationDescription cfg, boolean update){
		if(fExtPointId != null){
			try {
				CConfigBasedDescriptor dr = new CConfigBasedDescriptor(cfg, false);
				ICExtensionReference[] cextensions = dr.get(fExtPointId, update);
				if (cextensions.length > 0) {
					return cextensions[0];
				}
			} catch (CoreException e) {
			}
		}
		return null;
	}

	protected IProject getProject(){
		return fProject;
	}

	private boolean checkUpdateProvider(ICProjectDescription des, boolean recreate, boolean rescan){
		Object newProvider = null;
		Object oldProvider = null;

		synchronized(this){
			if(recreate || rescan || !fInited){
				ICExtensionReference ref = null;
				boolean newStyle = true;
				ICConfigurationDescription cfg = null;
				if(des != null){
					cfg = des.getDefaultSettingConfiguration();
					if(cfg != null){
						ref = getRef(cfg, false);
						newStyle = CProjectDescriptionManager.getInstance().isNewStyleCfg(cfg);
					}
				}

				if(ref != null){
					if(recreate || !ref.getID().equals(fExtId)){
						try {
							newProvider = ref.createExtension();
							if(!isValidProvider(newProvider))
								newProvider = null;
						} catch (CoreException e) {
						}
					}
				}

				if(newProvider == null){
					if(recreate || fProvider == null || newStyle != fIsNewStyle){
						newStyle = isNewStyleCfg(cfg);
						newProvider = createDefaultProvider(cfg, newStyle);
					}
				}

				if(newProvider != null){
					if(fProvider != null){
						deinitializeProvider(fProvider);
						oldProvider = fProvider;
					}

					fProvider = newProvider;
					if(ref != null)
						fExtId = ref.getID();

					fIsNewStyle = newStyle;

					initializeProvider(fProvider);
				}

				fInited = true;
			}
		}

		if(newProvider != null){
			postProcessProviderChange(newProvider, oldProvider);
			return true;
		}
		return false;
	}

	protected boolean isNewStyleCfg(ICConfigurationDescription des){
		return CProjectDescriptionManager.getInstance().isNewStyleCfg(des);
	}

	protected abstract boolean isValidProvider(Object o);

	protected abstract void initializeProvider(Object o);

	protected abstract void deinitializeProvider(Object o);

	protected abstract Object createDefaultProvider(ICConfigurationDescription cfgDes, boolean newStile);

	protected void postProcessProviderChange(Object newProvider, Object oldProvider){
	}

	public void close(){
		CProjectDescriptionManager.getInstance().removeCProjectDescriptionListener(this);
		if(fProvider != null){
			deinitializeProvider(fProvider);
		}
	}

	@Override
	public void handleEvent(CProjectDescriptionEvent event) {
		if(!fProject.equals(event.getProject()))
			return;

		doHandleEvent(event);
	}

	protected boolean doHandleEvent(CProjectDescriptionEvent event){
		boolean force = false;
		switch(event.getEventType()){
		case CProjectDescriptionEvent.LOADED:
			force = true;
			//$FALL-THROUGH$
		case CProjectDescriptionEvent.APPLIED:
			ICProjectDescription des = event.getNewCProjectDescription();
			if(des != null){
				updateProject(des.getProject());
				return checkUpdateProvider(des, force, true);
			}
			break;
		}

		return false;
	}
}
