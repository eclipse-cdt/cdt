/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.MIExpressions.ExpressionDMData;
import org.eclipse.cdt.dsf.mi.service.MIExpressions.MIExpressionDMC;
import org.eclipse.cdt.dsf.mi.service.MIRegisters;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class extends {@link IRegisters} service to utilize the "variable objects" 
 * mechanism of GDB. It extends the data model context and data types for registers to 
 * features of {@link IExpressionDMContext} and {@link IExpressionDMData}.
 *     
 * @since 4.3
 */
public class GDBRegisters extends MIRegisters {

    public static class GDBRegisterDMC extends MIExpressionDMC implements IRegisterDMContext {

        private int fRegNo = -1;
        private String fRegName;
        
        public GDBRegisterDMC( 
                String sessionId, 
                IDMContext dmc, 
                int regNo, 
                String regName ) {
            super( sessionId, createRegisterExpression( regName ), createRegisterExpression( regName ), dmc );
            fRegNo = regNo;
            fRegName = regName;
        }
        
        public GDBRegisterDMC( 
                String sessionId, 
                MIRegisterGroupDMC groupDMC, 
                int regNo, 
                String regName ) {
            super( sessionId, createRegisterExpression( regName ), createRegisterExpression( regName ), groupDMC );
            fRegNo = regNo;
            fRegName = regName;
        }
        
        public GDBRegisterDMC( 
                String sessionId, 
                IContainerDMContext containerDMC, 
                int regNo, 
                String regName ) {
            super( sessionId, createRegisterExpression( regName ), createRegisterExpression( regName ), containerDMC );
            fRegNo = regNo;
            fRegName = regName;
        }
        
        public int getRegNo() {
            return fRegNo;
        }

        public String getName() {
            return fRegName;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.mi.service.MIExpressions.MIExpressionDMC#equals(java.lang.Object)
         */
        @Override
        public boolean equals( Object other ) {
            return ( super.baseEquals( other ) 
                     && ((GDBRegisterDMC)other).fRegNo == fRegNo) 
                     && (((GDBRegisterDMC)other).fRegName.equals( fRegName ) );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.mi.service.MIExpressions.MIExpressionDMC#hashCode()
         */
        @Override
        public int hashCode() {
            return super.baseHashCode() ^ fRegNo;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.mi.service.MIExpressions.MIExpressionDMC#toString()
         */
        @Override
        public String toString() {
            return baseToString() + ".register[" + fRegNo + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public class GDBRegisterData extends ExpressionDMData implements IRegisterDMData {
        
        private String fName;
        private String fDescription;

        public GDBRegisterData( String name, String description, String type, int num, boolean edit, BasicType basicType ) {
            super( createRegisterExpression( name ), type, num, edit, basicType );
            fName = name;
            fDescription = description;
        }

        public GDBRegisterData( String name, String description, String type, int num, boolean edit ) {
            super( createRegisterExpression( name ), type, num, edit );
            fName = name;
            fDescription = description;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.mi.service.MIExpressions.ExpressionDMData#getName()
         */
        @Override
        public String getName() {
            return fName;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.gdb.service.IGDBRegisters.IGDBRegisterDMData#getDescription()
         */
        @Override
		public String getDescription() {
            return fDescription;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.gdb.service.IGDBRegisters.IGDBRegisterDMData#getExpression()
         */
        public String getExpression() {
            return super.getName();
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isReadable()
         */
        @Override
		public boolean isReadable() {
            return true;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isReadOnce()
         */
        @Override
		public boolean isReadOnce() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isWriteable()
         */
        @Override
		public boolean isWriteable() {
            return isEditable();
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isWriteOnce()
         */
        @Override
		public boolean isWriteOnce() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#hasSideEffects()
         */
        @Override
		public boolean hasSideEffects() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isVolatile()
         */
        @Override
		public boolean isVolatile() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData#isFloat()
         */
        @Override
		public boolean isFloat() {
            return false;
        }
    }

    public GDBRegisters( DsfSession session ) {
        super( session );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.MIRegisters#getRegisters(org.eclipse.cdt.dsf.datamodel.IDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
    @Override
    public void getRegisters( IDMContext dmc, final DataRequestMonitor<IRegisterDMContext[]> rm ) {
        final IExpressions exprService = getServicesTracker().getService( IExpressions.class );
        if ( exprService == null ) {
            rm.setStatus( new Status( IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Expressions service is not available", null ) ); //$NON-NLS-1$
            rm.done();
            return;
        }
        super.getRegisters( 
                dmc, 
                new DataRequestMonitor<IRegisterDMContext[]>( getSession().getExecutor(), rm ) {
        
                    /* (non-Javadoc)
                     * @see org.eclipse.cdt.dsf.concurrent.RequestMonitor#handleSuccess()
                     */
                    @Override
                    protected void handleSuccess() {
                        IRegisterDMContext[] regContexts = getData();
                        GDBRegisterDMC[] result = new GDBRegisterDMC[regContexts.length];
                        for ( int i = 0; i < regContexts.length; ++i ) {
                            result[i] = createGDBRegisterDMC( getSession().getId(), (MIRegisterDMC)regContexts[i], exprService );
                        }
                        rm.setData( result );
                        rm.done();
                    }            
                } );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.MIRegisters#getRegisterData(org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
    @Override
    public void getRegisterData( IRegisterDMContext regDMC, final DataRequestMonitor<IRegisterDMData> rm ) {
        final IExpressions exprService = getServicesTracker().getService( IExpressions.class );
        if ( !(regDMC instanceof GDBRegisterDMC) || exprService == null ) {
            rm.setStatus( new Status( IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Expressions service is not available", null ) ); //$NON-NLS-1$
            rm.done();
            return;
        }
        final GDBRegisterDMC gdbRegDMC = (GDBRegisterDMC)regDMC;
        exprService.getExpressionData( 
                gdbRegDMC, 
                new DataRequestMonitor<IExpressions.IExpressionDMData>( getExecutor(), rm ) {
                    
                    @Override
                    protected void handleSuccess() {
                        ExpressionDMData exprData = (ExpressionDMData)getData();
                        @SuppressWarnings( "deprecation" )
                        GDBRegisterData data = new GDBRegisterData( 
                                gdbRegDMC.getName(),
                                // no description is available
                                "", //$NON-NLS-1$
                                exprData.getTypeName(),
                                exprData.getNumChildren(),
                                exprData.isEditable(),
                                exprData.getBasicType() );
                        rm.setData( data );
                        rm.done();
                    }
                } );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.MIRegisters#getFormattedExpressionValue(org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
    @Override
    public void getFormattedExpressionValue( FormattedValueDMContext dmc, DataRequestMonitor<FormattedValueDMData> rm ) {
        IExpressions exprService = getServicesTracker().getService( IExpressions.class ); 
        if ( dmc.getParents().length == 1 && dmc.getParents()[0] instanceof GDBRegisterDMC ) {
            exprService.getFormattedExpressionValue( dmc, rm );
        }
        else {
            rm.setStatus( new Status( IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Unknown DMC type", null ) ); //$NON-NLS-1$
            rm.done();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.MIRegisters#getFormattedValueContext(org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext, java.lang.String)
     */
    @Override
    public FormattedValueDMContext getFormattedValueContext( IFormattedDataDMContext dmc, String formatId ) {
        if ( dmc instanceof GDBRegisterDMC ) {
            return new FormattedValueDMContext( this, dmc, formatId );
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.mi.service.MIRegisters#getAvailableFormats(org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
    @Override
    public void getAvailableFormats( IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm ) {
        final IExpressions exprService = getServicesTracker().getService( IExpressions.class );
        if ( exprService == null ) {
            rm.setStatus( new Status( IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Expressions service is not available", null ) ); //$NON-NLS-1$
            rm.done();
            return;
        }
        exprService.getAvailableFormats( dmc, rm );
    }

    GDBRegisterDMC createGDBRegisterDMC( String sessionId, MIRegisterDMC regDMC, IExpressions exprService ) {
    	exprService.createExpression( regDMC, createRegisterExpression( regDMC.getName() ) );
    	return new GDBRegisterDMC( sessionId, regDMC, regDMC.getRegNo(), regDMC.getName() );
    }

    static String createRegisterExpression( String regName ) {
        return String.format( "$%s", regName ); //$NON-NLS-1$
    }

    MIRegisterGroupDMC getRegisterGroupDMC( IRegisterDMContext regDMC ) {
        for ( IDMContext parent : regDMC.getParents() )
            if ( parent instanceof MIRegisterGroupDMC )
                return (MIRegisterGroupDMC)parent;
        return null;
    }
    
    IContainerDMContext getContainerDMC( IRegisterDMContext regDMC ) {
        for ( IDMContext parent : regDMC.getParents() )
            if ( parent instanceof IContainerDMContext )
                return (IContainerDMContext)parent;
        return null;
    }
}
