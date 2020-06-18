package com.br.clientehabitual;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.br.clientehabitual.banco.BancoDeDados;

public class cadastro_clientes extends AppCompatActivity {
    Button btnCadastrar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_clientes);
    }
    public void cadastrarCliente(View v) {
        BancoDeDados bancoDeDados= new BancoDeDados(getBaseContext());
        EditText nome = (EditText)findViewById(R.id.txtNome);
        EditText email =(EditText)findViewById(R.id.txtEmail);
        boolean resultado = bancoDeDados.cadastrarCliente(nome.getText().toString(), email.getText().toString());
        if (resultado) {
            Toast.makeText(getApplicationContext(), "Cliente cadastrado com sucesso!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Erro tente novamente!",
                    Toast.LENGTH_LONG).show();
        }
    }
}