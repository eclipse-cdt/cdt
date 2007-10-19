/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.service;

import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;

/**
 * Debugger service representing module handling logic of a debugger.  
 */
public interface IModules extends IDMService {
    
    /**
     * Symbol context represents the space into which module symbols are loaded.
     * Traditionally symbols are loaded in context of a process, but for other
     * types of debugging, like kernel or no-OS debugging, it's useful to 
     * separate the concept of a symbol context from a process.
     */
    public interface ISymbolDMContext extends IDMContext {}
    
    /**
     * Module context represents a single module that is loaded.
     */
    public interface IModuleDMContext extends IDMContext {}
    
    /**
     * Event indicating a change in the symbol information for given context.
     */
    public interface ModulesChangedDMEvent extends IDMEvent<ISymbolDMContext> {}

    /**
     * Specific event identifying that a new module was loaded into a 
     * symbol context.
     */
    public interface ModuleLoadedDMEvent extends ModulesChangedDMEvent {
        /** Returns context of the module that was loaded */
        IModuleDMContext getLoadedModuleContext();
    }
    
    public interface ModuleUnloadedDMEvent extends ModulesChangedDMEvent {
        /** Returns context of the module that was un-loaded */
        IModuleDMContext getUnloadedModuleContext();
    }

    /**
     * Object representing a unuqie location in a symbol context, based on
     * a module, section, and address offset within the seciton.
     */
    public final class ModuleSectionOffset {
        private final IModuleDMContext fModule;
        private final Section fSection;
        private final BigInteger fOffset;

        public IModuleDMContext getModule() { return fModule; }
        public Section getSection() { return fSection; }
        public BigInteger getOffset() { return fOffset; }
        
        public ModuleSectionOffset(IModuleDMContext module, Section section, BigInteger offset) {
            this.fModule = module;
            this.fSection = section;
            this.fOffset = offset;
        }

        @Override
        public int hashCode() {
            return fModule.hashCode() + fSection.hashCode() + fOffset.intValue();
        }

        @Override
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
    public interface ISymbolDMData extends IDMData {
        /** Convert link-time address 'addr' to run-time address */
        public long convertToRT(ModuleSectionOffset mso);

        /** Convert run-time address 'addr' to link-time address */
        public ModuleSectionOffset convertFromRT(IAddress addr);
    }
    
    /** Module information. */
    public interface IModuleDMData {
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
    void getModules(IDMContext symCtx, DataRequestMonitor<IModuleDMContext[]> rm);

    /**
     * Calculates the line numbers corresponding to the given address. 
     */
    void calcLineInfo(IDMContext symCtx, IAddress address, DataRequestMonitor<LineInfo[]> rm);
    
    /**
     * Calculates the addresses corresponding to the given source file location.
     */
    void calcAddressInfo(IDMContext symCtx, String file, int line, int col, DataRequestMonitor<AddressRange[]> rm);

}
