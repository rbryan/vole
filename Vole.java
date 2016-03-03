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
	BigInteger val;
	NumberVal(BigInteger val){
		this.val = val;
	}

	BigInteger getVal(){
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

	void printExpression(Expression expr, Writer out){
		try{
			if(expr instanceof Pair){
				Pair p = (Pair) expr;
				if(p.isNil()){
					out.write("nil");
				}else if(p.isList()){
					out.write("(");
					if(p.getCar() != null){
						printExpression(p.getCar(),out);
						out.write(" ");
					}

					if(p.getCdr() != null){
						printExpression(p.getCdr(),out);
					}
					out.write(")");
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

public class Vole{ 

	public static void main(String[] args){
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		Scanner inputScanner = new Scanner(System.in);
		Parser parser = new Parser();
		Evaluator evaluator = new Evaluator();

		while(true){
			try{
				writer.write("jlisp>");
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
