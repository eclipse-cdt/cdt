/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;


public class TokenUtil {
	private static final char[] SPACE = {' '};
	private static final char[] IMAGE_POUND_POUND = "##".toCharArray(); //$NON-NLS-1$
	private static final char[] IMAGE_POUND = "#".toCharArray(); //$NON-NLS-1$
	
	private static final char[] DIGRAPH_LBRACE=   "<%".toCharArray(); //$NON-NLS-1$
	private static final char[] DIGRAPH_RBRACE=   "%>".toCharArray(); //$NON-NLS-1$
	private static final char[] DIGRAPH_LBRACKET= "<:".toCharArray(); //$NON-NLS-1$
	private static final char[] DIGRAPH_RBRACKET= ":>".toCharArray(); //$NON-NLS-1$
	private static final char[] DIGRAPH_POUND = "%:".toCharArray(); //$NON-NLS-1$
	private static final char[] DIGRAPH_POUNDPOUND = "%:%:".toCharArray(); //$NON-NLS-1$

	public static boolean isOperator(int kind) {
		switch (kind) {
		case IToken.t_delete: case IToken.t_new:

		// bit operations
		case IToken.tAMPER: case IToken.tAMPERASSIGN:
		case IToken.tARROW: case IToken.tARROWSTAR:
		case IToken.tBITOR: case IToken.tBITORASSIGN:
		case IToken.tBITCOMPLEMENT:
		case IToken.tSHIFTL: case IToken.tSHIFTLASSIGN:
		case IToken.tSHIFTR: case IToken.tSHIFTRASSIGN:
		case IToken.tXOR: case IToken.tXORASSIGN:
		
        // logical operations
		case IToken.tNOT: case IToken.tAND: case IToken.tOR:

		// arithmetic
		case IToken.tDECR: case IToken.tINCR:
		case IToken.tDIV: case IToken.tDIVASSIGN:
		case IToken.tMINUS: case IToken.tMINUSASSIGN:
		case IToken.tMOD: case IToken.tMODASSIGN:
		case IToken.tPLUS: case IToken.tPLUSASSIGN:
		case IToken.tSTAR: case IToken.tSTARASSIGN:
		case IGCCToken.tMAX: case IGCCToken.tMIN:
			
		// comparison
		case IToken.tEQUAL: case IToken.tNOTEQUAL:
		case IToken.tGT: case IToken.tGTEQUAL:
		case IToken.tLT: case IToken.tLTEQUAL:
			
		// other
		case IToken.tASSIGN: case IToken.tCOMMA:
			return true;
		}
		
		return false;
	}

	public static char[] getImage(int type) {
        switch (type) {
    	case IToken.tPOUND:	       	return IMAGE_POUND;
    	case IToken.tPOUNDPOUND:   	return IMAGE_POUND_POUND;	
        case IToken.tCOLONCOLON:   	return Keywords.cpCOLONCOLON; 
        case IToken.tCOLON:        	return Keywords.cpCOLON;
        case IToken.tSEMI:         	return Keywords.cpSEMI;
        case IToken.tCOMMA:        	return Keywords.cpCOMMA;
        case IToken.tQUESTION:     	return Keywords.cpQUESTION;
        case IToken.tLPAREN :      	return Keywords.cpLPAREN;
        case IToken.tRPAREN :      	return Keywords.cpRPAREN;
        case IToken.tLBRACKET:     	return Keywords.cpLBRACKET;
        case IToken.tRBRACKET:     	return Keywords.cpRBRACKET;
        case IToken.tLBRACE:       	return Keywords.cpLBRACE;
        case IToken.tRBRACE:       	return Keywords.cpRBRACE;
        case IToken.tPLUSASSIGN:   	return Keywords.cpPLUSASSIGN;
        case IToken.tINCR:         	return Keywords.cpINCR;
        case IToken.tPLUS:         	return Keywords.cpPLUS;
        case IToken.tMINUSASSIGN:  	return Keywords.cpMINUSASSIGN;
        case IToken.tDECR:         	return Keywords.cpDECR;
        case IToken.tARROWSTAR:    	return Keywords.cpARROWSTAR;
        case IToken.tARROW:        	return Keywords.cpARROW;
        case IToken.tMINUS:        	return Keywords.cpMINUS;
        case IToken.tSTARASSIGN:   	return Keywords.cpSTARASSIGN;
        case IToken.tSTAR:         	return Keywords.cpSTAR;
        case IToken.tMODASSIGN:    	return Keywords.cpMODASSIGN;
        case IToken.tMOD:          	return Keywords.cpMOD;
        case IToken.tXORASSIGN:    	return Keywords.cpXORASSIGN;
        case IToken.tXOR:          	return Keywords.cpXOR;
        case IToken.tAMPERASSIGN:  	return Keywords.cpAMPERASSIGN;
        case IToken.tAND:          	return Keywords.cpAND;
        case IToken.tAMPER:        	return Keywords.cpAMPER;
        case IToken.tBITORASSIGN:  	return Keywords.cpBITORASSIGN;
        case IToken.tOR:           	return Keywords.cpOR;
        case IToken.tBITOR:        	return Keywords.cpBITOR;
        case IToken.tBITCOMPLEMENT:	return Keywords.cpCOMPL;
        case IToken.tNOTEQUAL:     	return Keywords.cpNOTEQUAL;
        case IToken.tNOT:          	return Keywords.cpNOT;
        case IToken.tEQUAL:        	return Keywords.cpEQUAL;
        case IToken.tASSIGN:       	return Keywords.cpASSIGN;
        case IToken.tSHIFTL:       	return Keywords.cpSHIFTL;
        case IToken.tLTEQUAL:      	return Keywords.cpLTEQUAL;
        case IToken.tLT:           	return Keywords.cpLT;
        case IToken.tSHIFTRASSIGN: 	return Keywords.cpSHIFTRASSIGN;
        case IToken.tSHIFTR:       	return Keywords.cpSHIFTR;
        case IToken.tGTEQUAL:      	return Keywords.cpGTEQUAL;
        case IToken.tGT_in_SHIFTR:
        case IToken.tGT:           	return Keywords.cpGT;
        case IToken.tSHIFTLASSIGN: 	return Keywords.cpSHIFTLASSIGN;
        case IToken.tELLIPSIS:     	return Keywords.cpELLIPSIS;
        case IToken.tDOTSTAR:      	return Keywords.cpDOTSTAR;
        case IToken.tDOT:          	return Keywords.cpDOT;
        case IToken.tDIVASSIGN:    	return Keywords.cpDIVASSIGN;
        case IToken.tDIV:          	return Keywords.cpDIV;
        
        case IGCCToken.tMIN:		return Keywords.cpMIN;
        case IGCCToken.tMAX:		return Keywords.cpMAX;
        
        case CPreprocessor.tSPACE:  return SPACE; 
        case CPreprocessor.tNOSPACE: return CharArrayUtils.EMPTY;
        
        default:
            return CharArrayUtils.EMPTY; 
        }
	}
	
	public static char[] getDigraphImage(int type) {
        switch (type) {
    	case IToken.tPOUND:	       	return DIGRAPH_POUND;
    	case IToken.tPOUNDPOUND:   	return DIGRAPH_POUNDPOUND;	
        case IToken.tLBRACKET:     	return DIGRAPH_LBRACKET;
        case IToken.tRBRACKET:     	return DIGRAPH_RBRACKET;
        case IToken.tLBRACE:       	return DIGRAPH_LBRACE;
        case IToken.tRBRACE:       	return DIGRAPH_RBRACE;
        
        default:
        	assert false: type;
            return CharArrayUtils.EMPTY; 
        }
	}
	
	
	/**
	 * Returns the last token in the given token list.
	 * @throws NullPointerException if the argument is null
	 */
	public static IToken getLast(IToken tokenList) {
		IToken last;
		do {
			last = tokenList;
		} while((tokenList = tokenList.getNext()) != null);
		return last;
	}
	
}
