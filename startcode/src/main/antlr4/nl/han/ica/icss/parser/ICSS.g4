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
OPEN_BRACKET: '[';
CLOSE_BRACKET: ']';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: ---
stylesheet:
    (
    variableAssignment
    | stylerule
    ) *
    ;

/*
    CSS selectors are:
    - First character lowercase
    - ID starts with #
    - Class starts with .
*/
selector
    : LOWER_IDENT
    | ID_IDENT
    | CLASS_IDENT
    ;

/*
    Stylerules start with { and end with }
    A stylerule can contain multiple declarations and clauses
*/
stylerule: selector OPEN_BRACE body CLOSE_BRACE;

/*
    If clause can contain declarations and new clauses
*/
if_clause
    : 'if' OPEN_BRACKET expression CLOSE_BRACKET OPEN_BRACE if_body CLOSE_BRACE
    | 'if' OPEN_BRACKET expression CLOSE_BRACKET OPEN_BRACE if_body CLOSE_BRACE else_clause;

/*
    Else clause can contain declarations and new clauses
*/
else_clause: 'else' OPEN_BRACE else_body CLOSE_BRACE;

/*
    Allow a body to contain multiple clauses and declarations in any order
*/
body:
    ( declaration
    | variableAssignment
    | if_clause
    ) *;

/*
    If clause bodies can contain everything except stylerules
*/
if_body:
    ( declaration
    | if_clause
    | variableAssignment
    ) *;

/*
    Else clauses can contain everything except stylerules and other clauses
*/
else_body:
    ( declaration
    | variableAssignment
    ) *;

/*
    Are used in if clauses
*/
expression
    : variableReference
    | TRUE
    | FALSE
    ;

/*
    Variables can only start with a capital letter
*/
variableReference: CAPITAL_IDENT;

/*
    Variables can be inialized with these values
*/
variableValue
    : TRUE
    | FALSE
    | value
    | calculation
    ;

/*
    Variables can be initialized with calculations
    Value := 10px + 1px * 1;
*/
variableAssignment: CAPITAL_IDENT ASSIGNMENT_OPERATOR (variableValue | variableReference) SEMICOLON;

/*
    Declarations like
    color: Blue;
*/
declaration: property COLON declarationValue SEMICOLON;

/*
    Declaration values including calculations
*/
declarationValue
    : calculation
    | variableReference
    | value
    ;

/*
    Operators to calculate with
*/
operator: MUL | PLUS | MIN;

/*
    Properties can only start with a lower case letter
*/
property: LOWER_IDENT;

/*
    Standard values
*/
value
    : PIXELSIZE
    | PERCENTAGE
    | COLOR
    ;

/*
    Calculations for pixels and percentages
    Scalar can be used for multiplying
*/
calculation: pixelCalculation | percentageCalculation;

/*
    Calculate pixels
    Pixel := 10 * 10px + 1 * 1px + 1px * 1 + 10px;
*/
pixelCalculation:
    pixel PLUS pixel
    | pixel PLUS pixelCalculation
    | pixelCalculation PLUS pixel
    | pixelCalculation PLUS pixelCalculation

    | pixel MIN pixel
    | pixel MIN pixelCalculation
    | pixelCalculation MIN pixel
    | pixelCalculation MIN pixelCalculation

    | pixel MUL scalar
    | scalar MUL pixel
    | pixelCalculation MUL scalar
    | scalar MUL pixelCalculation

    | variableReference PLUS pixelCalculation
    | pixelCalculation PLUS variableReference
    | pixel PLUS variableReference
    | variableReference PLUS pixel

    | variableReference MUL pixelCalculation
    | pixelCalculation MUL variableReference
    | pixel MUL variableReference
    | variableReference MUL  pixel

    | variableReference MIN pixelCalculation
    | pixelCalculation MIN variableReference
    | pixel MIN variableReference
    | variableReference MIN pixel
    ;

/*
    Calculate percentage
    Percent := 10 * 10% + 10% - 10% * 10;
*/
percentageCalculation:
    percent PLUS percent
    | percent PLUS percentageCalculation
    | percentageCalculation PLUS percent
    | percentageCalculation PLUS percentageCalculation

    | percent MIN percent
    | percent MIN percentageCalculation
    | percentageCalculation MIN percent
    | percentageCalculation MIN percentageCalculation

    | percent MUL scalar
    | scalar MUL percent
    | percentageCalculation MUL scalar
    | scalar MUL percentageCalculation

    | variableReference PLUS percentageCalculation
    | percentageCalculation PLUS variableReference
    | variableReference PLUS percent
    | percent PLUS variableReference

    | variableReference MUL percentageCalculation
    | percentageCalculation MUL variableReference
    | variableReference MUL percent
    | percent MUL variableReference

    | variableReference MIN percentageCalculation
    | percentageCalculation MIN variableReference
    | variableReference MIN percent
    | percent MIN variableReference
    ;

    scalar: SCALAR;
    pixel: PIXELSIZE;
    percent: PERCENTAGE;