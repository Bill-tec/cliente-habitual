package com.br.clientehabitual.banco;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.br.clientehabitual.models.Cliente;
import com.br.clientehabitual.models.Inadimplencia;
import com.br.clientehabitual.models.Produto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class InadimplenciaDAO {
    private SQLiteDatabase banco;
    private GerenciarBanco gerenciarBanco;
    private static final String[] campos = {"id", "dataCriacao", "dataBaixa", "clienteId", "situacao"};
    private static final String nomeTabela = "inadimplencias";

    public InadimplenciaDAO(Context context) {
        gerenciarBanco = new GerenciarBanco(context);
    }
    public Inadimplencia newInadimplencia(Inadimplencia inadimplencia){
        Cliente cliente = inadimplencia.getCliente();
        ArrayList<Produto> produtos = inadimplencia.getProdutos();
        banco = gerenciarBanco.getWritableDatabase();
        try{
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            SimpleDateFormat dataSimple = new SimpleDateFormat("dd-MM-yyyy");
            String dataInicio = dataSimple.format(date);
            ContentValues dados = new ContentValues();
            dados.put(campos[1], dataInicio);
            dados.put(campos[3], cliente.getId());
            dados.put(campos[4], "false");
            inadimplencia.setId(banco.insert(nomeTabela,null,dados));
            banco.close();
        } catch (Exception e){}
        return inadimplencia;
    }
    public List<Inadimplencia> listInadimplecias(){
        Calendar data = Calendar.getInstance();
        Inadimplencia inadimplencia = null;
        List<Inadimplencia> inadimplencias = new ArrayList<>();
        SQLiteDatabase db = gerenciarBanco.getReadableDatabase();
        Cursor cursor = db.query(nomeTabela, campos,null, null,null,null,null);
            while (cursor.moveToNext()) {
                Cliente cliente = new Cliente(cursor.getInt(3), "", "");
                inadimplencia.setCliente(cliente);
                try {
                    SimpleDateFormat dataSimple = new SimpleDateFormat("dd-MM-yyyy");
                    data.setTime(dataSimple.parse(cursor.getString(1)));
                    inadimplencia.setDataInicio(data);

                    data.setTime(dataSimple.parse(cursor.getString(2)));
                    inadimplencia.setDataFim(data);
                } catch (Exception e) {}
                inadimplencia.setId(cursor.getInt(0));
                inadimplencia.setQuitada(Boolean.getBoolean(cursor.getString(4)));
                inadimplencias.add(inadimplencia);
            }
        return inadimplencias;
    }
}
