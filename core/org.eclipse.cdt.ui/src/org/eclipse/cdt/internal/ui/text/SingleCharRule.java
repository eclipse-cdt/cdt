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
