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
    (variableAssignment | stylerule)
    | stylesheet
    (variableAssignment | stylerule)
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
    Contains if or if else clause
*/
clause: if_clause | if_clause else_clause;

/*
    If clause can contain declarations and new clauses
*/
if_clause: 'if' OPEN_BRACKET expression CLOSE_BRACKET OPEN_BRACE clause_body CLOSE_BRACE;

/*
    Else clause can contain declarations and new clauses
*/
else_clause: 'else' OPEN_BRACE clause_body CLOSE_BRACE;

/*
    Allow a body to contain multiple clauses and declarations in any order
*/
body:
    ( declaration
    | clause
    ) *;

/*
    Clause bodies can contain other clauses, declarations and variable assignments
*/
clause_body:
    ( declaration
    | clause
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
    Variables can be initialized with calculations
    Value := 10px + 1px * 1;
*/
variableAssignment: CAPITAL_IDENT ASSIGNMENT_OPERATOR variableValue SEMICOLON;

/*
    Variables can be inialized with these values
*/
variableValue
    : TRUE
    | FALSE
    | value
    ;

/*
    Declarations like
    color: Blue;
*/
declaration: property COLON declarationValue SEMICOLON;

/*
    Declaration values including calculations
*/
declarationValue
    : value
    | variableReference
    | variableReference operator calculation
    | calculation operator variableReference
    | calculation operator variableReference operator calculation
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
    A value can be a calculation or a color
*/
value
    : calculation
    | COLOR
    ;

/*
    Variables can only start with a capital letter
*/
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