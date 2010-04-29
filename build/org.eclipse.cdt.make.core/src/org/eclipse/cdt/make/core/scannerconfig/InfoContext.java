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
package org.eclipse.cdt.make.core.scannerconfig;

import org.eclipse.core.resources.IProject;

public final class InfoContext {
	private IProject fProject;
	private String fInstanceId;

	public InfoContext(IProject project){
		this(project, null);
	}

	public InfoContext(IProject project, String instanceId){
		this.fProject = project;
		this.fInstanceId = instanceId != null ? instanceId : "";  //$NON-NLS-1$
	}
	
	public String getInstanceId(){
		return fInstanceId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		
		if(!(obj instanceof InfoContext))
			return false;
		
		InfoContext other = (InfoContext)obj;
		if(fProject == null){
			if(other.fProject != null)
				return false;
		} else if(!fProject.equals(other.fProject))
			return false;
		
		if(!fInstanceId.equals(other.fInstanceId))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int code = fProject != null ? fProject.hashCode() : 0;
		
		code += fInstanceId.hashCode();
		
		return code;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		if(fProject != null)
			buf.append(fProject.toString());
		else
			buf.append("no project");  //$NON-NLS-1$
		if(fInstanceId.length() != 0){
			buf.append(" , instance: ");  //$NON-NLS-1$
			buf.append(fInstanceId);
		}
	
		return buf.toString();
	}
	
	/**
	 * a convenience method that specifies whether this is a default context,
	 * i.e. the one defined for the project with no extension filters
	 * 
	 * @return boolean
	 */
	public boolean isDefaultContext(){
//		if(fProject == null)
//			return false;
		
		if(fInstanceId.length() != 0)
			return false;
		
		return true;
	}
	
	public IProject getProject(){
		return fProject;
	}
}
