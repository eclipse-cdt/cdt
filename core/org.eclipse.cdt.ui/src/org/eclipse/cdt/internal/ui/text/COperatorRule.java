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
