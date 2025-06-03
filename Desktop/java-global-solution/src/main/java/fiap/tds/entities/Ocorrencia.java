package fiap.tds.entities; // Ou o pacote que você estiver usando

import java.math.BigDecimal;
import java.time.LocalDateTime; // Usando LocalDateTime para Timestamp
import java.util.Objects;

public class Ocorrencia {

    private int idOcorrencia;
    private boolean deleted;
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private String cep;
    private String tipoOcorrencia;
    private String nivelGravidade;
    private BigDecimal lat;
    private BigDecimal lon;
    private int idCidade;

    // Construtores
    public Ocorrencia() {
    }

    public Ocorrencia(String cep, LocalDateTime dataCriacao, boolean deleted, int idCidade, int idOcorrencia, BigDecimal lat, BigDecimal lon, String nivelGravidade, String tipoOcorrencia) {
        this.cep = cep;
        this.dataCriacao = dataCriacao;
        this.deleted = deleted;
        this.idCidade = idCidade;
        this.idOcorrencia = idOcorrencia;
        this.lat = lat;
        this.lon = lon;
        this.nivelGravidade = nivelGravidade;
        this.tipoOcorrencia = tipoOcorrencia;
    }

    // Getters e Setters
    public int getIdOcorrencia() {
        return idOcorrencia;
    }

    public void setIdOcorrencia(int idOcorrencia) {
        this.idOcorrencia = idOcorrencia;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public int getIdCidade() {
        return idCidade;
    }

    public void setIdCidade(int idCidade) {
        this.idCidade = idCidade;
    }

    public String getTipoOcorrencia() {
        return tipoOcorrencia;
    }

    public void setTipoOcorrencia(String tipoOcorrencia) {
        this.tipoOcorrencia = tipoOcorrencia;
    }

    public String getNivelGravidade() {
        return nivelGravidade;
    }

    public void setNivelGravidade(String nivelGravidade) {
        this.nivelGravidade = nivelGravidade;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }
}