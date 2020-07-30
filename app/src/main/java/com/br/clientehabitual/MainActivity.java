package com.br.clientehabitual;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.br.clientehabitual.banco.daos.ClienteDAO;
import com.br.clientehabitual.models.Cliente;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btnDelete, btnAtualizar;
    EditText id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void cadastroCliente(View v){
        Intent intent = new Intent(this, cadastro_clientes.class);
        startActivity(intent);
    }
    public void numeroClientes(){
        ClienteDAO dao = new ClienteDAO(getBaseContext());
        List<Cliente> clientes = dao.listaClientes();
        Toast.makeText(getApplicationContext(),"Numero de clientes: "+clientes.size(),Toast.LENGTH_LONG).show();
    }
}
