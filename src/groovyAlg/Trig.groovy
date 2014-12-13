package groovyAlg

trait Trig extends UnaryOp{

	String toString(){
		"${getSymbol()}(${f1.toString()})"
	}

	MathObject simplify(){
		this
	}
}
