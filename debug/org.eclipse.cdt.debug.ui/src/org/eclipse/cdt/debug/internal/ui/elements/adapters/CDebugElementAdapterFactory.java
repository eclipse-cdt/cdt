/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * IBM Corporation
 * ARM Limited - https://bugs.eclipse.org/bugs/show_bug.cgi?id=186981
 * Wind River Systems - adapted to work with platform Modules view (bug 210558)
 * Wind River Systems - flexible hierarchy Signals view (bug 338908)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.elements.adapters; 

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextProvider;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.IDisassemblyLine;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.cdt.debug.internal.core.CDisassemblyContextProvider;
import org.eclipse.cdt.debug.internal.core.model.DisassemblyRetrieval;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.SourceDisplayAdapter;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleContentProvider;
import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleMementoProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementAnnotationProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementContentProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementLabelProvider;
import org.eclipse.cdt.debug.ui.disassembly.IElementToggleBreakpointAdapter;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
 
public class CDebugElementAdapterFactory implements IAdapterFactory {

    private static IElementContentProvider fgDebugTargetContentProvider = new CDebugTargetContentProvider();
    private static IElementContentProvider fgThreadContentProvider = new CThreadContentProvider();
    private static IElementContentProvider fgStackFrameContentProvider = new CStackFrameContentProvider();
    private static IElementContentProvider fgModuleContentProvider = new ModuleContentProvider();
    private static IElementContentProvider fgCRegisterManagerContentProvider = new CRegisterManagerContentProvider();

    private static IModelProxyFactory fgDebugElementProxyFactory = new CDebugElementProxyFactory();
    
    private static IElementMementoProvider fgStackFrameMementoProvider = new CStackFrameMementoProvider();
    private static IElementMementoProvider fgModuleMementoProvider = new ModuleMementoProvider();

    private static IDisassemblyContextProvider fgDisassemblyContextProvider = new CDisassemblyContextProvider();
    private static IDocumentElementContentProvider fgDisassemblyContentProvider = new DisassemblyElementContentProvider();
    private static IDocumentElementLabelProvider fgDisassemblyLabelProvider = new DisassemblyElementLabelProvider();
    private static IElementToggleBreakpointAdapter fgDisassemblyToggleBreakpointAdapter = new DisassemblyToggleBreakpointAdapter();
    private static ISourceDisplay fgSourceDisplayAdapter = new SourceDisplayAdapter();

    private static IViewerInputProvider fgDefaultViewerInputProvider = new CDefaultViewerInputProvider();
    private static IViewerInputProvider fgStackFrameViewerInputProvider = new CStackFrameViewerInputProvider();
    private static IColumnPresentationFactory fgRegistersViewColumnPresentationFactory = new RegistersViewColumnPresentationFactory();
    private static IColumnPresentationFactory fgDefaultViewColumnPresentationFactory = new DefaultViewColumnPresentationFactory();
    private static IElementMementoProvider fgRegisterManagerProxyMementoProvider = new CRegisterManagerProxyMementoProvider();
    
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	@SuppressWarnings( "rawtypes" )
    public Object getAdapter( Object adaptableObject, Class adapterType ) {
	    if ( adapterType.isInstance( adaptableObject ) ) {
			return adaptableObject;
		}
		if ( adapterType.equals( IElementContentProvider.class ) ) {
			if ( adaptableObject instanceof ICDebugTarget ) {
				return fgDebugTargetContentProvider;
			}
            if ( adaptableObject instanceof ICThread ) {
                return fgThreadContentProvider;
            }
            if ( adaptableObject instanceof ICStackFrame ) {
                return fgStackFrameContentProvider;
            }
            if ( adaptableObject instanceof CRegisterManagerProxy ) {
                return fgCRegisterManagerContentProvider;
            }
            if ( adaptableObject instanceof ICModule || 
            		adaptableObject instanceof ICElement ) {
            	return fgModuleContentProvider;
            }
		}
		if ( adapterType.equals( IModelProxyFactory.class ) ) {
			if ( adaptableObject instanceof ICDebugTarget ) {
				return fgDebugElementProxyFactory;
			}
            if ( adaptableObject instanceof ICThread ) {
                return fgDebugElementProxyFactory;
            }
			if ( adaptableObject instanceof ICStackFrame ) {
			    return fgDebugElementProxyFactory;
			}
			if ( adaptableObject instanceof IModuleRetrieval ) {
                return fgDebugElementProxyFactory;
            }
            if ( adaptableObject instanceof DisassemblyRetrieval ) {
                return fgDebugElementProxyFactory;
            }
            if ( adaptableObject instanceof CRegisterManagerProxy ) {
                return fgDebugElementProxyFactory;
            }
		}
        if ( adapterType.equals( IElementMementoProvider.class ) ) {
            if ( adaptableObject instanceof ICStackFrame ) {
                return fgStackFrameMementoProvider;
            }
            if ( adaptableObject instanceof IModuleRetrieval 
                    || adaptableObject instanceof ICThread 
                    || adaptableObject instanceof ICModule
                    || adaptableObject instanceof ICElement ) {
                return fgModuleMementoProvider;
            }
            if ( adaptableObject instanceof CRegisterManagerProxy ) {
                return fgRegisterManagerProxyMementoProvider;
            }
        }
        if ( adapterType.equals( IDisassemblyContextProvider.class ) ) {
            if ( adaptableObject instanceof ICStackFrame ) {
                return fgDisassemblyContextProvider;
            }
        }
        if ( adapterType.equals( IDocumentElementContentProvider.class ) ) {
            if ( adaptableObject instanceof ICStackFrame ) {
                return fgDisassemblyContentProvider;
            }
        }
        if ( adapterType.equals( IDocumentElementLabelProvider.class ) ) {
            if ( adaptableObject instanceof IDisassemblyLine ) {
                return fgDisassemblyLabelProvider;
            }
        }
        if ( adapterType.equals( IElementToggleBreakpointAdapter.class ) ) {
            if ( adaptableObject instanceof IDisassemblyLine ) {
                return fgDisassemblyToggleBreakpointAdapter;
            }
        }
        if ( adapterType.equals( ISourceDisplay.class ) ) {
            if ( adaptableObject instanceof ICStackFrame ) {
                return fgSourceDisplayAdapter;
            }
        }
        if ( adapterType.equals( IViewerInputProvider.class ) ) {
            if ( adaptableObject instanceof ICDebugTarget 
                    || adaptableObject instanceof ICThread )
                return fgDefaultViewerInputProvider;
            if ( adaptableObject instanceof ICStackFrame ) {
                return fgStackFrameViewerInputProvider;
            }
        }
        if ( adapterType.equals( IColumnPresentationFactory.class ) ) {
            if ( adaptableObject instanceof CRegisterManagerProxy ) {
                return fgRegistersViewColumnPresentationFactory;
            } else if (adaptableObject instanceof ICDebugElement) {
            	return fgDefaultViewColumnPresentationFactory;
            }
        }
    	return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	@SuppressWarnings( "rawtypes" )
    public Class[] getAdapterList() {
		return new Class[] {
				IElementContentProvider.class,
				IModelProxyFactory.class,
        		IElementMementoProvider.class,
        		IDisassemblyContextProvider.class,
        		IDocumentElementContentProvider.class,
                IDocumentElementLabelProvider.class,
                IDocumentElementAnnotationProvider.class,
                IElementToggleBreakpointAdapter.class,
                ISourceDisplay.class,
                IViewerInputProvider.class,
			};
	}
}
