package com.br.clientehabitual.banco;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.br.clientehabitual.models.Cliente;

public class ClienteDAO {
    private SQLiteDatabase banco;
    private GerenciarBanco gerenciarBanco;

    public ClienteDAO(Context context){
        gerenciarBanco = new GerenciarBanco(context);
    }
    public boolean cadastrarCliente(Cliente cliente){
        banco = gerenciarBanco.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nome", cliente.getNome());
        valores.put("email", cliente.getEmail());
        long resultado = banco.insert("clientes", null, valores);
        banco.close();
        return resultado > 0;
    }
    public Cursor obterCliente(){
        String[] campos = {"_id", "nome", "email"};
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        Cursor cursor = db.query("clientes", campos, null, null, null, null, "titulo ASC");
        if(cursor!=null){
            cursor.moveToFirst();
        }
        db.close();
        return cursor;
    }
}
