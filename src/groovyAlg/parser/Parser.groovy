package groovyAlg.parser

import groovyAlg.*

/**
 * This class parses an expression
 *
 * A grammar which could define the arithmetic expression used in this library is
 *
 * ex1  ->  <ex2>([+-]<ex2>)*
 * ex2  ->  <ex3>([*\/]<ex3>)*
 * ex3  ->  <ex4>([^]<ex4>)
 * ex4  ->  <num> | <var> | <trig>(<ex1>) | log(<ex1>) | (<ex1>)
 * trig ->  sin | cos | tan | csc | sec | cot
 * num  ->  [0-9]*
 * var  ->  x
 *
 * Not that only ex1,ex2,ex3,and ex4 are needed to parse the expression and num,var and trig are combined into ex4
 *
 * @author  Ryan Stull <rstull1200@gmail.com>
 * @since   2015-01-08
 * @version 1.0
 */
class Parser {
    Lex_Analyzer lexer;


    private Parser(String formula) {
        lexer = new Lex_Analyzer(formula)
    }

    /**
     * Returns an arithmetic expression that the given string represents
     *
     * @param formula A string representation of the formula
     * @return An arithmetic expression representing the given formula
     */
    static ArithmeticExpression parse(String formula) {
        Parser p = new Parser(formula)
        p.lexer.lex()
        def rtrn = p.ex1().simplify()
        return rtrn
    }

    private ArithmeticExpression ex1() {
        List<ArithmeticExpression> f = []

        f << ex2()

        while (lexer.token == TOKEN_TYPE.ADD || lexer.token == TOKEN_TYPE.SUB) {
            switch (lexer.token) {
                case TOKEN_TYPE.ADD:
                    lexer.lex()
                    f << ex2()
                    break

                case TOKEN_TYPE.SUB:
                    lexer.lex()
                    f << ex2().negative()
                    break
            }
        }

        return new Add(f).simplify()
    }

    private ArithmeticExpression ex2() {
        List<ArithmeticExpression> f = []

        f << ex3()

        if (lexer.token == TOKEN_TYPE.MULT) {
            while (lexer.token == TOKEN_TYPE.MULT) {
                lexer.lex()
                f << ex3()
            }
            return new Multiply(f)
        }


        if (lexer.token == TOKEN_TYPE.DIV) {
            lexer.lex()
            f << ex3()
            return new Divide(f[0], f[1])
        }

        return f[0]
    }

    private ArithmeticExpression ex3() {
        ArithmeticExpression f1, f2

        f1 = ex4()

        if (lexer.token == TOKEN_TYPE.POW) {
            lexer.lex()
            f2 = ex4()
            return new Exponent(f1, f2)
        }

        return f1
    }

    private ArithmeticExpression ex4() {
        ArithmeticExpression f1, f2

        switch (lexer.token) {
            case TOKEN_TYPE.NUM:
                f1 = new Num(lexer.lexeme)
                lexer.lex()
                break

            case TOKEN_TYPE.SUB:
                lexer.lex()
                f1 = new Num(Integer.valueOf(lexer.lexeme) * -1)
                lexer.lex()
                break

            case TOKEN_TYPE.VAR:
                f1 = new Var()
                lexer.lex()
                break

            case [TOKEN_TYPE.SIN, TOKEN_TYPE.COS, TOKEN_TYPE.TAN, TOKEN_TYPE.CSC, TOKEN_TYPE.SEC, TOKEN_TYPE.COT, TOKEN_TYPE.LOG, 'inlist']:
                Closure constructor;
                switch (lexer.token) {
                    case TOKEN_TYPE.SIN:
                        constructor = Sin.metaClass.&invokeConstructor
                        break
                    case TOKEN_TYPE.COS:
                        constructor = Cos.metaClass.&invokeConstructor
                        break

                    case TOKEN_TYPE.TAN:
                        constructor = Tan.metaClass.&invokeConstructor
                        break

                    case TOKEN_TYPE.CSC:
                        constructor = Csc.metaClass.&invokeConstructor
                        break

                    case TOKEN_TYPE.SEC:
                        constructor = Sec.metaClass.&invokeConstructor
                        break

                    case TOKEN_TYPE.COT:
                        constructor = Cot.metaClass.&invokeConstructor
                        break

                    case TOKEN_TYPE.LOG:
                        constructor = Log.metaClass.&invokeConstructor
                        break

                    default:
                        Lex_Analyzer.error()
                }

                lexer.lex()
                if (lexer.token == TOKEN_TYPE.L_PAREN) {
                    lexer.lex()
                    f2 = ex1()
                    f1 = constructor(f2)
                    if (lexer.token == TOKEN_TYPE.R_PAREN) {
                        lexer.lex()
                    } else {
                        Lex_Analyzer.error()
                    }
                } else {
                    Lex_Analyzer.error()
                }

                break

            case TOKEN_TYPE.L_PAREN:
                lexer.lex()
                f1 = ex1()
                if (lexer.token == TOKEN_TYPE.R_PAREN) {
                    lexer.lex()
                } else {
                    Lex_Analyzer.error()
                }
                break
        }

        return f1
    }
}