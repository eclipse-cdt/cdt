package org.eclipse.cdt.internal.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.internal.parser.generated.CPPParser;
import org.eclipse.cdt.internal.parser.generated.ParseException;
import org.eclipse.cdt.internal.parser.generated.ParserFatalException;
import org.eclipse.cdt.internal.parser.generated.TokenMgrError;


public class CStructurizer {
	
	private static CStructurizer fgStructurizerSingelton= new CStructurizer();
	
	public static CStructurizer getCStructurizer() {
		return fgStructurizerSingelton;
	}
		
	private CPPParser fParser;
	private CStructurizer() {
	}
		
	public synchronized void parse(IStructurizerCallback callback, InputStream inputStream) throws IOException {
		LinePositionInputStream lpiStream= new LinePositionInputStream(inputStream);	
		try {
			ParserCallback cb= new ParserCallback(lpiStream, callback);
			if (fParser == null) {
				fParser= new CPPParser(lpiStream);
			} else {
				fParser.ReInit(lpiStream);
			}
			fParser.setParserCallback(cb);
			
			fParser.translation_unit();
		} catch (TokenMgrError error) {
			callback.reportError(error);
		} catch (ParseException e) {
			callback.reportError(e);
		} catch( ParserFatalException e ) {
			callback.reportError(e);
		}
	}
}
