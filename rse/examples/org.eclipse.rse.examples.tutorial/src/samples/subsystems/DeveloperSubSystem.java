/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Adapted original tutorial code to Open RSE.
 ********************************************************************************/

package samples.subsystems;

import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;

import samples.model.DeveloperResource;
import samples.model.TeamResource;

/**
 * This is our subsystem, which manages the remote connection and resources for
 *  a particular system connection object.
 */
public class DeveloperSubSystem extends SubSystem
{
	private TeamResource[] teams; // faked-out master list of teams
	private Vector devVector = new Vector(); // faked-out master list of developers
	private static int employeeId = 123456; // employee Id factory	

	/**
	 * @param host
	 * @param connectorService
	 */
	public DeveloperSubSystem(IHost host, IConnectorService connectorService) {
		super(host, connectorService);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeSubSystem(IProgressMonitor monitor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystem#uninitializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void uninitializeSubSystem(IProgressMonitor monitor) {
	}

	/**
	 * For drag and drop, and clipboard support of remote objects.
	 *   
	 * Return the remote object within the subsystem that corresponds to
	 * the specified unique ID.  Because each subsystem maintains it's own
	 * objects, it's the responsability of the subsystem to determine
	 * how an ID (or key) for a given object maps to the real object.
	 * By default this returns null.
	 *  
	 * @param key internal unique ID for object 
	 * @return Object identified by the given key
	 */
	public Object getObjectWithAbsoluteName(String key)
	{
		//  Functional opposite of getAbsoluteName(Object) in our resource adapters
		if (key.startsWith("Team_")) //$NON-NLS-1$
		{
			String teamName = key.substring(5);
			TeamResource[] allTeams = getAllTeams();
			for (int idx=0; idx<allTeams.length; idx++)
			   if (allTeams[idx].getName().equals(teamName))
			     return allTeams[idx];
		}
		else if (key.startsWith("Devr_")) //$NON-NLS-1$
		{
			String devrId = key.substring(5);
			DeveloperResource[] devrs = getAllDevelopers();
			for (int idx=0; idx<devrs.length; idx++)
			  if (devrs[idx].getId().equals(devrId))
			    return devrs[idx];            	
		}
		return null; 
	}

	/**
	 * When a filter is expanded, this is called for each filter string in the filter.
	 * Using the criteria of the filter string, it must return objects representing remote resources.
	 * For us, this will be an array of TeamResource objects.
	 * 
	 * @param monitor - the progress monitor in effect while this operation performs
	 * @param filterString - one of the filter strings from the expanded filter.
	 */
	protected Object[] internalResolveFilterString(IProgressMonitor monitor, String filterString)
         throws java.lang.reflect.InvocationTargetException,
                java.lang.InterruptedException                
	{
		int slashIdx = filterString.indexOf('/');
		if (slashIdx < 0)
		{
			// Fake it out for now and return dummy list. 
			// In reality, this would communicate with remote server-side code/data.
			TeamResource[] allTeams = getAllTeams();
			
			// Now, subset master list, based on filter string...
			NamePatternMatcher subsetter = new NamePatternMatcher(filterString);
			Vector v = new Vector();
			for (int idx=0; idx<allTeams.length; idx++)
			{
				if (subsetter.matches(allTeams[idx].getName()))
				  v.addElement(allTeams[idx]);
			}		
			TeamResource[] teams = new TeamResource[v.size()];
			for (int idx=0; idx<v.size(); idx++)
			   teams[idx] = (TeamResource)v.elementAt(idx);
			return teams;
		}
		else
		{
			String teamName = filterString.substring(0, slashIdx);
			String devrName = filterString.substring(slashIdx+1);
			TeamResource[] allTeams = getAllTeams();
			TeamResource match = null;
			for (int idx=0; (match==null) && (idx<allTeams.length); idx++)
			   if (allTeams[idx].getName().equals(teamName))
			     match = allTeams[idx];
			if (match != null)
			{
				DeveloperResource[] allDevrs = match.getDevelopers();
				// Now, subset master list, based on filter string...
				NamePatternMatcher subsetter = new NamePatternMatcher(devrName);
				Vector v = new Vector();
				for (int idx=0; idx<allDevrs.length; idx++)
			    {
			    	if (subsetter.matches(allDevrs[idx].getName()))
			    	  v.addElement(allDevrs[idx]);
			   	}		
			   	DeveloperResource[] devrs = new DeveloperResource[v.size()];
			   	for (int idx=0; idx<v.size(); idx++)
			   	   devrs[idx] = (DeveloperResource)v.elementAt(idx);
			   	return devrs;	
			}
		}
		return null;
	}

	/**
	 * When a remote resource is expanded, this is called to return the children of the resource, if
	 * the resource's adapter states the resource object is expandable. <br>
	 * For us, it is a Team resource that was expanded, and an array of Developer resources will be returned.
	 * 
	 * @param monitor - the progress monitor in effect while this operation performs
	 * @param parent - the parent resource object being expanded
	 * @param filterString - typically defaults to "*". In future additional user-specific quick-filters may be supported.
	 */
	protected Object[] internalResolveFilterString(IProgressMonitor monitor, Object parent, String filterString)
         throws java.lang.reflect.InvocationTargetException,
                java.lang.InterruptedException
	{
		// typically we ignore the filter string as it is always "*" 
		//  until support is added for "quick filters" the user can specify/select
		//  at the time they expand a remote resource.
		
		TeamResource team = (TeamResource)parent;
		return team.getDevelopers();
	}

	// ------------------	
	// Our own methods...
	// ------------------
	/**
	 * Get the list of all teams. Normally this would involve a trip the server, but we 
	 *  fake it out and return a hard-coded local list. 
	 * @return array of all teams
	 */
	public TeamResource[] getAllTeams()
	{
		if (teams == null) 
		  teams = createTeams("Team ", 4);
		return teams;		
	}
	/**
	 * Get the list of all developers. Normally this would involve a trip the server, but we 
	 *  fake it out and return a hard-coded local list. 
	 * @return array of all developers
	 */
	public DeveloperResource[] getAllDevelopers()
	{
		DeveloperResource[] allDevrs = new DeveloperResource[devVector.size()];
		for (int idx=0; idx<allDevrs.length; idx++)
		  allDevrs[idx] = (DeveloperResource)devVector.elementAt(idx);
		return allDevrs;		
	}
	/*
	 * Create and return a dummy set of teams
	 */
	private TeamResource[] createTeams(String prefix, int howMany)
	{
		TeamResource[] teams = new TeamResource[howMany];
		for (int idx=0; idx<teams.length; idx++)
		{
			teams[idx] = new TeamResource(this);
			teams[idx].setName(prefix + (idx+1));
			teams[idx].setDevelopers(createDevelopers(teams[idx].getName()+" developer",idx+1));
		}
		return teams;
	}

	/*
	 * Create and return a dummy set of developers
	 */
	private DeveloperResource[] createDevelopers(String prefix, int nbr)
	{
		DeveloperResource[] devrs = new DeveloperResource[nbr];
		for (int idx=0; idx<devrs.length; idx++)
		{
			devrs[idx] = new DeveloperResource(this);
			devrs[idx].setName(prefix + (idx+1));
			devrs[idx].setId(Integer.toString(employeeId++));
			devrs[idx].setDeptNbr(Integer.toString((idx+1)*100));
			devVector.add(devrs[idx]); // update master list
		}
		return devrs;
	}

}
