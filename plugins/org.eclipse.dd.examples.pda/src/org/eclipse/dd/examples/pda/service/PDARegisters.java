/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service;

import java.math.BigInteger;
import java.util.Hashtable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.osgi.framework.BundleContext;

/**
 * 
 */
public class PDARegisters extends AbstractDsfService implements IRegisters {
	
    /*
     *  Internal control variables.
     */
    public PDARegisters(DsfSession session) 
    {
        super(session);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.service.AbstractDsfService#getBundleContext()
     */
    @Override
    protected BundleContext getBundleContext() 
    {
        return PDAPlugin.getBundleContext();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.service.AbstractDsfService#initialize(org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
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
        /*
         * Sign up so we see events. We use these events to decide how to manage
         * any local caches we are providing as well as the lower level register
         * cache we create to get/set registers on the target.
         */
        getSession().addServiceEventListener(this, null);
        
        /*
         * Make ourselves known so clients can use us.
         */
        register(new String[]{IRegisters.class.getName(), PDARegisters.class.getName()}, new Hashtable<String,String>());

        requestMonitor.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.service.AbstractDsfService#shutdown(org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    @Override
    public void shutdown(RequestMonitor requestMonitor) 
    {
        unregister();
        getSession().removeServiceEventListener(this);
        super.shutdown(requestMonitor);
    }
    
    /*
     * These are the public interfaces for this service.
     * 
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#getRegisterGroups(org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext, org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    public void getRegisterGroups(IDMContext ctx, DataRequestMonitor<IRegisterGroupDMContext[]> rm ) {
    	PDAVirtualMachineDMContext execDmc = DMContexts.getAncestorOfType(ctx, PDAVirtualMachineDMContext.class);
        if (execDmc == null) {
            rm.setStatus( new Status( IStatus.ERROR , PDAPlugin.PLUGIN_ID , INVALID_HANDLE , "Container context not found", null ) ) ;   //$NON-NLS-1$
            rm.done();
            return;
        }
        
        MIRegisterGroupDMC generalGroupDMC  = new MIRegisterGroupDMC( this , execDmc, 0 , "General"  ) ;  //$NON-NLS-1$
        MIRegisterGroupDMC analysisGroupDMC = new MIRegisterGroupDMC( this , execDmc, 0 , "Analysis" ) ;  //$NON-NLS-1$
        
        rm.setData( new MIRegisterGroupDMC[] { generalGroupDMC, analysisGroupDMC } ) ;
        rm.done() ;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#getRegisters(org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    public void getRegisters(final IDMContext dmc, final DataRequestMonitor<IRegisterDMContext[]> rm) {
    	final MIRegisterGroupDMC groupDmc = DMContexts.getAncestorOfType(dmc, MIRegisterGroupDMC.class);
        if ( groupDmc == null ) { 
            rm.setStatus( new Status( IStatus.ERROR , PDAPlugin.PLUGIN_ID , INVALID_HANDLE , "RegisterGroup context not found", null ) ) ;   //$NON-NLS-1$
            rm.done();
            return;
        }

        String[] names = null;;

        if ( groupDmc.getName().equals( "General") ) {
        	
        	names = new String[] { "pc"           ,
                                   "sp"           ,
                                   "status"       ,
                                   "stackdepth"   ,
                                   "stack[0]"     ,
                                   "stack[1]"     ,
                                   "stack[2]"     ,
                                   "stack[3]"     ,
                                   "stack[4]"     ,
                                   "stack[5]"   } ;
        }
        else if ( groupDmc.getName().equals( "Analysis") ) {
        	
        	names = new String[] { "total-instructions"    ,
        	                       "add-instructions"      ,
        	                       "call-instructions"     ,
        	                       "dec-instructions"      ,
        	                       "dup-instructions"      ,
        	                       "halt-instructions"     ,
        	                       "output-instructions"   ,
        	                       "pop-instructions"      ,
        	                       "push-instructions"     ,
        	                       "return-instructions"   ,
        	                       "var-instructions"    } ;
        }
        else {
        	rm.setStatus(new Status(IStatus.ERROR , PDAPlugin.PLUGIN_ID , INTERNAL_ERROR , "Invalid group = " + groupDmc , null)); //$NON-NLS-1$
            rm.done();
        }
        
        IExecutionDMContext executionDmc = DMContexts.getAncestorOfType(dmc, IExecutionDMContext.class);
        
        if(executionDmc == null)
           	rm.setData(makeRegisterDMCs(groupDmc, names));
        else
           	rm.setData(makeRegisterDMCs(groupDmc, executionDmc, names));
        rm.done();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#getBitFields(org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    public void getBitFields( IDMContext regDmc , DataRequestMonitor<IBitFieldDMContext[]> rm ) {
    	
    	MIRegisterDMC registerDmc = DMContexts.getAncestorOfType(regDmc, MIRegisterDMC.class);
        
        if ( registerDmc == null ) { 
            rm.setStatus( new Status( IStatus.ERROR , PDAPlugin.PLUGIN_ID , INVALID_HANDLE , "No register in context: " + regDmc , null ) ) ;   //$NON-NLS-1$
            rm.done();
            return;
        }
        
        MIBitFieldDMC[] bitFieldDMCs = null;
        
        if ( registerDmc.getName().equals( "status") ) {
        	bitFieldDMCs = new MIBitFieldDMC[4];
        	bitFieldDMCs[ 0 ] = new MIBitFieldDMC( PDARegisters.this, registerDmc, 0, "BITS_00_07" ) ; //$NON-NLS-1$
        	bitFieldDMCs[ 1 ] = new MIBitFieldDMC( PDARegisters.this, registerDmc, 1, "BITS_08_15" ) ; //$NON-NLS-1$
        	bitFieldDMCs[ 2 ] = new MIBitFieldDMC( PDARegisters.this, registerDmc, 2, "BITS_16_23" ) ; //$NON-NLS-1$
        	bitFieldDMCs[ 3 ] = new MIBitFieldDMC( PDARegisters.this, registerDmc, 3, "BITS_24_31" ) ; //$NON-NLS-1$
        }
        else {
        	bitFieldDMCs = new MIBitFieldDMC[0];
        }
        
        rm.setData(bitFieldDMCs) ;
        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#writeRegister(org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext, java.lang.String, java.lang.String, org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    public void writeRegister(IRegisterDMContext regCtx, final String regValue, final String formatId, final RequestMonitor rm) {
    	MIRegisterGroupDMC grpDmc = DMContexts.getAncestorOfType(regCtx, MIRegisterGroupDMC.class);
    	if ( grpDmc == null ) { 
    		rm.setStatus( new Status( IStatus.ERROR , PDAPlugin.PLUGIN_ID , INVALID_HANDLE , "RegisterGroup context not found" , null ) ) ;   //$NON-NLS-1$
    		rm.done();
    		return;
    	}

    	rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, NOT_SUPPORTED, "Register is read only at this time", null)); //$NON-NLS-1$
    	rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#writeBitField(org.eclipse.dd.dsf.debug.service.IRegisters.IBitFieldDMContext, java.lang.String, java.lang.String, org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    public void writeBitField(IBitFieldDMContext bitFieldCtx, String bitFieldValue, String formatId, RequestMonitor rm) {
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, NOT_SUPPORTED, "Writing bit field not supported", null)); //$NON-NLS-1$
        rm.done();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#writeBitField(org.eclipse.dd.dsf.debug.service.IRegisters.IBitFieldDMContext, org.eclipse.dd.dsf.debug.service.IRegisters.IMnemonic, org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    public void writeBitField(IBitFieldDMContext bitFieldCtx, IMnemonic mnemonic, RequestMonitor rm) {
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, NOT_SUPPORTED, "Writing bit field not supported", null)); //$NON-NLS-1$
        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IFormattedValues#getAvailableFormats(org.eclipse.dd.dsf.debug.service.IFormattedValues.IFormattedDataDMContext, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
        
        rm.setData(new String[] { HEX_FORMAT, DECIMAL_FORMAT, OCTAL_FORMAT, BINARY_FORMAT, NATURAL_FORMAT });
        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IFormattedValues#getFormattedValueContext(org.eclipse.dd.dsf.debug.service.IFormattedValues.IFormattedDataDMContext, java.lang.String)
     */
    public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String formatId) {
        if ( dmc instanceof MIRegisterDMC ) {
            MIRegisterDMC regDmc = (MIRegisterDMC) dmc;
            return( new FormattedValueDMContext( this, regDmc, formatId));
        }
        else if ( dmc instanceof MIBitFieldDMC ) {
            MIBitFieldDMC bitFieldDmc = (MIBitFieldDMC) dmc;
            return( new FormattedValueDMContext( this, bitFieldDmc, formatId));
        }
        
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#findRegisterGroup(org.eclipse.dd.dsf.datamodel.IDMContext, java.lang.String, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    public void findRegisterGroup(IDMContext ctx, String name, DataRequestMonitor<IRegisterGroupDMContext> rm) {
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, NOT_SUPPORTED, "Finding a Register Group context not supported", null)); //$NON-NLS-1$
        rm.done();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#findRegister(org.eclipse.dd.dsf.datamodel.IDMContext, java.lang.String, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    public void findRegister(IDMContext ctx, String name, DataRequestMonitor<IRegisterDMContext> rm) {
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, NOT_SUPPORTED, "Finding a Register context not supported", null)); //$NON-NLS-1$
        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#findBitField(org.eclipse.dd.dsf.datamodel.IDMContext, java.lang.String, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    public void findBitField(IDMContext ctx, String name, DataRequestMonitor<IBitFieldDMContext> rm) {
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, NOT_SUPPORTED, "Finding a Register Group context not supported", null)); //$NON-NLS-1$
        rm.done();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.datamodel.IDMService#getModelData(org.eclipse.dd.dsf.datamodel.IDMContext, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    @SuppressWarnings("unchecked")
    public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
        /*
         * This is the method which is called when actual results need to be returned.  We
         * can be called either with a service DMC for which we return ourselves or we can
         * be called with the DMC's we have handed out. If the latter is the case then  we
         * data mine by talking to the Debug Engine.
         */
        
        if (dmc instanceof MIRegisterGroupDMC) {
            getRegisterGroupData((MIRegisterGroupDMC)dmc, (DataRequestMonitor<IRegisterGroupDMData>)rm);
        } else if (dmc instanceof MIRegisterDMC) {
            getRegisterData((MIRegisterDMC)dmc, (DataRequestMonitor<IRegisterDMData>)rm);
        } else if (dmc instanceof MIBitFieldDMC) {
            getBitFieldData((MIBitFieldDMC) dmc, (DataRequestMonitor<IBitFieldDMData>)rm);
        } else if (dmc instanceof FormattedValueDMContext) {
            getFormattedExpressionValue((FormattedValueDMContext)dmc, (DataRequestMonitor<FormattedValueDMData>)rm);
        } else {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, -1, "Unknown DMC type", null));  //$NON-NLS-1$
            rm.done();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IFormattedValues#getFormattedExpressionValue(org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMContext, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    public void getFormattedExpressionValue(FormattedValueDMContext dmc, DataRequestMonitor<FormattedValueDMData> rm) {
        if (dmc.getParents().length == 1 && dmc.getParents()[0] instanceof MIRegisterDMC) {
            getRegisterDataValue( (MIRegisterDMC) dmc.getParents()[0], dmc.getFormatID(), rm);
        }
        else if (dmc.getParents().length == 1 && dmc.getParents()[0] instanceof MIBitFieldDMC) {
            getBitFieldDataValue( (MIBitFieldDMC) dmc.getParents()[0], dmc.getFormatID(), rm);
        } else {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Unknown DMC type", null));  //$NON-NLS-1$
            rm.done();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#getRegisterGroupData(org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    public void getRegisterGroupData(IRegisterGroupDMContext regGroupDmc, DataRequestMonitor<IRegisterGroupDMData> rm) {
        
    	MIRegisterGroupDMC groupDmc = (MIRegisterGroupDMC) regGroupDmc;
    	
    	/*
    	 * Register group layout :
    	 * 
    	 *     + General
    	 *     + Analysis
    	 */
    	
    	if ( groupDmc.getName().equals( "General" ) ) {
    		 rm.setData( new RegisterGroupData( "General", "General Purpose Control Registers") ) ; //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	else if ( groupDmc.getName().equals( "Analysis" ) ) {
    		 rm.setData( new RegisterGroupData( "Analysis", "Code Analysis Registers") ) ; //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	else {
    		rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INVALID_HANDLE, "Unknown register group", null));  //$NON-NLS-1$
    	}
    	
        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#getRegisterData(org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    public void getRegisterData(IRegisterDMContext regDmc , DataRequestMonitor<IRegisterDMData> rm) {
        if (regDmc instanceof MIRegisterDMC) {
            MIRegisterDMC pdaRegDmc = (MIRegisterDMC)regDmc;
            String regName = pdaRegDmc.getName();
            String regDesc = BLANK_STRING;
            boolean foundReg = false;
            boolean isWriteable = false ;
            
                 if ( regName.equalsIgnoreCase( "pc"                  ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "sp"                  ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "status"              ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stackdepth"          ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[0]"            ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[1]"            ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[2]"            ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[3]"            ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[4]"            ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[5]"            ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "total-instructions"  ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "add-instructions"    ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "call-instructions"   ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dec-instructions"    ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dup-instructions"    ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "halt-instructions"   ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "output-instructions" ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "pop-instructions"    ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "push-instructions"   ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "return-instructions" ) )  { foundReg = true ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "var-instructions"    ) )  { foundReg = true ; } //$NON-NLS-1$
            
                 if ( regName.equalsIgnoreCase( "pc"                  ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "sp"                  ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "status"              ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stackdepth"          ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[0]"            ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[1]"            ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[2]"            ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[3]"            ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[4]"            ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[5]"            ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "total-instructions"  ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "add-instructions"    ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "call-instructions"   ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dec-instructions"    ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dup-instructions"    ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "halt-instructions"   ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "output-instructions" ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "pop-instructions"    ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "push-instructions"   ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "return-instructions" ) )  { isWriteable = false ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "var-instructions"    ) )  { isWriteable = false ; } //$NON-NLS-1$
                 
                 if ( regName.equalsIgnoreCase( "pc"                  ) )  { regDesc = "Program Counter" ;                                   } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "sp"                  ) )  { regDesc = "Stack Pointer" ;                                     } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "status"              ) )  { regDesc = "CPU Status" ;                                        } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "stackdepth"          ) )  { regDesc = "Current depth of the pushdown stack" ;               } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "stack[0]"            ) )  { regDesc = "Stack entry" ;                                       } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "stack[1]"            ) )  { regDesc = "Stack entry" ;                                       } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "stack[2]"            ) )  { regDesc = "Stack entry" ;                                       } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "stack[3]"            ) )  { regDesc = "Stack entry" ;                                       } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "stack[4]"            ) )  { regDesc = "Stack entry" ;                                       } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "stack[5]"            ) )  { regDesc = "Stack entry" ;                                       } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "total-instructions"  ) )  { regDesc = "Total # of instructions in the current program" ; } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "add-instructions"    ) )  { regDesc = "# of 'addr' instructions in the current program" ; } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "call-instructions"   ) )  { regDesc = "# of 'call' instructions in the current program" ; } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "dec-instructions"    ) )  { regDesc = "# of 'dec' instructions in the current program" ; } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "dup-instructions"    ) )  { regDesc = "# of 'dup' instructions in the current program" ; } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "halt-instructions"   ) )  { regDesc = "# of 'halt' instructions in the current program" ; } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "output-instructions" ) )  { regDesc = "# of 'output' instructions in the current program" ; } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "pop-instructions"    ) )  { regDesc = "# of 'pop' instructions in the current program" ; } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "push-instructions"   ) )  { regDesc = "# of 'push' instructions in the current program" ; } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "return-instructions" ) )  { regDesc = "# of 'return' instructions in the current program" ; } //$NON-NLS-1$ //$NON-NLS-2$
            else if ( regName.equalsIgnoreCase( "var-instructions"    ) )  { regDesc = "# of 'var' instructions in the current program" ; } //$NON-NLS-1$ //$NON-NLS-2$
                 
            if ( foundReg ) {
            	rm.setData(new RegisterData(regName, regDesc, false, isWriteable));
            	rm.done();
            }
            else {
            	 rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown register", null));  //$NON-NLS-1$
                 rm.done();
            }
        } else {
            rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown DMC type", null));  //$NON-NLS-1$
            rm.done();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.service.IRegisters#getBitFieldData(org.eclipse.dd.dsf.debug.service.IRegisters.IBitFieldDMContext, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    public void getBitFieldData(IBitFieldDMContext dmc, DataRequestMonitor<IBitFieldDMData> rm) {
    	
    	MIBitFieldDMC bitFieldDmc = (MIBitFieldDMC) dmc;
    	IMnemonic[] EmptyMnemonic = new IMnemonic[0];
    	BitFieldData  bData = null;
    	
    	if ( bitFieldDmc.getName().equals( "BITS_00_07" ) ) {
    		
    		bData = new BitFieldData( bitFieldDmc.getName(), "Status Bits 00 - 07", new BigInteger( "0" ) , true, true, true, false, false, false, false, false);
            
    		IBitGroup[] bitGroups = new IBitGroup[1];
    		
    		BitGroupData bGroup = new BitGroupData();
    		
    		bGroup.setBitCount(8);
    		bGroup.setStartBit(0);
    		
    		bitGroups[0] = bGroup;
    		
            bData.addBitGroups(bitGroups);
            bData.addMnemonics(EmptyMnemonic);
    	}
    	else if ( bitFieldDmc.getName().equals( "BITS_08_15" ) ) {
    		
    		bData = new BitFieldData( bitFieldDmc.getName(), "Status Bits 08 - 15", new BigInteger( "0" ) , true, true, true, false, false, false, false, false);
            
    		IBitGroup[] bitGroups = new IBitGroup[1];
    		
    		BitGroupData bGroup = new BitGroupData();
    		
    		bGroup.setBitCount(8);
    		bGroup.setStartBit(8);
    		
    		bitGroups[0] = bGroup;
    		
            bData.addBitGroups(bitGroups);
            bData.addMnemonics(EmptyMnemonic);
    	}
    	else if ( bitFieldDmc.getName().equals( "BITS_16_23" ) ) {

    		bData = new BitFieldData( bitFieldDmc.getName(), "Status Bits 16 - 23", new BigInteger( "0" ) , true, true, true, false, false, false, false, false);

    		IBitGroup[] bitGroups = new IBitGroup[1];

    		BitGroupData bGroup = new BitGroupData();

    		bGroup.setBitCount(8);
    		bGroup.setStartBit(16);

    		bitGroups[0] = bGroup;

    		bData.addBitGroups(bitGroups);
    		bData.addMnemonics(EmptyMnemonic);
    	}
    	else if ( bitFieldDmc.getName().equals( "BITS_24_31" ) ) {

    		bData = new BitFieldData( bitFieldDmc.getName(), "Status Bits 24 - 31", new BigInteger( "255" ) , true, true, true, false, false, false, false, false);

    		IBitGroup[] bitGroups = new IBitGroup[1];

    		BitGroupData bGroup = new BitGroupData();

    		bGroup.setBitCount(8);
    		bGroup.setStartBit(24);

    		bitGroups[0] = bGroup;

    		bData.addBitGroups(bitGroups);
    		bData.addMnemonics(EmptyMnemonic);
    	}
    	
    	rm.setData(bData);
        rm.done();
    }
    
    private void getRegisterDataValue( final MIRegisterDMC regDmc, final String formatId, final DataRequestMonitor<FormattedValueDMData> rm) {
        IExecutionDMContext miExecDmc = DMContexts.getAncestorOfType(regDmc, IExecutionDMContext.class);
        if(miExecDmc == null){
            // Set value to blank if execution dmc is not present
            rm.setData( new FormattedValueDMData( BLANK_STRING ) );
            rm.done();
            return;
        }

        String regName = regDmc.getName() ;
        String value = BLANK_STRING;
        
        if ( HEX_FORMAT.equals( formatId ) || NATURAL_FORMAT.equals( formatId ) ) {
        	
                 if ( regName.equalsIgnoreCase( "pc"                  ) )  { value = "0x00000043" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "sp"                  ) )  { value = "0x000000C6" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "status"              ) )  { value = "0x000000FF" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stackdepth"          ) )  { value = "0x00000005" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[0]"            ) )  { value = "0x0000000A" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[1]"            ) )  { value = "0x00000025" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[2]"            ) )  { value = "0x00000066" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[3]"            ) )  { value = "0x0000008D" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[4]"            ) )  { value = "0x000000A9" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[5]"            ) )  { value = "0x000000C6" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "total-instructions"  ) )  { value = "0x00000226" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "add-instructions"    ) )  { value = "0x0000000A" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "call-instructions"   ) )  { value = "0x00000014" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dec-instructions"    ) )  { value = "0x0000001E" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dup-instructions"    ) )  { value = "0x00000028" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "halt-instructions"   ) )  { value = "0x00000032" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "output-instructions" ) )  { value = "0x0000003C" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "pop-instructions"    ) )  { value = "0x00000046" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "push-instructions"   ) )  { value = "0x00000050" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "return-instructions" ) )  { value = "0x0000005A" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "var-instructions"    ) )  { value = "0x00000064" ; } //$NON-NLS-1$
                 
            rm.setData( new FormattedValueDMData( value ) );
            rm.done();
            return;
        }
        
        if ( OCTAL_FORMAT.equals( formatId ) ) {
        	
                 if ( regName.equalsIgnoreCase( "pc"                  ) )  { value = "00000103" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "sp"                  ) )  { value = "00000306" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "status"              ) )  { value = "00000400" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stackdepth"          ) )  { value = "00000005" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[0]"            ) )  { value = "00000012" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[1]"            ) )  { value = "00000045" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[2]"            ) )  { value = "00000146" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[3]"            ) )  { value = "00000215" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[4]"            ) )  { value = "00000251" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[5]"            ) )  { value = "00000306" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "total-instructions"  ) )  { value = "00001046" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "add-instructions"    ) )  { value = "00000012" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "call-instructions"   ) )  { value = "00000024" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dec-instructions"    ) )  { value = "00000036" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dup-instructions"    ) )  { value = "00000050" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "halt-instructions"   ) )  { value = "00000062" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "output-instructions" ) )  { value = "00000074" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "pop-instructions"    ) )  { value = "00000106" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "push-instructions"   ) )  { value = "00000120" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "return-instructions" ) )  { value = "00000132" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "var-instructions"    ) )  { value = "00000144" ; } //$NON-NLS-1$
                 
            rm.setData( new FormattedValueDMData( value ) );
            rm.done();
            return;
        }
        
        if ( BINARY_FORMAT.equals( formatId ) ) {
        	
                 if ( regName.equalsIgnoreCase( "pc"                  ) )  { value = "0b00000000000000000000000001000011" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "sp"                  ) )  { value = "0b00000000000000000000000011000110" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "status"              ) )  { value = "0b00000000000000000000001001010101" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stackdepth"          ) )  { value = "0b00000000000000000000000000000101" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[0]"            ) )  { value = "0b00000000000000000000000000001010" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[1]"            ) )  { value = "0b00000000000000000000000000100101" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[2]"            ) )  { value = "0b00000000000000000000000001100110" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[3]"            ) )  { value = "0b00000000000000000000000010001101" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[4]"            ) )  { value = "0b00000000000000000000000010101001" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[5]"            ) )  { value = "0b00000000000000000000000011000110" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "total-instructions"  ) )  { value = "0b00000000000000000000001000100110" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "add-instructions"    ) )  { value = "0b00000000000000000000000000001010" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "call-instructions"   ) )  { value = "0b00000000000000000000000000010100" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dec-instructions"    ) )  { value = "0b00000000000000000000000000011110" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dup-instructions"    ) )  { value = "0b00000000000000000000000000101000" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "halt-instructions"   ) )  { value = "0b00000000000000000000000000110010" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "output-instructions" ) )  { value = "0b00000000000000000000000000111100" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "pop-instructions"    ) )  { value = "0b00000000000000000000000001000110" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "push-instructions"   ) )  { value = "0b00000000000000000000000001010000" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "return-instructions" ) )  { value = "0b00000000000000000000000001011010" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "var-instructions"    ) )  { value = "0b00000000000000000000000001100100" ; } //$NON-NLS-1$
                 
            rm.setData( new FormattedValueDMData( value ) );
            rm.done();
            return;
        }

        if ( DECIMAL_FORMAT.equals( formatId ) ) {
        	
                 if ( regName.equalsIgnoreCase( "pc"                  ) )  { value =  "67" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "sp"                  ) )  { value = "198" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "status"              ) )  { value = "256" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stackdepth"          ) )  { value =   "5" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[0]"            ) )  { value =  "10" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[1]"            ) )  { value =  "37" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[2]"            ) )  { value = "102" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[3]"            ) )  { value = "141" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[4]"            ) )  { value = "169" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "stack[5]"            ) )  { value = "198" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "total-instructions"  ) )  { value = "550" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "add-instructions"    ) )  { value =  "10" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "call-instructions"   ) )  { value =  "20" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dec-instructions"    ) )  { value =  "30" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "dup-instructions"    ) )  { value =  "40" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "halt-instructions"   ) )  { value =  "50" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "output-instructions" ) )  { value =  "60" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "pop-instructions"    ) )  { value =  "70" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "push-instructions"   ) )  { value =  "80" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "return-instructions" ) )  { value =  "90" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "var-instructions"    ) )  { value = "100" ; } //$NON-NLS-1$
                 
            rm.setData( new FormattedValueDMData( value ) );
            rm.done();
            return;
        }
        
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown number format", null));  //$NON-NLS-1$
        rm.done();
    }
    
    private void getBitFieldDataValue( final MIBitFieldDMC regDmc, final String formatId, final DataRequestMonitor<FormattedValueDMData> rm) {
        IExecutionDMContext miExecDmc = DMContexts.getAncestorOfType(regDmc, IExecutionDMContext.class);
        if(miExecDmc == null){
            // Set value to blank if execution dmc is not present
            rm.setData( new FormattedValueDMData( BLANK_STRING ) );
            rm.done();
            return;
        }

        String regName = regDmc.getName() ;
        String value = BLANK_STRING;
        
        if ( HEX_FORMAT.equals( formatId ) || NATURAL_FORMAT.equals( formatId ) ) {
        	
        	     if ( regName.equalsIgnoreCase( "BITS_00_07" ) )  { value = "0x00" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_08_15" ) )  { value = "0x00" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_16_23" ) )  { value = "0x00" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_24_31" ) )  { value = "0xFF" ; } //$NON-NLS-1$
                 
            rm.setData( new FormattedValueDMData( value ) );
            rm.done();
            return;
        }
        
        if ( OCTAL_FORMAT.equals( formatId ) ) {
        	
                 if ( regName.equalsIgnoreCase( "BITS_00_07" ) )  { value = "0000" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_08_15" ) )  { value = "0000" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_16_23" ) )  { value = "0000" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_24_31" ) )  { value = "0377" ; } //$NON-NLS-1$
                 
            rm.setData( new FormattedValueDMData( value ) );
            rm.done();
            return;
        }
        
        if ( BINARY_FORMAT.equals( formatId ) ) {
        	
                 if ( regName.equalsIgnoreCase( "BITS_00_07" ) )  { value = "0b00000000" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_08_15" ) )  { value = "0b00000000" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_16_23" ) )  { value = "0b00000000" ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_24_31" ) )  { value = "0b11111111" ; } //$NON-NLS-1$
            
            rm.setData( new FormattedValueDMData( value ) );
            rm.done();
            return;
        }

        if ( DECIMAL_FORMAT.equals( formatId ) ) {
        	
        	     if ( regName.equalsIgnoreCase( "BITS_00_07" ) )  { value = "0"   ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_08_15" ) )  { value = "0"   ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_16_23" ) )  { value = "0"   ; } //$NON-NLS-1$
            else if ( regName.equalsIgnoreCase( "BITS_24_31" ) )  { value = "255" ; } //$NON-NLS-1$
                 
            rm.setData( new FormattedValueDMData( value ) );
            rm.done();
            return;
        }
        
        rm.setStatus(new Status(IStatus.ERROR, PDAPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown number format", null));  //$NON-NLS-1$
        rm.done();
    }
       
    private static final String BLANK_STRING = ""; //$NON-NLS-1$
	
    /*
     * Support class used to construct Register Group DMCs.
     */
	
    private static class MIRegisterGroupDMC extends AbstractDMContext implements IRegisterGroupDMContext {
    	
        private int fGroupNo;
        private String fGroupName;

        public MIRegisterGroupDMC(PDARegisters service, PDAVirtualMachineDMContext execDmc, int groupNo, String groupName) {
            super(service.getSession().getId(), new IDMContext[] { execDmc });
            fGroupNo = groupNo;
            fGroupName = groupName;
        }

        public int getGroupNo() { return fGroupNo; }
        public String getName() { return fGroupName; }

        @Override
        public boolean equals(Object other) {
            return ((super.baseEquals(other)) && (((MIRegisterGroupDMC) other).fGroupNo == fGroupNo) && 
                    (((MIRegisterGroupDMC) other).fGroupName.equals(fGroupName)));
        }
        
        @Override
        public int hashCode() { return super.baseHashCode() ^ fGroupNo; }
        @Override
        public String toString() { return baseToString() + ".group[" + fGroupNo + "]"; }             //$NON-NLS-1$ //$NON-NLS-2$
    }
       
    /*
     * Support class used to construct Register DMCs.
     */
    
    private static class MIRegisterDMC extends AbstractDMContext implements IRegisterDMContext {
    	
    	private int fRegNo;
    	private String fRegName;

        public MIRegisterDMC(PDARegisters service, MIRegisterGroupDMC group, int regNo, String regName) {
            super(service.getSession().getId(), 
                    new IDMContext[] { group });
              fRegNo = regNo;
              fRegName = regName;
        }

        public MIRegisterDMC(PDARegisters service, MIRegisterGroupDMC group, IExecutionDMContext execDmc, int regNo, String regName) {
            super(service.getSession().getId(), new IDMContext[] { execDmc, group });
            fRegNo = regNo;
            fRegName = regName;
        }
        
        public int getRegNo() { return fRegNo; }
        public String getName() { return fRegName; }

        @Override
        public boolean equals(Object other) {
            return ((super.baseEquals(other)) && (((MIRegisterDMC) other).fRegNo == fRegNo) && 
                    (((MIRegisterDMC) other).fRegName.equals(fRegName)));
        }

        @Override
        public int hashCode() { return super.baseHashCode() ^ fRegNo; }
        @Override
        public String toString() { return baseToString() + ".register[" + fRegNo + "]"; } //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /*
     * Support class used to construct BitField DMCs.
     */
    private static class MIBitFieldDMC extends AbstractDMContext implements IBitFieldDMContext {
        
        private int fBitFieldNo;
        private String fBitFieldName;

        public MIBitFieldDMC(PDARegisters service, MIRegisterDMC reg, int bitFieldNo, String bitFieldName) {
            super(service.getSession().getId(), new IDMContext[] { reg });

            fBitFieldNo = bitFieldNo;
            fBitFieldName = bitFieldName;
        }

        /*
         *  Getters.
         */
        public int getBitFieldNo() { return fBitFieldNo; }
        public String getName() { return fBitFieldName; }

        /*
         *  Required common manipulation routines.
         */
        @Override
        public boolean equals(Object other) {
            if (other instanceof MIBitFieldDMC) {

                MIBitFieldDMC  dmc = (MIBitFieldDMC) other;

                return( (super.baseEquals(other)) &&
                        (dmc.fBitFieldNo == fBitFieldNo) && 
                        (dmc.fBitFieldName.equals(fBitFieldName)));
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() { return super.baseHashCode() + fBitFieldName.hashCode(); }
        @Override
        public String toString() { return baseToString() + ".BitField[" + fBitFieldNo + "]"; } //$NON-NLS-1$ //$NON-NLS-2$
    }
       
    static class RegisterGroupData implements IRegisterGroupDMData {
    	
    	private String fGrpName;
    	private String fGrpDesc;
    	
    	public RegisterGroupData( String grpName, String desc ) {
    		fGrpName = grpName;
    		fGrpDesc = desc;
    	}
        public String getName() { return fGrpName; }
        public String getDescription() { return fGrpDesc; }
    }
    
    static class RegisterData implements IRegisterDMData {
    	
        private String fRegName;
        private String fRegDesc;
        private boolean fIsFloat;
        private boolean fIsWriteable;
    	
    	public RegisterData(String regName, String regDesc, boolean isFloat, boolean isWriteable ) {
    		
            fRegName = regName;
            fRegDesc = regDesc;
            fIsFloat = isFloat;
            fIsWriteable = isWriteable;
    	}
    	
    	public boolean isReadable() { return true; }
        public boolean isReadOnce() { return false; }
        public boolean isWriteable() { return fIsWriteable; }
        public boolean isWriteOnce() { return false; }
        public boolean hasSideEffects() { return false; }
        public boolean isVolatile() { return true; }

        public boolean isFloat() { return fIsFloat; }
        public String getName() { return fRegName; }
        public String getDescription() { return fRegDesc; }
    }

    /*
     * Used to create the a list of register data model contexts given a string list.
     */
    private MIRegisterDMC[] makeRegisterDMCs(MIRegisterGroupDMC groupDmc, String[] regNames) {
    	return makeRegisterDMCs(groupDmc, null, regNames);
    }
    
    /*
     * Used to create the a list of register data model contexts given a string list.
     */
    private MIRegisterDMC[] makeRegisterDMCs(MIRegisterGroupDMC groupDmc, IExecutionDMContext execDmc, String[] regNames) {
        MIRegisterDMC[] regDmcList = new MIRegisterDMC[regNames.length];
        int regNo = 0 ;
        for (String regName : regNames) {
        	if(execDmc != null)
        		regDmcList[regNo] = new MIRegisterDMC(this, groupDmc, execDmc, regNo, regName);
        	else
        		regDmcList[regNo] = new MIRegisterDMC(this, groupDmc, regNo, regName);
            regNo++;
        }
        
        return regDmcList;
    }
    
    private class BitGroupData implements IBitGroup {
        
        private int fStartBit = 0;
        private int fBitCount = 0;
        
        public void setStartBit( int startbit ) {
            fStartBit = startbit;
        }
        
        public void setBitCount( int bitcount ) {
            fBitCount = bitcount;
        }
        
        public int startBit() { return fStartBit; }
        public int bitCount() { return fBitCount; }
    }
    
    private class MnemonicData implements IMnemonic {
        
        private String fShortName;
        private String fLongName;
        private BigInteger fValue;
        private int fNumBits;
        
        public void setShortName( String name )  { fShortName = name; }
        public void setLongName( String name )   { fLongName = name; }
        public void setValue( BigInteger value ) { fValue = value; }
        public void setNumBits( int numBits )    { fNumBits = numBits; }
        
        public String getShortName() { return fShortName; }
        public String getLongName()  { return fLongName; }
        
        public BigInteger getValue() { return fValue; }
        public int getBitCount() { return fNumBits; }

        @Override
        public boolean equals( Object element ) {
            if ( element instanceof MnemonicData ) {
                MnemonicData mnem = (MnemonicData) element;
                return ( mnem.fShortName.equals( fShortName ) ) &&
                       ( mnem.fLongName.equals( fLongName ) ) &&
                       ( mnem.fValue.equals( fValue ) ) &&
                       ( mnem.fNumBits == fNumBits );
            }
            return false ;
        }
    }
    
    private class BitFieldData implements IBitFieldDMData {

        private String fRegName;
        private String fDesc;
        private IBitGroup[] fBitGroups;
        private IMnemonic[] fMnemonics;
        private boolean fIsZeroBasedNumbering;
        private boolean fIsZeroBitLeftMost;  
        private boolean fIsReadable;     
        private boolean fIsReadOnce;           
        private boolean fIsWriteable;          
        private boolean fIsWriteOnce;         
        private boolean fHasSideEffects;       
        private boolean fIsFloat; 
        private BigInteger fValue;
        
        public BitFieldData(String regName,  String desc, BigInteger value, 
        		            boolean isZeroBasedNumbering, boolean isZeroBitLeftMost, boolean isReadable,
        					boolean isReadOnce, boolean isWriteable, boolean isWriteOnce,         
        					boolean hasSideEffects, boolean isFloat  ) {

            fRegName = regName;
            fDesc = desc;
            fValue = value;
            fIsZeroBasedNumbering = isZeroBasedNumbering;
            fIsZeroBitLeftMost = isZeroBitLeftMost;  
            fIsReadable = isReadable;     
            fIsReadOnce = isReadOnce;           
            fIsWriteable = isWriteable;          
            fIsWriteOnce = isWriteOnce;         
            fHasSideEffects = hasSideEffects;       
            fIsFloat = isFloat;  
        }
        
        public void addBitGroups( IBitGroup[] groups ) { fBitGroups = groups; }
        public void addMnemonics( IMnemonic[] mnemonics ) { fMnemonics = mnemonics; }
        
        public IBitGroup[] getBitGroup()      { return fBitGroups;  }
        public IMnemonic[] getMnemonics()     { return fMnemonics;  }
        
        public boolean isZeroBasedNumbering() { return fIsZeroBasedNumbering; }
        public boolean isZeroBitLeftMost()    { return fIsZeroBitLeftMost; }
        public boolean isReadable()           { return fIsReadable; }
        public boolean isReadOnce()           { return fIsReadOnce; }
        public boolean isWriteable()          { return fIsWriteable; }
        public boolean isWriteOnce()          { return fIsWriteOnce; }
        public boolean hasSideEffects()       { return fHasSideEffects; }
        public boolean isFloat()              { return fIsFloat; }
        
        public String  getName()         { return fRegName; }
        public String  getDescription()  { return fDesc; }

        public IMnemonic getCurrentMnemonicValue() {
            
            for ( IMnemonic mnem : fMnemonics ) {
                if ( ((MnemonicData) mnem).fValue.equals( fValue ) ){
                    return mnem;
                }
            }
            return null;
        }
    }
    
    /*
     *   Event handling section. These event handlers control the caching state of the
     *   register caches. This service creates several cache objects. Not all of which
     *   need to be flushed. These handlers maintain the state of the caches.
     */

    public static class RegisterChangedDMEvent implements IRegisters.IRegisterChangedDMEvent {

    	private final IRegisterDMContext fRegisterDmc;
    	
    	RegisterChangedDMEvent(IRegisterDMContext registerDMC) { 
    		fRegisterDmc = registerDMC;
        }
        
		public IRegisterDMContext getDMContext() {
			return fRegisterDmc;
		}
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
    
    @SuppressWarnings("unused")
	private void generateRegisterChangedEvent(IRegisterDMContext dmc ) {
        getSession().dispatchEvent(new RegisterChangedDMEvent(dmc), getProperties());
    }
}
