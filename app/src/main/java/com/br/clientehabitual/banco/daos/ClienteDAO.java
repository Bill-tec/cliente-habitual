package com.br.clientehabitual.banco.daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.br.clientehabitual.banco.GerenciarBanco;
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
    public long cadastrarCliente(Cliente cliente){
        banco = gerenciarBanco.getWritableDatabase();
        ContentValues dados = new ContentValues();
        dados.put(campos[1], cliente.getNome());
        dados.put(campos[2], cliente.getEmail());
        long result = banco.insert(nomeTabela, null, dados);
        banco.close();
        return result;
    }
    public ArrayList<Cliente> listaClientes(){
        ArrayList<Cliente> clientes = new ArrayList<>();
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
    public Cliente getClienteId(Cliente cliente){
        String where = campos[0]+ " = " + cliente.getId();
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        Cursor cursor = db.query(nomeTabela, campos, where, null, null, null, null);
        if(cursor.moveToNext()){
            cliente = new Cliente(cursor.getInt(0),
                cursor.getString(1), cursor.getString(2));
        }db.close();
        return cliente;
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
