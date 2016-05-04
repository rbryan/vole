import Vole.Vole;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

class repl{
	public static void main(String [] args){
		Vole v = new Vole(new InputStreamReader(System.in),new OutputStreamWriter(System.out),new OutputStreamWriter(System.err));
		v.repl();
	}
}
