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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildIOType;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class BuildResource implements IBuildResource {
	private List fDepArgs = new ArrayList();
	private BuildIOType fProducerArg;
	private boolean fNeedsRebuild;
	private boolean fIsRemoved;
	private IPath fLocation; 
	private IPath fFullPath; 
	private boolean fIsProjectRc;
	private BuildDescription fInfo;

	protected BuildResource(BuildDescription info, IResource rc){
		this(info, info.calcResourceLocation(rc), rc.getFullPath());
	}

	protected BuildResource(BuildDescription info, IPath location, IPath fullPath){
		fLocation = location;
		fInfo = info;
		fFullPath = fullPath;
		if(fFullPath != null)
			fIsProjectRc = fFullPath.segment(0).equals(info.getProject().getName());

		info.resourceCreated(this);
		
		if(DbgUtil.DEBUG)
			DbgUtil.trace("resource " + location + " created");	//$NON-NLS-1$	//$NON-NLS-2$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#getLocation()
	 */
	public IPath getLocation() {
		return fLocation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#getFullPath()
	 */
	public IPath getFullPath() {
		return fFullPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#getProducerIOType()
	 */
	public IBuildIOType getProducerIOType() {
		return fProducerArg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#getDependentIOTypes()
	 */
	public IBuildIOType[] getDependentIOTypes() {
		return (BuildIOType[])fDepArgs.toArray(new BuildIOType[fDepArgs.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#needsRebuild()
	 */
	public boolean needsRebuild() {
		return fNeedsRebuild;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#isRemoved()
	 */
	public boolean isRemoved() {
		return fIsRemoved;
	}
	
	public void setRemoved(boolean removed) {
		if(DbgUtil.DEBUG){
			if(removed)
				DbgUtil.trace("REMOVED state: resource " + DbgUtil.resourceName(this));
		}
		fIsRemoved = removed;
		if(fIsRemoved)
			fNeedsRebuild = false;
	}
	
	public void setRebuildState(boolean rebuild){
		fNeedsRebuild = rebuild;
	}

	protected void addToArg(BuildIOType arg){
		if(arg.isInput()){
			fDepArgs.add(arg);
		} else {
			if(fProducerArg == null){
				fProducerArg = arg;
			} else {
				String err = "ProducerArgument not null!!!\n";	//$NON-NLS-1$
				if(DbgUtil.DEBUG){
					err = err + "curent producer: " + DbgUtil.dumpStep(fProducerArg.getStep()) + "\n producer attempt: " + DbgUtil.dumpStep(arg.getStep());	//$NON-NLS-1$	//$NON-NLS-2$
				}
					
				throw new IllegalArgumentException(err);
			}
		}
	}
	
	void removeFromArg(BuildIOType arg){
		if(arg.isInput()){
			fDepArgs.remove(arg);
		} else {
			if(fProducerArg == arg){
				fProducerArg = null;
			}else
				throw new IllegalArgumentException("Resource is not produced by this arg!!!");	//$NON-NLS-1$
		}
	}
	
	public boolean isProjectResource() {
		return fIsProjectRc;
	}
	
	BuildIOType[][] clear(){
		BuildIOType types[][] = new BuildIOType[2][];
		types[0] = new BuildIOType[1];
		types[0][0] = fProducerArg;
		BuildIOType outs[] = (BuildIOType[])getDependentIOTypes();
		types[1] = outs;
		
		if(fProducerArg != null)
			fProducerArg.removeResource(this);
		for(int i = 0; i < outs.length; i++){
			outs[i].removeResource(this);
		}
		
		return types;
	}
	
	BuildIOType[][] remove(){
		BuildIOType types[][] = clear();
		
		if(DbgUtil.DEBUG)
			DbgUtil.trace("resource " + DbgUtil.resourceName(this) + " removed");	//$NON-NLS-1$	//$NON-NLS-2$
		
		fInfo.resourceRemoved(this);
		fInfo = null;
		
		return types;
	}
	
	public IBuildDescription getBuildDescription(){
		return fInfo;
	}

	public IBuildStep[] getDependentSteps() {
		Set set = new HashSet();
		for(Iterator iter = fDepArgs.iterator(); iter.hasNext();){
			set.add(((BuildIOType)iter.next()).getStep());
		}
		return (BuildStep[])set.toArray(new BuildStep[set.size()]);
	}

	public IBuildStep getProducerStep() {
		if(fProducerArg != null)
			return fProducerArg.getStep();
		return null;
	}

}
