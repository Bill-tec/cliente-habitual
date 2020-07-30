package com.br.clientehabitual.banco.daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.br.clientehabitual.banco.GerenciarBanco;
import com.br.clientehabitual.models.Produto;

import java.util.ArrayList;

public class ListaDAO {
    private SQLiteDatabase banco;
    private GerenciarBanco gerenciarBanco;
    private static final String[] camposTodos = {"id", "nome"};
    private static final String nomeTabela = "lista_produtos";

    public ListaDAO(Context context){
        gerenciarBanco = new GerenciarBanco(context);
    }
    public void addProdutosListagem(Produto produto){
        banco = gerenciarBanco.getWritableDatabase();
            ContentValues dados = new ContentValues();
            dados.put(camposTodos[1], produto.getNome());
            banco.insert(nomeTabela,null, dados);
        banco.close();
    }
    public ArrayList<Produto> listProdutosListagem(){
        ArrayList<Produto> produtos = new ArrayList<>();
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        Cursor cursor = db.query(nomeTabela,camposTodos,null,null,null,null,null);
        while (cursor.moveToNext()){
            Produto p = new Produto(cursor.getInt(0), cursor.getString(1),0,0);
            produtos.add(p);
        }
        db.close();
        return produtos;
    }
    public void deleteProdutoListagem(Produto p){
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        String where = camposTodos[0] + " = " + p.getId();
        db.delete(nomeTabela,where,null);
        db.close();
    }
}
