/*
 * Copyright © 2023, Hablutzel Consulting, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grammar SpwingViewFile;


@parser::header {

/*
 * Copyright © 2023, Hablutzel Consulting, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.text.StringEscapeUtils;

}

@lexer::header{
/*
 * Copyright © 2023, Hablutzel Consulting, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
}

options {
    language = Java;
}


/**
 * Primary entry point for the grammar. See the
 * README_SVWF.md file for more information about SVWF
 * files
 */
svwfFile : statement+;


/**
 * An individual statement.
 */
statement
    :   componentClause
    |   defaultClause
    |   bindClause
    |   colorClause
    |   imageClause
    |   invokeStatement
    |   layoutClause
    ;


defaultClause
    :   'defaults' OBRACE defaultStatement (SEMI defaultStatement)* SEMI? CBRACE
    ;

defaultStatement
    :   classAlias=Identifier OPAREN (fixedOnlyKVPair (COMMA fixedOnlyKVPair)*)? CPAREN
    ;

componentClause
    :   'components' OBRACE componentStatement (SEMI componentStatement)* SEMI? CBRACE
    ;

bindClause
    :   'bind' OBRACE bindStatement (SEMI bindStatement)* SEMI? CBRACE
    ;

colorClause
    :   'colors' OBRACE colorStatement (SEMI colorStatement)* SEMI? CBRACE
    ;

imageClause
    :   'images' OBRACE imageStatement (SEMI imageStatement)* SEMI? CBRACE
    ;

invokeStatement
    :   'invoke' methodName=Identifier (root=rootClause)? SEMI
    ;

componentStatement
    :   componentName=Identifier COLON classAlias=Identifier (OPAREN (kvPair (COMMA kvPair)*)? CPAREN)?
    ;

kvPair
    :   k=Identifier (fixedValue | boundValue)
    ;

fixedOnlyKVPair
    :   k=Identifier fixedValue
    ;

fixedValue
    :   EQUAL v=pairValue
    ;

boundValue
    :   BIND_OP e=String_Literal
    ;

pairValue
    : integer=Integer_Literal
    | string=String_Literal
    | bool=Boolean_Literal
    | floatVal=Float_Literal
    | id=Identifier
    | AT bean=Identifier
    | size=dimension
    | in=inset
    ;

inset
    :   OPAREN top=Integer_Literal COMMA left=Integer_Literal COMMA bottom=Integer_Literal COMMA right=Integer_Literal CPAREN
    ;

dimension
    :   OPAREN width=Integer_Literal COMMA height=Integer_Literal CPAREN
    ;

bindStatement
    :   target=targetClause BIND_OP (rootClause)? expression=String_Literal triggerClause?
    ;

targetClause
    :   single=singleTargetClause
    |   group=groupTargetClause
    ;

singleTargetClause
    :   target=Identifier DOT property=Identifier
    ;

groupTargetClause
    :   (groupName=Identifier COLON)? OBRACE identifierElement (COMMA identifierElement)+ CBRACE DOT property=Identifier
    ;

rootClause
    :   OPAREN
        (   m='model'
        |   b=Identifier
        )? CPAREN
    ;

triggerClause
    :   (OBRACK stringElement (COMMA stringElement)* CBRACK)
    ;

stringElement
    :   element=String_Literal
    ;

colorStatement
    :   name=Identifier colorDefinition
    ;

colorDefinition
    :   OPAREN (intColorSpec | floatColorSpec) CPAREN
    |   bitFieldColorSpec
    ;

intColorSpec
    :   red=Integer_Literal COMMA green=Integer_Literal COMMA blue=Integer_Literal (COMMA alpha=Integer_Literal)?
    ;

floatColorSpec
    :   redf=Float_Literal COMMA greenf=Float_Literal COMMA bluef=Float_Literal (COMMA alphaf=Float_Literal)?
    ;

bitFieldColorSpec
    :   COLON bitField=Integer_Literal (hasAlpha=Boolean_Literal)?
    ;

imageStatement
    :   name=Identifier COLON imageSpec
    ;

imageSpec
    :    resourceName=String_Literal (root=rootClause)?
    |   'url' url=String_Literal
    ;

layoutClause
    :   'layout' OBRACE layoutStatement (SEMI layoutStatement)* SEMI? CBRACE
    ;

layoutStatement
    :   component=Identifier COLON
            (   borderLayoutDescription
            |   boxLayoutDescription
            |   buttonBarLayoutDescription
            |   gridBagLayoutDescription
            |   flowLayoutDescription )
    ;

gridBagLayoutDescription
    :   'gridBagLayout' OPAREN gridBagElementDescription (COMMA gridBagElementDescription)* CPAREN
    ;

gridBagElementDescription
    :   element=identifierElement modifiers=gridModifiers? placement=placementSpec?
    ;

gridModifiers
    :   OPAREN kvPair (COMMA kvPair)* CPAREN
    ;

placementSpec
    :   AT topLeft=Identifier (COLON botRight=Identifier)?
    ;

flowLayoutDescription
    :   'flowLayout' OPAREN identifierElement (COMMA identifierElement)* CPAREN
    ;

identifierElement
    :   element=Identifier
    ;

borderLayoutDescription
    :   'borderLayout' OPAREN borderElement (COMMA borderElement)* CPAREN
    ;

borderElement
    :   direction=('north'|'south'|'east'|'west'|'center') EQUAL identifierElement
    ;

boxLayoutDescription
    :   'boxLayout' orientation=('horizontal'|'vertical')? OPAREN boxElement (COMMA boxElement)* CPAREN
    ;

boxElement
    :   identifierElement
    |   rigidArea='rigidArea' size=dimension
    |   horizontalGlue='horizontalGlue'
    |   verticalGlue='verticalGlue'
    |   filler=OPAREN fillerSpec (COMMA fillerSpec)* CPAREN
    ;

buttonBarLayoutDescription
    :   'buttonBar' OPAREN identifierElement (COMMA identifierElement)* CPAREN
    ;


fillerSpec
    :   name=('minSize'|'maxSize'|'prefSize') size=dimension
    ;

/**
 * Lexer rules are after this point.
 */

OPAREN : '(';
CPAREN : ')';
OBRACE : '{';
CBRACE : '}';
OBRACK : '[';
CBRACK : ']';
COMMA : ',';
DOT : '.';
COLON : ':';
SEMI : ';';
EQUAL : '=';
HASH : '#';
AT : '@';
DASH : '-';
BIND_OP : '=>';

/**
 * Lexical representation for an integer
 * constant. This is largely the same as
 * for programming languages like C or Java,
 * with the following simplifications:
 *
 *     Only decimal and hex values are supported
 *     Underscores are not supported
 */
Integer_Literal
	:	Decimal_Literal
	|   Hex_Literal
	;

Float_Literal
	:	Sign? Digits '.' Digits? ExponentPart? FloatTypeSuffix?
	|	Sign? '.' Digits ExponentPart? FloatTypeSuffix?
	|	Sign? Digits ExponentPart FloatTypeSuffix?
	|	Sign? Digits FloatTypeSuffix
	;

fragment FloatTypeSuffix
    :   [fF]
    ;

 fragment
 ExponentPart
 	:	ExponentIndicator SignedInteger
 	;

 fragment
 ExponentIndicator
 	:	[eE]
 	;

fragment
Decimal_Literal
	:	DecimalNumeral LongSuffix?
	;

fragment
Hex_Literal
    :   HexNumeral LongSuffix?
    ;

fragment
LongSuffix
	:	[lL]
	;

fragment
DecimalNumeral
	:	'0'
	|   SignedDecimalBody
	;

fragment
SignedDecimalBody
    :   Sign? DecimalBody
	;

fragment
DecimalBody
    :   NonZeroDigit (Digits?)
    ;

fragment
Digits
	:	Digit (Digits)?
	;

fragment
Digit
	:	'0'
	|	NonZeroDigit
	;

fragment
NonZeroDigit
	:	[1-9]
	;



fragment
HexNumeral
	:	'0' [xX] HexDigits
	;

fragment
HexDigits
	:	HexDigit (HexDigits)?
	;

fragment
HexDigit
	:	[0-9a-fA-F]
	;


/**
 * Specification of the format for a decimal number -
 * a width (the total number of digits in the number)
 * and a precision (the number of digits right of the
 * decimal point
 */
 fragment
DecimalFormatSpecification
    :   DecimalBody '.' DecimalBody
    ;

fragment
DecimalLiteral
	:	Digits '.' Digits?
	|	'.' Digits
	;

fragment
SignedInteger
	:	Sign? Digits
	;

fragment
Sign
	:	[+-]
	;

Boolean_Literal
	:	'true'
	|	'false'
	;

Character_Literal
	:	'\'' SingleCharacter '\''
	|	'\'' EscapeSequence '\''
	;

fragment
SingleCharacter
	:	~['\\]
	;

String_Literal
	:	'"' StringCharacters? '"'
	;

fragment
StringCharacters
	:	StringCharacter+
	;
fragment
StringCharacter
	:	~["\\]
	|	EscapeSequence
	;

fragment
EscapeSequence
	:	'\\' [btnfr"'\\]
    |   UnicodeEscape // This is not in the spec but prevents having to preprocess the input
	;

fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

Identifier
	:	IdentifierStart IdentifierContinue*
	;

fragment
IdentifierStart
	:	[a-zA-Z$_] // these are the "java letters" below 0x7F
	|	// covers all characters above 0x7F which are not a surrogate
		~[\u0000-\u007F\uD800-\uDBFF]
		{Character.isJavaIdentifierStart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

fragment
IdentifierContinue
	:	[a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
	|	// covers all characters above 0x7F which are not a surrogate
		~[\u0000-\u007F\uD800-\uDBFF]
		{Character.isJavaIdentifierPart(_input.LA(-1))}?
	|	// covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
		[\uD800-\uDBFF] [\uDC00-\uDFFF]
		{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;


//
// Whitespace and comments
//

WS  :  [ \t\r\n\u000C]+ -> skip
    ;

COMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    :   CommentMarker ~[\r\n]* -> skip
    ;

fragment
CommentMarker
    :   '//'
    ;