package jdbc;

import java.sql.SQLException;
import java.util.*;

public class Controller {
	
	Scanner ent = new Scanner(System.in);
	
	// Función que inicia el 'ciclo' de la aplicación, llamando a la función 'opciones()'
	public void inicializa() {
		try {
			opciones();
		} catch(SQLException e) {
			System.err.println("Excepción relacionada con la base de datos."); 
			e.printStackTrace();
		} catch(Exception e2) {
			System.err.println("Se ha producido una excepción.");
			e2.printStackTrace();
		}
	}
	
	/* Función que, a través de la opción marcada por el usuario que le devuelve la función menu(), realiza 
	 las correspondientes llamadas a funciones del modelo, o de esta misma clase. */

	public void opciones() throws SQLException, Exception {
		
		
		String opcion = "";
		while(!opcion.equals("8")) {
			
			opcion = menu();
			
			/* En el caso en el que no haya ningún alumno registrado, obligo al usuario a registrar uno antes de realizar 
			cualquier otra acción. */
			if(!Model.hayAlumnos() && (!opcion.equals("2") || !opcion.equals("8"))) {
				System.out.println("No hay alumnos registrados. \nAntes de realizar cualquier " +
			"otra operación, debes registrar alguno. \nPulsa la tecla 'n' si quieres para salir del programa, y " + 
			"cualquier otra para continuar: ");
				String respuesta = ent.nextLine();
				
				if(!respuesta.toLowerCase().equals("n")) {
					nuevoAlumno();
					opcion = menu();
				} else {
					opcion = "8";
				}
					
			}
			
			switch(opcion) {
				case "1": Model.listarAlumnos(); break;
				case "2": nuevoAlumno(); break;
				case "3": borrarAlumno(); break;
				case "4": modificaNota(); break;
				case "5": modificaNombre(); break;
				case "6": System.out.println(Model.cuentaAlumnos()); break;
				case "7": opcionesExportar(); break;
				case "8": 
						System.out.println("Has elegido salir del programa"); 
						break;
				default: System.out.println("Opción no válida.");
			}			
		}
		Model.cerrarConexion();
		System.exit(0);
	}
	
	/* Función encargada de obtener los datos del nuevo alumno, y comprobar que estos son correctos, obligando al usuario
	a introducir datos válidos. También comprobará que el dni no esté ya registrado, ya que es la clave primaria. 
	Cuando todo sea correcto, llamará al modelo para registrar al nuevo alumno.*/
	
	public void nuevoAlumno() throws SQLException, Exception {
		String dni, nombre, nota; 
		System.out.println("REGISTRO DE ALUMNO\n");
		do {
			System.out.println("Introduce el dni con la letra:");
			dni = ent.nextLine();
		} while(!compruebaDni(dni));
		
		System.out.println("Introduce el nombre:");
		nombre = ent.nextLine();
					
		do {
			System.out.println("Introduce una nota entre 0 y 10: ");
			nota = ent.nextLine();
		} while(!validaNota(nota));

		if(!Model.buscaPorDni(dni)) {
			Alumno alumno = new Alumno(dni, nombre, Double.valueOf(nota));
			Model.insertar(alumno);
			System.out.println("Alumno registrado.");
		} else 
			System.out.println("Ese dni ya está registrado");
			
	}
	
	/* Pide el dni del alumno que se quiere borrar. Después, llama a la función que comprueba si el dni existe en la bbdd.
	Si existe, vuelve a llamar al metodo 'borrarAlumno()' del modelo, pasándole el dni como parámetro. */
	
	public void borrarAlumno() throws SQLException {
		System.out.println("ELIMINAR ALUMNO\n");
		System.out.println("Introduce el dni del alumno que quieres eliminar: ");
		String dni = ent.nextLine();
		while(!Model.buscaPorDni(dni)) {
			System.out.println("Ese dni no existe. Introduce el dni: ");
			dni = ent.nextLine();
		}
		Model.borrarAlumno(dni);
		System.out.println("Alumno eliminado.");
	}
	
	/* Primero comprueba que existe a través de la función 'buscaPorDni()' del modelo. Después, pide el nombre nuevo y 
	 llama a la función 'actualizarNombre()' del modelo. */
	public void modificaNombre() throws SQLException {
		System.out.println("MODIFICAR NOMBRE\n");
		String dni, nombre;
		System.out.println("Introduce el dni del alumno que quieres modificar: ");
		dni = ent.nextLine();
		while(!Model.buscaPorDni(dni)) {
			System.out.println("Ese dni no existe. Introduce el dni: ");
			dni = ent.nextLine();
		}
		System.out.println("Introduce el nuevo nombre: ");
		nombre = ent.nextLine();
		Model.actualizarNombre(dni, nombre);
		System.out.println("Nombre actualizado.");
	
	}
	/* Verifica que el dni existe. Después pide una nueva nota, comprobando que sea un valor válido(numérico del 0 a 10). 
	 Por último, llama a 'modificaNota' del modelo pasándole el dni y la nueva nota. */
	
	public void modificaNota() throws SQLException, Exception {
		System.out.println("MODIFICAR NOTA\n");
		String dni, nota;
				
		System.out.println("Introduce el dni del alumno: ");
		dni = ent.nextLine();
		while(!Model.buscaPorDni(dni)) {
			System.out.println("Ese dni no existe. Introduce el dni: ");
			dni = ent.nextLine();
		}
		
		do {
			System.out.println("Introduce una nota entre 0 y 10: ");
			nota = ent.nextLine();
		} while(!validaNota(nota));
	
		
		Model.modificaNota(dni, Double.valueOf(nota));
		System.out.println("Nota actualizada."); 
	}
	
	/* Función utilizada en el momento de registrar un usuario. Valida un dni, el cuál debe componerse de 8 números y 
	 1 letra. Además, se comprueba el orden, asegurando que la letra esté situada en la última posición del String. */
	
	public boolean compruebaDni(String dni) {
		// Comprobación de longitud
		if(dni.length() != 9) {
			return false;
		} 
		
		char[] cifras = dni.toCharArray();
		// Comprobación de que la parte numérica es correcta
		for(byte i = 0; i < 8 ; i++) {
			if(cifras[i] < 49 || cifras[i] > 57) {
				return false;
			}
		}
		// Comprobación de que la última cifra es una letra. Última comprobación.
		if((cifras[8] >= 65 && cifras[8] <= 90) || (cifras[8] >= 97 && cifras[8] <= 122)) {		
			return true;
		}
		return false;
	}

	// Función encargada de mostrar el menú con las opciones por pantalla, y devolver la opción que ha elegido el usuario.
	
	public String menu() {
		String opcion = "";
		System.out.println("************************************");
		System.out.println("¿Qué deseas hacer?");
		System.out.println("--------------------------------------\n" +
				"1. Listar alumnos \n2. Insertar alumno\n" +
				"3. Eliminar alumno \n4. Modificar nota\n" +
				"5. Modificar nombre \n6. Número de alumnos\n" +
				"7. Exportar\n8. Salir\n" + 
				"--------------------------------------\n"
		);
		opcion = ent.nextLine();
		return opcion;
	}
	
	/* Función que mostrará un submenú cuando el usuario elija la opción de 'Exportar', dando 3 alternativas
	 (XML, txt y JSON), y llamará a la función correspondiente del modelo según la opción elegida. */
	
	public void opcionesExportar() throws Exception {
		System.out.println("OPCIONES EXPORTAR\n");
		String opcion = "";
		System.out.println("¿Qué formato quieres?");
		System.out.println("\n1. Xml\n2. Fichero de texto\n3. JSON\n");
		opcion = ent.nextLine();
		switch(opcion) {
			case "1": Model.generaXML(); break;
			case "2": Model.generaFicheroTXT(); break;
			case "3": Model.generaJson(); break;
			default: System.out.println("Opción no válida");
		}
		opciones();
	}
	
	// Método que comprueba que la nota sea un valor válido(número positivo entre 0 y 10).
	public static boolean validaNota(String respuesta) {
		try {
			double edad = Double.parseDouble(respuesta);
			if(edad < 0 || edad > 10)
				return false;
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
}
