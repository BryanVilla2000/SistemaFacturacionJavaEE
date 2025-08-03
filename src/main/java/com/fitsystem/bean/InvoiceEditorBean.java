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
import jakarta.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("invoiceEditor")
@ViewScoped
public class InvoiceEditorBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "fitsystemPU")
    private transient EntityManager em;

    @Inject
    private AuthBean authBean; // Para asignar el usuario actual

    private Factura factura;
    private List<DetalleFactura> detalles;
    private String cedulaInput;
    private String codigoInput;
    private Integer cantidadInput;

    @PostConstruct
    public void init() {
        iniciarNueva();
    }

    private void iniciarNueva() {
        factura = new Factura();
        detalles = new ArrayList<>();
        cedulaInput = "";
        codigoInput = "";
        cantidadInput = null;
    }

    public void buscarCliente() {
        try {
            Cliente c = em.createQuery(
                    "SELECT c FROM Cliente c WHERE c.cedula = :cedula", Cliente.class)
                    .setParameter("cedula", cedulaInput)
                    .getSingleResult();
            factura.setCliente(c);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cédula no encontrada", null));
        }
    }

    public void agregarDetalle() {
        if (codigoInput == null || cantidadInput == null || cantidadInput <= 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Código y cantidad válidos son obligatorios", null));
            return;
        }
        try {
            Producto p = em.createQuery(
                    "SELECT p FROM Producto p WHERE p.codigo = :codigo", Producto.class)
                    .setParameter("codigo", codigoInput)
                    .getSingleResult();

            DetalleFactura d = new DetalleFactura();
            d.setProducto(p);
            d.setCantidad(cantidadInput);
            d.setPrecioUnitario(p.getPrecioUnitario());
            d.setSubtotal(p.getPrecioUnitario() * cantidadInput);
            detalles.add(d);

            factura.setTotal(getSubtotal());
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Código de producto inválido", null));
        }
    }

    public void eliminarDetalle(DetalleFactura detalle) {
        detalles.remove(detalle);
        factura.setTotal(getSubtotal());
    }

    @Transactional
    public String guardarFactura() {
        if (factura.getCliente() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Debe buscar un cliente primero", null));
            return null;
        }
        factura.setUsuario(authBean.getUsuarioAutenticado());
        factura.setTotal(getSubtotal());
        em.persist(factura);

        for (DetalleFactura d : detalles) {
            d.setFactura(factura);
            em.persist(d);
            Producto p = em.find(Producto.class, d.getProducto().getId_producto());
            p.setStock(p.getStock() - d.getCantidad());
            em.merge(p);
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Factura guardada correctamente", null));
        return "/views/invoices/list.xhtml?faces-redirect=true";
    }

    // — Cálculos auxiliares —

    public double getSubtotal() {
        return detalles.stream()
                .mapToDouble(DetalleFactura::getSubtotal)
                .sum()/1.12;
    }

    public double getIva() {
        return getSubtotal() * 0.12;
    }

    public double getTotalConIva() {
        return getSubtotal() + getIva();
    }

    // — Getters y Setters —

    public Factura getFactura() {
        return factura;
    }

    public List<DetalleFactura> getDetalles() {
        return detalles;
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

    public Integer getCantidadInput() {
        return cantidadInput;
    }

    public void setCantidadInput(Integer cantidadInput) {
        this.cantidadInput = cantidadInput;
    }
}
