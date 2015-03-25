/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui.breakpoints;
import org.eclipse.cdt.examples.dsf.pda.ui.editor.PDAEditor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.ui.texteditor.ITextEditor;



/**
 * Creates a toggle breakpoint adapter
 * <p>
 * This class is identical to the corresponding in PDA debugger implemented in 
 * org.eclipse.debug.examples.ui.
 * </p>
 */
public class PDAEditorAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("unchecked") // IAdapterFactory is Java 1.3
    public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof PDAEditor) {
			ITextEditor editorPart = (ITextEditor) adaptableObject;
			IResource resource = editorPart.getEditorInput().getAdapter(IResource.class);
			if (resource != null) {
				String extension = resource.getFileExtension();
				if (extension != null && extension.equals("pda")) {
				    if (adapterType.equals(IToggleBreakpointsTarget.class)) {
				        return new PDABreakpointAdapter();
				    }
				}
			}			
		}
		return null;
	}

    @SuppressWarnings("unchecked") // IAdapterFactory is Java 1.3
    public Class[] getAdapterList() {
		return new Class[]{IToggleBreakpointsTarget.class};
	}
}
