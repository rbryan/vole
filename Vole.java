import java.io.Reader;
import java.io.Writer;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Scanner;
import java.lang.System;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.math.BigInteger;


abstract class Expression{
	Expression(){}

	boolean isAtom(){
		if(this instanceof Atom)
			return true;
		return false;
	}
	
	static boolean isAtom(Expression e){
		if(e instanceof Atom)
			return true;
		return false;
	}

	boolean isNumber(){
		if(this instanceof NumberVal)
			return true;
		return false;
	}

	static boolean isNumber(Expression e){
		if(e instanceof NumberVal)
			return true;
		return false;
	}

	boolean isSymbol(){
		if(this instanceof SymbolVal)
			return true;
		return false;
	}

	static boolean isSymbol(Expression e){
		if(e instanceof SymbolVal)
			return true;
		return false;
	}

	boolean isBoolean(){
		if(this instanceof BooleanVal)
			return true;
		return false;
	}

	static boolean isBoolean(Expression e){
		if(e instanceof BooleanVal)
			return true;
		return false;
	}

	boolean isPair(){
		if(this instanceof Pair)
			return true;
		return false;
	}

	static boolean isPair(Expression e){
		if(e instanceof Pair)
			return true;
		return false;
	}

	boolean isProcedure(){
		if(this instanceof ProcedureVal)
			return true;
		return false;
	}

	static boolean isProcedure(Expression e){
		if(e instanceof ProcedureVal)
			return true;
		return false;
	}

	boolean isLambda(){
		if(this instanceof Lambda)
			return true;
		return false;
	}

	static boolean isLambda(Expression e){
		if(e instanceof Lambda)
			return true;
		return false;
	}

	boolean isJavaFunction(){
		if(this instanceof JavaFunction)
			return true;
		return false;
	}

	static boolean isJavaFunction(Expression e){
		if(e instanceof JavaFunction)
			return true;
		return false;
	}

	boolean isNil(){
		if(this.isPair()){
			Expression car = ((Pair) this).getCar();
			Expression cdr = ((Pair) this).getCdr();
			if(car == null && cdr == null)
				return true;
		}
		return false;
	}

	static boolean isNil(Expression e){
		if(e.isPair()){
			Expression car = ((Pair) e).getCar();
			Expression cdr = ((Pair) e).getCdr();
			if(car == null && cdr == null)
				return true;
		}
		return false;
	}

	boolean isList(){
		if(this.isPair()){
			Expression cdr = ((Pair) this).getCdr();
			if(cdr instanceof Pair)
				return true;
		}
		return false;
	}

	static boolean isList(Expression e){
		if(e.isPair()){
			Expression cdr = ((Pair) e).getCdr();
			if(cdr instanceof Pair)
				return true;
		}
		return false;
	}

}

abstract class Atom extends Expression{
	Atom(){}
}

class NumberVal extends Atom{
	BigInteger val;
	NumberVal(BigInteger val){
		this.val = val;
	}

	BigInteger getVal(){
		return val;
	}
}

class SymbolVal extends Atom{
	String identifier;

	SymbolVal(String ident){
		identifier = ident;
	}

	String getIdentifier(){
		return identifier;
	}

	@Override
	public boolean equals(Object key){
		if(key instanceof SymbolVal){
			SymbolVal keySym = (SymbolVal) key;
			if(keySym.getIdentifier().equals(this.identifier))
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}
}

class BooleanVal extends Atom{
	boolean val;

	BooleanVal(Boolean val){
		this.val = val;
	}

	boolean getVal(){
		return val;
	}
}

class Pair extends Expression{
	Expression car;
	Expression cdr;

	Pair(Expression car, Expression cdr){
		this.car = car;
		this.cdr = cdr;
	}

	Expression getCar(){
		return car;
	}

	Expression getCdr(){
		return cdr;
	}

	void setCdr(Expression exp){
		cdr = exp;
	}

	void setCar(Expression exp){
		car = exp;
	}

}


//Interface for adding functions written in java
abstract class ProcedureVal extends Atom{
	ProcedureVal(){}
}

//look at using anonymous classes for these
abstract class JavaFunction extends ProcedureVal{
	JavaFunction(){}
	abstract Expression call(Expression args);
}


class Lambda extends ProcedureVal {
	Environment closure;
	SymbolVal arg;
	Expression exp;

	Lambda(Environment closure, SymbolVal arg, Expression exp){
		this.closure = closure;
		this.arg = arg;
		this.exp = exp;
	}
	
	Lambda(Expression exp, Environment env){
		try{
			closure = new Environment(env);
			if(exp.isPair()){
				Expression car = ((Pair) exp).getCar();
				Expression cdr = ((Pair) exp).getCdr();
				if(car.isSymbol()){
					arg = (SymbolVal) car;
				}else if(car.isNil()){
					arg = null;
				}else{
					throw new Exception("Lambda expects the first argument to be a symbol or nil.");
				}
				this.exp = ((Pair)cdr).getCar();
			}else{
				throw new Exception("Lambda arguments in an unexpected form.");
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	Environment getClosure(){
		return closure;
	}

	SymbolVal getArg(){
		return arg;
	}

	Expression getExp(){
		return exp;
	}

	Environment getEvalEnvironment(Expression val, Environment env){
		Environment newEnv = new Environment(env);
		newEnv.concat(closure);
		if(arg != null)
			newEnv.add(arg,val);
		return newEnv;
	}


}


class Environment{
	Map<SymbolVal,Expression> map;

	Environment(){
		this.map = new HashMap<SymbolVal,Expression>();
	}

	Environment(Environment e){
		this.map = new HashMap<SymbolVal,Expression>(e.getMap());
	}

	Map<SymbolVal,Expression> getMap(){
		return map;
	}

	void add(SymbolVal key, Expression value){
		this.map.put(key,value);
	}

	void concat(Environment env){
		this.map.putAll(env.getMap());
	}

	Expression lookUp(SymbolVal val){
		Expression result = map.get(val);
		if(result == null)
			System.out.println(val.getIdentifier() + " is undefined.");

		return result;
	}

}

class Evaluator{
	Environment env;

	Evaluator(){
		env = new Environment();
	}

	Evaluator(Environment def){
		this.env = def;
	}

	Expression apply(Expression fn, Expression args){
		try{
			Parser printer = new Parser();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
			System.out.print("apply() called on:\t");
			printer.printExpression(new Pair(fn,args),writer);
			try{
				writer.flush();
			}catch(Exception e){
				e.printStackTrace();
			}
			System.out.println();

			if(fn.isAtom()){
				if(fn.isSymbol())
					return apply(env.lookUp((SymbolVal)fn),args);
				else if(fn.isLambda()){
					Lambda lambda = (Lambda) fn;
					Environment oldEnvironment = env;
					env = lambda.getEvalEnvironment(((Pair)args).getCar(),env);
					Expression result = eval(lambda.getExp());
					env = oldEnvironment;
					return result;
				}else if(fn.isJavaFunction()){
					System.out.println("Apply called a javafunction.");
					JavaFunction jfunc = (JavaFunction) fn;
					return jfunc.call(args);
				}else{
					throw new Exception("Apply can't apply that type.");
				}

			}else{
				return apply(eval(fn),args);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	Expression eval(Expression exp){
		return eval(exp,env);
	}


	Expression eval(Expression exp, Environment env){
		try{
			System.out.println(env.getMap());
			Parser printer = new Parser();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
			System.out.print("eval() called on:\t");
			printer.printExpression(exp,writer);
			try{
				writer.flush();
			}catch(Exception e){
				e.printStackTrace();
			}
			System.out.println();
			
			if(exp.isAtom()){
				if(exp.isSymbol()){
					System.out.println("Looked up " + ((SymbolVal) exp).getIdentifier());
					return env.lookUp((SymbolVal) exp);
				}else{
					return exp;
				}
			}else if(exp.isList()){
				Pair expList = (Pair) exp;
				Expression car = expList.getCar();
				Expression cdr = expList.getCdr();
				if(car.isAtom()){
					if(car instanceof SymbolVal){
						SymbolVal sym = (SymbolVal) car;
						if(sym.getIdentifier().equals("lambda"))
							return new Lambda(cdr,env);
					}
					return apply(car,evlis(cdr));
				}else{
					return apply(eval(car),evlis(cdr));
					
				}


			}

		}catch(Exception e){
			e.printStackTrace();
		}

		System.out.println("Eval couldn't figure out what to do.");

		return null;

	}

	Expression evlis(Expression list){
		if(list.isAtom())
			return list;
		else{
			Pair listPair = (Pair) list;
			if(listPair.isNil())
				return listPair;
			else
				return new Pair(eval(listPair.getCar()),evlis(listPair.getCdr()));
		}
	}

	static boolean eq(Expression a, Expression b){
		if(	a instanceof NumberVal &&
			b instanceof NumberVal){
			if(	((NumberVal) a).getVal().equals(((NumberVal) b).getVal()))
				return true;
			else
				return false;
		}
		if(	a instanceof SymbolVal &&
			b instanceof SymbolVal){
			if(((SymbolVal) a).getIdentifier().equals(((SymbolVal) b).getIdentifier()))
				return true;
			else
				return false;
		}
		if(	a instanceof BooleanVal &&
			b instanceof BooleanVal){
			if(((BooleanVal) a).getVal() == ((BooleanVal) b).getVal())
				return true;
			else
				return false;
		}
		if(	a instanceof Pair &&
			b instanceof Pair){
			if(	((Pair) a).isNil() &&
				((Pair) b).isNil())
				return true;
			else
				return false;

		}
		return false;

	}


}

class Parser{
	Reader input;

	Parser(){
		this.input = new BufferedReader(new InputStreamReader(System.in));
	}

	Parser(Reader input){
		this.input = input;
	}

	void setInput(Reader input){
		this.input = input;
	}

	Reader getInput(){
		return input;
	}

	int peek(){
		int c = 0;
		try{
			input.mark(1);
			c = (char) input.read();
			input.reset();
		}catch(Exception e){
			e.printStackTrace();
		}
		return c;
	}

	static void printExpression(Expression expr, Writer out){
		try{
			if(expr instanceof Pair){
				Pair p = (Pair) expr;
				if(p.isNil()){
					out.write("nil");
				}else{
					out.write("(");
					if(p.getCar() != null){
						printExpression(p.getCar(),out);
						out.write(" ");
					}
					out.write(" . ");
					if(p.getCdr() != null)
						printExpression(p.getCdr(),out);
					out.write(")");
				}
				return;
			}
			if(expr instanceof BooleanVal){
				BooleanVal val = (BooleanVal) expr;
				if(val.getVal())
					out.write("#t");
				else
					out.write("#f");
				return;
			}
			if(expr instanceof NumberVal){
				NumberVal val = (NumberVal) expr;
				out.write(val.getVal().toString());
				return;
			}
			if(expr instanceof SymbolVal){
				SymbolVal val = (SymbolVal) expr;
				out.write(val.getIdentifier());
				return;
			}
			if(expr instanceof Lambda){
				out.write("<Lambda Proc>");
				return;
			}
			if(expr instanceof JavaFunction){
				out.write("<JavaFunction Proc>");
				return;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	BooleanVal parseBoolean(){
		try{
			input.read();
			int c = input.read();
			if(c == 't')
				return new BooleanVal(true);
			else if(c == 'f')
				return new BooleanVal(false);
			else
				throw new Exception("Attempted to parse boolean but no boolean to be found.");
		}catch(Exception e){
			e.printStackTrace();
		}

		return null;
	}

	NumberVal parseNumber(){
		StringBuilder builder = new StringBuilder();
		int c = 0;

		try{
			c = peek();
			while( 	c != '#' &&
				c != '(' &&
				c != ')' &&
				c != ';' &&
			(short) c != -1  &&
				Character.isDigit(c) &&
				!Character.isWhitespace(c)){
				
				builder.append((char) c);
				input.read();
				c = peek();
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		Scanner s = new Scanner(builder.toString());

		return new NumberVal(s.nextBigInteger());
	}

	SymbolVal parseSymbol(){
		StringBuilder builder = new StringBuilder();
		int c = 0;

		try{
			c = peek();
			while( 	c != '#' &&
				c != '(' &&
				c != ')' &&
				c != ';' &&
			(short) c != -1  &&
				!Character.isWhitespace(c)){
				
				builder.append((char) c);
				input.read();
				c = peek();
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return new SymbolVal(builder.toString());

	}

	void parseComment(){
		Scanner scanner = new Scanner(input);
		scanner.useDelimiter("[\n]");
		String s = scanner.next(";.*");
		return;
	}

	Pair parseList(){
		int c;
		Pair head = null;
		Pair tail = null;

		try{
			c = peek();

			if(c == '('){
				//eat the '('
				input.read();
				
				c = peek();

				if((short) c == -1)
					throw new Exception("Unmatched '(' in file.");

				while((short) c != ')'){

					Expression exp = null;

					if(c == '(')
						exp = parseList();

					else if(c == '#')
						exp = parseBoolean();

					else if(c == ';'){
						parseComment();
						c = peek();
						continue;
					}
					
					else if(Character.isDigit(c))
						exp = parseNumber();

					else if(Character.isWhitespace(c)){
						input.read();
						c = peek();
						continue;
					}

					else if((short) c == -1)
						throw new Exception("Unexpected EOF.");

					//If it's none of those things it must be a symbol
					else 
						exp = parseSymbol();
					
					if(head == null){
						head = new Pair(exp,null);
						tail = head;
					}else{
						Pair newTail = new Pair(exp,null);
						tail.setCdr(newTail);
						tail = newTail;
					}

					c = peek();

				}

				//eat the ')'
				input.read();
				

			}else{
				throw new Exception("parseList() expected to start on a '('.");
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		if(head != null)
			tail.setCdr(new Pair(null,null));
		else
			head = new Pair(null,null);

		return head;

	}

	Expression parseSexp(){

		int c;

		try{
			c = peek();

			while((short) c != -1){
				if(Character.isWhitespace(c)){
					input.read();
					c = peek();
					continue;
				}

				else if(c == '#'){
					return parseBoolean();
				}

				else if(Character.isDigit(c)){
					return parseNumber();
				}
				
				else if(c == '('){
					return parseList();
				}

				else if(c == ';'){
					parseComment();
					c = peek();
					continue;
				}
				else if(c == ')'){
					throw new Exception("Unmatched ')' found.");
				}

				else
					return parseSymbol();
				

			}
		}catch(Exception e){
			e.printStackTrace();

		}

		return null;
	}


}


class Cons extends JavaFunction{
	public Cons(){}

	public Expression call(Expression exp){
		Pair list = (Pair) exp;
		Expression a = list.getCar();
		Expression b = ((Pair)list.getCdr()).getCar();
		return new Pair(a,b);
	}
}

class Core{
	Core(){}

	static Environment getEnv(){
		Environment env = new Environment();

		JavaFunction quote = new JavaFunction(){
			Expression call(Expression exp){
				return exp;
			}
		};

		env.add(new SymbolVal("quote"), quote);

		return env;
	}

}

class MathLib{

	MathLib(){}

	static Environment getEnv(){

		Environment env = new Environment();

		JavaFunction add = new JavaFunction(){
			Expression call(Expression exp){
				Pair expPair = (Pair) exp;
				NumberVal a = (NumberVal) (expPair.getCar());
				NumberVal b = (NumberVal) ((Pair) expPair.getCdr()).getCar();

				return new NumberVal(a.getVal().add(b.getVal()));
			}
		};

		env.add(new SymbolVal("+"),add);

		return env;
	}

}

public class Vole{ 

	public static void main(String[] args){
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		Scanner inputScanner = new Scanner(System.in);
		Parser parser = new Parser();
		Environment env = new Environment();
		env.add(new SymbolVal("cons"),new Cons());
		env.concat(MathLib.getEnv());
		Evaluator evaluator = new Evaluator(env);

		while(true){
			try{
				writer.write("vole>");
				writer.flush();
				String input = inputScanner.nextLine();
				parser.setInput(new StringReader(input));
				Expression exp = parser.parseSexp();
				Expression result = evaluator.eval(exp);
				parser.printExpression(result, writer);
				writer.write("\n");
				writer.flush();
			}catch(Exception e){
				e.printStackTrace();
			}
		}	
	}




}
