package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * @since 4.2
 */
public interface IGDBHardwareAndOS2 extends IGDBHardwareAndOS {
	
    /** Information about OS resource class. 
     * @since 4.2*/
    public interface IResourceClass
    {
    	/** The id of this resource class, used in GDB requests. */
    	public String getId();
    	/** Human-friendly description of this class, suitable for direct display in UI. */
    	public String getHumanDescription();
    }
    
    /**
     * Return a list of OS resource classes GDB knows about 
     * @param dmc
     * @param rm
     * @since 4.2
     */
    public void getResourceClasses(IDMContext dmc, DataRequestMonitor<IResourceClass[]> rm);
        
    /** Information about OS resources of specific resource class
     * This is conceptually a table. GDB provides column headers, and
     * data rows, but does not provide any additional information about
     * the meaning
	 * @since 4.2
	 */
    public interface IResourcesInformation
    {
    	/** Return the names of the columns in resource table. */
    	public String[] getColumnNames();
    	/** Returns rows of the resource table. Each element is an array
    	 * of the size equal to getColumnNames().length
    	 */
    	public String[][] getContent();
    }
    
    /**
	 * @since 4.2
	 */
    void getResourcesInformation(IDMContext dmc, String resourceClassId, DataRequestMonitor<IResourcesInformation> rm);
}
