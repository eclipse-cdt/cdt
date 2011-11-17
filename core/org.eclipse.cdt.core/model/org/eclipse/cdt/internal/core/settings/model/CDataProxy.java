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

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.extension.CDataObject;
import org.eclipse.core.resources.IProject;

public abstract class CDataProxy implements ICSettingObject {
	protected ICDataProxyContainer fParent;
	private CDataObject fData;
	private int fFlags;
	private CConfigurationDescription fConfiguration;
	private String fId;

	private static final int F_RESCAN = 1;
//	private static final int F_WRITABLE = 1 << 1;

	CDataProxy(CDataObject data, ICDataProxyContainer parent, CConfigurationDescription cfg) {
		fData = data;
		if(fData != null)
			fId = fData.getId();
		fParent = parent;
		fConfiguration = cfg;
	}

	@Override
	public ICSettingContainer getParent() {
		return fParent;
	}

	protected void setRescan(boolean rescan){
		if(isRescan() == rescan)
			return;

		if(rescan)
			addFlags(F_RESCAN);
		else
			clearFlags(F_RESCAN);
	}

	protected boolean isRescan(){
		if(checkFlags(F_RESCAN))
			return true;
		return false;//fData == null ? true : !fData.isValid();
	}

	private boolean checkFlags(int flags){
		return (fFlags & flags) == flags;
	}

	private void addFlags(int flags){
		fFlags |= flags;
	}

	private void clearFlags(int flags){
		fFlags &= (~flags);
	}

	protected CDataObject getData(boolean write){
		checkUpdate(write);
		return fData;
	}

	protected CDataObject doGetData(){
		return fData;
	}

	protected boolean containsWritableData(){
		return !(fData instanceof ICachedData);
	}

/*	protected void setWritable(boolean writable){
		if(writable == isWritable())
			return;
		if(writable)
			addFlags(F_WRITABLE);
		else
			clearFlags(F_WRITABLE);
	}
*/
/*	void setData(CDataObject data, boolean write){
		fData = data;
		setWritable(write);
		setRescan(false);
	}
*/

/*	void updateData(CDataObject data){
		fData = data;
		setRescan(false);
	}
*/
	void setData(CDataObject data){
		fId = data.getId();
		fData = data;
	}

	void internalSetId(String id){
		fId = id;
	}

	void doClearData(){
		fData = null;
		setRescan(true);
	}

	final protected void checkUpdate(boolean write){
		if((write && !containsWritableData())
				|| isRescan())
			fParent.updateChild(this, write);
	}

	void remove(){
		fData = null;
		fParent = null;
	}

	@Override
	public boolean isValid(){
		checkUpdate(false);
		return fData != null ? fData.isValid() : false;
	}

	@Override
	public ICConfigurationDescription getConfiguration() {
		return fConfiguration;
	}

	@Override
	public String getId() {
		return fId;
//		CDataObject data = getData(false);
//		return data != null ? data.getId() : null;
	}

/*	public int getKind() {
		CDataObject data = getData(false);
		return data != null ? data.getKind() : 0;
	}
*/
	@Override
	public String getName() {
		CDataObject data = getData(false);
		return data != null ? data.getName() : null;
	}

	void setConfiguration(CConfigurationDescription cfg){
		fConfiguration = cfg;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	protected IProject getProject(){
		ICConfigurationDescription cfg = getConfiguration();
		if(cfg == null)
			return null;

		ICProjectDescription projDes = cfg.getProjectDescription();
		if(projDes == null)
			return null;

		return projDes.getProject();
	}

	/**
	 * This method is intended for debugging purpose only.
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "name=["+getName()+"], id=["+getId()+"]";
	}
}
