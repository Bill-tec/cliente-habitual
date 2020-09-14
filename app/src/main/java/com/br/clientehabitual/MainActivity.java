package com.br.clientehabitual;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.br.clientehabitual.adapters.ClienteAdapter;
import com.br.clientehabitual.banco.daos.ClienteDAO;
import com.br.clientehabitual.banco.daos.InadimplenciaDAO;
import com.br.clientehabitual.models.Cliente;
import com.br.clientehabitual.models.Inadimplencia;
import com.br.clientehabitual.util.Conversoes;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private EditText pesquisa, valorpopup, dataPagamento;
    private Button add;
    private CheckBox registrarProdutos;
    private ListView lista;
    private ArrayList<Cliente> clientes;
    private ArrayAdapter adapter;
    private Conversoes conversoes;
    private ClienteDAO clienteDAO = new ClienteDAO(this);
    private InadimplenciaDAO inadimplenciaDAO = new InadimplenciaDAO(this);
    private DecimalFormat df = new DecimalFormat("#0.00");
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
                    if (inadimplencia.isQuitada()){
                        startActivityCliente(clientes.get(position));
                    }
                    Calendar calendar = Calendar.getInstance();
                    if (calendar.getTime().after(inadimplencia.getDataFim().getTime()) && clientes.get(position).getEmail().length() > 0){
                        emailConfirm(inadimplencia);
                    } else {
                        startActivityCliente(clientes.get(position));
                    }
                } else{
                    startActivityCliente(clientes.get(position));
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
    public void startActivityCliente(Cliente cliente){
        Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
        intent.putExtra("id",Long.toString(cliente.getId()));
        startActivity(intent);
    }
    public void popupNovoCliente(){
        AlertDialog.Builder dialogBuilder;
        final AlertDialog dialog;
        dialogBuilder = new AlertDialog.Builder(this);
        final  View popupClienteView = getLayoutInflater().inflate(R.layout.popup_cliente,null);
        dialogBuilder.setView(popupClienteView);
        dialog = dialogBuilder.create();
        dialog.setTitle("Nova Inadimplêcia!");
        dialog.show();

        final EditText nome = (EditText)popupClienteView.findViewById(R.id.popupNome);
        final EditText email = (EditText)popupClienteView.findViewById(R.id.popupEmail);

        valorpopup = popupClienteView.findViewById(R.id.popup_cliente_valor);
        dataPagamento = popupClienteView.findViewById(R.id.popup_data_pagamento);

        final TextView sifrao = popupClienteView.findViewById(R.id.sifrao);

        add = popupClienteView.findViewById(R.id.btnProximo);

        registrarProdutos = popupClienteView.findViewById(R.id.check_listar);
        registrarProdutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (registrarProdutos.isChecked()){
                    valorpopup.setVisibility(View.INVISIBLE);
                    dataPagamento.setVisibility(View.INVISIBLE);
                    sifrao.setVisibility(View.INVISIBLE);
                    add.setText("Proximo");
                    dataPagamento.setText("");
                    valorpopup.setText("");
                } else {
                    valorpopup.setVisibility(View.VISIBLE);
                    dataPagamento.setVisibility(View.VISIBLE);
                    sifrao.setVisibility(View.VISIBLE);
                    add.setText("Salvar");
                    dataPagamento.addTextChangedListener(new TextWatcher() {
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
                                dataPagamento.setText(current);
                                dataPagamento.setSelection(sel < current.length() ? sel : current.length());
                            }


                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                }
            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cliente cliente = new Cliente(0, nome.getText().toString().trim(),
                        email.getText().toString().trim());
                if (add.getText().toString().equals("Proximo")){
                    if (cliente.getNome().length() == 0){
                        Toast.makeText(getApplicationContext(),"Preencha os campos, e tente novamente!",Toast.LENGTH_SHORT).show();
                    } else {
                        dialog.dismiss();
                        Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
                        intent.putExtra("id",Long.toString(clienteDAO.cadastrarCliente(cliente).getId()));
                        startActivity(intent);
                    }
                } else{
                    /* ADICIONANDO APENAS O VALOR DA INADIMPLÊCIA*/
                    conversoes = new Conversoes();
                    Calendar data = Calendar.getInstance();
                    Inadimplencia inadimplencia = new Inadimplencia(0, data, conversoes.stringToCalendar(dataPagamento.getText().toString().replaceAll("/","-")),
                            cliente, false, Float.parseFloat(valorpopup.getText().toString().trim()));
                    if (cliente.getNome().length() == 0 || inadimplencia.getDataFim() == null || inadimplencia.getTotal() <= 0){
                        Toast.makeText(getApplicationContext(),"Preencha os campos, e tente novamente!",Toast.LENGTH_SHORT).show();
                    } else {
                        dialog.dismiss();
                        cliente = clienteDAO.cadastrarCliente(cliente);
                        inadimplencia.setCliente(cliente);
                        inadimplencia = inadimplenciaDAO.newInadimplencia(inadimplencia);
                        inadimplenciaDAO.setDataPagamentoInadimplencia(inadimplencia);
                        Toast.makeText(getApplicationContext(),"Cliente cadastrado com sucesso!",Toast.LENGTH_LONG).show();
                    }
                }
                gerarListaClientes();
            }
        });
    }
    public void emailConfirm(final Inadimplencia inadimplencia){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Enviar E-mail para: "+inadimplencia.getCliente().getNome()+" ?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String assunto = "Referente a inadimplência";
                        String mensagem = "Caro cliente: "+ inadimplencia.getCliente().getNome() + " por meio desse E-mail " +
                                "estamos entrando em contato com você para avisar sobre sua divida de: " +
                                df.format(inadimplencia.getTotal()+"R$".replaceAll(".",",")) +" com data de pagamento expirada no dia: "
                                + (conversoes.calendarToString(inadimplencia.getDataFim()).replaceAll("-","/")) +
                                " pedimos que compareça ao nosso estabelecimento para esclarecimentos e se possivel quitar sua divida. Atenciosamente: ";

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setData(Uri.parse("mailto:"));
                        intent.setType("text/plain");

                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{inadimplencia.getCliente().getEmail()});
                        intent.putExtra(Intent.EXTRA_SUBJECT, assunto);
                        intent.putExtra(Intent.EXTRA_TEXT, mensagem);

                        try {
                            startActivity(Intent.createChooser(intent,"Enviar E-mail"));
                            Toast.makeText(getApplicationContext(), "E-mail enviado com sucesso!",Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }

                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivityCliente(inadimplencia.getCliente());
                    }
                }).show();
    }
}
