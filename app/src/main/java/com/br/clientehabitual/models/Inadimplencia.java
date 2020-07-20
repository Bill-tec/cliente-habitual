package com.br.clientehabitual.models;

import java.util.ArrayList;
import java.util.Calendar;

public class Inadimplencia {
    private int id;
    private Calendar dataInicio;
    private Calendar dataFim;
    private Cliente cliente;
    private boolean quitada;
    private ArrayList<Produto> produtos;
    private float total;

    public Inadimplencia(int id, Calendar dataInicio, Calendar dataFim, Cliente cliente, boolean quitada, ArrayList<Produto> produtos) {
        this.id = id;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.cliente = cliente;
        this.quitada = quitada;
        this.produtos = produtos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Calendar getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Calendar dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Calendar getDataFim() {
        return dataFim;
    }

    public void setDataFim(Calendar dataFim) {
        this.dataFim = dataFim;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public boolean isQuitada() {
        return quitada;
    }

    public void setQuitada(boolean quitada) {
        this.quitada = quitada;
    }

    public ArrayList<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(ArrayList<Produto> produtos) {
        this.produtos = produtos;
    }
}
