package examen.parc202112;

import java.util.ArrayList;

/** Clase para gestión de objetos de producto en minitienda
 */
public class Producto {
	private int id;
	private String nombre; 
	private double precio;
	private ArrayList<Compra> listaCompras;  // lista de compras de ese producto
	
	public Producto(int id, String nombre, double precio, ArrayList<Compra> listaCompras) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.precio = precio;
		this.listaCompras = listaCompras;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
		this.precio = precio;
	}

	public ArrayList<Compra> getListaCompras() {
		return listaCompras;
	}
	
	
	@Override
	public String toString() {
		return id + "\t" + nombre + "\t" + precio;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Producto) {
			return id == ((Producto)obj).id;
		} else {
			return false;
		}
	}
	
}
