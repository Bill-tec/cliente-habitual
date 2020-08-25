package com.br.clientehabitual;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import java.text.DecimalFormat;
import java.util.Calendar;

public class ClienteActivity extends AppCompatActivity {
    private EditText nome, email,preco, quantidade;
    private Button adicionar, limpar;
    private TextView total;
    private Cliente cliente;
    private ClienteDAO clienteDAO = new ClienteDAO(this);
    private ProdutoDAO produtoDAO = new ProdutoDAO(this);
    private Inadimplencia inadimplencia;
    private InadimplenciaDAO inadimplenciaDAO = new InadimplenciaDAO(this);
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
        gerarListaProdutos();

        quantidade = (EditText)findViewById(R.id.produto_quantidade);
        nome = (EditText) findViewById(R.id.produto_nome);
        preco = (EditText) findViewById(R.id.produto_preco);

        adicionar = findViewById(R.id.btn_add);
        adicionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Produto produto = new Produto(0, nome.getText().toString().trim(),
                        Float.parseFloat(preco.getText().toString().trim()),
                        Integer.parseInt(quantidade.getText().toString().trim()));

                 if(produto.getNome().length() == 0 || produto.getQuantidade() <=0 || produto.getPreco() <= 0){
                    Toast.makeText(getApplicationContext(),"Erro, verifique os campos e tente novamente!",Toast.LENGTH_SHORT).show();
                } else {
                     if (inadimplencia == null){
                         Calendar data = Calendar.getInstance();
                         inadimplencia = new Inadimplencia(0,data, null,cliente, false);
                         inadimplencia = inadimplenciaDAO.newInadimplencia(inadimplencia);
                     }
                    produtoDAO.addProduto(inadimplencia.getId(),produto);
                }
            }
        });

        limpar = findViewById(R.id.btn_limpar);
        limpar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                inadimplencia = inadimplenciaDAO.getInadimpleciaCliente(cliente);
                if (inadimplencia != null){
                    Toast.makeText(getApplicationContext(),"Seu cliente ainda tem inadimplência ativa!",Toast.LENGTH_SHORT).show();
                }else {
                    confirmDeleteCliente();
                }
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
        ListView lista = findViewById(R.id.lst_produtos);
        if (inadimplencia != null){
            inadimplencia.setProdutos(produtoDAO.listProdutosInad(inadimplencia));
            ArrayAdapter adapter = new ProdutoAdapter(this,inadimplencia.getProdutos());
            lista.setAdapter(adapter);
            TextView total = findViewById(R.id.label_Total);
            double valorT = 0;
            if (inadimplencia.getProdutos().isEmpty()){
                total.setText("");
            }else {
                for (Produto p :inadimplencia.getProdutos()) {
                    valorT = valorT + (p.getQuantidade() * p.getPreco());
                }
                DecimalFormat df = new DecimalFormat("#.00");
                total.setText(df.format(valorT) + "R$");
            }
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
                    Toast.makeText(getApplicationContext(),"Produto atualizado com sucesso!",Toast.LENGTH_SHORT).show();
                    gerarListaProdutos();
                }
            }
        });

        Button remover = popupProdutoView.findViewById(R.id.popup_produto_btn_remover);
        remover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteProduto(produto);
            }
        });
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

    public void confirmDeleteProduto(final Produto produto){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Deseja remover: "+produto.getNome()+" ?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        produtoDAO.deleteProdutoId(produto);
                        Toast.makeText(getApplicationContext(),"Produto removido com sucesso!",Toast.LENGTH_SHORT).show();
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

}