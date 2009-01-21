/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionDelta;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IProject;

/**
 * Events fired for the project delats.
 * 
 * FIXME JBB: Should work out the difference between DATA_APPLIED AND APPLIED events
 * current DATA_APPLIED occurs first and fNewDescription is modified. Need to clearly define
 * the project description state transitions
 */

public final class CProjectDescriptionEvent {
	public static final int LOADED = 1;
	public static final int ABOUT_TO_APPLY = 1 << 1;
	public static final int APPLIED = 1 << 2;
	public static final int COPY_CREATED = 1 << 3;
	public static final int DATA_APPLIED = 1 << 4;
	public static final int ALL = LOADED | ABOUT_TO_APPLY | APPLIED | COPY_CREATED | DATA_APPLIED;
	
	private int fType;
	/** A *writable* new description */
	private ICProjectDescription fNewDescription;
	/** The previous description should be read-only */
	private ICProjectDescription fOldDescription;
	private ICProjectDescription fAppliedDescription;
	private ICDescriptionDelta fProjDelta;
	private ICDescriptionDelta fActiveCfgDelta;
	private ICDescriptionDelta fIndexCfgDelta;
	private IProject fProject;
	
	public CProjectDescriptionEvent(int type,
			ICDescriptionDelta delta,
			ICProjectDescription newDes,
			ICProjectDescription oldDes,
			ICProjectDescription appliedDes) {
		fType = type;
		fProjDelta = delta;
		fNewDescription = newDes;
		fOldDescription = oldDes;
		fAppliedDescription = appliedDes;
		if (fNewDescription != null) {
			fProject = fNewDescription.getProject();
		} else if (fOldDescription != null) {
			fProject = fOldDescription.getProject();
		}
	}
	
	public IProject getProject() {
		return fProject;
	}

	public int getEventType() {
		return fType;
	}
	
	public ICDescriptionDelta getProjectDelta() {
		return fProjDelta;
	}
	
	public ICDescriptionDelta getActiveCfgDelta() {
		if (fActiveCfgDelta == null) {
			fActiveCfgDelta = getDelta(true);
		}
		return fActiveCfgDelta;
	}

	public ICDescriptionDelta getDefaultSettingCfgDelta() {
		if (fIndexCfgDelta == null) {
			fIndexCfgDelta = getDelta(false);
		}
		return fIndexCfgDelta;
	}
	
	private ICDescriptionDelta getDelta(boolean active) {
		ICDescriptionDelta delta = null;
		switch(getEventType()) {
		case LOADED:
		case ABOUT_TO_APPLY:
		case APPLIED:
		case DATA_APPLIED:
			if (fProjDelta != null) {
				ICProjectDescription oldDes = getOldCProjectDescription();
				ICProjectDescription newDes = getNewCProjectDescription();
				if (oldDes == null) {
					ICConfigurationDescription cfg = getCfg(newDes, active);
					if (cfg != null) {
						delta = findCfgDelta(fProjDelta, cfg.getId());
					}
				} else if (newDes == null) {
					ICConfigurationDescription cfg = getCfg(oldDes, active);
					if (cfg != null) {
						delta = findCfgDelta(fProjDelta, cfg.getId());
					}
				} else {
					ICConfigurationDescription newCfg = getCfg(newDes, active);
					ICConfigurationDescription oldCfg = getCfg(oldDes, active);
					if (oldCfg == null) {
						if (newCfg != null) {
							delta = new CProjectDescriptionDelta(newCfg, null);
						}
					} else if (newCfg == null) {
						delta = new CProjectDescriptionDelta(null, oldCfg);
					} else if (newCfg.getId().equals(oldCfg.getId())) {
						delta = findCfgDelta(fProjDelta, newCfg.getId());
					} else {
						delta = CProjectDescriptionManager.getInstance().createDelta(newCfg, oldCfg);
					}
				}
			}
			break;
			
		case COPY_CREATED:
			break;
		}
		return delta;
	}
	
	private ICConfigurationDescription getCfg(ICProjectDescription des, boolean active) {
		return active ? des.getActiveConfiguration() : des.getDefaultSettingConfiguration(); 
	}
	
	private ICDescriptionDelta findCfgDelta(ICDescriptionDelta delta, String id) {
		if (delta == null)
			return null;
		ICDescriptionDelta children[] = delta.getChildren();
		for (int i = 0; i < children.length; i++) {
			ICSettingObject s = children[i].getNewSetting();
			if (s != null && id.equals(s.getId()))
				return children[i];
		}
		return null;
	}

	public ICProjectDescription getOldCProjectDescription() {
		return fOldDescription;
	}

	/**
	 * Return the new description which is writeable
	 * @return writable new description
	 */
	public ICProjectDescription getNewCProjectDescription() {
		return fNewDescription;
	}

	public ICProjectDescription getAppliedCProjectDescription() {
		return fAppliedDescription;
	}
}
