/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents the Build Macro that could be loaded
 * and stored in XML
 * 
 * @since 3.0
 *
 */
public class StorableBuildMacro extends BuildMacro {
	public static final String STRING_MACRO_ELEMENT_NAME = "stringMacro"; //$NON-NLS-1$
	public static final String STRINGLIST_MACRO_ELEMENT_NAME = "stringListMacro"; //$NON-NLS-1$
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String VALUE = "value"; //$NON-NLS-1$
	public static final String TYPE = "type"; //$NON-NLS-1$
	
	public static final String VALUE_ELEMENT_NAME = "value"; //$NON-NLS-1$
	public static final String VALUE_ELEMENT_VALUE = "name"; //$NON-NLS-1$
	
    public static final String TYPE_TEXT = "VALUE_TEXT"; //$NON-NLS-1$
    public static final String TYPE_TEXT_LIST = "VALUE_TEXT_LIST"; //$NON-NLS-1$
    public static final String TYPE_PATH_FILE = "VALUE_PATH_FILE"; //$NON-NLS-1$
    public static final String TYPE_PATH_FILE_LIST = "VALUE_PATH_FILE_LIST"; //$NON-NLS-1$
    public static final String TYPE_PATH_DIR = "VALUE_PATH_DIR"; //$NON-NLS-1$
    public static final String TYPE_PATH_DIR_LIST = "VALUE_PATH_DIR_LIST"; //$NON-NLS-1$
    public static final String TYPE_PATH_ANY = "VALUE_PATH_ANY"; //$NON-NLS-1$
    public static final String TYPE_PATH_ANY_LIST = "VALUE_PATH_ANY_LIST";  //$NON-NLS-1$
	
	public StorableBuildMacro(String name, int type, String value){
		super(name,type,value);
	}
	
	public StorableBuildMacro(String name, int type, String value[]){
		super(name,type,value);
	}

	public StorableBuildMacro(Element element){
		load(element);
	}
	
	private void load(Element element){
		fName = element.getAttribute(NAME);

		fType = typeStringToInt(element.getAttribute(TYPE));
		
		if(!MacroResolver.isStringListMacro(fType))
			fStringValue = element.getAttribute(VALUE);
		else {
			NodeList nodeList = element.getChildNodes();
			List values = new ArrayList();
			for (int i = 0; i < nodeList.getLength(); ++i) {
				Node node = nodeList.item(i);
				if (node.getNodeName().equals(VALUE_ELEMENT_NAME)) {
					values.add(((Element)node).getAttribute(VALUE_ELEMENT_VALUE));
				}
			}
			fStringListValue = (String[])values.toArray(new String[values.size()]);
		}
	}
	
	private int typeStringToInt(String typeString){
		int type;
		
		if(TYPE_TEXT_LIST.equals(typeString))
			type = VALUE_TEXT_LIST;
		else if(TYPE_PATH_FILE.equals(typeString))
			type = VALUE_PATH_FILE;
		else if(TYPE_PATH_FILE_LIST.equals(typeString))
			type = VALUE_PATH_FILE_LIST;
		else if(TYPE_PATH_DIR.equals(typeString))
			type = VALUE_PATH_DIR;
		else if(TYPE_PATH_DIR_LIST.equals(typeString))
			type = VALUE_PATH_DIR_LIST;
		else if(TYPE_PATH_ANY.equals(typeString))
			type = VALUE_PATH_ANY;
		else if(TYPE_PATH_ANY_LIST.equals(typeString))
			type = VALUE_PATH_ANY_LIST;
		else
			type = VALUE_TEXT;
		
		return type;
	}
	
	private String typeIntToString(int type){
		String stringType;

		switch(type){
		case VALUE_TEXT_LIST:
			stringType = TYPE_TEXT_LIST;
			break;
		case VALUE_PATH_FILE:
			stringType = TYPE_PATH_FILE;
			break;
		case VALUE_PATH_FILE_LIST:
			stringType = TYPE_PATH_FILE_LIST;
			break;
		case VALUE_PATH_DIR:
			stringType = TYPE_PATH_DIR;
			break;
		case VALUE_PATH_DIR_LIST:
			stringType = TYPE_PATH_DIR_LIST;
			break;
		case VALUE_PATH_ANY:
			stringType = TYPE_PATH_ANY;
			break;
		case VALUE_PATH_ANY_LIST:
			stringType = TYPE_PATH_ANY_LIST;
			break;
		case VALUE_TEXT:
		default:
			stringType = TYPE_TEXT;
			break;
		}
		
		return stringType;
	}

	public void serialize(Document doc, Element element){
		if(fName != null)
			element.setAttribute(NAME,fName);
		
		element.setAttribute(TYPE,typeIntToString(fType));

		if(!MacroResolver.isStringListMacro(fType)){
			if(fStringValue != null)
				element.setAttribute(VALUE,fStringValue);
		}
		else {
			if(fStringListValue != null && fStringListValue.length > 0){
				for(int i = 0; i < fStringListValue.length; i++){
					Element valEl = doc.createElement(VALUE_ELEMENT_NAME);
					element.appendChild(valEl);
					if(fStringListValue[i] != null)
						valEl.setAttribute(VALUE_ELEMENT_VALUE, fStringListValue[i]);
				}
			}
		}

	}
}
