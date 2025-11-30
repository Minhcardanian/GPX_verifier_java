package org.trail.attemptverifier.model;

import jakarta.persistence.*;

@Entity
@Table(name = "runners")
public class Runner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String runnerId;

    public Runner() {}

    public Runner(String runnerId) {
        this.runnerId = runnerId;
    }

    public Long getId() {
        return id;
    }

    public String getRunnerId() {
        return runnerId;
    }

    public void setRunnerId(String runnerId) {
        this.runnerId = runnerId;
    }
}
