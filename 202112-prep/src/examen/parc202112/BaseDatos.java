package examen.parc202112;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Clase de gestión de base de datos del sistema de compras
 * Incluye un logger que registra todas las operaciones de base de datos
 */
public class BaseDatos {
	private static Connection conexion;
	private static Logger logger = Logger.getLogger( "BaseDatos" );
	
	/** Abre conexión con la base de datos
	 * @param nombreBD	Nombre del fichero de base de datos
	 * @param reiniciaBD	true si se quiere reiniciar la base de datos (se borran sus contenidos si los tuviera y se crean datos por defecto)
	 * @return	true si la conexión ha sido correcta, false en caso contrario
	 */
	public static boolean abrirConexion( String nombreBD, boolean reiniciaBD ) {
		try {
			logger.log( Level.INFO, "Carga de librería org.sqlite.JDBC" );
			Class.forName("org.sqlite.JDBC");  // Carga la clase de BD para sqlite
			logger.log( Level.INFO, "Abriendo conexión con " + nombreBD );
			conexion = DriverManager.getConnection("jdbc:sqlite:" + nombreBD );
			if (reiniciaBD) {
				Statement statement = conexion.createStatement();
				String sent = "DROP TABLE IF EXISTS producto";
				logger.log( Level.INFO, "Statement: " + sent );
				statement.executeUpdate( sent );
				sent = "CREATE TABLE producto (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre varchar(100), precio real);";
				logger.log( Level.INFO, "Statement: " + sent );
				statement.executeUpdate( sent );
				sent = "DROP TABLE IF EXISTS compra";
				logger.log( Level.INFO, "Statement: " + sent );
				statement.executeUpdate( sent );
				sent = "CREATE TABLE compra (id INTEGER PRIMARY KEY AUTOINCREMENT, idProducto int, cliente varchar(40), fecha bigint, cantidad int);";
				logger.log( Level.INFO, "Statement: " + sent );
				statement.executeUpdate( sent );
				try {
					Scanner scanner = new Scanner( BaseDatos.class.getResourceAsStream("productos-inic.txt") );
					while (scanner.hasNextLine()) {
						String linea = scanner.nextLine();
						String[] datos = linea.split( "\t" );
						sent = "insert into producto (id, nombre, precio) values (" + datos[0] + ",'" + datos[1] + "'," + datos[2] + ");";
						logger.log( Level.INFO, "Statement: " + sent );
						statement.executeUpdate( sent );
					}
					scanner.close();
					scanner = new Scanner( BaseDatos.class.getResourceAsStream("compras-inic.txt") );
					while (scanner.hasNextLine()) {
						String linea = scanner.nextLine();
						String[] datos = linea.split( "\t" );
						sent = "insert into compra (id, idProducto, cliente, fecha, cantidad) values (" + datos[0] + "," + datos[1] + ",'" + datos[2] + "'," + datos[3] + "," + datos[4] + ");";
						logger.log( Level.INFO, "Statement: " + sent );
						statement.executeUpdate( sent );
					}
					scanner.close();
				} catch(Exception e) {
					logger.log( Level.SEVERE, "Excepción", e );
				}
			}
			return true;
		} catch(Exception e) {
			logger.log( Level.SEVERE, "Excepción", e );
			return false;
		}
	}	
	
	/** Cierra la conexión abierta de base de datos ({@link #abrirConexion(String)})
	 */
	public static void cerrarConexion() {
		try {
			logger.log( Level.INFO, "Cerrando conexión" );
			conexion.close();
		} catch (SQLException e) {
			logger.log( Level.SEVERE, "Excepción", e );
		}
	}
	
	/** Lee los productos de la conexión de base de datos abierta (debe abrirse previamente con {@link #abrirConexion(String, boolean)}
	 * @return	Lista completa de productos, null si hay algún error
	 */
	public static ArrayList<Producto> getProductos() {
		try (Statement statement = conexion.createStatement()) {
			ArrayList<Producto> ret = new ArrayList<>();
			String sent = "select * from producto;";
			logger.log( Level.INFO, "Statement: " + sent );
			ResultSet rs = statement.executeQuery( sent );
			while( rs.next() ) { // Leer el resultset
				int id = rs.getInt("id");
				String nombre = rs.getString("nombre");
				double precio = rs.getDouble("precio");
				ret.add( new Producto ( id, nombre, precio, new ArrayList<Compra>() ) );
			}
			return ret;
		} catch (Exception e) {
			logger.log( Level.SEVERE, "Excepción", e );
			return null;
		}
	}

	/** Inserta una compra en la base de datos abierta (debe abrirse previamente con {@link #abrirConexion(String, boolean)}
	 * Actualiza el id de la compra insertada
	 * @param compra	Compra a insertar
	 * @return	true si la inserción es correcta, false en caso contrario
	 */
	public static boolean insertarCompra( Compra compra ) {
		try (Statement statement = conexion.createStatement()) {
			String sent = "insert into compra (idProducto, cliente, fecha, cantidad) values (" + compra.getProducto().getId() + ",'" + compra.getCliente() + "'," + compra.getFecha() + "," + compra.getCantidad() + ");";
			logger.log( Level.INFO, "Statement: " + sent );
			int insertados = statement.executeUpdate( sent );
			if (insertados!=1) return false;  // Error en inserción
			// Búsqueda de la fila insertada - para ello hay que recuperar la clave autogenerada. Hay varias maneras, vemos dos diferentes:
			// Se hace utilizando método del propio objeto statement
			ResultSet rrss = statement.getGeneratedKeys();  // Genera un resultset ficticio con las claves generadas del último comando
			rrss.next();  // Avanza a la única fila 
			int pk = rrss.getInt( 1 );  // Coge la única columna (la primary key autogenerada)
			compra.setId( pk );
			return true;
		} catch (Exception e) {
			logger.log( Level.SEVERE, "Excepción", e );
			return false;
		}
	}
	
	/** Lee todas las compras de la conexión de base de datos abierta (debe abrirse previamente con {@link #abrirConexion(String, boolean)}
	 * @param productos	Lista de todos los productos existentes
	 * @return	Lista completa de compras, vacía si no hay ninguna, null si hay algún error
	 */
	public static ArrayList<Compra> getCompras( ArrayList<Producto> productos ) {
		try (Statement statement = conexion.createStatement()) {
			ArrayList<Compra> ret = new ArrayList<>();
			String sent = "select * from compra;";
			logger.log( Level.INFO, "Statement: " + sent );
			ResultSet rs = statement.executeQuery( sent );
			while( rs.next() ) { // Leer el resultset
				int id = rs.getInt("id");
				long fecha = rs.getLong("fecha");
				String cliente = rs.getString("cliente");
				int cantidad = rs.getInt("cantidad");
				int idProd = rs.getInt("idProducto");
				for (Producto p : productos) {
					if (p.getId() == idProd) {
						ret.add( new Compra( id, fecha, cliente, cantidad, p ) );
						break;
					}
				}
			}
			return ret;
		} catch (Exception e) {
			logger.log( Level.SEVERE, "Excepción", e );
			return null;
		}
	}

	/** Lee las compras de un producto dado de la conexión de base de datos abierta (debe abrirse previamente con {@link #abrirConexion(String, boolean)}
	 * @return	Lista completa de compras de ese producto, vacía si no hay ninguna, null si hay algún error
	 */
	public static ArrayList<Compra> getCompras( Producto producto ) {
		try (Statement statement = conexion.createStatement()) {
			ArrayList<Compra> ret = new ArrayList<>();
			String sent = "select * from compra where idProducto=" + producto.getId() + ";";
			logger.log( Level.INFO, "Statement: " + sent );
			ResultSet rs = statement.executeQuery( sent );
			while( rs.next() ) { // Leer el resultset
				int id = rs.getInt("id");
				long fecha = rs.getLong("fecha");
				String cliente = rs.getString("cliente");
				int cantidad = rs.getInt("cantidad");
				ret.add( new Compra( id, fecha, cliente, cantidad, producto ) );
			}
			return ret;
		} catch (Exception e) {
			logger.log( Level.SEVERE, "Excepción", e );
			return null;
		}
	}

	/** Borra una compra en la base de datos abierta (debe abrirse previamente con {@link #abrirConexion(String, boolean)}
	 * @param compra	Compra a borrar
	 * @throws SQLException	Lanzada si no existe la compra a borrar, o se produce cualquier otro error de base de datos
	 */
	public static void borrarCompra( Compra compra ) throws SQLException {
		Statement statement = conexion.createStatement();
		String sent = "delete from compra where id=" + compra.getId() + ";";
		logger.log( Level.INFO, "Statement: " + sent );
		int borrados = statement.executeUpdate( sent );
		if (borrados==0) throw new SQLException( "No se ha borrado ninguna compra con id=" + compra.getId() );
		statement.close();
	}
	
	
}
