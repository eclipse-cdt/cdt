/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.filetype;

import java.util.EventObject;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;

public class ResolverChangeEvent extends EventObject {
	
	public static final int EVENT_ADD			= 0x10;
	public static final int EVENT_REMOVE		= 0x20;
	public static final int EVENT_SET			= 0x40;
	public static final int EVENT_MASK			= 0xF0;

	public static final int ELEMENT_LANGUAGE 	= 0x01;
	public static final int ELEMENT_FILETYPE 	= 0x02;
	public static final int ELEMENT_ASSOCIATION = 0x04;
	public static final int ELEMENT_RESOLVER	= 0x08;
	public static final int ELEMENT_MASK		= 0x0F;

	private Object fElement;
	private int fEventType;

	/**
	 * Create a new change event.
	 * 
	 * @param resolver file type resolver this event applies to
	 */
	public ResolverChangeEvent(IContainer container, int eventType, Object element) {
		super(container);
		fEventType = eventType;
		fElement = element;
	}

	public IContainer getContainer() {
		return (IContainer)getSource();
	}

	public int getEventType() {
		return fEventType & EVENT_MASK;
	}

	public Object getElement() {
		return fElement;
	}

	public int getElementType() {
		if (fElement instanceof ICLanguage) {
			return ELEMENT_LANGUAGE;
		} else if (fElement instanceof ICFileType) {
			return ELEMENT_FILETYPE;
		} else if (fElement instanceof ICFileTypeAssociation) {
			return ELEMENT_ASSOCIATION;
		} else if (fElement instanceof ICFileTypeResolver) {
			return ELEMENT_RESOLVER;
		}
		return 0;
	}


	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("ResolverChangeEvent ["); //$NON-NLS-1$

		IContainer container = getContainer();
		if (container instanceof IProject) {
			buf.append("For project "); //$NON-NLS-1$
			buf.append(container.getName());
		} else {
			buf.append("For workspace"); //$NON-NLS-1$
		}

		buf.append(' ');

		switch (getEventType()) {
		case EVENT_ADD:
			buf.append("add"); //$NON-NLS-1$
			break;
		case EVENT_REMOVE:
			buf.append("remove"); //$NON-NLS-1$
			break;
		case EVENT_SET:
			buf.append("set"); //$NON-NLS-1$
			break;
		default:
			buf.append("?unknown event?"); //$NON-NLS-1$
		break;
		}
		buf.append(' '); 
		switch (getElementType()) {
		case ELEMENT_LANGUAGE:
		{
			ICLanguage lang = (ICLanguage)getElement();
			buf.append("language "); //$NON-NLS-1$
			buf.append(lang.getName());
			break;
		}
		case ELEMENT_FILETYPE:
		{
			ICFileType fileType = (ICFileType)getElement();
			buf.append("filetype "); //$NON-NLS-1$
			buf.append(fileType.getName());
			break;
		}
		case ELEMENT_ASSOCIATION:
		{
			ICFileTypeAssociation association = (ICFileTypeAssociation)getElement();
			buf.append("assoc "); //$NON-NLS-1$
			buf.append(association.getPattern());
			break;
		}
		case ELEMENT_RESOLVER:
		{
			ICFileTypeResolver resolver = (ICFileTypeResolver)getElement();
			buf.append("resolver "); //$NON-NLS-1$
			break;
		}
		default:
			buf.append("?unknown source?"); //$NON-NLS-1$
		break;
		}
	
		buf.append(']');
		return buf.toString();
	}
}
