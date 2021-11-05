package jdbc;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.google.gson.Gson;

/* Clase con atributos y métodos estáticos que se encarga de realizar las operaciones con la base de datos. */
public class Model {
	
	static Connection conexion = null;
	static String url = "jdbc:postgresql://localhost/alumnosdb"; 
	static String rutaFicheros = "/home/fernando/Escritorio/"; // ruta donde se generarán los ficheros(xml, txt y json)
	static String tablaAlumnos = "alumnos";
	
	/* Función encargada de establecer la conexión con la base de datos. Todas las funciones utilizarán esta conexión 
	 durante la ejecución del programa, y se cerrará con la llamada a la función 'cerrarConexion()', definida al final del
	 fichero. */
	
	public static void iniciaConexion(){
		try{
			conexion = DriverManager.getConnection(url, "alumno", "alumno");
			System.out.println("Conexión establecida con " + url);
		} catch(SQLException e){
			e.printStackTrace();	
		} catch(Exception e){ 
			e.printStackTrace();
		}

	}
	
	// Función que comprueba si hay algún alumno registrado, llamando a la función 'cuentaAlumnos()' de la bbdd.
	
	public static boolean hayAlumnos() throws SQLException {
		boolean hayAlumnos = false;
		CallableStatement callFunction = conexion.prepareCall("{ ? = call public.cuentaAlumnos()}");
		callFunction.registerOutParameter(1, Types.INTEGER);
		callFunction.execute();
		int alumnos = callFunction.getInt(1);
		callFunction.close();
		if(alumnos > 0)
			hayAlumnos = true;
		return hayAlumnos;
	}
	
	/* Función que registra un nuevo alumno. No hay necesidad de ejecutar esta operación en una transacción, ya que es 
	sólo una acción, pero lo he puesto a modo de ejemplo, capturando aquí mismo la excepción, a diferencia del resto de 
	métodos en los cuáles 'lanzo' las excepciones hasta el método 'inicializa()' del controlador, donde las capturo. */
	
	public static void insertar(Alumno alumno)  {

		String sentenciaSql = "INSERT INTO " + tablaAlumnos +"(dni, nombre, nota) VALUES (?,?,?);";
		
		try{
			PreparedStatement preparedStat = conexion.prepareStatement(sentenciaSql);
			conexion.setAutoCommit(false);
					
			preparedStat.setString(1, alumno.getDni().toString());
			preparedStat.setString(2, alumno.getNombre().toString());
			preparedStat.setDouble(3, alumno.getNota());
			preparedStat.execute();				
			
			preparedStat.close();
			conexion.commit();	
		} catch(SQLException e){

			try {
				conexion.rollback();
			} catch(SQLException e2) {
				System.err.println("El rollback ha fallado.");
				e2.printStackTrace();
			}
			
		}
		
	}
	
	/* Elimina un alumno a través del dni. Antes de llamar a ésta función, se hace una comprobación previa de que el dni
	corresponde con el de algún alumno. */
	
	public static void borrarAlumno(String dni) throws SQLException {
			
		String delete = "DELETE FROM "+ tablaAlumnos +" WHERE dni = ?";
		PreparedStatement stat = conexion.prepareStatement(delete);
		stat.setString(1, dni);
		stat.execute();
		stat.close();
	
	}

	// Lista los alumnos actualmente registrados. 
	
	public static void listarAlumnos() throws SQLException {
		String consulta = "SELECT * FROM " + tablaAlumnos + ";";
		Statement stat = conexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = stat.executeQuery(consulta);
		System.out.println("ALUMNOS");
		System.out.println("-----------------------------------");
		while(rs.next()){			
			System.out.println("-----------------------------------");
			System.out.println(
				"\nDNI: " + rs.getString("dni") + 
				"\nNombre: " + rs.getString("nombre") +
				"\nNota: " + rs.getDouble("nota")
			);
		}
		stat.close();
	}

	// Comprueba que el dni recibido como parámetro corresponde con el de algún alumno registrado.
	
	public static boolean buscaPorDni(String dni) throws SQLException {
		String consulta = "SELECT * FROM " + tablaAlumnos + "; ";
		Statement stat = conexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = stat.executeQuery(consulta);
		while(rs.next()){
			if(dni.equals(rs.getString("dni").toString())){
				return true;
			}
		}
		rs.close();
		stat.close();
		return false;
	}
	
	// Función que llama a una función definida en la bbdd, que devuelve el número total de alumnos.
	
	public static String cuentaAlumnos() throws SQLException {

		CallableStatement callFunction = conexion.prepareCall("{ ? = call public.cuentaAlumnos()}");
		callFunction.registerOutParameter(1, Types.INTEGER);
		callFunction.execute();
		int alumnos = callFunction.getInt(1);
		callFunction.close();
		if(alumnos != 0){
			return "Hay " + alumnos + " alumnos.";
		} else return "No hay alumnos";
		
	}
	
	/* Funciones de actualización de datos(nombre y nota, respectivamente). La comprobación de que el dni existe se realiza
	antes de la llamada a estas funciones. */
	
	public static void actualizarNombre(String dni, String nombreNuevo) throws SQLException {
		String sqlUpdate = "UPDATE " + tablaAlumnos +" SET nombre=? WHERE dni LIKE ?;";
		PreparedStatement preparedStat = conexion.prepareStatement(sqlUpdate);
		preparedStat.setString(1, nombreNuevo);
		preparedStat.setString(2, dni);
		preparedStat.executeUpdate();
		preparedStat.close();
	}
	public static void modificaNota(String dni, double nota) throws SQLException {
		String sqlUpdate = "UPDATE " + tablaAlumnos + " SET nota=? WHERE dni LIKE ?;";
		PreparedStatement preparedStat = conexion.prepareStatement(sqlUpdate);
		preparedStat.setDouble(1, nota);
		preparedStat.setString(2, dni);
		preparedStat.executeUpdate();
		preparedStat.close();
	}
	
	/* Función que carga los alumnos de la BBDD en un ArrayList. Esta función será llamada para generar los archivos xml, 
	txt y json. */
	
	public static ArrayList<Alumno> cargaAlumnos() throws SQLException {
		String consulta = "SELECT * FROM " + tablaAlumnos + ";";
		ArrayList<Alumno> alumnos = new ArrayList<Alumno>();
		Statement stat = conexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet resultado = stat.executeQuery(consulta);
		Alumno a = null; String dni, nombre; double nota;
		
		while(resultado.next()) {
			a = new Alumno(resultado.getString("dni"), resultado.getString("nombre"), Double.valueOf(resultado.getString("nota")));
			alumnos.add(a);
		}
		resultado.close();
		stat.close();
		return alumnos;
		
	}
	
	/* Las siguientes 3 funciones son las encargadas de persistir en archivos xml, txt y json, respectivamente, los alumnos
	registrados en la BBDD*/
	
	// XML
	public static void generaXML() throws Exception {
		
		ArrayList<Alumno> alumnos = cargaAlumnos();
		
		if(alumnos.isEmpty()) {
			System.out.println("No hay alumnos.");
			return;
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation dom = builder.getDOMImplementation();
		Document document = dom.createDocument(null, "xml", null);
		Element raiz = document.createElement("alumnos");
		document.getDocumentElement().appendChild(raiz);
		
		Node nodoAlumno, nodoDni, nodoNombre, nodoNota;
		Text texto;
		
		for(Alumno alumno: alumnos) {
			nodoAlumno = document.createElement("alumno");
			raiz.appendChild(nodoAlumno);
			
			nodoDni = document.createElement("dni");
			nodoAlumno.appendChild(nodoDni);
			texto = document.createTextNode(alumno.getDni());
			nodoDni.appendChild(texto);
			
			nodoNombre = document.createElement("nombre");
			nodoAlumno.appendChild(nodoNombre);
			texto = document.createTextNode(alumno.getNombre());
			nodoNombre.appendChild(texto);
			
			nodoNota = document.createElement("nota");
			nodoAlumno.appendChild(nodoNota);
			texto = document.createTextNode(Double.toString(alumno.getNota()));
			nodoNota.appendChild(texto);	
		}
		
		Source source = new DOMSource(document);
		Result result = new StreamResult(new File(rutaFicheros + "alumnos.xml"));
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(source, result);
		System.out.println("XML creado en el Escritorio.");
		
	}
	
	// TXT
	public static void generaFicheroTXT() throws SQLException, IOException {
		
		ArrayList<Alumno> alumnos = cargaAlumnos();
		if(alumnos.isEmpty()) {
			System.out.println("No hay alumnos");
			return;
		}
		
		File file = new File(rutaFicheros + "alumnos.txt");
		if(file.exists()) {
			file.delete();
			file = new File(rutaFicheros + "alumnos.txt");
		}			
		Path path = file.toPath();
		Files.writeString(path, "--------------- ALUMNOS ---------------", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		for(Alumno a: alumnos) {
			Files.writeString(path, "\n" + a.toString() , StandardOpenOption.APPEND );
		}
		System.out.println("Fichero de texto creado en el Escritorio.");		
	}
	
	// TXT
	public static void generaJson() throws SQLException, IOException {
		ArrayList<Alumno> alumnos = cargaAlumnos();
		if(alumnos.isEmpty()) {
			System.out.println("No hay alumnos.");
			return;
		}
		File file = new File(rutaFicheros + "alumnos.json");
		if(file.exists()) {
			file.delete();
			file = new File(rutaFicheros + "alumnos.json");
		}
		Path path = file.toPath();
		Gson gson = new Gson();
		String JSON = gson.toJson(alumnos);
		Files.writeString(path, "\n" + JSON, StandardOpenOption.CREATE, StandardOpenOption.APPEND);			
		
		System.out.println("Fichero JSON creado");
	}
	
	
	
	// Función encargada de cerrar la conexión con la base de datos
	
	public static void cerrarConexion() throws SQLException {
		conexion.close();
	}	
}
