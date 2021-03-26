grammar ICSS;

//--- LEXER: ---

//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: ---
stylesheet: (variableAssignment | block) * EOF;

selector: LOWER_IDENT | ID_IDENT | CLASS_IDENT;
block: selector OPEN_BRACE declaration * CLOSE_BRACE;
variableAssignment: CAPITAL_IDENT ASSIGNMENT_OPERATOR (TRUE | FALSE | value | calculation) SEMICOLON;
declaration: property COLON (value | variableReference) SEMICOLON;
property: LOWER_IDENT;
value: PIXELSIZE | PERCENTAGE | COLOR;
variableReference: CAPITAL_IDENT;

/*
    Calculations for pixels and percentages
    Scalar can be used for multiplying
*/
calculation: pixelCalculation | percentageCalculation;

/*
    Calculate pixels
    Pixel := 10 * 10px + 1 * 1px + 1px * 1 + 10px;
*/
pixelCalculation
    : PIXELSIZE
    | pixelCalculation MUL SCALAR
    | SCALAR MUL pixelCalculation
    | pixelCalculation PLUS pixelCalculation
    | pixelCalculation MIN pixelCalculation
    ;

/*
    Calculate percentage
    Percent := 10 * 10% + 10% - 10% * 10;
*/
percentageCalculation
    : PERCENTAGE
    | percentageCalculation MUL SCALAR
    | SCALAR MUL percentageCalculation
    | percentageCalculation PLUS percentageCalculation
    | percentageCalculation MIN percentageCalculation
    ;