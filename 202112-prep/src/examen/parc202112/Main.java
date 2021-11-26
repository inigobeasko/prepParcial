package examen.parc202112;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Ventana principal de minitienda
 */
@SuppressWarnings("serial")
public class Main extends JFrame{

	private static Main ventana;  // Ventana única principal
	public static void main(String[] args) {
		ventana = new Main();
		ventana.setVisible( true );
	}
	private static SimpleDateFormat sdf = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" ); // Formateador de fechas con horas
	
	private JLabel lInfo; // Información
	private DefaultTableModel mDatos;  // Modelo de datos de tabla central
	private JTable tDatos;      // Tabla central de la ventana
	private ArrayList<Producto> listaProds;
	
	/** Constructor de la ventana
	 */
	public Main() {
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		setSize( 640, 400 );
		setTitle( "Minitienda Programación III" );
		lInfo = new JLabel( " " );
		tDatos = new JTable();
		tDatos.setFont( new Font( "Arial", Font.PLAIN, 14 ) );
		JPanel pBotonera = new JPanel();
		getContentPane().add( lInfo, BorderLayout.NORTH );
		getContentPane().add( new JScrollPane(tDatos), BorderLayout.CENTER );
		getContentPane().add( pBotonera, BorderLayout.SOUTH );
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				if (new File("minitienda.db").exists()) {
					// Poner el parámetro a true si se quiere reiniciar la base de datos
					BaseDatos.abrirConexion( "minitienda.db", false );  // Abrir base de datos existente
				} else {
					BaseDatos.abrirConexion( "minitienda.db", true );  // Crear base de datos con datos iniciales
				}
				verProductos();  // Según se inicia la ventana se visualizan los productos
			}
			@Override
			public void windowClosed(WindowEvent e) {
				BaseDatos.cerrarConexion();
			}
		});
		
		JButton b = new JButton( "Prods" );
		b.setFont( new Font( "Arial", Font.PLAIN, 12 ) );
		pBotonera.add( b );
		b.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				verProductos();
			}
		});
		
		b = new JButton( "Compras" );
		b.setFont( new Font( "Arial", Font.PLAIN, 12 ) );
		pBotonera.add( b );
		b.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				verCompras();
			}
		});
		
		b = new JButton( "+ Compra" );
		b.setFont( new Font( "Arial", Font.PLAIN, 12 ) );
		pBotonera.add( b );
		b.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				accionCompra();
			}
		});
		
		b = new JButton( "- Compra" );
		b.setFont( new Font( "Arial", Font.PLAIN, 12 ) );
		pBotonera.add( b );
		b.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				anularCompra();
			}
		});
		
	}
	
	// Carga todos los productos en memoria partiendo de la base de datos
	private void cargarProds() {
		listaProds = BaseDatos.getProductos();  // Carga los productos con compras vacías
		for (Producto p : listaProds) {
			p.getListaCompras().addAll( BaseDatos.getCompras(p) );  // Añade las compras de cada producto
		}
	}

	// Ver productos en la ventana
	private void verProductos() {
		Vector<String> cabeceras = new Vector<String>( Arrays.asList( "Cod", "Nombre", "Precio" ) );
		mDatos = new DefaultTableModel(  // Inicializa el modelo
			new Vector<Vector<Object>>(),  // Datos de la jtable (vector de vectores) - vacíos de momento
			cabeceras  // Cabeceras de la jtable
		);
		cargarProds();
		for (Producto p : listaProds) {
			mDatos.addRow( new Object[] { p.getId(), p.getNombre(), p.getPrecio() } );
		}
		tDatos.setModel( mDatos );
		// Pone tamaños a las columnas de la tabla
		tDatos.getColumnModel().getColumn(0).setMinWidth(40);
		tDatos.getColumnModel().getColumn(0).setMaxWidth(40);
		tDatos.getColumnModel().getColumn(2).setMinWidth(60);
		tDatos.getColumnModel().getColumn(2).setMaxWidth(60);		
	}
	
	// Ver todas las compras en la tabla principal de la ventana
	private void verCompras() {
		Vector<String> cabeceras = new Vector<String>( Arrays.asList( "Cod", "Prod", "Cliente", "Fecha", "Cant" ) );
		mDatos = new DefaultTableModel(  // Inicializa el modelo
			new Vector<Vector<Object>>(),  // Datos de la jtable (vector de vectores) - vacíos de momento
			cabeceras  // Cabeceras de la jtable
		);
		ArrayList<Compra> compras = BaseDatos.getCompras( listaProds );
		for (Compra compra : compras) {
			String fecha = sdf.format( new Date( compra.getFecha() ) );
			mDatos.addRow( new Object[] { compra.getId(), compra.getProducto().getId(), compra.getCliente(), fecha, compra.getCantidad() } );
		}
		tDatos.setModel( mDatos );
		// Pone tamaños a las columnas de la tabla
		tDatos.getColumnModel().getColumn(0).setMinWidth(40);
		tDatos.getColumnModel().getColumn(0).setMaxWidth(40);
		tDatos.getColumnModel().getColumn(1).setMinWidth(40);
		tDatos.getColumnModel().getColumn(1).setMaxWidth(40);
		tDatos.getColumnModel().getColumn(3).setMinWidth(160);
		tDatos.getColumnModel().getColumn(3).setMaxWidth(160);		
		tDatos.getColumnModel().getColumn(4).setMinWidth(40);
		tDatos.getColumnModel().getColumn(4).setMaxWidth(40);
	}
	
	// Lanza una acción de compra
	private void accionCompra() {
		if (listaProds==null || listaProds.isEmpty()) return;
		Producto producto = selProducto();
		if (producto==null) return;  // No elegido producto
		do {  // Ciclo de validación de cantidad (pide cantidad hasta que sea correcta o escape
			try {
				String resp = JOptionPane.showInputDialog( ventana, "Cantidad a comprar:", "1" );
				if (resp==null) return; // No definida cantidad
				int cantidad = Integer.parseInt( resp );
				resp = JOptionPane.showInputDialog( ventana, "Cliente que compra:", "" );
				if (resp.isEmpty()) return; // No definido cliente
				if (cantidad > 0) {  // Correcto: insertar
					Compra compra = new Compra( 0, System.currentTimeMillis(), resp, cantidad, producto );  // id 0 porque no lo sabemos
					BaseDatos.insertarCompra( compra );  // El método modifica el id al insertarlo
					lInfo.setText( "Añadida compra: " + compra );
					break; // Sale del ciclo
				} else {
					// Error en cantidad no positiva
					JOptionPane.showMessageDialog( ventana, "Error en cantidad, introduce un número mayor que cero" );
				}
			} catch (NumberFormatException e) {
				// Error en cantidad
				JOptionPane.showMessageDialog( ventana, "Error en cantidad, introduce un número mayor que cero" );
			}
		} while (true);
		verCompras();  // Ver la tabla de compras
	}
		private Producto selProducto() {
			String[] opciones = new String[ listaProds.size() ];
			int i = 0;
			for (Producto p : listaProds) {
				opciones[ i ] = p.getNombre();
				i++;
			}
			Object selProducto = JOptionPane.showInputDialog( ventana, "Selecciona artículo:", "Compra", JOptionPane.QUESTION_MESSAGE, null, opciones, null );
			if (selProducto==null) return null;  // No elegido producto
			Producto producto = null;
			for (Producto p : listaProds)
				if (p.getNombre().equals( selProducto )) { producto = p; break; }
			return producto;
		}
	
	// Lanza una anulación de compra
	private void anularCompra() {
		if (listaProds==null || listaProds.isEmpty()) return;
		Producto producto = selProducto();
		if (producto==null) return;  // No elegido producto
		ArrayList<Compra> listaCompras = BaseDatos.getCompras( producto );
		if (listaCompras!=null && listaCompras.size()>0) {
			producto.getListaCompras().clear();
			producto.getListaCompras().addAll( listaCompras );
			Compra compra = selCompra( producto );
			if (compra!=null) {
				anularCompra( compra );
			}
		} else {
			lInfo.setText( "No hay compras de este producto" );
		}
	}

	// Anula una compra ya conocida borrándola de la base de datos y mostrando las compras en ventana
	private void anularCompra( Compra compra ) {
		try {
			BaseDatos.borrarCompra( compra );
			lInfo.setText( "Borrada compra: " + compra );
			verCompras();  // Ver la tabla de compras refrescada
		} catch (SQLException e) {
			lInfo.setText( "Error en borrado de compra: " + compra );
		}
	}
	
		private Compra selCompra( Producto producto ) {
			String[] opciones = new String[ producto.getListaCompras().size() ];
			int i = 0;
			for (Compra c : producto.getListaCompras()) {
				opciones[ i ] = sdf.format( new Date( c.getFecha() ) ) + " (" + c.getCantidad() + " unidades)";
				i++;
			}
			Object selCompra = JOptionPane.showInputDialog( ventana, "Selecciona compra:", "Selección compra de " + producto.getNombre(), JOptionPane.QUESTION_MESSAGE, null, opciones, null );
			if (selCompra==null) return null;  // No elegida compra
			int seleccionado = 0;
			for (String texto : opciones) {
				if (texto.equals( selCompra )) { 
					break; 
				}
				seleccionado++; 
			}
			return producto.getListaCompras().get( seleccionado );
		}
	
}
