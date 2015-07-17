// ECMA-262 5.1

grammar ECMAScript;

singleExpression
	: Identifier
	;

functionDeclaration
	: Identifier '('
	;

WhiteSpaceSequence
	: WhiteSpace+ -> channel(HIDDEN)
	;

fragment WhiteSpace
	: [\t\u000B\u000C]
	// Unicode cat: Zs
	| '\u0020'
	| '\u00A0'
	| '\u1680'
	| [\u2000-\u200A]
	| '\u202F'
	| '\u205F'
	| '\u3000'
	;

MultiLineComment
	: '/*' .*? '*/' -> channel(HIDDEN)
	;

SingleLineCOmment
	: '//' ~[\r\n\u2028\u2029]* -> channel(HIDDEN)
	;

Identifier
	: IdentifierStart IdentifierPart*
	;

fragment IdentifierStart
	: UnicodeLetter
	| [$_]
	| '\\' UnicodeEscapeSequence
	;

fragment IdentifierPart
	: IdentifierStart
	| UnicodeCombiningMark
	| UnicodeDigit
	| UnicodeConnectorPunctuation
	| [\u200C\u200D]
	;

fragment HexDigit
	: [0-9a-fA-f]
	;

fragment UnicodeEscapeSequence
	: '\\' HexDigit HexDigit HexDigit HexDigit
	;

// Unicode cats: Lu, Ll, Lt, Lm, Lo, Nl
fragment UnicodeLetter
	: [\u0041-\u005A]
	| [\u0061-\u007A]
	| '\u00AA'
	| '\u00B5'
	| '\u00BA'
	| [\u00C0-\u00D6]
	| [\u00D8-\u00F6]
	| [\u00F8-\u02C1]
	| [\u02C6-\u02D1]
	| [\u02E0-\u02E4]
	| '\u02EC'
	| '\u02EE'
	| [\u0370-\u0374]
	| [\u0376-\u037D]
	| '\u037F'
	| '\u0386'
	| [\u0388-\u03F5]
	| [\u03F7-\u0481]
	| [\u048A-\u0559]
	| [\u0561-\u0587]
	| [\u05D0-\u05F2]
	| [\u0620-\u064A]
	| [\u066E-\u066F]
	| [\u0671-\u06D3]
	| '\u06D5'
	| [\u06E5-\u06E6]
	| [\u06EE-\u06EF]
	| [\u06FA-\u06FC]
	| '\u06FF'
	| '\u0710'
	| [\u0712-\u072F]
	| [\u074D-\u07A5]
	| '\u07B1'
	| [\u07CA-\u07EA]
	| [\u07F4-\u07F5]
	| [\u07FA-\u0815]
	| '\u081A'
	| '\u0824'
	| '\u0828'
	| [\u0840-\u0858]
	| [\u08A0-\u08B4]
	| [\u0904-\u0939]
	| '\u093D'
	| '\u0950'
	| [\u0958-\u0961]
	| [\u0971-\u0980]
	| [\u0985-\u09B9]
	| '\u09BD'
	| '\u09CE'
	| [\u09DC-\u09E1]
	| [\u09F0-\u09F1]
	| [\u0A05-\u0A39]
	| [\u0A59-\u0A5E]
	| [\u0A72-\u0A74]
	| [\u0A85-\u0AB9]
	| '\u0ABD'
	| [\u0AD0-\u0AE1]
	| '\u0AF9'
	| [\u0B05-\u0B39]
	| '\u0B3D'
	| [\u0B5C-\u0B61]
	| '\u0B71'
	| [\u0B83-\u0BB9]
	| '\u0BD0'
	| [\u0C05-\u0C3D]
	| [\u0C58-\u0C61]
	| [\u0C85-\u0CB9]
	| '\u0CBD'
	| [\u0CDE-\u0CE1]
	| [\u0CF1-\u0CF2]
	| [\u0D05-\u0D3D]
	| '\u0D4E'
	| [\u0D5F-\u0D61]
	| [\u0D7A-\u0D7F]
	| [\u0D85-\u0DC6]
	| [\u0E01-\u0E30]
	| [\u0E32-\u0E33]
	| [\u0E40-\u0E46]
	| [\u0E81-\u0EB0]
	| [\u0EB2-\u0EB3]
	| [\u0EBD-\u0EC6]
	| [\u0EDC-\u0F00]
	| [\u0F40-\u0F6C]
	| [\u0F88-\u0F8C]
	| [\u1000-\u102A]
	| '\u103F'
	| [\u1050-\u1055]
	| [\u105A-\u105D]
	| '\u1061'
	| [\u1065-\u1066]
	| [\u106E-\u1070]
	| [\u1075-\u1081]
	| '\u108E'
	| [\u10A0-\u10FA]
	| [\u10FC-\u135A]
	| [\u1380-\u138F]
	| [\u13A0-\u13FD]
	| [\u1401-\u166C]
	| [\u166F-\u167F]
	| [\u1681-\u169A]
	| [\u16A0-\u16EA]
	| [\u16EE-\u1711]
	| [\u1720-\u1731]
	| [\u1740-\u1751]
	| [\u1760-\u1770]
	| [\u1780-\u17B3]
	| '\u17D7'
	| '\u17DC'
	| [\u1820-\u18A8]
	| [\u18AA-\u191E]
	| [\u1950-\u19C9]
	| [\u1A00-\u1A16]
	| [\u1A20-\u1A54]
	| '\u1AA7'
	| [\u1B05-\u1B33]
	| [\u1B45-\u1B4B]
	| [\u1B83-\u1BA0]
	| [\u1BAE-\u1BAF]
	| [\u1BBA-\u1BE5]
	| [\u1C00-\u1C23]
	| [\u1C4D-\u1C4F]
	| [\u1C5A-\u1C7D]
	| [\u1CE9-\u1CEC]
	| [\u1CEE-\u1CF1]
	| [\u1CF5-\u1CF6]
	| [\u1D00-\u1DBF]
	| [\u1E00-\u1FBC]
	| '\u1FBE'
	| [\u1FC2-\u1FCC]
	| [\u1FD0-\u1FDB]
	| [\u1FE0-\u1FEC]
	| [\u1FF2-\u1FFC]
	| '\u2071'
	| '\u207F'
	| [\u2090-\u209C]
	| '\u2102'
	| '\u2107'
	| [\u210A-\u2113]
	| '\u2115'
	| [\u2119-\u211D]
	| '\u2124'
	| '\u2126'
	| '\u2128'
	| [\u212A-\u212D]
	| [\u212F-\u2139]
	| [\u213C-\u213F]
	| [\u2145-\u2149]
	| '\u214E'
	| [\u2160-\u2188]
	| [\u2C00-\u2CE4]
	| [\u2CEB-\u2CEE]
	| [\u2CF2-\u2CF3]
	| [\u2D00-\u2D6F]
	| [\u2D80-\u2DDE]
	| '\u2E2F'
	| [\u3005-\u3007]
	| [\u3021-\u3029]
	| [\u3031-\u3035]
	| [\u3038-\u303C]
	| [\u3041-\u3096]
	| [\u309D-\u309F]
	| [\u30A1-\u30FA]
	| [\u30FC-\u318E]
	| [\u31A0-\u31BA]
	| [\u31F0-\u31FF]
	| [\u3400-\u4DB5]
	| [\u4E00-\uA48C]
	| [\uA4D0-\uA4FD]
	| [\uA500-\uA60C]
	| [\uA610-\uA61F]
	| [\uA62A-\uA66E]
	| [\uA67F-\uA69D]
	| [\uA6A0-\uA6EF]
	| [\uA717-\uA71F]
	| [\uA722-\uA788]
	| [\uA78B-\uA801]
	| [\uA803-\uA805]
	| [\uA807-\uA80A]
	| [\uA80C-\uA822]
	| [\uA840-\uA873]
	| [\uA882-\uA8B3]
	| [\uA8F2-\uA8F7]
	| '\uA8FB'
	| '\uA8FD'
	| [\uA90A-\uA925]
	| [\uA930-\uA946]
	| [\uA960-\uA97C]
	| [\uA984-\uA9B2]
	| '\uA9CF'
	| [\uA9E0-\uA9E4]
	| [\uA9E6-\uA9EF]
	| [\uA9FA-\uAA28]
	| [\uAA40-\uAA42]
	| [\uAA44-\uAA4B]
	| [\uAA60-\uAA76]
	| '\uAA7A'
	| [\uAA7E-\uAAAF]
	| '\uAAB1'
	| [\uAAB5-\uAAB6]
	| [\uAAB9-\uAABD]
	| '\uAAC0'
	| [\uAAC2-\uAADD]
	| [\uAAE0-\uAAEA]
	| [\uAAF2-\uAAF4]
	| [\uAB01-\uAB5A]
	| [\uAB5C-\uABE2]
	| [\uAC00-\uD7FB]
	| [\uF900-\uFB1D]
	| [\uFB1F-\uFB28]
	| [\uFB2A-\uFBB1]
	| [\uFBD3-\uFD3D]
	| [\uFD50-\uFDFB]
	| [\uFE70-\uFEFC]
	| [\uFF21-\uFF3A]
	| [\uFF41-\uFF5A]
	| [\uFF66-\uFFDC]
	;

// Unicode cats: Mn, Mc
UnicodeCombiningMark
	: [\u0300-\u036F]
	| [\u0483-\u0487]
	| [\u0591-\u05BD]
	| '\u05BF'
	| [\u05C1-\u05C2]
	| [\u05C4-\u05C5]
	| '\u05C7'
	| [\u0610-\u061A]
	| [\u064B-\u065F]
	| '\u0670'
	| [\u06D6-\u06DC]
	| [\u06DF-\u06E4]
	| [\u06E7-\u06E8]
	| [\u06EA-\u06ED]
	| '\u0711'
	| [\u0730-\u074A]
	| [\u07A6-\u07B0]
	| [\u07EB-\u07F3]
	| [\u0816-\u0819]
	| [\u081B-\u0823]
	| [\u0825-\u0827]
	| [\u0829-\u082D]
	| [\u0859-\u085B]
	| [\u08E3-\u0903]
	| [\u093A-\u093C]
	| [\u093E-\u094F]
	| [\u0951-\u0957]
	| [\u0962-\u0963]
	| [\u0981-\u0983]
	| '\u09BC'
	| [\u09BE-\u09CD]
	| '\u09D7'
	| [\u09E2-\u09E3]
	| [\u0A01-\u0A03]
	| [\u0A3C-\u0A51]
	| [\u0A70-\u0A71]
	| [\u0A75-\u0A83]
	| '\u0ABC'
	| [\u0ABE-\u0ACD]
	| [\u0AE2-\u0AE3]
	| [\u0B01-\u0B03]
	| '\u0B3C'
	| [\u0B3E-\u0B57]
	| [\u0B62-\u0B63]
	| '\u0B82'
	| [\u0BBE-\u0BCD]
	| '\u0BD7'
	| [\u0C00-\u0C03]
	| [\u0C3E-\u0C56]
	| [\u0C62-\u0C63]
	| [\u0C81-\u0C83]
	| '\u0CBC'
	| [\u0CBE-\u0CD6]
	| [\u0CE2-\u0CE3]
	| [\u0D01-\u0D03]
	| [\u0D3E-\u0D4D]
	| '\u0D57'
	| [\u0D62-\u0D63]
	| [\u0D82-\u0D83]
	| [\u0DCA-\u0DDF]
	| [\u0DF2-\u0DF3]
	| '\u0E31'
	| [\u0E34-\u0E3A]
	| [\u0E47-\u0E4E]
	| '\u0EB1'
	| [\u0EB4-\u0EBC]
	| [\u0EC8-\u0ECD]
	| [\u0F18-\u0F19]
	| '\u0F35'
	| '\u0F37'
	| '\u0F39'
	| [\u0F3E-\u0F3F]
	| [\u0F71-\u0F84]
	| [\u0F86-\u0F87]
	| [\u0F8D-\u0FBC]
	| '\u0FC6'
	| [\u102B-\u103E]
	| [\u1056-\u1059]
	| [\u105E-\u1060]
	| [\u1062-\u1064]
	| [\u1067-\u106D]
	| [\u1071-\u1074]
	| [\u1082-\u108D]
	| '\u108F'
	| [\u109A-\u109D]
	| [\u135D-\u135F]
	| [\u1712-\u1714]
	| [\u1732-\u1734]
	| [\u1752-\u1753]
	| [\u1772-\u1773]
	| [\u17B4-\u17D3]
	| '\u17DD'
	| [\u180B-\u180D]
	| '\u18A9'
	| [\u1920-\u193B]
	| [\u1A17-\u1A1B]
	| [\u1A55-\u1A7F]
	| [\u1AB0-\u1ABD]
	| [\u1B00-\u1B04]
	| [\u1B34-\u1B44]
	| [\u1B6B-\u1B73]
	| [\u1B80-\u1B82]
	| [\u1BA1-\u1BAD]
	| [\u1BE6-\u1BF3]
	| [\u1C24-\u1C37]
	| [\u1CD0-\u1CD2]
	| [\u1CD4-\u1CE8]
	| '\u1CED'
	| [\u1CF2-\u1CF4]
	| [\u1CF8-\u1CF9]
	| [\u1DC0-\u1DFF]
	| [\u20D0-\u20DC]
	| '\u20E1'
	| [\u20E5-\u20F0]
	| [\u2CEF-\u2CF1]
	| '\u2D7F'
	| [\u2DE0-\u2DFF]
	| [\u302A-\u302F]
	| [\u3099-\u309A]
	| '\uA66F'
	| [\uA674-\uA67D]
	| [\uA69E-\uA69F]
	| [\uA6F0-\uA6F1]
	| '\uA802'
	| '\uA806'
	| '\uA80B'
	| [\uA823-\uA827]
	| [\uA880-\uA881]
	| [\uA8B4-\uA8C4]
	| [\uA8E0-\uA8F1]
	| [\uA926-\uA92D]
	| [\uA947-\uA953]
	| [\uA980-\uA983]
	| [\uA9B3-\uA9C0]
	| '\uA9E5'
	| [\uAA29-\uAA36]
	| '\uAA43'
	| [\uAA4C-\uAA4D]
	| [\uAA7B-\uAA7D]
	| '\uAAB0'
	| [\uAAB2-\uAAB4]
	| [\uAAB7-\uAAB8]
	| [\uAABE-\uAABF]
	| '\uAAC1'
	| [\uAAEB-\uAAEF]
	| [\uAAF5-\uAAF6]
	| [\uABE3-\uABEA]
	| [\uABEC-\uABED]
	| '\uFB1E'
	| [\uFE00-\uFE0F]
	| [\uFE20-\uFE2F]
	;

UnicodeDigit
	: [\u0030-\u0039]
	| [\u0660-\u0669]
	| [\u06F0-\u06F9]
	| [\u07C0-\u07C9]
	| [\u0966-\u096F]
	| [\u09E6-\u09EF]
	| [\u0A66-\u0A6F]
	| [\u0AE6-\u0AEF]
	| [\u0B66-\u0B6F]
	| [\u0BE6-\u0BEF]
	| [\u0C66-\u0C6F]
	| [\u0CE6-\u0CEF]
	| [\u0D66-\u0D6F]
	| [\u0DE6-\u0DEF]
	| [\u0E50-\u0E59]
	| [\u0ED0-\u0ED9]
	| [\u0F20-\u0F29]
	| [\u1040-\u1049]
	| [\u1090-\u1099]
	| [\u17E0-\u17E9]
	| [\u1810-\u1819]
	| [\u1946-\u194F]
	| [\u19D0-\u19D9]
	| [\u1A80-\u1A99]
	| [\u1B50-\u1B59]
	| [\u1BB0-\u1BB9]
	| [\u1C40-\u1C49]
	| [\u1C50-\u1C59]
	| [\uA620-\uA629]
	| [\uA8D0-\uA8D9]
	| [\uA900-\uA909]
	| [\uA9D0-\uA9D9]
	| [\uA9F0-\uA9F9]
	| [\uAA50-\uAA59]
	| [\uABF0-\uABF9]
	| [\uFF10-\uFF19]
	;

UnicodeConnectorPunctuation
	: '\u005F'
	| [\u203F-\u2040]
	| '\u2054'
	| [\uFE33-\uFE34]
	| [\uFE4D-\uFE4F]
	| '\uFF3F'
	;
