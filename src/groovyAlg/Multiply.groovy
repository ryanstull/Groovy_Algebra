package groovyAlg

import groovy.transform.InheritConstructors

@InheritConstructors
class Multiply extends MultiOp {

    String symbol = "*"
    Closure<Number> operation = { a, b -> a * b }
    Number identity = 1

    /**
     * @return The derivative of a multiplication of terms
     * For example if there are 3 terms... f(x)g(x)h(x) the derivative is
     * f'(x)g(x)h(x)+f(x)g'(x)h(x)+f(x)g(x)h'(x)
     */
    ArithmeticExpression derivative() {
        new Add(terms.collect { a ->
            new Multiply([a.derivative()] + terms.grep {
                !it.is(a)
            })
        }).simplify()
    }

    /**
     * {@inheritDoc}
     */
    ArithmeticExpression simplify() {
        Multiply rtrn = this.clone()
        def newTerms = rtrn.terms

        for (int i = 0; i < terms.size(); i++) {
            newTerms[i] = terms[i].simplify()
        }
        for (int i = 0; i < newTerms.size(); i++) {
            if(newTerms[i] instanceof Multiply){
                newTerms.addAll(newTerms[i].terms)
                newTerms.remove(i)
            }
        }

        if (terms.contains(new Num(0))) {
            return new Num(0)
        }

        newTerms.retainAll{
            it!=new Num(1)
        }

        for(int i=newTerms.size()-1;i>=0;i--){
            if(newTerms[i] instanceof Num){
                for(int j=i-1;j>=0;j--){
                    if(newTerms[j] instanceof Num){
                        newTerms[j] = new Num(newTerms[j].num*newTerms[i].num)
                        newTerms.remove(i)
                    }
                }
            }
        }

        if (rtrn.terms.size()==1){
            return rtrn.terms[0]
        }

        def map = new HashMap<ArithmeticExpression, List<ArithmeticExpression>>()
        for(it in newTerms){
            def func
            if (it instanceof Exponent) {
                func = it.terms[0]
                if (!map.containsKey(func)) {
                    map.put(func, [])
                }
                map[func] << it.terms[1]
            } else {
                func = it
                if (!map.containsKey(func)) {
                    map.put(func, [])
                }
                map[func] << new Num(1)
            }
        }

        def terms2 = []
        map.each { k, v ->
            if (v.size()==1 && v[0]==new Num(1)){
                terms2 << k
            }else{
                terms2 << new Exponent(k, new Add(v).simplify())
            }

        }

        rtrn.terms = terms2
        newTerms = rtrn.terms

        for (int i = 0; i < newTerms.size(); i++) {
            newTerms[i] = newTerms[i].simplify()
        }

        if (rtrn.terms.size()==1){
            return rtrn.terms[0]
        }

        rtrn.sort()

        return rtrn
    }

    String toString() {
        terms.collect { it.toString() }.join()
    }
}