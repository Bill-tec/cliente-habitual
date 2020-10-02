package com.br.clientehabitual.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.br.clientehabitual.R;
import com.br.clientehabitual.models.Produto;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ProdutoAdapter extends ArrayAdapter<Produto> {
    private final Context context;
    private final ArrayList<Produto> produtos;
    public ProdutoAdapter(Context context, ArrayList<Produto> produtos){
        super(context, R.layout.modelo_lista_produtos, produtos);
        this.context = context;
        this.produtos = produtos;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.modelo_lista_produtos, parent, false);

        TextView textViewQuantidade = rowView.findViewById(R.id.lst_produto_qtd);
        TextView textViewNome = rowView.findViewById(R.id.lst_produto_nome);
        TextView textViewPrecoUnd = rowView.findViewById(R.id.lst_produto_preco_und);
        TextView textViewPrecoTotal = rowView.findViewById(R.id.lst_produto_preco_total);

        DecimalFormat df = new DecimalFormat("#0,00");

        textViewQuantidade.setText(Integer.toString(produtos.get(position).getQuantidade()));
        textViewNome.setText(produtos.get(position).getNome());
        textViewPrecoUnd.setText(df.format(produtos.get(position).getPreco()));
        textViewPrecoTotal.setText(df.format(produtos.get(position).getQuantidade() *
                produtos.get(position).getPreco()));

        return rowView;
    }
}
