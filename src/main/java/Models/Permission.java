package Models;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "permission", uniqueConstraints = {
        @UniqueConstraint(name = "uk_permission_profil_module", columnNames = {"profil_id", "module_id"})
}, indexes = {
        @Index(name = "idx_permission_profil_id", columnList = "profil_id"),
        @Index(name = "idx_permission_module_id", columnList = "module_id")
})
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPrivilege;

    @Column(nullable = false)
    private Boolean canCreate = false;

    @Column(nullable = false)
    private Boolean canRead = false;

    @Column(nullable = false)
    private Boolean canUpdate = false;

    @Column(nullable = false)
    private Boolean canDelete = false;

    @Column(nullable = false)
    private Boolean canExecuted = false;

    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profil_id", nullable = false)
    private Profil profil;

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    public Long getIdPrivilege() {
        return idPrivilege;
    }

    public void setIdPrivilege(Long idPrivilege) {
        this.idPrivilege = idPrivilege;
    }

    public Boolean getCanCreate() {
        return canCreate;
    }

    public void setCanCreate(Boolean canCreate) {
        this.canCreate = canCreate;
    }

    public Boolean getCanRead() {
        return canRead;
    }

    public void setCanRead(Boolean canRead) {
        this.canRead = canRead;
    }

    public Boolean getCanUpdate() {
        return canUpdate;
    }

    public void setCanUpdate(Boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    public Boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }

    public Boolean getCanExecuted() {
        return canExecuted;
    }

    public void setCanExecuted(Boolean canExecuted) {
        this.canExecuted = canExecuted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Profil getProfil() {
        return profil;
    }

    public void setProfil(Profil profil) {
        this.profil = profil;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }
}


