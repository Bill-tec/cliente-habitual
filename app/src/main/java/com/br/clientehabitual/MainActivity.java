package com.br.clientehabitual;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.br.clientehabitual.banco.ClienteDAO;
import com.br.clientehabitual.models.Cliente;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ClienteDAO dao = new ClienteDAO(getBaseContext());
        List<Cliente> clientes = dao.listaClientes();
        Toast.makeText(getApplicationContext(),"Numero de clientes: "+clientes.size(),Toast.LENGTH_LONG).show();
    }
    public void cadastroCliente(View v){
        Intent intent = new Intent(this, cadastro_clientes.class);
        startActivity(intent);
    }
}
