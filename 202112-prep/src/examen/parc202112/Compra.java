package examen.parc202112;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Clase para gestión de objetos de compra en minitienda
 */
public class Compra {
	private int id;
	private long fecha;
	private String cliente;
	private int cantidad;
	private Producto producto;
	
	public Compra(int id, long fecha, String cliente, int cantidad, Producto producto) {
		super();
		this.id = id;
		this.fecha = fecha;
		this.cliente = cliente;
		this.cantidad = cantidad;
		this.producto = producto;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getFecha() {
		return fecha;
	}

	public void setFecha(long fecha) {
		this.fecha = fecha;
	}

	public String getCliente() {
		return cliente;
	}

	public void setCliente(String cliente) {
		this.cliente = cliente;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

		private static SimpleDateFormat sdf = new SimpleDateFormat( "dd/MM/yy HH:mm:ss" );
	@Override
	public String toString() {
		return id + "\t" + producto.getId() + "\t" + sdf.format( new Date( fecha ) ) + "\t" + cantidad + " unidades";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Compra) {
			return id == ((Compra)obj).id;
		} else {
			return false;
		}
	}
}
