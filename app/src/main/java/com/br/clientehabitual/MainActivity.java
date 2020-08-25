package com.br.clientehabitual;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

import com.br.clientehabitual.banco.daos.ClienteDAO;
import com.br.clientehabitual.models.Cliente;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText pesquisa;
    private ListView lista;
    private ArrayList<Cliente> clientes;
    private ArrayAdapter<Cliente> adapter;
    private ClienteDAO clienteDAO = new ClienteDAO(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onResume() {
        super.onResume();
        gerarListaClientes();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_nova_inadimplencia:
                popupNovoCliente();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    public void gerarListaClientes(){
        clientes = clienteDAO.listaClientes();

        lista = findViewById(R.id.mainLista);
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,clientes);
        lista.setTextFilterEnabled(true);
        lista.setAdapter(adapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
                intent.putExtra("id",Long.toString(clientes.get(position).getId()));
                startActivity(intent);
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
}
