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
    public boolean cadastrarCliente(Cliente cliente){
        banco = gerenciarBanco.getWritableDatabase();
        ContentValues dados = new ContentValues();
        dados.put(campos[1], cliente.getNome());
        dados.put(campos[2], cliente.getEmail());
        long result = banco.insert(nomeTabela, null, dados);
        banco.close();
        return result > 0;
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
        cursor.close();
        db.close();
        return clientes;
    }
    public Cliente listaClientesId(Cliente cliente){
        String where = campos[0]+ " = " + cliente.getId();
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        Cursor cursor = db.query(nomeTabela, campos, where, null, null, null, "nome ASC");
        cliente = new Cliente(cursor.getInt(0),
                cursor.getString(1), cursor.getString(2));
        db.close();
        return cliente;
    }
    public List<Cliente> listaClientesNome(Cliente cliente){
        List<Cliente> clientes = new ArrayList<>();
        String where = campos[1] + " LIKE '%" + cliente.getNome()+ "%'";
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        Cursor cursor = db.query(nomeTabela, campos, where, null, null, null, "id ASC");
        while(cursor.moveToNext()){
            cliente = new Cliente(cursor.getInt(0),
                    cursor.getString(1),cursor.getString(2));
            clientes.add(cliente);
        };
        db.close();
        return clientes;
    }
    public void atualizarCliente(Cliente cliente){
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        String where = campos[0] + " =" + cliente.getId();
        ContentValues dados = new ContentValues();
        dados.put(campos[1], cliente.getNome());
        dados.put(campos[2], cliente.getEmail());
        db.update(nomeTabela, dados, where,null);
        db.close();
    }
    public void deleteCliente(Cliente cliente) {
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        String where = campos[0] +"= "+cliente.getId();
        db.delete(nomeTabela, where,null);
        db.close();
    }
}
