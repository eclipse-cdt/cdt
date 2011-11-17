/*******************************************************************************
 * Copyright (c) 2006, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.net.URI;
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class BuildResource implements IBuildResource {
	private List<BuildIOType> fDepArgs = new ArrayList<BuildIOType>();
	private BuildIOType fProducerArg;
	private boolean fNeedsRebuild;
	private boolean fIsRemoved;
	private IPath fFullWorkspacePath;
	private boolean fIsProjectRc;
	private BuildDescription fInfo;
	private URI fLocationURI;

	protected BuildResource(BuildDescription info, IResource rc){
		this(info, info.calcResourceLocation(rc), rc.getLocationURI());
	}

	protected BuildResource(BuildDescription info, IPath fullWorkspacePath, URI locationURI){

		if(locationURI == null)
			throw new IllegalArgumentException(); // must point to somewhere!

		fLocationURI = locationURI;

		fFullWorkspacePath = fullWorkspacePath;
		fInfo = info;

		fIsProjectRc = (fullWorkspacePath != null);

		info.resourceCreated(this);

		if(DbgUtil.DEBUG)
			DbgUtil.trace("resource " + fullWorkspacePath + " created");	//$NON-NLS-1$	//$NON-NLS-2$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#getLocation()
	 */
	@Override
	public IPath getLocation() {
		if(fFullWorkspacePath == null) {
			return new Path(fLocationURI.getPath());
		}

		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(fFullWorkspacePath);
		if(resource == null) {
			return new Path(fLocationURI.getPath());
		}

		if(resource.getLocation() != null)
			return resource.getLocation();
		else
			return new Path(fLocationURI.getPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#getFullPath()
	 */
	@Override
	public IPath getFullPath() {
		return fFullWorkspacePath;
		//return new Path(getLocationURI().getPath().toString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#getProducerIOType()
	 */
	@Override
	public IBuildIOType getProducerIOType() {
		return fProducerArg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#getDependentIOTypes()
	 */
	@Override
	public IBuildIOType[] getDependentIOTypes() {
		return fDepArgs.toArray(new BuildIOType[fDepArgs.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#needsRebuild()
	 */
	@Override
	public boolean needsRebuild() {
		return fNeedsRebuild;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildResource#isRemoved()
	 */
	@Override
	public boolean isRemoved() {
		return fIsRemoved;
	}

	public void setRemoved(boolean removed) {
		if(DbgUtil.DEBUG){
			if(removed)
				DbgUtil.trace("REMOVED state: resource " + DbgUtil.resourceName(this)); //$NON-NLS-1$
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
			} else if(fProducerArg.getStep() == fInfo.getInputStep()) {
				BuildStep inStep = (BuildStep)fInfo.getInputStep();
				inStep.removeResource(fProducerArg, this, true);
				fProducerArg = arg;
			} else {
				String err = "ProducerArgument not null!!!\n";	//$NON-NLS-1$

				String rcName = DbgUtil.resourceName(this);
				String step1Name = DbgUtil.stepName(fProducerArg.getStep());
				String step2Name = DbgUtil.stepName(arg.getStep());
				String rcs[] = new String[]{rcName, step1Name, step2Name};

				String externalizedErr = BuildModelMessages.getFormattedString("BuildResource.0", rcs); //$NON-NLS-1$

				if(DbgUtil.DEBUG){
					err = err + externalizedErr + "curent producer: " + DbgUtil.dumpStep(fProducerArg.getStep()) + "\n producer attempt: " + DbgUtil.dumpStep(arg.getStep());	//$NON-NLS-1$	//$NON-NLS-2$
				}


				throw new IllegalArgumentException(externalizedErr);
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

	@Override
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

	@Override
	public IBuildDescription getBuildDescription(){
		return fInfo;
	}

	@Override
	public IBuildStep[] getDependentSteps() {
		Set<IBuildStep> set = new HashSet<IBuildStep>();
		for(Iterator<BuildIOType> iter = fDepArgs.iterator(); iter.hasNext();){
			set.add(iter.next().getStep());
		}
		return set.toArray(new BuildStep[set.size()]);
	}

	@Override
	public IBuildStep getProducerStep() {
		if(fProducerArg != null)
			return fProducerArg.getStep();
		return null;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("BR "); //$NON-NLS-1$
		IPath fullPath = getFullPath();
		if(fullPath != null)
			buf.append("WSP|").append(fullPath); //$NON-NLS-1$
		else
			buf.append("FS|").append(getLocation()); //$NON-NLS-1$

		return buf.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource#getLocationURI()
	 */
	@Override
	public URI getLocationURI() {
		return fLocationURI;
	}

}
