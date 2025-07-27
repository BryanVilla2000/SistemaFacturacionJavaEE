package com.fitsystem.bean;

import com.fitsystem.model.Cliente;
import com.fitsystem.model.DetalleFactura;
import com.fitsystem.model.Factura;
import com.fitsystem.model.Producto;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("facturaBean")
@ViewScoped
public class FacturaBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "fitsystemPU")
    private transient EntityManager em;

    @Inject
    private AuthBean authBean;

    private Factura factura;
    private List<DetalleFactura> detalles;
    private List<Factura> listado;

    private String cedulaInput;
    private String codigoInput;
    private int cantidadInput;

    @PostConstruct
    public void init() {
        nuevaVenta();
        // cargar facturas emitidas
        listado = em.createQuery(
                "SELECT f FROM Factura f ORDER BY f.fecha DESC", Factura.class)
                .getResultList();
    }

    /** Inicializa todo para una nueva venta */
    public void nuevaVenta() {
        factura = new Factura();
        factura.setTotal(0.0); // importante para evitar null
        detalles = new ArrayList<>();
        cedulaInput = "";
        codigoInput = "";
        cantidadInput = 1;
    }

    /** 1. Buscar cliente por cédula */
    public void buscarCliente() {
        TypedQuery<Cliente> q = em.createQuery(
                "SELECT c FROM Cliente c WHERE c.cedula = :cedula", Cliente.class);
        q.setParameter("cedula", cedulaInput);
        List<Cliente> resultados = q.getResultList();
        if (resultados.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "No existe cliente con esa cédula", null));
            factura.setCliente(null);
        } else {
            factura.setCliente(resultados.get(0));
        }
    }

    /** 2. Agregar detalle de producto */
    public void agregarDetalle() {
        Producto p = em.createQuery(
                "SELECT p FROM Producto p WHERE p.codigo = :codigo", Producto.class)
                .setParameter("codigo", codigoInput)
                .getResultStream().findFirst().orElse(null);
        if (p == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Código de producto inválido", null));
            return;
        }
        DetalleFactura d = new DetalleFactura();
        d.setProducto(p);
        d.setCantidad(cantidadInput);
        d.setPrecioUnitario(p.getPrecioUnitario());
        d.setSubtotal(p.getPrecioUnitario() * cantidadInput);
        detalles.add(d);
        recalcTotal();
    }

    /** 3. Eliminar un ítem */
    public void eliminarDetalle(DetalleFactura d) {
        detalles.remove(d);
        recalcTotal();
    }

    private void recalcTotal() {
        double sum = detalles.stream()
                .mapToDouble(DetalleFactura::getSubtotal)
                .sum();
        factura.setTotal(sum);
    }

    /** 4. Guardar la factura en BD */
    @Transactional
    public String guardarFactura() {
        if (factura.getCliente() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Debe buscar un cliente primero", null));
            return null;
        }
        factura.setUsuario(authBean.getUsuarioAutenticado());
        detalles.forEach(d -> d.setFactura(factura));
        factura.setDetalles(detalles);
        em.persist(factura);
        return "list.xhtml?faces-redirect=true";
    }

    /** Anular (eliminar) una factura ya emitida */
    @Transactional
    public void anularFactura(Factura f) {
        try {
            Factura toRemove = em.find(Factura.class, f.getId_factura());
            em.remove(toRemove);
            listado.remove(f);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Factura anulada", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al anular la factura.", null));
        }
    }

    // Getters para subtotales e IVA (nunca devolverán null)
    public double getSubtotal() {
        return factura.getTotal();
    }

    public double getIva() {
        return factura.getTotal() * 0.12;
    }

    public double getTotalConIva() {
        return factura.getTotal() * 1.12;
    }

    // — Getters / Setters —
    public Factura getFactura() {
        return factura;
    }

    public List<DetalleFactura> getDetalles() {
        return detalles;
    }

    public List<Factura> getListado() {
        return listado;
    }

    public String getCedulaInput() {
        return cedulaInput;
    }

    public void setCedulaInput(String cedulaInput) {
        this.cedulaInput = cedulaInput;
    }

    public String getCodigoInput() {
        return codigoInput;
    }

    public void setCodigoInput(String codigoInput) {
        this.codigoInput = codigoInput;
    }

    public int getCantidadInput() {
        return cantidadInput;
    }

    public void setCantidadInput(int cantidadInput) {
        this.cantidadInput = cantidadInput;
    }
}
