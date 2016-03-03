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


class Expression{
	Expression(){
		
	}

	boolean isList(){
		if(this instanceof Pair){
			Pair p = (Pair) this;
			if(p.cdr instanceof Pair)
				return true;
		}
		return false;
	}
}

class NumberVal extends Expression{
	Number val;
	NumberVal(Number val){
		this.val = val;
	}

	Number getVal(){
		return val;
	}
}

class SymbolVal extends Expression{
	String identifier;

	SymbolVal(String ident){
		identifier = ident;
	}

	String getIdentifier(){
		return identifier;
	}
}

class BooleanVal extends Expression{
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

	boolean isNil(){
		if(car == null && cdr == null)
			return true;
		return false;
	}
}

class ProcedureVal extends Expression{
	Environment closure;
	SymbolVal arg;
	Expression exp;

	ProcedureVal(Environment closure, SymbolVal arg, Expression exp){
		this.closure = closure;
		this.arg = arg;
		this.exp = exp;
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

}

class Evaluator{
	Environment env;

	Evaluator(){
		env = new Environment();
	}

	Evaluator(Environment def){
		this.env = def;
	}

	Expression eval(Expression exp){
		return eval(exp,env);
	}

	Expression eval(Expression exp, Environment env){
		if(exp.isList()){
			Pair list = (Pair) exp;
			Expression procExpr = eval(list.getCar(),env);
			if(procExpr instanceof ProcedureVal){
				ProcedureVal proc = (ProcedureVal) procExpr;
				Environment evalEnv = proc.getEvalEnvironment(list.getCdr(),env);
				return eval(proc.getExp(),evalEnv);
			}
		}
		
		return exp;
			
	}

}

public class Vole{ 

	public static void main(String[] args){

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		Scanner inputScanner = new Scanner(System.in);
		Evaluator evaluator = new Evaluator();

		while(true){
			try{
				writer.write("jlisp>");
				writer.flush();
				String input = inputScanner.nextLine();
				Expression exp = parseSexp(new StringReader(input));
				Expression result = evaluator.eval(exp);
				printExpression(result, writer);
				writer.write("\n");
				writer.flush();
			}catch(Exception e){
				e.printStackTrace();
			}
		}	
	}


	static int peek(Reader r){
		int c = 0;
		try{
			r.mark(1);
			c = (char) r.read();
			r.reset();
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
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	static BooleanVal parseBoolean(Reader input){
		try{
			input.read();
			if(input.read() == 't')
				return new BooleanVal(true);
			else
				return new BooleanVal(false);
		}catch(Exception e){
			e.printStackTrace();
		}

		return null;
	}

	static NumberVal parseNumber(Reader input){
		System.out.println("Called parseNumber();");
		StringBuilder builder = new StringBuilder();
		int c = 0;

		try{
			c = peek(input);
			while( 	c != '#' &&
				c != '(' &&
				c != ')' &&
				c != ';' &&
			(short) c != -1  &&
				Character.isDigit(c) &&
				!Character.isWhitespace(c)){
				
				builder.append((char) c);
				input.read();
				c = peek(input);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return new NumberVal(Integer.parseInt(builder.toString()));
	}

	static SymbolVal parseSymbol(Reader input){
		System.out.println("Called parseSymbol();");
		StringBuilder builder = new StringBuilder();
		int c = 0;

		try{
			c = peek(input);
			while( 	c != '#' &&
				c != '(' &&
				c != ')' &&
				c != ';' &&
			(short) c != -1  &&
				!Character.isWhitespace(c)){
				
				builder.append((char) c);
				input.read();
				c = peek(input);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return new SymbolVal(builder.toString());

	}

	static void parseComment(Reader input){
		Scanner scanner = new Scanner(input);
		scanner.useDelimiter("[\n]");
		String s = scanner.next(";.*");
		return;
	}

	static Pair parseList(Reader input){
		System.out.println("Called parseList()");
		int c;
		Pair head = null;
		Pair tail = null;

		try{
			c = peek(input);

			if(c == '('){
				//eat the '('
				input.read();
				
				c = peek(input);

				if((short) c == -1)
					throw new Exception("Unmatched '(' in file.");

				while((short) c != ')'){

					Expression exp = null;

					if(c == '(')
						exp = parseList(input);

					else if(c == '#')
						exp = parseBoolean(input);

					else if(c == ';'){
						parseComment(input);
						c = peek(input);
						continue;
					}
					
					else if(Character.isDigit(c))
						exp = parseNumber(input);

					else if(Character.isWhitespace(c)){
						input.read();
						c = peek(input);
						continue;
					}

					else if((short) c == -1)
						throw new Exception("Unexpected EOF.");

					//If it's none of those things it must be a symbol
					else 
						exp = parseSymbol(input);
					
					if(head == null){
						head = new Pair(exp,null);
						tail = head;
					}else{
						Pair newTail = new Pair(exp,null);
						tail.setCdr(newTail);
						tail = newTail;
					}

					c = peek(input);

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

	static Expression parseSexp(Reader input){

		int c;

		try{
			c = peek(input);

			while((short) c != -1){
				if(Character.isWhitespace(c)){
					input.read();
					c = peek(input);
					continue;
				}

				else if(c == '#'){
					return parseBoolean(input);
				}

				else if(Character.isDigit(c)){
					return parseNumber(input);
				}
				
				else if(c == '('){
					return parseList(input);
				}

				else if(c == ';'){
					parseComment(input);
					c = peek(input);
					continue;
				}
				else if(c == ')'){
					throw new Exception("Unmatched ')' found.");
				}

				else
					return parseSymbol(input);
				

			}
		}catch(Exception e){
			e.printStackTrace();

		}

		return null;
	}


}
