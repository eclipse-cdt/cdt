/*******************************************************************************
 * Copyright (c) 2007, 2009 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ARM Limited - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnPresentation;
import org.eclipse.debug.internal.ui.model.elements.DebugElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * Label provider for variables and registers.
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=186981
 * 
 * Using the internal platform classes because the API hasn't been defined.
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=187500 and 
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=187502 
 */
public class CVariableLabelProvider extends DebugElementLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.DebugElementLabelProvider#getLabel(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	protected String getLabel( TreePath elementPath, IPresentationContext context, String columnId ) throws CoreException {
		if ( columnId != null ) {
			IVariable variable = (IVariable)elementPath.getLastSegment();
			IValue value = variable.getValue();
			return getColumnText( variable, value, context, columnId );
		}
		return super.getLabel( elementPath, context, columnId );
	}

	protected String getValueText( IVariable variable, IValue value, IPresentationContext context ) throws CoreException {
		if ( value instanceof ICValue ) {
			return CDebugUIUtils.getValueText( value );
		}
		return null;
	}

	protected String getVariableTypeName( IVariable variable, IPresentationContext context ) throws CoreException {
		if ( variable instanceof ICVariable ) {
			return CDebugUIUtils.getVariableTypeName( ((ICVariable)variable).getType() );
		}
		return null;
	}

	protected String getVariableName( IVariable variable, IPresentationContext context ) throws CoreException {
		return CDebugUIUtils.getVariableName( variable );
	}

	protected String getColumnText( IVariable variable, IValue value, IPresentationContext context, String columnId ) throws CoreException {
		if ( VariableColumnPresentation.COLUMN_VARIABLE_NAME.equals( columnId ) ) {
			return getVariableName( variable, context );
		}
		else if ( VariableColumnPresentation.COLUMN_VARIABLE_TYPE.equals( columnId ) ) {
			return getVariableTypeName( variable, context );
		}
		else if ( VariableColumnPresentation.COLUMN_VARIABLE_VALUE.equals( columnId ) ) {
			return getValueText( variable, value, context );
		}
		return null; // super.getColumnText( variable, value, context, columnId );
	}

	protected ImageDescriptor getImageDescriptor( TreePath elementPath, IPresentationContext presentationContext, String columnId ) throws CoreException {
		if ( columnId == null || VariableColumnPresentation.COLUMN_VARIABLE_NAME.equals( columnId ) ) {
			return super.getImageDescriptor( elementPath, presentationContext, columnId );
		}
		return null; // super.getImageDescriptor( elementPath, presentationContext, columnId );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.DebugElementLabelProvider#getBackground(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	protected RGB getBackground( TreePath elementPath, IPresentationContext presentationContext, String columnId ) throws CoreException {
		Object element = elementPath.getLastSegment();
		if ( columnId != null ) {
			if ( element instanceof IVariable ) {
				IVariable variable = (IVariable)element;
				if ( variable.hasValueChanged() ) {
					// No public access to the changed value background color of the Variables view. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=187509
					return DebugUITools.getPreferenceColor( IDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND ).getRGB();
				}
			}
		}
		return super.getBackground( elementPath, presentationContext, columnId );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.DebugElementLabelProvider#getFontData(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	protected FontData getFontData( TreePath elementPath, IPresentationContext presentationContext, String columnId ) throws CoreException {
		// No public access to the Variables view text font id. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=187509
		return JFaceResources.getFontDescriptor( IDebugUIConstants.PREF_VARIABLE_TEXT_FONT ).getFontData()[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.DebugElementLabelProvider#getForeground(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	protected RGB getForeground( TreePath elementPath, IPresentationContext presentationContext, String columnId ) throws CoreException {
		Object element = elementPath.getLastSegment();
		if ( columnId == null ) {
			if ( element instanceof IVariable ) {
				IVariable variable = (IVariable)element;
				if ( variable.hasValueChanged() ) {
					return DebugUITools.getPreferenceColor( IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR ).getRGB();
				}
			}
		}
		return super.getForeground( elementPath, presentationContext, columnId );
	}
}
