package org.eclipse.dd.dsf.debug;

import java.math.BigInteger;

import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelEvent;
import org.eclipse.dd.dsf.model.IDataModelService;

/**
 * Debugger service representing module handling logic of a debugger.  
 * <br>
 * TODO: I meant this as a replacement for Application service as well as the 
 * registry API in Riverbed 1.  But I still don't fully understand the format 
 * of the symbol data that is stored in the registry, so that needs to be added
 * to this interface.  
 */
public interface IModules extends IDataModelService {
    
    /**
     * Symbol context represents the space into which module symbols are loaded.
     * Traditionally symbols are loaded in context of a process, but for other
     * types of debugging, like kernel or no-OS debugging, it's useful to 
     * separate the concept of a symbol context from a process.
     */
    public interface ISymbolDMC extends IDataModelContext {}
    
    /**
     * Module context represents a single module that is loaded.
     */
    public interface IModuleDMC extends IDataModelContext<ModuleData> {}
    
    /**
     * Event indicating a change in the symbol information for given context.
     */
    public interface ModulesChangedEvent extends IDataModelEvent<ISymbolDMC> {}

    /**
     * Specific event identifying that a new module was loaded into a 
     * symbol context.
     */
    public interface ModuleLoadedEvent extends ModulesChangedEvent {
        /** Returns context of the module that was loaded */
        IModuleDMC getLoadedModuleContext();
    }
    
    public interface ModuleUnloadedEvent extends ModulesChangedEvent {
        /** Returns context of the module that was un-loaded */
        IModuleDMC getUnloadedModuleContext();
    }

    /**
     * Object representing a unuqie location in a symbol context, based on
     * a module, section, and address offset within the seciton.
     */
    public final class ModuleSectionOffset {
        private final IModuleDMC fModule;
        private final Section fSection;
        private final BigInteger fOffset;

        public IModuleDMC getModule() { return fModule; }
        public Section getSection() { return fSection; }
        public BigInteger getOffset() { return fOffset; }
        
        public ModuleSectionOffset(IModuleDMC module, Section section, BigInteger offset) {
            this.fModule = module;
            this.fSection = section;
            this.fOffset = offset;
        }

        public int hashCode() {
            return fModule.hashCode() + fSection.hashCode() + fOffset.intValue();
        }

        public boolean equals(Object o) {
            if (!(o instanceof ModuleSectionOffset)) return false;
            ModuleSectionOffset mso = (ModuleSectionOffset)o;
            return fModule.equals(mso.fModule) && fSection.equals(mso.fSection) && fOffset.equals(mso.fOffset);
        }
    }
    
    /**
     * Symbol context data includes a mapping between run-time addresses and 
     * module-section-offset coordinates.
     */
    public interface SymbolData extends IDataModelData {
        /** Convert link-time address 'addr' to run-time address */
        public long convertToRT(ModuleSectionOffset mso);

        /** Convert run-time address 'addr' to link-time address */
        public ModuleSectionOffset convertFromRT(IAddress addr);
    }
    
    /** Module information. */
    public interface ModuleData extends IDataModelData {
        String getName();
        String getFile();
        long getTimeStamp();
        Section[] getSections();
    }
    
    /** Section information */
    public interface Section {
        String getName();
        IAddress getStartAddress();
        BigInteger getCount();
    }

    /** Line information about a particular address */
    public interface LineInfo  {
        IAddress getAddress();
        String getSourceFile();
        int getStartLine();
        int getStartColumn();
        int getEndLine();
        int getEndColumn();
    }
    
    /** Address information about a particular file/line */
    public interface AddressRange {
        IAddress getStartAddress();
        IAddress getEndAddress();
    }
    
    /** 
     * Retreives the list of modules loaded in given symbol context. 
     */ 
    void getModules(ISymbolDMC symCtx, GetDataDone<IModuleDMC[]> done);

    /**
     * Calculates the line numbers corresponding to the given address. 
     */
    void calcLineInfo(ISymbolDMC symCtx, IAddress address, GetDataDone<LineInfo[]> done);
    
    /**
     * Calculates the addresses corresponding to the given source file location.
     */
    void calcAddressInfo(ISymbolDMC symCtx, String file, int line, int col, GetDataDone<AddressRange[]> done);

}
