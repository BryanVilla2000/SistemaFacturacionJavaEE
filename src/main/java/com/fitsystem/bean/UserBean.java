package com.fitsystem.bean;

import com.fitsystem.model.Usuario;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;

@Named("userBean")
@ViewScoped
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "fitsystemPU")
    private transient EntityManager em;

    private Usuario usuario = new Usuario();
    private List<Usuario> usuarios;

    @PostConstruct
    public void init() {
        usuarios = em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
    }

    public String crearUsuario() {
        usuario = new Usuario();
        return "/views/users/form.xhtml?faces-redirect=true";
    }

    @Transactional
    public String agregarUsuario() {
        usuario.setPassword(hashPassword(usuario.getPassword()));
        em.persist(usuario);
        return "/views/users/list.xhtml?faces-redirect=true";
    }

    public String editarUsuario(Usuario u) {
        return "/views/users/form.xhtml?faces-redirect=true&id=" + u.getId_usuario();
    }

    @Transactional
    public String modificarUsuario() {
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            usuario.setPassword(hashPassword(usuario.getPassword()));
        } else {
            // si no cambió la contraseña, recupérala del original
            Usuario original = em.find(Usuario.class, usuario.getId_usuario());
            usuario.setPassword(original.getPassword());
        }
        em.merge(usuario);
        return "/views/users/list.xhtml?faces-redirect=true";
    }

    @Transactional
    public String eliminarUsuario(Usuario u) {
        Usuario toRemove = em.merge(u);
        em.remove(toRemove);
        return "/views/users/list.xhtml?faces-redirect=true";
    }

    public void loadUsuario(ComponentSystemEvent event) {
        Long id = usuario.getId_usuario();
        if (id != null) {
            usuario = em.find(Usuario.class, id);
            usuario.setPassword(""); // limpia el campo para no mostrar el hash
        }
    }

    // ------ hashing de contraseña ------
    private String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            // convertir a hex
            Formatter fmt = new Formatter();
            for (byte b : hash) {
                fmt.format("%02x", b);
            }
            return fmt.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No se pudo encriptar la contraseña", e);
        }
    }
    // -----------------------------------

    // Getters y setters

    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    public List<Usuario> getUsuarios() {
        return usuarios;
    }
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
