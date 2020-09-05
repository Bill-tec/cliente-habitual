package com.br.clientehabitual;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.br.clientehabitual.adapters.ClienteAdapter;
import com.br.clientehabitual.banco.daos.ClienteDAO;
import com.br.clientehabitual.banco.daos.InadimplenciaDAO;
import com.br.clientehabitual.models.Cliente;
import com.br.clientehabitual.models.Inadimplencia;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText pesquisa;
    private ListView lista;
    private ArrayList<Cliente> clientes;
    private ArrayAdapter adapter;
    private ClienteDAO clienteDAO = new ClienteDAO(this);
    private InadimplenciaDAO inadimplenciaDAO = new InadimplenciaDAO(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button novaInadimplencia = findViewById(R.id.btn_nova_inadimplencia);
        novaInadimplencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupNovoCliente();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        gerarListaClientes();
    }
    public void gerarListaClientes(){
        clientes = clienteDAO.listaClientes();

        lista = findViewById(R.id.mainLista);
        adapter = new ClienteAdapter(this,clientes);
        lista.setTextFilterEnabled(true);
        lista.setAdapter(adapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Inadimplencia inadimplencia = inadimplenciaDAO.getInadimpleciaCliente(clientes.get(position));
                if (inadimplencia != null){
                    if (inadimplencia.getDataFim() != null && inadimplencia.isQuitada() && clientes.get(position).getEmail().length() > 0){
                        emailConfirm(clientes.get(position));
                    }
                } else{
                    Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
                    intent.putExtra("id",Long.toString(clientes.get(position).getId()));
                    startActivity(intent);
                }
            }
        });

        pesquisa = findViewById(R.id.mainPesquisa);
        pesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MainActivity.this.adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    public void popupNovoCliente(){
        AlertDialog.Builder dialogBuilder;
        final AlertDialog dialog;
        dialogBuilder = new AlertDialog.Builder(this);
        final  View popupClienteView = getLayoutInflater().inflate(R.layout.popup_cliente,null);
        dialogBuilder.setView(popupClienteView);
        dialog = dialogBuilder.create();
        dialog.setTitle("Novo Cliente!");
        dialog.show();

        final EditText nome = (EditText)popupClienteView.findViewById(R.id.popupNome);
        final EditText email = (EditText)popupClienteView.findViewById(R.id.popupEmail);

        Button add = popupClienteView.findViewById(R.id.btnProximo);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cliente cliente = new Cliente(0, nome.getText().toString().trim(),
                        email.getText().toString().trim());

                if (cliente.getNome().length() == 0 || cliente.getEmail().length() == 0){
                    Toast.makeText(getApplicationContext(),"Preencha os campos, e tente novamente!",Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
                    intent.putExtra("id",Long.toString(clienteDAO.cadastrarCliente(cliente)));
                    startActivity(intent);
                }
            }
        });
    }
    public void emailConfirm(final Cliente cliente){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Enviar E-mail para: "+cliente.getNome()+" ?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }

                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
    public void popupMandarEmail(){
        AlertDialog.Builder dialogBuilder;
        final AlertDialog dialog;
        dialogBuilder = new AlertDialog.Builder(this);
        final  View popupClienteView = getLayoutInflater().inflate(R.layout.popup_cliente,null);
        dialogBuilder.setView(popupClienteView);
        dialog = dialogBuilder.create();
        dialog.setTitle("Novo Cliente!");
        dialog.show();

        final EditText nome = (EditText)popupClienteView.findViewById(R.id.popupNome);
        final EditText email = (EditText)popupClienteView.findViewById(R.id.popupEmail);

        Button add = popupClienteView.findViewById(R.id.btnProximo);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cliente cliente = new Cliente(0, nome.getText().toString().trim(),
                        email.getText().toString().trim());

                if (cliente.getNome().length() == 0 || cliente.getEmail().length() == 0){
                    Toast.makeText(getApplicationContext(),"Preencha os campos, e tente novamente!",Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
                    intent.putExtra("id",Long.toString(clienteDAO.cadastrarCliente(cliente)));
                    startActivity(intent);
                }
            }
        });
    }
}
