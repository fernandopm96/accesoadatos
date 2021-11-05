package jdbc;

public class Main {
	public static void main(String [] args) {
		
		/* Inicio la conexión con la base de datos, después creo una instancia del controlador, y luego llamo a 'inicializa()'.
		A partir de ahí, se establecerá un bucle en el controlador, que irá llamando a métodos del modelo para
		realizar las operaciones que el usuario desee respecto a la base de datos. */
		Model.iniciaConexion(); 
		Controller controller = new Controller();
		controller.inicializa();
		
	}
}
