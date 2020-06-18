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
    Button btnCadastrar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_clientes);
    }
    public void cadastrarCliente(View v) {
        ClienteDAO clienteDAO = new ClienteDAO(getBaseContext());
        EditText nome = (EditText)findViewById(R.id.txtNome);
        EditText email =(EditText)findViewById(R.id.txtEmail);
        Cliente cliente = new Cliente();
        cliente.setNome(nome.getText().toString().trim());
        cliente.setEmail(email.getText().toString().trim());
        boolean resultado = clienteDAO.cadastrarCliente(cliente);
        if (resultado) {
            Toast.makeText(getApplicationContext(), "Cliente cadastrado com sucesso!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Erro tente novamente!",
                    Toast.LENGTH_LONG).show();
        }
    }
}