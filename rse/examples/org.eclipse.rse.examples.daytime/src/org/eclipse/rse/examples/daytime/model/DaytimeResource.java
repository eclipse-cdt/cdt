/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.examples.daytime.model;

import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * This models the time of day on a remote system.
 * It might as well model any other remote resource.
 */
public class DaytimeResource extends AbstractResource {

	private String fDaytime;
	
	/** Default constructor */
	public DaytimeResource() {
		super();
	}
	
	/** 
	 * Constructor when parent subsystem is given 
	 * @param subsystem the parent subsystem
	 */
	public DaytimeResource(ISubSystem subsystem) {
		super(subsystem);
	}
	
	public String getDaytime() {
		return fDaytime;
	}

	public void setDaytime(String daytime) {
		fDaytime = daytime;
	}
	
}
