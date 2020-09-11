package com.br.clientehabitual.banco.daos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.br.clientehabitual.banco.GerenciarBanco;
import com.br.clientehabitual.models.Cliente;
import com.br.clientehabitual.models.Inadimplencia;
import com.br.clientehabitual.util.Conversoes;

public class InadimplenciaDAO {
    private SQLiteDatabase banco;
    private GerenciarBanco gerenciarBanco;
    private static final String[] campos = {"id", "dataInicio", "dataFim", "clienteId", "quitada","total"};
    private static final String nomeTabela = "inadimplencias";
    private Conversoes converter = new Conversoes();

    public InadimplenciaDAO(Context context) {
        gerenciarBanco = new GerenciarBanco(context);
    }
    public Inadimplencia newInadimplencia(Inadimplencia inadimplencia){
        banco = gerenciarBanco.getWritableDatabase();
        ContentValues dados = new ContentValues();
        dados.put(campos[1], converter.calendarToString(inadimplencia.getDataInicio()));
        dados.put(campos[3], inadimplencia.getCliente().getId());
        dados.put(campos[4], 0);
        dados.put(campos[5], inadimplencia.getTotal());
        inadimplencia.setId(banco.insert(nomeTabela,null,dados));
        banco.close();
        return inadimplencia;
    }
    public Inadimplencia getInadimpleciaCliente(Cliente cliente){
        Inadimplencia inadimplencia = null;
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        String where = campos[3] + " = "+cliente.getId();
        Cursor cursor = db.query(nomeTabela, campos,where, null,null,null,null);
        if (cursor.moveToNext()) {
            inadimplencia = new Inadimplencia(cursor.getLong(0),converter.stringToCalendar(
                    cursor.getString(1)) , converter.stringToCalendar(cursor.getString(2)),
                    cliente, false, cursor.getFloat(5));
            if (cursor.getInt(4) == 1){
                inadimplencia.setQuitada(true);
            }
        }
        return inadimplencia;
    }
    public void setDataPagamentoInadimplencia(Inadimplencia inad){
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        String where = campos[0] + " = "+ inad.getId();
        ContentValues dados = new ContentValues();
        dados.put(campos[2], converter.calendarToString(inad.getDataFim()));
        db.update(nomeTabela,dados,where,null);
        db.close();
    }public void setValorTotal(Inadimplencia inad){
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        String where = campos[0] + " = "+ inad.getId();
        ContentValues dados = new ContentValues();
        dados.put(campos[5], inad.getTotal());
        db.update(nomeTabela,dados,where,null);
        db.close();
    }public void setQuitInadimplencia(Inadimplencia inad){
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        String where = campos[0] + " = "+ inad.getId();
        ContentValues dados = new ContentValues();
        dados.put(campos[4], 1);
        db.update(nomeTabela,dados,where,null);
        db.close();
    }
    public void deleteInadimplencia(Inadimplencia inad) {
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        String where = campos[0] + " = " + inad.getId();
        db.delete(nomeTabela, where, null);
        db.close();
    }

}
