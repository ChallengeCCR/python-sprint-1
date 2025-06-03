package fiap.tds.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Abrigo {

    private int idAbrigo;
    private boolean deleted;
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private String cep;
    private String nomeAbrigo;
    private String enderecoAbrigo;
    private int capacidadeMaxima;
    private String statusFuncionamento;
    private String nivelSegurancaAtual;
    private String telefoneContato;
    private int idCidade;
    private BigDecimal lat;
    private BigDecimal lon;

    // Construtores
    public Abrigo() {
    }

    public Abrigo(int capacidadeMaxima, String cep, LocalDateTime dataCriacao, boolean deleted, String enderecoAbrigo, int idAbrigo, int idCidade, BigDecimal lat, BigDecimal lon, String nivelSegurancaAtual, String nomeAbrigo, String statusFuncionamento, String telefoneContato) {
        this.capacidadeMaxima = capacidadeMaxima;
        this.cep = cep;
        this.dataCriacao = dataCriacao;
        this.deleted = deleted;
        this.enderecoAbrigo = enderecoAbrigo;
        this.idAbrigo = idAbrigo;
        this.idCidade = idCidade;
        this.lat = lat;
        this.lon = lon;
        this.nivelSegurancaAtual = nivelSegurancaAtual;
        this.nomeAbrigo = nomeAbrigo;
        this.statusFuncionamento = statusFuncionamento;
        this.telefoneContato = telefoneContato;
    }

    // Getters and Setters
    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public void setCapacidadeMaxima(int capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
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

    public String getEnderecoAbrigo() {
        return enderecoAbrigo;
    }

    public void setEnderecoAbrigo(String enderecoAbrigo) {
        this.enderecoAbrigo = enderecoAbrigo;
    }

    public int getIdAbrigo() {
        return idAbrigo;
    }

    public void setIdAbrigo(int idAbrigo) {
        this.idAbrigo = idAbrigo;
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

    public String getNivelSegurancaAtual() {
        return nivelSegurancaAtual;
    }

    public void setNivelSegurancaAtual(String nivelSegurancaAtual) {
        this.nivelSegurancaAtual = nivelSegurancaAtual;
    }

    public String getNomeAbrigo() {
        return nomeAbrigo;
    }

    public void setNomeAbrigo(String nomeAbrigo) {
        this.nomeAbrigo = nomeAbrigo;
    }

    public String getStatusFuncionamento() {
        return statusFuncionamento;
    }

    public void setStatusFuncionamento(String statusFuncionamento) {
        this.statusFuncionamento = statusFuncionamento;
    }

    public String getTelefoneContato() {
        return telefoneContato;
    }

    public void setTelefoneContato(String telefoneContato) {
        this.telefoneContato = telefoneContato;
    }

    public int getIdCidade() {
        return idCidade;
    }

    public void setIdCidade(int idCidade) {
        this.idCidade = idCidade;
    }
}