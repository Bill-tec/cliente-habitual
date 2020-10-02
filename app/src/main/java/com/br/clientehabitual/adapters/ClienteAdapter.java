package com.br.clientehabitual.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.br.clientehabitual.R;
import com.br.clientehabitual.banco.daos.InadimplenciaDAO;
import com.br.clientehabitual.models.Cliente;
import com.br.clientehabitual.models.Inadimplencia;
import com.br.clientehabitual.util.Conversoes;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ClienteAdapter extends ArrayAdapter<Cliente>{
    private final Context context;
    private final ArrayList<Cliente> clientes;
    private Conversoes conversoes = new Conversoes();

    public ClienteAdapter(Context context, ArrayList<Cliente> clientes){
        super(context, R.layout.modelo_lista_clientes, clientes);
        this.context = context;
        this.clientes = clientes;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.modelo_lista_clientes, parent, false);

        TextView textViewNome = rowView.findViewById(R.id.nome);
        TextView textViewTotal = rowView.findViewById(R.id.total);
        TextView textViewData = rowView.findViewById(R.id.data_pagamento);

        textViewNome.setText(clientes.get(position).getNome());

        DecimalFormat df = new DecimalFormat("#0,00");
        InadimplenciaDAO inadimplenciaDAO = new InadimplenciaDAO(context);
        Inadimplencia inadimplencia = inadimplenciaDAO.getInadimpleciaCliente(clientes.get(position));

        if (inadimplencia != null){
            if (inadimplencia.isQuitada()){
                textViewTotal.setText("Sem dividas!");
                textViewTotal.setTextColor(Color.GREEN);
                textViewNome.setTextColor(Color.GREEN);
            } else if(inadimplencia.getDataFim() != null){
                Calendar calendar = Calendar.getInstance();
                if (calendar.getTime().after(inadimplencia.getDataFim().getTime())){
                    textViewTotal.setTextColor(Color.RED);
                    textViewData.setTextColor(Color.RED);
                    textViewNome.setTextColor(Color.RED);
                    textViewTotal.setText("R$ "+df.format(inadimplencia.getTotal()));
                    textViewData.setText(conversoes.calendarToString(inadimplencia.getDataFim()));
                } else {
                    textViewTotal.setText("R$ "+df.format(inadimplencia.getTotal()));
                    textViewData.setText(conversoes.calendarToString(inadimplencia.getDataFim()));
                }
            } else{
                textViewTotal.setText("R$ "+df.format(inadimplencia.getTotal()));
            }
        }
        return rowView;
    }
}