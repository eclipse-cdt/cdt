/*******************************************************************************
 * Copyright (c) 2005, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Checks for single char.
 *
 * @author P.Tomaszewski
 */
public abstract class SingleCharRule implements IRule
{

    /** Style token. */
    private IToken token;

    /**
     * Creates new rule.
     * @param token Style token.
     */
    public SingleCharRule(IToken token)
    {
        super();
        this.token = token;
    }

    /**
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    @Override
	public IToken evaluate(ICharacterScanner scanner)
    {
        int ch = scanner.read();

        if (isRuleChar(ch))
        {
            return token;
        }
        scanner.unread();
        return Token.UNDEFINED;
    }

    /**
     * Checks if char is rule char.
     * @param ch Char to check.
     * @return <b>true</b> if rule char.
     */
    protected abstract boolean isRuleChar(int ch);
}
