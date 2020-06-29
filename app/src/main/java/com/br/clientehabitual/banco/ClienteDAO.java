package com.br.clientehabitual.banco;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.br.clientehabitual.models.Cliente;

import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    private SQLiteDatabase banco;
    private GerenciarBanco gerenciarBanco;
    private static final String[] campos = {"id", "nome", "email"};
    private static final String nomeTabela = "clientes";

    public ClienteDAO(Context context){
        gerenciarBanco = new GerenciarBanco(context);
    }
    public void cadastrarCliente(Cliente cliente){
        banco = gerenciarBanco.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(campos[1], cliente.getNome());
        valores.put(campos[2], cliente.getEmail());
        banco.insert(nomeTabela, null, valores);
        banco.close();
    }
    public List<Cliente> listaClientes(){
        List<Cliente> clientes = new ArrayList<>();
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        Cursor cursor = db.query(nomeTabela, campos, null, null, null, null, "nome ASC");
        while(cursor.moveToNext()){
            Cliente c = new Cliente(cursor.getInt(0),
                    cursor.getString(1),cursor.getString(2));
            clientes.add(c);
        };
        db.close();
        return clientes;
    }
    public Cliente listaClientesId(Cliente cliente){
        String where = campos[0]+ " = " + cliente.getId();
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        Cursor cursor = db.query(nomeTabela, campos, where, null, null, null, "nome ASC");
        if (cursor != null) {
            cliente = new Cliente(cursor.getInt(0),
                    cursor.getString(1), cursor.getString(2));
        }else{
            cliente = null;
        }
        db.close();
        return cliente;
    }
    public List<Cliente> listaClientesNome(Cliente cliente){
        List<Cliente> clientes = new ArrayList<>();
        String where = campos[1] + " LIKE '%" + cliente.getNome()+ "%'";
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        Cursor cursor = db.query(nomeTabela, campos, where, null, null, null, "id ASC");
        while(cursor.moveToNext()){
            Cliente c = new Cliente(cursor.getInt(0),
                    cursor.getString(1),cursor.getString(2));
            clientes.add(c);
        };
        db.close();
        return clientes;
    }
}
