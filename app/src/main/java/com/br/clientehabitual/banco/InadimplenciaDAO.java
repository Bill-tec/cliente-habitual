package com.br.clientehabitual.banco;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.br.clientehabitual.models.Inadimplencia;

public class InadimplenciaDAO {
    private SQLiteDatabase banco;
    private GerenciarBanco gerenciarBanco;
    private static final String[] campos = {"id", "dataCriacao", "dataBaixa", "clienteId", "situacao", "produtoNome", "produtoPreco","quantidade", "total"};
    private static final String nomeTabela = "inadimplencias";

    public InadimplenciaDAO(Context context) {
        gerenciarBanco = new GerenciarBanco(context);
    }

}
