package com.br.clientehabitual;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.br.clientehabitual.banco.ClienteDAO;
import com.br.clientehabitual.models.Cliente;

public class cadastro_clientes extends AppCompatActivity {
    private EditText nome, email;
    Button btnCadastrar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_clientes);
    }
    public void cadastrarCliente(View v) {
        ClienteDAO clienteDAO = new ClienteDAO(getBaseContext());
        nome = (EditText)findViewById(R.id.txtNome);
        email =(EditText)findViewById(R.id.txtEmail);
        Cliente cliente = new Cliente(0,nome.getText().toString().trim(),
                email.getText().toString().trim());
        clienteDAO.cadastrarCliente(cliente);
    }
}