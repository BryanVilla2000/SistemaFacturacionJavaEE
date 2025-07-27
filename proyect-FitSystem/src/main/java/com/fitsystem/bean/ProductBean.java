package com.fitsystem.bean;

import com.fitsystem.model.Producto;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

@Named("productBean")
@ViewScoped
public class ProductBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "fitsystemPU")
    private transient EntityManager em;

    /** El producto en edición/creación */
    private Producto producto = new Producto();
    /** Lista de todos los productos */
    private List<Producto> productos;

    @PostConstruct
    public void init() {
        productos = em.createQuery("SELECT p FROM Producto p", Producto.class)
                      .getResultList();
    }

    /** Navega al formulario para crear un nuevo producto */
    public String crearProducto() {
        producto = new Producto();
        return "/views/products/form.xhtml?faces-redirect=true";
    }

    /** Persiste un nuevo producto con validación de código único */
    @Transactional
    public String agregarProducto() {
        // 1) Comprobar unicidad de 'codigo'
        TypedQuery<Long> countQ = em.createQuery(
            "SELECT COUNT(p) FROM Producto p WHERE p.codigo = :codigo", Long.class);
        countQ.setParameter("codigo", producto.getCodigo());
        if (countQ.getSingleResult() > 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Ya existe un producto con ese código",
                                 null));
            return null;
        }

        // 2) Persistir
        try {
            em.persist(producto);
            return "/views/products/list.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Error al guardar el producto",
                                 null));
            return null;
        }
    }

    /** Redirige al formulario con parámetro id para editar */
    public String editarProducto(Producto p) {
        return "/views/products/form.xhtml?faces-redirect=true&id=" + p.getId_producto();
    }

    /** Modifica un producto existente con validación de código único */
    @Transactional
    public String modificarProducto() {
        // 1) Comprobar unicidad excluyendo el mismo registro
        TypedQuery<Long> countQ = em.createQuery(
            "SELECT COUNT(p) FROM Producto p WHERE p.codigo = :codigo AND p.id_producto <> :id", Long.class);
        countQ.setParameter("codigo", producto.getCodigo());
        countQ.setParameter("id", producto.getId_producto());
        if (countQ.getSingleResult() > 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Ya existe otro producto con ese código",
                                 null));
            return null;
        }

        // 2) Merge
        try {
            em.merge(producto);
            return "/views/products/list.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Error al modificar el producto",
                                 null));
            return null;
        }
    }

    /** Elimina el producto indicado */
    @Transactional
    public String eliminarProducto(Producto p) {
        Producto toRemove = em.merge(p);
        em.remove(toRemove);
        return "/views/products/list.xhtml?faces-redirect=true";
    }

    /**
     * Listener antes de renderizar form.xhtml: si viene id, carga el producto
     */
    public void loadProducto(ComponentSystemEvent event) {
        Long id = producto.getId_producto();
        if (id != null) {
            producto = em.find(Producto.class, id);
        }
    }

    // — Getters y setters —

    public Producto getProducto() {
        return producto;
    }
    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public List<Producto> getProductos() {
        return productos;
    }
    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}
