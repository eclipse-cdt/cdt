package org.eclipse.rse.subsystems.files.dstore.subsystem;

import java.util.Vector;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;

public class DStoreWindowsFileSubSystemConfiguration extends DStoreFileSubSystemConfiguration
{

	protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr)
	{
		ISystemFilterPool pool = null;
		try {
		  // -----------------------------------------------------
		  // create a pool named filters
		  // -----------------------------------------------------      			  
		  pool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), true); // true=>is deletable by user
		  if (pool == null) // hmmm, why would this happen?
		  {
			SystemBasePlugin.logError("Creating default filter pool "+getDefaultFilterPoolName(mgr.getName(), getId())+" for mgr "+mgr.getName()+" failed.",null);
			return null;
		  }
		 if (isUserPrivateProfile(mgr))
		  {
		      Vector filterStrings = new Vector();
		      RemoteFileFilterString defaultFilterString = new RemoteFileFilterString(this);

		      filterStrings.add(defaultFilterString.toString());
		      //System.out.println("creating filter...");	
		      String filterName = SystemFileResources.RESID_FILTER_DRIVES;

		      mgr.createSystemFilter(pool, filterName, filterStrings);
		      
		      /*
		      // Create 'My Home' filter for local (should apply to both _isWindows and linux clients)
	    	  filterName = SystemFileResources.RESID_FILTER_MYHOME;
	    	  RemoteFileFilterString myDocsFilterString = new RemoteFileFilterString(this);
	    	  myDocsFilterString.setPath(System.getProperty("user.home") + getSeparator());
	    	  Vector myDocsFilterStrings = new Vector();
	    	  myDocsFilterStrings.add(myDocsFilterString.toString());
	    	  mgr.createSystemFilter(pool, filterName, myDocsFilterStrings);
	    	  */
	    	  

		      // ----------------------
		      // "My Home" filter...
		      // ----------------------
		      filterStrings = new Vector();
		      RemoteFileFilterString myHomeFilterString = new RemoteFileFilterString(this);
		      myHomeFilterString.setPath(getSeparator());
		      filterStrings.add(".\\*");
		      ISystemFilter filter = mgr.createSystemFilter(pool, SystemFileResources.RESID_FILTER_MYHOME,filterStrings);
		      filter.setNonChangable(true);
		      filter.setSingleFilterStringOnly(true);


		      //System.out.println("filter created");		  
		      // -----------------------------------------------------
		      // add a default named filter for integrated file system    	
		      // -----------------------------------------------------      	
		      //filterStrings = new Vector();
		      //filterStrings.add(new AS400IFSFilterString().toString());
		      //mgr.createSystemFilter(pool,rb.getString(IAS400Constants.RESID_IFS_LIST),filterStrings);
		  }
		}
		 catch (Exception exc)
			{
				SystemBasePlugin.logError("Error creating default filter pool",exc);
			}
			return pool;
	}

	public String getSeparator()
	{
		return "\\";
	}

	public char getSeparatorChar()
	{
		return '\\';
	}

	

}
