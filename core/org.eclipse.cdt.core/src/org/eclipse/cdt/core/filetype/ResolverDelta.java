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


public class ResolverDelta {
	public static final int EVENT_ADD			= 0x10;
	public static final int EVENT_REMOVE		= 0x20;
	public static final int EVENT_SET			= 0x40;
	public static final int EVENT_MASK			= 0xF0;

	public static final int ELEMENT_LANGUAGE 	= 0x01;
	public static final int ELEMENT_FILETYPE 	= 0x02;
	public static final int ELEMENT_ASSOCIATION = 0x04;
	public static final int ELEMENT_MASK		= 0x0F;
	
	private Object	fElement;
	private int		fEvent;

	public ResolverDelta(int eventType, int elementType, Object element) {
		fElement	= element;
		fEvent		= eventType | elementType;
	}

	public ResolverDelta(ICLanguage lang, int event) {
		this(event, ELEMENT_LANGUAGE, lang);
	}

	public ResolverDelta(ICFileType type, int event) {
		this(event, ELEMENT_FILETYPE, type);
	}
	
	public ResolverDelta(ICFileTypeAssociation assoc, int event) {
		this(event, ELEMENT_ASSOCIATION, assoc);
	}

	public Object getElement() {
		return fElement;
	}

	public int getElementType() {
		return fEvent & ELEMENT_MASK;
	}

	public int getEventType() {
		return fEvent & EVENT_MASK;
	}

	public ICLanguage getLanguage() {
		return ((fElement instanceof ICLanguage) ? ((ICLanguage) fElement) : null);
	}

	public ICFileType getFileType() {
		return ((fElement instanceof ICFileType) ? ((ICFileType) fElement) : null);
	}

	public ICFileTypeAssociation getAssociation() {
		return ((fElement instanceof ICFileTypeAssociation) ? ((ICFileTypeAssociation) fElement) : null);
	}

	public int getEvent() {
		return fEvent;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		switch (getEventType()) {
			case EVENT_ADD:
				buf.append("add");
				break;
			case EVENT_REMOVE:
				buf.append("remove");
				break;
			case EVENT_SET:
				buf.append("set");
				break;
			default:
				buf.append("?unknown event?");
				break;
		}
		buf.append(" ");
		switch (getElementType()) {
			case ELEMENT_LANGUAGE:
				buf.append("language ");
				buf.append(null != getLanguage() ? getLanguage().getName() : "?");
				break;
			case ELEMENT_FILETYPE:
				buf.append("filetype ");
				buf.append(null != getFileType() ? getFileType().getName() : "?");
				break;
			case ELEMENT_ASSOCIATION:
				buf.append("assoc ");
				buf.append(null != getAssociation() ? getAssociation().getPattern() : "?");
				break;
			default:
				buf.append("?unknown source?");
				break;
		}
		
		return buf.toString();
	}
}
