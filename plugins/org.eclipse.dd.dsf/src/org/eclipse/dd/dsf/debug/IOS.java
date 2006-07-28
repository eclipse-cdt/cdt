package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelService;

/**
 * Provides generic access to operating system objects and methods to 
 * manipulate those objects.  This is a much more extensive interface than
 * the NativeProcesses service but for simple debugging, it serves the same 
 * purpose:  to list/create/terminate processes and attach debugger to them.
 * <p>
 * TODO: need methods for performing actions on objects (create, terminate, 
 * etc).
 * @see INativeProcesses
 */
public interface IOS extends IDataModelService {
    
    /**
     * Context object for the whole OS, for debuggers that support 
     * debugging multiple targets/cores simultaneously.
     */
    public interface IOSDMC extends IDataModelContext<IOSData> {}

    /**
     * Data object describing OS info
     */
    public interface IOSData extends IDataModelData {
        String getName();
        String getDescription();
        String getVersion();        
    }

    /**
     * Context for a OS object type, such as process, kernel task, semaphore, etc.
     */
    public interface IObjectTypeDMC extends IDataModelContext<IObjectTypeData> {}
    
    /**
     * Description data for a OS object type.
     */
    public interface IObjectTypeData extends IDataModelData {
        String getName();
        String getDescription();
        String getSingularName();
        String getPluralName();
        boolean hasExecutionContext();
        boolean hasModulesContext();
        boolean hasMemoryContext();
    }

    /**
     * OS object context.
     */
    public interface IObjectDMC extends IDataModelContext<IObjectData> {}

    /**
     * Description data for an OS object.
     */
    public interface IObjectData extends IDataModelData {
        String getName();
        String getID();
        boolean canAttachDebugger();
        boolean isDebuggerAttached(); 
        IRunControl.IExecutionDMC getExecutionDMC();
        IModules.ISymbolDMC getSymbolDMC();
        IMemory.IMemoryContext getMemoryContext();
    }
    
    /**
     * Retrieves list of OS object types.
     * @param os OS context.
     * @param parent Optional parent type.
     * @param done Return token.
     */
    public void getObjectTypes(IOSDMC os, IObjectTypeDMC parent, GetDataDone<IObjectTypeDMC[]> done);
    
    /**
     * Retrieves list of OS objects for given type.
     * @param os OS context.
     * @param type The object type.
     * @param parent Optional parent of the requested objects.
     * @param done Return token.
     */
    public void getObjects(IOSDMC os, IObjectTypeDMC type, IObjectDMC parent, GetDataDone<IObjectDMC[]> done);
    
    /**
     * Attaches the debugger to given OS object context.
     */
    public void attachDebuggerToObject(IObjectDMC objectDmc, Done done);
}
