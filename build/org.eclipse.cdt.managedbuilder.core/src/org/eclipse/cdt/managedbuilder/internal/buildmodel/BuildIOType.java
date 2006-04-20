/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.buildmodel.IBuildIOType;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IOutputType;

public class BuildIOType implements IBuildIOType {
	private BuildStep fStep;
	private List fResources = new ArrayList();
	private boolean fIsInput;
	private boolean fIsPrimary;
	private String fLinkId;
	private IBuildObject fIoType;

	protected BuildIOType(BuildStep action, boolean input, boolean primary,/* BuildPattern pattern,*/ IBuildObject ioType ) {
		fStep = action;
		fIsInput = input;
		fIsPrimary = primary;
		if(ioType != null){
			if(input){
				if(ioType instanceof IInputType)
					fLinkId = ((IInputType)ioType).getBuildVariable();
				else
					throw new IllegalArgumentException("wrong arg");	//$NON-NLS-1$
			} else {
				if(ioType instanceof IOutputType) {
					fLinkId = ((IOutputType)ioType).getBuildVariable();
				} else
					throw new IllegalArgumentException("wrong arg");	//$NON-NLS-1$
			}
			fIoType = ioType;
		} else {
			//TODO
		}
		((BuildDescription)fStep.getBuildDescription()).typeCreated(this);
	}

	public IBuildResource[] getResources() {
		return (BuildResource[])fResources.toArray(new BuildResource[fResources.size()]);
	}

	public IBuildStep getStep() {
		return fStep;
	}

	public void addResource(BuildResource rc){
		fResources.add(rc);
		rc.addToArg(this);
		if(DbgUtil.DEBUG)
			DbgUtil.trace("resource " + DbgUtil.resourceName(rc) + " added as "  	//$NON-NLS-1$	//$NON-NLS-2$
					+ (fIsInput ? "input" : "output")	//$NON-NLS-1$	//$NON-NLS-2$
					+ " to the action " + DbgUtil.stepName(fStep));	//$NON-NLS-1$
		
		((BuildDescription)fStep.getBuildDescription()).resourceAddedToType(this, rc);
	}
	
	public void removeResource(BuildResource rc){
		fResources.remove(rc);
		rc.removeFromArg(this);
		
		if(DbgUtil.DEBUG)
			DbgUtil.trace("resource " + DbgUtil.resourceName(rc) + " removed as "  	//$NON-NLS-1$	//$NON-NLS-2$
					+ (fIsInput ? "input" : "output")	//$NON-NLS-1$	//$NON-NLS-2$
					+ " from the action " + DbgUtil.stepName(fStep));	//$NON-NLS-1$

		((BuildDescription)fStep.getBuildDescription()).resourceRemovedFromType(this, rc);
	}

	public boolean isInput() {
		return fIsInput;
	}
	
	public boolean isPrimary(){
		return fIsPrimary;
	}
	
	public String getLinkId(){
		if(!fIsInput && fStep.getTool() != null && /*(fLinkId == null || fLinkId.length() == 0) && */ 
				fStep.getTool().getCustomBuildStep()){
			IBuildResource rcs[] = getResources();
			if(rcs.length != 0){
				BuildDescription.ToolAndType tt = ((BuildDescription)fStep.getBuildDescription()).getToolAndType((BuildResource)rcs[0], false);
				if(tt != null){
					IInputType type = tt.fTool.getPrimaryInputType();
					if(type != null)
						fLinkId = type.getBuildVariable();
				} else {
				}
			}
				
		}
		return fLinkId;
	}

	public IBuildObject getIoType(){
		return fIoType;
	}
	
	BuildResource[] remove(){
		BuildResource rcs[] = (BuildResource[])getResources();
		
		for(int i = 0; i < rcs.length; i++){
			removeResource(rcs[i]);
		}
		
		fStep = null;
		return rcs;
	}
}
