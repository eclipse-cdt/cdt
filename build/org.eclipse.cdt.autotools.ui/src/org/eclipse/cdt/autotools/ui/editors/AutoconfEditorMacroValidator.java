/*******************************************************************************
 * Copyright (c) 2007 Red Hat Corporation, (c) 2008 Nokia Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	  Red Hat Incorporated - initial API and implementation
 *    Ed Swartz (Nokia) - refactoring
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfMacroElement;
import org.eclipse.cdt.autotools.ui.editors.parser.IAutoconfMacroValidator;
import org.eclipse.cdt.autotools.ui.editors.parser.InvalidMacroException;
import org.eclipse.cdt.autotools.ui.editors.parser.ParseException;
import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.cdt.internal.autotools.ui.preferences.AutotoolsEditorPreferenceConstants;
import org.eclipse.cdt.internal.autotools.ui.text.hover.AutoconfPrototype;
import org.eclipse.cdt.internal.autotools.ui.text.hover.AutoconfTextHover;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;


/**
 *	Validate a macro call by checking against the stored macro prototypes
 */
public class AutoconfEditorMacroValidator implements IAutoconfMacroValidator {
	public final String AUTOCONF_MACRO_ARGS_TOO_FEW = "AutoconfMacroArgsTooFew"; //$NON-NLS-1$
	public final String AUTOCONF_MACRO_ARGS_TOO_MANY = "AutoconfMacroArgsTooMany"; //$NON-NLS-1$

	private AutoconfEditor fEditor;

	public AutoconfEditorMacroValidator(AutoconfEditor autoconfEditor) {
		fEditor = autoconfEditor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.autotools.ui.editors.parser.IAutoconfMacroValidator#validateMacroCall(org.eclipse.cdt.autotools.core.ui.editors.parser.AutoconfMacroElement)
	 */
	public void validateMacroCall(AutoconfMacroElement macro)
			throws ParseException, InvalidMacroException {
		AutoconfPrototype p = AutoconfTextHover.getPrototype(macro.getName(), fEditor);
		if (p != null) {
			boolean tooFew = false;
			boolean tooMany = false;
			boolean justRight = false;
			int parms = macro.getParameterCount();
			int numPrototypes = p.getNumPrototypes();
			int minParms = 0;
			int maxParms = 0;
			for (int i = 0; i < numPrototypes; ++i) {
				if (parms < p.getMinParms(i)) {
					tooFew = true;
					minParms = p.getMinParms(i);
				} else if (parms > p.getMaxParms(i)) {
					tooMany = true;
					maxParms = p.getMaxParms(i);
				} else {
					justRight = true;
					break;
				}
			}
			
			int length = macro.getEndOffset() - macro.getStartOffset();
			int start = macro.getStartOffset();
			int end = macro.getEndOffset();
			int lineNumber = 0;
			try {
				lineNumber = macro.getDocument().getLineOfOffset(start);
			} catch (BadLocationException e) {
				
			}
			
			if (!justRight) {
				if (tooFew) {
					String formatString = AutoconfEditorMessages.getFormattedString(AUTOCONF_MACRO_ARGS_TOO_FEW, 
							AutotoolsPlugin.getDefault().getPreferenceStore().getString(AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION),
							p.getName(), Integer.toString(minParms));
					throw new ParseException(
							formatString,
							start, end,
							lineNumber, 0, length,
							IMarker.SEVERITY_WARNING);
				} else if (tooMany) {
					String formatString = AutoconfEditorMessages.getFormattedString(AUTOCONF_MACRO_ARGS_TOO_MANY,
							AutotoolsPlugin.getDefault().getPreferenceStore().getString(AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION),
							p.getName(), Integer.toString(maxParms));
					throw new ParseException(
							formatString,
							start, end,
							lineNumber, 0, length,
							IMarker.SEVERITY_WARNING);
				}
			}
			
			IProject project = fEditor.getProject();
			String acDocVer = AutoconfTextHover.getDefaultAutoconfMacrosVer();
			try {
				String acVer = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION);
				if (acVer != null)
					acDocVer = acVer;
				else { // look for compat project properties
					acVer = project.getPersistentProperty(AutotoolsPropertyConstants.AUTOCONF_VERSION_COMPAT);
					if (acVer != null)
						acDocVer = acVer;
				}
			} catch (CoreException ce1) {
				// do nothing
			}
			
			macro.validate(acDocVer);

		}

	}

}
