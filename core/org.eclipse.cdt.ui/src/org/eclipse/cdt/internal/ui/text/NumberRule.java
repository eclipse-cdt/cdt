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

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Recognizes numbers;
 *
 * @author P.Tomaszewski
 */
public class NumberRule implements IRule
{
    /** Style token. */
    private IToken token;

    
    /**
     * Creates new number rule.
     * @param token Style token. 
     */
    public NumberRule(IToken token)
    {
        super();
        this.token = token;
    }

    /**
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner)
    {
        int startCh = scanner.read();
        int ch; 
        
        if (isNumberStart(startCh))
        {
            ch = scanner.read();
            boolean hexNumber = ch == 'x';
            boolean decNumber = false;
            if (!hexNumber)
            {
                decNumber = Character.isDigit((char)ch);
            }
            if (!hexNumber && !decNumber)
            {
                scanner.unread();
                // If minus only it should be qualified as operator.
                if (startCh == '-')
                {
                    scanner.unread();
                    return Token.UNDEFINED;
                }
                return token;
            }
            if (hexNumber)
            {
                do
                {
                    ch = scanner.read();
                } while (isHexNumberPart((char)ch));
            }
            else if (decNumber)
            {
                do
                {
                    ch = scanner.read();
                } while (Character.isDigit((char)ch));                    
            }
            scanner.unread();
            return token;
        }
        scanner.unread();
        return Token.UNDEFINED;
    }

    /**
     * Checks if start of number.
     * @param ch Char to check.
     * @return <b>true</b> if Number.
     */
    private boolean isNumberStart(int ch)
    {
        return ch == '-' || Character.isDigit((char)ch);
    }
    
    /**
     * Checks if part of hex number;
     * @param ch Char to check.
     * @return <b>true</b>
     */
    private boolean isHexNumberPart(int ch)
    {
        return Character.isDigit((char)ch) || ch == 'a' || ch == 'b'
            || ch == 'c' || ch == 'd' || ch == 'e' || ch == 'f' || ch == 'A'
            || ch == 'B' || ch == 'C' || ch == 'D' || ch == 'E' || ch == 'F';
    }
}
