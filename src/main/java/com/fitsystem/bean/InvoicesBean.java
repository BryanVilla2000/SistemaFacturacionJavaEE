package com.fitsystem.bean;

import com.fitsystem.model.Factura;
import com.fitsystem.model.DetalleFactura;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

@Named("invoices")
@ViewScoped
public class InvoicesBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "fitsystemPU")
    private transient EntityManager em;

    private List<Factura> facturas;   // para el listado
    private Factura factura;          // para la vista individual
    private Long facturaId;           // recibe el parámetro ?id=

    @PostConstruct
    public void init() {
        // Cargo todas las facturas para el listado
        facturas = em.createQuery(
            "SELECT f FROM Factura f ORDER BY f.fecha DESC", Factura.class)
            .getResultList();
    }

    /** 
     * Acción desde list.xhtml: redirige a view.xhtml con el parámetro id 
     */
    public String verFactura(Long id) {
        return "/views/invoices/view.xhtml?faces-redirect=true&id=" + id;
    }

    /**
     * Listener de preRenderView en view.xhtml: lee el parámetro y carga la factura
     * En view.xhtml:
     *   <f:metadata>
     *     <f:viewParam name="id" value="#{invoices.facturaId}" />
     *     <f:event   type="preRenderView" listener="#{invoices.loadFactura}" />
     *   </f:metadata>
     */
    public void loadFactura() {
        if (facturaId != null) {
            factura = em.find(Factura.class, facturaId);
        }
    }

    // —— Getters y setters ——————————————————

    public List<Factura> getFacturas() {
        return facturas;
    }

    public Factura getFactura() {
        return factura;
    }

    public Long getFacturaId() {
        return facturaId;
    }
    public void setFacturaId(Long facturaId) {
        this.facturaId = facturaId;
    }

    // —— Cálculos auxiliares para view.xhtml ——————————————————

    /** Suma de los subtotales de cada detalle (sin IVA) */
    public double getSubtotal() {
        if (factura == null || factura.getDetalles() == null) {
            return 0.0;
        }
        return factura.getDetalles()
                      .stream()
                      .mapToDouble(DetalleFactura::getSubtotal)
                      .sum();
    }

    /** IVA al 12% sobre el subtotal */
    public double getIva() {
        return getSubtotal() * 0.12;
    }

    /** Total incluyendo IVA */
    public double getTotalConIva() {
        return getSubtotal() + getIva();
    }
}
