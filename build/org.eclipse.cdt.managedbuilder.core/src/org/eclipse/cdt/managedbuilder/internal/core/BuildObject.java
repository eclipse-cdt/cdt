/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;


import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PluginVersionIdentifier;

public class BuildObject implements IBuildObject {

	protected String id;
	protected String name;
	
	protected PluginVersionIdentifier version = null;
	protected String managedBuildRevision = null;
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}
	
	/**
	 * @return Returns the managedBuildRevision.
	 */
	public String getManagedBuildRevision() {
		return managedBuildRevision;
	}
	
	/**
	 * @return Returns the version.
	 */
	public PluginVersionIdentifier getVersion() {
			return version;
	}

	/**
	 * @param version The version to set.
	 */
	public void setVersion(PluginVersionIdentifier version) {
		this.version = version;
	}

	/**
	 * @return Returns the Id without the version (if any).
	 */
	public String getBaseId() {
		return ManagedBuildManager.getIdFromIdAndVersion(id);
	}

	
	public PluginVersionIdentifier getVersionFromId() {
		String versionNumber;
		IStatus status = null;
	
		
		versionNumber = ManagedBuildManager.getVersionFromIdAndVersion( getId());
		
		if( versionNumber == null) {
			// It means, Tool Integrator either not provided version information in 'id' or  provided in wrong format,
			// So get the default version based on 'managedBuildRevision' attribute.
								
			if ( getManagedBuildRevision() != null) {
				PluginVersionIdentifier tmpManagedBuildRevision = new PluginVersionIdentifier( getManagedBuildRevision() );
				if (tmpManagedBuildRevision.isEquivalentTo(new PluginVersionIdentifier("1.2.0")) )	//$NON-NLS-1$
					versionNumber = "0.0.1";	//$NON-NLS-1$
				else if (tmpManagedBuildRevision.isEquivalentTo(new PluginVersionIdentifier("2.0.0")) )	//$NON-NLS-1$
					versionNumber = "0.0.2";	//$NON-NLS-1$
				else if (tmpManagedBuildRevision.isEquivalentTo(new PluginVersionIdentifier("2.1.0")) )	//$NON-NLS-1$
					versionNumber = "0.0.3";	//$NON-NLS-1$
				else
					versionNumber = "0.0.4";	//$NON-NLS-1$
			} else {
				versionNumber = "0.0.0";	//$NON-NLS-1$
			}
		}
		return new PluginVersionIdentifier(versionNumber);
	}

	public void setManagedBuildRevision(String managedBuildRevision) {
		this.managedBuildRevision = managedBuildRevision;
	}
	
	/*
	 * updates revision for this build object and all its children
	 */
	public void updateManagedBuildRevision(String revision){
		setManagedBuildRevision(revision);
		setVersion(getVersionFromId());
	}
}
