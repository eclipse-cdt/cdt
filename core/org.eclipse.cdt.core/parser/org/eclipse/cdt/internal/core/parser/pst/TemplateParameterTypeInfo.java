/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jul 5, 2004
 */
package org.eclipse.cdt.internal.core.parser.pst;

/**
 * @author aniefer
 */
public class TemplateParameterTypeInfo extends TypeInfo {
    
    public ITypeInfo.eType getTemplateParameterType() {
    	return _templateParameterType;
    }
    public void setTemplateParameterType( ITypeInfo.eType type ) {
    	_templateParameterType = type;
    }
    public boolean equals( Object t ) {
    	if( !super.equals( t ) ){
    		return false;
    	}
    	return _templateParameterType == ((ITypeInfo)t).getTemplateParameterType();
    }
    
    private ITypeInfo.eType _templateParameterType = t_typeName;
}
