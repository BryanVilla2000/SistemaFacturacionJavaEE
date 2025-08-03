package com.fitsystem.bean;

import com.fitsystem.model.Cliente;
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

@Named("clients")
@ViewScoped
public class Clients implements Serializable {
    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "fitsystemPU")
    private transient EntityManager em;

    /** El cliente que estamos editando o creando */
    private Cliente cliente = new Cliente();
    /** La lista de todos los clientes */
    private List<Cliente> clientes;

    @PostConstruct
    public void init() {
        // Cargo la lista al arrancar
        clientes = em.createQuery("SELECT c FROM Cliente c", Cliente.class)
                     .getResultList();
    }

    /** Navega a form.xhtml para dar de alta */
    public String crearCliente() {
        cliente = new Cliente();
        return "/views/clients/form.xhtml?faces-redirect=true";
    }

    /** Persiste un nuevo cliente con validación de cédula única */
    @Transactional
    public String agregarCliente() {
        // 1) Chequeo previo de unicidad de cédula
        TypedQuery<Long> q = em.createQuery(
            "SELECT COUNT(c) FROM Cliente c WHERE c.cedula = :cedula", Long.class);
        q.setParameter("cedula", cliente.getCedula());
        Long count = q.getSingleResult();
        if (count > 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Ya existe un cliente con esa cédula",
                                 null));
            return null;
        }

        // 2) Persistir
        try {
            em.persist(cliente);
            return "/views/clients/list.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Error al guardar el cliente",
                                 null));
            return null;
        }
    }

    /** Redirige a form.xhtml con ?id= para edición */
    public String editarCliente(Cliente c) {
        return "/views/clients/form.xhtml?faces-redirect=true&id=" + c.getId_cliente();
    }

    /** Actualiza el cliente editado con validación de cédula única */
    @Transactional
    public String modificarCliente() {
        // 1) Chequeo de unicidad excluyendo al propio registro
        TypedQuery<Long> q = em.createQuery(
            "SELECT COUNT(c) FROM Cliente c WHERE c.cedula = :cedula AND c.id_cliente <> :id", Long.class);
        q.setParameter("cedula", cliente.getCedula());
        q.setParameter("id", cliente.getId_cliente());
        if (q.getSingleResult() > 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Ya existe otro cliente con esa cédula",
                                 null));
            return null;
        }

        // 2) Merge
        try {
            em.merge(cliente);
            return "/views/clients/list.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Error al modificar el cliente",
                                 null));
            return null;
        }
    }

    /** Elimina el cliente */
    @Transactional
    public String eliminarCliente(Cliente c) {
        Cliente toRemove = em.merge(c);
        em.remove(toRemove);
        return "/views/clients/list.xhtml?faces-redirect=true";
    }

    /**
     * Listener antes de renderizar form.xhtml: si viene id, carga el cliente
     */
    public void loadCliente(ComponentSystemEvent event) {
        Long id = cliente.getId_cliente();
        if (id != null) {
            cliente = em.find(Cliente.class, id);
        }
    }

    // — Getters y setters —

    public Cliente getCliente() {
        return cliente;
    }
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }
    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }
}
