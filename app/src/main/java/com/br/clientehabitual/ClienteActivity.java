package com.br.clientehabitual;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.br.clientehabitual.adapters.ProdutoAdapter;
import com.br.clientehabitual.banco.daos.ClienteDAO;
import com.br.clientehabitual.banco.daos.InadimplenciaDAO;
import com.br.clientehabitual.banco.daos.ProdutoDAO;
import com.br.clientehabitual.models.Cliente;
import com.br.clientehabitual.models.Inadimplencia;
import com.br.clientehabitual.models.Produto;
import com.br.clientehabitual.util.Conversoes;

import java.text.DecimalFormat;
import java.util.Calendar;

public class ClienteActivity extends AppCompatActivity {
    private EditText nome, email,preco, quantidade, date;
    private Button adicionar, limpar;
    private TextView total, dataInicio,textViewdataPagamento;
    private Cliente cliente;
    private ClienteDAO clienteDAO = new ClienteDAO(this);
    private ProdutoDAO produtoDAO = new ProdutoDAO(this);
    private Inadimplencia inadimplencia;
    private InadimplenciaDAO inadimplenciaDAO = new InadimplenciaDAO(this);
    private DecimalFormat df = new DecimalFormat("#.00");

    private Conversoes converter = new Conversoes();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);

        Intent intent = getIntent();
        int id = Integer.parseInt(intent.getStringExtra("id"));
        cliente = new Cliente(id,"","");
        cliente = clienteDAO.getClienteId(cliente);
        setTitle(cliente.getNome());


        inadimplencia = inadimplenciaDAO.getInadimpleciaCliente(cliente);
        if (inadimplencia != null) {
            if (inadimplencia.getDataFim() != null){
                textViewdataPagamento = (TextView)findViewById(R.id.data_pagamento);
                textViewdataPagamento.setText(converter.calendarToString(inadimplencia.getDataFim()).replaceAll("-","/"));
            }
        }
        gerarListaProdutos();

        textViewdataPagamento = (TextView) findViewById(R.id.data_pagamento);
        textViewdataPagamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataPagamentoPopUp();
            }
        });

        adicionar = findViewById(R.id.btn_add);
        adicionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                quantidade = (EditText)findViewById(R.id.produto_quantidade);
                nome = (EditText) findViewById(R.id.produto_nome);
                preco = (EditText) findViewById(R.id.produto_preco);

                Produto produto = new Produto(0, nome.getText().toString().trim(),
                        Float.parseFloat(preco.getText().toString().trim()),
                        Integer.parseInt(quantidade.getText().toString().trim()));

                 if(produto.getQuantidade() <=0 || produto.getPreco() <= 0){
                    Toast.makeText(getApplicationContext(),"Erro, verifique os campos e tente novamente!",Toast.LENGTH_SHORT).show();
                } else {
                     if (inadimplencia == null){
                         Calendar data = Calendar.getInstance();
                         inadimplencia = new Inadimplencia(0,data, null,cliente, false);
                         inadimplencia = inadimplenciaDAO.newInadimplencia(inadimplencia);
                     } else if (inadimplencia.isQuitada() == true){
                         inadimplenciaDAO.deleteInadimplencia(inadimplencia);
                         Calendar data = Calendar.getInstance();
                         inadimplencia = new Inadimplencia(0,data, null,cliente, false);
                         inadimplencia = inadimplenciaDAO.newInadimplencia(inadimplencia);
                     }
                     inadimplencia.setTotal(inadimplencia.getTotal() + (produto.getQuantidade() * produto.getPreco()));
                     inadimplenciaDAO.setValorTotal(inadimplencia);
                     produtoDAO.addProduto(inadimplencia.getId(),produto);
                     quantidade.setText("1");
                     nome.setText("");
                     preco.setText("");
                     gerarListaProdutos();
                }
            }
        });

        limpar = findViewById(R.id.btn_limpar);
        limpar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantidade = (EditText)findViewById(R.id.produto_quantidade);
                nome = (EditText) findViewById(R.id.produto_nome);
                preco = (EditText) findViewById(R.id.produto_preco);

                quantidade.setText("1");
                nome.setText("");
                preco.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_edit_delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sub_cliente_editar:
                popupAtualizarCliente();
                return true;
            case R.id.sub_cliente_delete:
                if (inadimplencia == null){
                    confirmDeleteCliente();
                }
                else if (inadimplencia.isQuitada() == false){
                    Toast.makeText(getApplicationContext(),"Seu cliente ainda tem inadimplência ativa!",Toast.LENGTH_LONG).show();
                }else {
                    confirmDeleteCliente();
                }
                return true;
            case R.id.inadimplencia_baixa:
            popupBaixaInadimplencia();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void popupAtualizarCliente(){
        AlertDialog.Builder dialogBuilder;
        final AlertDialog dialog;
        dialogBuilder = new AlertDialog.Builder(this);
        final  View popupClienteView = getLayoutInflater().inflate(R.layout.popup_cliente,null);
        dialogBuilder.setView(popupClienteView);
        dialog = dialogBuilder.create();
        dialog.show();

        nome = (EditText)popupClienteView.findViewById(R.id.popupNome);
        email = (EditText)popupClienteView.findViewById(R.id.popupEmail);
        nome.setText(cliente.getNome());
        email.setText(cliente.getEmail());
        Button add = popupClienteView.findViewById(R.id.btnProximo);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cliente clienteEdit = new Cliente(cliente.getId(), nome.getText().toString().trim(),
                        email.getText().toString().trim());

                if (cliente.getNome().length() == 0 || cliente.getEmail().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Preencha os campos, e tente novamente!", Toast.LENGTH_SHORT).show();
                } else if (cliente.getNome() ==  clienteEdit.getNome() && cliente.getEmail() == clienteEdit.getEmail()) {
                    Toast.makeText(getApplicationContext(),"Nenhum dado para atualizar!",Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();

                    clienteDAO.atualizarCliente(clienteEdit);
                    cliente = clienteEdit;
                    setTitle(cliente.getNome());
                    Toast.makeText(getApplicationContext(),"Cliente atualizado com sucesso!",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    public void gerarListaProdutos(){
        if (inadimplencia != null){
            ListView lista = findViewById(R.id.lst_produtos);

            dataInicio = (TextView) findViewById(R.id.data_incio);
            dataInicio.setText(converter.calendarToString(inadimplencia.getDataInicio()).replaceAll("-","/"));

            inadimplencia.setProdutos(produtoDAO.listProdutosInad(inadimplencia));
            ArrayAdapter adapter = new ProdutoAdapter(this,inadimplencia.getProdutos());
            lista.setAdapter(adapter);
            total = findViewById(R.id.label_Total);
            total.setText(df.format(inadimplencia.getTotal())+ "R$");
            lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    popupProduto(inadimplencia.getProdutos().get(position));
                }
            });
        }
    }
    public void popupProduto(final Produto produto){
        AlertDialog.Builder dialogBuilder;
        final AlertDialog dialog;
        dialogBuilder = new AlertDialog.Builder(this);
        final  View popupProdutoView = getLayoutInflater().inflate(R.layout.popup_produto,null);
        dialogBuilder.setView(popupProdutoView);
        dialog = dialogBuilder.create();
        dialog.setTitle("Editar Produto");
        dialog.show();
        DecimalFormat df = new DecimalFormat("#.00");

        quantidade = popupProdutoView.findViewById(R.id.popup_produto_quantidade);
        quantidade.setText(Integer.toString(produto.getQuantidade()));

        nome = (EditText)popupProdutoView.findViewById(R.id.popup_produto_nome);
        nome.setText(produto.getNome());

        preco = (EditText)popupProdutoView.findViewById(R.id.popup_produto_preco);
        preco.setText(df.format(produto.getPreco()));

        total = popupProdutoView.findViewById(R.id.popup_total);
        total.setText(df.format(produto.getQuantidade() * produto.getPreco()));

        Button att = popupProdutoView.findViewById(R.id.popup_produto_btn_atualizar);
        att.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Produto produtoEdit = new Produto(produto.getId(), nome.getText().toString().trim(),
                        Float.parseFloat(preco.getText().toString().trim()),
                        Integer.parseInt(quantidade.getText().toString().trim()));

                if (produto.getNome().length() == 0 || produto.getPreco() <= 0 || produto.getQuantidade() <= 0) {
                    Toast.makeText(getApplicationContext(), "Verifique os campos, e tente novamente!", Toast.LENGTH_SHORT).show();
                } else if (produto.getNome() ==  produtoEdit.getNome() && produto.getPreco() == produtoEdit.getPreco() && produto.getQuantidade() == produtoEdit.getQuantidade()) {
                    Toast.makeText(getApplicationContext(),"Nenhum dado para atualizar!",Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    produtoDAO.updateProduto(produtoEdit);
                    inadimplencia.setTotal(inadimplencia.getTotal() + ((produto.getQuantidade() * produto.getPreco())
                        - (produtoEdit.getQuantidade() * produtoEdit.getPreco())));
                    inadimplenciaDAO.setValorTotal(inadimplencia);
                    Toast.makeText(getApplicationContext(),"Produto atualizado com sucesso!",Toast.LENGTH_SHORT).show();
                    gerarListaProdutos();
                }
            }
        });

        Button remover = popupProdutoView.findViewById(R.id.popup_produto_btn_remover);
        remover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteProduto(produto,dialog);
            }
        });
    }
    public void popupBaixaInadimplencia(){
        AlertDialog.Builder dialogBuilder;
        final AlertDialog dialog;
        dialogBuilder = new AlertDialog.Builder(this);
        final  View popupPagamentoView = getLayoutInflater().inflate(R.layout.popup_pagamento_parcial,null);
        dialogBuilder.setView(popupPagamentoView);
        dialog = dialogBuilder.create();
        dialog.setTitle("Baixa parcial");
        dialog.show();

        TextView total = popupPagamentoView.findViewById(R.id.popup_edittext_pagamento_total);
        total.setText(df.format(inadimplencia.getTotal()) +"R$");


        final Button descontar = popupPagamentoView.findViewById(R.id.descontar);
        descontar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (descontar.getText().equals("descontar")){
                    inadimplenciaDAO.setValorTotal(inadimplencia);
                    inadimplencia.setTotal(inadimplencia.getTotal() - Float.parseFloat(preco.getText().toString().trim()));
                    Toast.makeText(getApplicationContext(),"Valor descontado com sucesso!",Toast.LENGTH_SHORT).show();
                } else {
                    inadimplencia.setQuitada(true);
                    inadimplenciaDAO.setQuitInadimplencia(inadimplencia);
                    Toast.makeText(getApplicationContext(),"Inadimplência quitada com sucesso!",Toast.LENGTH_SHORT).show();
                    textViewdataPagamento.setText("");
                }
                dialog.dismiss();
            }
        });

        preco = popupPagamentoView.findViewById(R.id.valor);
        preco.addTextChangedListener(new TextWatcher() {
            TextView resto_total = popupPagamentoView.findViewById(R.id.resto);
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (preco.getText().toString().trim().length() > 0){
                    if (inadimplencia.getTotal() > Float.parseFloat(preco.getText().toString().trim())){
                        resto_total.setText("Resto: " + df.format(inadimplencia.getTotal() - Float.parseFloat(preco.getText().toString().trim())));
                        descontar.setText("Descontar");
                    }else {
                        resto_total.setText("Troco: " + df.format(inadimplencia.getTotal() - Float.parseFloat(preco.getText().toString().trim())).replaceAll("-",""));
                        descontar.setText("Quitar");
                    }
                }else {
                    resto_total.setText("");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        gerarListaProdutos();
    }
    public void confirmDeleteCliente(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Deseja excluir: "+cliente.getNome()+" ?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clienteDAO.deleteCliente(cliente);
                        Toast.makeText(getApplicationContext(),"Cliente removido com sucesso!",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ClienteActivity.this,MainActivity.class);
                        startActivity(intent);
                    }

                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
    public void confirmDeleteProduto(final Produto produto,final AlertDialog dialogProduto){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Deseja remover: "+produto.getNome()+" ?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        produtoDAO.deleteProdutoId(produto);
                        Toast.makeText(getApplicationContext(),"Produto removido com sucesso!",Toast.LENGTH_SHORT).show();
                        inadimplencia.setTotal(inadimplencia.getTotal() - (produto.getQuantidade() * produto.getPreco()));
                        inadimplenciaDAO.setValorTotal(inadimplencia);
                        dialogProduto.dismiss();
                        dialog.dismiss();
                        gerarListaProdutos();
                    }

                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }public void dataPagamentoPopUp(){
        AlertDialog.Builder dialogBuilder;
        final AlertDialog dialog;
        dialogBuilder = new AlertDialog.Builder(this);
        final  View popupDataView = getLayoutInflater().inflate(R.layout.popup_data,null);
        dialogBuilder.setView(popupDataView);
        dialog = dialogBuilder.create();
        dialog.setTitle("Previsão para pagamento!");
        dialog.show();

        date = (EditText) popupDataView.findViewById(R.id.popup_data_edittext);
        date.addTextChangedListener(new TextWatcher() {
            String current = "";
            String ddmmyyyy = "DDMMYYYY";
            Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    }else{
                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                        cal.set(Calendar.MONTH, mon-1);
                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);

                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    date.setText(current);
                    date.setSelection(sel < current.length() ? sel : current.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        final Button salvar = popupDataView.findViewById(R.id.btn_pupup_data_dalvar);
        if (inadimplencia.getDataFim() != null){
            date.setText(converter.calendarToString(inadimplencia.getDataFim()).replaceAll("-","/"));
            salvar.setText("Atualizar");
        }
        salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Calendar calendar = converter.stringToCalendar(date.getText().toString().replaceAll("/","-"));
                    inadimplencia.setDataFim(calendar);
                    inadimplenciaDAO.setDataPagamentoInadimplencia(inadimplencia);
                    Toast.makeText(getApplicationContext(),"Data salva com sucesso!",Toast.LENGTH_SHORT).show();
                    textViewdataPagamento.setText(converter.calendarToString(inadimplencia.getDataFim()));
                    dialog.dismiss();
            }
        });
    }

}