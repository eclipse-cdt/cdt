/*
 * Created on 19-Aug-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class MakeTarget implements IMakeTarget {

	MakeTarget(String targetBuilderID, String targetName) {
		// dinglis-TODO Auto-generated constructor stub
	}

	void setName(String name) {
		// dinglis-TODO Auto-generated method stub
	}

	void setContainer(IContainer container) {
		// dinglis-TODO Auto-generated method stub		
	}

	public String getName() {
		// dinglis-TODO Auto-generated method stub
		return null;
	}

	public String getTargetBuilderID() {
		// dinglis-TODO Auto-generated method stub
		return null;
	}

	public String getBuilderID() {
		// dinglis-TODO Auto-generated method stub
		return null;
	}

	public boolean isStopOnError() {
		// dinglis-TODO Auto-generated method stub
		return false;
	}

	public void setStopOnError(boolean stopOnError) {
		// dinglis-TODO Auto-generated method stub

	}

	public boolean isDefaultBuildCmd() {
		// dinglis-TODO Auto-generated method stub
		return false;
	}

	public void setUseDefaultBuildCmd(boolean useDefault) {
		// dinglis-TODO Auto-generated method stub

	}

	public IPath getBuildCommand() {
		// dinglis-TODO Auto-generated method stub
		return null;
	}

	public void setBuildCommand(IPath command) {
		// dinglis-TODO Auto-generated method stub

	}

	public String getBuildArguments() {
		// dinglis-TODO Auto-generated method stub
		return null;
	}

	public void setBuildArguments() {
		// dinglis-TODO Auto-generated method stub

	}

	public IContainer getContainer() {
		// dinglis-TODO Auto-generated method stub
		return null;
	}

	public void build(IProgressMonitor monitor) throws CoreException {
		// dinglis-TODO Auto-generated method stub
		
	}
}
