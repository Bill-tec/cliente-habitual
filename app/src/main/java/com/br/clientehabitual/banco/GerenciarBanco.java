package com.br.clientehabitual.banco;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GerenciarBanco extends SQLiteOpenHelper {
    public static final String NOME_BANCO = "bancoDeDados.db";
    public static final int VERSAO = 1;
    public GerenciarBanco(Context context){
        super(context,NOME_BANCO,null,VERSAO);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        String clientesSql="CREATE TABLE clientes(id integer primary key autoincrement, nome text, " +
                "email text)";

        String inadimpleciasSql="CREATE TABLE inadimplencias(id integer primary key autoincrement, dataInicio text, " +
                "dataFim text, clienteId integer , quitada integer, total real, foreign key (clienteId) references clientes(id))";

        String produtosSql="CREATE TABLE produtos(id integer primary key autoincrement," +
                "inadimplenciaId integer references inadimplencias(id),nome text, preco real, quantidade integer)";

        String produtosListaSql="CREATE TABLE lista_produtos(id integer primary key autoincrement,nome text)";
        db.execSQL(clientesSql);
        db.execSQL(inadimpleciasSql);
        db.execSQL(produtosSql);
        db.execSQL(produtosListaSql);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        db.execSQL("DROP TABLE IF EXISTS clientes");
        db.execSQL("DROP TABLE IF EXISTS inadimplencias");
        db.execSQL("DROP TABLE IF EXISTS produtos");
        db.execSQL("DROP TABLE IF EXISTS lista_produtos");
        onCreate(db);
    }}
