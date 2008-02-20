/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.expressions;

import java.util.Map;

import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext;

/**
 * 
 */
public class ExpressionDMData implements IExpressionDMData {

    final private String fExpression;
    
    public ExpressionDMData(String expression) {
        fExpression = expression;
    }
    
    public BasicType getBasicType() {
        return BasicType.basic;
    }

    public String getEncoding() {
        return null;
    }

    public Map<String, Integer> getEnumerations() {
        return null;
    }

    public String getName() {
        return fExpression;
    }

    public IRegisterDMContext getRegister() {
        return null;
    }

    public String getStringValue() {
        return null;
    }

    public String getTypeId() {
        return null;
    }

    public String getTypeName() {
        return null;
    }

}
