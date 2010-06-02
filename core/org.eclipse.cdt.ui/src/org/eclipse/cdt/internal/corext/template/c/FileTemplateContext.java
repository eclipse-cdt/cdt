/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * A template context for plain file resources.
 *
 * @since 5.0
 */
public class FileTemplateContext extends TemplateContext {

	private String fLineDelimiter;

	public FileTemplateContext(String contextTypeId, String lineDelimiter) {
		super(CUIPlugin.getDefault().getCodeTemplateContextRegistry().getContextType(contextTypeId));
		fLineDelimiter= lineDelimiter;
	}

	/*
	 * @see org.eclipse.jface.text.templates.TemplateContext#evaluate(org.eclipse.jface.text.templates.Template)
	 */
	@Override
	public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
		// test that all variables are defined
		Iterator<?> iterator= getContextType().resolvers();
		while (iterator.hasNext()) {
			TemplateVariableResolver var= (TemplateVariableResolver) iterator.next();
			if (var.getClass() == FileTemplateContextType.FileTemplateVariableResolver.class) {
				Assert.isNotNull(getVariable(var.getType()), "Variable " + var.getType() + " not defined"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		if (!canEvaluate(template))
			return null;
			
		String pattern= changeLineDelimiter(template.getPattern(), fLineDelimiter);
		
		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(pattern);
		getContextType().resolve(buffer, this);
		return buffer;
	}
	
	private static String changeLineDelimiter(String code, String lineDelim) {
		try {
			ILineTracker tracker= new DefaultLineTracker();
			tracker.set(code);
			int nLines= tracker.getNumberOfLines();
			if (nLines == 1) {
				return code;
			}
			
			StringBuffer buf= new StringBuffer();
			for (int i= 0; i < nLines; i++) {
				if (i != 0) {
					buf.append(lineDelim);
				}
				IRegion region = tracker.getLineInformation(i);
				String line= code.substring(region.getOffset(), region.getOffset() + region.getLength());
				buf.append(line);
			}
			return buf.toString();
		} catch (BadLocationException e) {
			// can not happen
			return code;
		}
	}		

	/*
	 * @see org.eclipse.jface.text.templates.TemplateContext#canEvaluate(org.eclipse.jface.text.templates.Template)
	 */
	@Override
	public boolean canEvaluate(Template template) {
		return true;
	}
	
	public void setResourceVariables(IFile file) {
		setVariable(FileTemplateContextType.FILENAME, file.getName());
		setVariable(FileTemplateContextType.FILEBASE, new Path(file.getName()).removeFileExtension().lastSegment());
		IPath location= file.getLocation();
		setVariable(FileTemplateContextType.FILELOCATION, location != null ? location.toOSString() : ""); //$NON-NLS-1$
		setVariable(FileTemplateContextType.FILEPATH, file.getFullPath().toString());
		setVariable(FileTemplateContextType.PROJECTNAME, file.getProject().getName());
	}

}
