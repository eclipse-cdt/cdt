package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.dd.dsf.model.IDataModelService;

/**
 * Service for accessing debugger symbols.  This service builds on the Modules
 * service, but not all debuggers provide access for parsing symbols so this
 * service is separated.
 * @see IModules
 */
public interface ISymbols extends IDataModelService {
    public interface SymbolObjectDMC extends IDataModelContext<SymbolObjectData> {}
    
    /**
     * Data about a debug symbol.  
     */
    public interface SymbolObjectData extends IDataModelData {
        String getName();
        String getTypeName();
        String getFilepath();
    }

    /**
     * Indicates that the list of symbol objects is changed.  Parsing debug 
     * symbols can be a long running operation (order of 10's of seconds or 
     * minues), so it is useful for the service to provide access to the data
     * even while it's still parsing.  This event may be issued periodically
     * by the service to indicate that a section of debug symbols has been 
     * parsed.
     * TODO: This is not an IModelEvent because the context of this event is 
     * the whole service.  This needs to be fixed.
     */
    public interface SymbolDataChanged extends IDataModelEvent<IModules.ISymbolDMC> {}
    
    /**
     * Retrieves the list of symbols.
     * @param symCtx Symbols context to retrieve symbols for.
     * @param done Return token.  The return value is an iterator (rather than 
     * array) since there could be a very large number of symbols returned.
     */
    public void getSymbols(IModules.ISymbolDMC symCtx, GetDataDone<Iterable<SymbolObjectDMC>> done);
}
