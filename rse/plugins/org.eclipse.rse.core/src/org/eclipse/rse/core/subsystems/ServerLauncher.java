/********************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * David McKnight  (IBM)  - [224671] [api] org.eclipse.rse.core API leaks non-API types
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;
import java.util.Arrays;
import java.util.List;

import org.eclipse.rse.core.model.ILabeledObject;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.internal.core.RSECoreMessages;

/**
 * Abstract class intended to be extended to provide a means for starting a
 * remote server from the client. DStore-based connector services use this to
 * determine whether to start a sever via daemon, REXEC, or some other
 * mechanism. For systems that don't need to start remote servers from RSE, this
 * is not needed.
 *
 * @since 3.0 moved from non-API to API
 */
public abstract class ServerLauncher extends RSEModelObject implements IServerLauncherProperties, ILabeledObject
{

	protected String _name;
	private String _label = null;
	protected IConnectorService _connectorService;

	/**
	 * Constructs a server launcher
	 * @param name name of the server launcher
	 * @param service the associated connector service
	 */
	protected ServerLauncher(String name, IConnectorService service)
	{
		super();
		_name = name;
		_connectorService = service;
	}

	/**
	 * Returns the name of the server launcher
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Returns the label to display in a ui for the server launcher
	 */
	public String getLabel() {
		if (_label != null) return _label;
		return _name;
	}

	/**
	 * Sets the label to use for display in a ui for the server launcher
	 */
	public void setLabel(String label) {
		_label = label;
		setDirty(true);
	}

	/**
	 * Returns the description of the server launcher
	 */
	public String getDescription()
	{
		return RSECoreMessages.RESID_MODELOBJECTS_SERVERLAUNCHER_DESCRIPTION;
	}

	/**
	 * Returns the associated connector service
	 */
	public IConnectorService getConnectorService()
	{
		return _connectorService;
	}

	public IRSEPersistableContainer getPersistableParent() {
		return _connectorService;
	}

	public IRSEPersistableContainer[] getPersistableChildren() {
		List children = Arrays.asList(getPropertySets());
		IRSEPersistableContainer[] result = new IRSEPersistableContainer[children.size()];
		children.toArray(result);
		return result;
	}

	/**
	 * Clone the contents of this server launcher into the given server launcher
	 * <i>Your sublcass must override this if you add additional attributes! Be sure
	 *  to call super.cloneServerLauncher(newOne) first.</i>
	 * @return the given new server launcher, for convenience.
	 */
	public IServerLauncherProperties cloneServerLauncher(IServerLauncherProperties newOne)
	{
		newOne.addPropertySets(getPropertySets());
		return newOne;
	}

	/**
	 * Commits the associated connector service to be persisted
	 */
	public boolean commit()
	{
		return getConnectorService().getHost().commit();
	}



} //ServerLauncherImpl