package fiap.tds.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Cidade {
    private int idCidade;
    private boolean deleted;
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private String cepCidade;
    private String nomeCidade;
    private int quantidadeOcorrencias;
    private int quantidadeAbrigos;
    private BigDecimal lat;
    private BigDecimal lon;

    // Construtores
    public Cidade() {
    }

    public Cidade(String cepCidade, LocalDateTime dataCriacao, boolean deleted, int idCidade, BigDecimal lat, BigDecimal lon, String nomeCidade, int quantidadeAbrigos, int quantidadeOcorrencias) {
        this.cepCidade = cepCidade;
        this.dataCriacao = dataCriacao;
        this.deleted = deleted;
        this.idCidade = idCidade;
        this.lat = lat;
        this.lon = lon;
        this.nomeCidade = nomeCidade;
        this.quantidadeAbrigos = quantidadeAbrigos;
        this.quantidadeOcorrencias = quantidadeOcorrencias;
    }

    // Getters and Setters
    public String getCepCidade() {
        return cepCidade;
    }

    public void setCepCidade(String cepCidade) {
        this.cepCidade = cepCidade;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getIdCidade() {
        return idCidade;
    }

    public void setIdCidade(int idCidade) {
        this.idCidade = idCidade;
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

    public String getNomeCidade() {
        return nomeCidade;
    }

    public void setNomeCidade(String nomeCidade) {
        this.nomeCidade = nomeCidade;
    }

    public int getQuantidadeAbrigos() {
        return quantidadeAbrigos;
    }

    public void setQuantidadeAbrigos(int quantidadeAbrigos) {
        this.quantidadeAbrigos = quantidadeAbrigos;
    }

    public int getQuantidadeOcorrencias() {
        return quantidadeOcorrencias;
    }

    public void setQuantidadeOcorrencias(int quantidadeOcorrencias) {
        this.quantidadeOcorrencias = quantidadeOcorrencias;
    }
}
