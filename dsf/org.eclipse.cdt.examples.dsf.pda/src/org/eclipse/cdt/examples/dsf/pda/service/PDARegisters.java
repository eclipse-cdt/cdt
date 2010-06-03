/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.service;

import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDABitField;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAGroupsCommand;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAListResult;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDARegister;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDARegistersCommand;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDARegistersCommandResult;
import org.osgi.framework.BundleContext;

/**
 * 
 */
public class PDARegisters extends AbstractDsfService 
    implements IRegisters, IEventListener, ICachingService
{

    private static class RegisterGroupDMContext extends AbstractDMContext implements IRegisterGroupDMContext {
        final private String fName;

        public RegisterGroupDMContext(String sessionId, PDAVirtualMachineDMContext dmc, String groupName) {
            super(sessionId, new IDMContext[] { dmc });
            fName = groupName;
        }

        @Override
        public boolean equals(Object other) {
            return ((super.baseEquals(other)) &&  
                    (((RegisterGroupDMContext) other).fName.equals(fName)));
        }
        
        @Override
        public int hashCode() { return super.baseHashCode() + fName.hashCode(); }
        
        @Override
        public String toString() { return baseToString() + ".group[" + fName + "]"; } //$NON-NLS-1$ //$NON-NLS-2$
    }
       
    private static class RegisterDMContext extends AbstractDMContext implements IRegisterDMContext {
        final private PDARegister fRegister;

        public RegisterDMContext(String sessionId, PDAThreadDMContext thread, RegisterGroupDMContext group, PDARegister reg) {
            super(sessionId, new IDMContext[] { thread, group });
            fRegister = reg;
        }

        @Override
        public boolean equals(Object other) {
            return ((super.baseEquals(other)) && 
                    (((RegisterDMContext) other).fRegister.fName.equals(fRegister.fName)));
        }

        @Override
        public int hashCode() { return super.baseHashCode() + fRegister.fName.hashCode(); }
        @Override
        public String toString() { return baseToString() + ".register[" + fRegister.fName + "]"; } //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /*
     * Support class used to construct BitField DMCs.
     */
    private static class BitFieldDMContext extends AbstractDMContext implements IBitFieldDMContext {
        
        final private PDABitField fBitField;

        public BitFieldDMContext(String sessionId, RegisterDMContext reg, PDABitField bitField) {
            super(sessionId, new IDMContext[] { reg });

            fBitField = bitField;
        }

        /*
         *  Required common manipulation routines.
         */
        @Override
        public boolean equals(Object other) {
            if (other instanceof BitFieldDMContext) {
                BitFieldDMContext  dmc = (BitFieldDMContext) other;
                return( (super.baseEquals(other)) &&
                        (dmc.fBitField.fName.equals(fBitField.fName)));
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() { return super.baseHashCode() + fBitField.fName.hashCode(); }
        
        @Override
        public String toString() { return baseToString() + ".bitfield[" + fBitField.fName + "]"; } //$NON-NLS-1$ //$NON-NLS-2$
    }
       
    private static class RegisterGroupDMData implements IRegisterGroupDMData {
        
        final private String fName;
        
        public RegisterGroupDMData(String name) {
            fName = name;
        }
        public String getName() { return fName; }
        public String getDescription() { return "Description of the " + fName + " register group"; }
    }
    
    private static class RegisterDMData implements IRegisterDMData {
        
        final private PDARegister fRegister;
        
        public RegisterDMData(PDARegister reg) {
            fRegister = reg;
        }
        
        public boolean isReadable() { return true; }
        public boolean isReadOnce() { return false; }
        public boolean isWriteable() { return fRegister.fWritable; }
        public boolean isWriteOnce() { return false; }
        public boolean hasSideEffects() { return false; }
        public boolean isVolatile() { return true; }

        public boolean isFloat() { return false; }
        public String getName() { return fRegister.fName; }
        public String getDescription() { return "Description of the " + fRegister.fName + " register"; }
    }
    
    private static class Mnemonic implements IMnemonic {
        Mnemonic(String name, String value, int numBits) {
            fName = name;
            fValue = new BigInteger(value);
            fNumBits = numBits;
        }
        final private String fName;
        final private BigInteger fValue;
        final private int fNumBits;
        
        public String getShortName() { return fName; }
        public String getLongName()  { return fName; }
        
        public BigInteger getValue() { return fValue; }
        public int getBitCount() { return fNumBits; }

        @Override
        public boolean equals( Object element ) {
            if ( element instanceof Mnemonic ) {
                Mnemonic mnem = (Mnemonic) element;
                return ( mnem.fName.equals( fName ) ) &&
                       ( mnem.fValue.equals( fValue ) ) &&
                       ( mnem.fNumBits == fNumBits );
            }
            return false ;
        }
    }
    
    private class BitFieldDMData implements IBitFieldDMData {

        final private PDABitField fBitField;
        final private IBitGroup[] fBitGroups;
        final private Mnemonic[] fMnemonics;
        final private BigInteger fValue;
        final private Mnemonic fMnemonicValue;
        
        public BitFieldDMData(PDABitField bitField, String value) {
            fBitField = bitField;
            fValue = new BigInteger(value);
            
            fBitGroups = new IBitGroup[] { 
                new IBitGroup() {
                    public int startBit() { return fBitField.fOffset; }
                    public int bitCount() { return fBitField.fCount; }
                }
            };

            fMnemonics = new Mnemonic[fBitField.fMnemonics.size()];
            Mnemonic mnemonicValue = null;
            int i = 0;
            for (Map.Entry<String, String> mnemonicEntry : fBitField.fMnemonics.entrySet()) {
                fMnemonics[i] = new Mnemonic(mnemonicEntry.getKey(), mnemonicEntry.getValue(), fBitField.fCount);
                if (fValue.equals(fMnemonics[i].fValue)) {
                    mnemonicValue = fMnemonics[i];
                }
                i++;
            }
            fMnemonicValue = mnemonicValue;
        }
        
        
        public IBitGroup[] getBitGroup()      { return fBitGroups;  }
        public IMnemonic[] getMnemonics()     { return fMnemonics;  }
        
        public boolean isZeroBasedNumbering() { return true; }
        public boolean isZeroBitLeftMost()    { return true; }
        public boolean isReadable()           { return true; }
        public boolean isReadOnce()           { return false; }
        public boolean isWriteable()          { return true; }
        public boolean isWriteOnce()          { return false; }
        public boolean hasSideEffects()       { return false; }
        public boolean isFloat()              { return false; }
        
        public String  getName()         { return fBitField.fName; }
        public String  getDescription()  { return "Description of the " + fBitField.fName + " bit field"; }

        public IMnemonic getCurrentMnemonicValue() { return fMnemonicValue; }
    }
    
    private static class RegisterChangedDMEvent extends AbstractDMEvent<IRegisterDMContext> implements IRegisterChangedDMEvent {
        RegisterChangedDMEvent(IRegisterDMContext registerDmc) { 
            super(registerDmc);
        }
    }
    
    private PDACommandControl fCommandControl;
    private PDAExpressions fExpressions;
    private CommandCache fNamesCache;
    
    public PDARegisters(DsfSession session) 
    {
        super(session);
    }

    @Override
    protected BundleContext getBundleContext() 
    {
        return PDAPlugin.getBundleContext();
    }
    
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
            new RequestMonitor(getExecutor(), requestMonitor) { 
                @Override
                protected void handleSuccess() {
                    doInitialize(requestMonitor);
                }});
    }
    
    private void doInitialize(RequestMonitor requestMonitor) {

        fCommandControl = getServicesTracker().getService(PDACommandControl.class);
        fExpressions = getServicesTracker().getService(PDAExpressions.class);
        
        // Create the cache to store the register definitions.  This cache 
        // only needs to be reset upon the "registers" event and is available
        // all the time.
        fNamesCache = new CommandCache(getSession(), fCommandControl);
        fNamesCache.setContextAvailable(fCommandControl.getContext(), true);
        
        // Add the register service as a listener to PDA events, to catch 
        // the "registers" events from the command control.
        fCommandControl.addEventListener(this);
        
        // Sign up so we see events. We use these events to decide how to manage
        // any local caches we are providing as well as the lower level register
        // cache we create to get/set registers on the target.
        getSession().addServiceEventListener(this, null);

        // Make ourselves known so clients can use us.
        register(new String[]{IRegisters.class.getName(), PDARegisters.class.getName()}, new Hashtable<String,String>());

        requestMonitor.done();
    }

    @Override
    public void shutdown(RequestMonitor requestMonitor) 
    {
        unregister();
        fCommandControl.removeEventListener(this);
        getSession().removeServiceEventListener(this);        
        super.shutdown(requestMonitor);
    }
    
    public void getRegisterGroups(IDMContext ctx, final DataRequestMonitor<IRegisterGroupDMContext[]> rm ) {
    	final PDAVirtualMachineDMContext dmc = DMContexts.getAncestorOfType(ctx, PDAVirtualMachineDMContext.class);
        if (dmc == null) {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Container context not found");   //$NON-NLS-1$
            return;
        }
        
        fNamesCache.execute(
            new PDAGroupsCommand(dmc), 
            new DataRequestMonitor<PDAListResult>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    IRegisterGroupDMContext[] groups = new IRegisterGroupDMContext[getData().fValues.length];
                    for (int i = 0; i < getData().fValues.length; i++) {
                        groups[i] = new RegisterGroupDMContext(getSession().getId(), dmc, getData().fValues[i]);
                    }
                    rm.setData(groups);
                    rm.done();
                };
            }); 
    }
    
    public void getRegisters(final IDMContext ctx, final DataRequestMonitor<IRegisterDMContext[]> rm) {
        final PDAThreadDMContext execDmc = DMContexts.getAncestorOfType(ctx, PDAThreadDMContext.class);
        if ( execDmc == null ) { 
            PDAPlugin.failRequest(rm, INVALID_HANDLE , "Thread context not found");   //$NON-NLS-1$
            return;
        }

        final RegisterGroupDMContext groupDmc = DMContexts.getAncestorOfType(ctx, RegisterGroupDMContext.class);
        if ( groupDmc == null ) { 
            PDAPlugin.failRequest(rm, INVALID_HANDLE , "Group context not found");   //$NON-NLS-1$
            return;
        }

        fNamesCache.execute(
            new PDARegistersCommand(execDmc, groupDmc != null ? groupDmc.fName : null), 
            new DataRequestMonitor<PDARegistersCommandResult>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    IRegisterDMContext[] groups = new IRegisterDMContext[getData().fRegisters.length];
                    for (int i = 0; i < getData().fRegisters.length; i++) {
                        groups[i] = new RegisterDMContext(getSession().getId(), execDmc, groupDmc, getData().fRegisters[i]);
                    }
                    rm.setData(groups);
                    rm.done();
                };
            }); 
        
    }
    
    public void getBitFields( IDMContext dmc , DataRequestMonitor<IBitFieldDMContext[]> rm ) {
    	
    	RegisterDMContext registerDmc = DMContexts.getAncestorOfType(dmc, RegisterDMContext.class);
        
        if ( registerDmc == null ) { 
            PDAPlugin.failRequest(rm,INVALID_HANDLE, "No register in context: " + dmc) ;   //$NON-NLS-1$
            return;
        }
        
        PDABitField[] bitFields = registerDmc.fRegister.fBitFields;
        BitFieldDMContext[] bitFieldDMCs = new BitFieldDMContext[bitFields.length];
        
        for (int i = 0; i < bitFields.length; i++) {
            bitFieldDMCs[i] = new BitFieldDMContext(getSession().getId(), registerDmc, bitFields[i]);
        }
        
        rm.setData(bitFieldDMCs) ;
        rm.done();
    }

    public void writeRegister(final IRegisterDMContext regCtx, String regValue, String formatId, final RequestMonitor rm) {
        if (regCtx instanceof RegisterDMContext) {
            IExpressionDMContext exprCtx = createRegisterExpressionDmc( (RegisterDMContext)regCtx );
            fExpressions.writeExpression(
                exprCtx, regValue, formatId, false,  
                new RequestMonitor(getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        generateRegisterChangedEvent( (RegisterDMContext)regCtx );
                        rm.done();
                    }
                });

        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid context");   //$NON-NLS-1$
    	}
        
    }

    public void writeBitField(final IBitFieldDMContext bitFieldCtx, String bitFieldValue, String formatId, final RequestMonitor rm) {
        if (bitFieldCtx instanceof BitFieldDMContext) {
            IExpressionDMContext exprCtx = createBitFieldExpressionDmc( (BitFieldDMContext)bitFieldCtx );
            fExpressions.writeExpression(
                exprCtx, bitFieldValue, formatId, false, 
                new RequestMonitor(getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        generateRegisterChangedEvent( 
                            DMContexts.getAncestorOfType(bitFieldCtx, RegisterDMContext.class) );
                        rm.done();
                    }
                });
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid context");   //$NON-NLS-1$
        }
    }
    
    public void writeBitField(IBitFieldDMContext bitFieldCtx, IMnemonic mnemonic, RequestMonitor rm) {
        if (mnemonic instanceof Mnemonic) {
            writeBitField(bitFieldCtx, ((Mnemonic)mnemonic).fValue.toString(), NATURAL_FORMAT, rm);
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid mnemonic");   //$NON-NLS-1$
        }
    }

    public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
        IExpressionDMContext exprCtx = null;
        if ( dmc instanceof RegisterDMContext ) {
            exprCtx = createRegisterExpressionDmc((RegisterDMContext)dmc);
        } else if ( dmc instanceof BitFieldDMContext ) {
            exprCtx = createBitFieldExpressionDmc((BitFieldDMContext)dmc);
        }
        if (exprCtx != null) {
            fExpressions.getAvailableFormats(exprCtx, rm);
        } else {
            throw new IllegalArgumentException("Invalid register/bit field context " + dmc);
        }
    }

    public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId) {
        IExpressionDMContext exprCtx = null;
        if ( dmc instanceof RegisterDMContext ) {
            exprCtx = createRegisterExpressionDmc((RegisterDMContext)dmc);
        } else if ( dmc instanceof BitFieldDMContext ) {
            exprCtx = createBitFieldExpressionDmc((BitFieldDMContext)dmc);
        }
        if (exprCtx != null) {
            return fExpressions.getFormattedValueContext(exprCtx, formatId);
        } else {
            throw new IllegalArgumentException("Invalid register/bit field context " + dmc);
        }
    }

    public void findRegisterGroup(IDMContext ctx, String name, DataRequestMonitor<IRegisterGroupDMContext> rm) {
        PDAPlugin.failRequest(rm, NOT_SUPPORTED, "Finding context not supported"); //$NON-NLS-1$
    }
    
    public void findRegister(IDMContext ctx, String name, DataRequestMonitor<IRegisterDMContext> rm) {
        PDAPlugin.failRequest(rm, NOT_SUPPORTED, "Finding context not supported"); //$NON-NLS-1$
    }

    public void findBitField(IDMContext ctx, String name, DataRequestMonitor<IBitFieldDMContext> rm) {
        PDAPlugin.failRequest(rm, NOT_SUPPORTED, "Finding context not supported"); //$NON-NLS-1$
    }
    
    public void getFormattedExpressionValue(FormattedValueDMContext dmc, DataRequestMonitor<FormattedValueDMData> rm) {
        fExpressions.getFormattedExpressionValue(dmc, rm);
    }
    
    public void getRegisterGroupData(IRegisterGroupDMContext regGroupDmc, DataRequestMonitor<IRegisterGroupDMData> rm) {
        if (regGroupDmc instanceof RegisterGroupDMContext) {
            rm.setData(new RegisterGroupDMData( ((RegisterGroupDMContext)regGroupDmc).fName ));
            rm.done();
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid context");  //$NON-NLS-1$
        }
    }

    public void getRegisterData(IRegisterDMContext regDmc , DataRequestMonitor<IRegisterDMData> rm) {
        if (regDmc instanceof RegisterDMContext) {
            rm.setData(new RegisterDMData( ((RegisterDMContext)regDmc).fRegister ));
            rm.done();
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid context");  //$NON-NLS-1$
        }
    }
    
    public void getBitFieldData(IBitFieldDMContext dmc, final DataRequestMonitor<IBitFieldDMData> rm) {
        if ( !(dmc instanceof BitFieldDMContext) ) {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid context");  //$NON-NLS-1$
        }
    	final BitFieldDMContext bitFieldDmc = (BitFieldDMContext) dmc;
    	
    	IExpressionDMContext bitFieldExprDmc = createBitFieldExpressionDmc(bitFieldDmc);
    	FormattedValueDMContext formattedBitFieldDmc = 
    	    fExpressions.getFormattedValueContext(bitFieldExprDmc, NATURAL_FORMAT);
    	fExpressions.getFormattedExpressionValue(
    	    formattedBitFieldDmc, 
    	    new DataRequestMonitor<FormattedValueDMData>(getExecutor(), rm) {
    	        @Override
    	        protected void handleSuccess() {
    	            rm.setData(new BitFieldDMData(bitFieldDmc.fBitField, getData().getFormattedValue()));
    	            rm.done();
    	        }
    	    });
    }
    
    private IExpressionDMContext createRegisterExpressionDmc(RegisterDMContext dmc) {
        return fExpressions.createExpression(dmc, "$" + dmc.fRegister.fName);
    }

    private IExpressionDMContext createBitFieldExpressionDmc(BitFieldDMContext dmc) {
        RegisterDMContext regDmc = DMContexts.getAncestorOfType(dmc, RegisterDMContext.class);
        return fExpressions.createExpression(dmc, "$" + regDmc.fRegister.fName + "." + dmc.fBitField.fName);
    }
       
    
    @DsfServiceEventHandler 
    public void eventDispatched(IRunControl.IResumedDMEvent e) {
    }
    
    @DsfServiceEventHandler 
    public void eventDispatched(
    IRunControl.ISuspendedDMEvent e) {
    }

    @DsfServiceEventHandler 
    public void eventDispatched(final IRegisters.IRegisterChangedDMEvent e) {
    }
    
	private void generateRegisterChangedEvent(RegisterDMContext dmc ) {
        getSession().dispatchEvent(new RegisterChangedDMEvent(dmc), getProperties());
    }

    public void eventReceived(Object output) {
        if (!(output instanceof String)) return;
        if ("registers".equals(output)) {
            fNamesCache.reset();
        }
    }
    
    public void flushCache(IDMContext context) {
        fExpressions.flushCache(context);
    }
}
