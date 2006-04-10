/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.clientserver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A singleton class that contains useful methods related to encodings.
 */
public class SystemEncodingUtil {
	
	private static SystemEncodingUtil instance;
	public static String ENCODING_UTF_8 = "UTF-8";

	/**
	 * Constructor to create the utility class.
	 */
	private SystemEncodingUtil() {
		super();
	}
	
	/**
	 * Returns the singleton instance of the utility class.
	 * @return the singleton instance.
	 */
	public static SystemEncodingUtil getInstance() {
		
		if (instance == null) {
			instance = new SystemEncodingUtil();
		}
		
		return instance;
	}
	
	/**
	 * Gets the encoding of the environment. This is the encoding being used by the JVM,
	 * which by default is the machine encoding, unless changed explicitly.
	 * @return the evironment encoding.
	 */
	public String getEnvironmentEncoding() {
		return System.getProperty("file.encoding");
	}
	
	/**
	 * Returns whether the file is an XML file.
	 * @param filePath the file path.
	 * @return <code>true</code> if the file is an XML file, <code>false</code> otherwise.
	 */
	public boolean isXML(String filePath) {
		
		int index = filePath.lastIndexOf(".");
		
		// check if there is a "."
		if (index == -1) {
			return false;
		}
		else {
			
			// check if the name ends with "."
			if (index == filePath.length() - 1) {
				return false;
			}
			else {
				String extension = filePath.substring(index+1);
				
				if (extension.equalsIgnoreCase("xml") || extension.equalsIgnoreCase("xmi")) {
					return true;
				}
				else {
					return false;
				}
			}
		}
	}
	
	/**
	 * Gets the encoding of an XML file.
	 * @param filePath the file path.
	 * @return the encoding, or <code>null</code> if the encoding could not be determined.
	 */
	public String getXMLFileEncoding(String filePath) throws IOException {
		
		String encoding = null;
		
		// this is an implementation of the encoding detection algorithm
		// as specified in Appendix F of the XML specification
		
		FileInputStream stream = new FileInputStream(filePath);
		
		// try to get the encoding if the file starts with a BOM
		String encodingGuess = getEncodingFromBOM(stream);
		
		stream.close();
		
		// if no BOM, read in bytes corresponding to the first four chars in the header, i.e. "<?xm"
		// and try to determine the encoding from that
		if (encodingGuess == null) {
			
			stream = new FileInputStream(filePath);

			byte[] temp = new byte[4];
			
			stream.read(temp);
			
			
			// UCS-4 or other encoding with a 32-bit code unit and ASCII characters encoded as
			// ASCII values, in respectively big-endian (1234), little-endian (4321) and two
			// unusual byte orders (2143 and 3412). The encoding declaration must be read to
			// determine which of UCS-4 or other supported 32-bit encodings applies. 

			// UCS-4, big-endian order (1234 order)
			if (temp[0] == 0x00 && temp[1] == 0x00 && temp[2] == 0x00 && temp[3] == 0x3C) {
				encodingGuess = null;
			}
			// UCS-4, little-endian order (4321 order)
			else if (temp[0] == 0x3C && temp[1] == 0x00 && temp[2] == 0x00 && temp[3] == 0x00) {
				encodingGuess = null;
			}
			// UCS-4, unusual octet order (2143)
			else if (temp[0] == 0x00 && temp[1] == 0x00 && temp[2] == 0x3C && temp[3] == 0x00) {
				encodingGuess = null;
			}
			// UCS-4, unusual octet order (3412)
			else if (temp[0] == 0x00 && temp[1] == 0x3C && temp[2] == 0x00 && temp[3] == 0x00) {
				encodingGuess = null;
			}
			
			
			// UTF-16BE or big-endian ISO-10646-UCS-2 or other encoding with a 16-bit code unit
			// in big-endian order and ASCII characters encoded as ASCII values (the encoding
			// declaration must be read to determine which)
			else if (temp[0] == 0x00 && temp[1] == 0x3C && temp[2] == 0x00 && temp[3] == 0x3F) {
				encodingGuess = "UnicodeBigUnmarked";
			}
			
			
			// UTF-16LE or little-endian ISO-10646-UCS-2 or other encoding with a 16-bit code unit
			// in little-endian order and ASCII characters encoded as ASCII values (the encoding
			// declaration must be read to determine which)
			else if (temp[0] == 0x3C && temp[1] == 0x00 && temp[2] == 0x3F && temp[3] == 0x00) {
				encodingGuess = "UnicodeLittleUnmarked";
			}
			
			
			// UTF-8, ISO 646, ASCII, some part of ISO 8859, Shift-JIS, EUC, or any other 7-bit,
			// 8-bit, or mixed-width encoding which ensures that the characters of ASCII have their
			// normal positions, width, and values; the actual encoding declaration must be read to
			// detect which of these applies, but since all of these encodings use the same bit patterns
			// for the relevant ASCII characters, the encoding declaration itself may be read reliably
			else if (temp[0] == 0x3C && temp[1] == 0x3F && temp[2] == 0x78 && temp[3] == 0x6D) {
				encodingGuess = SystemEncodingUtil.ENCODING_UTF_8;
			}
			
			
			// EBCDIC (in some flavor; the full encoding declaration must be read to tell which
			// code page is in use)
			else if (temp[0] == 0x4C && temp[1] == 0x6F && temp[2] == 0xA7 && temp[3] == 0x94) {
				encodingGuess = "Cp037";
			}
			
			
			// UTF-8 without an encoding declaration, or else the data stream is mislabeled
			// (lacking a required encoding declaration), corrupt, fragmentary, or enclosed in a
			// wrapper of some kind
			else {
				
				// From section 4.3.3 of the XML specification:
				// In the absence of information provided by an external transport protocol
				// (e.g. HTTP or MIME), it is an error for an entity including an encoding declaration
				// to be presented to the XML processor in an encoding other than that named in the
				// declaration, or for an entity which begins with neither a Byte Order Mark nor an
				// encoding declaration to use an encoding other than UTF-8. Note that since ASCII is
				// a subset of UTF-8, ordinary ASCII entities do not strictly need an encoding declaration.
				
				// We'll assume that this is UTF-8 or ASCII encoding without an encoding declaration.
				// Of course, it could also be another encoding that doesn't have an encoding declaration
				// in which case it has violated the XML specification (any encoding beside UTF-8 or UTF-16
				// must specify the character encoding). From section 4.3.3 of the XML specification:
				// In the absence of external character encoding information (such as MIME headers),
				// parsed entities which are stored in an encoding other than UTF-8 or UTF-16 must begin
				// with a text declaration (see 4.3.1 The Text Declaration) containing an encoding declaration. 
				encodingGuess = SystemEncodingUtil.ENCODING_UTF_8;
			}
			
			stream.close();
		}
		
		// if we have a guess, we need to read in the encoding declaration to get the actula encoding
		// the guess tells us the encoding of the family
		if (encodingGuess != null) {
			
			stream = new FileInputStream(filePath);
			InputStreamReader reader = new InputStreamReader(stream, encodingGuess);
			BufferedReader bufReader = new BufferedReader(reader);
			
			String line = bufReader.readLine();
			
			while (line != null) {
				
				int encodingIndex = line.indexOf("encoding");
				
				// look for the encoding attribute
				if (encodingIndex != -1) {
					
					// we look in the same line first
					boolean sameLine = true;
					
					boolean doubleQuoteFound = false;
					boolean singleQuoteFound = false;
					
					while (line != null) {
						
						// now look for the begin quote, which does not have to be
						// on the same line as the encoding attribute declaration
						int beginQuote = -1;
						
						// search in same line first
						if (sameLine) {
							
							// look for double quote
							beginQuote = line.indexOf('\"', encodingIndex+9);
							
							// if double quote not found, then try single quote
							if (beginQuote == -1) {
								beginQuote = line.indexOf('\'', encodingIndex+9);
								
								// single quote found, so flag it
								if (beginQuote != -1) {
									singleQuoteFound = true;
								}
							}
							// double quote found, so flag it
							else {
								doubleQuoteFound = true;
							}
						}
						// search in another line now
						else {
							
							// look for double quote
							beginQuote = line.indexOf('\"');
							
							// if single quote not found, then try single quote 
							if (beginQuote == -1) {
								beginQuote = line.indexOf('\'');
								
								// single quote found, so flag it
								if (beginQuote != -1) {
									singleQuoteFound = true;
								}
							}
							// double quote found, so flag it
							else {
								doubleQuoteFound = true;
							}
						}
					
						// if begin quote found, look for endquote which should be on the same line
						if (beginQuote != -1) {
							int endQuote = -1;
							
							if (doubleQuoteFound) {
								endQuote = line.indexOf('\"', beginQuote+1);
							}
							else if (singleQuoteFound){
								endQuote = line.indexOf('\'', beginQuote+1);
							}
								
							// if end quote found, encoding is in between begin quote and end quote
							if (endQuote != -1) {
								encoding = line.substring(beginQuote+1, endQuote);
							}
							
							break;
						}
						
						line = bufReader.readLine();
						
						if (sameLine) {
							sameLine = false;
						}
					}
				}
				
				line = bufReader.readLine();
			}
			
			// if the encoding declaration was not found
			if (encoding == null) {
				
				// check if our initial guess was UTF-8 or UTF-16
				// those do not have to have an encoding declaration
				if (encodingGuess.equals(SystemEncodingUtil.ENCODING_UTF_8) || encodingGuess.startsWith("UnicodeBig") ||
					encodingGuess.equals("UnicodeLittle")) {
					encoding = encodingGuess;
				}
			}
		}
		
		return encoding;
	}
	
	/**
	 * Gets the encoding from the Byte Order Mark (BOM).
	 * @param filePath the file path.
	 * @return the encoding, or <code>null</code> if there is no BOM.
	 */
	public String getEncodingFromBOM(String filePath) throws IOException {
		FileInputStream stream = new FileInputStream(filePath);
		
		String encoding = getEncodingFromBOM(stream);
		
		stream.close();
		
		return encoding;
	}
	
	/**
	 * Gets the encoding from the Byte Order Mark (BOM).
	 * @param filePath the file path.
	 * @return the encoding, or <code>null</code> if there is no BOM.
	 */
	private String getEncodingFromBOM(InputStream stream) throws IOException {
		
		byte[] bomBytes = new byte[4];
		
		// read the first three bytes
		stream.read(bomBytes, 0, 3);
		
		// check if UTF-8 BOM
		if (bomBytes[0] == 0xEF && bomBytes[1] == 0xBB && bomBytes[2] == 0xBF) {
			return SystemEncodingUtil.ENCODING_UTF_8;
		}
		
		// now read the fourth byte
		stream.read(bomBytes, 3, 1);
		
		// check if it matches some other encoding BOM
		
		// UCS-4, big-endian order (1234 order)
		if (bomBytes[0] == 0x00 && bomBytes[1] == 0x00 && bomBytes[2] == 0xFE && bomBytes[3] == 0xFF) {
			return null;
		}
		// UCS-4, little-endian order (4321 order)
		else if (bomBytes[0] == 0xFF && bomBytes[1] == 0xFE && bomBytes[2] == 0x00 && bomBytes[3] == 0x00) {
			return null;
		}
		// UCS-4, unusual octet order (2143)
		else if (bomBytes[0] == 0x00 && bomBytes[1] == 0x00 && bomBytes[2] == 0xFF && bomBytes[3] == 0xFE) {
			return null;
		}
		// UCS-4, unusual octet order (3412)
		else if (bomBytes[0] == 0xFE && bomBytes[1] == 0xFF && bomBytes[2] == 0x00 && bomBytes[3] == 0x00) {
			return null;
		}
		// UTF-16, big-endian order
		else if (bomBytes[0] == 0xFE && bomBytes[1] == 0xFF && !(bomBytes[2] == 0x00 && bomBytes[3] == 0x00)) {
			return "UnicodeBig";
		}
		// UTF-16, little-endian order
		else if (bomBytes[0] == 0xFF && bomBytes[1] == 0xFE && !(bomBytes[2] == 0x00 && bomBytes[3] == 0x00)) {
			return "UnicodeLittle";
		}
		// not a BOM
		else {
			return null;
		}
	}
}