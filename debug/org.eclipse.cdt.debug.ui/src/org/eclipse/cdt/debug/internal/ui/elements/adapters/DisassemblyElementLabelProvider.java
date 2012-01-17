/*******************************************************************************
 * Copyright (c) 2008, 2009 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.IDisassemblyInstruction;
import org.eclipse.cdt.debug.core.model.IDisassemblySourceLine;
import org.eclipse.cdt.debug.internal.ui.disassembly.editor.DisassemblyEditorPresentation;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementLabelProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementLabelUpdate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * org.eclipse.cdt.debug.internal.ui.elements.adapters.DisassemblyElementLabelProvider: 
 * //TODO Add description.
 */
public class DisassemblyElementLabelProvider implements IDocumentElementLabelProvider {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.ui.disassembly.IDocumentElementLabelProvider#update(org.eclipse.cdt.debug.ui.disassembly.IDocumentElementLabelUpdate[])
     */
    @Override
	public void update( final IDocumentElementLabelUpdate[] updates ) {
        Job job = new Job( "Label update" ) { //$NON-NLS-1$

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
            protected IStatus run( IProgressMonitor monitor ) {
                for( int i = 0; i < updates.length; i++ ) {
                    IDocumentElementLabelUpdate update = updates[i];
                    if ( !update.isCanceled() ) {
                        retrieveLabel( update );
                    }
                    update.done();
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem( true );
        job.schedule();
    }

    protected void retrieveLabel( IDocumentElementLabelUpdate update ) {
        IPresentationContext context = update.getPresentationContext();
        if ( context instanceof DisassemblyEditorPresentation ) {
            DisassemblyEditorPresentation presentation = (DisassemblyEditorPresentation)context;
            Object element = update.getElement();
            if ( element instanceof IDisassemblyInstruction ) {
                IDisassemblyInstruction instruction = (IDisassemblyInstruction)element;
                StringBuilder sb = new StringBuilder();
                if ( presentation.showAddresses() ) {
                    BigInteger address = instruction.getAdress().getValue();
                    sb.append( "0x" ); //$NON-NLS-1$
                    sb.append( CDebugUtils.prependString( Long.toHexString( address.longValue() ), 8, '0' ) );
                    sb.append( '\t' );
                }
                sb.append( instruction.getInstructionText() );
                update.setLabel( DisassemblyEditorPresentation.ATTR_LINE_LABEL, sb.toString() );
            }
            else if ( element instanceof IDisassemblySourceLine ) {
                IDisassemblySourceLine line = (IDisassemblySourceLine)element;
                StringBuilder sb = new StringBuilder();
                if ( presentation.showLineNumbers() ) {
                    sb.append( line.getLineNumber() );
                    sb.append( '\t' );
                }
                sb.append( getSourceLineText( line ) );
                update.setLabel( DisassemblyEditorPresentation.ATTR_LINE_LABEL, sb.toString() );
            }
        }
    }

    private String getSourceLineText( IDisassemblySourceLine line ) {
        File file = line.getFile();
        String text = MessageFormat.format( "File {0} not found.", new Object[] {file.getPath()} );
        ISourceLocator locator = line.getDebugTarget().getLaunch().getSourceLocator();
        if ( locator instanceof ISourceLookupDirector ) {
            ISourceLookupDirector director = (ISourceLookupDirector)locator;
            Object sourceElement = director.getSourceElement( file.getPath() );
            if ( sourceElement != null ) {
                File lookupFile = null;
                if ( sourceElement instanceof IFile ) {
                    lookupFile = ((IFile)sourceElement).getLocation().toFile();
                }
                else if ( sourceElement instanceof IStorage ) {
                    lookupFile = ((IStorage)sourceElement).getFullPath().toFile();
                }
                if ( lookupFile != null ) {
                    try {
                        text = readLine( lookupFile, line.getLineNumber() - 1 );
                    }
                    catch( IOException e ) {
                        text = e.getLocalizedMessage();
                    }
                }
            }            
        }
        return text;
    }

    private String readLine( File file, int lineNumber ) throws IOException {
        FileReader fr = new FileReader( file );
        BufferedReader br = new BufferedReader( fr );
        
        try {
            int count = 0;
            String result = null;
            do {
                result = br.readLine();
                if ( count++ == lineNumber )
                    return result;
            } 
            while( result != null );
        }
        finally {
            br.close();
        }
        
        throw new IOException( MessageFormat.format( "Line {0} doesn't exist in {1}.", new Object[] {Integer.valueOf( lineNumber ), file.getPath()}) );
    }
}
