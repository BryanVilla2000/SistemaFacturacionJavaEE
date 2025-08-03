package com.fitsystem.bean;

import com.fitsystem.model.Usuario;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;

@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;  // contraseña en texto plano ingresada
    private Usuario usuarioAutenticado;

    @PersistenceContext(unitName = "fitsystemPU")
    private transient EntityManager em;

    /**  
     * Intenta autenticar al usuario: busca por username, compara hashes  
     */
    @Transactional
    public String login() {
        // 1) Buscar usuario por nombre de usuario
        List<Usuario> resultados = em.createQuery(
            "SELECT u FROM Usuario u WHERE u.username = :username", Usuario.class)
            .setParameter("username", username)
            .getResultList();

        if (resultados.isEmpty()) {
            FacesContext.getCurrentInstance()
                .addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                     "Usuario o contraseña incorrectos",
                                     null));
            return null;
        }

        Usuario u = resultados.get(0);
        // 2) Hashear la contraseña ingresada
        String hashedInput = hashPassword(password);

        // 3) Comparar hashes
        if (!hashedInput.equals(u.getPassword())) {
            FacesContext.getCurrentInstance()
                .addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                     "Usuario o contraseña incorrectos",
                                     null));
            return null;
        }

        // 4) Autenticación exitosa: guardar en sesión
        usuarioAutenticado = u;
        HttpSession session = (HttpSession)
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSession(true);
        session.setAttribute("authBean", this);

        // 5) Redirigir al dashboard
        return "/views/dashboard.xhtml?faces-redirect=true";
    }

    /**
     * Cierra la sesión y limpia datos
     */
    public String logout() {
        HttpSession session = (HttpSession)
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSession(false);
        if (session != null) {
            session.invalidate();
        }
        usuarioAutenticado = null;
        username = null;
        password = null;
        return "/login.xhtml?faces-redirect=true";
    }

    /**
     * Hashea una contraseña en claro usando SHA-256
     */
    private String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            Formatter fmt = new Formatter();
            for (byte b : hash) {
                fmt.format("%02x", b);
            }
            return fmt.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al encriptar la contraseña", e);
        }
    }

    // ——— Getters y Setters ———

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    public boolean isLogueado() {
        return usuarioAutenticado != null;
    }
}
