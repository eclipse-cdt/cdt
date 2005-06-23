/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.rules.IToken;

/**
 * Rule to recognize operators.
 *
 * @author P.Tomaszewski
 */
public class COperatorRule extends SingleCharRule
{
    /**
     * Creates new rule.
     * @param token Style token.
     */
    public COperatorRule(IToken token)
    {
        super(token);
    }

    /**
     * @see org.eclipse.cdt.internal.ui.text.SingleCharRule#isRuleChar(int)
     */
    public boolean isRuleChar(int ch)
    {
        return (ch == ';' || ch == '.' || ch == ':' || ch == '=' || ch == '-'
            || ch == '+' || ch == '\\' || ch == '*' || ch == '!' || ch == '%'
            || ch == '^' || ch == '&' || ch == '~' || ch == '>' || ch == '<')
            || ch == '|';
    }
}
