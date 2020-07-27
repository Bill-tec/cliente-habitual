package com.br.clientehabitual.banco;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.br.clientehabitual.models.Inadimplencia;
import com.br.clientehabitual.models.Produto;

import java.io.Serializable;
import java.util.ArrayList;

public class ProdutoDAO implements Serializable {
    private SQLiteDatabase banco;
    private GerenciarBanco gerenciarBanco;
    private static final String[] camposTodos = {"id","inadimplenciaId", "nome", "preco","quantidade"};
    private static final String nomeTabela = "produtos";

    public ProdutoDAO(Context context){
        gerenciarBanco = new GerenciarBanco(context);
    }
    public void addProdutos(Inadimplencia inadimplencia){
        ArrayList<Produto> produtos = inadimplencia.getProdutos();
        banco = gerenciarBanco.getWritableDatabase();
        for (Produto p:produtos) {
            ContentValues dados = new ContentValues();
            dados.put(camposTodos[1], inadimplencia.getId());
            dados.put(camposTodos[2], p.getNome());
            dados.put(camposTodos[3], p.getPreco());
            dados.put(camposTodos[4], p.getQuantidade());
            banco.insert(nomeTabela,null, dados);
        }
        banco.close();
    }
    public Inadimplencia listProdutosInad(Inadimplencia inad){
        ArrayList<Produto> produtos = new ArrayList<>();
        String where = camposTodos[1] +" = "+ inad.getId();
        String[] campos = {"id", "nome", "preco","quantidade"};
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        Cursor cursor = db.query(nomeTabela,campos,where,null,null,null,"id ASC");
        db.close();
        while (cursor.moveToNext()){
            Produto p = new Produto(cursor.getInt(0),cursor.getString(1),
                    cursor.getFloat(2),cursor.getInt(3));
            produtos.add(p);
        }
        inad.setProdutos(produtos);
        return inad;
    }
    public void deleteProdutoId(Produto produto){
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        String where = camposTodos[0]+" = "+ produto.getId();
        db.delete(nomeTabela,where,null);
        db.close();
    }
    public void deleteAllProdutoInadimplencia(Inadimplencia inad){
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        String where = camposTodos[1]+" = "+ inad.getId();
        db.delete(nomeTabela,where,null);
        db.close();
    }
    public void updateProduto(Produto produto){
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        String where = camposTodos[0]+" = "+ produto.getId();
        ContentValues dados = new ContentValues();
        dados.put(camposTodos[2], produto.getNome());
        dados.put(camposTodos[3], produto.getPreco());
        dados.put(camposTodos[4], produto.getQuantidade());
        db.update(nomeTabela,dados,where,null);
        db.close();
    }
}
